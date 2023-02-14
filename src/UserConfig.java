/**
 * Profiles.java: Very limited representation of UserConfig.xml
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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UserConfig
{
	final private static String XML_TAG_USERCONFIG = "DesktopUserConfig";
	final private static String XML_TAG_GHOSTNICK  = "TrainingGhostNick";
	final private static String XML_TAG_MULTIGHOST = "MultiGhost";

	private File     file     = null;
	private Document document = null;

	public UserConfig(File xmlfile) throws ProfileException
	{
		try
		{
			this.file = xmlfile;
			this.document = FNX.getDOMDocument(this.file);
			this.document.setXmlStandalone(true);

			if(document.getElementsByTagName(XML_TAG_USERCONFIG).getLength() == 0)
			{
				throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_USERCONFIG));
			}
		}
		catch(ParserConfigurationException|SAXException|IOException e)
		{
			throw new ProfileException("Could not parse XML file", e);
		}
	}

	public boolean getMultiGhost()
	{
		NodeList nodes = this.document.getElementsByTagName(XML_TAG_MULTIGHOST);

		if(nodes.getLength() == 0)
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_MULTIGHOST));
		}

		switch(((Element) nodes.item(0)).getTextContent().toLowerCase())
		{
			case "true":
				return true;

			case "false":
				return false;

			default:
				throw new ProfileException(String.format("Invalid content at <%s> tag", XML_TAG_MULTIGHOST));
		}
	}

	public String getTrainingGhostNick()
	{
		NodeList nodes = this.document.getElementsByTagName(XML_TAG_GHOSTNICK);

		if(nodes.getLength() == 0)
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_GHOSTNICK));
		}

		String nick = ((Element) nodes.item(0)).getTextContent();

		if(nick.length() > 0)
		{
			return nick;
		}

		return null;
	}
}
