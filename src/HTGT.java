import java.io.*;
import java.util.prefs.*;
import java.util.regex.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.InputSource;

import java.lang.IndexOutOfBoundsException;

public class HTGT
{
	// Diverse fixe Konstanten für die Anwendung
	private static String    APPLICATION_NAME  = "HTGT"; // u.a. für cfg!
	private static String    APPLICATION_TITLE = "HTGT (HAPPYTEC Ghosttool)";
	private static String    APPLICATION_API   = "HAPPYTEC-eSports-API";
	private static String    APPLICATION_IDENT = "HTGT <https://github.com/froonix/happytec-ghosttool>";
	private static Dimension WINDOW_SIZE_START = new Dimension(800, 400);
	private static Dimension WINDOW_SIZE_MIN   = new Dimension(400, 200);

	// --- Standardpfade (oder über Registry auslesen?) ---
	// MAC: /Applications/SkiChallenge16.app/Contents/MacOS/SkiChallenge16.app/Contents/MacOS/Game_Data/OfflineProfiles.xml
	// LINUX: ~/.wine/drive_c/Games/Ski Challenge 16/Game_Data
	// WINDOWS: C:\\Games\\Ski Challenge 16\\Game_Data

	// Konfigurationsnamen für java.util.prefs
	private static String CFG_CWD   = "last-directory";
	private static String CFG_TOKEN = "esports-token";

	private static Preferences                cfg;
	private static File                       file;
	private static int                        profile;

	private static OfflineProfiles            OfflineProfiles;

	private static JFrame                     mainwindow;
	private static JTable                     maintable;
	private static DefaultTableModel          mainmodel;
	private static ArrayList<DynamicMenuItem> menuitems;

	public static void about()
	{
		JOptionPane.showMessageDialog(mainwindow, "HTML content with link?", APPLICATION_TITLE, JOptionPane.PLAIN_MESSAGE);
	}

	public static void main(String[] args) throws Exception
	{
		// Aktuell gibt es nur eine Konfiguration für den ganzen User-
		// account. Das heißt, dass mehrere unterschiedliche Bewerbe und
		// OfflineProfiles nicht möglich sind. Siehe GitHub Issue #7.
		cfg = Preferences.userRoot().node(APPLICATION_NAME);

		mainwindow = new JFrame(APPLICATION_TITLE);
		mainwindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainwindow.setJMenuBar(getMenubar());

		mainwindow.addWindowListener(new HTGT_WindowAdapter());

		Object rowData[][] = {};
		Object columnNames[] = {"Spieler", "Strecke", "Wetter", "Ergebnis"};

		mainmodel = new DefaultTableModel(rowData, columnNames);
		maintable = new HTGT_JTable(mainmodel);

		// Nur ganze Zeilen dürfen markiert werden!
		maintable.setColumnSelectionAllowed(false);
		maintable.setFocusable(false);

		// Spalten dürfen nicht verschoben oder verkleinert werden!
		maintable.getTableHeader().setReorderingAllowed(false);
		maintable.getTableHeader().setResizingAllowed(false);

		JScrollPane scrollPane = new JScrollPane(maintable);
		mainwindow.add(scrollPane, BorderLayout.CENTER);

		reset();

		mainwindow.setSize(WINDOW_SIZE_START);
		mainwindow.setMinimumSize(WINDOW_SIZE_MIN);
		mainwindow.setVisible(true);
	}

	private static JMenuBar getMenubar() throws Exception
	{
		JMenuBar menu = new JMenuBar();

		menu.add(getMenu("file"));
		menu.add(getMenu("edit"));
		menu.add(getMenu("view"));
		menu.add(getMenu("api"));
		menu.add(getMenu("help"));
		disableMenuItems();

		return menu;
	}

	private static JMenu getMenu(String key) throws Exception
	{
		String title;
		switch(key)
		{
			case "file": title = "Datei";      break;
			case "edit": title = "Bearbeiten"; break;
			case "view": title = "Ansicht";    break;
			case "api":  title = "Server";     break;
			case "help": title = "Hilfe";      break;

			default: throw new Exception(String.format("Unknown menu »%s«", key));
		}

		JMenu menu = new JMenu(title);

		switch(key)
		{
			case "file":
				menu.add(new DynamicMenuItem("Öffnen",                  HTGT.class.getName(), "openFile"));
				menu.addSeparator(); // --------------------------------
				menu.add(registerDynMenuItem("Speichern",               HTGT.class.getName(), "saveFile"));
				menu.add(registerDynMenuItem("Speichern unter",         HTGT.class.getName(), "saveFileAs"));
				menu.addSeparator(); // --------------------------------
				menu.add(registerDynMenuItem("Schließen",               HTGT.class.getName(), "closeFile"));
				menu.add(new DynamicMenuItem("Beenden",                 HTGT.class.getName(), "quit"));
				break;

			case "edit":
				menu.add(registerDynMenuItem("Ausschneiden",            HTGT.class.getName(), "cutToClipboard"));
				menu.add(registerDynMenuItem("Kopieren",                HTGT.class.getName(), "copyToClipboard"));
				menu.add(registerDynMenuItem("Einfügen",                HTGT.class.getName(), "copyFromClipboard"));
				menu.add(registerDynMenuItem("Löschen",                 HTGT.class.getName(), "deleteRows"));
				break;

			case "view":
				menu.add(registerDynMenuItem("Profil auswählen",        HTGT.class.getName(), "selectProfile"));
				menu.addSeparator(); // --------------------------------
				menu.add(registerDynMenuItem("Aktualisieren",           HTGT.class.getName(), "reloadFile"));
				break;

			case "api":
				menu.add(registerDynMenuItem("Geist hochladen",         HTGT.class.getName(), "ghostUpload"));
				menu.add(registerDynMenuItem("Geist herunterladen",     HTGT.class.getName(), "ghostDownload"));
				menu.addSeparator(); // --------------------------------
				menu.add(new DynamicMenuItem("API-Token ändern",        HTGT.class.getName(), "setupToken"));
				menu.add(new DynamicMenuItem("API-Token löschen",       HTGT.class.getName(), "deleteToken"));
				break;

			case "help":
				menu.add(new DynamicMenuItem("Über",                    HTGT.class.getName(), "about"));
				break;
		}

		return menu;
	}

	private static JMenuItem registerDynMenuItem(String t, String c, String m)
	{
		if(menuitems == null)
		{
			menuitems = new ArrayList<DynamicMenuItem>();
		}

		DynamicMenuItem DMI = new DynamicMenuItem(t, c, m);
		menuitems.add(DMI);
		return DMI;
	}

	private static void disableMenuItems()
	{
		changeMenuItems(false);
	}

	private static void enableMenuItems()
	{
		changeMenuItems(true);
	}

	private static void changeMenuItems(boolean e)
	{
		if(menuitems != null)
		{
			for(int i = 0; i < menuitems.size(); i++)
			{
				menuitems.get(i).setEnabled(e);
			}
		}
	}

	private static void reset()
	{
		profile = 0; file = null;
		OfflineProfiles = null;

		syncGUI();
	}

	private static void clearTable()
	{
		mainmodel.setRowCount(0);
	}

	private static void hideTableHeader()
	{
		// Das ist ein sehr schmutziger Hack...
		maintable.getTableHeader().setUI(null);
	}

	private static void showTableHeader()
	{
		// Und das ist eine noch viel unschönere Lösung...
		maintable.getTableHeader().setUI(new BasicTableHeaderUI());
	}

	private static void highlightLastRow()
	{
		highlightLastRows(1);
	}

	private static void highlightLastRows(int num)
	{
		if(num < 1)
		{
			throw new IndexOutOfBoundsException(String.format("%d < 1", num));
		}

		int row = mainmodel.getRowCount();
		highlightRows(row - num, row - 1);
	}

	private static void highlightRows(int start, int end)
	{
		maintable.clearSelection();
		maintable.addRowSelectionInterval(start, end);
	}

	public static void updateWindowTitle()
	{
		String filename = "";
		String suffix = "";

		if(OfflineProfiles != null)
		{
			filename = " – " + file.getAbsolutePath();

			if(OfflineProfiles.changed())
			{
				suffix = " *";
			}
		}

		mainwindow.setTitle(APPLICATION_TITLE + filename + suffix);
	}

	public static void ghostImport(String xmlstring)
	{
		int i = 0;
		try
		{
			Pattern pattern = Pattern.compile("(<GhostDataPair[^>]+>)", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(xmlstring);

			String xmltag;
			while(matcher.find())
			{
				xmltag = matcher.group(1);

				GhostElement ghostElement = new GhostElement(xmltag);
				addGhost(ghostElement, true);
				ghostElement.printDetails();

				i++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		JOptionPane.showMessageDialog(null, "Importierte Geister: " + i);

		if(i > 0)
		{
			highlightLastRows(i);
		}
	}

	public static void selectProfile()
	{
		//if(!fileLoaded())
		//{
		//	return;
		//}

		// TODO: Zuletzt genutzes Profil in CFG abspeichern und auslesen
		// ...

		String selection = null;
		String[] profiles;
		String[] values;

		try
		{
			profiles = OfflineProfiles.getProfiles();
			values = new String[profiles.length];
		}
		catch(Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(mainwindow, "Eines der Profile enthält Fehler.", null, JOptionPane.ERROR_MESSAGE);
			return;
		}

		for(int i = 0; i < profiles.length; i++)
		{
			values[i] = String.format("[%02d] %s", i + 1, profiles[i]);

			if(profile == i)
			{
				selection = values[i];
			}
		}

		String input = (String)JOptionPane.showInputDialog(
			mainwindow,
			"Aktuell genutztes Profil aus der XML-Datei:",
			"Profilauswahl",
			JOptionPane.PLAIN_MESSAGE,
			null,
			values,
			selection
		);

		if(input == null)
		{
			System.out.println("selectProfile: CANCEL");
			return;
		}
		else
		{
			int selected = 0;
			String value = (String) input;

			for(int i = 0; i < values.length; i++)
			{
				if(values[i].equals(value))
				{
					selected = i;
					break;
				}
			}

			selectProfile(selected);
		}
	}

	public static void selectProfile(int index)
	{
		if(OfflineProfiles == null)
		{
			return;
		}

		if(index >= OfflineProfiles.getProfileCount())
		{
			index = 0;
		}

		if(OfflineProfiles.getProfileCount() > 0)
		{
			OfflineProfiles.selectProfile(index);
		}

		profile = index;
		syncGUI();
	}

	public static void syncGUI()
	{
		updateWindowTitle();
		hideTableHeader();
		clearTable();

		if(OfflineProfiles != null && OfflineProfiles.getGhostCount() > 0)
		{
			showTableHeader();

			for(int i = 0; i < OfflineProfiles.getGhostCount(); i++)
			{
				addGhost(OfflineProfiles.getGhost(i), false);
			}
		}
	}

	public static void addGhost(GhostElement ghost, boolean create)
	{
		if(create)
		{
			try
			{
				OfflineProfiles.addGhost(ghost);
				updateWindowTitle();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(mainwindow, "Fehler beim Hinzufügen des Geists!", null, JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		Object tmp[] = { ghost.getNickname(), ghost.getTrackName(), ghost.getWeatherName(), ghost.getResult() };
		mainmodel.addRow(tmp);
	}

	public static void deleteGhost(int index)
	{
		if(index >= OfflineProfiles.getGhostCount())
		{
			throw new IndexOutOfBoundsException(String.format("Ghost #%d", index));
		}

		try
		{
			OfflineProfiles.deleteGhost(index);
			mainmodel.removeRow(index);
			updateWindowTitle();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

/***********************************************************************
 *                          CLIPBOARD ACTIONS                          *
 ***********************************************************************/

	public static void cutToClipboard()
	{
		rowsAction(true, true);
	}

	public static void copyToClipboard()
	{
		rowsAction(true, false);
	}

	public static void deleteRows()
	{
		rowsAction(false, true);
	}

	private static void rowsAction(boolean copy, boolean delete)
	{
		StringBuilder data = new StringBuilder();
		int[] selection = maintable.getSelectedRows();

		for(int i = selection.length - 1; i > -1; i--)
		{
			int row = selection[i];

			if(copy)
			{
				data.insert(0, OfflineProfiles.getGhost(row).toString());
			}

			if(delete)
			{
				deleteGhost(row);
			}
		}

		System.out.println(data.toString());

		if(copy)
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data.toString()), null);
		}

		if(delete)
		{
			updateWindowTitle();
		}
	}

	public static void copyFromClipboard()
	{
		try
		{
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable transferData = systemClipboard.getContents(null);

			for(DataFlavor dataFlavor : transferData.getTransferDataFlavors())
			{
				Object content = transferData.getTransferData(dataFlavor);

				if(content instanceof String)
				{
					ghostImport(content.toString());
					break;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

/***********************************************************************
 *                             API ACTIONS                             *
 ***********************************************************************/

	public static void deleteToken()
	{
		cfg(CFG_TOKEN, null);
		JOptionPane.showMessageDialog(mainwindow, "Dein Zugangsschlüssel wurde aus der lokalen Konfiguration gelöscht!\n\nDu kannst ihn über das Menü jederzeit erneut eintragen.", APPLICATION_API, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void setupToken()
	{
		while(true)
		{
			Object input = JOptionPane.showInputDialog(
				mainwindow,
				"Bitte gib deinen persönlichen Zugriffsschlüssel ein:",
				APPLICATION_API,
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				cfg(CFG_TOKEN)
			);

			if(input == null)
			{
				System.out.println("setupToken: CANCEL");
				return;
			}

			String token = String.format("%s", input).trim().toLowerCase();
			Pattern p = Pattern.compile("^[A-F0-9]+$", Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(token);

			if(token.length() == 0)
			{
				System.out.println("setupToken: EMPTY");
				continue;
			}
			else if(!m.find())
			{
				System.out.println("setupToken: INVALID");
				continue;
			}
			else
			{
				System.out.printf("setupToken: VALUE (%d)\n", token.length());
				System.out.printf("New API token: %s\n", token);
				cfg(CFG_TOKEN, token);
				return;
			}
		}
	}

	public static void ghostUpload()
	{
		// ...
		// ...
	}

	public static void ghostDownload()
	{
		String token = cfg(CFG_TOKEN);
		if(token == null || token.equals(""))
		{
			setupToken(); token = cfg(CFG_TOKEN);
			if(token == null || token.equals(""))
			{
				return;
			}
		}

		eSportsAPI api = new eSportsAPI(token, APPLICATION_IDENT);
		String last_input = "";

		while(true)
		{
			Object input = JOptionPane.showInputDialog(
				mainwindow,
				"Um einen Geist vom Server herunterzuladen, trage einfach die Ghost-ID ein:",
				"Geist herunterladen",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				last_input
			);

			if(input == null)
			{
				System.out.println("ghostDownload: CANCEL");
				return;
			}

			int id = FNX.intval(input.toString());
			last_input = Integer.toString(id);

			if(id <= 0)
			{
				System.out.println("ghostDownload: NULL");
				continue;
			}
			else
			{
				System.out.printf("ghostDownload: ID (%d)\n", id);

				// ladegrafik? oder wenigstens ein ladedialog?
				// ...

				String ghostdata = api.getGhostByID(id);

				if(ghostdata == null)
				{
					// TODO: Get errmsg from API!
					JOptionPane.showMessageDialog(null, "Download fehlgeschlagen...");
					continue;
				}

				addGhost(new GhostElement(ghostdata), true);
				JOptionPane.showMessageDialog(null, "Erledigt!");

				highlightLastRow();

				return;
			}
		}
	}

/***********************************************************************
 *                            FILE ACTIONS                             *
 ***********************************************************************/

	public static void openFile()
	{
		if(!closeFile())
		{
			return;
		}

		JFileChooser chooser = new JFileChooser(cfg(CFG_CWD));

		FileFilter filter = new FileNameExtensionFilter("XML-Dateien", "xml");
		chooser.addChoosableFileFilter(filter); chooser.setFileFilter(filter);

		int code = chooser.showOpenDialog(null);

		if(code == JFileChooser.APPROVE_OPTION)
		{
			System.err.println("JFileChooser: APPROVE_OPTION");
			File tmp = chooser.getSelectedFile();

			if(tmp.exists())
			{
				System.err.println("XML inputfilename: " + tmp.getAbsolutePath());
				cfg(CFG_CWD, String.format("%s", tmp.getParent()));

				try
				{
					OfflineProfiles = new OfflineProfiles(tmp);
					file = tmp;

					selectProfile(0);
					updateWindowTitle();
					enableMenuItems();
				}
				catch(Exception e)
				{
					reset();

					e.printStackTrace();
					JOptionPane.showMessageDialog(mainwindow, "Fehler beim Laden der XML-Datei!", null, JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if(code == JFileChooser.CANCEL_OPTION)
		{
			System.err.println("JFileChooser: CANCEL_OPTION");
		}
		else if(code == JFileChooser.ERROR_OPTION)
		{
			System.err.println("JFileChooser: ERROR_OPTION");
		}

		// todo: listener für dateiänderungen
		// ...
	}

	public static void reloadFile()
	{
		if(unsavedChanges())
		{
			return;
		}

		try
		{
			OfflineProfiles.reload();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		selectProfile(profile);
		syncGUI();
	}

	public static boolean saveFile()
	{
		return saveFile(false);
	}

	public static boolean saveFile(boolean force)
	{
		if(OfflineProfiles == null)
		{
			return false;
		}

		if(force || OfflineProfiles.changed())
		{
			try
			{
				PrintWriter tmp = new PrintWriter(file);
				tmp.printf("%s", OfflineProfiles.toString());
				tmp.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				return false;
			}

			OfflineProfiles.saved();
			updateWindowTitle();
		}

		return true;
	}

	// TODO: Noch nicht fertig!
	// TODO: XML-Filter auslagern!
	public static void saveFileAs()
	{
		if(OfflineProfiles == null)
		{
			return;
		}




		JFileChooser chooser = new ImprovedFileChooser(file.getParent());

		FileFilter filter = new FileNameExtensionFilter("XML-Dateien", "xml");
		chooser.addChoosableFileFilter(filter); chooser.setFileFilter(filter);
		chooser.setSelectedFile(file);

		int code = chooser.showSaveDialog(null);

		if(code == JFileChooser.APPROVE_OPTION)
		{
			System.err.println("saveFileAs: APPROVE_OPTION");
			File tmp = chooser.getSelectedFile();

			if(tmp != null)
			{
				System.err.println("XML outputfilename: " + tmp.getAbsolutePath());
				cfg(CFG_CWD, String.format("%s", tmp.getAbsolutePath()));

				try
				{
					file = tmp;
					saveFile(true);
					OfflineProfiles.updateFile(file);
					return;
				}
				catch(Exception e)
				{
					e.printStackTrace();
					// OfflineProfiles File?
				}
			}
		}
		else if(code == JFileChooser.CANCEL_OPTION)
		{
			System.err.println("saveFileAs: CANCEL_OPTION");
		}
		else if(code == JFileChooser.ERROR_OPTION)
		{
			System.err.println("saveFileAs: ERROR_OPTION");
		}
	}

	public static boolean closeFile()
	{
		if(unsavedChanges())
		{
			return false;
		}

		disableMenuItems();
		reset();

		return true;
	}

	public static boolean unsavedChanges()
	{
		if(OfflineProfiles != null && OfflineProfiles.changed())
		{
			int input = JOptionPane.showConfirmDialog(mainwindow,
				"Die Änderungen wurden nicht gespeichert! Trotzdem fortfahren?",
				"Änderungen verwerfen?",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE
			);

			if(input == JOptionPane.NO_OPTION)
			{
				System.out.println("quit: NO");
				return true;
			}
			else
			{
				System.out.println("quit: YES");
			}
		}

		return false;
	}

	public static void quit()
	{
		if(closeFile())
		{
			System.exit(0);
		}
	}

/***********************************************************************
 *                        CONFIGURATION HELPER                         *
 ***********************************************************************/

	// Konfiguration "key" auslesen.
	private static String cfg(String key)
	{
		// return cfg.get(key, "");
		return cfg.get(key, null);
	}

	// Setze Konfiguration "key" auf den Wert "value".
	// Mit NULL als Wert wird die Konfiguration gelöscht!
	// Es wird immer der neue gesetzte Wert zurückgegeben.
	private static String cfg(String key, String value)
	{
		if(value == null)
		{
			cfg.remove(key);
			return null;
		}

		cfg.put(key, value);
		return cfg(key);
	}
}

class HTGT_JTable extends JTable
{
	DefaultTableCellRenderer renderLeft;
	DefaultTableCellRenderer renderCenter;
	DefaultTableCellRenderer renderRight;

	public HTGT_JTable(TableModel dm)
	{
		super(dm);

		this.renderLeft = new DefaultTableCellRenderer();
		renderLeft.setHorizontalAlignment(SwingConstants.LEFT);

		this.renderCenter = new DefaultTableCellRenderer();
		renderCenter.setHorizontalAlignment(SwingConstants.CENTER);

		this.renderRight = new DefaultTableCellRenderer();
		renderRight.setHorizontalAlignment(SwingConstants.RIGHT);
	}

	public TableCellRenderer getCellRenderer(int row, int column)
	{
		if(column > 0)
		{
			return renderRight;
		}
		else
		{
			return renderLeft;
		}
	}

	public boolean isCellEditable(int row, int column)
	{
		return false;
	};
}

class HTGT_WindowAdapter extends java.awt.event.WindowAdapter
{
	public void windowClosing(java.awt.event.WindowEvent windowEvent)
	{
		HTGT.quit();
	}
}

// TODO: Alle Funktionen, die normalerweise ausgegraut werden,
//       müssen prüfen, ob OfflineProfiles gerade geladen ist!
// ...
