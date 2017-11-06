/**
 * Profiles.java: Very limited representation of UserConfig.xml
 * Copyright (C) 2017 Christian Schrötter <cs@fnx.li>
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
import java.io.PrintWriter;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.xml.sax.InputSource;

public class UserConfig
{
	final private static String XML_TAG_USERCONFIG = "DesktopUserConfig";
	final private static String XML_TAG_MULTIGHOST = "MultiGhost";

	private File     file     = null;
	private Document document = null;

	public UserConfig(File xmlfile) throws Exception
	{
		this.file = xmlfile;
		this.document = FNX.getDOMDocument(this.file);
		this.document.setXmlStandalone(true);

		if(document.getElementsByTagName(this.XML_TAG_USERCONFIG).getLength() == 0)
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_USERCONFIG));
		}
	}

	public boolean getMultiGhost() throws Exception
	{
		NodeList nodes = this.document.getElementsByTagName(this.XML_TAG_MULTIGHOST);

		if(nodes.getLength() == 0)
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_MULTIGHOST));
		}

		switch(((Element) nodes.item(0)).getTextContent().toLowerCase())
		{
			case "true": return true;
			case "false": return false;
			default: throw new Exception();
		}
	}
}
