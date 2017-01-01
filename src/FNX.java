import java.io.*;
import java.util.*;
import java.net.URLEncoder;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

import java.io.UnsupportedEncodingException;

abstract class FNX
{
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

	private static void setupDOMParser() throws Exception
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

	public static Document getDOMDocument(String xml) throws Exception
	{
		setupDOMParser();
		return dBuilder.parse(new InputSource(new StringReader(xml)));
	}

	public static Document getDOMDocument(File file) throws Exception
	{
		setupDOMParser();
		return dBuilder.parse(file);
	}

	public static String getStringFromDOM(Document input, boolean full) throws Exception
	{
		return getStringFromDOM(new DOMSource(input), new StreamResult(new StringWriter()), full);
	}

	public static String getStringFromDOM(Element input, boolean full) throws Exception
	{
		return getStringFromDOM(new DOMSource(input), new StreamResult(new StringWriter()), full);
	}

	public static String getStringFromDOM(DOMSource input, StreamResult output, boolean full) throws Exception
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
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

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
}
