/**
 * HTGT.java: Main class (GUI) for Happytec-Ghosttool
 * Copyright (C) 2020 Christian Schrötter <cs@fnx.li>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.Reader;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import java.nio.file.attribute.FileTime;

import java.net.URI;

import java.lang.IndexOutOfBoundsException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import java.util.prefs.Preferences;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.plaf.basic.BasicTableHeaderUI;

import javax.swing.plaf.LayerUI;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class HTGT
{
	// Diverse fixe Konstanten für die Anwendung
	final public static String    APPLICATION_VERSION = "git-master";
	final public static String    APPLICATION_NAME    = "HTGT"; // cfg, updates, …
	final public static String    APPLICATION_TITLE   = "HTGT.app";
	final public static String    APPLICATION_API     = "HAPPYTEC-eSports-API";
	final public static String    APPLICATION_IDENT   = "HTGT %s <https://github.com/froonix/happytec-ghosttool>";
	final public static Dimension WINDOW_SIZE_START   = new Dimension(900, 600);
	final public static Dimension WINDOW_SIZE_MIN     = new Dimension(600, 200);
	final public static long      UPDATE_INTERVAL     = 86400000L; // daily
	final public static long      WEATHER_INTERVAL    = 3600000L; // hourly
	final public static int       FF_CHECK_INTERVAL   = 2000; // 2 seconds
	final public static long      FF_OBSERVER_DELAY   = 1000; // 1 second
	final public static String    SPECIAL_PROFILE     = "SpecialProfile";
	final public static String    DEFAULT_PROFILE     = "DefaultUser";
	final public static String    VERSION_FILE        = "htgt-version.txt";
	final public static int       NICKNAME_MINLEN     = 3;
	final public static int       NICKNAME_MAXLEN     = 13;
	final public static String    NICKNAME_REGEX      = "^(?i:[A-Z0-9_]{" + NICKNAME_MINLEN + "," + NICKNAME_MAXLEN + "})$";
	final public static String    NICKNAME_REGEX_NOT  = "^[0-9]+$";
	final public static boolean   ENABLE_AUTOSAVE     = true;
	final public static boolean   ENABLE_RACE         = true;
	final public static boolean   ENABLE_XTC          = true;
	final public static boolean   ENABLE_SUC          = true;
	final public static boolean   ENABLE_WATCHSERVICE = true;
	final public static boolean   ENABLE_BLURRY       = true;
	final public static int       FONTSIZE            = 13;
	final public static double    FONTSMALL           = 0.75;
	final public static int       HISTORY_SIZE        = 10;

	final public static int        NONE  = 0;
	final public static int        CTRL  = getCtrlMask();
	final public static int        SHIFT = ActionEvent.SHIFT_MASK;
	final public static int        ALT   = ActionEvent.ALT_MASK;

	// Diverse Links ohne https:// davor, da sie als Ziel direkt angezeigt werden sollen!
	final public static String    URL_WWW  = "github.com/froonix/happytec-ghosttool";
	final public static String    URL_API  = "www.esports.happytec.at";

	// Redirect-Service für diverse andere Links oder Aktionen. (Leitet derzeit alles nur zum Forenthread...)
	final public static String    URL_REDIRECT = "https://www.esports.happytec.at/redirect/desktop/HTGT.php?dst=%s";

	// Bei jeder neuen verfügbaren Sprache muss dieser Wert erhöht werden.
	// Dadurch wird der Dialog für die Sprachauswahl erneut angezeigt werden.
	// Es sollte nicht dazu verwendet werden, um die Sprachen zu zählen!
	final public static int TRANSLATION_VERSION = 3;

	// Alle verfügbaren Sprachen als Locale-String.
	// In dieser Reihenfolge werden sie auch angezeigt!
	final public static String[] LOCALES = new String[]{ "de_DE", "en_UK", "it_IT", "sk_SK" };

	// Konfigurationsnamen für java.util.prefs
	final public static String CFG_LOCALE      = "locale";
	final public static String CFG_TRANSLATION = "translation";
	final public static String CFG_API         = "api-host";
	final public static String CFG_DC          = "dll-check";
	final public static String CFG_UC          = "update-check";
	final public static String CFG_DEFAULT     = "default-file";
	final public static String CFG_TOKEN       = "esports-token";
	final public static String CFG_CWD         = "last-directory";
	final public static String CFG_CWDPORT     = "last-port-directory";
	final public static String CFG_RPROFILE    = "last-regular-profile";
	final public static String CFG_PROFILE     = "last-profile";
	final public static String CFG_MODE        = "last-gamemode";
	final public static String CFG_WEATHER     = "last-weather";
	final public static String CFG_TRACK       = "last-track";
	final public static String CFG_NDG         = "never-download";
	final public static String CFG_ARG         = "always-replace";
	final public static String CFG_AAR         = "always-apply";
	final public static String CFG_WC          = "weather-check";
	final public static String CFG_TRACKS      = "track-order";
	final public static String CFG_RACE        = "race.%s.%s";

	// ALWAYS THE INDEX NUMBER! WITHOUT THE EXTRA COUNT.
	final public static int OFFSET_RACE   =  0;
	final public static int OFFSET_TICKET =  1;
	final public static int OFFSET_SUC    =  2;

	final public static int PROFILE_NONE    =  0;
	final public static int PROFILE_DEFAULT = -1;
	final public static int PROFILE_SPECIAL = -2;

	final public static int BUTTON_ALWAYS =  2;
	final public static int BUTTON_YES    =  1;
	final public static int BUTTON_CLOSED =  0;
	final public static int BUTTON_NO     = -1;
	final public static int BUTTON_NEVER  = -2;

	// DON'T USE VALUES GREATER THAN ZERO HERE...
	// AND DON'T FORGET: CLOSED_OPTION IS -1 TOO!
	final public static int BUTTON_CANCEL = -1;
	final public static int BUTTON_PREV   = -2;
	final public static int BUTTON_NEXT   = -3;

	final public static String MENU_STATIC  = "static";                // Immer aktiv, unabhängig vom Kontext/Status.
	final public static String MENU_DEFAULT = "default";               // Aktiv, sobald eine XML-Datei geladen wurde.
	final public static String MENU_UNDO    = "undo";                  // Aktiv, sobald der Verlauf ältere Strings enthält.
	final public static String MENU_REDO    = "redo";                  // Aktiv, sobald der Verlauf neuere Strings enthält.
	final public static String MENU_TOKEN   = "token";                 // Aktiv, sobald ein API-Token existiert – unabhängig vom Kontext/Status.
	final public static String MENU_FTOKEN  = "ftoken";                // Aktiv, sobald ein API-Token existiert und eine XML-Datei geladen wurde.
	final public static String MENU_STOKEN  = "stoken";                // Aktiv, sobald ein API-Token existiert und Geister markiert wurden.
	final public static String MENU_PTOKEN  = "ptoken";                // Aktiv, sobald ein API-Token im geladenen XML-Profil existiert.
	final public static String MENU_SELECT  = "select";                // Aktiv, sobald Geister markiert wurden.

	private static Locale                     defaultLocaleAtStartUp;
	private static ResourceBundle             lang;

	private static Preferences                cfg;
	private static File                       dll;
	private static File                       file;
	private static int                        profile;
	private static String                     nickname;

	private static String[]                   history;
	private static int                        historyIndex;

	private static String                     token;
	private static eSportsAPI                 anonAPI;
	private static eSportsAPI                 api;

	private static int                        lastFilterOption;
	private static boolean                    lastApplicationStatus;
	private static volatile int               lastApplicationPosition;
	private static volatile GhostElement      lastApplicationGhost;
	private static volatile Map<Integer,Map>  lastApplicationDestinations;
	private static volatile boolean           ffDownload;

	private static OfflineProfiles            OfflineProfiles;

	private static JDialog                    ffDialog;
	private static JButton                    ffButton;
	private static JOptionPane                ffBody;
	private static HTGT_FFM_KeyListener       ffListener;
	private static int                        ffModification;
	private static int                        ffStarted;
	private static boolean                    ffForce;
	private static HTGT_FFM_Analyst           aFFM;
	private static HTGT_FFM_Observer          oFFM;

	private static volatile int               uploadedCount;
	private static volatile int               appliedCount;

	private static JFrame                     mainWindow;
	private static JTable                     maintable;
	private static DefaultTableModel          mainmodel;
	private static LayerUI<Container>         mainLayer;
	private static Container                  mainPane;

	private static Map<String,ArrayList<DynamicMenuItem>> menuitems;

	public static int getCtrlMask()
	{
		try
		{
			return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		}
		catch(NoSuchMethodError e)
		{
			return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		}
	}

	public static void about()
	{
		String licence = String.format(
			  "Copyright (C) 2020 Christian Schr&ouml;tter &lt;cs@fnx.li&gt;<br /><br />"
			+ "This program is free software; you can redistribute it and/or modify<br />"
			+ "it under the terms of the GNU General Public License as published by<br />"
			+ "the Free Software Foundation; either version 3 of the License, or<br />"
			+ "(at your option) any later version.<br /><br />"
			+ "This program is distributed in the hope that it will be useful,<br />"
			+ "but WITHOUT ANY WARRANTY; without even the implied warranty of<br />"
			+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the<br />"
			+ "GNU General Public License for more details.<br /><br />"
			+ "You should have received a copy of the GNU General Public License<br />"
			+ "along with this program; if not, write to the Free Software Foundation,<br />"
			+ "Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA"
		);

		String content =
			  "<html>"
			+ "	<body style='font-family: sans-serif;'>"
			+ "		<b>Application:</b> %1$s<br /><b>Version:</b> %3$s"
			+ "		<br /><br />Website: <a href='https://%5$s'>%5$s</a><br />%2$s: <a href='https://%6$s'>%6$s</a>"
			+ "		<br /><br /><pre style='font-family: monospace; padding: 10px; color: #AAAAAA; border: 1px solid #CCCCCC;'>%4$s</pre>"
			+ "		<br /><br />"
			+ "		<table align=\"center\" border=\"0\" style=\"border: 1px solid #888888;\">"
			+ "			<tr><th align=\"center\" colspan=\"2\" style=\"background-color: #888888; color: #FFFFFF;\">Thanks to all translators!</th></tr>"
			+ "			<tr><td align=\"right\">Slovak:</td><td align=\"left\"><a href=\"https://www.forum.happytec.at/profile.php?mode=viewprofile&amp;u=837\" style=\"color: #000000;\"><b>SVK_starec</b></a></td></tr>"
			+ "			<tr><td align=\"right\">Italian:</td><td align=\"left\"><a href=\"https://www.forum.happytec.at/profile.php?mode=viewprofile&amp;u=1593\" style=\"color: #000000;\"><b>RivaStyle</b></a></td></tr>"
			+ "		</table>"
			+ "		<br /><br /><div align='center'><i>" + FNX.getLangString(lang, "aboutExtendedHeader") + "</i><br /><br /><a href='%7$s' style='text-decoration: none;'><b>" + FNX.getLangString(lang, "aboutExtendedLink") + "</b></a></div>"
			+ "	</body>"
			+ "</html>"
		;

		// TODO: HTML-Ressource und Lizenz auslagern und hier nur ersetzen?
		// ...

		JOptionPane.showOptionDialog(mainWindow, FNX.getHTMLPane(String.format(content, APPLICATION_NAME, APPLICATION_API, getVersion(true), licence, URL_WWW, URL_API, getRedirectURL("support"))), FNX.getLangString(lang, "aboutTitle"), JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
	}

	public static void faq()
	{
		openURL("faq");
	}

	public static void support()
	{
		openURL("support");
	}

	private static void openURL(String dst)
	{
		try
		{
			if(Desktop.isDesktopSupported())
			{
				Desktop.getDesktop().browse(new URI(getRedirectURL(dst)));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void exceptionHandler(Exception e)
	{
		exceptionHandler(e, null);
	}

	private static void exceptionHandler(Exception e, String msg)
	{
		if(!FNX.requireEDT())
		{
			e.printStackTrace();

			return;
		}

		FNX.windowToFront(mainWindow);
		FNX.displayExceptionSummary(e, FNX.formatLangString(lang, "errorTitle"), msg, FNX.formatLangString(lang, "errorBody"));
	}

	public static String getIdent()
	{
		return String.format(APPLICATION_IDENT, getVersion(false));
	}

	public static String getVersion(boolean full)
	{
		if(APPLICATION_VERSION.toUpperCase().startsWith("GIT-"))
		{
			try
			{
				Reader r;
				BufferedReader b;
				InputStream i;
				String v;

				if((i = HTGT.class.getResourceAsStream("/" + VERSION_FILE)) != null)
				{
					r = new InputStreamReader(i);
				}
				else
				{
					// Schmutziger Hack fürs Makefile...
					r = new FileReader("./" + VERSION_FILE);
				}

				b = new BufferedReader(r);
				v = b.readLine();

				if(v != null && v.length() > 0)
				{
					if(full)
					{
						return String.format("%s (%s)", v, APPLICATION_VERSION);
					}
					else
					{
						return v;
					}
				}
			}
			catch(Exception e)
			{
				/* ... */
			}
		}

		return APPLICATION_VERSION;
	}

	private static String getRedirectURL(String dst)
	{
		return String.format(URL_REDIRECT, FNX.urlencode(dst));
	}

	public static void main(String[] args)
	{
		if(args.length > 0)
		{
			if(args[0].equals("-v"))
			{
				// Required for Makefile!
				System.out.println(getVersion(false));
				System.exit(0);
			}
			else if(args[0].equals("-d"))
			{
				FNX.enableDebugging();
			}
		}

		// Beim Debuggen aus dem Editor heraus sollen standardmäßig alle Debugausgaben sichtbar sein.
		// Heißt im Detail: Wenn die Versionsdatei im JAR nicht existiert, wird der Debugmodus aktiviert.
		// In allen anderen Fällen, wenn übers Makefile ein normales Build generiert wurde, ist er inaktiv.
		// Er kann natürlich trotzdem jederzeit über den Parameter "-d" bzw. das Debugscript aktiviert werden.
		if(HTGT.class.getResource("/" + VERSION_FILE) == null)
		{
			FNX.enableDebugging();

			FNX.dbg("Huh? This isn't a JAR! Happy debugging... :-)");
		}

		FNX.dbgf("%s version: %s", APPLICATION_NAME, getVersion(true));

		// Aktuell gibt es nur eine Konfiguration für den ganzen User-
		// account. Das heißt, dass mehrere unterschiedliche Bewerbe und
		// OfflineProfiles nicht möglich sind. Siehe GitHub Issue #7.
		cfg = Preferences.userRoot().node(APPLICATION_NAME);

		// ...
		setupLocale();

		// Wenn neue Sprachen verfügbar sind, darf der User erneut auswählen.
		// Eventuell ist jetzt seine bevorzugte Muttersprache endlich dabei.
		if(FNX.intval(cfg(CFG_TRANSLATION)) < TRANSLATION_VERSION)
		{
			FNX.dbgf("New translation(s) available! [cfg=%s; cur=%d]", cfg(CFG_TRANSLATION), TRANSLATION_VERSION);

			selectLanguage(true, (cfg(CFG_TRANSLATION) != null ? true : false));
			cfg(CFG_TRANSLATION, Integer.toString(TRANSLATION_VERSION));
		}

		String apihost = cfg(CFG_API);
		if(apihost != null && apihost.length() > 0)
		{
			FNX.dbg("API FQDN: " + apihost);
			eSportsAPI.setHost(apihost);
		}

		// Wird u.a. für das Kontextmenü bei Eingaben benötigt.
		UIManager.addAuxiliaryLookAndFeel(new FNX_LookAndFeel());

		Font smallPlain = new Font(Font.SANS_SERIF, Font.PLAIN,  (int) Math.round(FONTSIZE * FONTSMALL));
		Font smallBold  = new Font(Font.SANS_SERIF, Font.BOLD,   (int) Math.round(FONTSIZE * FONTSMALL));
		Font plain      = new Font(Font.SANS_SERIF, Font.PLAIN,  FONTSIZE);
		Font bold       = new Font(Font.SANS_SERIF, Font.BOLD,   FONTSIZE);

		Color white     = new Color(255, 255, 255); // #fff
		Color black     = new Color(  0,   0,   0); // #000
		Color darkGray  = new Color(136, 136, 136); // #888
		Color lightGray = new Color(204, 204, 204); // #ccc
		Color lightBlue = new Color( 68, 136, 255); // #48f

		// http://nadeausoftware.com/articles/2008/11/all_ui_defaults_names_common_java_look_and_feels_windows_mac_os_x_and_linux
		UIManager.put("Menu.font",                                      bold);
		UIManager.put("MenuItem.font",                                  plain);
		UIManager.put("MenuItem.acceleratorFont",                       smallPlain);
		UIManager.put("Button.font",                                    bold);
		UIManager.put("OptionPane.buttonFont",                          bold);
		UIManager.put("OptionPane.messageFont",                         plain);
		UIManager.put("TableHeader.font",                               plain);
		UIManager.put("Table.font",                                     plain);
		UIManager.put("TextField.font",                                 plain);
		UIManager.put("ComboBox.font",                                  plain);
		UIManager.put("List.font",                                      plain);
		UIManager.put("List.font",                                      plain);
		UIManager.put("Label.font",                                     plain);

		// Tabelle
		UIManager.put("Table.gridColor",                                darkGray);

		// Tabellenheader
		UIManager.put("TableHeader.cellBorder",                         darkGray);
		UIManager.put("TableHeader.background",                         darkGray);
		UIManager.put("TableHeader.foreground",                         white);

		// Normale Tabellenzeilen
		UIManager.put("Table.background",                               white);
		UIManager.put("Table.foreground",                               black);

		/*
		// Markierte Tabellenzeilen
		UIManager.put("Table.selectionBackground",                      lightBlue);
		UIManager.put("Table.selectionForeground",                      white);
		*/

		/*
		// Normale Menüs
		UIManager.put("MenuBar.background",                             lightGray);
		UIManager.put("MenuBar.foreground",                             black);

		// Aktive Menüs
		UIManager.put("Menu.selectionBackground",                       white);
		UIManager.put("Menu.selectionForeground",                       black);

		// Normale Menüzeilen
		UIManager.put("MenuItem.background",                            lightGray);
		UIManager.put("MenuItem.foreground",                            black);
		UIManager.put("MenuItem.acceleratorForeground",                 lightBlue);

		// Aktive Menüzeilen
		UIManager.put("MenuItem.selectionBackground",                   white);
		UIManager.put("MenuItem.selectionForeground",                   black);
		UIManager.put("MenuItem.acceleratorSelectionForeground",        lightBlue);

		// Deaktivierte Menüzeilen
		UIManager.put("MenuItem.disabledBackground",                    lightGray);
		UIManager.put("MenuItem.disabledForeground",                    darkGray);

		// Trennlinien in Menüs
		UIManager.put("Separator.foreground",                           lightGray);
		*/

		ImprovedFileChooser.setLanguageStrings();

		// Diverse Übersetzungen für Systemdialoge
		UIManager.put("OptionPane.okButtonText",                        FNX.getLangString(lang, "ok"));
		UIManager.put("OptionPane.cancelButtonText",                    FNX.getLangString(lang, "cancel"));
		UIManager.put("OptionPane.yesButtonText",                       FNX.getLangString(lang, "yes"));
		UIManager.put("OptionPane.noButtonText",                        FNX.getLangString(lang, "no"));

		mainWindow = new JFrame(APPLICATION_TITLE);
		mainWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainWindow.setJMenuBar(getMenubar());

		mainWindow.addWindowListener(new HTGT_WindowAdapter());

		Object rowData[][] = {};
		Object columnNames[] = new String[6];
		columnNames[0] = FNX.getLangString(lang, "player");
		columnNames[1] = FNX.getLangString(lang, "gameMode");
		columnNames[2] = FNX.getLangString(lang, "trackName");
		columnNames[3] = FNX.getLangString(lang, "weatherName");
		columnNames[4] = FNX.getLangString(lang, "skiSettings");
		columnNames[5] = FNX.getLangString(lang, "timeResult");

		mainPane = mainWindow.getContentPane();
		mainmodel = new DefaultTableModel(rowData, columnNames);
		maintable = new HTGT_JTable(mainmodel);

		// Nur ganze Zeilen dürfen markiert werden!
		maintable.setColumnSelectionAllowed(false);
		maintable.setFocusable(false);

		// Spalten dürfen nicht verschoben oder verkleinert werden!
		maintable.getTableHeader().setReorderingAllowed(false);
		maintable.getTableHeader().setResizingAllowed(false);

		// macOS würde z.B. gar keine Rahmen anzeigen.
		// Das dürfte aber an der weißen Farbe liegen.
		maintable.setShowHorizontalLines(true);
		maintable.setShowVerticalLines(true);

		// Für die Menüelemente müssen wir wissen, wann eine Auswahl getroffen wurde.
		maintable.getSelectionModel().addListSelectionListener(new HTGT_SelectionHandler());

		// ...
		maintable.requestFocusInWindow();

		JScrollPane scrollPane = new JScrollPane(maintable);
		mainWindow.add(scrollPane, BorderLayout.CENTER);

		reset();

		mainWindow.setSize(WINDOW_SIZE_START);
		mainWindow.setMinimumSize(WINDOW_SIZE_MIN);
		mainWindow.setVisible(true);

		// Die automatische Updateprüfung wird im Hintergrund ausgeführt...
		new Thread(new HTGT_Background(HTGT_Background.EXEC_UPDATECHECK)).start();

		// DEBUG ONLY !!!
		//openDefaultFile();
		//fastFollowTest();
	}

	public static void blur()
	{
		if(!ENABLE_BLURRY)
		{
			return;
		}
		else if(mainLayer == null)
		{
			mainLayer = new BlurLayerUI();
		}

		mainWindow.setVisible(false);
		FNX.dbg("Blurring main window...");
		mainWindow.setContentPane(new JLayer<Container>(mainPane, mainLayer));
		mainWindow.setVisible(true);
	}

	public static void unblur()
	{
		if(!ENABLE_BLURRY)
		{
			return;
		}

		mainWindow.setVisible(false);
		FNX.dbg("Unblurring main window...");
		mainWindow.setContentPane(mainPane);
		mainWindow.setVisible(true);
	}

	private static JMenuBar getMenubar()
	{
		// TODO: Statt dem UTF-8 Symbol ein PNG-Icon verwenden?
		// Das unterstützt der Font unter Windows nämlich nicht.
		// ...

		//JButton langButton = new JButton(String.format("🗺 %s", FNX.getLangString(lang, "language")));
		JButton langButton = new JButton(String.format("%s", FNX.getLangString(lang, "languageSelectionTitle")));
		langButton.addActionListener(new FlexibleActionHandler(HTGT.class.getName(), "selectLanguage"));
		langButton.setOpaque(true); langButton.setContentAreaFilled(false);
		langButton.setBorderPainted(false); langButton.setFocusable(false);

		JMenuBar menu = new JMenuBar();

		menu.add(getMenu("file"));
		menu.add(getMenu("edit"));
		menu.add(getMenu("view"));
		menu.add(getMenu("api"));
		menu.add(getMenu("help"));

		disableMenuItems();

		menu.add(Box.createHorizontalGlue());
		menu.add(langButton);

		return menu;
	}

	private static JMenu getMenu(String key)
	{
		String langKey = String.format("menu.%s", key);
		String title = FNX.getLangString(lang, langKey);

		JMenu menu = new JMenu(title);

		switch(key)
		{
			case "file":
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".open",                      "openFile",               KeyStroke.getKeyStroke(KeyEvent.VK_O,      CTRL)));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".openDefault",               "openDefaultFile",        KeyStroke.getKeyStroke(KeyEvent.VK_O,      CTRL | SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".save",                      "saveFile",               KeyStroke.getKeyStroke(KeyEvent.VK_S,      CTRL)));
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".saveAs",                    "saveFileAs",             KeyStroke.getKeyStroke(KeyEvent.VK_S,      CTRL | SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".close",                     "closeFile",              KeyStroke.getKeyStroke(KeyEvent.VK_W,      CTRL)));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".quit",                      "quit",                   KeyStroke.getKeyStroke(KeyEvent.VK_Q,      CTRL)));
				break;

			case "edit":
				if(ENABLE_AUTOSAVE)
				{
					menu.add(registerDynMenuItem(MENU_UNDO, langKey + ".undo",                      "undoHistory",            KeyStroke.getKeyStroke(KeyEvent.VK_Z,      CTRL)));
					menu.add(registerDynMenuItem(MENU_REDO, langKey + ".redo",                      "redoHistory",            KeyStroke.getKeyStroke(KeyEvent.VK_Y,      CTRL)));
					menu.addSeparator(); // ------------------------------------------------------------------------------------------------------------------------------------------------------------
				}

				menu.add(registerDynMenuItem(MENU_SELECT,   langKey + ".cut",                       "cutToClipboard",         KeyStroke.getKeyStroke(KeyEvent.VK_X,      CTRL)));
				menu.add(registerDynMenuItem(MENU_SELECT,   langKey + ".copy",                      "copyToClipboard",        KeyStroke.getKeyStroke(KeyEvent.VK_C,      CTRL)));
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".paste",                     "copyFromClipboard",      KeyStroke.getKeyStroke(KeyEvent.VK_V,      CTRL)));
				menu.add(registerDynMenuItem(MENU_SELECT,   langKey + ".delete",                    "deleteRows",             KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, NONE)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".markAll",                   "selectAll",              KeyStroke.getKeyStroke(KeyEvent.VK_A,      CTRL)));
				menu.add(registerDynMenuItem(MENU_SELECT,   langKey + ".invertSelection",           "invertSelection",        KeyStroke.getKeyStroke(KeyEvent.VK_I,      CTRL)));
				menu.add(registerDynMenuItem(MENU_SELECT,   langKey + ".markNone",                  "clearSelection",         KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, NONE)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_SELECT,   langKey + ".copyToProfile",             "copyGhosts",             KeyStroke.getKeyStroke(KeyEvent.VK_C,      CTRL | SHIFT)));
				menu.add(registerDynMenuItem(MENU_SELECT,   langKey + ".moveToProfile",             "moveGhosts",             KeyStroke.getKeyStroke(KeyEvent.VK_M,      CTRL | SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".import",                    "importFile",             KeyStroke.getKeyStroke(KeyEvent.VK_I,      CTRL | SHIFT)));
				menu.add(registerDynMenuItem(MENU_SELECT,   langKey + ".export",                    "exportFile",             KeyStroke.getKeyStroke(KeyEvent.VK_E,      CTRL | SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".resort",                    "resort",                 KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,  ALT)));
				break;

			case "view":
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".selectProfile",             "selectProfile",          KeyStroke.getKeyStroke(KeyEvent.VK_F6,     NONE)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".selectSpecialProfile",      "selectSpecialProfile",   KeyStroke.getKeyStroke(KeyEvent.VK_S,      SHIFT)));
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".selectDefaultProfile",      "selectDefaultProfile",   KeyStroke.getKeyStroke(KeyEvent.VK_D,      SHIFT)));
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".selectRegularProfile",      "selectRegularProfile",   KeyStroke.getKeyStroke(KeyEvent.VK_A,      SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".selectPrevProfile",         "selectPrevProfile",      KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,   NONE)));
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".selectNextProfile",         "selectNextProfile",      KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,  NONE)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".createProfile",             "createProfile",          KeyStroke.getKeyStroke(KeyEvent.VK_N,      CTRL | SHIFT)));
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".renameProfile",             "renameProfile",          KeyStroke.getKeyStroke(KeyEvent.VK_R,      CTRL | SHIFT)));
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".deleteProfile",             "deleteProfile",          KeyStroke.getKeyStroke(KeyEvent.VK_D,      CTRL | SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".reload",                    "reloadFile",             KeyStroke.getKeyStroke(KeyEvent.VK_F5,     NONE)));
				break;

			case "api":
				menu.add(registerDynMenuItem(MENU_STOKEN,   langKey + ".uploadGhosts",              "ghostUpload",            KeyStroke.getKeyStroke(KeyEvent.VK_F3,     NONE)));
				menu.add(registerDynMenuItem(MENU_FTOKEN,   langKey + ".downloadGhost",             "ghostSelect",            KeyStroke.getKeyStroke(KeyEvent.VK_F4,     NONE)));
				menu.add(registerDynMenuItem(MENU_FTOKEN,   langKey + ".downloadGhostsByIDs",       "ghostDownload",          KeyStroke.getKeyStroke(KeyEvent.VK_F4,     SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_FTOKEN,   langKey + ".fastFollowMode",            "fastFollow",             KeyStroke.getKeyStroke(KeyEvent.VK_F7,     NONE)));
				menu.add(registerDynMenuItem(MENU_FTOKEN,   langKey + ".fastFollowModeForce",       "fastFollowForce",        KeyStroke.getKeyStroke(KeyEvent.VK_F8,     NONE)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_TOKEN,    langKey + ".displayInfo",               "playerInfo",             KeyStroke.getKeyStroke(KeyEvent.VK_F9,     NONE)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_FTOKEN,   langKey + ".copyTokenToProfile",        "copyTokenToProfile",     KeyStroke.getKeyStroke(KeyEvent.VK_T,      CTRL | SHIFT)));
				menu.add(registerDynMenuItem(MENU_PTOKEN,   langKey + ".copyTokenFromProfile",      "copyTokenFromProfile",   KeyStroke.getKeyStroke(KeyEvent.VK_U,      CTRL | SHIFT)));
				menu.add(registerDynMenuItem(MENU_PTOKEN,   langKey + ".removeTokenFromProfile",    "removeTokenFromProfile"));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".changeToken",               "setupToken",             KeyStroke.getKeyStroke(KeyEvent.VK_F2,     NONE)));
				menu.add(registerDynMenuItem(MENU_TOKEN,    langKey + ".deleteToken",               "deleteToken",            KeyStroke.getKeyStroke(KeyEvent.VK_F2,     SHIFT)));
				break;

			case "help":
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".about",                     "about"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".manual",                    "faq",                    KeyStroke.getKeyStroke(KeyEvent.VK_F1,     NONE)));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".support",                   "support",                KeyStroke.getKeyStroke(KeyEvent.VK_F1,     SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".updateCheckApp",            "updateCheck",            KeyStroke.getKeyStroke(KeyEvent.VK_U,      CTRL)));
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".updateCheckDLL",            "updateCheckDLL",         KeyStroke.getKeyStroke(KeyEvent.VK_U,      CTRL | SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".changeDefaultPath",         "changeDefaultFile",      KeyStroke.getKeyStroke(KeyEvent.VK_D,      SHIFT)));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".resetDefaultPath",          "resetDefaultFile",       KeyStroke.getKeyStroke(KeyEvent.VK_R,      SHIFT)));
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".applyDefaultPath",          "applyDefaultFile"));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".disableQuestions",          "disableQuestions"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".enableQuestions",           "enableQuestions"));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".resetConfiguration",        "clearConfigDialog",      KeyStroke.getKeyStroke(KeyEvent.VK_R,      CTRL)));
				break;

			default:
				FNX.dbgf("Unknown menu »%s«", key);
				return null;
		}

		return menu;
	}

	private static JMenuItem registerDynMenuItem(String o, String t, String m)
	{
		return registerDynMenuItem(o, t, m, null);
	}

	private static JMenuItem registerDynMenuItem(String o, String t, String m, KeyStroke k)
	{
		if(menuitems == null)
		{
			menuitems = new HashMap<String,ArrayList<DynamicMenuItem>>();
		}

		if(menuitems.get(o) == null)
		{
			menuitems.put(o, new ArrayList<DynamicMenuItem>());
		}

		DynamicMenuItem DMI = new DynamicMenuItem(FNX.getLangString(lang, t), HTGT.class.getName(), m, k);
		menuitems.get(o).add(DMI);

		return DMI;
	}

	private static void updateMenuItems()
	{
		String  token  = null;
		String  ptoken = null;
		boolean op     = false;

		try
		{
			token = cfg(CFG_TOKEN);

			if(OfflineProfiles != null)
			{
				op = true;
				ptoken = OfflineProfiles.getToken();
			}
			else
			{
				op = false;
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}

		if(token != null)
		{
			enableMenuItems(MENU_TOKEN);
		}
		else
		{
			disableMenuItems(MENU_TOKEN);
		}

		if(token != null && op)
		{
			enableMenuItems(MENU_FTOKEN);
		}
		else
		{
			disableMenuItems(MENU_FTOKEN);
		}

		if(ptoken != null)
		{
			enableMenuItems(MENU_PTOKEN);
		}
		else
		{
			disableMenuItems(MENU_PTOKEN);
		}

		if(op)
		{
			enableMenuItems(MENU_DEFAULT);
		}
		else
		{
			disableMenuItems(MENU_DEFAULT);
			disableMenuItems(MENU_UNDO);
			disableMenuItems(MENU_REDO);
		}

		updateSelectionMenuItems();
	}

	public static void updateSelectionMenuItems()
	{
		if(maintable != null && maintable.getSelectedRows().length > 0)
		{
			updateSelectionMenuItems(true);
		}
		else
		{
			updateSelectionMenuItems(false);
		}
	}

	public static void updateSelectionMenuItems(Boolean action)
	{
		if(OfflineProfiles == null)
		{
			action = null;
		}

		disableMenuItems(MENU_SELECT);
		disableMenuItems(MENU_STOKEN);

		if(action != null && action)
		{
			if(cfg(CFG_TOKEN) != null)
			{
				enableMenuItems(MENU_STOKEN);
			}

			enableMenuItems(MENU_SELECT);
		}
	}

	private static void disableMenuItems()
	{
		updateMenuItems();
	}

	private static void disableMenuItems(String o)
	{
		changeMenuItems(o, false);
	}

	private static void enableMenuItems()
	{
		updateMenuItems();
	}

	private static void enableMenuItems(String o)
	{
		changeMenuItems(o, true);
	}

	private static void changeMenuItems(String o, boolean e)
	{
		if(menuitems != null && menuitems.get(o) != null)
		{
			for(int i = 0; i < menuitems.get(o).size(); i++)
			{
				menuitems.get(o).get(i).setEnabled(e);
			}
		}
	}

	private static void reset()
	{
		dll             = null;
		file            = null;
		OfflineProfiles = null;
		profile         = 0;

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

	private static void highlightRows(int[] rows)
	{
		maintable.clearSelection();
		for(int i = 0; i < rows.length; i++)
		{
			maintable.addRowSelectionInterval(rows[i], rows[i]);
		}
	}

	public static void selectAll()
	{
		if(maintable != null)
		{
			maintable.selectAll();
		}
	}

	public static void clearSelection()
	{
		if(maintable != null)
		{
			maintable.clearSelection();
		}
	}

	public static void invertSelection()
	{
		if(maintable != null)
		{
			int[] selection = maintable.getSelectedRows();
			maintable.selectAll();

			for(int i = 0; i < selection.length; i++)
			{
				maintable.removeRowSelectionInterval(selection[i], selection[i]);
			}
		}
	}

	public static void updateWindowTitle()
	{
		String filename = "";
		String profilename = "";
		String suffix = "";

		if(OfflineProfiles != null)
		{
			profilename = " – " + nickname;
			filename = " – " + file.getAbsolutePath();

			//profilename = ": " + nickname + "";
			//filename = " @ " + file.getAbsolutePath();

			if(OfflineProfiles.changed())
			{
				suffix = " *";
			}
		}

		mainWindow.setTitle(APPLICATION_TITLE + profilename + filename + suffix);
		//mainWindow.setTitle(APPLICATION_NAME + profilename + filename + suffix);
	}

	public static int ghostImport(File f) throws Exception
	{
		return ghostImport(f, false);
	}

	public static int ghostImport(File f, boolean force) throws Exception
	{
		return ghostImport(GhostElement.parseGhosts(f), force);
	}

	public static int ghostImport(String xmlstring) throws Exception
	{
		return ghostImport(xmlstring, false);
	}

	public static int ghostImport(String xmlstring, boolean force) throws Exception
	{
		return ghostImport(GhostElement.parseGhosts(xmlstring), force);
	}

	public static int ghostImport(GhostElement ghost)
	{
		return ghostImport(ghost, false);
	}

	public static int ghostImport(GhostElement ghost, boolean force)
	{
		return ghostImport(new GhostElement[]{ghost}, force);
	}

	public static int ghostImport(GhostElement[] ghosts)
	{
		return ghostImport(ghosts, false);
	}

	public static int ghostImport(GhostElement[] ghosts, boolean force)
	{
		FNX.dbg("ghosts.length: " + ghosts.length);
		ArrayList<Integer> selection = new ArrayList<Integer>();
		boolean deleteDuplicates = isSpecialProfile() ? false : true;
		boolean delete = false;

		if(ghosts.length > 0)
		{
			if(!force && deleteDuplicates)
			{
				for(int i = 0; i < ghosts.length; i++)
				{
					if(OfflineProfiles.getGhostsByCondition(ghosts[i]).length > 0)
					{
						delete = true;
					}
				}

				if(delete)
				{
					if(OfflineProfiles == null || !confirmGhostReplacement())
					{
						return -1;
					}
				}
			}

			for(int i = 0; i < ghosts.length; i++)
			{
				addGhost(ghosts[i], true);
				ghosts[i].printDetails();

				if(deleteDuplicates)
				{
					int[] ghostDel = OfflineProfiles.getGhostsByCondition(ghosts[i]);
					for(int h = ghostDel.length - 2; h > -1; h--)
					{
						deleteGhost(ghostDel[h]);
					}
				}
			}

			if(!deleteDuplicates)
			{
				highlightLastRows(ghosts.length);
			}
			else
			{
				for(int i = 0; i < ghosts.length; i++)
				{
					for(int h = 0; h < OfflineProfiles.getGhostCount(); h++)
					{
						if(OfflineProfiles.getGhost(h).getConditions().equals(ghosts[i].getConditions()))
						{
							selection.add(h);
						}
					}
				}

				highlightRows(selection.stream().mapToInt(i -> i).toArray());
			}
		}

		return ghosts.length;
	}

	public static void selectProfile()
	{
		selectProfile(true);
	}

	public static void selectProfile(boolean all)
	{
		String suffix = null;
		String selection = null;
		String[] profiles;
		String[] values;

		int c, h;
		c = h = 0;

		try
		{
			profiles = OfflineProfiles.getProfiles();
			values = new String[profiles.length];

			for(int i = 0; i < profiles.length; i++)
			{
				if(i == OfflineProfiles.defaultProfile())
				{
					suffix = FNX.getLangString(lang, "defaultProfile");
				}
				else if(isSpecialProfile(profiles[i]))
				{
					suffix = FNX.getLangString(lang, "specialProfile");
				}
				else
				{
					suffix = "";
				}

				if(suffix.length() > 0)
				{
					if(!all)
					{
						continue;
					}

					suffix = String.format(" (%s)", suffix);
				}

				values[i] = String.format("[%0" + Integer.toString(FNX.strlen(profiles.length)) + "d] %s%s", i + 1, profiles[i], suffix);
				c++;

				if(profile == i)
				{
					selection = values[i];
				}
			}

			if(c != values.length)
			{
				// -------------------------------------------- //
				// Diese Lösung ist leider noch nicht perfekt.  //
				// Damit sie keine Probleme macht, geht das     //
				// Programm davon aus, dass das Spezial- und    //
				// Standardprofil am Ende der Liste existieren. //
				// Das ist zwar standardmäßig der Fall, aber    //
				// eigentlich eine nicht korrekte Vermutung.    //
				// -------------------------------------------- //

				String[] realValues = new String[c];
				for(int i = 0; i < values.length; i++)
				{
					if(values[i] != null)
					{
						realValues[h++] = values[i];
					}
				}
				values = realValues;
			}

			Integer selected = (Integer) inputDialog(FNX.getLangString(lang, "profileSelectionTitle"), FNX.formatLangString(lang, "profileSelectionBody"), values, selection);

			if(selected == null)
			{
				return;
			}

			selectProfile(selected);
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	public static void selectProfile(int index) throws Exception
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
			nickname = OfflineProfiles.getProfiles()[index];
			int lastProfile = PROFILE_NONE;

			if(OfflineProfiles.defaultProfile() == index)
			{
				lastProfile = PROFILE_DEFAULT;
			}
			else if(isSpecialProfile(index))
			{
				lastProfile = PROFILE_SPECIAL;
			}
			else
			{
				cfg(CFG_RPROFILE, Integer.toString(index));

				lastProfile = index + 1;
			}

			cfg(CFG_PROFILE, Integer.toString(lastProfile));
		}

		profile = index;
		syncGUI();
	}

	public static void selectNextProfile()
	{
		selectNearProfile(1);
	}

	public static void selectPrevProfile()
	{
		selectNearProfile(-1);
	}

	private static void selectNearProfile(int n)
	{
		if(OfflineProfiles == null)
		{
			return;
		}

		n = profile + n;

		if(n >= OfflineProfiles.getProfileCount())
		{
			n = 0;
		}
		else if(n < 0)
		{
			n = OfflineProfiles.getProfileCount() - 1;
		}

		try
		{
			selectProfile(n);
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	public static void selectProfileByNumber(Integer i)
	{
		FNX.dbgf("i=%d", i);

		try
		{
			if(i > 0)
			{
				selectProfile(i - 1);
			}
			else if(i < 0)
			{
				selectSpecialProfile();
			}
			else
			{
				selectDefaultProfile();
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	public static void selectLastProfile()
	{
		if(OfflineProfiles == null)
		{
			return;
		}

		try
		{
			int lastProfile = FNX.intval(cfg(CFG_PROFILE));
			int selectedProfile = PROFILE_NONE;

			if(lastProfile == PROFILE_DEFAULT)
			{
				selectedProfile = OfflineProfiles.defaultProfile();
				FNX.dbgf("Last used profile: DEFAULT (%d)", selectedProfile);
			}
			else if(lastProfile == PROFILE_SPECIAL)
			{
				String[] profiles = OfflineProfiles.getProfiles();

				for(int i = 0; i < profiles.length; i++)
				{
					if(isSpecialProfile(profiles[i]))
					{
						selectedProfile = i;
						FNX.dbgf("Last used profile: SPECIAL (%d)", selectedProfile);
						break;
					}
				}
			}
			else if(lastProfile != PROFILE_NONE)
			{
				selectedProfile = lastProfile - 1;
				selectedProfile = (selectedProfile < 0 || selectedProfile >= OfflineProfiles.getProfileCount()) ? PROFILE_NONE : selectedProfile;
				FNX.dbgf("Last used profile: %d", selectedProfile);
			}
			else
			{
				FNX.dbg("Last used profile unknown...");
			}

			selectProfile(selectedProfile);
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	public static void selectDefaultProfile()
	{
		try
		{
			int defaultProfile = OfflineProfiles.defaultProfile();

			if(defaultProfile < 0)
			{
				throw new Exception("Default profile not found!");
			}
			else
			{
				selectProfile(defaultProfile);
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	public static void selectSpecialProfile()
	{
		try
		{
			String[] profiles = OfflineProfiles.getProfiles();

			for(int i = 0; i < profiles.length; i++)
			{
				if(isSpecialProfile(profiles[i]))
				{
					selectProfile(i);
					return;
				}
			}

			throw new Exception("Special profile not found!");
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	public static void selectRegularProfile()
	{
		try
		{
			selectProfile(FNX.intval(cfg(CFG_RPROFILE)));
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	public static void syncGUI()
	{
		if(!FNX.requireEDT())
		{
			return;
		}

		updateWindowTitle();
		hideTableHeader();
		clearTable();

		if(OfflineProfiles != null)
		{
			if(OfflineProfiles.getGhostCount() > 0)
			{
				showTableHeader();

				for(int i = 0; i < OfflineProfiles.getGhostCount(); i++)
				{
					addGhost(OfflineProfiles.getGhost(i), false);
				}
			}
		}

		updateMenuItems();
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
				exceptionHandler(e);
				return;
			}
		}

		Object tmp[] = {ghost.getNickname(), ghost.getGameModeName(), ghost.getTrackName(), ghost.getWeatherName(), gmHelper.formatSki(ghost.getSki()), ghost.getResult()};
		mainmodel.addRow(tmp);
		showTableHeader();
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
			exceptionHandler(e);
		}
	}

	private static boolean confirmGhostReplacement()
	{
		return confirmGhostReplacement(null);
	}

	private static boolean confirmGhostReplacement(String title)
	{
		int action;

		if(cfg(CFG_ARG) != null)
		{
			FNX.dbg("Forcing ghost replacement because of previous choice...");
			action = BUTTON_YES;
		}
		else
		{
			action = threesomeDialog(JOptionPane.WARNING_MESSAGE, title, FNX.formatLangString(lang, "confirmGhostReplacement"), true);
		}

		if(action == BUTTON_ALWAYS)
		{
			cfg(CFG_ARG, "true");
			action = BUTTON_YES;
		}

		if(action == BUTTON_YES)
		{
			return true;
		}

		return false;
	}

	private static boolean checkProfile()
	{
		return checkProfile(false);
	}

	private static boolean checkProfile(boolean simple)
	{
		try
		{
			if(profile == OfflineProfiles.defaultProfile() || isSpecialProfile())
			{
				if(simple && getRegularProfileCount() == 1)
				{
					FNX.dbg("There is only one regular profile!");
					selectProfile(0);
				}
				else
				{
					if(confirmDialog(FNX.formatLangString(lang, "incompatibleProfile")))
					{
						selectProfile(false);
					}

					if(profile == OfflineProfiles.defaultProfile() || isSpecialProfile())
					{
						return true;
					}
				}
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}

		return false;
	}

	public static void fastFollowForce()
	{
		fastFollow(true);
	}

	public static void fastFollow()
	{
		fastFollow(false);
	}

	private static boolean fastFollowMode() throws Exception
	{
		if(OfflineProfiles == null || checkProfile(true) || unsavedChanges() || !prepareAPI())
		{
			return false;
		}
		else if(ffDownload)
		{
			// Es wurde nur der Download angefordert.
			// Der Upload ist bereits zuvor passiert.
			return fastFollowDownload();
		}
		ffDownload = false;

		FNX.dbg("Go...");

		try
		{
			int[][][][] results;
			GhostElement[][][] oldProfileGhosts = null;
			GhostElement[][][] oldDefaultGhosts = null;
			int oldProfileCount = OfflineProfiles.getProfileCount();
			int oldDefaultProfile = OfflineProfiles.defaultProfile();

			// DEBUY ONLY !!!
			FNX.dbg("----- GHOSTS BEFORE RELOADING -----");
			for(int i = 0; i < OfflineProfiles.getGhostCount(); i++)
			{
				FNX.dbgf("  #%02d: %s", i, OfflineProfiles.getGhost(i).getDebugDetails());
			}
			FNX.dbg("-----------------------------------");

			oldProfileGhosts = OfflineProfiles.getAllGhosts();

			if(oldDefaultProfile > -1)
			{
				OfflineProfiles.selectProfile(oldDefaultProfile);
				oldDefaultGhosts = OfflineProfiles.getAllGhosts();
				OfflineProfiles.selectProfile(profile);
			}

			OfflineProfiles tmp;

			if(!FNX.isEDT())
			{
				try
				{
					FNX.dbg("Cloning OfflineProfiles object...");
					tmp = new OfflineProfiles(file);
					tmp.selectProfile(profile);
				}
				catch(Exception e)
				{
					exceptionHandler(e);
					return false;
				}
			}
			else
			{
				tmp = OfflineProfiles;
				reloadFile(true);
			}

			// DEBUY ONLY !!!
			FNX.dbg("----- GHOSTS AFTER RELOADING -----");
			for(int i = 0; i < tmp.getGhostCount(); i++)
			{
				FNX.dbgf("  #%02d: %s", i, tmp.getGhost(i).getDebugDetails());
			}
			FNX.dbg("----------------------------------");

			GhostElement[][][] newProfileGhosts = null;
			GhostElement[][][] newDefaultGhosts = null;
			int newProfileCount = tmp.getProfileCount();
			int newDefaultProfile = tmp.defaultProfile();

			if(oldProfileCount != newProfileCount || oldDefaultProfile != newDefaultProfile)
			{
				FNX.dbgf("Unsupported changes: %d != %d || %d != %d", oldProfileCount, newProfileCount, oldDefaultProfile, newDefaultProfile);
				errorMessage(FNX.getLangString(lang, "fastFollowMode"), "Unsupported changes detected!");
				return false;
			}

			newProfileGhosts = tmp.getAllGhosts();

			if(newDefaultProfile > -1)
			{
				FNX.dbgf("Switching to profile %d...", newDefaultProfile);
				tmp.selectProfile(newDefaultProfile);

				newDefaultGhosts = tmp.getAllGhosts();

				FNX.dbgf("Using old profile %d...", profile);
				tmp.selectProfile(profile);
			}

			int[] modes = gmHelper.getGameModeIDs();
			String[] tracks = gmHelper.getTracks(true);
			int[] weathers = gmHelper.getWeatherIDs();

			String currentGhost = null;
			int lastUploadedMode = -1;
			int lastUploadedTrack = -1;
			int lastUploadedWeather = -1;
			boolean lastFromDefault = false;
			boolean realUpload = false;

			ArrayList<ArrayList> ghosts = new ArrayList<ArrayList>();

			for(int m = 0; m < modes.length; m++)
			{
				for(int t = 0; t < tracks.length; t++)
				{
					for(int w = 0; w < weathers.length; w++)
					{
						//FNX.dbgf("[%02d][%02d][%02d]: %s - %s", m, t, w, (oldProfileGhosts[m][t][w] == null ? "null" : "data"), (newProfileGhosts[m][t][w] == null ? "null" : "data"));

						if((oldProfileGhosts[m][t][w] == null && newProfileGhosts[m][t][w] != null) || (oldProfileGhosts[m][t][w] != null && newProfileGhosts[m][t][w] != null && (oldProfileGhosts[m][t][w].getTime() != newProfileGhosts[m][t][w].getTime() || !newProfileGhosts[m][t][w].getHash().equals(oldProfileGhosts[m][t][w].getHash()))))
						{
							FNX.dbgf("Changed result: %s / %s / %s", gmHelper.getGameModeName(modes[m]), gmHelper.getTrack(tracks[t]), gmHelper.getWeatherName(weathers[w]));

							ArrayList<Object> item = new ArrayList<Object>(4);
							item.add(m); item.add(t); item.add(w);
							item.add(newProfileGhosts[m][t][w]);
							ghosts.add(item);

							lastUploadedMode = m;
							lastUploadedTrack = t;
							lastUploadedWeather = w;

							if(newProfileGhosts[m][t][w].hasTicket())
							{
								lastUploadedWeather = gmHelper.WEATHER_TICKET;
							}
						}
					}
				}
			}

			if(newDefaultProfile > -1)
			{
				for(int m = 0; m < modes.length; m++)
				{
					for(int t = 0; t < tracks.length; t++)
					{
						for(int w = 0; w < weathers.length; w++)
						{
							if((oldDefaultGhosts[m][t][w] == null && newDefaultGhosts[m][t][w] != null) || (oldDefaultGhosts[m][t][w] != null && newDefaultGhosts[m][t][w] != null && (oldDefaultGhosts[m][t][w].getTime() != newDefaultGhosts[m][t][w].getTime() || !newDefaultGhosts[m][t][w].getHash().equals(oldDefaultGhosts[m][t][w].getHash()))))
							{
								FNX.dbgf("Changed (default) result: %s / %s / %s", gmHelper.getGameModeName(modes[m]), gmHelper.getTrack(tracks[t]), gmHelper.getWeatherName(weathers[w]));

								ArrayList<Object> item = new ArrayList<Object>(4);
								item.add(m); item.add(t); item.add(w);
								item.add(newDefaultGhosts[m][t][w]);
								ghosts.add(item);

								lastUploadedMode = m;
								lastUploadedTrack = t;
								lastUploadedWeather = w;
								lastFromDefault = true;

								if(newDefaultGhosts[m][t][w].hasTicket())
								{
									lastUploadedWeather = gmHelper.WEATHER_TICKET;
								}

								// gmHelper.WEATHER_SUC?
								// ...
							}
						}
					}
				}
			}

			if(ghosts.size() > 0)
			{
				FNX.dbgf("ghosts.size() = %d", ghosts.size());

				// TODO: Wir brauchen einen Uploadcache in der API-Klasse!
				// Andernfalls würden wir beim EDT alles nochmals hochladen.
				// Nicht vergessen, dass dieser Cache immer geleert gehört.
				// ...

				if(ffForce)
				{
					for(int i = 0; i < ghosts.size(); i++)
					{
						ArrayList item = ghosts.get(i);
						GhostElement ghost = (GhostElement) item.get(3);
						int w = (int) item.get(2);

						FNX.dbgf("Uploading ghost in FORCE mode: %s", ghost.getDebugDetails());

						if(ghostUploadExtended(new GhostElement[]{ghost}, true, false) < 1)
						{
							return false;
						}

						realUpload = lastApplicationStatus;

						FNX.dbgf("Last FO from server reply: %d", lastFilterOption);

						switch(lastFilterOption)
						{
							case eSportsAPI.FO_TICKET:
								lastUploadedWeather = gmHelper.WEATHER_TICKET;
								break;

							case eSportsAPI.FO_SUC:
								lastUploadedWeather = gmHelper.WEATHER_SUC;
								break;

							default: // eSportsAPI.FO_NONE
								lastUploadedWeather = w;
								break;
						}

						FNX.dbgf("Set lastUploadedWeather to: %d", lastUploadedWeather);
					}
				}
				else
				{
					int[][] filter = new int[ghosts.size()][3];
					for(int i = 0; i < ghosts.size(); i++)
					{
						filter[i][0] = modes[(int) ghosts.get(i).get(0)];
						filter[i][1] = (int) ghosts.get(i).get(1);
						filter[i][2] = weathers[(int) ghosts.get(i).get(2)];
					}

					results = api.getSelectiveResults(filter);

					for(int i = 0; i < ghosts.size(); i++)
					{
						ArrayList item = ghosts.get(i);
						GhostElement ghost = (GhostElement) item.get(3);
						int w = (int) item.get(2);
						int t = (int) item.get(1);
						int m = (int) item.get(0);
						int o = eSportsAPI.FO_NONE;

						if(ghost.hasTicket())
						{
							o = eSportsAPI.FO_TICKET;
						}

						// eSportsAPI.FO_SUC?
						// ...

						if(results[o][m][t][w] == -1|| (!gmHelper.isReverseGameMode(m) && ghost.getTime() < results[o][m][t][w]) || (gmHelper.isReverseGameMode(m) && ghost.getTime() > results[o][m][t][w]))
						{
							FNX.dbgf("Uploading ghost: %s", ghost.getDebugDetails());

							if(ghostUploadExtended(new GhostElement[]{ghost}, true, false) < 1)
							{
								return false;
							}

							realUpload = true;
						}
						else
						{
							FNX.dbgf("Ghost upload not possible, because old result (%d) is better or equal: %s", results[o][m][t][w], ghost.getDebugDetails());

							if(o == eSportsAPI.FO_TICKET)
							{
								FNX.dbg("Still uploading it because it's a TICKET ghost...");

								if(ghostUploadExtended(new GhostElement[]{ghost}, true, true) < 1)
								{
									return false;
								}
							}

							// eSportsAPI.FO_SUC?
							// ...
						}
					}
				}
			}
			else
			{
				FNX.dbg("Nothing to upload...");
			}

			if(realUpload && lastUploadedMode > -1 && lastUploadedTrack > -1 && lastUploadedWeather != -1 && !foreignGhostEnabled())
			{
				if(cfg(CFG_NDG) == null)
				{
					// Der Download passiert in einer eigenen Methode.
					// Dieser muss womöglich im EDT ausgeführt werden.
					if(!fastFollowDownload())
					{
						return false;
					}
				}
				else
				{
					FNX.dbg("Skipping ghost download because of previous choice...");
				}
			}

			FNX.dbg("End of try block...");
		}
		/*
		catch(InterruptedException e)
		{
			e.printStackTrace();
			return false;
		}
		catch(eSportsAPIException e)
		{
			APIError(e);
			return false;
		}
		catch(Exception e)
		{
			exceptionHandler(e);
			return false;
		}
		*/
		finally
		{
			FNX.dbg("Final block...");

			if(FNX.isEDT())
			{
				try
				{
					FNX.dbg("Final try block...");

					// Das ursprüngliche Profil aktivieren!
					FNX.dbgf("Restoring profile %d...", profile);
					OfflineProfiles.selectProfile(profile);

					FNX.dbg("Final try block finished...");
				}
				catch(Exception e)
				{
					FNX.dbg("Final block: Exception!");

					exceptionHandler(e);
					syncGUI();
				}
			}
		}

		// TODO: Exceptions wieder teilweise hier abfangen?
		// Zumindestens im EDT? Oder ist das bereits der Fall?
		// ...

		if(!FNX.isEDT())
		{
			// Da wir mit einem geklonten OfflineProfiles-Objekt arbeiten,
			// muss die originale Instanz noch neu geladen werden. Wenn das
			// Programm bis hier läuft, wird kein Durchlauf im EDT benötigt.
			reloadFile(true);
		}

		FNX.dbg("Bye...");

		return true;
	}

	private static boolean fastFollowDownload()
	{
		ffDownload = false;

		if(!FNX.requireEDT())
		{
			ffDownload = true;

			return false;
		}
		else if(lastApplicationGhost == null || cfg(CFG_NDG) != null)
		{
			FNX.dbg("Nothing to do here...");

			return true;
		}

		try
		{
			String currentGhostLine = null;
			int g = lastApplicationGhost.getGameMode();
			String t = lastApplicationGhost.getTrack();
			int w = lastApplicationGhost.getWeather();
			int r = w;

			switch(lastFilterOption)
			{
				case eSportsAPI.FO_TICKET:
					r = gmHelper.WEATHER_TICKET;
					break;

				case eSportsAPI.FO_SUC:
					r = gmHelper.WEATHER_SUC;
					break;
			}

			if(r > -1)
			{
				reloadFile(true);

				int currentGhosts[] = OfflineProfiles.getGhostsByCondition(g, t, w);

				if(currentGhosts.length > 0)
				{
					GhostElement currentGhost = OfflineProfiles.getGhost(currentGhosts[0]);
					currentGhostLine = String.format("%s%n", FNX.formatLangString(lang, "fastFollowCurrentGhost", currentGhost.getNickname(), currentGhost.getResult()));
				}
			}

			int action = threesomeDialog(FNX.getLangString(lang, "fastFollowMode"),
				FNX.formatLangString(lang, "fastFollowGhostQuestion", gmHelper.getTrack(t), gmHelper.getGameModeName(g), gmHelper.getWeatherName(r)) +
					(!ENABLE_AUTOSAVE ? String.format("%n%s", FNX.formatLangString(lang, "fastFollowNoAutosave")) : "") +
					(currentGhostLine != null ? String.format("%n%s", currentGhostLine) : "")
			, false);

			if(action == BUTTON_NEVER)
			{
				cfg(CFG_NDG, "true");
			}
			else if(action == BUTTON_YES)
			{
				Boolean result = ghostSelect(g, t, r, true, ((r < -1) ? true : false));

				if(result != null && result == true)
				{
					if(OfflineProfiles.changed() && !saveFile(true))
					{
						throw new Exception("Could not save file");
					}
				}
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}

		return true;
	}

	public static void fastFollow(boolean force)
	{
		if(OfflineProfiles == null || checkProfile(true) || unsavedChanges())
		{
			return;
		}

		try
		{
			if(!prepareAPI())
			{
				return;
			}

			fastFollowStart(force);
		}
		/*
		catch(eSportsAPIException e)
		{
			APIError(e);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		*/
		catch(Exception e)
		{
			exceptionHandler(e);
		}
		/*
		finally
		{
			try
			{
				// Das ursprüngliche Profil aktivieren!
				FNX.dbgf("Restoring profile %d...", profile);
				OfflineProfiles.selectProfile(profile);
			}
			catch(Exception e)
			{
				exceptionHandler(e);
				syncGUI();
			}
		}
		*/
	}

	public static Integer fastFollowEvaluation() throws Exception
	{
		if(fastFollowMode())
		{
			FNX.dbg("FFM returned TRUE");

			return 1;
		}

		FNX.dbg("FFM returned FALSE");

		return 0;
	}

	public static boolean fastFollowAnalyze()
	{
		// Wir müssen sicherstellen, dass nur ein Thread gleichzeitig läuft.
		// Beim Observer-Thread ist das noch einfach, da die GUI blockiert ist.
		// Die Analyst-Threads werden jedoch immer sofort bei Bedarf angeworfen.
		if(aFFM != null && !aFFM.isDone())
		{
			FNX.dbg("FFM evaluation thread already running! Skipping...");

			// Wenn der Thread bereits läuft, wird er einfach beim nächsten
			// Durchlauf von process() ausgeführt werden. Das ist unkritisch,
			// weil das Interval niedrig genug ist. Sollten sich zwischenzeitlich
			// mehrere Anfragen ansammeln, sollte das theoretisch auch unkritisch
			// sein, weil immer nur der neueste Request eine Ausführung bewirkt.
			return false;
		}

		aFFM = new HTGT_FFM_Analyst();
		aFFM.execute();

		return true;
	}

	// Die nachfolgende Variable wird durch fastFollowBlock() erstmals auf 1 gesetzt.
	// Durch fastFollowStop() wird sie auf -1 gesetzt, wodurch die Schleife weiterläuft.
	// In anderen allen Fällen (z.B. bei 1) wird die Schleife nach dem JDialog beendet!
	private static void fastFollowStart(boolean force)
	{
		if(!FNX.requireEDT() || OfflineProfiles == null || checkProfile(true) || unsavedChanges() || !prepareAPI())
		{
			return;
		}
		else if(ffStarted != 0)
		{
			FNX.dbg("ffStarted != 0");
			return;
		}
		else
		{
			FNX.dbg("ffStarted > 0");
			ffStarted = 1;
		}

		ffForce = force;
		appliedCount = 0;
		uploadedCount = 0;
		ffDownload = false;
		ffModification = -1;
		lastApplicationPosition = 0;
		lastApplicationGhost = null;
		lastApplicationDestinations = new HashMap();

		boolean firstRun = true;

		while(true)
		{
			try
			{
				blur();

				try
				{
					oFFM = new HTGT_FFM_Observer();
					oFFM.setFile(file);

					if(firstRun)
					{
						// Diese API-Anfrage ist hier noch nicht notwendig.
						// Dadurch wird aber schon hier geprüft, ob der Token
						// gültig ist und ob aktive Strecken verfügbar sind.
						int[][][][] results = api.getAllResults();

						firstRun = false;
						oFFM.firstRun();
					}
					else
					{
						oFFM.secondRun();
					}
				}
				catch(eSportsAPIException e)
				{
					HTGT.fastFollowStop();
					APIError(e);
					break;
				}

				FNX.dbg("Opening new dialog...");

				if(ffBody == null)
				{
					if(ffButton == null)
					{
						ffButton = new JButton(FNX.getLangString(lang, "fastFollowStop"));
						ffButton.addActionListener(new HTGT_FFM_ActionListener());
					}

					ffBody = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE);
					ffBody.setOptions(new Object[]{ffButton});
				}

				fastFollowStatus();

				if(ffDialog == null)
				{
					if(ffListener == null)
					{
						ffListener = new HTGT_FFM_KeyListener();
					}

					ffDialog = ffBody.createDialog(mainWindow, null);
					ffDialog.addKeyListener(ffListener);
					ffDialog.setFocusable(true);
				}

				fastFollowUnlock();

				ffDialog.setTitle(FNX.getLangString(lang, "fastFollowMode" + (ffForce ? "Force" : "")));
				ffDialog.setVisible(true);

				HTGT.fastFollowStop();

				if(ffStarted < 0)
				{
					FNX.dbg("Dialog closed!");
					break;
				}
				else
				{
					FNX.dbg("Hiding dialog...");

					fastFollowMode();

					continue;
				}
			}
			catch(eSportsAPIException e)
			{
				HTGT.fastFollowStop();
				APIError(e);
				continue;
			}
			catch(Exception e)
			{
				HTGT.fastFollowStop();
				exceptionHandler(e);
				break;
			}
			finally
			{
				// Dieser Teil ist unbedingt notwendig!
				// Andernfalls würden die Threads bei
				// Exceptions einfach weiterlaufen.
				fastFollowStop();

				try
				{
					unblur();
					reloadFile(true);
					syncGUI();
				}
				catch(Exception e)
				{
					exceptionHandler(e);
				}
			}
		}

		ffStarted = 0;
	}

	public static void fastFollowLock()
	{
		if(!FNX.requireEDT())
		{
			return;
		}

		FNX.dbg("Locking FFM interface...");

		//if(ffDialog != null)
		//ffDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		if(ffButton != null)
		ffButton.setEnabled(false);

		if(ffListener != null)
		ffListener.disable();
	}

	public static void fastFollowUnlock()
	{
		if(!FNX.requireEDT())
		{
			return;
		}

		FNX.dbg("Unlocking FFM interface...");

		//if(ffDialog != null)
		//ffDialog.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		if(ffDialog != null)
		ffDialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		if(ffButton != null)
		ffButton.setEnabled(true);

		if(ffListener != null)
		ffListener.enable();
	}

	public static void fastFollowStatus()
	{
		fastFollowStatus(-1);
	}

	public static synchronized void fastFollowStatus(int time)
	{
		if(!FNX.requireEDT())
		{
			return;
		}
		else if(time > -1)
		{
			ffModification = time;
		}

		LocalDateTime ldt;
		DateTimeFormatter dtf;
		String stateLine = "";

		if(ffBody != null)
		{
			try
			{
				FNX.dbg("Updating status message...");

				ldt = LocalDateTime.ofEpochSecond(ffModification, 0, OffsetDateTime.now().getOffset());
				dtf = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

				ffBody.setMessage(
					FNX.formatLangString(lang, "fastFollowModeBody", OfflineProfiles.getProfiles()[profile]) +
					FNX.formatLangString(lang, "fastFollowMode" + ((ffModification > 0) ? "Extended" : "Empty"), ldt.format(dtf), uploadedCount, appliedCount) +
					getResultDestinations()
				);

				if(ffDialog != null)
				{
					// Passt die Größe des Dialog-Fensters an den neuen Text an.
					// Dadurch braucht es keine Leerzeilen mehr als Platzhalter.
					ffDialog.pack();
				}

				// reloadFile(true)
				// syncGUI()
			}
			catch(ProfileException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static String getResultDestinations()
	{
		if(!lastApplicationDestinations.isEmpty())
		{
			StringBuilder data = new StringBuilder();
			data.insert(0, System.lineSeparator());

			int i = 0; int[] r = new int[lastApplicationDestinations.size()];
			for(Map<String,Object> v : lastApplicationDestinations.values())
			{
				// TODO: Limit einbauen, falls es zu viele GR gibt?
				// Das sollte aber definitiv nur für PT-Items gelten!
				// ...

				try
				{
					// Wenn sich die Spielmodus/Strecke/Wetter Kombination geändert hat, werden bisherige Statuseinträge ausgeblendet und gelöscht. Aber nur, wenn sie schon angezeigt wurden.
					if(lastApplicationGhost != null && v.containsKey("__seen") && (lastApplicationGhost.getGameMode() != (int) v.get("GameMode") || !lastApplicationGhost.getTrack().equalsIgnoreCase((String) v.get("Track")) || lastApplicationGhost.getWeather() != (int) v.get("Weather"))
					)
					{
						r[i++] = (int) v.get("TrackID");
						continue;
					}

					// -----------------------------------------
					// Nicht verwendte Elemente:
					// Time=1582141897, GhostID=787, GroupID=1, Begin=1581995570, End=1582153200, TrackID=2660
					// -----------------------------------------

					String suffix = " (%s)";
					if((boolean) v.get("PT"))
					{
						suffix = String.format(suffix, FNX.formatLangString(gmHelper.getLangBundle(), "pt_group", (String) v.get("GroupName")));
					}
					else if((boolean) v.get("SUC"))
					{
						suffix = String.format(suffix, gmHelper.getWeatherName(gmHelper.WEATHER_SUC));
					}
					else if((boolean) v.get("Ticket"))
					{
						suffix = String.format(suffix, gmHelper.getWeatherName(gmHelper.WEATHER_TICKET));
					}
					else if((boolean) v.get("Race"))
					{
						suffix = String.format(suffix, gmHelper.getWeatherName(gmHelper.WEATHER_RACE));
					}
					else
					{
						suffix = "";
					}

					data.append(FNX.formatLangString(lang, "fastFollowModeState",
						(int) v.get("GhostID"),
						gmHelper.getGameModeName((int) v.get("GameMode")),
						gmHelper.getTrack((String) v.get("Track")),
						gmHelper.getWeatherName((int) v.get("Weather")),
						gmHelper.getResult((int) v.get("OldResult")),
						(int) v.get("OldPosition"),
						gmHelper.getResult((int) v.get("NewResult")),
						(int) v.get("NewPosition"),
						suffix
					));

					v.put("__seen", true);
				}
				catch(gmException e)
				{
					exceptionHandler(e);
				}
			}

			if(r.length > 0)
			{
				for(i = 0; i < r.length; i++)
				{
					if(r[i] > 0)
					{
						FNX.dbgf("Removing data for track %d from lastApplicationDestinations...", r[i]);
						lastApplicationDestinations.remove(r[i]);
					}
				}
			}

			return data.toString();
		}

		return "";
	}

	public static void fastFollowStop()
	{
		HTGT.fastFollowStop(true);
	}

	public static void fastFollowStop(boolean suspend)
	{
		if(!FNX.requireEDT())
		{
			return;
		}
		else if(ffStarted == 0)
		{
			FNX.dbg("FFM not started! Unable to unblock...");

			return;
		}
		else if(!suspend)
		{
			ffStarted = -1;
		}
		else if(!HTGT.ENABLE_WATCHSERVICE)
		{
			try
			{
				// Noch einen allerletzten Durchlauf starten.
				// Damit uns wirklich nichts entgangen ist...
				fastFollowLock();
				fastFollowMode();
				fastFollowUnlock();
			}
			catch(eSportsAPIException e)
			{
				APIError(e);
			}
			catch(Exception e)
			{
				exceptionHandler(e);
			}
		}

		if(oFFM != null || aFFM != null)
		{
			FNX.dbg("stopping FFM now!");

			if(oFFM != null)
			{
				oFFM.cancel(true);
				oFFM = null;
			}

			if(aFFM != null)
			{
				aFFM.cancel(true);
				aFFM = null;
			}
		}

		if(ffDialog != null)
		ffDialog.setVisible(false);
	}

	private static Profiles getProfileHandle(String nick)
	{
		File profilesFile = new File(String.format("%2$s%1$s%3$s", File.separator, file.getParent().toString(), "Profiles.xml"));

		if(profilesFile == null || !profilesFile.exists() || !profilesFile.isFile())
		{
			FNX.dbgf("Other XML file not found: %s", profilesFile);
			errorMessage(null, FNX.formatLangString(lang, "fileNotFound", "Profiles.xml"));
		}
		else
		{
			FNX.dbgf("Other XML file: %s", profilesFile);

			try
			{
				Profiles profileHandle = new Profiles(profilesFile);

				if(nick != null && !profileHandle.profileExists(nick))
				{
					throw new Exception("Could not find nickname in profile");
				}
				else
				{
					return profileHandle;
				}
			}
			catch(Exception e)
			{
				exceptionHandler(e);
			}
		}

		return null;
	}

	public static void createProfile()
	{
		if(OfflineProfiles == null)
		{
			return;
		}

		Profiles profiles = getProfileHandle(null);

		if(profiles == null || unsavedChanges())
		{
			return;
		}

		boolean error = false;
		String message = null;
		String nick = null;

		while(true)
		{
			message = String.format(
				FNX.formatLangString(lang, "createProfileBody", nickname) +
				"%n" + FNX.formatLangString(lang, "profileWarning") +
				"%n%n" + FNX.formatLangString(lang, "profileModification") + "%n" +
				(!ENABLE_AUTOSAVE ? FNX.getLangString(lang, "profileNoAutosave") : "") +
				(ENABLE_AUTOSAVE ? FNX.getLangString(lang, "profileAutosave") : "") +
				(error ? "%n%n" + FNX.formatLangString(lang, "invalidProfileName", NICKNAME_MINLEN, NICKNAME_MAXLEN) : "") +
				"%n%n" + FNX.formatLangString(lang, "createProfileInput")
			);

			if((nick = (String) inputDialog(FNX.getLangString(lang, "createProfileTitle"), message, nick)) != null)
			{
				if(nick.length() > 0)
				{
					if(!nick.matches(NICKNAME_REGEX) || nick.matches(NICKNAME_REGEX_NOT))
					{
						error = true;
					}
					else if(nick.equalsIgnoreCase(SPECIAL_PROFILE) || nick.equalsIgnoreCase(DEFAULT_PROFILE))
					{
						errorMessage(FNX.formatLangString(lang, "unsupportedProfileName", SPECIAL_PROFILE, DEFAULT_PROFILE));
					}
					else if(OfflineProfiles.getProfileByNick(nick) > -1)
					{
						errorMessage(FNX.formatLangString(lang, "profileAlreadyExists"));
					}
					else
					{
						break;
					}
				}
			}
			else
			{
				return;
			}
		}

		try
		{
			// TODO: Reload profile? (falls die SC erst später geschlossen wurde)
			// ...

			resetHistory();

			OfflineProfiles.addProfile(nick);
			profiles.addProfile(nick);

			profiles.saveProfiles();
			saveFile(true);

			profile = 0;
			reloadFile();
		}
		catch(Exception e)
		{
			exceptionHandler(e);
			reloadFile();
			return;
		}

		//infoDialog("Das Profil wurde hinzugefügt.");
	}

	public static void renameProfile()
	{
		if(OfflineProfiles == null || checkProfile() || unsavedChanges())
		{
			return;
		}

		Profiles profiles = getProfileHandle(nickname);

		if(profiles == null)
		{
			return;
		}

		boolean error = false;
		String message = null;
		String nick = nickname;

		while(true)
		{
			message = String.format(
				FNX.formatLangString(lang, "renameProfileQuestion", nickname) +
				"%n" + FNX.formatLangString(lang, "profileWarning") +
				"%n%n" + FNX.formatLangString(lang, "profileModification") + "%n" +
				(!ENABLE_AUTOSAVE ? FNX.getLangString(lang, "profileNoAutosave") : "") +
				(ENABLE_AUTOSAVE ? FNX.getLangString(lang, "profileAutosave") : "") +
				(error ? "%n%n" + FNX.formatLangString(lang, "invalidProfileName", NICKNAME_MINLEN, NICKNAME_MAXLEN) : "") +
				"%n%n" + FNX.formatLangString(lang, "renameProfileInput")
			);

			if((nick = (String) inputDialog(FNX.getLangString(lang, "renameProfileTitle"), message, nick)) != null)
			{
				if(nick.length() > 0)
				{
					if(!nick.matches(NICKNAME_REGEX) || nick.matches(NICKNAME_REGEX_NOT))
					{
						error = true;
					}
					else if(nick.equalsIgnoreCase(SPECIAL_PROFILE) || nick.equalsIgnoreCase(DEFAULT_PROFILE))
					{
						errorMessage(FNX.formatLangString(lang, "unsupportedProfileName", SPECIAL_PROFILE, DEFAULT_PROFILE));
					}
					else if(nick.equalsIgnoreCase(nickname))
					{
						continue;
					}
					else if(OfflineProfiles.getProfileByNick(nick) > -1)
					{
						errorMessage(FNX.formatLangString(lang, "profileAlreadyExists"));
					}
					else
					{
						break;
					}
				}
			}
			else
			{
				return;
			}
		}

		try
		{
			// TODO: Reload profile? (falls die SC erst später geschlossen wurde)
			// ...

			resetHistory();

			profiles.renameProfile(nickname, nick);
			OfflineProfiles.renameProfile(nick);

			profiles.saveProfiles();
			saveFile(true);

			profile = 0;
			reloadFile();
		}
		catch(Exception e)
		{
			exceptionHandler(e);
			reloadFile();
			return;
		}

		//infoDialog("Das Profil wurde umbenannt.");
	}

	public static void deleteProfile()
	{
		if(OfflineProfiles == null || checkProfile())
		{
			return;
		}

		try
		{
			Profiles profiles = getProfileHandle(nickname);

			if(profiles == null)
			{
				return;
			}

			if(getRegularProfileCount() < 2)
			{
				infoDialog(null, FNX.formatLangString(lang, "lastRegularProfile"));
				return;
			}

			if(unsavedChanges())
			{
				return;
			}

			String message = String.format(
				FNX.formatLangString(lang, "deleteProfileQuestion", nickname) +
				"%n%n" + FNX.getLangString(lang, "profileModification") + "%n" +
				(!ENABLE_AUTOSAVE ? FNX.getLangString(lang, "profileNoAutosave") : "") +
				(ENABLE_AUTOSAVE ? FNX.getLangString(lang, "profileAutosave") : "") +
				"%n%n" + FNX.getLangString(lang, "profileWarning")
			);

			if(!confirmDialog(JOptionPane.WARNING_MESSAGE, FNX.getLangString(lang, "deleteProfileTitle"), message))
			{
				return;
			}

			// TODO: Reload profile? (falls die SC erst später geschlossen wurde)
			// ...

			resetHistory();

			profiles.deleteProfile(nickname);
			OfflineProfiles.deleteProfile(profile);

			profiles.saveProfiles();
			saveFile(true);

			profile = 0;
			reloadFile();
		}
		catch(Exception e)
		{
			exceptionHandler(e);
			reloadFile();
			return;
		}

		// infoDialog("Das Profil wurde gelöscht.");
	}

	private static boolean foreignGhostEnabled()
	{
		File userConfigFile = new File(String.format("%2$s%1$s%3$s", File.separator, file.getParent().toString(), "UserConfig.xml"));

		if(userConfigFile == null || !userConfigFile.exists() || !userConfigFile.isFile())
		{
			FNX.dbgf("User config XML file not found: %s", userConfigFile);
		}
		else
		{
			FNX.dbgf("User config XML file: %s", userConfigFile);

			try
			{
				UserConfig config = new UserConfig(userConfigFile);

				if(config.getMultiGhost())
				{
					FNX.dbg("MultiGhost setting is enabled.");
					return true;
				}
				else if(config.getTrainingGhostNick() != null)
				{
					FNX.dbg("TrainingGhostNick selection is not empty.");
					return true;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		FNX.dbg("MultiGhost setting is disabled.");
		FNX.dbg("TrainingGhostNick selection is empty.");

		return false;
	}

	public static void resort()
	{
		if(OfflineProfiles != null)
		{
			if(isSpecialProfile())
			{
				ArrayList<GhostElement>[][][] ghosts = OfflineProfiles.getGhostList();

				for(int i = (OfflineProfiles.getGhostCount() - 1); i > -1; i--)
				{
					deleteGhost(i);
				}

				for(int m = 0; m < ghosts.length; m++)
				{
					for(int t = 0; t < ghosts[m].length; t++)
					{
						for(int w = 0; w < ghosts[m][t].length; w++)
						{
							if(ghosts[m][t][w] != null)
							{
								ghosts[m][t][w].sort(Comparator.comparing(GhostElement::getTime));

								for(int i = 0; i < ghosts[m][t][w].size(); i++)
								{
									addGhost(ghosts[m][t][w].get(i), true);
								}
							}
						}
					}
				}
			}
			else
			{
				GhostElement[][][] ghosts;

				if((ghosts = OfflineProfiles.getAllGhosts(true)) == null)
				{
					if(confirmGhostReplacement())
					{
						ghosts = OfflineProfiles.getAllGhosts(false);
					}
					else
					{
						return;
					}
				}

				for(int i = (OfflineProfiles.getGhostCount() - 1); i > -1; i--)
				{
					deleteGhost(i);
				}

				for(int m = 0; m < ghosts.length; m++)
				{
					for(int t = 0; t < ghosts[m].length; t++)
					{
						for(int w = 0; w < ghosts[m][t].length; w++)
						{
							if(ghosts[m][t][w] != null)
							{
								addGhost(ghosts[m][t][w], true);
							}
						}
					}
				}
			}

			autoSave();
		}
	}

	public static void updateCheckDLL()
	{
		updateCheckDLL(true, false);
	}

	protected static void updateCheckDLL(boolean force, boolean auto)
	{
		long lastDLLCheck;
		int newDLLAvailable;

		if(dll == null || !dll.exists() || !dll.isFile())
		{
			FNX.dbg("DLL not initialized or not found.");

			if(!auto)
			{
				errorMessage(null, FNX.formatLangString(lang, "patchUpdatesDLL404"));
			}

			return;
		}

		Date date = new Date();
		lastDLLCheck = cfg.getLong(CFG_DC, 0L);
		FNX.dbgf("Current time: %d", date.getTime());
		FNX.dbgf("Last DLL check: %d", lastDLLCheck);
		FNX.dbgf("Check interval: %d", UPDATE_INTERVAL);

		if(lastDLLCheck <= 0L || date.getTime() > (lastDLLCheck + UPDATE_INTERVAL))
		{
			cfg.putLong(CFG_DC, date.getTime());
			force = true;
		}

		if(force)
		{
			if(anonAPI == null)
			{
				// Der Token wird absichtlich nicht mitgesendet!
				anonAPI = new eSportsAPI(null, getIdent());
			}

			try
			{
				// TODO: Check for NULL?
				String hash = FNX.sha512(dll);
				FNX.dbgf("SHA512: %s", hash);

				if(anonAPI.updateAvailable("SC.DLL", hash, auto))
				{
					FNX.dbg("New DLL available!" + ((auto) ? " (autocheck)" : ""));

					if(Desktop.isDesktopSupported())
					{
						if(confirmDialog(JOptionPane.INFORMATION_MESSAGE, null, FNX.formatLangString(lang, "patchUpdatesAvailableExtended")))
						{
							Desktop.getDesktop().browse(new URI(getRedirectURL("update-dll")));
						}
					}
					else
					{
						infoDialog(FNX.formatLangString(lang, "patchUpdatesAvailable"));
					}
				}
				else
				{
					FNX.dbg("No new DLL available..." + ((auto) ? " (autocheck)" : ""));

					if(!auto)
					{
						infoDialog(FNX.formatLangString(lang, "patchUpdatesNone"));
					}
				}
			}
			catch(eSportsAPIException e)
			{
				if(!auto)
				{
					APIError(e);
				}
				else
				{
					e.printStackTrace();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	private static Object inputDialog(String title, Object message, Object initialSelectionValue)
	{
		return inputDialog(title, message, null, initialSelectionValue);
	}

	private static Object inputDialog(String title, Object message, Object[] selectionValues, Object initialSelectionValue)
	{
		if(selectionValues != null)
		{
			FNX.dbgf("Input dialog: SELECTION %s", title);
		}
		else
		{
			FNX.dbgf("Input dialog: INPUTFIELD %s", title);
		}

		Object input = JOptionPane.showInputDialog(mainWindow, message, title, JOptionPane.PLAIN_MESSAGE, null, selectionValues, initialSelectionValue);

		if(input == null)
		{
			FNX.dbg("Input dialog: CANCEL");
			return null;
		}

		String selected = input.toString();

		if(selectionValues != null)
		{
			for(int i = 0; i < selectionValues.length; i++)
			{
				if(selected.equals(selectionValues[i]))
				{
					FNX.dbgf("Input dialog: SELECTED #%d", i);
					return i;
				}
			}

			FNX.dbg("Input dialog: SELECTION UNKNOWN");
			return null;
		}
		else
		{
			selected = selected.trim();
			FNX.dbgf("Input dialog: VALUE(%d) %s", selected.length(), selected);
			return selected;
		}
	}

	private static File openDialog(String directory)
	{
		return fileDialog(true, directory, null);
	}

	private static File saveDialog(String directory, File selection)
	{
		return fileDialog(false, directory, selection);
	}

	private static File fileDialog(boolean open, String directory, File selection)
	{
		JFileChooser chooser;
		int code;

		if(open)
		{
			FNX.dbgf("File dialog: OPEN %s", directory);
			chooser = new JFileChooser(directory);
		}
		else
		{
			FNX.dbgf("File dialog: SAVE %s", directory);
			chooser = new ImprovedFileChooser(directory);
		}

		if(selection != null)
		{
			FNX.dbgf("File dialog: SET %s", selection.getAbsolutePath());
			chooser.setSelectedFile(selection);
		}

		// FNX.dbg("File dialog: FILTER *.xml");
		FileFilter filter = new FileNameExtensionFilter(FNX.getLangString(lang, "xmlFiles"), "xml");
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);

		if(open)
		{
			code = chooser.showOpenDialog(null);
		}
		else
		{
			code = chooser.showSaveDialog(null);
		}

		if(code == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = chooser.getSelectedFile();
			FNX.dbgf("File dialog: APPROVE %s", selectedFile);

			if(selectedFile != null && (!open || selectedFile.exists()))
			{
				return selectedFile;
			}
			else
			{
				FNX.dbg("File dialog: FILE NOT FOUND");
			}
		}
		else if(code == JFileChooser.CANCEL_OPTION)
		{
			FNX.dbg("File dialog: CANCEL");
		}
		else if(code == JFileChooser.ERROR_OPTION)
		{
			FNX.dbg("File dialog: ERROR");
		}
		else
		{
			FNX.dbg("File dialog: UNKNOWN");
		}

		return null;
	}

	private static boolean confirmDialog(String msg)
	{
		return confirmDialog(null, msg);
	}

	private static boolean confirmDialog(String title, String msg)
	{
		return confirmDialog(JOptionPane.QUESTION_MESSAGE, title, msg);
	}

	private static boolean confirmDialog(int type, String title, String msg)
	{
		FNX.windowToFront(mainWindow);

		FNX.dbgf("New yes/no confirm dialog: %s", title);

		if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mainWindow, msg, title, JOptionPane.YES_NO_OPTION, type))
		{
			FNX.dbg("return TRUE (confirmed)");
			return true;
		}
		else
		{
			FNX.dbg("return FALSE (not confirmed)");
			return false;
		}

		/*
		Integer[] values = new Integer[]{BUTTON_YES, BUTTON_NO};
		String[] buttons = FNX.getLangStrings(lang, new String[]{"yes", "no"});
		Object defaultButton = buttons[0];

		int result = JOptionPane.showOptionDialog(mainWindow, msg, title, JOptionPane.YES_NO_OPTION, type, null, buttons, defaultButton);

		if(result != JOptionPane.CLOSED_OPTION && values[result] == BUTTON_YES)
		{
			FNX.dbg("return TRUE (confirmed)");
			return true;
		}
		else
		{
			FNX.dbg("return FALSE (not confirmed)");
			return false;
		}
		*/
	}

	private static int threesomeDialog(String msg, boolean appendix)
	{
		return threesomeDialog(null, msg, appendix);
	}

	private static int threesomeDialog(String title, String msg, boolean appendix)
	{
		return threesomeDialog(JOptionPane.QUESTION_MESSAGE, title, msg, appendix);
	}

	private static int threesomeDialog(int type, String title, String msg, boolean appendix)
	{
		FNX.windowToFront(mainWindow);

		String[] buttons;
		Integer[] values;
		Object defaultButton;

		if(appendix)
		{
			FNX.dbgf("New threesome (yes/always/no) dialog: %s", title);
			values = new Integer[]{BUTTON_YES, BUTTON_ALWAYS, BUTTON_NO};
			buttons = FNX.getLangStrings(lang, new String[]{"yes", "always", "no"});
			defaultButton = buttons[0];
		}
		else
		{
			FNX.dbgf("New threesome (yes/never/no) dialog: %s", title);
			values = new Integer[]{BUTTON_YES, BUTTON_NEVER, BUTTON_NO};
			buttons = FNX.getLangStrings(lang, new String[]{"yes", "never", "no"});
			defaultButton = buttons[0];
		}

		int result = JOptionPane.showOptionDialog(mainWindow, msg, title, JOptionPane.YES_NO_CANCEL_OPTION, type, null, buttons, defaultButton);

		if(result == JOptionPane.CLOSED_OPTION)
		{
			FNX.dbg("return BUTTON_CLOSED");
			return BUTTON_CLOSED;
		}
		else
		{
			FNX.dbgf("return [%d] (%s)", values[result], buttons[result]);
			return values[result].intValue();
		}
	}

	private static Object stepDialog(String title, Object message)
	{
		return stepDialog(JOptionPane.PLAIN_MESSAGE, title, message, null, null, false);
	}

	private static Object stepDialog(String title, Object message, boolean prev)
	{
		return stepDialog(JOptionPane.PLAIN_MESSAGE, title, message, null, null, prev);
	}

	private static Object stepDialog(int type, String title, Object message)
	{
		return stepDialog(type, title, message, null, null, false);
	}

	private static Object stepDialog(int type, String title, Object message, boolean prev)
	{
		return stepDialog(type, title, message, null, null, prev);
	}

	private static Object stepDialog(String title, Object message, Object[] selectionValues, Object initialSelectionValue)
	{
		return stepDialog(JOptionPane.PLAIN_MESSAGE, title, message, selectionValues, initialSelectionValue, false);
	}

	private static Object stepDialog(int type, String title, Object message, Object[] selectionValues, Object initialSelectionValue)
	{
		return stepDialog(type, title, message, selectionValues, initialSelectionValue, false);
	}

	private static Object stepDialog(String title, Object message, Object[] selectionValues, Object initialSelectionValue, boolean prev)
	{
		return stepDialog(JOptionPane.PLAIN_MESSAGE, title, message, selectionValues, initialSelectionValue, prev);
	}

	private static Object stepDialog(int type, String title, Object message, Object[] selectionValues, Object initialSelectionValue, boolean prev)
	{
		FNX.windowToFront(mainWindow);

		String[] buttons;
		Integer[] values;
		Object defaultButton;
		String dialogType;

		JComboBox  comboBox = null;
		Integer[] returnValues = null;
		JTextField textField = null;

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1));
		panel.add(new JLabel((String) message));

		if(selectionValues != null)
		{
			DefaultComboBoxModel model = new DefaultComboBoxModel();
			returnValues = new Integer[selectionValues.length];

			for(int i = 0, h = 0; i < selectionValues.length; i++)
			{
				if(selectionValues[i] != null)
				{
					model.addElement(selectionValues[i]);
					returnValues[h++] = i;
				}
			}

			if(initialSelectionValue != null)
			{
				model.setSelectedItem(initialSelectionValue);
			}

			dialogType = "SELECTION";
			comboBox = new JComboBox(model);
			panel.add(comboBox);
		}
		else
		{
			dialogType = "INPUTFIELD";
			textField = new JTextField(initialSelectionValue != null ? initialSelectionValue.toString() : null);
			panel.add(textField);
		}

		String langPrev = String.format("« %s", FNX.getLangString(lang, "prev"));
		String langCancel = String.format("%s", FNX.getLangString(lang, "cancel"));
		String langNext = String.format("%s »", FNX.getLangString(lang, "next"));

		if(prev)
		{
			FNX.dbgf("New prev/next step %s dialog: %s", dialogType, title);
			values = new Integer[]{BUTTON_PREV, BUTTON_CANCEL, BUTTON_NEXT};
			buttons = new String[]{langPrev, langCancel, langNext};
			defaultButton = buttons[2];
		}
		else
		{
			FNX.dbgf("New next step %s dialog: %s", dialogType, title);
			values = new Integer[]{BUTTON_CANCEL, BUTTON_NEXT};
			buttons = new String[]{langCancel, langNext};
			defaultButton = buttons[1];
		}

		int result = JOptionPane.showOptionDialog(mainWindow, panel, title, JOptionPane.YES_NO_CANCEL_OPTION, type, null, buttons, defaultButton);

		if(result == JOptionPane.CLOSED_OPTION)
		{
			FNX.dbgf("RETURN: %d (CLOSED)", BUTTON_CANCEL);
			return BUTTON_CANCEL;
		}
		else if(buttons[result] == defaultButton)
		{
			if(selectionValues != null)
			{
				int selectedIndex = comboBox.getSelectedIndex();
				String selectedItem = comboBox.getSelectedItem().toString();

				if(selectedIndex < 0 || selectedIndex >= returnValues.length || returnValues[selectedIndex] == null)
				{
					throw new IndexOutOfBoundsException(String.format("selectedIndex = %d; selectedItem = %s", selectedIndex, selectedItem));
				}

				FNX.dbgf("RETURN SELECTION: %d (%s)", returnValues[selectedIndex], selectedItem);

				return (int) returnValues[selectedIndex];
			}
			else
			{
				FNX.dbgf("RETURN INPUT: %s", textField.getText());
				return textField.getText();
			}
		}

		FNX.dbgf("RETURN: %d (%s)", values[result], buttons[result]);
		return values[result];
	}

	private static void errorMessage(String msg)
	{
		errorMessage(FNX.getLangString(lang, "errorTitle"), msg);
	}

	private static void errorMessage(String title, String msg)
	{
		messageDialog(JOptionPane.ERROR_MESSAGE, title, msg);
	}

	private static void infoDialog(String msg)
	{
		infoDialog(null, msg);
	}

	private static void infoDialog(String title, String msg)
	{
		messageDialog(JOptionPane.INFORMATION_MESSAGE, title, msg);
	}

	private static void messageDialog(String msg)
	{
		messageDialog(null, msg);
	}

	private static void messageDialog(String title, String msg)
	{
		messageDialog(JOptionPane.PLAIN_MESSAGE, title, msg);
	}

	private static void messageDialog(int type, String title, String msg)
	{
		if(!FNX.requireEDT())
		{
			FNX.dbgf("Hidden message: %s", msg);

			return;
		}

		FNX.windowToFront(mainWindow); // <-- APIError, errorMessage, ...
		JOptionPane.showMessageDialog(mainWindow, msg, title, type);
	}

	private static boolean noSelection()
	{
		FNX.dbg("No selection available!");
		return false;
	}

	public static void selectLanguage()
	{
		selectLanguage(false, false);
	}

	public static void selectLanguage(boolean init, boolean updates)
	{
		Locale locale;
		String[] localeParts;

		Locale defaultLocale = null;
		String defaultLocaleString;

		if((defaultLocaleString = cfg(CFG_LOCALE)) != null)
		{
			localeParts = splitLocaleString(defaultLocaleString);

			if(localeParts != null)
			{
				defaultLocale = new Locale.Builder().setLanguage(localeParts[0]).setRegion(localeParts[1]).build();
			}
		}

		if(defaultLocale == null)
		{
			defaultLocale = Locale.getDefault();
		}

		String[] values = new String[LOCALES.length];
		String selection = null;

		Locale oldLocale = Locale.getDefault();
		FNX.dbgf("Changing locale: %s", defaultLocaleAtStartUp);
		Locale.setDefault(defaultLocaleAtStartUp);

		for(int i = 0; i < LOCALES.length; i++)
		{
			localeParts = splitLocaleString(LOCALES[i]);
			locale = new Locale.Builder().setLanguage(localeParts[0]).setRegion(localeParts[1]).build();

			values[i] = String.format("%2$s (%1$s)", locale.getLanguage().toUpperCase(), locale.getDisplayLanguage());

			if(locale.getLanguage().equals(defaultLocale.getLanguage()))
			{
				selection = values[i];
			}
		}

		FNX.dbgf("Resetting locale: %s", oldLocale);
		Locale.setDefault(oldLocale);

		Integer selected = (Integer) inputDialog(FNX.getLangString(lang, "languageSelectionTitle"), String.format((updates ? FNX.getLangString(lang, "languageSelectionExtended") + "%n%n" : "") + FNX.getLangString(lang, "languageSelectionBody")), values, selection);

		if(selected != null && !LOCALES[selected].equals(defaultLocale.toString()))
		{
			cfg(CFG_LOCALE, LOCALES[selected]);

			if(!init)
			{
				// Es wäre extrem aufwändig die ganze GUI neu aufzubauen und alle externen Klassen neu zu laden. Ein Neustart ist einfacher!
				messageDialog(FNX.getLangString(lang, "languageSelectionTitle"), FNX.formatLangString(lang, "languageSelectionRestart"));
			}
			else
			{
				setupLocale();
			}
		}
	}

	private static void updateDefaultLocale()
	{
		String locale = cfg(CFG_LOCALE);

		if(locale != null && Arrays.asList(LOCALES).contains(locale))
		{
			String[] localeParts = splitLocaleString(locale);

			if(localeParts != null)
			{
				Locale current = Locale.getDefault();

				if(!Locale.getDefault().getLanguage().equals(localeParts[0]))
				{
					FNX.dbgf("Old default locale: %s", Locale.getDefault());
					Locale.setDefault(new Locale(localeParts[0], FNX.isValidLocale(localeParts[0] + "_" + current.getCountry()) ? current.getCountry() : localeParts[1]));
					FNX.dbgf("New default locale: %s", Locale.getDefault());
				}
				else
				{
					FNX.dbgf("Default locale %s is (nearly) identical to %s_%s! Not changing locale...", Locale.getDefault(), localeParts[0], localeParts[1]);
				}
			}
		}
		else
		{
			FNX.dbgf("Unknown locale string: %s", locale);
			FNX.dbgf("Default locale: %s", Locale.getDefault());
		}
	}

	private static void setupLocale()
	{
		if(defaultLocaleAtStartUp == null)
		{
			defaultLocaleAtStartUp = Locale.getDefault();
		}

		updateDefaultLocale();
		lang = FNX.getLangBundle("HTGT");
	}

	private static String[] splitLocaleString(String locale)
	{
		String[] localeParts = locale.split("_");

		if(localeParts.length == 2)
		{
			return localeParts;
		}

		FNX.dbgf("Invalid locale string: %s", locale);

		return null;
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

		if(copy)
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data.toString()), null);
		}

		if(delete)
		{
			updateWindowTitle();
			autoSave();
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
					// messageDialog(null, "");
					break;
				}
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e, null);
		}

		autoSave();
	}

/***********************************************************************
 *                             API ACTIONS                             *
 ***********************************************************************/

	public static void APIError(eSportsAPIException e)
	{
		APIError(e, null);
	}

	// Fehlermeldung der API formatiert ausgeben.
	private static void APIError(eSportsAPIException e, String msg)
	{
		e.printStackTrace();

		if(e.getErrorCode().equals("TOKEN_INVALID"))
		{
			FNX.dbg("API token invalid: Removed from prefs!");
			updateToken(null);
		}

		if(!FNX.requireEDT())
		{
			return;
		}

		msg = (msg == null) ? FNX.formatLangString(lang, "APIError") : msg;
		msg = FNX.formatLangString(lang, "APIErrorDetails", msg, e.getErrorCode(), e.getErrorMessage()).trim();

		if(e.getCalming())
		{
			infoDialog(APPLICATION_API, msg);
		}
		else
		{
			errorMessage(APPLICATION_API, msg);
		}
	}

	// Erzwungene Updateprüfung.
	public static void updateCheck()
	{
		updateCheck(true, false);
	}

	// Updateprüfung über die API durchführen.
	protected static void updateCheck(boolean force, boolean auto)
	{
		long lastUpdateCheck;
		int updatesAvailable;

		if(APPLICATION_VERSION.toUpperCase().startsWith("GIT-"))
		{
			FNX.dbgf("Update check disabled: %s", APPLICATION_VERSION);

			if(!auto)
			{
				infoDialog(FNX.formatLangString(lang, "updatesDevBuild"));
			}

			return;
		}

		Date date = new Date();
		lastUpdateCheck = cfg.getLong(CFG_UC, 0L);
		FNX.dbgf("Current time: %d", date.getTime());
		FNX.dbgf("Last update check: %d", lastUpdateCheck);
		FNX.dbgf("Check interval: %d", UPDATE_INTERVAL);

		if(lastUpdateCheck <= 0L || date.getTime() > (lastUpdateCheck + UPDATE_INTERVAL))
		{
			cfg.putLong(CFG_UC, date.getTime());
			force = true;
		}

		if(force)
		{
			if(anonAPI == null)
			{
				// Der Token wird absichtlich nicht mitgesendet!
				anonAPI = new eSportsAPI(null, getIdent());
			}

			try
			{
				if(anonAPI.updateAvailable(APPLICATION_NAME, APPLICATION_VERSION, auto))
				{
					FNX.dbg("New update available!" + ((auto) ? " (autocheck)" : ""));

					if(Desktop.isDesktopSupported())
					{
						if(confirmDialog(JOptionPane.INFORMATION_MESSAGE, null, FNX.formatLangString(lang, "updatesAvailableExtended")))
						{
							Desktop.getDesktop().browse(new URI(getRedirectURL("update")));
						}
					}
					else
					{
						infoDialog(FNX.formatLangString(lang, "updatesAvailable"));
					}
				}
				else
				{
					FNX.dbg("No updates available..." + ((auto) ? " (autocheck)" : ""));

					if(!auto)
					{
						infoDialog(FNX.formatLangString(lang, "updatesNone"));
					}
				}
			}
			catch(eSportsAPIException e)
			{
				if(!auto)
				{
					APIError(e);
				}
				else
				{
					e.printStackTrace();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void copyTokenToProfile()
	{
		if(OfflineProfiles == null || checkProfile() || !prepareAPI())
		{
			return;
		}

		try
		{
			if(confirmDialog(JOptionPane.WARNING_MESSAGE, null, FNX.formatLangString(lang, "copyTokenToProfileQuestion")))
			{
				FNX.dbg("Copying token to active profile...");
				OfflineProfiles.setToken(token);

				if(!token.equals(OfflineProfiles.getToken()))
				{
					throw new Exception("Could not copy token");
				}
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}

		updateWindowTitle();
		updateMenuItems();
		autoSave();
	}

	public static void copyTokenFromProfile()
	{
		if(OfflineProfiles == null || checkProfile())
		{
			return;
		}

		try
		{
			String newToken = OfflineProfiles.getToken();

			if(newToken == null)
			{
				FNX.dbg("No token in active profile!");
			}
			else
			{
				if(confirmDialog(JOptionPane.WARNING_MESSAGE, null, FNX.formatLangString(lang, "copyTokenFromProfileQuestion")))
				{
					FNX.dbg("Copying token from active profile...");
					updateToken(newToken);
				}
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	public static void removeTokenFromProfile()
	{
		if(OfflineProfiles == null || checkProfile())
		{
			return;
		}

		try
		{
			if(OfflineProfiles.getToken() == null)
			{
				FNX.dbg("No token in active profile!");
			}
			else if(confirmDialog(JOptionPane.WARNING_MESSAGE, null, FNX.formatLangString(lang, "deleTokenFromProfileQuestion")))
			{
				FNX.dbg("Removing token from active profile...");
				OfflineProfiles.deleteToken();

				if(OfflineProfiles.getToken() != null)
				{
					throw new Exception("Could not remove token");
				}
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}

		updateWindowTitle();
		updateMenuItems();
		autoSave();
	}

	// Token aktualisieren und Cache leeren.
	private static void updateToken(String t)
	{
		removeConfig(CFG_WC);
		cfg(CFG_TOKEN, t);
		updateMenuItems();
	}

	// API-Token aus der Konfiguration löschen.
	public static void deleteToken()
	{
		if(confirmDialog(APPLICATION_API, FNX.formatLangString(lang, "deleteToken")))
		{
			updateToken(null);
		}
	}

	// API-Token erstmalig eintragen oder ändern.
	// Wird fix in der Konfiguration gespeichert!
	public static void setupToken()
	{
		String oldToken = cfg(CFG_TOKEN);
		String newToken = null;

		while(true)
		{
			if((newToken = (String) inputDialog(APPLICATION_API, FNX.formatLangString(lang, "tokenInput"), oldToken)) != null)
			{
				newToken = newToken.toLowerCase();
				if(!newToken.matches("^[a-f0-9]+$"))
				{
					FNX.dbg("Invalid API token! Asking once again...");
					errorMessage(FNX.formatLangString(lang, "invalidToken"));
					continue;
				}
				else if(oldToken == null || !oldToken.equals(newToken))
				{
					updateToken(newToken);
				}
				else
				{
					FNX.dbg("API token not changed.");
				}
			}

			break;
		}
	}

	// Token vom User abfragen, falls noch nicht vorhanden.
	// Zusätzlich wird nur hier ein eSportsAPI-Objekt erzeugt.
	// Die einzige Ausnahme ist der Updatecheck über die API.
	private static boolean prepareAPI()
	{
		String oldToken = token;
		for(int i = 0; i < 3; i++)
		{
			if((token = cfg(CFG_TOKEN)) != null)
			{
				if(oldToken == null || !oldToken.equals(token))
				{
					FNX.dbg("Token changed! Resetting API instance...");
					api = new eSportsAPI(token, getIdent());
				}

				return true;
			}
			else
			{
				FNX.dbgf("Asking for API token... (try #%d)", i + 1);
				setupToken();
			}
		}

		FNX.dbg("Three times is enough! No API token available.");

		api = null;
		token = null;
		return false;
	}

	// Markierte Geister über die API hochladen.
	// Danach Bestätigung zur Übernahme anzeigen.
	public static boolean ghostUpload()
	{
		GhostElement[] ghosts;

		if(OfflineProfiles == null)
		{
			return false;
		}

		int[] selection = maintable.getSelectedRows();
		if(selection.length == 0) return noSelection();

		ghosts = new GhostElement[selection.length];
		for(int i = 0; i < selection.length; i++)
		{
			ghosts[i] = OfflineProfiles.getGhost(selection[i]);
		}

		return ghostUpload(ghosts, false, false);
	}

	// Interne Funktion für den sofortigen Upload eines Geists.
	private static boolean ghostUpload(GhostElement ghost)
	{
		return ghostUpload(new GhostElement[]{ghost}, false, false);
	}

	// ...
	private static boolean ghostUpload(GhostElement ghost, boolean silent)
	{
		return ghostUpload(new GhostElement[]{ghost}, silent, false);
	}

	// ...
	private static boolean ghostUpload(GhostElement[] ghosts)
	{
		return ghostUpload(ghosts, false, false);
	}

	// Kompatibilitätswrapper
	private static boolean ghostUpload(GhostElement[] ghosts, boolean silent, boolean doNotApply)
	{
		if(ghostUploadExtended(ghosts, silent, doNotApply) > 0)
		{
			return true;
		}

		return false;
	}

	// Interne Funktion für den sofortigen Upload von Geistern. Erweiterte Version für FFM.
	private static int ghostUploadExtended(GhostElement[] ghosts, boolean silent, boolean doNotApply)
	{
		lastApplicationPosition = 0;
		lastApplicationStatus = false;
		List<Map<String,Object>> result;
		boolean error = false;

		if(!prepareAPI())
		{
			return 0;
		}
		else if(!FNX.isEDT())
		{
			silent = true;
		}

		try
		{
			result = api.getExtendedGhostIDs(ghosts);
			uploadedCount += result.size();

			if(result.size() != ghosts.length)
			{
				FNX.dbgf("ghosts(%d) != selection(%d)", result.size(), ghosts.length);
				exceptionHandler(new eSportsAPIException("SERVER_DUMB"));

				return -1;
			}
		}
		catch(eSportsAPIException e)
		{
			APIError(e);
			return -1;
		}

		for(int i = 0; i < result.size(); i++)
		{
			GhostElement ghost = ghosts[i];
			Map<String,Object> info = result.get(i);

			int ghostID = Integer.parseInt(info.get("GhostID").toString());
			boolean applicable = (Integer.parseInt(info.get("Applicable").toString()) > 0);

			lastFilterOption = Integer.parseInt(info.get("FilterOption").toString());

			FNX.dbgf("Item #%d uploaded as ghost ID %d: %s", i, ghostID, ghost.getDebugDetails());

			if(!applicable)
			{
				FNX.dbgf("Ghost with ID %d not applicable, skipping...", ghostID);

				if(!silent)
				{
					infoDialog(APPLICATION_API, FNX.formatLangString(lang, "ghostNotApplicable", ghostID));
				}
			}
			else if(!doNotApply)
			{
				int action;

				if(cfg(CFG_AAR) != null)
				{
					FNX.dbg("Forcing result registration because of previous choice...");
					action = BUTTON_YES;
				}
				else if(!FNX.requireEDT())
				{
					return -1;
				}
				else
				{
					int w = ghost.getWeather();
					String weatherName = "?";

					switch(lastFilterOption)
					{
						case eSportsAPI.FO_TICKET:
							w = gmHelper.WEATHER_TICKET;
							break;

						case eSportsAPI.FO_SUC:
							w = gmHelper.WEATHER_SUC;
							break;
					}

					try
					{
						weatherName = gmHelper.getWeatherName(w);
					}
					catch(gmException e)
					{
						/* ... */
					}

					action = threesomeDialog(APPLICATION_API, FNX.formatLangString(lang, "ghostApplyQuestion", ghost.getNickname(), ghost.getGameModeName(), ghost.getTrackName(), weatherName, ghost.getResult()), true);
				}

				if(action == BUTTON_ALWAYS)
				{
					cfg(CFG_AAR, "true");
					action = BUTTON_YES;
				}

				if(action == BUTTON_YES)
				{
					try
					{
						boolean status = false;
						int position = -1;

						if(silent && FNX.isEDT() && ffStarted == 0)
						{
							status = api.applyResultByGhostID(ghostID);
						}
						else
						{
							position = api.applyResultByGhostIDExtended(ghostID);
							status = (position >= 0);
						}

						if(status)
						{
							FNX.dbgf("Successfully applied result from ghost with ID %d. (expected rank %d)", ghostID, position);

							Map[] lastResultDestinations = api.getLastResultDestinations();
							if(lastResultDestinations != null && lastResultDestinations.length > 0)
							{
								if(ffStarted == 0)
								{
									FNX.dbg("Initializing lastApplicationDestinations.");
									lastApplicationDestinations = new HashMap();
								}

								for(int h = 0; h < lastResultDestinations.length; h++)
								{
									if(!(boolean) lastResultDestinations[h].get("Applied"))
									{
										FNX.dbgf("Ignoring not applied ghost from track %d...", (int) lastResultDestinations[h].get("TrackID"));
										continue;
									}

									FNX.dbgf("lastApplicationDestinations.put(%d, %s)", (int) lastResultDestinations[h].get("TrackID"), lastResultDestinations[h].toString());
									lastApplicationDestinations.put((int) lastResultDestinations[h].get("TrackID"), lastResultDestinations[h]);
								}
							}

							lastApplicationGhost = ghost;
							appliedCount++;

							if(!silent)
							{
								infoDialog(APPLICATION_API, FNX.formatLangString(lang, "ghostApplySuccess", ghostID) + getResultDestinations());
							}

							lastApplicationPosition = position;
							lastApplicationStatus = true;
						}
						else
						{
							throw new eSportsAPIException();
						}
					}
					catch(eSportsAPIException e)
					{
						if(silent && e.getMessage().equals("GHOST_DOPING") && cfg(CFG_AAR) != null)
						{
							FNX.dbgf("Silently discarding GHOST_DOPING exception at ghost with ID %d.", ghostID);
						}
						else
						{
							FNX.dbgf("Failed to apply ghost with ID %d.", ghostID);
							APIError(e, FNX.formatLangString(lang, "ghostApplyFailed", ghostID));
							error = true;
						}
					}
				}
			}
		}

		if(!error)
		{
			return 1;
		}
		else
		{
			return 0;
		}

		// TODO: Return -1 for Non-EDT questions!
		// ...
	}

	// Eingabefeld für Geist-IDs zum Herunterladen. Mehrere IDs können
	// durch beliebige nicht-numerische Trennzeichen angegeben werden.
	public static void ghostDownload()
	{
		int id;
		String[] parts;
		String input = null;
		ArrayList<Integer> ids;

		if(OfflineProfiles != null && prepareAPI())
		{
			while(true)
			{
				if((input = (String) inputDialog(APPLICATION_API, FNX.formatLangString(lang, "ghostDownloadByID"), input)) != null)
				{
					ids = new ArrayList<Integer>(0);
					parts = input.split("[^0-9]+");

					for(int i = 0; i < parts.length; i++)
					{
						if((id = FNX.intval(parts[i].trim())) > 0)
						{
							ids.add(id);
						}
					}

					if(!ghostDownload(ids.stream().mapToInt(i -> i).toArray()))
					{
						continue;
					}
					else
					{
						//infoDialog("Der Download von mindestens einem Geist war erfolgreich.");

						autoSave();
					}
				}

				break;
			}
		}
	}

	// Download einer einzelnen Geist-ID über die API.
	private static boolean ghostDownload(int id)
	{
		return ghostDownload(id, false);
	}

	// ...
	private static boolean ghostDownload(int id, boolean force)
	{
		return ghostDownload(new int[]{id}, force);
	}

	// Download mehrerer Geist-IDs über die API.
	private static boolean ghostDownload(int[] ids)
	{
		return ghostDownload(ids, false);
	}

	// ...
	private static boolean ghostDownload(int[] ids, boolean force)
	{
		try
		{
			Integer[] id;
			GhostElement[] ghostdata;

			if(prepareAPI())
			{
				if(ids.length == 0)
				{
					FNX.dbg("ids.length = 0");
					return false;
				}
				else if(ids.length == 1)
				{
					FNX.dbg("ids.length = 1");
					ghostdata = new GhostElement[1];
					ghostdata[0] = api.getGhostByID(ids[0]);
				}
				else
				{
					FNX.dbg("ids.length > 1");
					ghostdata = api.getGhostsByIDs(ids);
				}

				int imported = ghostImport(ghostdata, force);

				if(imported > 0)
				{
					return true;
				}
				else if(imported == -1)
				{
					FNX.dbg("imported = -1");
					return false;
				}
				else
				{
					throw new eSportsAPIException();
				}
			}
		}
		catch(eSportsAPIException e)
		{
			APIError(e);
		}

		return false;
	}

	// Auswahl eines Spielmodus für den Geistdownload. Das passiert
	// offline, erst die Rangliste wird über die API vom Server geladen.
	public static void ghostSelect()
	{
		if(OfflineProfiles == null || !prepareAPI())
		{
			return;
		}

		Integer input;
		Boolean result;
		String selection;

		int[] modes = gmHelper.getGameModeIDs();
		String[] values = new String[modes.length];
		String lastMode = cfg(CFG_MODE);

		while(true)
		{
			selection = null;
			for(int i = 0; i < modes.length; i++)
			{
				try
				{
					values[i] = gmHelper.getGameModeName(modes[i]);
				}
				catch(gmException e)
				{
					e.printStackTrace();
					values[i] = "";
				}

				// TODO: Eigentlich nicht ganz korrekt, da es als "int" verglichen werden müsste. So ist es aber einheitlich und einfacher.
				if(lastMode != null && lastMode.equals(Integer.toString(modes[i])))
				{
					selection = values[i];
				}
			}

			input = (Integer) stepDialog(APPLICATION_API, FNX.getLangString(lang, "ghostDownloadModes"), values, selection);

			if(input >= 0)
			{
				lastMode = cfg(CFG_MODE, input.toString());
				result = ghostSelect(input.intValue());

				if(result != null && !result)
				{
					continue;
				}
			}

			break;
		}
	}

	// Auswahl einer Strecke/Wetter für den Geistdownload.
	// Hierbei braucht es manchmal eine Onlineanbindung.
	private static Boolean ghostSelect(int mode)
	{
		try
		{
			if(OfflineProfiles == null || !prepareAPI())
			{
				return false;
			}

			// Der API-Aufruf muss seit v0.1.1 auf jeden Fall erfolgen!
			// Wir brauchen nämlich die Streckenreihenfolge, siehe #65.
			if(/*ENABLE_RACE &&*/ !updateRaceWeather())
			{
				return false;
			}

			String trackOrder = cfg(CFG_TRACKS);
			if(trackOrder != null && trackOrder.length() > 0)
			{
				gmHelper.setTrackOrder(trackOrder.split("[^a-z]+"));
			}

			Integer input;
			Boolean result;
			String selection;

			String[]   tracks      = gmHelper.getTracksByGameMode(mode, true, true);
			int[]      weathers    = gmHelper.getWeatherIDs(ENABLE_RACE, ENABLE_XTC, ENABLE_SUC);
			int        raceWeather = gmHelper.WEATHER_NONE;
			int        tickets     = ENABLE_XTC ? 1 : 0;
			int        suc         = ENABLE_SUC ? 1 : 0;
			int        addition    = 0;

			String[]   values;
			String[][] conditions;

			if(mode == gmHelper.GAMEMODE_MM_EXTREMEICE)
			{
				if(ENABLE_RACE)
				{
					values     = new String[tracks.length * (2 + tickets + suc)];
					conditions = new String[tracks.length * (2 + tickets + suc)][3];
					addition++;
				}
				else
				{
					values     = new String[tracks.length];
					conditions = new String[tracks.length][3];
				}
			}
			else
			{
				values     = new String[tracks.length * weathers.length];
				conditions = new String[tracks.length * weathers.length][3];
			}

			String lastTrack   = cfg(CFG_TRACK);
			String lastWeather = cfg(CFG_WEATHER);

			while(true)
			{
				selection = null;
				for(int i = 0; i < tracks.length; i++)
				{
					for(int h = 0; h < weathers.length; h++)
					{
						int key = (i * weathers.length) + h;

						if(mode == gmHelper.GAMEMODE_MM_EXTREMEICE)
						{
							key = i * (addition + 1 + tickets + suc);

							if(weathers[h] == gmHelper.WEATHER_RACE)
							{
								key += OFFSET_RACE + 1;
							}
							else if(weathers[h] == gmHelper.WEATHER_TICKET)
							{
								key += OFFSET_TICKET + 1;
							}
							else if(weathers[h] == gmHelper.WEATHER_SUC)
							{
								key += OFFSET_SUC + 1;
							}
							else if(weathers[h] != gmHelper.WEATHER_ICE)
							{
								continue;
							}
						}

						if(weathers[h] == gmHelper.WEATHER_RACE || weathers[h] < -1)
						{
							String suffix = "";
							switch(weathers[h])
							{
								case gmHelper.WEATHER_TICKET:
									suffix = "-T";
									break;

								case gmHelper.WEATHER_SUC:
									suffix = "-S";
									break;
							}

							raceWeather = cfg.getInt(String.format(CFG_RACE, gmHelper.getGameMode(mode, true), tracks[i].toUpperCase() + suffix), gmHelper.WEATHER_NONE);

							if(raceWeather == gmHelper.WEATHER_NONE)
							{
								values[key] = null;
								continue;
							}
						}

						try
						{
							if(mode == gmHelper.GAMEMODE_DEFAULT)
							{
								if(weathers[h] == gmHelper.WEATHER_RACE || weathers[h] < -1)
								{
									values[key] = String.format("%s – %s (%s)", gmHelper.getTrack(tracks[i]), gmHelper.getWeatherName(weathers[h]), gmHelper.getWeatherName(raceWeather));
								}
								else
								{
									values[key] = String.format("%s (%s)", gmHelper.getTrack(tracks[i]), gmHelper.getWeatherName(weathers[h]));
								}
							}
							else if(mode == gmHelper.GAMEMODE_MM_EXTREMEICE)
							{
								if(weathers[h] == gmHelper.WEATHER_RACE || weathers[h] < -1)
								{
									values[key] = String.format("%s: %s – %s", gmHelper.getGameModeName(mode), gmHelper.getTrack(tracks[i]), gmHelper.getWeatherName(weathers[h]));
								}
								else
								{
									values[key] = String.format("%s: %s", gmHelper.getGameModeName(mode), gmHelper.getTrack(tracks[i]));
								}
							}
							else
							{
								if(weathers[h] == gmHelper.WEATHER_RACE || weathers[h] < -1)
								{
									values[key] = String.format("%s: %s – %s (%s)", gmHelper.getGameModeName(mode), gmHelper.getTrack(tracks[i]), gmHelper.getWeatherName(weathers[h]), gmHelper.getWeatherName(raceWeather));
								}
								else
								{
									values[key] = String.format("%s: %s (%s)", gmHelper.getGameModeName(mode), gmHelper.getTrack(tracks[i]), gmHelper.getWeatherName(weathers[h]));
								}
							}
						}
						catch(gmException e)
						{
							e.printStackTrace();
							values[key] = "";
						}

						conditions[key][0] = values[key];
						conditions[key][1] = tracks[i];
						conditions[key][2] = Integer.toString(weathers[h]);

						// TODO: Eigentlich nicht ganz korrekt, da das Wetter als "int" verglichen werden müsste. So ist es aber einheitlich und einfacher.
						if(lastTrack != null && lastTrack.equalsIgnoreCase(tracks[i]) && lastWeather != null && lastWeather.equals(Integer.toString(weathers[h])))
						{
							selection = values[key];
						}
					}
				}

				input = (Integer) stepDialog(APPLICATION_API, FNX.getLangString(lang, "ghostDownloadTracks"), values, selection, true);

				if(input >= 0)
				{
					lastTrack = cfg(CFG_TRACK, conditions[input][1]);
					lastWeather = cfg(CFG_WEATHER, conditions[input][2]);

					result = ghostSelect(mode, lastTrack, Integer.parseInt(lastWeather), false, ENABLE_RACE);

					if(result == null)
					{
						return null;
					}
					else if(result)
					{
						return true;
					}
					else
					{
						continue;
					}
				}
				else if(input == BUTTON_CANCEL)
				{
					return null;
				}
				else if(input == BUTTON_PREV)
				{
					return false;
				}

				break;
			}
		}
		catch(gmException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}

		return false;
	}

	// Auswahl eines Geists aus der Rangliste zum Herunterladen.
	// Vorher muss bereits nach Strecke/Wetter gefragt worden sein!
	private static Boolean ghostSelect(int mode, String track, int weather)
	{
		return ghostSelect(mode, track, weather, false);
	}

	private static Boolean ghostSelect(int mode, String track, int weather, boolean force)
	{
		return ghostSelect(mode, track, weather, force, false);
	}

	// Ermöglicht alle Rückfragen zu umgehen, die beim Download auftreten.
	private static Boolean ghostSelect(int mode, String track, int weather, boolean force, boolean forceWeather)
	{
		try
		{
			List<Map<String,Object>> results;
			Integer selection;

			if(OfflineProfiles != null && prepareAPI())
			{
				results = api.getResultsByCondition(mode, track, weather, forceWeather);

				if(results.size() > 0)
				{
					FNX.dbgf("Got %d results.", results.size());
					Integer[] ghosts = new Integer[results.size()];
					String[] values = new String[results.size()];
					String preSelection = null;

					for(int i = 0; i < results.size(); i++)
					{
						Map<String,Object> result = results.get(i);
						ghosts[i] = Integer.parseInt(result.get("GhostID").toString());
						values[i] = String.format("%0" + Integer.toString(FNX.strlen(results.size())) + "d. %s – %s", result.get("Position"), gmHelper.getResult(Integer.parseInt(result.get("Result").toString())), result.get("Nickname"));

						if(i == api.getNextResultIndex())
						{
							preSelection = values[i];
						}
					}

					selection = (Integer) stepDialog(APPLICATION_API, FNX.getLangString(lang, "ghostDownloadGhosts"), values, preSelection, true);

					if(selection >= 0)
					{
						ghostDownload(ghosts[selection], true);
						autoSave();

						return true;
					}
					else if(selection == BUTTON_CANCEL)
					{
						return null;
					}
					else if(selection == BUTTON_PREV)
					{
						return false;
					}
				}
				else
				{
					// Das sollte nie passieren, da es RESULT_EMPTY gibt!
					FNX.dbg("Something went really wrong! We got an empty result list...");
					throw new eSportsAPIException("SERVER_DUMB");
				}
			}
		}
		catch(eSportsAPIException e)
		{
			APIError(e);
		}

		return false;
	}

	// Details des Tokens anzeigen.
	public static void playerInfo()
	{
		try
		{
			if(prepareAPI())
			{
				Map<String,Object> data = api.getPlayerInfo();
				data.forEach((k,v) -> FNX.dbgf("playerDetails.%s: %s", k, v));

				messageDialog(APPLICATION_API, FNX.formatLangString(lang, "playerInfo", data.get("Useraccount"), data.get("Nickname"), data.get("CompetitionName")));
			}
		}
		catch(eSportsAPIException e)
		{
			APIError(e);
		}
	}

	public static boolean updateRaceWeather()
	{
		Date date = new Date();
		long lastWeatherCheck = cfg.getLong(CFG_WC, 0L);

		// Damit die Ranglisten vom Rennen sofort auswählbar sind, dürfen sie
		// maximal bis zur nächsten vollen Stunde zwischengespeichert werden...
		long checkInterval = Math.min(3600000L, WEATHER_INTERVAL);
		checkInterval = date.getTime() / checkInterval * checkInterval;

		FNX.dbgf("Current time: %d", date.getTime());
		FNX.dbgf("Last weather check: %d", lastWeatherCheck);
		FNX.dbgf("Check interval: %d", checkInterval);

		String trackOrder = cfg(CFG_TRACKS);
		if(trackOrder == null || trackOrder.length() == 0)
		{
			// Upgrade: v0.1.0 » v0.1.1
			FNX.dbg("Forcing cache flush...");
			lastWeatherCheck = 0L;
		}

		if(lastWeatherCheck < checkInterval)
		{
			if(prepareAPI())
			{
				try
				{
					int[] modes = gmHelper.getGameModeIDs();
					String[] tracks = gmHelper.getTracks(true);
					int[][][] test = api.getRaceWeather();

					for(int i = 0; i < test.length; i++)
					{
						for(int m = 0; m < modes.length; m++)
						{
							for(int t = 0; t < tracks.length; t++)
							{
								String s = "";
								switch(i)
								{
									case OFFSET_TICKET:
										s = "-T";
										break;

									case OFFSET_SUC:
										s = "-S";
										break;
								}

								FNX.dbgf("Race weather: %s @ %s (%d) = %d", gmHelper.getGameModeName(modes[m]), gmHelper.getTrack(tracks[t]), i, test[i][m][t]);
								cfg.putInt(String.format(CFG_RACE, gmHelper.getGameMode(modes[m], true), tracks[t].toUpperCase() + s), test[i][m][t]);
							}
						}
					}

					// Streckenreihenfolge für später abspeichern...
					cfg(CFG_TRACKS, String.join(",", api.getServerTracks()));

					cfg.putLong(CFG_WC, date.getTime());
					return true;
				}
				catch(eSportsAPIException e)
				{
					APIError(e);
				}
				catch(gmException e)
				{
					e.printStackTrace();
				}
			}

			return false;
		}

		return true;
	}

	public static boolean isSpecialProfile()
	{
		return isSpecialProfile(null);
	}

	public static boolean isSpecialProfile(int i)
	{
		try
		{
			if(OfflineProfiles != null && OfflineProfiles.getProfileCount() >= i)
			{
				return isSpecialProfile(OfflineProfiles.getProfiles()[i]);
			}
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}

		return false;
	}

	public static boolean isSpecialProfile(String nick)
	{
		if(nick == null)
		{
			if(OfflineProfiles != null && nickname != null)
			{
				nick = nickname;
			}
			else
			{
				return false;
			}
		}

		return nick.equals(SPECIAL_PROFILE);
	}

	public static int getRegularProfileCount() throws ProfileException
	{
		int defaultProfile = OfflineProfiles.defaultProfile();
		String[] allProfiles = OfflineProfiles.getProfiles();
		int regularProfiles = 0;

		for(int i = 0; i < allProfiles.length; i++)
		{
			if(i != defaultProfile && !isSpecialProfile(allProfiles[i]))
			{
				regularProfiles++;
			}
		}

		return regularProfiles;
	}

/***********************************************************************
 *                            FILE ACTIONS                             *
 ***********************************************************************/

	public static File getFile()
	{
		return file;
	}

	// Öffnet eine neue Datei, beachtet aber ungespeicherte Änderungen
	// in einer eventuell bereits geöffneten Datei. Ohne die explizite
	// Zustimmung, gehen keine Daten verloren. Die Funktion hat leider
	// einen Schönheitsfehler: Die bisherige Datei wird bereits vor dem
	// Dialog geschlossen. Wird danach keine Datei ausgewählt, ist
	// nachher gar keine mehr geladen. Das sollte korrigiert werden.
	public static void openFile()
	{
		if(closeFile() && OfflineProfiles == null)
		{
			if((file = openDialog(cfg(CFG_CWD))) != null)
			{
				cfg(CFG_CWD, file.getParent().toString());
				openInternalFile();
			}
		}
	}

	// Datei öffnen, interne Version ohne Rückfragen.
	private static void openInternalFile()
	{
		try
		{
			resetHistory();
			OfflineProfiles = new OfflineProfiles(file);
			updateHistory(true);

			selectLastProfile();
			updateWindowTitle();
			enableMenuItems();

			FNX.dbg("Successfully loaded XML file! Let's rumble...");
		}
		catch(FileNotFoundException e)
		{
			errorMessage(FNX.formatLangString(lang, "fileNotFound", e.getMessage()));
		}
		catch(Exception e)
		{
			reset();
			exceptionHandler(e);
		}

		checkDLL();
	}

	private static void checkDLL()
	{
		if(dll == null)
		{
			dll = new File(String.format("%2$s%1$s%3$s%1$s%4$s", File.separator, file.getParent().toString(), "Managed", "Assembly-CSharp.dll"));
		}

		if(dll.exists() && dll.isFile())
		{
			FNX.dbgf("DLL file exists: %s", dll.getAbsolutePath().toString());
			new Thread(new HTGT_Background(HTGT_Background.EXEC_DLLCHECK)).start();
		}
		else
		{
			FNX.dbgf("DLL file not found: %s", dll.getAbsolutePath().toString());
		}
	}

	// Standardpfad je nach OS öffnen.
	public static void openDefaultFile()
	{
		File defaultFile;

		if(closeFile() && OfflineProfiles == null)
		{
			if((defaultFile = getDefaultFile()) != null)
			{
				file = defaultFile;
				openInternalFile();
			}
		}
	}

	// Standardpfad aus Konfiguration auslesen.
	// Oder den Pfad automatisch ermitteln.
	public static File getDefaultFile()
	{
		String defaultPath;
		String osName;

		if((defaultPath = cfg(CFG_DEFAULT)) == null || defaultPath.length() == 0)
		{
			// TODO: Check for null pointer?
			osName = System.getProperty("os.name").toLowerCase();

			if(osName.indexOf("windows") != -1)
			{
				defaultPath = "C:\\Games\\Ski Challenge 16\\Game_Data\\OfflineProfiles.xml";
			}
			else if(osName.indexOf("linux") != -1)
			{
				defaultPath = System.getProperty("user.home") + "/.wine/drive_c/Games/Ski Challenge 16/Game_Data/OfflineProfiles.xml";
			}
			else if(osName.indexOf("mac") != -1)
			{
				defaultPath = "/Applications/SkiChallenge16.app/Contents/MacOS/SkiChallenge16.app/Contents/MacOS/Game_Data/OfflineProfiles.xml";
			}
			else
			{
				exceptionHandler(new Exception(String.format("Unsupported value for os.name: %s", osName)));
				return null;
			}
		}

		return new File(defaultPath);
	}

	// Standardpfad zurücksetzen.
	public static void resetDefaultFile()
	{
		if(confirmDialog(FNX.formatLangString(lang, "resetDefaultPath")))
		{
			cfg(CFG_DEFAULT, null);
		}
	}

	// Standardpfad ändern.
	public static void changeDefaultFile()
	{
		String defaultFile = getDefaultFile().getAbsolutePath();

		while(true)
		{
			if((defaultFile = (String) inputDialog(null, FNX.formatLangString(lang, "defaultPathSetup"), defaultFile)) == null)
			{
				break;
			}

			if(defaultFile.length() > 0)
			{
				File defaultFileHandler = new File(defaultFile);

				if(defaultFileHandler == null || !defaultFileHandler.exists() || !defaultFileHandler.isFile())
				{
					errorMessage(FNX.formatLangString(lang, "pathNotFound"));
				}
				else
				{
					cfg(CFG_DEFAULT, defaultFile);
					dll = null; checkDLL();
					return;
				}
			}
		}
	}

	// Aktuelle Datei als Standardpfad übernehmen
	public static void applyDefaultFile()
	{
		if(OfflineProfiles != null)
		{
			cfg(CFG_DEFAULT, file.getAbsolutePath());
		}
	}

	// Liest die Datei neu ein, beachtet aber ungespeicherte Änderungen.
	// Ohne die explizite Bestätigung des Users, geht nichts verloren.
	public static void reloadFile()
	{
		try
		{
			reloadFile(false);
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	// Liest die Datei neu ein, beachtet keine ungespeicherte Änderungen!
	// Diese Funktion ist für interne Zwecke über Fast-Follow gedacht.
	private static void reloadFile(boolean force) throws Exception
	{
		if(!force && (OfflineProfiles == null || unsavedChanges()))
		{
			return;
		}

		FNX.dbg("Reloading file...");
		OfflineProfiles.reload();
		selectProfile(profile);
		syncGUI();
	}

	// Änderungen automatisch speichern.
	private static void autoSave()
	{
		if(ENABLE_AUTOSAVE)
		{
			FNX.dbg("AUTOSAVE TRIGGERED!");
			saveFile(false);
		}
	}

	// Speichert Änderungen, wenn es welche gibt.
	public static void saveFile()
	{
		if(ENABLE_AUTOSAVE)
		{
			if(unsavedChanges())
			{
				FNX.dbg("THIS IS A BUG! THERE ARE UNSAVED CHANGES BUT AUTOSAVE IS ENABLED.");
				exceptionHandler(new Exception("AUTOSAVE & UNSAVED TRIGGERED"));
			}
			else
			{
				infoDialog(FNX.formatLangString(lang, "autoSaveEnabled"));
			}
		}

		if(!saveFile(false))
		{
			FNX.dbg("Failed to save file! (safe internal state)");
			errorMessage(FNX.formatLangString(lang, "fileSaveFailed"));
		}
	}

	// Speichert die Änderungen in der aktuellen Datei.
	private static boolean saveFile(boolean force)
	{
		if(OfflineProfiles == null)
		{
			return false;
		}

		if(force || OfflineProfiles.changed())
		{
			FNX.dbg("Something to save...");

			if(!saveFile(OfflineProfiles.toString()))
			{
				return false;
			}

			updateHistory(true);
			OfflineProfiles.saved();
			updateWindowTitle();
		}
		else
		{
			FNX.dbg("Nothing to save...");
		}

		return true;
	}

	private static boolean saveFile(String xml)
	{
		try
		{
			PrintWriter tmp = new PrintWriter(file);
			tmp.printf("%s", xml);
			tmp.close();

			return true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	// "Speichern unter" Dialog.
	public static void saveFileAs()
	{
		File selectedFile;

		if(OfflineProfiles == null)
		{
			return;
		}

		if((selectedFile = saveDialog(file.getParent().toString(), file)) != null)
		{
			cfg(CFG_CWD, selectedFile.getParent().toString());

			try
			{
				file = selectedFile;
				if(!saveFile(true))
				{
					throw new Exception();
				}

				OfflineProfiles.updateFile(file);
				FNX.dbg("File saved to new location.");
			}
			catch(Exception e)
			{
				exceptionHandler(e);
			}
		}
	}

	// Gibt es noch ungespeicherte Änderungen?
	// Fragt den User, was gemacht werden soll.
	public static boolean unsavedChanges()
	{
		if(OfflineProfiles != null && OfflineProfiles.changed())
		{
			if(!confirmDialog(JOptionPane.WARNING_MESSAGE, null, FNX.formatLangString(lang, "unsavedChanges")))
			{
				return true;
			}
		}

		return false;
	}

	// Schließt die aktuelle Datei, beachtet aber ungespeicherte Änderungen.
	// Ohne explizite Bestätigung, gehen keine ungesicherten Daten verloren.
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

		reset();
		resetHistory();
		disableMenuItems();

		return true;
	}

	// Programm beenden, aber ungespeicherte Änderungen beachten.
	// Ohne explizite Bestätigung, gehen keine Daten verloren.
	public static void quit()
	{
		if(closeFile())
		{
			FNX.dbg("Good bye!");
			System.exit(0);
		}
		else
		{
			FNX.dbg("File not closed.");
		}
	}

	// Auswahl einer Datei, in die markierte Geister exportiert werden sollen.
	// Es handelt sich dabei um eine korrekte XML-Datei, mit eigenen Knoten.
	public static boolean exportFile()
	{
		StringBuilder data;
		File selectedFile;

		if(OfflineProfiles != null)
		{
			int[] selection = maintable.getSelectedRows();
			if(selection.length == 0) return noSelection();

			Date date = new Date(); DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
			selectedFile = new File(String.format("%s-Export_%s.xml", APPLICATION_NAME, dateFormat.format(date)));

			while(true)
			{
				if((selectedFile = saveDialog(cfg(CFG_CWDPORT), selectedFile)) != null)
				{
					cfg(CFG_CWDPORT, selectedFile.getParent().toString());

					if(!selectedFile.toString().matches("^.+\\.\\w*?$$")) //
					{
						selectedFile = new File(selectedFile.toString() + ".xml");

						if(selectedFile.exists() && !ImprovedFileChooser.overwriteFile(selectedFile))
						{
							continue;
						}
					}

					break;
				}
				else
				{
					return false;
				}
			}

			try
			{
				data = new StringBuilder();
				for(int i = selection.length - 1; i > -1; i--)
				{
					GhostElement ghost = OfflineProfiles.getGhost(selection[i]);
					FNX.dbgf("Exporting line %d: %s", selection[i], ghost.getDebugDetails());
					data.insert(0, String.format("\t<!-- %s @ %s (%s / %s): %s (%s) -->\r\n\t%s\r\n", ghost.getNickname(), ghost.getTrackName(), ghost.getGameModeName(), ghost.getWeatherName(), ghost.getResult(), gmHelper.formatSki(ghost.getSki(), true), ghost.toString()));
				}

				data.insert(0, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<GhostList>\r\n\r\n");
				data.append(String.format("</GhostList>\r\n<!-- %s -->\r\n", FNX.getDateString()));

				PrintWriter pw = new PrintWriter(selectedFile);
				pw.printf("%s", data.toString()); pw.close();

				FNX.dbg("Export to file successfully!");
				infoDialog(FNX.formatLangString(lang, "exportToFileSuccess", selectedFile));

				return true;
			}
			catch(Exception e)
			{
				exceptionHandler(e);
			}
		}

		return false;
	}

	// Auswahl einer Datei, aus der Geister importiert werden sollen.
	public static void importFile()
	{
		String parentPath;
		File selectedFile;
		int importCounter;

		if(OfflineProfiles != null && (selectedFile = openDialog(cfg(CFG_CWDPORT))) != null)
		{
			cfg(CFG_CWDPORT, selectedFile.getParent().toString());

			try
			{
				if((importCounter = ghostImport(selectedFile)) > 0)
				{
					FNX.dbgf("importCounter = %d (ok)", importCounter);
					infoDialog(FNX.formatLangString(lang, "importedGhostsCount", importCounter));
				}
				else if(importCounter == 0)
				{
					FNX.dbg("importCounter = 0 (none)");
					errorMessage(FNX.formatLangString(lang, "noGhostsInFile"));
				}
			}
			catch(Exception e)
			{
				exceptionHandler(e);
			}

			autoSave();
		}
	}

	private static void updateHistoryMenuItems()
	{
		if(historyIndex < history.length && history[historyIndex + 1] != null)
		{
			enableMenuItems(MENU_UNDO);
		}
		else
		{
			disableMenuItems(MENU_UNDO);
		}

		if(historyIndex > 0 && history[historyIndex - 1] != null)
		{
			enableMenuItems(MENU_REDO);
		}
		else
		{
			disableMenuItems(MENU_REDO);
		}
	}

	/*
	private static void cleanHistory()
	{
		resetHistory();
		updateHistory(true);
	}
	*/

	private static void resetHistory()
	{
		historyIndex = 0;
		history = new String[HISTORY_SIZE];
		FNX.dbg("History cleared! (index: 0)");

		disableMenuItems(MENU_UNDO);
		disableMenuItems(MENU_REDO);

		dumpHistory();
	}

	private static void updateHistory()
	{
		updateHistory(false);
	}

	private static void updateHistory(boolean force)
	{
		if(OfflineProfiles == null)
		{
			return;
		}
		else if(!force && !OfflineProfiles.changed())
		{
			FNX.dbg("Nothing changed, not updating history.");
			return;
		}

		String[] newHistory = new String[history.length];
		newHistory[0] = OfflineProfiles.toString();

		int n = 1; int i = 0;
		int c = history.length;

		if(historyIndex != 0)
		{
			FNX.dbgf("History index is %d, let's rewind...", historyIndex);

			i = historyIndex;
			historyIndex = 0;
		}

		//dumpHistory("pre-rewind");

		while(i < c && n < c)
		{
			newHistory[n++] = history[i++];
		}

		history = newHistory;
		FNX.dbg("History updated!");

		//dumpHistory("post-rewind");
		dumpHistory();

		updateHistoryMenuItems();
	}

	public static void undoHistory()
	{
		restoreHistory(historyIndex + 1);
	}

	public static void redoHistory()
	{
		restoreHistory(historyIndex - 1);
	}

	private static boolean restoreHistory(int newIndex)
	{
		if(OfflineProfiles != null && newIndex >= 0 && newIndex < history.length && history[newIndex] != null)
		{
			FNX.dbgf("Restoring from history index %d...", newIndex);

			try
			{
				saveFile(history[newIndex]);
				OfflineProfiles.reload();
				selectProfile(profile);
				syncGUI();

				historyIndex = newIndex;
				updateHistoryMenuItems();

				dumpHistory();
				return true;
			}
			catch(Exception e)
			{
				exceptionHandler(e);
			}
		}
		else
		{
			FNX.dbgf("Restoring from history index %d is impossible!", newIndex);
		}

		dumpHistory();

		return false;
	}

	private static void dumpHistory()
	{
		dumpHistory(null);
	}

	private static void dumpHistory(String title)
	{
		title = (title != null) ? String.format(" (%s)", title) : "";
		FNX.dbgf("----- START OF HISTORY DUMP%s -----", title);

		int length = FNX.strlen(HISTORY_SIZE);
		for(int i = 0; i < history.length; i++)
		{
			FNX.dbgf("%3$s[%1$0" + length + "d] = %2$s", i, (history[i] == null ? "NULL" : String.format("%d byte", history[i].length())), (i == historyIndex ? "!" : " "));
		}

		FNX.dbgf("----- END OF HISTORY DUMP%s -----", title);
	}

/***********************************************************************
 *                        CONFIGURATION HELPER                         *
 ***********************************************************************/

	// Konfiguration $key auslesen. Wenn sie noch nicht
	// existiert, wird der Standardwert $def zurückgegeben.
	private static String getConfig(String key, String def)
	{
		return cfg.get(key, def);
	}

	// Setze Konfiguration $key auf den Wert $value.
	private static void setConfig(String key, String value)
	{
		String oldValue;
		String newValue;

		if(key.equals(CFG_TOKEN))
		{
			oldValue = String.format("HIDDEN String(%d)", getConfig(key, "").length());
			newValue = String.format("HIDDEN String(%d)", value.length());
		}
		else
		{
			oldValue = getConfig(key, null);
			newValue = value;
		}

		if(getConfig(key, "").equals(value))
		{
			FNX.dbgf("Config for key \"%s\" unchanged: %s", key, value);
		}
		else
		{
			FNX.dbgf("Old config for key \"%s\": %s", key, oldValue);
			FNX.dbgf("New config for key \"%s\": %s", key, newValue);

			cfg.put(key, value);
		}
	}

	// Entferne Konfiguration $key.
	private static void removeConfig(String key)
	{
		String oldValue;

		if(key.equals(CFG_TOKEN))
		{
			oldValue = String.format("HIDDEN String(%d)", getConfig(key, "").length());
		}
		else
		{
			oldValue = getConfig(key, null);
		}

		FNX.dbgf("Removing config for key \"%s\", old value: %s", key, oldValue);

		cfg.remove(key);
	}

	// Liefert die Konfiguration $key.
	// Der Standardwert ist hierbei NULL.
	private static String cfg(String key)
	{
		return getConfig(key, null);
	}

	// Setzt die Konfiguration $key auf den Wert $value.
	// Wenn $value NULL ist, wird die Konfiguration gelöscht!
	// Gibt immer den neu gesetzten Wert der Konfiguration aus.
	private static String cfg(String key, String value)
	{
		if(value == null)
		{
			removeConfig(key);
		}
		else
		{
			setConfig(key, value);
		}

		return cfg(key);
	}

	// Alle Konfigurationswerte löschen.
	// Gibt den Status der Aktion zurück.
	private static boolean clearConfig()
	{
		try
		{
			FNX.dbg("Clearing config!");
			cfg.clear(); return true;
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}

		return false;
	}

	// Alle Konfigurationswerte löschen,
	// aber mit Rückfrage und Statusmeldung.
	public static void clearConfigDialog()
	{
		if(confirmDialog(null, FNX.formatLangString(lang, "resetConfigQuestion")))
		{
			clearConfig();
		}
	}

	// Alle Immer/Nie Fragen aktivieren.
	// Braucht definitiv keine Bestätigung.
	public static void enableQuestions()
	{
		removeConfig(CFG_NDG);
		removeConfig(CFG_ARG);
		removeConfig(CFG_AAR);
	}

	// Alle Immer/Nie Fragen deaktivieren.
	// Quasi ein sofortiger Silent-Modus.
	public static void disableQuestions()
	{
		cfg(CFG_NDG, "true");
		cfg(CFG_ARG, "true");
		cfg(CFG_AAR, "true");
	}

/***********************************************************************
 *                            MISCELLANEOUS                            *
 ***********************************************************************/

	public static void copyGhosts()
	{
		ghostsProfileAction(false);
	}

	public static void moveGhosts()
	{
		ghostsProfileAction(true);
	}

	private static void ghostsProfileAction(boolean move)
	{
		if(OfflineProfiles != null && maintable != null)
		{
			String title = FNX.getLangString(lang, (move ? "move" : "copy") + "ToProfile");
			String message = "";

			boolean warnSRC = false;
			boolean warnDST = false;

			try
			{
				int[] selection = maintable.getSelectedRows();

				if(selection.length == 0)
				{
					return;
				}

				int[] modes = gmHelper.getGameModeIDs();
				String[] tracks = gmHelper.getTracks(true);
				int[] weathers = gmHelper.getWeatherIDs();

				GhostElement[][][] result = new GhostElement[modes.length][tracks.length][weathers.length];
				GhostElement[] ghosts = new GhostElement[selection.length];

				for(int i = 0; i < selection.length; i++)
				{
					int m = -1;
					int t = -1;
					int w = -1;

					ghosts[i] = OfflineProfiles.getGhost(selection[i]);

					for(int h = 0; h < modes.length; h++)
					{
						if(modes[h] == ghosts[i].getGameMode())
						{
							m = h;
							break;
						}
					}

					for(int h = 0; h < tracks.length; h++)
					{
						if(tracks[h].equals(ghosts[i].getTrack()))
						{
							t = h;
							break;
						}
					}

					for(int h = 0; h < weathers.length; h++)
					{
						if(weathers[h] == ghosts[i].getWeather())
						{
							w = h;
							break;
						}
					}

					if(m < 0 || t < 0 || w < 0)
					{
						throw new Exception();
					}
					else if(result[m][t][w] != null)
					{
						warnSRC = true;
					}
					else
					{
						result[m][t][w] = ghosts[i];
					}
				}

				if(warnSRC)
				{
					message = FNX.getLangString(lang, "multipleGhostsWarning") + "%n%n";

					if(isSpecialProfile())
					{
						if(confirmDialog(JOptionPane.WARNING_MESSAGE, title, String.format(message + FNX.getLangString(lang, "exportGhostsToFileQuestion"))))
						{
							exportFile();
						}

						return;
					}
				}

				String[] profiles = OfflineProfiles.getProfiles();
				String[] values = new String[warnSRC ? 1 : (profiles.length - 1)];
				int[] profileIDs = new int[values.length];

				String defaultSelection = null;
				String suffix = null;

				for(int i = 0, h = 0; i < profiles.length; i++)
				{
					if(i == profile || (warnSRC && !isSpecialProfile(i)))
					{
						continue;
					}

					if(i == OfflineProfiles.defaultProfile())
					{
						suffix = FNX.getLangString(lang, "defaultProfile");
					}
					else if(isSpecialProfile(profiles[i]))
					{
						suffix = FNX.getLangString(lang, "specialProfile");
					}
					else
					{
						suffix = "";
					}

					if(suffix.length() > 0)
					{
						suffix = String.format(" (%s)", suffix);
					}

					values[h] = String.format("[%0" + Integer.toString(FNX.strlen(profiles.length)) + "d] %s%s", i + 1, profiles[i], suffix);
					profileIDs[h] = i;

					if(!isSpecialProfile() && isSpecialProfile(i))
					{
						defaultSelection = values[h];
					}

					h++;
				}

				if(warnSRC && values[0] == null)
				{
					errorMessage(title, FNX.formatLangString(lang, "noSpecialProfileAvailable"));
					return;
				}

				Integer selected = (Integer) inputDialog(title, message + FNX.formatLangString(lang, "selectDestinationProfile"), values, defaultSelection);

				if(selected == null)
				{
					return;
				}

				FNX.dbgf("Selected profile ID: %2$d (item #%1$d)", selected, profileIDs[selected]);
				selected = profileIDs[selected];

				FNX.dbgf("Switching to profile %d...", selected);
				OfflineProfiles.selectProfile(selected);

				if(!isSpecialProfile(selected))
				{
					GhostElement[][][] existing = OfflineProfiles.getAllGhosts();

					for(int m = 0; m < result.length; m++)
					{
						for(int t = 0; t < result[m].length; t++)
						{
							for(int w = 0; w < result[m][t].length; w++)
							{
								if(existing[m][t][w] != null && result[m][t][w] != null)
								{
									existing[m][t][w].printDetails();
									result[m][t][w].printDetails();

									warnDST = true;
									break;
								}
							}
						}
					}

					if(warnDST && !confirmGhostReplacement(title))
					{
						return;
					}
				}

				for(int i = 0; i < ghosts.length; i++)
				{
					if(warnDST)
					{
						for(int h = (OfflineProfiles.getGhostCount() - 1); h > -1; h--)
						{
							GhostElement ghost = OfflineProfiles.getGhost(h);

							if(ghost.getGameMode() == ghosts[i].getGameMode() && ghost.getTrack().equals(ghosts[i].getTrack()) && ghost.getWeather() == ghosts[i].getWeather())
							{
								OfflineProfiles.deleteGhost(h);
							}
						}
					}

					OfflineProfiles.addGhost(ghosts[i]);
				}

				FNX.dbgf("Using old profile %d...", profile);
				OfflineProfiles.selectProfile(profile);

				if(move)
				{
					for(int i = 0; i < selection.length; i++)
					{
						deleteGhost(selection[i]);
					}
				}
			}
			catch(Exception e)
			{
				exceptionHandler(e);
			}
			finally
			{
				try
				{
					// Das ursprüngliche Profil aktivieren!
					FNX.dbgf("Restoring profile %d...", profile);
					OfflineProfiles.selectProfile(profile);
				}
				catch(Exception e)
				{
					exceptionHandler(e);
					syncGUI();
				}
				finally
				{
					autoSave();
				}
			}
		}
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

		HTGT_ActionListener action;
		ActionMap actionMap = getActionMap();
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,         HTGT.NONE             ), "scrollUpChangeSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,       HTGT.NONE             ), "scrollDownChangeSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,              HTGT.NONE             ), "selectPreviousRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP,           HTGT.NONE             ), "selectPreviousRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,              HTGT.SHIFT            ), "selectPreviousRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP,           HTGT.SHIFT            ), "selectPreviousRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,            HTGT.NONE             ), "selectNextRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN,         HTGT.NONE             ), "selectNextRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,            HTGT.SHIFT            ), "selectNextRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN,         HTGT.SHIFT            ), "selectNextRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,            HTGT.NONE             ), "selectFirstRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,            HTGT.CTRL             ), "selectFirstRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,            HTGT.SHIFT            ), "selectFirstRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,            HTGT.SHIFT + HTGT.CTRL), "selectFirstRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,             HTGT.NONE             ), "selectLastRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,             HTGT.CTRL             ), "selectLastRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,             HTGT.SHIFT            ), "selectLastRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,             HTGT.SHIFT + HTGT.CTRL), "selectLastRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,          HTGT.NONE             ), "clearSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A,               HTGT.CTRL             ), "selectAll");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE ,         HTGT.NONE             ), "moveGhostsToProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY ,       HTGT.NONE             ), "copyGhostsToProfile");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT ,       HTGT.NONE             ), "selectPreviousGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT,         HTGT.NONE             ), "selectPreviousGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,           HTGT.NONE             ), "selectPreviousGameProfile");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,             HTGT.NONE             ), "selectNextGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT,        HTGT.NONE             ), "selectNextGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,            HTGT.NONE             ), "selectNextGameProfile");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA,           HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,          HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL,         HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEPARATOR,       HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_CIRCUMFLEX,      HTGT.NONE             ), "selectSpecialGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_CIRCUMFLEX, HTGT.NONE             ), "selectSpecialGameProfile");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMBER_SIGN,     HTGT.NONE             ), "selectDefaultGameProfile");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,           HTGT.NONE             ), "selectRegularGameProfile");

		action = new HTGT_ActionListener();
		action.setPrivateAction("moveGhosts");
		actionMap.put("moveGhostsToProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("copyGhosts");
		actionMap.put("copyGhostsToProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("selectPrevProfile");
		actionMap.put("selectPreviousGameProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("selectNextProfile");
		actionMap.put("selectNextGameProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("selectDefaultProfile");
		actionMap.put("selectDefaultGameProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateAction("selectRegularProfile");
		actionMap.put("selectRegularGameProfile", action);

		action = new HTGT_ActionListener();
		action.setPrivateArguments(new Object[]{-1});
		action.setPrivateAction("selectProfileByNumber");
		actionMap.put("selectSpecialGameProfile", action);

		for(int i = 0; i < 10; i++)
		{
			try
			{
				inputMap.put(KeyStroke.getKeyStroke(KeyEvent.class.getField("VK_NUMPAD" + i).getInt(null), HTGT.NONE), "selectGameProfileWithNumber" + i);
				inputMap.put(KeyStroke.getKeyStroke(KeyEvent.class.getField("VK_" + i).getInt(null), HTGT.NONE), "selectGameProfileWithNumber" + i);

				action = new HTGT_ActionListener();
				action.setPrivateArguments(new Object[]{i});
				action.setPrivateAction("selectProfileByNumber");
				actionMap.put("selectGameProfileWithNumber" + i, action);
			}
			catch(NoSuchFieldException|IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		//getColumnModel().addColumnModelListener(this);
		getModel().addTableModelListener(this);
	}

	public void test123()
	{
		FNX.dbg("hello world");
	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column)
	{
		if(column == 0)
		{
			return renderLeft;
		}
		else if(column == 5)
		{
			return renderRight;
		}
		else
		{
			return renderCenter;
		}
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}
}

class HTGT_WindowAdapter extends WindowAdapter
{
	@Override
	public void windowClosing(WindowEvent windowEvent)
	{
		HTGT.quit();
	}
}

class HTGT_Background implements Runnable
{
	public static final int EXEC_UPDATECHECK = 1;
	public static final int EXEC_DLLCHECK    = 3;

	private int exec;

	public HTGT_Background(int exec)
	{
		this.exec = exec;
	}

	@Override
	public void run()
	{
		switch(this.exec)
		{
			case EXEC_UPDATECHECK:
				HTGT.updateCheck(false, true);
				break;

			case EXEC_DLLCHECK:
				HTGT.updateCheckDLL(false, true);
				break;
		}
	}
}

class HTGT_SelectionHandler implements ListSelectionListener
{
	public void valueChanged(ListSelectionEvent e)
	{
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if(!e.getValueIsAdjusting())
		{
			if(lsm.isSelectionEmpty())
			{
				FNX.dbg("No selection available - disabling menus...");
				HTGT.updateSelectionMenuItems(false);
			}
			else
			{
				FNX.dbg("Selection available - enabling menus...");
				HTGT.updateSelectionMenuItems(true);
			}
		}
	}
}

class HTGT_FFM_Observer extends SwingWorker<Integer,Integer>
{
	private File fileHandle;
	private boolean initState;
	private boolean queueState;
	private boolean firstRun;
	private FileTime oldTime;
	private FileTime newTime;
	private int currentTime;

	public void setFile(File f)
	{
		fileHandle = f;
	}

	public void firstRun()
	{
		firstRun = true;

		secondRun();
	}

	public void secondRun()
	{
		execute();
	}

	protected Integer doInBackground() throws IOException, InterruptedException
	{
		if(fileHandle == null)
		{
			throw new IllegalStateException("FFM not initialized");
		}

		// Ein schmutziger Hack, damit auf jeden Fall die GUI bereits
		// blockiert ist. Andernfalls könnte das zu unschönen Race-
		// Conditions führen, die noch nicht abgefangen werden.
		Thread.sleep(HTGT.FF_OBSERVER_DELAY);

		// Durch diesen kleinen Hack wird die Datei beim Start
		// auf jeden Fall einmal neu eingelesen. Dadurch sollte
		// es keine Probleme mehr geben, wenn User den FFM erst
		// zu spät starten. Wenn es keine Änderungen gibt, macht
		// das nichts, da das sowieso im Hintergrund passiert...
		oldTime = Files.getLastModifiedTime(fileHandle.toPath());
		FNX.dbgf("FFM background thread started: o=%d", oldTime.toMillis());

		if(firstRun)
		{
			FNX.dbg("This is the first run, triggering now!");
			publish((int) (oldTime.toMillis() / 1000) * -1);

			firstRun = false;
		}
		else
		{
			FNX.dbg("This is not the first run...");
			publish((int) (oldTime.toMillis() / 1000));
		}

		int m;
		while(true)
		{
			if(HTGT.ENABLE_WATCHSERVICE)
			{
				FNX.dbg("Preparing watchservice...");

				WatchService watchService = FileSystems.getDefault().newWatchService();

				File file = HTGT.getFile();
				String basename = file.getName();

				Path path = Paths.get(file.getParent().toString());
				path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

				FNX.dbg("Waiting for first event...");

				WatchKey key;
				Long last = 0L;
				while((key = watchService.take()) != null)
				{
					for(WatchEvent<?> event : key.pollEvents())
					{
						if(basename.equals(event.context().toString()))
						{
							newTime = Files.getLastModifiedTime(fileHandle.toPath());

							if(newTime.toMillis() > last)
							{
								last = newTime.toMillis();

								if(newTime.toInstant().isAfter(Instant.now().minusMillis(HTGT.FF_OBSERVER_DELAY)))
								{
									publish((int) (newTime.toMillis() / 1000));
									Thread.sleep(HTGT.FF_OBSERVER_DELAY);

									newTime = Files.getLastModifiedTime(fileHandle.toPath());

									if(newTime.toMillis() > last)
									{
										FNX.dbgf("Watchservice event delayed: %s (o=%d n=%d)", event.kind(), oldTime.toMillis(), newTime.toMillis());

										continue;
									}
								}

								FNX.dbgf("Watchservice event received: %s (o=%d n=%d)", event.kind(), oldTime.toMillis(), newTime.toMillis());
								publish((int) (newTime.toMillis() / 1000) * -1);
								oldTime = newTime;
							}
							else
							{
								FNX.dbgf("Watchservice event ignored: %s (o=%d n=%d)", event.kind(), oldTime.toMillis(), newTime.toMillis());
							}
						}
						else
						{
							FNX.dbgf("Watchservice event ignored: %s (%s)", event.kind(), event.context());
						}
					}
					key.reset();

					FNX.dbg("Waiting for next event...");
				}
				break;
			}
			else
			{
				newTime = Files.getLastModifiedTime(fileHandle.toPath());
				m = 1;

				if(newTime.compareTo(oldTime) > 0)
				{
					// Durch diesen Teil sparen wir uns die Wartezeit vor dem Laden der XML-Datei.
					// Dadurch soll sichergestell werden, dass das Spiel mit dem Speichern fertig ist.
					// Klar, das ist auch kein 100% Schutz und es gibt zig andere problematische Stellen.
					// Aber es ist ein grundlegender Schutz, dass wir keine halbfertigen Dateien einlesen.
					if(newTime.toInstant().isAfter(Instant.now().minusMillis(HTGT.FF_OBSERVER_DELAY)))
					{
						FNX.dbgf("File modification time changed, but it's too early: o=%d n=%d d=%d", oldTime.toMillis(), newTime.toMillis(), HTGT.FF_OBSERVER_DELAY);
					}
					else
					{
						FNX.dbgf("File modification time changed: o=%d n=%d d=%d", oldTime.toMillis(), newTime.toMillis(), HTGT.FF_OBSERVER_DELAY);
						oldTime = newTime;
						m = -1;
					}
				}
				else
				{
					FNX.dbg("Nothing to do! Sleeping...");
				}

				publish((int) (newTime.toMillis() / 1000) * m);
				Thread.sleep(HTGT.FF_CHECK_INTERVAL);
			}
		}

		return 0;
	}

	protected void process(List chunks)
	{
		int modificationTime = 0;
		boolean invokeCheck = false;

		for(int i = 0; i < chunks.size(); i++)
		{
			int chunk = (int) chunks.get(i);

			if(chunk < 0)
			{
				invokeCheck = true;
				modificationTime = chunk * -1;
			}
			else
			{
				modificationTime = chunk;

				if(currentTime != modificationTime)
				{
					initState = false;
				}
			}

			currentTime = modificationTime;
		}

		if(isCancelled())
		{
			FNX.dbg("Already cancelled!");
		}
		else if(invokeCheck || queueState)
		{
			if(queueState)
			{
				FNX.dbg("Executing earlier queue request(s)...");
			}
			else
			{
				FNX.dbg("Woohoo! Ready to party...");
			}

			if(!HTGT.fastFollowAnalyze())
			{
				FNX.dbg("Worker thread busy, waiting for next run...");

				queueState = true;
			}
			else
			{
				HTGT.fastFollowStatus(modificationTime);

				queueState = false;
				initState = true;
			}
		}
		else if(!initState)
		{
			FNX.dbg("Updating status message...");
			HTGT.fastFollowStatus(modificationTime);
			initState = true;
		}
		else
		{
			FNX.dbg("Nothing to do, sleeping...");
		}
	}

	protected void done()
	{
		FNX.dbg("done() called");

		try
		{
			get();
		}
		catch(CancellationException e)
		{
			FNX.dbg("FFM background thread stopped.");
		}
		catch(InterruptedException e)
		{
			FNX.dbg("FFM background thread interrupted.");
			e.printStackTrace();
		}
		catch(ExecutionException e)
		{
			HTGT.exceptionHandler(e);
		}

		HTGT.fastFollowStop();
		FNX.dbg("done() finished");
	}
}

class HTGT_FFM_Analyst extends SwingWorker<Integer,Integer>
{
	public HTGT_FFM_Analyst()
	{
		HTGT.fastFollowLock();
	}

	protected Integer doInBackground() throws Exception
	{
		FNX.dbg("FFM evaluation thread started.");

		return HTGT.fastFollowEvaluation();
	}

	protected void done()
	{
		try
		{
			Integer result = get();

			FNX.dbgf("FFM evaluation thread result: %d", result);

			if(result < 1)
			{
				HTGT.fastFollowStop();
			}
			else
			{
				HTGT.fastFollowStatus();
			}
		}
		catch(CancellationException e)
		{
			FNX.dbg("FFM evaluation thread stopped.");
		}
		catch(InterruptedException e)
		{
			FNX.dbg("FFM evaluation thread interrupted.");
			e.printStackTrace();
		}
		catch(ExecutionException e)
		{
			FNX.dbg("FFM evaluation thread caused an exception...");

			HTGT.fastFollowStop();

			if(e.getCause() instanceof eSportsAPIException)
			{
				HTGT.APIError((eSportsAPIException) e.getCause());
			}
			else
			{
				HTGT.exceptionHandler(e);
			}
		}

		HTGT.fastFollowUnlock();

		FNX.dbg("FFM evaluation thread finished.");
	}
}

// Wird benötigt, damit der benutzerdefinierte Button eine Funktion hat.
class HTGT_FFM_ActionListener implements ActionListener
{
	public void actionPerformed(ActionEvent e)
	{
		FNX.dbg("Button clicked");
		HTGT.fastFollowStop(false);
	}
}

// Wird benötigt, damit wirklich jeder Ausstieg deaktiviert werden kann.
class HTGT_FFM_KeyListener extends KeyAdapter /*implements KeyListener*/
{
	private boolean disable;

	public void enable()
	{
		disable = false;
	}

	public void disable()
	{
		disable = true;
	}

	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			if(disable)
			{
				FNX.dbg("Ignoring VK_ESCAPE event");
				e.consume();
			}
			else
			{
				FNX.dbg("VK_ESCAPE event triggered");
				HTGT.fastFollowStop(false);
			}
		}
	}
}

// Wird für zusätzliche Hotkeys ohne Menüeintrag benötigt.
class HTGT_ActionListener extends AbstractAction
{
	private String action;
	private Object[] args;

	public void setPrivateAction(String m)
	{
		action = m;
	}

	public void setPrivateArguments(Object[] a)
	{
		args = a;
	}

	public void actionPerformed(ActionEvent e)
	{
		if(action != null)
		{
			FNX.actionCallback("HTGT", action, args);
		}
	}
}

// TODO: Echte Sortierung der Tabelle ermöglichen? Dafür bräuchten wir
// aber die einzelnen Geister irgendwo versteckt in der Tabelle, oder?
// https://docs.oracle.com/javase/tutorial/uiswing/components/table.html#sorting
// ...

// TODO: Menüfunktion (Hilfe), um den Pfad der aktuellen Datei in die Zwischenablage zu kopieren?
// ...

// Note: FNX_ContextMenu.java uses or overrides a deprecated API.
// ...

// Ich glaube, der FFM hat ein kleines Problem.
// Bei jedem Start explodiert der Ram-Verbrauch.
// Egal, ob mit oder ohne ENABLE_BLURRY...
// System.gc() bringt auch nichts.
// ...
