import java.io.*;
import java.util.*;
import java.net.URLEncoder;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

import java.io.UnsupportedEncodingException;

abstract class FNX
{
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
}
