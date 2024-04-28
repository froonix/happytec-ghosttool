/**
 * Profiles.java: Very limited representation of Profiles.xml
 * Copyright (C) 2016-2024 Christian Schrötter <cs@fnx.li>
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

public class Profiles
{
	final private static String XML_TAG_PROFILES = "ProfilList";
	final private static String XML_TAG_PROFILE  = "Profile";
	final private static String XML_TAG_PLAYER   = "Player";

	private File     file     = null;
	private Document document = null;
	private Node     profiles = null;

	public Profiles(File xmlfile) throws ProfileException
	{
		try
		{
			this.file = xmlfile;
			this.document = FNX.getDOMDocument(this.file);
			this.postParsing();
		}
		catch(ParserConfigurationException|SAXException|IOException e)
		{
			throw new ProfileException("Could not parse XML file", e);
		}
	}

	private void postParsing()
	{
		this.document.setXmlStandalone(true);

		NodeList profileNodes = document.getElementsByTagName(XML_TAG_PROFILES);

		if(profileNodes.getLength() == 0)
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_PROFILES));
		}

		this.profiles = profileNodes.item(0);
	}

	public void reload() throws ProfileException
	{
		try
		{
			this.document = FNX.getDOMDocument(this.file);
			this.postParsing();
		}
		catch(ParserConfigurationException|SAXException|IOException e)
		{
			throw new ProfileException("Could not reload XML file", e);
		}
	}

	public void addProfile(String nickname)
	{
		// TODO: In eigene Datei auslagern, einlesen und Nicknamen ersetzen.
		// Siehe auch die Verwendung von VERSION_FILE in HTGT.getVersion()!
		// ...

		String xml = String.format("<Profile xsi:type=\"GameProfile\"><Progress><Entries /></Progress><Inventory /><Controls entries=\"\" nickname=\"\" OnlineSaved=\"False\" /><Player Nickname=\"%s\" OnlineSaved=\"false\" SocialId=\"\" SocialName=\"\" SocialNetworkType=\"1\" ShowAgb=\"false\"><Location>-1</Location></Player><TrackList OnlineSaved=\"false\" /><Settings entries=\"\" nickname=\"\" OnlineSaved=\"False\" /></Profile>", nickname);

		try
		{
			Document profileDoc = FNX.getDOMDocument(xml);
			NodeList profileNodes = profileDoc.getElementsByTagName(XML_TAG_PROFILE);

			if(profileNodes.getLength() != 1)
			{
				throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_PROFILE));
			}

			Element profileElement = (Element) profileNodes.item(0);
			Node importedNode = this.document.importNode(profileElement, true);
			this.profiles.insertBefore(importedNode, this.profiles.getFirstChild());
		}
		catch(ParserConfigurationException|SAXException|IOException e)
		{
			throw new ProfileException("Could not parse XML template for new Profile", e);
		}
	}

	public void deleteProfile(String nickname) throws ProfileException
	{
		Node[] profileNodes = this.getProfiles();

		for(int i = 0; i < profileNodes.length; i++)
		{
			Element profile = (Element) profileNodes[i];

			if(compareProfile(profile, nickname))
			{
				profile.getParentNode().removeChild(profile);
				resetCurrentProfile();
				return;
			}
		}

		throw new ProfileException(String.format("Profile not found: %s", nickname));
	}

	public void renameProfile(String oldNickname, String newNickname) throws ProfileException
	{
		Node[] profileNodes = this.getProfiles();

		for(int i = 0; i < profileNodes.length; i++)
		{
			Element profile = (Element) profileNodes[i];

			if(compareProfile(profile, oldNickname))
			{
				this.renamePlayer(profile, newNickname);
				return;
			}
		}

		throw new ProfileException(String.format("Profile not found: %s", oldNickname));
	}

	private Node[] getProfiles()
	{
		NodeList profileNodes = this.document.getElementsByTagName(XML_TAG_PROFILE);

		if(profileNodes.getLength() == 0)
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_PROFILE));
		}

		Node[] profiles = new Node[profileNodes.getLength()];

		for(int i = 0; i < profileNodes.getLength(); i++)
		{
			profiles[i] = profileNodes.item(i);
		}

		return profiles;
	}

	private Node getPlayerNode(Element c)
	{
		NodeList players = c.getElementsByTagName(XML_TAG_PLAYER);

		if(players.getLength() > 0)
		{
			return players.item(0);
		}
		else
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_PLAYER));
		}
	}

	private boolean compareProfile(Element c, String n)
	{
		Element player = (Element) this.getPlayerNode(c);

		if(n.equals(player.getAttribute("Nickname")))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private void renamePlayer(Element c, String n)
	{
		((Element) this.getPlayerNode(c)).setAttribute("Nickname", n);
	}

	// ACHTUNG: DefaultProfile nicht implementiert!
	public boolean profileExists(String nickname)
	{
		Node[] profileNodes = this.getProfiles();

		for(int i = 0; i < profileNodes.length; i++)
		{
			if(compareProfile((Element) profileNodes[i], nickname))
			{
				return true;
			}
		}

		return false;
	}

	public boolean hasOnlineToken(String nickname) throws ProfileException
	{
		Node[] profileNodes = this.getProfiles();

		for(int i = 0; i < profileNodes.length; i++)
		{
			Element profile = (Element) profileNodes[i];

			if(compareProfile(profile, nickname))
			{
				String token = ((Element) this.getPlayerNode(profile)).getAttribute("OnlineToken");

				if(token != null && token.length() > 0)
				{
					return true;
				}

				return false;
			}
		}

		throw new ProfileException(String.format("Profile not found: %s", nickname));
	}

	public int getCurrentProfile()
	{
		return FNX.intval(this.document.getDocumentElement().getAttribute("CurrentProfile"));
	}

	public void resetCurrentProfile()
	{
		this.setCurrentProfile(0);
	}

	public void setCurrentProfile(int n) throws ProfileException
	{
		this.document.getDocumentElement().setAttribute("CurrentProfile", String.valueOf(n));

		if(this.getCurrentProfile() != n)
		{
			throw new ProfileException("Could not set current profile");
		}
	}

	public void saveProfiles() throws ProfileException, FileNotFoundException
	{
		PrintWriter tmp = new PrintWriter(file);
		tmp.printf("%s", FNX.getWinNL(FNX.getCleanXML(this.document)));
		tmp.close();
	}
}
