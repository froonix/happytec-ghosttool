/**
 * FNX.java: Static helper methods for various stuff
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
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

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

	public static void displayExceptionSummary(Exception e, String title, String header, String footer)
	{
		e.printStackTrace();

		JOptionPane.showMessageDialog(null, String.format("%s%n%n%s: %s%n%n%s", ((header != null) ? header : ""), e.getClass().getCanonicalName(), e.getMessage(), ((footer != null) ? footer : "")).trim(), title, JOptionPane.ERROR_MESSAGE);
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

	public static void actionCallback(String className, String methodName)
	{
		try
		{
			Class<?> c = Class.forName(className);
			Method m = c.getDeclaredMethod(methodName);
			m.invoke(null);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
