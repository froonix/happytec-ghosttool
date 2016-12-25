

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.ActionEvent;

import java.awt.Color;
import java.awt.BorderLayout;

import java.io.File;
import java.io.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.ArrayList;

import java.util.Objects;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.KeyEvent;
import java.awt.Dimension;

import java.util.prefs.*;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.xml.sax.InputSource;

// HappyTec GhostTool
public class HTGT
{
	static String filename;
	static Document xml;
	static NodeList TrainingGhosts;
	private static DefaultTableModel TableModel;
	private static boolean fileChanged = false;
	private static int selectedProfile;

	private static Preferences cfg;

	static JFrame mainwindow;

	public static void main(String[] args) throws Exception
	{
		// die ganze logik für die xml-verarbeitung in eine eigene klasse packen!
		// und den ganzen gui kram eventuell auch in eine eigene klasse?
		// ...

		cfg = Preferences.userRoot().node("HTGT");


		try
		{
			chooseFile();
			mainWindow();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		//setupToken();

		eSportsAPI test = new eSportsAPI("xyz");
		String output = test.test();
		System.out.println(output);

	}

	public static void ghostDelete()
	{
		JOptionPane.showMessageDialog(null, "Geister können direkt über die Schaltfläche in jeder Zeile gelöscht werden.");
	}

	public static void ghostInput()
	{
		while(true)
		{
			// JFrame frame = new JFrame("Ghost Input");
			Object input = JOptionPane.showInputDialog(
				mainwindow,
				"Um einen Geist hinzuzufügen, kopiere die XML-Daten (<GhostDataPair ... />) in das Eingabefeld:",
				"Geist einfügen",
				JOptionPane.PLAIN_MESSAGE,
				null, null,
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

				int i = 0;
				try
				{
					//DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					//DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

					Pattern pattern = Pattern.compile("(<GhostDataPair[^>]+>)", Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(xmlstring);


					String xmltag;
					while(matcher.find())
					{
						xmltag = matcher.group(1);

						//Document xmldoc = dBuilder.parse(new InputSource(new StringReader(xmltag)));
						//NodeList ghosts = xmldoc.getElementsByTagName("GhostDataPair");
						//Element ghost = (Element) ghosts.item(0);
						//GhostElement ghostElement = new GhostElement(ghost);

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

				return;
			}




		}
	}

	public static void deleteToken()
	{
		cfg.remove("esports-token");
		JOptionPane.showMessageDialog(mainwindow, "Dein Zugangsschlüssel wurde aus der lokalen Konfiguration gelöscht!\n\nDu kannst ihn über das Menü jederzeit erneut eintragen.", "HAPPYTEC-eSports-API", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void setupToken()
	{
		while(true)
		{
			//JFrame frame = new JFrame("Token Setup");
			Object input = JOptionPane.showInputDialog(
				mainwindow,
				"Bitte gib deinen persönlichen Zugriffsschlüssel ein:",
				"HAPPYTEC-eSports-API",
				JOptionPane.PLAIN_MESSAGE,
				null, null,
				cfg.get("esports-token", "")
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
				cfg.put("esports-token", token);
				return;
			}
		}
	}

	public static void chooseFile()
	{
		// TODO: Get app path from registry?



		// todo: get last path from cfg/prefs!
		JFileChooser chooser = new JFileChooser("./example");

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

					// save current directory to cfg/prefs
					// ...

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
		File input = new File(filename);

		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			xml = dBuilder.parse(input);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void selectProfile()
	{
		NodeList OfflineProfiles = xml.getElementsByTagName("OfflineProfile");
		Object[] possibilities = new Object[OfflineProfiles.getLength()];

		String currentSelection = null;
		for(int i = 0; i < OfflineProfiles.getLength(); i++)
		{


			Node profile = OfflineProfiles.item(i);
			Element nickname = (Element) profile;

			Element nick = (Element) nickname.getElementsByTagName("Nickname").item(0);

			String item = String.format("[%02d] %s", i + 1, nick.getTextContent());
			possibilities[i] = item;

			if(selectedProfile == i)
			{
				currentSelection = item;
			}
		}

		// Object[] possibilities = {"ham", "spam", "yam"};
		String input = (String)JOptionPane.showInputDialog(
			mainwindow,
			"Aktuell genutztes Profil aus der XML-Datei:",
			"Profilauswahl",
			JOptionPane.PLAIN_MESSAGE,
			null,
			possibilities,
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
			for(int i = 0; i < OfflineProfiles.getLength(); i++)
			{
				Node profile = OfflineProfiles.item(i);
				Element nickname = (Element) profile;

				Element nick = (Element) nickname.getElementsByTagName("Nickname").item(0);

				if(inputstring.equals(String.format("[%02d] %s", i + 1, nick.getTextContent())))
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

		// todo: nachträgliche auswahl über menüleiste ermöglichen (radio buttons?)
		// selectProfile(0);


		Object rowData[][] = {};
		Object columnNames[] = { "Spieler", "Strecke", "Wetter", "Ergebnis", "" };
		TableModel = new DefaultTableModel(rowData, columnNames);
		JTable table = new JTable(TableModel)
		{
			// private static final long serialVersionUID = 1L;
			public boolean isCellEditable(int row, int column)
			{
				if(column == 4)
				{
					return true;
				}
				else
				{
					return false;
				}
			};
		};

		Action delete = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				// todo: auslagern!
				fileChanged();

				JTable table = (JTable)e.getSource();
				int modelRow = Integer.valueOf( e.getActionCommand() );

				// confirm!
				// ...

				Node ghost = TrainingGhosts.item(modelRow);
				Element ghostElement = (Element) ghost;
				ghost.getParentNode().removeChild(ghostElement);

				/*
				try
				{
					saveDocument(xml);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				*/

				((DefaultTableModel)table.getModel()).removeRow(modelRow);
				System.out.println("Row " + modelRow + " deleted!");
			}
		};

		ButtonColumn buttonColumn = new ButtonColumn(table, delete, 4);
		// buttonColumn.setMnemonic(KeyEvent.VK_D);

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

		JScrollPane scrollPane = new JScrollPane(table);
		mainwindow.add(scrollPane, BorderLayout.CENTER);

		// test (bei neuauswahl von profil erforderlich!)
		// clearTable();

		/*
		// Der nachfolgende Teil ist ein Beispiel, wie man das veränderte XML ausgibt oder speichert.
		try
		{
			xml.setXmlStandalone(true);
			printDocument(xml, System.out);
			// todo: alte datei backuppen!
			saveDocument(xml);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		*/
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

			// Element ghostElement = (Element) ghostnode;
			// ghostElement.getParentNode().appendChild(ghost.getElement());




			/*
			try
				{
					saveDocument(xml);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				**/
		}

		displayGhost(ghost);
	}

	private static void displayGhost(GhostElement ghost)
	{
		Object tmp[] = { ghost.getNickname(), ghost.getTrackName(), ghost.getWeatherName(), ghost.getResult(), "Geist löschen" };
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

	/*
	private static Object[][] addRow(Object[][] array, Object object)
	{
		ArrayList<Object> lst = new ArrayList<Object>();
		for (Object o : array) { lst.add(o); }
		lst.add(object); return lst.toArray();
	}
	*/

	private static void mainWindow()
	{
		// todo: speichern nur beim beenden oder über menüeintrag.
		// ...

		JMenuBar menuBar = new JMenuBar();

		JMenu menuFile = new JMenu("Datei");
		JMenuItem menuItemFileOpen = new JMenuItem(new AbstractAction("Öffnen") { public void actionPerformed(ActionEvent e) { return; }});
		JMenuItem menuItemFileSave = new JMenuItem(new AbstractAction("Speichern") { public void actionPerformed(ActionEvent e) { HTGT.saveFile(); }});
		JMenuItem menuItemFileSaveAs = new JMenuItem(new AbstractAction("Speichern unter") { public void actionPerformed(ActionEvent e) { return; }});
		JMenuItem menuItemFileQuit = new JMenuItem(new AbstractAction("Beenden") { public void actionPerformed(ActionEvent e) { HTGT.quit(); }});
		menuFile.add(menuItemFileOpen);
		menuFile.addSeparator();
		menuFile.add(menuItemFileSave);
		menuFile.add(menuItemFileSaveAs);
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

		JMenu menuGhost = new JMenu("Geister");
		JMenuItem menuItemSelectProfile = new JMenuItem(new AbstractAction("Profil auswählen") { public void actionPerformed(ActionEvent e) { HTGT.selectProfile(); }});
		JMenuItem menuItemGhostInput = new JMenuItem(new AbstractAction("Geist einfügen") { public void actionPerformed(ActionEvent e) { HTGT.ghostInput(); }});
		JMenuItem menuItemGhostDelete = new JMenuItem(new AbstractAction("Geist löschen") { public void actionPerformed(ActionEvent e) { HTGT.ghostDelete(); }});
		menuGhost.add(menuItemSelectProfile);
		menuGhost.addSeparator();
		menuGhost.add(menuItemGhostInput);
		menuGhost.add(menuItemGhostDelete);
		menuBar.add(menuGhost);

		JMenu menuAPI = new JMenu("API");
		JMenuItem menuItemAPITokenChange = new JMenuItem(new AbstractAction("Token ändern") { public void actionPerformed(ActionEvent e) { HTGT.setupToken(); }});
		JMenuItem menuItemAPITokenClean = new JMenuItem(new AbstractAction("Token löschen") { public void actionPerformed(ActionEvent e) { HTGT.deleteToken(); }});
		menuAPI.add(menuItemAPITokenChange);
		menuAPI.add(menuItemAPITokenClean);
		menuBar.add(menuAPI);

		// JMenu menuHelp = new JMenu("Hilfe");
		// menuBar.add(menuHelp);

		mainwindow = new JFrame("HTGT (HAPPYTEC Ghosttool)");
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

		// mainwindow.setLocation(50,50);
		mainwindow.setSize(800,400);
		mainwindow.setMinimumSize(new Dimension(400, 200));
		mainwindow.setVisible(true);
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

	public static void saveFile()
	{
		if(fileChanged)
		{
			try
			{
				xml.setXmlStandalone(true);
				// printDocument(xml, System.out);
				saveDocument(xml);

				fileSaved();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static void fileSaved()
	{
		fileChanged = false;
		mainwindow.setTitle("HTGT (HAPPYTEC Ghosttool)");
	}

	public static void fileChanged()
	{
		fileChanged = true;
		mainwindow.setTitle("HTGT (HAPPYTEC Ghosttool) *");
	}
}
