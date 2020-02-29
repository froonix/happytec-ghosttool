/**
 * FNX.java: Static helper methods for various stuff
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

import java.awt.Toolkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import java.lang.reflect.Method;

import java.net.URLEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class FNX
{
	private static DateFormat             dateFormat;

	private static DocumentBuilderFactory dbFactory;
	private static DocumentBuilder        dBuilder;

	private static boolean                debugMode;
	private static DateFormat             debugDate;

	public static String getDateString()
	{
		if(dateFormat == null)
		{
			// http://stackoverflow.com/a/3914498
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		return dateFormat.format(new Date());
	}

	// return string length of int
	public static int strlen(int i)
	{
		return Integer.toString(i).length();
	}

	// return signed int from string
	public static int intval(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch(NumberFormatException e)
		{
			return 0;
		}
	}

	// return (un)signed int from string
	public static int intval(String s, boolean unsigned)
	{
		int i = intval(s);

		if(unsigned)
		{
			i = Math.abs(i);
		}

		return i;
	}

	public static String urlencode(String s)
	{
		try
		{
			return URLEncoder.encode(s, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			throw new UnsupportedOperationException(e);
		}
	}

	public static String buildQueryString(Map<?,?> map)
	{
		StringBuilder qs = new StringBuilder();
		for(Map.Entry<?,?> entry : map.entrySet())
		{
			if(qs.length() > 0)
			{
				qs.append("&");
			}

			qs.append(String.format("%s=%s", urlencode(entry.getKey().toString()), urlencode(entry.getValue().toString())));
		}

		return qs.toString();
	}

	private static void setupDOMParser() throws SAXException, ParserConfigurationException, IOException
	{
		if(dbFactory == null)
		{
			dbFactory = DocumentBuilderFactory.newInstance();
		}

		if(dBuilder == null)
		{
			dBuilder = dbFactory.newDocumentBuilder();
		}
	}

	public static Document getDOMDocument(String xml) throws SAXException, ParserConfigurationException, IOException
	{
		setupDOMParser();
		return dBuilder.parse(new InputSource(new StringReader(xml)));
	}

	public static Document getDOMDocument(File file) throws SAXException, ParserConfigurationException, IOException
	{
		setupDOMParser();
		return dBuilder.parse(file);
	}

	public static String getStringFromDOM(Document input, boolean full) throws TransformerException
	{
		return getStringFromDOM(new DOMSource(input), new StreamResult(new StringWriter()), full);
	}

	public static String getStringFromDOM(Element input, boolean full) throws TransformerException
	{
		return getStringFromDOM(new DOMSource(input), new StreamResult(new StringWriter()), full);
	}

	public static String getStringFromDOM(DOMSource input, StreamResult output, boolean full) throws TransformerException
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();

		if(full)
		{
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		}
		else
		{
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		}

		t.setOutputProperty(OutputKeys.METHOD, "xml");
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		// t.setOutputProperty(OutputKeys.STANDALONE, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

		t.transform(input, output);

		return output.getWriter().toString();
	}

	public static String getWinNL(String string)
	{
		if(!System.lineSeparator().equals("\r\n"))
		{
			string = string.replace(System.lineSeparator(), "\r\n");
		}

		return string;
	}

	public static void displayExceptionSummary(Exception e, String title, String header, String footer)
	{
		e.printStackTrace();

		JOptionPane.showMessageDialog(null, String.format("%s%n%n%s: %s%n%n%s", ((header != null) ? header : ""), e.getClass().getCanonicalName(), e.getMessage(), ((footer != null) ? footer : "")).trim(), title, JOptionPane.ERROR_MESSAGE);
	}

	/*
	// http://stackoverflow.com/a/14011536
	public static void displayExceptionDetails(Exception e, String title)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);

		e.printStackTrace(pw);
		String st = sw.getBuffer().toString();
		System.err.println(st);

		javax.swing.JTextArea jta = new javax.swing.JTextArea(st);
		JScrollPane jsp = new JScrollPane(jta)
		{
			@Override
			public java.awt.Dimension getPreferredSize()
			{
				return new java.awt.Dimension(480, 320);
			}
		};
		JOptionPane.showMessageDialog(null, jsp, title, JOptionPane.ERROR_MESSAGE);
	}
	*/

	public static String sha512(File file)
	{
		int length;
		byte[] block;
		byte[] bytes;

		InputStream in;
		MessageDigest md;

		StringBuilder sb;
		String hash = null;

		try
		{
			in = new FileInputStream(file);
			md = MessageDigest.getInstance("SHA-512");

			block = new byte[4096];
			while((length = in.read(block)) > 0)
			{
				md.update(block, 0, length);
			}

			bytes = md.digest();
			sb = new StringBuilder();

			for(int i = 0; i < bytes.length; i++)
			{
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			hash = sb.toString();
		}
		catch(NoSuchAlgorithmException|IOException e)
		{
			e.printStackTrace();
			return null;
		}

		return hash;
	}

	public static String sha512(String input)
	{
		try
		{
			return sha512(input.getBytes("ISO-8859-1"));
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public static String sha512(byte[] input)
	{
		byte[] bytes;
		StringBuilder sb;
		String hash = null;

		try
		{
			bytes = MessageDigest.getInstance("SHA-512").digest(input);
			sb = new StringBuilder();

			for(int i = 0; i < bytes.length; i++)
			{
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			hash = sb.toString();
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		}

		return hash;
	}

	public static void windowToFront(JFrame window)
	{
		if(window == null)
		{
			return;
		}

		if(window.isAlwaysOnTopSupported())
		{
			// http://stackoverflow.com/a/18015090
			boolean aot = window.isAlwaysOnTop();
			window.setAlwaysOnTop(true);
			window.setAlwaysOnTop(aot);
		}
		else
		{
			// fallback solution
			window.toFront();
		}
	}

	public static String getCleanXML(Document doc)
	{
		String XML;

		try
		{
			doc.getDocumentElement().normalize();
			XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//text()[normalize-space(.) = '']");
			NodeList blankTextNodes = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);

			for(int i = 0; i < blankTextNodes.getLength(); i++)
			{
				blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
			}

			return FNX.getStringFromDOM(doc, true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static JEditorPane getHTMLPane(String html)
	{
		JLabel label = new JLabel();
		JEditorPane e = new JEditorPane("text/html", html);
		e.addHyperlinkListener(new FNX_HyperlinkListener());
		e.setBackground(label.getBackground());
		e.setEditable(false);

		return e;
	}

	public static ResourceBundle getLangBundle(String bundle)
	{
		return ResourceBundle.getBundle(String.format("RealLangBundle_%s", bundle));
	}

	/*
	public static String formatLangString(ResourceBundle lang, String key)
	{
		return String.format(getLangString(lang, key));
	}
	*/

	public static String formatLangString(ResourceBundle lang, String key, Object... args)
	{
		return String.format(getLangString(lang, key), args);
	}

	public static String getLangString(ResourceBundle lang, String key)
	{
		return lang.getString(key);
	}

	public static String[] getLangStrings(ResourceBundle lang, String[] keys)
	{
		String[] results = new String[keys.length];

		for(int i = 0; i < keys.length; i++)
		{
			results[i] = getLangString(lang, keys[i]);
		}

		return results;
	}

	public static boolean checkLangStringExists(ResourceBundle lang, String key)
	{
		return lang.containsKey(key);
	}

	// Ruft die Methode einer statischen Klasse ohne Argumente auf.
	public static void actionCallback(String className, String methodName)
	{
		actionCallback(className, methodName, new Object[0]);
	}

	// Ruft die Methode einer statischen Klasse mit beliebigen Argumenten auf.
	// WICHTIG: Der dritte Parameter muss unbedingt NULL sein, damit es klappt!
	public static void actionCallback(String className, String methodName, Object ignore, Object... args)
	{
		if(ignore != null)
		{
			throw new RuntimeException();
		}

		actionCallback(className, methodName, args);
	}

	// Ruft die Methode einer statischen Klasse mit den Argumenten in args[] auf.
	public static void actionCallback(String className, String methodName, Object[] args)
	{
		int l = (args != null) ? args.length : 0;

		dbgf("c=%s m=%s a=%d", className, methodName, l);

		try
		{
			Class<?> methodParams[] = new Class<?>[l];

			for(int i = 0; i < l; i++)
			{
				if(args[i] instanceof Integer)
				{
					methodParams[i] = Integer.class;
				}
				else if(args[i] instanceof Long)
				{
					methodParams[i] = Long.class;
				}
				else if(args[i] instanceof String)
				{
					methodParams[i] = String.class;
				}
				else if(args[i] instanceof Boolean)
				{
					methodParams[i] = Boolean.class;
				}
				// ...
				// ...
				else
				{
					methodParams[i] = args[i].getClass().getComponentType();
				}

				if(methodParams[i] == null)
				{
					throw new RuntimeException();
				}

				//dbgf("methodParams[%d] = %s (%s)", i, args[i]);
			}

			Class<?> c = Class.forName(className);
			Method m = c.getDeclaredMethod(methodName, methodParams);

			m.invoke(null, args);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void enableDebugging()
	{
		setDebugging(true);
	}

	public static void disableDebugging()
	{
		setDebugging(false);
	}

	public static void setDebugging(boolean value)
	{
		debugMode = value;
	}

	public static boolean getDebugging()
	{
		return debugMode;
	}

	private static synchronized void dbg(String msg, int trace)
	{
		if(debugMode)
		{
			if(debugDate == null)
			{
				debugDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZZZZ");
			}

			Long threadID = Thread.currentThread().getId();
			String threadType = (threadID == 1) ? "R" : (isEDT() ? "E" : "W");

			System.err.printf("%2$5d-%3$s [%1$s] %4$s - %5$s%n", debugDate.format(new Date()), threadID, threadType, Thread.currentThread().getStackTrace()[2 + trace].toString(), msg);
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

	public static boolean isEDT()
	{
		return (Thread.currentThread().getId() == 1 || SwingUtilities.isEventDispatchThread());
	}

	public static boolean requireEDT()
	{
		if(!isEDT())
		{
			FNX.dbg("This is not an EDT! (Event Dispatch Thread)", 1);
			//throw new RuntimeException("Not in EDT! Oops...");

			return false;
		}

		return true;
	}

	// Das ist die einzige Lösung, die wirklich funktioniert!
	// Quelle: https://stackoverflow.com/a/3684815/3747688
	public static boolean isValidLocale(String value)
	{
		for(Locale locale : Locale.getAvailableLocales())
		{
			if(value.equals(locale.toString()))
			{
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	public static int getCtrlMask()
	{
		Toolkit dtk = Toolkit.getDefaultToolkit();

		try
		{
			return dtk.getMenuShortcutKeyMaskEx();
		}
		catch(NoSuchMethodError e)
		{
			return dtk.getMenuShortcutKeyMask();
		}
	}
}
