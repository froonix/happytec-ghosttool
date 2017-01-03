import java.io.*;
import java.util.*;
import java.util.prefs.*;
import java.util.regex.*;

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
	final private static String    APPLICATION_VERSION = "0.0.0-alpha2";
	final private static String    APPLICATION_NAME    = "HTGT"; // cfg, updates, …
	final private static String    APPLICATION_TITLE   = "HTGT (HAPPYTEC Ghosttool)";
	final private static String    APPLICATION_API     = "HAPPYTEC-eSports-API";
	final private static String    APPLICATION_IDENT   = "HTGT <https://github.com/froonix/happytec-ghosttool>";
	final private static Dimension WINDOW_SIZE_START   = new Dimension(800, 400);
	final private static Dimension WINDOW_SIZE_MIN     = new Dimension(400, 200);
	final private static long      UPDATE_INTERVAL     = 86400000L; // daily

	// --- Standardpfade (oder über Registry auslesen?) ---
	// MAC: /Applications/SkiChallenge16.app/Contents/MacOS/SkiChallenge16.app/Contents/MacOS/Game_Data/OfflineProfiles.xml
	// LINUX: ~/.wine/drive_c/Games/Ski Challenge 16/Game_Data
	// WINDOWS: C:\\Games\\Ski Challenge 16\\Game_Data

	// Konfigurationsnamen für java.util.prefs
	private static String CFG_UC      = "update-check";
	private static String CFG_TOKEN   = "esports-token";
	private static String CFG_CWD     = "last-directory";
	private static String CFG_WEATHER = "last-weather";
	private static String CFG_TRACK   = "last-track";

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
		String licence = String.format(
			  "    Copyright (C) 2017 Christian Schrötter <cs@fnx.li>%n%n"
			+ "    This program is free software; you can redistribute it and/or modify%n"
			+ "    it under the terms of the GNU General Public License as published by%n"
			+ "    the Free Software Foundation; either version 3 of the License, or%n"
			+ "    (at your option) any later version.%n%n"
			+ "    This program is distributed in the hope that it will be useful,%n"
			+ "    but WITHOUT ANY WARRANTY; without even the implied warranty of%n"
			+ "    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the%n"
			+ "    GNU General Public License for more details.%n%n"
			+ "    You should have received a copy of the GNU General Public License%n"
			+ "    along with this program; if not, write to the Free Software Foundation,%n"
			+ "    Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA"
		);

		String line = ""; // "----------------------------------------------------------------------------------------------------"
		messageDialog(APPLICATION_TITLE, String.format("Application: %s%nVersion: %s%n%nWebsite: https://github.com/froonix/happytec-ghosttool%n%s by www.esports.happytec.at%n%n%s%n%n%s%n%n%s%n%n", APPLICATION_NAME, APPLICATION_VERSION, APPLICATION_API, line, licence, line));
	}

	public static void updateCheck()
	{
		updateCheck(true, true);
	}

	public static void updateCheck(boolean force, boolean msg)
	{
		if(APPLICATION_VERSION.toUpperCase().startsWith("GIT-"))
		{
			if(msg)
			{
				messageDialog(JOptionPane.INFORMATION_MESSAGE, null, "Du verwendest eine Entwicklerversion, da macht eine Updateprüfung keinen Sinn.");
			}

			return;
		}

		long lastUpdateCheck;
		Date date = new Date();

		if(cfg(CFG_UC) == null)
		{
			lastUpdateCheck = 0L;
		}
		else
		{
			lastUpdateCheck = Long.parseLong(cfg(CFG_UC));
		}

		System.out.printf("Last update check: %d%n", lastUpdateCheck);
		if(lastUpdateCheck <= 0 || date.getTime() > (lastUpdateCheck + UPDATE_INTERVAL))
		{
			cfg(CFG_UC, Objects.toString(date.getTime(), null));
			force = true;
		}

		if(force)
		{
			eSportsAPI api = new eSportsAPI(null, APPLICATION_IDENT);
			int updates = api.updateAvailable(APPLICATION_NAME, APPLICATION_VERSION);

			if(updates == 1)
			{
				messageDialog(JOptionPane.INFORMATION_MESSAGE, null, String.format("Es ist ein Update verfügbar!%n%nBitte besuche die Website, um es herunterzuladen."));
			}
			else if(updates == 0)
			{
				if(msg)
				{
					messageDialog(JOptionPane.INFORMATION_MESSAGE, null, "Es gibt keine Updates, du verwendest bereits die aktuellste Version.");
				}
			}
			else
			{
				if(msg)
				{
					APIError(api, "Updateprüfung fehlgeschlagen!");
				}
			}
		}
	}

	public static void main(String[] args) throws Exception
	{
		if(args.length > 0 && args[0].equals("-v"))
		{
			System.out.println(APPLICATION_VERSION);
			System.exit(0);
		}

		if(!confirmDialog(JOptionPane.PLAIN_MESSAGE, APPLICATION_TITLE, String.format("Dieses Programm befindet sich noch in der Entwicklungs-/Testphase! Die Verwendung erfolgt auf eigene Gefahr.%n%nDer Autor übernimmt keine Haftung für Schäden, die direkt oder indirekt durch dieses Programm verursacht wurden.%nBitte erstelle selbst Backups deiner OfflineProfiles.xml XML-Datei(en), bevor du diese in diesem Programm öffnest.%n%nWillst du wirklich fortfahren?")))
		{
			System.exit(0);
		}

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

		// ...
		updateCheck(false, false);
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
				menu.add(new DynamicMenuItem("Öffnen",                            HTGT.class.getName(), "openFile"));
				menu.addSeparator(); // --------------------------------
				menu.add(registerDynMenuItem("Speichern",                         HTGT.class.getName(), "saveFile"));
				menu.add(registerDynMenuItem("Speichern unter",                   HTGT.class.getName(), "saveFileAs"));
				menu.addSeparator(); // --------------------------------
				menu.add(registerDynMenuItem("Schließen",                         HTGT.class.getName(), "closeFile"));
				menu.add(new DynamicMenuItem("Beenden",                           HTGT.class.getName(), "quit"));
				break;

			case "edit":
				menu.add(registerDynMenuItem("Ausschneiden",                      HTGT.class.getName(), "cutToClipboard"));
				menu.add(registerDynMenuItem("Kopieren",                          HTGT.class.getName(), "copyToClipboard"));
				menu.add(registerDynMenuItem("Einfügen",                          HTGT.class.getName(), "copyFromClipboard"));
				menu.add(registerDynMenuItem("Löschen",                           HTGT.class.getName(), "deleteRows"));
				break;

			case "view":
				menu.add(registerDynMenuItem("Profil auswählen",                  HTGT.class.getName(), "selectProfile"));
				menu.addSeparator(); // --------------------------------
				menu.add(registerDynMenuItem("Aktualisieren",                     HTGT.class.getName(), "reloadFile"));
				break;

			case "api":
				menu.add(registerDynMenuItem("Geister hochladen",                 HTGT.class.getName(), "ghostUpload"));
				menu.add(registerDynMenuItem("Geister durch ID(s) herunterladen", HTGT.class.getName(), "ghostDownload"));
				menu.add(registerDynMenuItem("Geist auswählen und herunterladen", HTGT.class.getName(), "ghostSelect"));
				menu.addSeparator(); // --------------------------------
				menu.add(new DynamicMenuItem("Spieler-/Bewerbsdetails",           HTGT.class.getName(), "playerInfo"));
				menu.addSeparator(); // --------------------------------
				menu.add(new DynamicMenuItem("API-Token ändern",                  HTGT.class.getName(), "setupToken"));
				menu.add(new DynamicMenuItem("API-Token löschen",                 HTGT.class.getName(), "deleteToken"));
				break;

			case "help":
				menu.add(new DynamicMenuItem("Updateprüfung",                     HTGT.class.getName(), "updateCheck"));
				menu.addSeparator(); // --------------------------------
				menu.add(new DynamicMenuItem("Über diese App",                    HTGT.class.getName(), "about"));
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
		if(OfflineProfiles == null || !confirmDialog(JOptionPane.WARNING_MESSAGE, null, String.format("Es kann nur einen aktiven Geist pro Strecken/Wetter Kombination in einem Profil geben.%nBeim Import werden andere eventuell vorhandene Geister ohne Rückfrage gelöscht!%n%nBist du sicher, dass du fortfahren möchtest?")))
		{
			return;
		}

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

				int[] ghosts = OfflineProfiles.getGhostsByCondition(ghostElement);
				for(int h = ghosts.length - 2; h > -1; h--)
				{
					deleteGhost(ghosts[h]);
				}

				i++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// messageDialog(null, "Importierte Geister: " + i);

		/*
		if(i > 0)
		{
			highlightLastRows(i);
		}
		*/
	}

	public static void selectProfile()
	{
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
			errorMessage("Eines der Profile enthält Fehler.");
			return;
		}

		for(int i = 0; i < profiles.length; i++)
		{
			values[i] = String.format("[%0" + Integer.toString(FNX.strlen(profiles.length)) + "d] %s%s", i + 1, profiles[i], ((i == OfflineProfiles.defaultProfile()) ? " (Standardprofil)" : ""));

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
				errorMessage("Fehler beim Hinzufügen des Geists!");
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

	private static boolean confirmDialog(String title, String msg)
	{
		return confirmDialog(JOptionPane.QUESTION_MESSAGE, title, msg);
	}

	private static boolean confirmDialog(int type, String title, String msg)
	{
		if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mainwindow, msg, title, JOptionPane.YES_NO_OPTION, type))
		{
			return true;
		}

		return false;
	}

	private static void errorMessage(String msg)
	{
		errorMessage("Fehler", msg);
	}

	private static void errorMessage(String title, String msg)
	{
		messageDialog(JOptionPane.ERROR_MESSAGE, title, msg);
	}

	private static void messageDialog(String title, String msg)
	{
		messageDialog(JOptionPane.PLAIN_MESSAGE, title, msg);
	}

	private static void messageDialog(int type, String title, String msg)
	{
		JOptionPane.showMessageDialog(mainwindow, msg, title, type);
	}

	private static void noSelection()
	{
		messageDialog(null, String.format("Die gewünschte Aktion funktioniert nur, wenn bereits Geister ausgewählt wurden.%n%nMarkiere eine Zeile mit der Maus, eine Mehrfachauswahl ist durch Halten der Strg/Ctrl Taste möglich."));
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
		if(OfflineProfiles == null)
		{
			return;
		}

		StringBuilder data = new StringBuilder();
		int[] selection = maintable.getSelectedRows();

		if(selection.length == 0)
		{
			noSelection();
			return;
		}

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
		if(OfflineProfiles == null)
		{
			return;
		}

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

	private static void APIError(eSportsAPI api, String msg)
	{
		errorMessage(APPLICATION_API, String.format("%s%n%nFehlercode: %s%n%s", msg, api.getErrorCode(), api.getErrorMessage()).trim());
	}

	public static void deleteToken()
	{
		cfg(CFG_TOKEN, null);
		messageDialog(JOptionPane.INFORMATION_MESSAGE, APPLICATION_API, String.format("Dein Zugangsschlüssel wurde aus der lokalen Konfiguration gelöscht!%n%nDu kannst ihn über das Menü jederzeit erneut eintragen."));
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
				System.out.printf("setupToken: VALUE (%d)%n", token.length());
				System.out.printf("New API token: %s%n", token);
				cfg(CFG_TOKEN, token);
				return;
			}
		}
	}

	private static String getToken()
	{
		String token = cfg(CFG_TOKEN);
		if(token == null || token.equals(""))
		{
			setupToken(); token = cfg(CFG_TOKEN);
			if(token == null || token.equals(""))
			{
				return null;
			}
		}

		return token;
	}

	public static void ghostUpload()
	{
		if(OfflineProfiles == null)
		{
			return;
		}

		int[] selection = maintable.getSelectedRows();

		if(selection.length == 0)
		{
			noSelection();
			return;
		}

		/*
		if(!confirmDialog(APPLICATION_API, String.format("Sollen die markierten Geister (%d) wirklich zum Server hochgeladen werden?%n%nDie Ergebnisse werden bei dieser Aktion automatisch für deinen Spieler übernommen!", selection.length)))
		{
			return;
		}
		*/

		GhostElement[] ghosts = new GhostElement[selection.length];
		for(int i = 0; i < selection.length; i++)
		{
			ghosts[i] = OfflineProfiles.getGhost(selection[i]);
		}

		String token = getToken();
		if(token == null)
		{
			return;
		}

		eSportsAPI api = new eSportsAPI(token, APPLICATION_IDENT);
		int[] ghostIDs = api.getGhostIDs(ghosts);

		if(ghostIDs == null)
		{
			APIError(api, "Upload fehlgeschlagen...");
			return;
		}
		else if(ghostIDs.length != selection.length)
		{
			errorMessage("ghostIDs.length != selection.length");
			return;
		}

		for(int i = 0; i < ghostIDs.length; i++)
		{
			GhostElement ghost = ghosts[i];
			if(!confirmDialog(APPLICATION_API, String.format("Willst du das nachfolgende Ergebnis wirklich in die Rangliste eintragen?%n%nNickname: %s%nStrecke: %s (%s)%nErgebnis: %s", ghost.getNickname(), ghost.getTrackName(), ghost.getWeatherName(), ghost.getResult())))
			{
				continue;
			}

			if(!api.applyResultByGhostID(ghostIDs[i]))
			{
				System.out.printf("Hochgeladener Geist: ID %d (Übernahme fehlgeschlagen)%n", ghostIDs[i]);
				APIError(api, String.format("Der Geist mit der ID %d konnte nicht übernommen werden!", ghostIDs[i]));
			}
			else
			{
				System.out.printf("Hochgeladener Geist: ID %d (erfolgreich übernommen)%n", ghostIDs[i]);
				messageDialog(JOptionPane.INFORMATION_MESSAGE, APPLICATION_API, String.format("Der Geist mit der ID %d wurde erfolgreich eingetragen!%n%nEs wird jedoch einige Minuten dauern, bis die Rangliste aktualisiert wird.", ghostIDs[i]));
			}
		}
	}

	public static void ghostDownload()
	{
		if(OfflineProfiles == null)
		{
			return;
		}

		String token = getToken();
		if(token == null)
		{
			return;
		}

		String last_input = "";

		while(true)
		{
			Object input = JOptionPane.showInputDialog(
				mainwindow,
				"Um einen Geist vom Server herunterzuladen, trage einfach die Ghost-ID ein:",
				APPLICATION_API,
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

			ArrayList<Integer> ids = new ArrayList<Integer>(0);
			String[] parts = input.toString().split("[^0-9]+");

			for(int i = 0; i < parts.length; i++)
			{
				int id = FNX.intval(parts[i].trim());

				if(id > 0)
				{
					ids.add(id);
				}
			}

			// ladegrafik? oder wenigstens ein ladedialog?
			// ...

			if(!ghostDownload(ids.stream().mapToInt(i -> i).toArray()))
			{
				continue;
			}

			return;
		}
	}

	public static boolean ghostDownload(int id)
	{
		int[] ids = new int[1];
		ids[0] = id;

		return ghostDownload(ids);
	}

	public static boolean ghostDownload(int[] ids)
	{
		String token = getToken();
		if(token == null)
		{
			return false;
		}

		Integer[] id;
		String ghostdata;

		eSportsAPI api = new eSportsAPI(token, APPLICATION_IDENT);

		if(ids.length == 0)
		{
			System.out.println("ghostDownload: NULL");
			return false;
		}
		else if(ids.length == 1)
		{
			System.out.println("ghostDownload: single ID");
			ghostdata = api.getGhostByID(ids[0]);
		}
		else
		{
			System.out.printf("ghostDownload: IDs (%d)", ids.length);
			ghostdata = api.getGhostsByIDs(ids);
		}

		if(ghostdata == null)
		{
			APIError(api, "Download fehlgeschlagen...");
			return false;
		}

		ghostImport(ghostdata);

		return true;
	}

	public static void ghostSelect()
	{
		if(OfflineProfiles == null)
		{
			return;
		}

		String token = getToken();
		if(token == null)
		{
			return;
		}

		String selection   = null;
		String lastTrack   = cfg(CFG_TRACK);
		String lastWeather = cfg(CFG_WEATHER);

		String[]   tracks     = gmHelper.getTracks();
		int[]      weathers   = gmHelper.getWeatherIDs();
		String[]   values     = new String[tracks.length * weathers.length];
		String[][] conditions = new String[tracks.length * weathers.length][3];

		for(int i = 0; i < tracks.length; i++)
		{
			for(int h = 0; h < weathers.length; h++)
			{
				int key = (i * weathers.length) + h;
				values[key] = String.format("%s (%s)", gmHelper.getTrack(tracks[i]), gmHelper.getWeatherName(weathers[h]));

				conditions[key][0] = values[key];
				conditions[key][1] = tracks[i];
				conditions[key][2] = Integer.toString(weathers[h]);

				// TODO: Eigentlich nicht ganz korrekt, da das Wetter als "int" verglichen werden müsste.
				if(lastTrack != null && lastTrack.toLowerCase().equals(tracks[i].toLowerCase()) && lastWeather != null && lastWeather.equals(Integer.toString(weathers[h])))
				{
					selection = values[key];
				}
			}
		}

		String input = (String)JOptionPane.showInputDialog(
			mainwindow,
			"Um einen Geist direkt aus der Rangliste herunterzuladen, wähle zuerst die gewünschte Strecke aus:",
			APPLICATION_API,
			JOptionPane.PLAIN_MESSAGE,
			null,
			values,
			selection
		);

		if(input == null)
		{
			System.out.println("...: CANCEL");
			return;
		}

		String selectedTrack   = null;
		String selectedWeather = null;
		String value = (String) input;

		for(int i = 0; i < conditions.length; i++)
		{
			if(conditions[i][0].equals(value))
			{
				selectedTrack = conditions[i][1];
				selectedWeather = conditions[i][2];
				break;
			}
		}

		if(selectedTrack == null || selectedWeather == null)
		{
			Exception e = new Exception();
			e.printStackTrace(); return;
		}

		System.out.printf("SELECTED: %s (%s)%n", selectedTrack, selectedWeather);

		cfg(CFG_TRACK, selectedTrack);
		cfg(CFG_WEATHER, selectedWeather);

		ghostSelect(selectedTrack, Integer.parseInt(selectedWeather));
	}

	public static void ghostSelect(String track, int weather)
	{
		if(OfflineProfiles == null)
		{
			return;
		}

		String token = getToken();
		if(token == null)
		{
			return;
		}

		eSportsAPI api = new eSportsAPI(token, APPLICATION_IDENT);
		List<Map<String,Object>> results = api.getResultsByCondition(track, weather);

		if(results == null)
		{
			APIError(api, "Die Rangliste konnte nicht geladen werden...");
			return;
		}

		String selection = null;
		String[] values = new String[results.size()];
		Integer[] ghosts = new Integer[results.size()];

		for(int i = 0; i < results.size(); i++)
		{
			Map<String,Object> result = results.get(i);
			ghosts[i] = Integer.parseInt(result.get("GhostID").toString());
			values[i] = String.format("%0" + Integer.toString(FNX.strlen(results.size())) + "d. %s – %s", result.get("Position"), gmHelper.getResult(Integer.parseInt(result.get("Result").toString())), result.get("Nickname"));
		}

		String input = (String)JOptionPane.showInputDialog(
			mainwindow,
			"Nachfolgend alle verfügbaren Geister der gewählten Strecke:",
			APPLICATION_API,
			JOptionPane.PLAIN_MESSAGE,
			null,
			values,
			null
		);

		if(input == null)
		{
			System.out.println("...: CANCEL");
			return;
		}

		String value = (String) input;
		Integer ghost = null;

		for(int i = 0; i < values.length; i++)
		{
			if(values[i].equals(value))
			{
				ghost = ghosts[i];
				break;
			}
		}

		if(ghost == null)
		{
			return;
		}

		System.out.printf("selected ghost: %d%n", ghost);

		ghostDownload(ghost);
	}

	public static void playerInfo()
	{
		String token = getToken();
		if(token == null)
		{
			return;
		}

		eSportsAPI api = new eSportsAPI(token, APPLICATION_IDENT);
		Map<String,Object> data = api.getPlayerInfo();

		if(data == null)
		{
			APIError(api, "Da ging etwas schief...");
			return;
		}

		messageDialog(APPLICATION_API, String.format("Nachfolgend alle Details des angegebenen API-Tokens.%n%nBewerb: %3$s%nTeilnehmer: %2$s%n%nHAPPYTEC-Account: %1$s", data.get("Useraccount"), data.get("Nickname"), data.get("CompetitionName")));
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

		// TODO: Wenn Config leer ist, Standardpfade je nach OS anbieten?
		// ...

		JFileChooser chooser = new JFileChooser(cfg(CFG_CWD));

		// TODO: XML-Filter auslagern, wird auch beim Speichern genutzt.
		// ...

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
					errorMessage("Fehler beim Laden der XML-Datei!");
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
		if(OfflineProfiles == null || unsavedChanges())
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
		if(OfflineProfiles == null)
		{
			return true;
		}
		else if(unsavedChanges())
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
			if(!confirmDialog(JOptionPane.WARNING_MESSAGE, null, String.format("Deine Bearbeitungen wurden noch nicht gespeichert.%nWenn du fortfährst, gehen die Änderungen verloren!%n%nTrotzdem ohne Speichern fortfahren?")))
			{
				System.out.println("quit: NO");
				return true;
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
	DefaultTableCellRenderer renderLeft = new DefaultTableCellRenderer();
	DefaultTableCellRenderer renderCenter = new DefaultTableCellRenderer();
	DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();
	{
		renderLeft.setHorizontalAlignment(SwingConstants.LEFT);
		renderCenter.setHorizontalAlignment(SwingConstants.CENTER);
		renderRight.setHorizontalAlignment(SwingConstants.RIGHT);
	}

	public HTGT_JTable(TableModel dm)
	{
		super(dm);
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

// TODO: Sinnvollere und einheitliche Konsolenausgaben,
//       die einen echten Mehrwert für den User haben.
// ...
