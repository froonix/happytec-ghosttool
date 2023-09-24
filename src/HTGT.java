/**
 * HTGT.java: Main class (GUI) for Happytec-Ghosttool
 * Copyright (C) 2016-2023 Christian Schrötter <cs@fnx.li>
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
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.Reader;

import java.nio.charset.StandardCharsets;

import java.nio.file.Path;

import java.net.URI;

import java.lang.IndexOutOfBoundsException;

import java.security.cert.Certificate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import javax.swing.plaf.basic.BasicTableHeaderUI;

import javax.swing.plaf.LayerUI;

import javax.swing.table.DefaultTableModel;

public class HTGT
{
	// Diverse fixe Konstanten für die Anwendung
	final public static String    APPLICATION_VERSION = "0.1.15";
	final public static String    APPLICATION_NAME    = "HTGT"; // cfg, updates, …
	final public static String    APPLICATION_TITLE   = "HTGT.app";
	final public static String    APPLICATION_API     = "HAPPYTEC-eSports-API";
	final public static String    APPLICATION_IDENT   = "HTGT %s <https://htgt.app/>";
	final public static Dimension WINDOW_SIZE_START   = new Dimension(900, 600);
	final public static Dimension WINDOW_SIZE_MIN     = new Dimension(600, 200);
	final public static int[]     TEXTAREA_SIZE       = {16, 76}; // rows/cols
	final public static long      UPDATE_INTERVAL     = 86400000L; // daily
	final public static long      WEATHER_INTERVAL    = 3600000L; // hourly
	final public static int       FF_CHECK_INTERVAL   = 2000; // 2 seconds
	final public static long      FF_OBSERVER_DELAY   = 1000; // 1 second
	final public static long      FF_PT_LIMIT         = 10;
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

	final public static int       NONE  = 0;
	final public static int       CTRL  = FNX.getCtrlMask();
	final public static int       SHIFT = ActionEvent.SHIFT_MASK;
	final public static int       ALT   = ActionEvent.ALT_MASK;

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
	final public static String CFG_API_HOST    = "api-host";
	final public static String CFG_API_PROTO   = "api-proto";
	final public static String CFG_API_VERIFY  = "api-insecure";
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
	final public static String CFG_SMSG        = "server-message";
	final public static String CFG_IPV4        = "ipv4";
	final public static String CFG_UC_CONSENT  = "auto-update-check";

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

	private static Locale                                               defaultLocaleAtStartUp;
	private static ResourceBundle                                       lang;

	private static Preferences                                          cfg;
	private static File                                                 dll;
	private static File                                                 file;
	private static int                                                  profile;
	private static String                                               nickname;

	private static String[]                                             history;
	private static int                                                  historyIndex;

	private static String                                               token;
	private static eSportsAPI                                           anonAPI;
	private static eSportsAPI                                           api;

	private static int                                                  lastFilterOption;
	private static boolean                                              lastApplicationStatus;
	private static volatile int                                         lastApplicationPosition;
	private static volatile GhostElement                                lastApplicationGhost;
	private static volatile HashMap<Integer,HashMap<String,Object>>     lastApplicationDestinations;
	private static volatile boolean                                     ffDownload;

	private static OfflineProfiles                                      OfflineProfiles;

	private static JDialog                                              ffDialog;
	private static JButton                                              ffButton;
	private static JOptionPane                                          ffBody;
	private static HTGT_FFM_KeyListener                                 ffListener;
	private static int                                                  ffModification;
	private static int                                                  ffStarted;
	private static boolean                                              ffForce;
	private static HTGT_FFM_Analyst                                     aFFM;
	private static HTGT_FFM_Observer                                    oFFM;

	private static volatile int                                         uploadedCount;
	private static volatile int                                         appliedCount;

	private static JFrame                                               mainWindow;
	private static JTable                                               mainTable;
	private static DefaultTableModel                                    mainModel;
	private static JLayer<Container>                                    mainLayer;
	private static Container                                            mainPane;
	private static MainLayerUI                                          mainUI;

	private static Map<String,ArrayList<DynamicMenuItem>> menuitems;

	public static void about()
	{
		String licence = String.format(
			  "Copyright (C) 2016-2023 Christian Schr&ouml;tter &lt;cs@fnx.li&gt;<br /><br />"
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

	public static void privacy()
	{
		openURL("privacy");
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
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public static void exceptionHandler(Throwable e)
	{
		exceptionHandler(e, null);
	}

	private static void exceptionHandler(Throwable e, String msg)
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
			catch(Throwable e)
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

		if(cfg(CFG_IPV4) == null)
		{
			// Bevorzuge IPv6-Verbindungen, wenn diese verfügbar sind.
			System.setProperty("java.net.preferIPv6Addresses", "true");
			FNX.dbg("java.net.preferIPv6Addresses enabled");
		}

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

		// Zustimmung für automatische Updateprüfung einholen.
		// Wird nur einmalig gemacht, danach im Menü änderbar.
		requestUpdateCheckConsent();

		String apiproto = cfg(CFG_API_PROTO);
		if(apiproto != null && apiproto.length() > 0)
		{
			FNX.dbgf("API Protocol: %s", apiproto);
			eSportsAPI.setProtocol(apiproto);
		}

		String apihost = cfg(CFG_API_HOST);
		if(apihost != null && apihost.length() > 0)
		{
			FNX.dbgf("API Hostname: %s", apihost);
			eSportsAPI.setHost(apihost);
		}

		if(cfg(CFG_API_VERIFY) != null)
		{
			FNX.dbg("API: Certificate verification disabled!");
			eSportsAPI.disableVerification();
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

		mainModel = new DefaultTableModel(rowData, columnNames);
		mainTable = new HTGT_JTable(mainModel);

		// Nur ganze Zeilen dürfen markiert werden!
		mainTable.setColumnSelectionAllowed(false);
		mainTable.setFocusable(false);

		// Spalten dürfen nicht verschoben oder verkleinert werden!
		mainTable.getTableHeader().setReorderingAllowed(false);
		mainTable.getTableHeader().setResizingAllowed(false);

		// macOS würde z.B. gar keine Rahmen anzeigen.
		// Das dürfte aber an der weißen Farbe liegen.
		mainTable.setShowHorizontalLines(true);
		mainTable.setShowVerticalLines(true);

		// Für die Menüelemente müssen wir wissen, wann eine Auswahl getroffen wurde.
		mainTable.getSelectionModel().addListSelectionListener(new HTGT_SelectionHandler());

		// ...
		mainTable.requestFocusInWindow();

		JScrollPane scrollPane = new JScrollPane(mainTable);
		mainWindow.add(scrollPane, BorderLayout.CENTER);

		reset();

		mainUI = new MainLayerUI();
		mainPane = mainWindow.getContentPane();
		mainLayer = new JLayer<>(mainPane, mainUI);
		mainWindow.setContentPane(mainLayer);

		mainWindow.setSize(WINDOW_SIZE_START);
		mainWindow.setMinimumSize(WINDOW_SIZE_MIN);
		mainWindow.setVisible(true);

		// Die automatische Updateprüfung wird im Hintergrund ausgeführt...
		new Thread(new HTGT_Background(HTGT_Background.EXEC_UPDATECHECK)).start();
	}

	public static void blur()
	{
		if(!ENABLE_BLURRY)
		{
			return;
		}

		FNX.dbg("Blurring main window...");
		mainUI.enableEffects();
		mainLayer.repaint();
	}

	public static void unblur()
	{
		if(!ENABLE_BLURRY)
		{
			return;
		}

		FNX.dbg("Unblurring main window...");
		mainUI.disableEffects();
		mainLayer.repaint();
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

		if(FNX.getDebugging())
		{
			menu.add(getMenu("debug"));
		}

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
//				menu.add(registerDynMenuItem(MENU_FTOKEN,   langKey + ".copyTokenToProfile",        "copyTokenToProfile",     KeyStroke.getKeyStroke(KeyEvent.VK_T,      CTRL | SHIFT)));
//				menu.add(registerDynMenuItem(MENU_PTOKEN,   langKey + ".copyTokenFromProfile",      "copyTokenFromProfile",   KeyStroke.getKeyStroke(KeyEvent.VK_U,      CTRL | SHIFT)));
//				menu.add(registerDynMenuItem(MENU_PTOKEN,   langKey + ".removeTokenFromProfile",    "removeTokenFromProfile"));
//				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".changeToken",               "setupToken",             KeyStroke.getKeyStroke(KeyEvent.VK_F2,     NONE)));
				menu.add(registerDynMenuItem(MENU_TOKEN,    langKey + ".deleteToken",               "deleteToken",            KeyStroke.getKeyStroke(KeyEvent.VK_F2,     SHIFT)));
				break;

			case "help":
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".about",                     "about"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".privacy",                   "privacy"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".manual",                    "faq",                    KeyStroke.getKeyStroke(KeyEvent.VK_F1,     NONE)));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".support",                   "support",                KeyStroke.getKeyStroke(KeyEvent.VK_F1,     SHIFT)));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".updateCheck",               "setupUpdateCheck"));
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

			case "debug":
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".changeAPI",                 "changeAPI"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".useHTTPS",                  "useHTTPS"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".useHTTP",                   "useHTTP"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".enableVerification",        "enableVerification"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".disableVerification",       "disableVerification"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".getCertChain",              "displayCertificateChain"));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".enableIPv6",                "enableIPv6"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".disableIPv6",               "disableIPv6"));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".resetTranslation",          "resetTranslationVersion"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".decrementTranslation",      "decrementTranslationVersion"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".resetServerMessage",        "resetServerMessageVersion"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".resetUpdateCheck",          "resetUpdateCheckTimers"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".resetTrackList",            "resetTrackListTimer"));
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".dumpLastVars",              "dumpLastVariables"));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
				menu.add(registerDynMenuItem(MENU_STATIC,   langKey + ".quickDebug",                "quickDebugWrapper"));
				menu.addSeparator(); // ----------------------------------------------------------------------------------------------------------------------------------------------------------------
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
			menuitems = new HashMap<>();
		}

		if(menuitems.get(o) == null)
		{
			menuitems.put(o, new ArrayList<>());
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
		catch(Throwable e)
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
		if(mainTable != null && mainTable.getSelectedRows().length > 0)
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
		mainModel.setRowCount(0);
	}

	private static void hideTableHeader()
	{
		// Das ist ein sehr schmutziger Hack...
		mainTable.getTableHeader().setUI(null);
	}

	private static void showTableHeader()
	{
		// Und das ist eine noch viel unschönere Lösung...
		mainTable.getTableHeader().setUI(new BasicTableHeaderUI());
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

		int row = mainModel.getRowCount();
		highlightRows(row - num, row - 1);
	}

	private static void highlightRows(int start, int end)
	{
		scrolltoRow(end);
		mainTable.clearSelection();
		mainTable.addRowSelectionInterval(start, end);
	}

	private static void highlightRows(int[] rows)
	{
		if(rows.length > 0)
		{
			scrolltoRow(rows[rows.length - 1]);
		}

		mainTable.clearSelection();
		for(int i = 0; i < rows.length; i++)
		{
			mainTable.addRowSelectionInterval(rows[i], rows[i]);
		}
	}

	private static void scrolltoRow(int row)
	{
		mainTable.scrollRectToVisible(mainTable.getCellRect(row, 1, true));
	}

	public static void selectAll()
	{
		if(mainTable != null)
		{
			mainTable.selectAll();
		}
	}

	public static void clearSelection()
	{
		if(mainTable != null)
		{
			mainTable.clearSelection();
		}
	}

	public static void invertSelection()
	{
		if(mainTable != null)
		{
			int[] selection = mainTable.getSelectedRows();
			mainTable.selectAll();

			for(int i = 0; i < selection.length; i++)
			{
				mainTable.removeRowSelectionInterval(selection[i], selection[i]);
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
		ArrayList<Integer> selection = new ArrayList<>();
		String[] importedGhosts = new String[ghosts.length];
		boolean deleteDuplicates = !isSpecialProfile();
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

			ghostloop:
			for(int i = 0; i < ghosts.length; i++)
			{
				ghosts[i].printDetails();
				String ghostHash = ghosts[i].getSimpleHash();
				importedGhosts[i] = ghostHash;

				if(!deleteDuplicates)
				{
					for(int h = 0; h < OfflineProfiles.getGhostCount(); h++)
					{
						if(OfflineProfiles.getGhost(h).getSimpleHash().equals(ghostHash))
						{
							FNX.dbgf("Not importing ghost #%d, because it already exists at destination as item #%d. Skipping!", i, h);
							continue ghostloop;
						}
					}
				}

				addGhost(ghosts[i], true);

				if(deleteDuplicates)
				{
					int[] ghostDel = OfflineProfiles.getGhostsByCondition(ghosts[i]);
					for(int h = ghostDel.length - 2; h > -1; h--)
					{
						deleteGhost(ghostDel[h]);
					}
				}
			}

			for(int i = 0; i < importedGhosts.length; i++)
			{
				if(importedGhosts[i] == null)
				{
					continue;
				}

				for(int h = 0; h < OfflineProfiles.getGhostCount(); h++)
				{
					if(OfflineProfiles.getGhost(h).getSimpleHash().equals(importedGhosts[i]))
					{
						selection.add(h);
					}
				}
			}

			if(selection.size() > 0)
			{
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
		catch(Throwable e)
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

		if(FNX.isEDT())
		{
			syncGUI();
		}
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
		catch(Throwable e)
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
		catch(Throwable e)
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
		catch(Throwable e)
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
		catch(Throwable e)
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
		catch(Throwable e)
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
		catch(Throwable e)
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
			catch(Throwable e)
			{
				exceptionHandler(e);
				return;
			}
		}

		Object tmp[] = {ghost.getNickname(), ghost.getGameModeName(), ghost.getTrackName(), ghost.getWeatherName(), gmHelper.formatSki(ghost.getSki()), ghost.getResult()};
		mainModel.addRow(tmp);
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
			mainModel.removeRow(index);
			updateWindowTitle();
		}
		catch(Throwable e)
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
		catch(Throwable e)
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

		try
		{
			int[][][][] results;
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

			OfflineProfiles tmp;

			if(!FNX.isEDT())
			{
				try
				{
					FNX.dbg("Cloning OfflineProfiles object...");
					tmp = new OfflineProfiles(file);
					tmp.selectProfile(profile);
				}
				catch(Throwable e)
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

			ArrayList<ArrayList<Object>> ghosts = new ArrayList<>();

			for(int m = 0; m < modes.length; m++)
			{
				for(int t = 0; t < tracks.length; t++)
				{
					for(int w = 0; w < weathers.length; w++)
					{
						if((oldProfileGhosts[m][t][w] == null && newProfileGhosts[m][t][w] != null) || (oldProfileGhosts[m][t][w] != null && newProfileGhosts[m][t][w] != null && (oldProfileGhosts[m][t][w].getTime() != newProfileGhosts[m][t][w].getTime() || !newProfileGhosts[m][t][w].getHash().equals(oldProfileGhosts[m][t][w].getHash()))))
						{
							FNX.dbgf("Changed result: %s / %s / %s", gmHelper.getGameModeName(modes[m]), gmHelper.getTrack(tracks[t]), gmHelper.getWeatherName(weathers[w]));

							ArrayList<Object> item = new ArrayList<>(4);
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

								ArrayList<Object> item = new ArrayList<>(4);
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
				// TODO: Wir brauchen einen Uploadcache in der API-Klasse!
				// Andernfalls würden wir beim EDT alles nochmals hochladen.
				// Nicht vergessen, dass dieser Cache immer geleert gehört.
				// ...

				if(ffForce)
				{
					for(int i = 0; i < ghosts.size(); i++)
					{
						ArrayList<Object> item = ghosts.get(i);
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
						ArrayList<Object> item = ghosts.get(i);
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
		}
		finally
		{
			if(FNX.isEDT())
			{
				try
				{
					// Das ursprüngliche Profil aktivieren!
					FNX.dbgf("Restoring profile %d...", profile);
					OfflineProfiles.selectProfile(profile);
				}
				catch(Throwable e)
				{
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
				Boolean result = ghostSelect(g, t, r, ((r < -1) ? true : false));

				if(result != null && result == true)
				{
					if(OfflineProfiles.changed() && !saveFile(true))
					{
						throw new Exception("Could not save file");
					}
				}
			}
		}
		catch(Throwable e)
		{
			exceptionHandler(e);
		}

		return true;
	}

	public static void fastFollow(boolean force)
	{
		if(OfflineProfiles == null || checkProfile(true) || unsavedChanges() || onlineMode())
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
		catch(Throwable e)
		{
			exceptionHandler(e);
		}
	}

	public static Integer fastFollowEvaluation() throws Exception
	{
		if(fastFollowMode())
		{
			return 1;
		}

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
			return;
		}
		else
		{
			ffStarted = 1;
		}

		ffForce = force;
		appliedCount = 0;
		uploadedCount = 0;
		ffDownload = false;
		ffModification = -1;
		lastApplicationPosition = 0;
		lastApplicationGhost = null;
		lastApplicationDestinations = new HashMap<>();

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
			catch(Throwable e)
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
				catch(Throwable e)
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

	public static void fastFollowStatus(int time)
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

				ffBody.setMessage(FNX.nl2br(String.format("<html><body>%s</body></html>",
					FNX.formatLangString(lang, "fastFollowModeBody", FNX.getLangString(lang, "fastFollowModeExplain" + ((ffForce ? "Force" : ""))), FNX.escapeHTML(OfflineProfiles.getProfiles()[profile])) +
					FNX.formatLangString(lang, "fastFollowMode" + ((ffModification > 0) ? "Extended" : "Empty"), FNX.escapeHTML(ldt.format(dtf)), uploadedCount, appliedCount) +
					getResultDestinations()
				)));

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

			int i = 0;
			int pts = 0;
			int[] r = new int[lastApplicationDestinations.size()];

			for(Map<String,Object> v : lastApplicationDestinations.values())
			{
				// Wenn sich die Spielmodus/Strecke/Wetter Kombination geändert hat, werden bisherige Statuseinträge ausgeblendet und gelöscht. Aber nur, wenn sie schon angezeigt wurden.
				if(lastApplicationGhost != null && v.containsKey("__seen") && (lastApplicationGhost.getGameMode() != (int) v.get("GameMode") || !lastApplicationGhost.getTrack().equalsIgnoreCase((String) v.get("Track")) || lastApplicationGhost.getWeather() != (int) v.get("Weather"))
				)
				{
					r[i++] = (int) v.get("TrackID");
					continue;
				}

				String suffix = " (%s)";
				if((boolean) v.get("PT"))
				{
					if(pts++ > FF_PT_LIMIT)
					{
						FNX.dbgf("Too many PT destinations! Skipping PT from group %s...", (String) v.get("GroupName"));

						continue;
					}

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
					FNX.escapeHTML(gmHelper.getGameModeName((int) v.get("GameMode"))),
					FNX.escapeHTML(gmHelper.getTrack((String) v.get("Track"))),
					FNX.escapeHTML(gmHelper.getWeatherName((int) v.get("Weather"))),
					FNX.escapeHTML(gmHelper.getResult((int) v.get("OldResult"))),
					(int) v.get("OldPosition"),
					FNX.escapeHTML(gmHelper.getResult((int) v.get("NewResult"))),
					(int) v.get("NewPosition"),
					FNX.escapeHTML(suffix)
				));

				v.put("__seen", true);
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
			catch(Throwable e)
			{
				exceptionHandler(e);
			}
		}

		if(oFFM != null || aFFM != null)
		{
			FNX.dbg("Stopping FFM now!");

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
			catch(Throwable e)
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
			resetHistory();

			profiles.reload();
			OfflineProfiles.addProfile(nick);
			profiles.addProfile(nick);

			profiles.saveProfiles();
			saveFile(true);

			profile = 0;
			reloadFile();
		}
		catch(Throwable e)
		{
			exceptionHandler(e);
			reloadFile();
			return;
		}
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
			resetHistory();

			profiles.reload();
			profiles.renameProfile(nickname, nick);
			OfflineProfiles.renameProfile(nick);

			profiles.saveProfiles();
			saveFile(true);

			profile = 0;
			reloadFile();
		}
		catch(Throwable e)
		{
			exceptionHandler(e);
			reloadFile();
			return;
		}
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

			resetHistory();

			profiles.reload();
			profiles.deleteProfile(nickname);
			OfflineProfiles.deleteProfile(profile);

			profiles.saveProfiles();
			saveFile(true);

			profile = 0;
			reloadFile();
		}
		catch(Throwable e)
		{
			exceptionHandler(e);
			reloadFile();
			return;
		}
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
			catch(Throwable e)
			{
				e.printStackTrace();
			}

			if(hasOnlineToken())
			{
				FNX.dbg("ONLINE mode is active.");
				return true;
			}
		}

		FNX.dbg("MultiGhost setting is disabled.");
		FNX.dbg("TrainingGhostNick selection is empty.");
		FNX.dbg("OnlineToken not set or empty.");

		return false;
	}

	private static boolean hasOnlineToken()
	{
		try
		{
			Profiles profiles = getProfileHandle(null);

			if(profiles != null && profiles.hasOnlineToken(nickname))
			{
				return true;
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

		return false;
	}

	private static boolean onlineMode()
	{
		return (hasOnlineToken() && !confirmDialog(FNX.formatLangString(lang, "onlineModeConfirmation")));
	}

	public static void resort()
	{
		if(OfflineProfiles != null)
		{
			if(isSpecialProfile())
			{
				ArrayList<ArrayList<ArrayList<ArrayList<GhostElement>>>> ghosts = OfflineProfiles.getGhostList();

				for(int i = (OfflineProfiles.getGhostCount() - 1); i > -1; i--)
				{
					deleteGhost(i);
				}

				for(int m = 0; m < ghosts.size(); m++)
				{
					for(int t = 0; t < ghosts.get(m).size(); t++)
					{
						for(int w = 0; w < ghosts.get(m).get(t).size(); w++)
						{
							ArrayList<GhostElement> item = ghosts.get(m).get(t).get(w);
							item.sort(Comparator.comparing(GhostElement::getTime));

							for(int i = 0; i < item.size(); i++)
							{
								addGhost(item.get(i), true);
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

		if(auto && !updateCheckConsent())
		{
			FNX.dbg("No consent for auto update check.");

			return;
		}
		else if(dll == null || !dll.exists() || !dll.isFile())
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
			setupAnonymousAPI();

			try
			{
				// TODO: Check for NULL?
				String hash = FNX.sha512(dll);
				FNX.dbgf("SHA512: %s", hash);

				if(anonAPI.updateAvailable("SC.DLL", hash, auto, FNX.intval(cfg(CFG_SMSG))))
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

				handleServerMessage(anonAPI);
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
			catch(Throwable e)
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

	private static Object stepDialog(String title, Object message, String[] selectionValues, String initialSelectionValue)
	{
		return stepDialog(JOptionPane.PLAIN_MESSAGE, title, message, selectionValues, initialSelectionValue, false);
	}

	private static Object stepDialog(int type, String title, Object message, String[] selectionValues, String initialSelectionValue)
	{
		return stepDialog(type, title, message, selectionValues, initialSelectionValue, false);
	}

	private static Object stepDialog(String title, Object message, String[] selectionValues, String initialSelectionValue, boolean prev)
	{
		return stepDialog(JOptionPane.PLAIN_MESSAGE, title, message, selectionValues, initialSelectionValue, prev);
	}

	private static Object stepDialog(int type, String title, Object message, String[] selectionValues, String initialSelectionValue, boolean prev)
	{
		FNX.windowToFront(mainWindow);

		String[] buttons;
		Integer[] values;
		Object defaultButton;
		String dialogType;

		JComboBox<String> comboBox = null;
		Integer[] returnValues = null;
		JTextField textField = null;

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0, 1));
		panel.add(new JLabel((String) message));

		if(selectionValues != null)
		{
			ArrayList<String> items = new ArrayList<>();
			returnValues = new Integer[selectionValues.length];

			for(int i = 0, h = 0; i < selectionValues.length; i++)
			{
				if(selectionValues[i] != null)
				{
					items.add(selectionValues[i]);
					returnValues[h++] = i;
				}
			}

			comboBox = new JComboBox<>(items.toArray(new String[items.size()]));

			if(initialSelectionValue != null)
			{
				comboBox.setSelectedItem(initialSelectionValue);
			}

			panel.add(comboBox);
			dialogType = "SELECTION";
		}
		else
		{
			textField = new JTextField(initialSelectionValue != null ? initialSelectionValue : null);

			panel.add(textField);
			dialogType = "INPUTFIELD";
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

	private static void displayTextArea(String msg)
	{
		displayTextArea(msg, null);
	}

	private static void displayTextArea(String msg, String title)
	{
		JTextArea textarea = new JTextArea(msg, TEXTAREA_SIZE[0], TEXTAREA_SIZE[1]);
		textarea.setEditable(false); textarea.setWrapStyleWord(true); textarea.setLineWrap(true);
		JScrollPane jScrollPane = new JScrollPane(textarea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		JOptionPane.showMessageDialog(mainWindow, jScrollPane, title, JOptionPane.PLAIN_MESSAGE);
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
		int[] selection = mainTable.getSelectedRows();

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
		catch(Throwable e)
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

		// Das ist nur eine schnelle Notlösung...
		// Langfristig soll #82 implementiert werden.
		if(e.getErrorCode().startsWith("INTERNAL_") && e.getCause() != null)
		{
			msg = String.format("<html><body><p>%2$s</p><br /><p style=\"width: %1$.0fpx; background-color: #888888; border: 1px solid #cccccc; color: #ffffff; padding: 5px;\">%3$s</p></body></html>", WINDOW_SIZE_MIN.getWidth() / 2, FNX.escapeHTML(msg), FNX.escapeHTML(e.getCause().toString()));
		}

		if(e.getCalming())
		{
			infoDialog(APPLICATION_API, msg);
		}
		else
		{
			errorMessage(APPLICATION_API, msg);
		}
	}

	// Zustimmung für automatische Updateprüfung erteilt?
	public static boolean updateCheckConsent()
	{
		return (cfg.getInt(CFG_UC_CONSENT, 0) > 0);
	}

	// Zustimmung für automatische Updateprüfung einholen.
	protected static void requestUpdateCheckConsent()
	{
		if(cfg.getInt(CFG_UC_CONSENT, 0) == 0)
		{
			setupUpdateCheck();
		}
	}

	// Zustimmung für automatische Updateprüfung ändern.
	protected static void setupUpdateCheck()
	{
		int value = 0;

		if(confirmDialog(null, FNX.formatLangString(lang, "consentUpdateCheck", UPDATE_INTERVAL / 3600000L)))
		{
			value = 1;
		}
		else
		{
			value = -1;
		}

		cfg.putInt(CFG_UC_CONSENT, value);
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

		if(auto && !updateCheckConsent())
		{
			FNX.dbg("No consent for auto update check.");

			return;
		}
		else if(APPLICATION_VERSION.toUpperCase().startsWith("GIT-"))
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
			setupAnonymousAPI();

			try
			{
				if(anonAPI.updateAvailable(APPLICATION_NAME, APPLICATION_VERSION, auto, FNX.intval(cfg(CFG_SMSG))))
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

				handleServerMessage(anonAPI);
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
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void copyTokenToProfile()
	{
		if(OfflineProfiles == null || checkProfile() || onlineMode() || !prepareAPI())
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
		catch(Throwable e)
		{
			exceptionHandler(e);
		}

		updateWindowTitle();
		updateMenuItems();
		autoSave();
	}

	public static void copyTokenFromProfile()
	{
		if(OfflineProfiles == null || checkProfile() || onlineMode())
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
		catch(Throwable e)
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
		catch(Throwable e)
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

	private static void setupAnonymousAPI()
	{
		if(anonAPI == null)
		{
			// Der Token wird absichtlich nicht mitgesendet!
			anonAPI = new eSportsAPI(null, getIdent());
		}
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

		int[] selection = mainTable.getSelectedRows();
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

							ArrayList<HashMap<String,Object>> lastResultDestinations = api.getLastResultDestinations();
							if(lastResultDestinations != null && lastResultDestinations.size() > 0)
							{
								if(ffStarted == 0)
								{
									FNX.dbg("Initializing lastApplicationDestinations.");
									lastApplicationDestinations = new HashMap<>();
								}

								for(int h = 0; h < lastResultDestinations.size(); h++)
								{
									HashMap<String,Object> item = lastResultDestinations.get(h);

									if(!(boolean) item.get("Applied"))
									{
										FNX.dbgf("Ignoring not applied ghost from track %d...", (int) item.get("TrackID"));
										continue;
									}

									lastApplicationDestinations.put((int) item.get("TrackID"), item);
								}
							}

							lastApplicationGhost = ghost;
							appliedCount++;

							if(!silent)
							{
								infoDialog(APPLICATION_API, String.format("<html><body>%s</body></html>", FNX.nl2br(FNX.formatLangString(lang, "ghostApplySuccess", ghostID) + getResultDestinations())));
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
					ids = new ArrayList<>(0);
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
				lastMode = cfg(CFG_MODE, String.valueOf(modes[input.intValue()]));
				result = ghostSelect(modes[input.intValue()]);

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

					result = ghostSelect(mode, lastTrack, Integer.parseInt(lastWeather), ENABLE_RACE);

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
		catch(Throwable e)
		{
			exceptionHandler(e);
		}

		return false;
	}

	// Auswahl eines Geists aus der Rangliste zum Herunterladen.
	// Vorher muss bereits nach Strecke/Wetter gefragt worden sein!
	private static Boolean ghostSelect(int mode, String track, int weather, boolean forceWeather)
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

				messageDialog(APPLICATION_API, String.format("<html><body>%s</body></html>", FNX.nl2br(FNX.formatLangString(lang, "playerInfo", FNX.escapeHTML((String) data.get("Useraccount")), FNX.escapeHTML((String) data.get("Nickname")), FNX.escapeHTML((String) data.get("CompetitionName"))))));
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
		catch(Throwable e)
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

	public static void handleServerMessage(eSportsAPI a)
	{
		int version = a.getLastServerMessageVersion();
		String text = a.getLastServerMessageText();

		if(text != null && text.length() > 0)
		{
			FNX.dbg("Server message received! Displaying it now...");
			displayTextArea(text, FNX.formatLangString(lang, "serverMessageTitle", ((version > -1) ? version : 0)));
		}

		if(version > -1)
		{
			cfg(CFG_SMSG, Integer.toString(a.getLastServerMessageVersion()));
		}

		a.resetLastServerMessage();
	}

/***********************************************************************
 *                            DEBUG STUFF                              *
 ***********************************************************************/

	public static void resetServerMessageVersion()
	{
		removeConfig(CFG_SMSG);
	}

	public static void resetTranslationVersion()
	{
		removeConfig(CFG_TRANSLATION);
		infoDialog(FNX.formatLangString(lang, "debugRestart"));
	}

	public static void decrementTranslationVersion()
	{
		cfg(CFG_TRANSLATION, String.valueOf(TRANSLATION_VERSION - 1));
		infoDialog(FNX.formatLangString(lang, "debugRestart"));
	}

	public static void resetUpdateCheckTimers()
	{
		removeConfig(CFG_DC);
		removeConfig(CFG_UC);
	}

	public static void resetTrackListTimer()
	{
		removeConfig(CFG_WC);
	}

	public static void dumpLastVariables()
	{
		StringBuilder t = new StringBuilder();
		String[] a = {CFG_CWD, CFG_CWDPORT, null, CFG_RPROFILE, CFG_PROFILE, null, CFG_MODE, CFG_TRACK, CFG_WEATHER};

		for(String e : a)
		{
			if(e == null)
			{
				t.append(String.format("%n"));
			}
			else
			{
				t.append(String.format("%s = %s%n", e, cfg(e)));
			}
		}

		displayTextArea(t.toString().trim(), FNX.getLangString(lang, "menu.debug.dumpLastVars"));
	}

	public static void enableIPv6()
	{
		removeConfig(CFG_IPV4);
		infoDialog(FNX.formatLangString(lang, "debugRestart"));
	}

	public static void disableIPv6()
	{
		cfg(CFG_IPV4, "true");
		infoDialog(FNX.formatLangString(lang, "debugRestart"));
	}

	public static void enableVerification()
	{
		removeConfig(CFG_API_VERIFY);
		eSportsAPI.enableVerification();
	}

	public static void disableVerification()
	{
		cfg(CFG_API_VERIFY, "true");
		eSportsAPI.disableVerification();
	}

	public static void useHTTP()
	{
		changeProtocol("http");
	}

	public static void useHTTPS()
	{
		changeProtocol("https");
	}

	private static void changeProtocol(String protocol)
	{
		if(eSportsAPI.getDefaultProtocol().equalsIgnoreCase(protocol))
		{
			removeConfig(CFG_API_PROTO);
			protocol = null;
		}
		else
		{
			cfg(CFG_API_PROTO, protocol);
		}

		try
		{
			eSportsAPI.setProtocol(protocol);
		}
		catch(IllegalArgumentException e)
		{
			exceptionHandler(e);
			removeConfig(CFG_API_PROTO);
		}
	}

	public static void changeAPI()
	{
		String fqdn = cfg(CFG_API_HOST);

		while(true)
		{
			if((fqdn = (String) inputDialog(FNX.getLangString(lang, "menu.debug.changeAPI"), FNX.formatLangString(lang, "debugChangeAPI"), fqdn)) == null || fqdn.length() <= 0 || eSportsAPI.getDefaultHost().equalsIgnoreCase(fqdn))
			{
				removeConfig(CFG_API_HOST);
				fqdn = null;
			}
			else
			{
				cfg(CFG_API_HOST, fqdn);
			}

			try
			{
				eSportsAPI.setHost(fqdn);
				return;
			}
			catch(IllegalArgumentException e)
			{
				exceptionHandler(e);
				removeConfig(CFG_API_HOST);
			}
		}
	}

	public static void displayCertificateChain()
	{
		if(api == null)
		{
			return;
		}

		Certificate[] chain = api.getCertificateChain();

		if(chain != null && chain.length > 0)
		{
			StringBuilder s = new StringBuilder();
			for(int i = 0; i < chain.length; i++)
			{
				s.append(String.format("----- CERTIFICATE #%d -----%n%n%s%n%n", i, chain[i].toString()));
			}

			displayTextArea(s.toString().trim(), FNX.getLangString(lang, "menu.debug.getCertChain"));
		}
	}

	public static void quickDebugWrapper()
	{
		FNX.dbg("Refused to debug without 0xCOFFEE! ;-)");
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
		catch(Throwable e)
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
		catch(Throwable e)
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

		if(FNX.isEDT())
		{
			syncGUI();
		}
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

			if(!saveFile(OfflineProfiles.toString(), force))
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

	private static boolean saveFile(String xml, boolean force)
	{
		try
		{
			if(!force && !compareFileHash())
			{
				FNX.dbg("Disk on file has changed. Asking the user for confirmation...");

				Integer[] values = new Integer[]{BUTTON_NO, BUTTON_YES};
				String[] buttons = FNX.getLangStrings(lang, new String[]{"fileChangedConfirmationReload", "fileChangedConfirmationOverwrite"});
				Object defaultButton = buttons[1];

				while(true)
				{
					int result = JOptionPane.showOptionDialog(mainWindow, FNX.formatLangString(lang, "fileChangedConfirmation"), null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, buttons, defaultButton);

					if(result == JOptionPane.CLOSED_OPTION)
					{
						FNX.dbg("Confirmation closed...");
						continue;
					}
					else if(values[result] == BUTTON_NO)
					{
						FNX.dbg("Aborted on user request! Reloading file...");
						reloadFile(true);
						return false;
					}
					else
					{
						FNX.dbg("Overwriting disk on file...");
						break;
					}
				}
			}

			PrintWriter tmp = new PrintWriter(file);
			tmp.printf("%s", xml);
			tmp.close();

			return true;
		}
		catch(Throwable e)
		{
			exceptionHandler(e);
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
			catch(Throwable e)
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
			int[] selection = mainTable.getSelectedRows();
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
					data.insert(0, String.format("\t<!-- %s @ %s (%s / %s): %s (%s) -->\r\n\t%s\r\n", FNX.escapeHTML(ghost.getNickname()), ghost.getTrackName(), ghost.getGameModeName(), ghost.getWeatherName(), ghost.getResult(), gmHelper.formatSki(ghost.getSki(), true), ghost.toString()));
				}

				data.insert(0, "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<GhostList>\r\n\r\n");
				data.append(String.format("</GhostList>\r\n<!-- %s -->\r\n", FNX.getDateString()));

				PrintWriter pw = new PrintWriter(selectedFile);
				pw.printf("%s", data.toString()); pw.close();

				FNX.dbg("Export to file successfully!");
				infoDialog(FNX.formatLangString(lang, "exportToFileSuccess", selectedFile));

				return true;
			}
			catch(Throwable e)
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
			catch(Throwable e)
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
				if(!saveFile(history[newIndex], false))
				{
					return false;
				}

				OfflineProfiles.reload();
				selectProfile(profile);
				syncGUI();

				historyIndex = newIndex;
				updateHistoryMenuItems();

				dumpHistory();
				return true;
			}
			catch(Throwable e)
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

	private static boolean compareFileHash()
	{
		if(OfflineProfiles != null && file != null)
		{
			try
			{
				String newHash = (new OfflineProfiles(file)).getInitialGhostsHash();
				String oldHash = OfflineProfiles.getInitialGhostsHash();
				FNX.dbgf("oldHash=%s; newHash=%s", oldHash, newHash);

				if(oldHash.equals(newHash))
				{
					return true;
				}
			}
			catch(Throwable e)
			{
				exceptionHandler(e);
				return false;
			}
		}

		return false;
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
		catch(Throwable e)
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
		if(OfflineProfiles != null && mainTable != null)
		{
			String title = FNX.getLangString(lang, (move ? "move" : "copy") + "ToProfile");
			String message = "";

			boolean warnSRC = false;
			boolean warnDST = false;
			String ghostHash = null;

			try
			{
				int[] selection = mainTable.getSelectedRows();

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

				ghostloop:
				for(int i = 0; i < ghosts.length; i++)
				{
					if(warnDST || isSpecialProfile(selected))
					{
						if(!warnDST)
						{
							ghostHash = ghosts[i].getSimpleHash();
						}

						for(int h = (OfflineProfiles.getGhostCount() - 1); h > -1; h--)
						{
							GhostElement ghost = OfflineProfiles.getGhost(h);

							if(warnDST)
							{
								if(ghost.getGameMode() == ghosts[i].getGameMode() && ghost.getTrack().equals(ghosts[i].getTrack()) && ghost.getWeather() == ghosts[i].getWeather())
								{
									OfflineProfiles.deleteGhost(h);
								}
							}
							else
							{
								if(ghost.getSimpleHash().equals(ghostHash))
								{
									FNX.dbgf("Selected ghost #%d already exists at destination as item #%d. Skipping!", i, h);
									continue ghostloop;
								}
							}
						}
					}

					OfflineProfiles.addGhost(ghosts[i]);
				}

				FNX.dbgf("Using old profile %d...", profile);
				OfflineProfiles.selectProfile(profile);

				if(move)
				{
					for(int i = selection.length - 1; i > -1; i--)
					{
						deleteGhost(selection[i]);
					}
				}
			}
			catch(Throwable e)
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
				catch(Throwable e)
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

// TODO: Echte Sortierung der Tabelle ermöglichen? Dafür bräuchten wir
// aber die einzelnen Geister irgendwo versteckt in der Tabelle, oder?
// https://docs.oracle.com/javase/tutorial/uiswing/components/table.html#sorting
// ...

// TODO: Menüfunktion (Hilfe), um den Pfad der aktuellen Datei in die Zwischenablage zu kopieren?
// ...

// Ich glaube, der FFM hat ein kleines Problem.
// Bei jedem Start explodiert der Ram-Verbrauch.
// Egal, ob mit oder ohne ENABLE_BLURRY...
// System.gc() bringt auch nichts.
// ...

