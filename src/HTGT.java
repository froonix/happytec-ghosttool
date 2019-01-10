/**
 * HTGT.java: Main class (GUI) for Happytec-Ghosttool
 * Copyright (C) 2019 Christian Schrötter <cs@fnx.li>
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

import java.nio.file.attribute.FileTime;

import java.net.URI;

import java.lang.IndexOutOfBoundsException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import java.util.prefs.Preferences;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
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
import java.awt.event.KeyEvent;

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
import javax.swing.UIManager;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.plaf.basic.BasicTableHeaderUI;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class HTGT
{
	// Diverse fixe Konstanten für die Anwendung
	final private static String    APPLICATION_VERSION = "git-master";
	final private static String    APPLICATION_NAME    = "HTGT"; // cfg, updates, …
	final private static String    APPLICATION_TITLE   = "HTGT.app";
	final private static String    APPLICATION_API     = "HAPPYTEC-eSports-API";
	final private static String    APPLICATION_IDENT   = "HTGT %s <https://github.com/froonix/happytec-ghosttool>";
	final private static Dimension WINDOW_SIZE_START   = new Dimension(900, 600);
	final private static Dimension WINDOW_SIZE_MIN     = new Dimension(600, 200);
	final private static long      UPDATE_INTERVAL     = 86400000L; // daily
	final private static long      WEATHER_INTERVAL    = 3600000L; // hourly
	final private static int       FF_CHECK_INTERVAL   = 5000; // 5 seconds
	final private static String    SPECIAL_PROFILE     = "SpecialProfile";
	final private static String    DEFAULT_PROFILE     = "DefaultUser";
	final private static String    VERSION_FILE        = "htgt-version.txt";
	final private static int       NICKNAME_MINLEN     = 3;
	final private static int       NICKNAME_MAXLEN     = 13;
	final private static String    NICKNAME_REGEX      = "^(?i:[A-Z0-9_]{" + NICKNAME_MINLEN + "," + NICKNAME_MAXLEN + "})$";
	final private static String    NICKNAME_REGEX_NOT  = "^[0-9]+$";
	final private static boolean   ENABLE_AUTOSAVE     = true;
	final private static boolean   ENABLE_RACE         = true;
	final private static boolean   ENABLE_XTC          = true;
	final private static boolean   ENABLE_SUC          = true;
	final private static int       FONTSIZE            = 13;
	final private static double    FONTSMALL           = 0.75;
	final private static int       HISTORY_SIZE        = 10;

	final public static int        NONE  = 0;
	final public static int        CTRL  = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	final public static int        SHIFT = ActionEvent.SHIFT_MASK;
	final public static int        ALT   = ActionEvent.ALT_MASK;

	// Diverse Links ohne https:// davor, da sie als Ziel direkt angezeigt werden sollen!
	private final static String    URL_WWW  = "github.com/froonix/happytec-ghosttool";
	private final static String    URL_API  = "www.esports.happytec.at";

	// Redirect-Service für diverse andere Links oder Aktionen. (Leitet derzeit alles nur zum Forenthread...)
	private final static String    URL_REDIRECT = "https://www.esports.happytec.at/redirect/desktop/HTGT.php?dst=%s";

	// Bei jeder neuen verfügbaren Sprache muss dieser Wert erhöht werden.
	// Dadurch wird der Dialog für die Sprachauswahl erneut angezeigt werden.
	// Es sollte nicht dazu verwendet werden, um die Sprachen zu zählen!
	private final static int TRANSLATION_VERSION = 3;

	// Alle verfügbaren Sprachen als Locale-String.
	// In dieser Reihenfolge werden sie auch angezeigt!
	private final static String[] LOCALES = new String[]{ "de_DE", "en_UK", "it_IT", "sk_SK" };

	// Konfigurationsnamen für java.util.prefs
	final private static String CFG_LOCALE      = "locale";
	final private static String CFG_TRANSLATION = "translation";
	final private static String CFG_API         = "api-host";
	final private static String CFG_DC          = "dll-check";
	final private static String CFG_UC          = "update-check";
	final private static String CFG_DEFAULT     = "default-file";
	final private static String CFG_TOKEN       = "esports-token";
	final private static String CFG_CWD         = "last-directory";
	final private static String CFG_CWDPORT     = "last-port-directory";
	final private static String CFG_PROFILE     = "last-profile";
	final private static String CFG_MODE        = "last-gamemode";
	final private static String CFG_WEATHER     = "last-weather";
	final private static String CFG_TRACK       = "last-track";
	final private static String CFG_NDG         = "never-download";
	final private static String CFG_ARG         = "always-replace";
	final private static String CFG_AAR         = "always-apply";
	final private static String CFG_WC          = "weather-check";
	final private static String CFG_TRACKS      = "track-order";
	final private static String CFG_RACE        = "race.%s.%s";

	// ALWAYS THE INDEX NUMBER! WITHOUT THE EXTRA COUNT.
	final private static int OFFSET_RACE   =  0;
	final private static int OFFSET_TICKET =  1;
	final private static int OFFSET_SUC    =  2;

	final private static int PROFILE_NONE    =  0;
	final private static int PROFILE_DEFAULT = -1;
	final private static int PROFILE_SPECIAL = -2;

	final private static int BUTTON_ALWAYS =  2;
	final private static int BUTTON_YES    =  1;
	final private static int BUTTON_CLOSED =  0;
	final private static int BUTTON_NO     = -1;
	final private static int BUTTON_NEVER  = -2;

	// DON'T USE VALUES GREATER THAN ZERO HERE...
	// AND DON'T FORGET: CLOSED_OPTION IS -1 TOO!
	final private static int BUTTON_CANCEL = -1;
	final private static int BUTTON_PREV   = -2;
	final private static int BUTTON_NEXT   = -3;

	final private static String MENU_STATIC  = "static";                // Immer aktiv, unabhängig vom Kontext/Status.
	final private static String MENU_DEFAULT = "default";               // Aktiv, sobald eine XML-Datei geladen wurde.
	final private static String MENU_UNDO    = "undo";                  // Aktiv, sobald der Verlauf ältere Strings enthält.
	final private static String MENU_REDO    = "redo";                  // Aktiv, sobald der Verlauf neuere Strings enthält.
	final private static String MENU_TOKEN   = "token";                 // Aktiv, sobald ein API-Token existiert – unabhängig vom Kontext/Status.
	final private static String MENU_FTOKEN  = "ftoken";                // Aktiv, sobald ein API-Token existiert und eine XML-Datei geladen wurde.
	final private static String MENU_STOKEN  = "stoken";                // Aktiv, sobald ein API-Token existiert und Geister markiert wurden.
	final private static String MENU_PTOKEN  = "ptoken";                // Aktiv, sobald ein API-Token im geladenen XML-Profil existiert.
	final private static String MENU_SELECT  = "select";                // Aktiv, sobald Geister markiert wurden.

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
	private static int                        lastApplicationPosition;

	private static boolean                    debugMode;
	private static DateFormat                 debugDate;

	private static OfflineProfiles            OfflineProfiles;

	private static volatile boolean           ffState;
	private static volatile JDialog           ffDialog;
	private static volatile boolean           ffChanged;

	private static JFrame                     mainWindow;
	private static JTable                     maintable;
	private static DefaultTableModel          mainmodel;

	private static Map<String,ArrayList<DynamicMenuItem>> menuitems;

	private static void dbg(String msg, int trace)
	{
		if(debugMode)
		{
			if(debugDate == null)
			{
				debugDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZZ");
			}

			System.err.printf("[%s] %s - %s%n", debugDate.format(new Date()), Thread.currentThread().getStackTrace()[2 + trace].toString(), msg);
		}
	}

	public static void dbgf(String msg, Object... args)
	{
		dbg(String.format(msg, args), 1);
	}

	public static void dbg(String msg)
	{
		dbg(msg, 1);
	}

	public static void about()
	{
		String licence = String.format(
			  "Copyright (C) 2019 Christian Schr&ouml;tter &lt;cs@fnx.li&gt;<br /><br />"
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

	private static void exceptionHandler(Exception e)
	{
		exceptionHandler(e, null);
	}

	private static void exceptionHandler(Exception e, String msg)
	{
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
				debugMode = true;
			}
			else
			{
				debugMode = false;
			}
		}

		// Beim Debuggen aus dem Editor heraus sollen standardmäßig alle Debugausgaben sichtbar sein.
		// Heißt im Detail: Wenn die Versionsdatei im JAR nicht existiert, wird der Debugmodus aktiviert.
		// In allen anderen Fällen, wenn übers Makefile ein normales Build generiert wurde, ist er inaktiv.
		// Er kann natürlich trotzdem jederzeit über den Parameter "-d" bzw. das Debugscript aktiviert werden.
		if(HTGT.class.getResource("/" + VERSION_FILE) == null)
		{
			debugMode = true;

			dbg("Huh? This isn't a JAR! Happy debugging... :-)");
		}

		dbgf("%s version: %s", APPLICATION_NAME, getVersion(true));

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
			dbgf("New translation(s) available! [cfg=%s; cur=%d]", cfg(CFG_TRANSLATION), TRANSLATION_VERSION);

			selectLanguage(true, (cfg(CFG_TRANSLATION) != null ? true : false));
			cfg(CFG_TRANSLATION, Integer.toString(TRANSLATION_VERSION));
		}

		String apihost = cfg(CFG_API);
		if(apihost != null && apihost.length() > 0)
		{
			dbg("API FQDN: " + apihost);
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
				menu.add(registerDynMenuItem(MENU_PTOKEN,   langKey + ".removeTokenFromProfile",    "removeTokenFromProfile", KeyStroke.getKeyStroke(KeyEvent.VK_R,      CTRL | SHIFT)));
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
				menu.add(registerDynMenuItem(MENU_DEFAULT,  langKey + ".applyDefaultPath",          "applyDefaultFile",       KeyStroke.getKeyStroke(KeyEvent.VK_A,      SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".resetQuestions",            "resetQuestions",         KeyStroke.getKeyStroke(KeyEvent.VK_R,      CTRL | SHIFT)));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".resetConfiguration",        "clearConfigDialog",      KeyStroke.getKeyStroke(KeyEvent.VK_R,      CTRL)));
				break;

			default:
				dbgf("Unknown menu »%s«", key);
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
		dbg("ghosts.length: " + ghosts.length);
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
		String suffix = null;
		String selection = null;
		String[] profiles;
		String[] values;

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
					suffix = String.format(" (%s)", suffix);
				}

				values[i] = String.format("[%0" + Integer.toString(FNX.strlen(profiles.length)) + "d] %s%s", i + 1, profiles[i], suffix);

				if(profile == i)
				{
					selection = values[i];
				}
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
				lastProfile = index + 1;
			}

			cfg(CFG_PROFILE, Integer.toString(lastProfile));
		}

		profile = index;
		syncGUI();
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
				dbgf("Last used profile: DEFAULT (%d)", selectedProfile);
			}
			else if(lastProfile == PROFILE_SPECIAL)
			{
				String[] profiles = OfflineProfiles.getProfiles();

				for(int i = 0; i < profiles.length; i++)
				{
					if(isSpecialProfile(profiles[i]))
					{
						selectedProfile = i;
						dbgf("Last used profile: SPECIAL (%d)", selectedProfile);
						break;
					}
				}
			}
			else if(lastProfile != PROFILE_NONE)
			{
				selectedProfile = lastProfile - 1;
				selectedProfile = (selectedProfile < 0 || selectedProfile >= OfflineProfiles.getProfileCount()) ? PROFILE_NONE : selectedProfile;
				dbgf("Last used profile: %d", selectedProfile);
			}
			else
			{
				dbg("Last used profile unknown...");
			}

			selectProfile(selectedProfile);
		}
		catch(Exception e)
		{
			exceptionHandler(e);
		}
	}

	public static void syncGUI()
	{
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
			dbg("Forcing ghost replacement because of previous choice...");
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
		if(profile == OfflineProfiles.defaultProfile() || isSpecialProfile())
		{
			if(confirmDialog(FNX.formatLangString(lang, "incompatibleProfile")))
			{
				selectProfile();
			}

			if(profile == OfflineProfiles.defaultProfile() || isSpecialProfile())
			{
				return true;
			}
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

	public static void fastFollow(boolean force)
	{
		if(OfflineProfiles == null || checkProfile() || unsavedChanges())
		{
			return;
		}

		try
		{
			if(!prepareAPI())
			{
				return;
			}

			// Diese API-Anfrage ist hier noch nicht notwendig.
			// Dadurch wird aber schon hier geprüft, ob der Token
			// gültig ist und ob aktive Strecken verfügbar sind.
			int[][][][] results = api.getAllResults();

			while(true)
			{
				JOptionPane msg = new JOptionPane(FNX.formatLangString(lang, "fastFollowModeBody"), JOptionPane.PLAIN_MESSAGE);
				msg.setOptions(new String[]{FNX.getLangString(lang, "cancel")});
				ffDialog = msg.createDialog(mainWindow, FNX.getLangString(lang, "fastFollowMode" + (force ? "Force" : "")));

				ffState = true;
				dbg("Starting worker thread...");
				new Thread(new HTGT_Background(HTGT_Background.EXEC_FASTFOLLOW)).start();
				dbg("Opening blocking info dialog...");
				ffDialog.setVisible(true);
				ffState = false;

				if(ffChanged)
				{
					dbg("We are back in the main thread!");

					GhostElement[][][] oldProfileGhosts = null;
					GhostElement[][][] oldDefaultGhosts = null;
					int oldProfileCount = OfflineProfiles.getProfileCount();
					int oldDefaultProfile = OfflineProfiles.defaultProfile();

					oldProfileGhosts = OfflineProfiles.getAllGhosts();

					if(oldDefaultProfile > -1)
					{
						OfflineProfiles.selectProfile(oldDefaultProfile);
						oldDefaultGhosts = OfflineProfiles.getAllGhosts();
						OfflineProfiles.selectProfile(profile);
					}

					reloadFile(true);

					GhostElement[][][] newProfileGhosts = null;
					GhostElement[][][] newDefaultGhosts = null;
					int newProfileCount = OfflineProfiles.getProfileCount();
					int newDefaultProfile = OfflineProfiles.defaultProfile();

					if(oldProfileCount != newProfileCount || oldDefaultProfile != newDefaultProfile)
					{
						dbgf("Unsupported changes: %d != %d || %d != %d", oldProfileCount, newProfileCount, oldDefaultProfile, newDefaultProfile);
						errorMessage(FNX.getLangString(lang, "fastFollowMode"), "Unsupported changes detected!");
						return;
					}

					newProfileGhosts = OfflineProfiles.getAllGhosts();

					if(newDefaultProfile > -1)
					{
						dbgf("Switching to profile %d...", newDefaultProfile);
						OfflineProfiles.selectProfile(newDefaultProfile);

						newDefaultGhosts = OfflineProfiles.getAllGhosts();

						dbgf("Using old profile %d...", profile);
						OfflineProfiles.selectProfile(profile);
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
								if((oldProfileGhosts[m][t][w] == null && newProfileGhosts[m][t][w] != null) || (oldProfileGhosts[m][t][w] != null && newProfileGhosts[m][t][w] != null && oldProfileGhosts[m][t][w].getTime() != newProfileGhosts[m][t][w].getTime()))
								{
									dbgf("Changed result: %s / %s / %s", gmHelper.getGameModeName(modes[m]), gmHelper.getTrack(tracks[t]), gmHelper.getWeatherName(weathers[w]));

									// ghostUpload(newProfileGhosts[t][w], true);

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
									if((oldDefaultGhosts[m][t][w] == null && newDefaultGhosts[m][t][w] != null) || (oldDefaultGhosts[m][t][w] != null && newDefaultGhosts[m][t][w] != null && oldDefaultGhosts[m][t][w].getTime() != newDefaultGhosts[m][t][w].getTime()))
									{
										dbgf("Changed (default) result: %s / %s / %s", gmHelper.getGameModeName(modes[m]), gmHelper.getTrack(tracks[t]), gmHelper.getWeatherName(weathers[w]));

										// ghostUpload(newDefaultGhosts[t][w], true);

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
						if(force)
						{
							for(int i = 0; i < ghosts.size(); i++)
							{
								ArrayList item = ghosts.get(i);
								GhostElement ghost = (GhostElement) item.get(3);
								int w = (int) item.get(2);

								dbgf("Uploading ghost in FORCE mode: %s", ghost.getDebugDetails());
								ghostUpload(ghost, true);

								realUpload = lastApplicationStatus;

								dbgf("Last FO from server reply: %d", lastFilterOption);

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

								dbgf("Set lastUploadedWeather to: %d", lastUploadedWeather);
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
									dbgf("Uploading ghost: %s", ghost.getDebugDetails());
									ghostUpload(ghost, true);
									realUpload = true;
								}
								else
								{
									dbgf("Ghost upload not possible, because old result (%d) is better or equal: %s", results[o][m][t][w], ghost.getDebugDetails());

									if(o == eSportsAPI.FO_TICKET)
									{
										dbg("Still uploading it because it's a TICKET ghost...");
										ghostUpload(new GhostElement[]{ghost}, true, true);
									}

									// eSportsAPI.FO_SUC?
									// ...
								}
							}
						}
					}

					// lastApplicationPosition? (ExpectedPosition)
					// ...

					if(realUpload && lastUploadedMode > -1 && lastUploadedTrack > -1 && lastUploadedWeather != -1 && !foreignGhostEnabled())
					{
						if(/*lastFromDefault &&*/ lastUploadedWeather > -1 && newProfileGhosts[lastUploadedMode][lastUploadedTrack][lastUploadedWeather] != null)
						{
							currentGhost = String.format("%s%n", FNX.formatLangString(lang, "fastFollowCurrentGhost", newProfileGhosts[lastUploadedMode][lastUploadedTrack][lastUploadedWeather].getNickname(), newProfileGhosts[lastUploadedMode][lastUploadedTrack][lastUploadedWeather].getResult()));
						}

						if(cfg(CFG_NDG) == null)
						{
							int realWeather;
							switch(lastUploadedWeather)
							{
								case gmHelper.WEATHER_SUC:
								case gmHelper.WEATHER_TICKET:
									realWeather = lastUploadedWeather;
									break;

								default:
									realWeather = weathers[lastUploadedWeather];
									break;
							}

							int action = threesomeDialog(FNX.getLangString(lang, "fastFollowMode"),
								FNX.formatLangString(lang, "fastFollowGhostQuestion", gmHelper.getTrack(tracks[lastUploadedTrack]), gmHelper.getGameModeName(modes[lastUploadedMode]), gmHelper.getWeatherName(realWeather)) +
								(!ENABLE_AUTOSAVE ? String.format("%n%s", FNX.formatLangString(lang, "fastFollowNoAutosave")) : "") + (currentGhost != null ? String.format("%n%s", currentGhost) : "")
							, false);

							if(action == BUTTON_NEVER)
							{
								cfg(CFG_NDG, "true");
							}
							else if(action == BUTTON_YES)
							{
								Boolean result = ghostSelect(modes[lastUploadedMode], tracks[lastUploadedTrack], realWeather, true, ((realWeather < -1) ? true : false));

								if(result != null && result == true)
								{
									if(OfflineProfiles.changed() && !saveFile(true))
									{
										throw new Exception("Could not save file");
									}
								}
							}
						}
						else
						{
							dbg("Skipping ghost download because of previous choice...");
						}
					}

					continue;
				}
				else
				{
					dbg("Dialog canceled or closed.");
					break;
				}

				// Thread.sleep(1000);
			}
		}
		catch(eSportsAPIException e)
		{
			APIError(e);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
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
				dbgf("Restoring profile %d...", profile);
				OfflineProfiles.selectProfile(profile);
			}
			catch(Exception e)
			{
				exceptionHandler(e);
				syncGUI();
			}
		}
	}

	protected static void fastFollowWorker()
	{
		FileTime oldTime;
		FileTime newTime;

		ffChanged = false;

		if(OfflineProfiles == null)
		{
			return;
		}

		try
		{
			oldTime = Files.getLastModifiedTime(file.toPath());

			while(true)
			{
				if(!ffState)
				{
					dbg("Killed via external state variable.");
					return;
				}

				newTime = Files.getLastModifiedTime(file.toPath());

				if(newTime.compareTo(oldTime) > 0)
				{
					dbg("File modification time changed!");
					Thread.sleep(1000);
					oldTime = newTime;
					ffChanged = true;
					return;
				}
				else
				{
					dbg("Nothing to do. Sleeping...");
				}

				Thread.sleep(FF_CHECK_INTERVAL);
			}
		}
		catch(IOException e)
		{
			exceptionHandler(e);
			return;
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		finally
		{
			ffState = false;
			ffDialog.setVisible(false);
			dbg("Cleanup. Goodbye...");
		}
	}

	private static Profiles getProfileHandle(String nick)
	{
		File profilesFile = new File(String.format("%2$s%1$s%3$s", File.separator, file.getParent().toString(), "Profiles.xml"));

		if(profilesFile == null || !profilesFile.exists() || !profilesFile.isFile())
		{
			dbgf("Other XML file not found: %s", profilesFile);
			errorMessage(null, FNX.formatLangString(lang, "fileNotFound", "Profiles.xml"));
		}
		else
		{
			dbgf("Other XML file: %s", profilesFile);

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
		if(OfflineProfiles == null)
		{
			return;
		}

		Profiles profiles = getProfileHandle(nickname);

		if(profiles == null || checkProfile() || unsavedChanges())
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

			if(regularProfiles < 2)
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
			dbgf("User config XML file not found: %s", userConfigFile);
		}
		else
		{
			dbgf("User config XML file: %s", userConfigFile);

			try
			{
				UserConfig config = new UserConfig(userConfigFile);

				if(config.getMultiGhost())
				{
					dbg("MultiGhost setting is enabled.");
					return true;
				}
				else if(config.getTrainingGhostNick() != null)
				{
					dbg("TrainingGhostNick selection is not empty.");
					return true;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		dbg("MultiGhost setting is disabled.");
		dbg("TrainingGhostNick selection is empty.");

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
			dbg("DLL not initialized or not found.");

			if(!auto)
			{
				errorMessage(null, FNX.formatLangString(lang, "patchUpdatesDLL404"));
			}

			return;
		}

		Date date = new Date();
		lastDLLCheck = cfg.getLong(CFG_DC, 0L);
		dbgf("Current time: %d", date.getTime());
		dbgf("Last DLL check: %d", lastDLLCheck);
		dbgf("Check interval: %d", UPDATE_INTERVAL);

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
				dbgf("SHA512: %s", hash);

				if(anonAPI.updateAvailable("SC.DLL", hash, auto))
				{
					dbg("New DLL available!" + ((auto) ? " (autocheck)" : ""));

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
					dbg("No new DLL available..." + ((auto) ? " (autocheck)" : ""));

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
			dbgf("Input dialog: SELECTION %s", title);
		}
		else
		{
			dbgf("Input dialog: INPUTFIELD %s", title);
		}

		Object input = JOptionPane.showInputDialog(mainWindow, message, title, JOptionPane.PLAIN_MESSAGE, null, selectionValues, initialSelectionValue);

		if(input == null)
		{
			dbg("Input dialog: CANCEL");
			return null;
		}

		String selected = input.toString();

		if(selectionValues != null)
		{
			for(int i = 0; i < selectionValues.length; i++)
			{
				if(selected.equals(selectionValues[i]))
				{
					dbgf("Input dialog: SELECTED #%d", i);
					return i;
				}
			}

			dbg("Input dialog: SELECTION UNKNOWN");
			return null;
		}
		else
		{
			selected = selected.trim();
			dbgf("Input dialog: VALUE(%d) %s", selected.length(), selected);
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
			dbgf("File dialog: OPEN %s", directory);
			chooser = new JFileChooser(directory);
		}
		else
		{
			dbgf("File dialog: SAVE %s", directory);
			chooser = new ImprovedFileChooser(directory);
		}

		if(selection != null)
		{
			dbgf("File dialog: SET %s", selection.getAbsolutePath());
			chooser.setSelectedFile(selection);
		}

		// dbg("File dialog: FILTER *.xml");
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
			dbgf("File dialog: APPROVE %s", selectedFile);

			if(selectedFile != null && (!open || selectedFile.exists()))
			{
				return selectedFile;
			}
			else
			{
				dbg("File dialog: FILE NOT FOUND");
			}
		}
		else if(code == JFileChooser.CANCEL_OPTION)
		{
			dbg("File dialog: CANCEL");
		}
		else if(code == JFileChooser.ERROR_OPTION)
		{
			dbg("File dialog: ERROR");
		}
		else
		{
			dbg("File dialog: UNKNOWN");
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

		dbgf("New yes/no confirm dialog: %s", title);

		if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(mainWindow, msg, title, JOptionPane.YES_NO_OPTION, type))
		{
			dbg("return TRUE (confirmed)");
			return true;
		}
		else
		{
			dbg("return FALSE (not confirmed)");
			return false;
		}

		/*
		Integer[] values = new Integer[]{BUTTON_YES, BUTTON_NO};
		String[] buttons = FNX.getLangStrings(lang, new String[]{"yes", "no"});
		Object defaultButton = buttons[0];

		int result = JOptionPane.showOptionDialog(mainWindow, msg, title, JOptionPane.YES_NO_OPTION, type, null, buttons, defaultButton);

		if(result != JOptionPane.CLOSED_OPTION && values[result] == BUTTON_YES)
		{
			dbg("return TRUE (confirmed)");
			return true;
		}
		else
		{
			dbg("return FALSE (not confirmed)");
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
			dbgf("New threesome (yes/always/no) dialog: %s", title);
			values = new Integer[]{BUTTON_YES, BUTTON_ALWAYS, BUTTON_NO};
			buttons = FNX.getLangStrings(lang, new String[]{"yes", "always", "no"});
			defaultButton = buttons[0];
		}
		else
		{
			dbgf("New threesome (yes/never/no) dialog: %s", title);
			values = new Integer[]{BUTTON_YES, BUTTON_NEVER, BUTTON_NO};
			buttons = FNX.getLangStrings(lang, new String[]{"yes", "never", "no"});
			defaultButton = buttons[0];
		}

		int result = JOptionPane.showOptionDialog(mainWindow, msg, title, JOptionPane.YES_NO_CANCEL_OPTION, type, null, buttons, defaultButton);

		if(result == JOptionPane.CLOSED_OPTION)
		{
			dbg("return BUTTON_CLOSED");
			return BUTTON_CLOSED;
		}
		else
		{
			dbgf("return [%d] (%s)", values[result], buttons[result]);
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
			dbgf("New prev/next step %s dialog: %s", dialogType, title);
			values = new Integer[]{BUTTON_PREV, BUTTON_CANCEL, BUTTON_NEXT};
			buttons = new String[]{langPrev, langCancel, langNext};
			defaultButton = buttons[2];
		}
		else
		{
			dbgf("New next step %s dialog: %s", dialogType, title);
			values = new Integer[]{BUTTON_CANCEL, BUTTON_NEXT};
			buttons = new String[]{langCancel, langNext};
			defaultButton = buttons[1];
		}

		int result = JOptionPane.showOptionDialog(mainWindow, panel, title, JOptionPane.YES_NO_CANCEL_OPTION, type, null, buttons, defaultButton);

		if(result == JOptionPane.CLOSED_OPTION)
		{
			dbgf("RETURN: %d (CLOSED)", BUTTON_CANCEL);
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

				dbgf("RETURN SELECTION: %d (%s)", returnValues[selectedIndex], selectedItem);

				return (int) returnValues[selectedIndex];
			}
			else
			{
				dbgf("RETURN INPUT: %s", textField.getText());
				return textField.getText();
			}
		}

		dbgf("RETURN: %d (%s)", values[result], buttons[result]);
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
		FNX.windowToFront(mainWindow); // <-- APIError, errorMessage, ...
		JOptionPane.showMessageDialog(mainWindow, msg, title, type);
	}

	private static boolean noSelection()
	{
		dbg("No selection available!");
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
				dbgf("Old default locale: %s", Locale.getDefault());
				Locale.setDefault(new Locale(localeParts[0], localeParts[1]));
				dbgf("New default locale: %s", Locale.getDefault());
			}
		}
		else
		{
			dbgf("Unknown locale string: %s", locale);
			dbgf("Default locale: %s", Locale.getDefault());
		}
	}

	private static void setupLocale()
	{
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

		dbgf("Invalid locale string: %s", locale);

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

	private static void APIError(eSportsAPIException e)
	{
		APIError(e, null);
	}

	// Fehlermeldung der API formatiert ausgeben.
	private static void APIError(eSportsAPIException e, String msg)
	{
		e.printStackTrace();

		if(e.getErrorCode().equals("TOKEN_INVALID"))
		{
			dbg("API token invalid: Removed from prefs!");
			updateToken(null);
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
			dbgf("Update check disabled: %s", APPLICATION_VERSION);

			if(!auto)
			{
				infoDialog(FNX.formatLangString(lang, "updatesDevBuild"));
			}

			return;
		}

		Date date = new Date();
		lastUpdateCheck = cfg.getLong(CFG_UC, 0L);
		dbgf("Current time: %d", date.getTime());
		dbgf("Last update check: %d", lastUpdateCheck);
		dbgf("Check interval: %d", UPDATE_INTERVAL);

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
					dbg("New update available!" + ((auto) ? " (autocheck)" : ""));

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
					dbg("No updates available..." + ((auto) ? " (autocheck)" : ""));

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
				dbg("Copying token to active profile...");
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
				dbg("No token in active profile!");
			}
			else
			{
				if(confirmDialog(JOptionPane.WARNING_MESSAGE, null, FNX.formatLangString(lang, "copyTokenFromProfileQuestion")))
				{
					dbg("Copying token from active profile...");
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
				dbg("No token in active profile!");
			}
			else if(confirmDialog(JOptionPane.WARNING_MESSAGE, null, FNX.formatLangString(lang, "deleTokenFromProfileQuestion")))
			{
				dbg("Removing token from active profile...");
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
					dbg("Invalid API token! Asking once again...");
					errorMessage(FNX.formatLangString(lang, "invalidToken"));
					continue;
				}
				else if(oldToken == null || !oldToken.equals(newToken))
				{
					updateToken(newToken);
				}
				else
				{
					dbg("API token not changed.");
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
					dbg("Token changed! Resetting API instance...");
					api = new eSportsAPI(token, getIdent());
				}

				return true;
			}
			else
			{
				dbgf("Asking for API token... (try #%d)", i + 1);
				setupToken();
			}
		}

		dbg("Three times is enough! No API token available.");

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

	// Interne Funktion für den sofortigen Upload von Geistern.
	private static boolean ghostUpload(GhostElement[] ghosts, boolean silent, boolean doNotApply)
	{
		lastApplicationPosition = 0;
		lastApplicationStatus = false;
		List<Map<String,Object>> result;
		boolean error = false;

		if(!prepareAPI())
		{
			return false;
		}

		try
		{
			result = api.getExtendedGhostIDs(ghosts);
			if(result.size() != ghosts.length)
			{
				dbgf("ghosts(%d) != selection(%d)", result.size(), ghosts.length);
				exceptionHandler(new eSportsAPIException("SERVER_DUMB"));
				return false;
			}
		}
		catch(eSportsAPIException e)
		{
			APIError(e);
			return false;
		}

		for(int i = 0; i < result.size(); i++)
		{
			GhostElement ghost = ghosts[i];
			Map<String,Object> info = result.get(i);

			int ghostID = Integer.parseInt(info.get("GhostID").toString());
			boolean applicable = (Integer.parseInt(info.get("Applicable").toString()) > 0);

			lastFilterOption = Integer.parseInt(info.get("FilterOption").toString());

			dbgf("Item #%d uploaded as ghost ID %d: %s", i, ghostID, ghost.getDebugDetails());

			if(!applicable)
			{
				dbgf("Ghost with ID %d not applicable, skipping...", ghostID);

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
					dbg("Forcing result registration because of previous choice...");
					action = BUTTON_YES;
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

						if(silent)
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
							dbgf("Successfully applied result from ghost with ID %d. (expected rank %d)", ghostID, position);

							if(!silent)
							{
								if(position > 0)
								{
									infoDialog(APPLICATION_API, FNX.formatLangString(lang, "ghostApplySuccessExtended", ghostID, position));
								}
								else
								{
									infoDialog(APPLICATION_API, FNX.formatLangString(lang, "ghostApplySuccess", ghostID));
								}
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
						error = true;
						dbgf("Failed to apply ghost with ID %d.", ghostID);
						APIError(e, FNX.formatLangString(lang, "ghostApplyFailed", ghostID));
					}
				}
			}
		}

		if(!error)
		{
			return true;
		}
		else
		{
			return false;
		}
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
					dbg("ids.length = 0");
					return false;
				}
				else if(ids.length == 1)
				{
					dbg("ids.length = 1");
					ghostdata = new GhostElement[1];
					ghostdata[0] = api.getGhostByID(ids[0]);
				}
				else
				{
					dbg("ids.length > 1");
					ghostdata = api.getGhostsByIDs(ids);
				}

				int imported = ghostImport(ghostdata, force);

				if(imported > 0)
				{
					return true;
				}
				else if(imported == -1)
				{
					dbg("imported = -1");
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
					dbgf("Got %d results.", results.size());
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
					dbg("Something went really wrong! We got an empty result list...");
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
				data.forEach((k,v) -> dbgf("playerDetails.%s: %s", k, v));

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

		dbgf("Current time: %d", date.getTime());
		dbgf("Last weather check: %d", lastWeatherCheck);
		dbgf("Check interval: %d", checkInterval);

		String trackOrder = cfg(CFG_TRACKS);
		if(trackOrder == null || trackOrder.length() == 0)
		{
			// Upgrade: v0.1.0 » v0.1.1
			dbg("Forcing cache flush...");
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

								dbgf("Race weather: %s @ %s (%d) = %d", gmHelper.getGameModeName(modes[m]), gmHelper.getTrack(tracks[t]), i, test[i][m][t]);
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

/***********************************************************************
 *                            FILE ACTIONS                             *
 ***********************************************************************/

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

			dbg("Successfully loaded XML file! Let's rumble...");
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
			dbgf("DLL file exists: %s", dll.getAbsolutePath().toString());
			new Thread(new HTGT_Background(HTGT_Background.EXEC_DLLCHECK)).start();
		}
		else
		{
			dbgf("DLL file not found: %s", dll.getAbsolutePath().toString());
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

		dbg("Reloading file...");
		OfflineProfiles.reload();
		selectProfile(profile);
		syncGUI();
	}

	// Änderungen automatisch speichern.
	private static void autoSave()
	{
		if(ENABLE_AUTOSAVE)
		{
			dbg("AUTOSAVE TRIGGERED!");
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
				dbg("THIS IS A BUG! THERE ARE UNSAVED CHANGES BUT AUTOSAVE IS ENABLED.");
				exceptionHandler(new Exception("AUTOSAVE & UNSAVED TRIGGERED"));
			}
			else
			{
				infoDialog(FNX.formatLangString(lang, "autoSaveEnabled"));
			}
		}

		if(!saveFile(false))
		{
			dbg("Failed to save file! (safe internal state)");
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
			dbg("Something to save...");

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
			dbg("Nothing to save...");
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
				dbg("File saved to new location.");
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
			dbg("Good bye!");
			System.exit(0);
		}
		else
		{
			dbg("File not closed.");
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
					dbgf("Exporting line %d: %s", selection[i], ghost.getDebugDetails());
					data.insert(0, String.format("\t<!-- %s @ %s (%s / %s): %s (%s) -->\r\n\t%s\r\n", ghost.getNickname(), ghost.getTrackName(), ghost.getGameModeName(), ghost.getWeatherName(), ghost.getResult(), gmHelper.formatSki(ghost.getSki(), true), ghost.toString()));
				}

				data.insert(0, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<GhostList>\r\n\r\n");
				data.append(String.format("</GhostList>\r\n<!-- %s -->\r\n", FNX.getDateString()));

				PrintWriter pw = new PrintWriter(selectedFile);
				pw.printf("%s", data.toString()); pw.close();

				dbg("Export to file successfully!");
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
					dbgf("importCounter = %d (ok)", importCounter);
					infoDialog(FNX.formatLangString(lang, "importedGhostsCount", importCounter));
				}
				else if(importCounter == 0)
				{
					dbg("importCounter = 0 (none)");
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
		dbg("History cleared! (index: 0)");

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
			dbg("Nothing changed, not updating history.");
			return;
		}

		String[] newHistory = new String[history.length];
		newHistory[0] = OfflineProfiles.toString();

		int n = 1; int i = 0;
		int c = history.length;

		if(historyIndex != 0)
		{
			dbgf("History index is %d, let's rewind...", historyIndex);

			i = historyIndex;
			historyIndex = 0;
		}

		//dumpHistory("pre-rewind");

		while(i < c && n < c)
		{
			newHistory[n++] = history[i++];
		}

		history = newHistory;
		dbg("History updated!");

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
			dbgf("Restoring from history index %d...", newIndex);

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
			dbgf("Restoring from history index %d is impossible!", newIndex);
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
		dbgf("----- START OF HISTORY DUMP%s -----", title);

		int length = FNX.strlen(HISTORY_SIZE);
		for(int i = 0; i < history.length; i++)
		{
			dbgf("%3$s[%1$0" + length + "d] = %2$s", i, (history[i] == null ? "NULL" : String.format("%d byte", history[i].length())), (i == historyIndex ? "!" : " "));
		}

		dbgf("----- END OF HISTORY DUMP%s -----", title);
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
			dbgf("Config for key \"%s\" unchanged: %s", key, value);
		}
		else
		{
			dbgf("Old config for key \"%s\": %s", key, oldValue);
			dbgf("New config for key \"%s\": %s", key, newValue);

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

		dbgf("Removing config for key \"%s\", old value: %s", key, oldValue);

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
			dbg("Clearing config!");
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

	// Alle Immer/Nie Fragen zurücksetzen.
	// Braucht definitiv keine Bestätigung.
	public static void resetQuestions()
	{
		removeConfig(CFG_NDG);
		removeConfig(CFG_ARG);
		removeConfig(CFG_AAR);
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
					errorMessage(title, message + FNX.formatLangString(lang, "noSpecialProfileAvailable"));
					return;
				}

				Integer selected = (Integer) inputDialog(title, message + FNX.formatLangString(lang, "selectDestinationProfile"), values, defaultSelection);

				if(selected == null)
				{
					return;
				}

				dbgf("Selected profile ID: %2$d (item #%1$d)", selected, profileIDs[selected]);
				selected = profileIDs[selected];

				dbgf("Switching to profile %d...", selected);
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

				dbgf("Using old profile %d...", profile);
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
					dbgf("Restoring profile %d...", profile);
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

		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,      HTGT.NONE             ), "selectPreviousRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP,   HTGT.NONE             ), "selectPreviousRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,      HTGT.SHIFT            ), "selectPreviousRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP,   HTGT.SHIFT            ), "selectPreviousRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,    HTGT.NONE             ), "selectNextRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, HTGT.NONE             ), "selectNextRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,    HTGT.SHIFT            ), "selectNextRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, HTGT.SHIFT            ), "selectNextRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,    HTGT.NONE             ), "selectFirstRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,    HTGT.CTRL             ), "selectFirstRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,    HTGT.SHIFT            ), "selectFirstRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,    HTGT.SHIFT + HTGT.CTRL), "selectFirstRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,     HTGT.NONE             ), "selectLastRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,     HTGT.CTRL             ), "selectLastRow");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,     HTGT.SHIFT            ), "selectLastRowExtendSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_END,     HTGT.SHIFT + HTGT.CTRL), "selectLastRowExtendSelection");

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,  HTGT.NONE             ), "clearSelection");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A,       HTGT.CTRL             ), "selectAll");

		//VK_PAGE_UP
		//VK_PAGE_DOWN

		//getColumnModel().addColumnModelListener(this);
		getModel().addTableModelListener(this);
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

class HTGT_WindowAdapter extends java.awt.event.WindowAdapter
{
	@Override
	public void windowClosing(java.awt.event.WindowEvent windowEvent)
	{
		HTGT.quit();
	}
}

class HTGT_Background implements Runnable
{
	public static final int EXEC_UPDATECHECK = 1;
	public static final int EXEC_FASTFOLLOW  = 2;
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

			case EXEC_FASTFOLLOW:
				HTGT.fastFollowWorker();
				break;

			case EXEC_DLLCHECK:
				HTGT.updateCheckDLL(false, true);
				break;
		}
	}
}

class HTGT_SelectionHandler implements javax.swing.event.ListSelectionListener
{
	public void valueChanged(ListSelectionEvent e)
	{
		javax.swing.ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if(!e.getValueIsAdjusting())
		{
			if(lsm.isSelectionEmpty())
			{
				HTGT.dbg("No selection available - disabling menus...");
				HTGT.updateSelectionMenuItems(false);
			}
			else
			{
				HTGT.dbg("Selection available - enabling menus...");
				HTGT.updateSelectionMenuItems(true);
			}
		}
	}
}
