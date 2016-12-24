import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.JFileChooser;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.awt.Color;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.border.LineBorder;
import javax.swing.border.Border;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import javax.swing.table.*;

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

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.KeyEvent;

// HappyTec GhostTool
public class HTGT
{
	static String filename;
	static Document xml;
	static NodeList TrainingGhosts;
	private static DefaultTableModel TableModel;

	static JFrame mainwindow;

	public static void main(String[] args)
	{
		// die ganze logik für die xml-verarbeitung in eine eigene klasse packen!
		// und den ganzen gui kram eventuell auch in eine eigene klasse?
		// ...

		try
		{
			chooseFile();
			mainWindow();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void chooseFile()
	{
		// TODO: Get app path from registry?
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

	public static void selectProfile(int i)
	{
		NodeList OfflineProfiles = xml.getElementsByTagName("OfflineProfile");
		Element test = (Element) OfflineProfiles.item(i);
		TrainingGhosts = test.getElementsByTagName("GhostDataPair");
	}

	public static void updateMainWindow()
	{

		// todo: nachträgliche auswahl über menüleiste ermöglichen (radio buttons?)
		selectProfile(0);

		Object rowData[][] = {};
		Object columnNames[] = { "Nick", "Track", "Weather", "Time", "" };
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
				JTable table = (JTable)e.getSource();
				int modelRow = Integer.valueOf( e.getActionCommand() );

				// confirm!
				// ...

				Node ghost = TrainingGhosts.item(modelRow);
				Element ghostElement = (Element) ghost;
				ghost.getParentNode().removeChild(ghostElement);
				try
				{
					saveDocument(xml);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}

				((DefaultTableModel)table.getModel()).removeRow(modelRow);
				System.out.println("Row " + modelRow + " deleted!");
			}
		};

		ButtonColumn buttonColumn = new ButtonColumn(table, delete, 4);
		buttonColumn.setMnemonic(KeyEvent.VK_D);

		// todo: zeile löschen über DEL key?
		// ...

		for(int i = 0; i < TrainingGhosts.getLength(); i++)
		{
			Node ghost = TrainingGhosts.item(i);
			Element ghostElement = (Element) ghost;

			GhostElement test = new GhostElement(ghostElement);
			test.printDetails();
			addGhost(test, false);
		}

		JScrollPane scrollPane = new JScrollPane(table);
		mainwindow.add(scrollPane, BorderLayout.CENTER);

		// test (bei neuauswahl von profil erforderlich!)
		// clearTable();

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
			// add to xml node
			// ...
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
		JMenuBar menuBar = new JMenuBar();

		JMenu menuFile = new JMenu("Datei");
		JMenuItem menuItemFileOpen = new JMenuItem(new AbstractAction("XML-Datei öffnen") { public void actionPerformed(ActionEvent e) { HTGT.chooseFile(); HTGT.updateMainWindow(); }});
		JMenuItem menuItemFileRead = new JMenuItem(new AbstractAction("Ansicht aktualisieren") { public void actionPerformed(ActionEvent e) { HTGT.loadXML(); HTGT.updateMainWindow(); }});
		JMenuItem menuItemFileQuit = new JMenuItem(new AbstractAction("Programm beenden") { public void actionPerformed(ActionEvent e) { System.exit(0); }});
		menuFile.add(menuItemFileOpen);
		menuFile.add(menuItemFileRead);
		menuFile.add(menuItemFileQuit);
		menuBar.add(menuFile);

		JMenu menuProfile = new JMenu("Profil");
		menuBar.add(menuProfile);

		JMenu menuHelp = new JMenu("Hilfe");
		menuBar.add(menuHelp);

		mainwindow = new JFrame("HTGT (HAPPYTEC Ghosttool)");
		mainwindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainwindow.setJMenuBar(menuBar);

		updateMainWindow();

		// mainwindow.setLocation(50,50);
		mainwindow.setSize(800,400);
		mainwindow.setVisible(true);
	}
}
