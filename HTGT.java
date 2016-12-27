import java.io.*;
import java.util.prefs.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
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

	// Konfigurationsnamen für java.util.prefs
	private static String CFG_CWD   = "last-directory";
	private static String CFG_TOKEN = "esports-token";

	private static Preferences cfg;                                     // Konfiguration
	private static boolean     fileChanged;                             // XML-Daten geändert?
	private static String      filename;                                // Aktuell geladene Datei
	private static Document    xml;                                     // Aktuelles XML-Dokument

	private static int         selectedProfile;                         // Aktuell gewähltes Profil
	private static NodeList    TrainingGhosts;                          // Geister des aktuellen Profiles

	private static DefaultTableModel TableModel;
	private static JFrame            mainwindow;
	private static JTable            maintable;

	public static void main(String[] args) throws Exception
	{
		cfg = Preferences.userRoot().node(APPLICATION_NAME);


		JMenuBar menuBar = new JMenuBar();

		JMenu menuFile = new JMenu("Datei");
		JMenuItem menuItemFileOpen = new JMenuItem(new AbstractAction("Öffnen") { public void actionPerformed(ActionEvent e) { return; }});
		JMenuItem menuItemFileSave = new JMenuItem(new AbstractAction("Speichern") { public void actionPerformed(ActionEvent e) { HTGT.saveFile(); }});
		JMenuItem menuItemFileSaveAs = new JMenuItem(new AbstractAction("Speichern unter") { public void actionPerformed(ActionEvent e) { return; }});
		JMenuItem menuItemFileQuit = new JMenuItem(new AbstractAction("Beenden") { public void actionPerformed(ActionEvent e) { HTGT.quit(); }});
		// menuFile.add(menuItemFileOpen);
		// menuFile.addSeparator();
		menuFile.add(menuItemFileSave);
		// menuFile.add(menuItemFileSaveAs);
		menuFile.addSeparator();
		menuFile.add(menuItemFileQuit);
		menuBar.add(menuFile);

		/*
		JRadioButtonMenuItem profile;
		JMenu menuProfile = new JMenu("Profil");
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem rbMenuItem1 = new JRadioButtonMenuItem("1...");
		JRadioButtonMenuItem rbMenuItem2 = new JRadioButtonMenuItem("2...");
		JRadioButtonMenuItem rbMenuItem3 = new JRadioButtonMenuItem("3...");
		JRadioButtonMenuItem rbMenuItem4 = new JRadioButtonMenuItem("4...");
		rbMenuItem2.setSelected(true);
		group.add(rbMenuItem1);
		group.add(rbMenuItem2);
		group.add(rbMenuItem3);
		group.add(rbMenuItem4);
		menuProfile.add(rbMenuItem1);
		menuProfile.add(rbMenuItem2);
		menuProfile.add(rbMenuItem3);
		menuProfile.add(rbMenuItem4);
		menuBar.add(menuProfile);
		*/

		JMenu menuEdit = new JMenu("Bearbeiten");
		JMenuItem menuItemEditInsert = new JMenuItem(new AbstractAction("Einfügen (aus Zwischenablage)") { public void actionPerformed(ActionEvent e) { HTGT.importFromClipboard(); }});
		JMenuItem menuItemEditCopy = new JMenuItem(new AbstractAction("Kopieren (in Zwischenablage)") { public void actionPerformed(ActionEvent e) { HTGT.test(); }});
		JMenuItem menuItemEditDelete = new JMenuItem(new AbstractAction("Markierte löschen") { public void actionPerformed(ActionEvent e) { HTGT.deleteGhost(); }});
		menuEdit.add(menuItemEditInsert);
		menuEdit.add(menuItemEditCopy);
		menuEdit.addSeparator();
		menuEdit.add(menuItemEditDelete);
		menuBar.add(menuEdit);

		// todo: einfügen/kopieren über zwischenablage!
		// ...

		JMenu menuGhost = new JMenu("Geister");
		JMenuItem menuItemSelectProfile = new JMenuItem(new AbstractAction("Profil auswählen") { public void actionPerformed(ActionEvent e) { HTGT.selectProfile(); }});
		JMenuItem menuItemGhostInput = new JMenuItem(new AbstractAction("Geist einfügen") { public void actionPerformed(ActionEvent e) { HTGT.ghostInput(); }});
		// JMenuItem menuItemGhostDelete = new JMenuItem(new AbstractAction("Geist löschen") { public void actionPerformed(ActionEvent e) { HTGT.ghostDelete(); }});
		JMenuItem menuItemGhostDownload = new JMenuItem(new AbstractAction("Geist herunterladen") { public void actionPerformed(ActionEvent e) { HTGT.ghostDownload(); }});
		menuGhost.add(menuItemSelectProfile);
		menuGhost.addSeparator();
		menuGhost.add(menuItemGhostInput);
		// menuGhost.add(menuItemGhostDelete);
		menuGhost.addSeparator();
		menuGhost.add(menuItemGhostDownload);
		menuBar.add(menuGhost);

		JMenu menuAPI = new JMenu("API");
		JMenuItem menuItemAPITokenChange = new JMenuItem(new AbstractAction("Token ändern") { public void actionPerformed(ActionEvent e) { HTGT.setupToken(); }});
		JMenuItem menuItemAPITokenClean = new JMenuItem(new AbstractAction("Token löschen") { public void actionPerformed(ActionEvent e) { HTGT.deleteToken(); }});
		menuAPI.add(menuItemAPITokenChange);
		menuAPI.add(menuItemAPITokenClean);
		menuBar.add(menuAPI);

		// JMenu menuHelp = new JMenu("Hilfe");
		// menuBar.add(menuHelp);

		mainwindow = new JFrame(APPLICATION_TITLE);
		// mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainwindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainwindow.setJMenuBar(menuBar);

		mainwindow.addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent)
			{
				quit();
			}
		});


		updateMainWindow();

		// mainwindow.setLocation(0, 0);
		mainwindow.setSize(WINDOW_SIZE_START);
		mainwindow.setMinimumSize(WINDOW_SIZE_MIN);
		mainwindow.setVisible(true);




		try
		{
			chooseFile();
			selectProfile(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


	}

	public static void test()
	{
		StringBuilder data = new StringBuilder();
		int[] selection = maintable.getSelectedRows();

		for (int i = selection.length - 1; i > -1; i--)
		{
			int row = selection[i];

			Node ghost = TrainingGhosts.item(i);
			Element ghostElement = (Element) ghost;

			try
			{
				TransformerFactory tf = TransformerFactory.newInstance();
				Transformer transformer = tf.newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				// transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

				StreamResult result = new StreamResult(new StringWriter());
				transformer.transform(new DOMSource(ghostElement), result);
				data.append(result.getWriter().toString());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		System.out.println(data.toString());

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data.toString()), null);
	}

	public static void deleteGhost()
	{
		int[] selection = maintable.getSelectedRows();

		// confirm!
		// ...

		for (int i = selection.length - 1; i > -1; i--)
		{
			int row = selection[i];



			deleteGhost(row);
		}
	}

	public static void deleteGhost(int i)
	{
		if(i > (TrainingGhosts.getLength() - 1))
		{
			throw new IndexOutOfBoundsException(String.format("Ghost #%d", i));
		}

		Node ghost = TrainingGhosts.item(i);
		Element ghostElement = (Element) ghost;

		fileChanged();
		ghost.getParentNode().removeChild(ghostElement);
		TableModel.removeRow(i);

		System.out.println("Row " + i + " deleted!");
	}

	public static void ghostDelete()
	{
		JOptionPane.showMessageDialog(null, "Geister können direkt über die Schaltfläche in jeder Zeile gelöscht werden.");
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

				return;
			}
		}
	}

	public static void ghostInput()
	{
		while(true)
		{
			Object input = JOptionPane.showInputDialog(
				mainwindow,
				"Um einen Geist hinzuzufügen, kopiere die XML-Daten (<GhostDataPair ... />) in das Eingabefeld:",
				"Geist einfügen",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				""
			);

			if(input == null)
			{
				System.out.println("ghostInput: CANCEL");
				return;
			}

			String xmlstring = String.format("%s", input);

			if(xmlstring.length() == 0)
			{
				System.out.println("ghostInput: EMPTY");
				continue;
			}
			else
			{
				System.out.printf("ghostInput: VALUE (%d)\n", xmlstring.length());

				ghostImport(xmlstring);

				return;
			}
		}
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
	}

	public static void importFromClipboard()
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

	public static void chooseFile()
	{
		JFileChooser chooser = new JFileChooser(cfg(CFG_CWD));

		FileFilter filter = new FileNameExtensionFilter("XML-Dateien", "xml");
		chooser.addChoosableFileFilter(filter); chooser.setFileFilter(filter);

		while(true)
		{
			int code = chooser.showOpenDialog(null);

			if(code == JFileChooser.APPROVE_OPTION)
			{
				System.err.println("JFileChooser: APPROVE_OPTION");
				filename = chooser.getSelectedFile().getAbsolutePath();

				if(filename != "")
				{
					System.err.println("XML filename: " + filename);
					cfg(CFG_CWD, String.format("%s", chooser.getCurrentDirectory()));

					loadXML();
					return;
				}
			}
			else if(code == JFileChooser.CANCEL_OPTION)
			{
				System.err.println("JFileChooser: CANCEL_OPTION");
				System.exit(0);
			}
			else if(code == JFileChooser.ERROR_OPTION)
			{
				System.err.println("JFileChooser: ERROR_OPTION");
				System.exit(0);
			}
		}
	}

	public static void loadXML()
	{
		fileChanged = false;
		File input = new File(filename);

		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			xml = dBuilder.parse(input);
			xml.setXmlStandalone(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private static String formatProfileSelection(int i, String nick)
	{
		return String.format("[%02d] %s", i + 1, nick);
	}

	public static void selectProfile()
	{
		if(!fileLoaded())
		{
			return;
		}

		// TODO: Zuletzt genutzes Profile in CFG abspeichern und auslesen
		// ...

		NodeList OfflineProfiles = xml.getElementsByTagName("OfflineProfile");
		Object[] availableProfiles = new Object[OfflineProfiles.getLength()];

		String currentSelection = null;
		for(int i = 0; i < OfflineProfiles.getLength(); i++)
		{
			Node ProfileNode = OfflineProfiles.item(i);
			Element ProfileElement = (Element) ProfileNode;

			Element NickElement = (Element) ProfileElement.getElementsByTagName("Nickname").item(0);
			String option = formatProfileSelection(i, NickElement.getTextContent());

			availableProfiles[i] = option;

			if(i == selectedProfile)
			{
				currentSelection = option;
			}
		}

		String input = (String)JOptionPane.showInputDialog(
			mainwindow,
			"Aktuell genutztes Profil aus der XML-Datei:",
			"Profilauswahl",
			JOptionPane.PLAIN_MESSAGE,
			null,
			availableProfiles,
			currentSelection
		);

		if(input == null)
		{
			System.out.println("selectProfile: CANCEL");
			return;
		}
		else
		{
			String inputstring = (String) input;

			int selection = 0;



			for(int i = 0; i < availableProfiles.length; i++)
			{
				if(inputstring.equals(availableProfiles[i]))
				{
					selection = i;
					break;
				}
			}

			System.out.println("selection: " + inputstring);
			System.out.println("profile: " + selection);

			clearTable();
			selectProfile(selection);


		}
	}

	public static void selectProfile(int i)
	{
		if(!fileLoaded())
		{
			return;
		}

		selectedProfile = i;

		NodeList OfflineProfiles = xml.getElementsByTagName("OfflineProfile");
		Element test = (Element) OfflineProfiles.item(i);
		TrainingGhosts = test.getElementsByTagName("GhostDataPair");

		for(int h = 0; h < TrainingGhosts.getLength(); h++)
			{
				Node ghost = TrainingGhosts.item(h);
				Element ghostElement = (Element) ghost;

				GhostElement ge = new GhostElement(ghostElement);
				ge.printDetails();
				addGhost(ge, false);
			}
	}

	public static void updateMainWindow()
	{

		Object rowData[][] = {};
		Object columnNames[] = { "Spieler", "Strecke", "Wetter", "Ergebnis" /*, ""*/ };
		TableModel = new DefaultTableModel(rowData, columnNames);
		maintable = new JTable(TableModel)
		{
			// private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column)
			{
//				if(column == 4)
//				{
//					return true;
//				}
//				else
//				{
					return false;
//				}
			};
		};

		maintable.setFocusable(false);
		// maintable.setRowSelectionAllowed(true);
		maintable.setColumnSelectionAllowed(false);
		//maintable.setCellSelectionEnabled(false);

		//Action delete = new AbstractAction()
		//{
			//public void actionPerformed(ActionEvent e)
			//{
				//// todo: auslagern!
				//fileChanged();

				//JTable table = (JTable)e.getSource();
				//int modelRow = Integer.valueOf( e.getActionCommand() );

				//// confirm!
				//// ...

				//Node ghost = TrainingGhosts.item(modelRow);
				//Element ghostElement = (Element) ghost;
				//ghost.getParentNode().removeChild(ghostElement);

				///*
				//try
				//{
					//saveDocument(xml);
				//}
				//catch(Exception ex)
				//{
					//ex.printStackTrace();
				//}
				//*/

				//((DefaultTableModel)table.getModel()).removeRow(modelRow);
				//System.out.println("Row " + modelRow + " deleted!");
			//}
		//};

		//ButtonColumn buttonColumn = new ButtonColumn(maintable, delete, 4);
		//// buttonColumn.setMnemonic(KeyEvent.VK_D);

		// todo: zeile löschen über DEL key?
		// ...

		/*
		for(int i = 0; i < TrainingGhosts.getLength(); i++)
		{
			Node ghost = TrainingGhosts.item(i);
			Element ghostElement = (Element) ghost;

			GhostElement test = new GhostElement(ghostElement);
			test.printDetails();
			addGhost(test, false);
		}
		*/

		selectProfile(0);

		JScrollPane scrollPane = new JScrollPane(maintable);
		mainwindow.add(scrollPane, BorderLayout.CENTER);
	}

	private static void clearTable()
	{
		TableModel.setRowCount(0);

		/*
		if(TableModel.getRowCount() > 0)
		{
			for(int i = TableModel.getRowCount() - 1; i > -1; i--)
			{
				TableModel.removeRow(i);
			}
		}
		*/
	}

	public static void addGhost(GhostElement ghost, boolean create)
	{
		// validate xml data?
		// ...

		if(create)
		{
			fileChanged();

			// add to xml node
			// ...

			// todo: das funktioniert nur, wenn es bereits elemente gibt.
			// für den anderen fall braucht es eine andere lösung!
			Node ghostnode = TrainingGhosts.item(TrainingGhosts.getLength() - 1);
			Element ghostElement = (Element) ghostnode;

			Node newNode = xml.importNode(ghost.getElement(), true);
			ghostElement.getParentNode().appendChild(newNode);
		}

		displayGhost(ghost);
	}

	private static void displayGhost(GhostElement ghost)
	{
		Object tmp[] = { ghost.getNickname(), ghost.getTrackName(), ghost.getWeatherName(), ghost.getResult() /*, "Geist löschen"*/ };
		TableModel.addRow(tmp);
	}

	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		// transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc),
			 new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	public static void saveDocument(Document doc) throws IOException, TransformerException
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		// transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc),
			 new StreamResult(new File(filename + ".new")));
	}

	public static void quit()
	{
		if(fileChanged)
		{
			int input = JOptionPane.showConfirmDialog(mainwindow,
				"Die Änderungen wurden nicht gespeichert! Trotzdem beenden?",
				"Es gibt nicht gespeicherte Änderungen",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE
			);

			if(input == JOptionPane.NO_OPTION)
			{
				System.out.println("quit: NO");
				return;
			}
			else
			{
				System.out.println("quit: YES");
			}
		}

		System.exit(0);
	}

	public static boolean saveFile()
	{
		if(fileChanged)
		{
			try
			{
				saveDocument(xml);
				fileSaved();

				return true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				fileChanged();
			}
		}

		return false;
	}

	private static void fileSaved()
	{
		fileChanged = false;
		mainwindow.setTitle(APPLICATION_TITLE);
	}

	public static void fileChanged()
	{
		fileChanged = true;
		mainwindow.setTitle(APPLICATION_TITLE + " *");
	}

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

	public static boolean fileLoaded()
	{
		if(xml != null)
		{
			return true;
		}

		return false;
	}
}
