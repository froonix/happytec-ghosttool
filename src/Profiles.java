/**
 * Profiles.java: Very limited representation of Profiles.xml
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

public class Profiles
{
	final private static String XML_TAG_PROFILES = "ProfilList";
	final private static String XML_TAG_PROFILE  = "Profile";
	final private static String XML_TAG_PLAYER   = "Player";

	private File     file     = null;
	private Document document = null;
	private Node     profiles = null;

	public Profiles(File xmlfile) throws Exception
	{
		this.file = xmlfile;
		this.document = FNX.getDOMDocument(this.file);
		this.document.setXmlStandalone(true);

		NodeList profileNodes = document.getElementsByTagName(this.XML_TAG_PROFILES);

		if(profileNodes.getLength() == 0)
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_PROFILES));
		}

		this.profiles = profileNodes.item(0);
	}

	public void addProfile(String nickname) throws Exception
	{
		// TODO: In eigene Datei auslagern, einlesen und Nicknamen ersetzen.
		// Siehe auch die Verwendung von VERSION_FILE in HTGT.getVersion()!
		// ...

		// TODO: Zufällige Farbe vom Anzug usw.
		// ...

		String xml = String.format("<Profile xsi:type=\"GameProfile\"><Progress><Entries /></Progress><Inventory /><Controls entries=\"LEFT:LeftArrow,LEFT-T:,RIGHT:RightArrow,RIGHT-T:,ACCEL:UpArrow,ACCEL-T:,BRAKE:Tab,BRAKE-T:,JUMP:Space,JUMP-T:,CAMERA:C,CAMERA-T:,GHOST:G,GHOST-T:,REWIND:Backspace,REWIND-T:,RESTART:R,RESTART-T:,ACTION:Return,ACTION-T:,JOYJUMP:JoystickButton0,JOYJUMP-T:,JOYCAMERA:JoystickButton1,JOYCAMERA-T:,JOYGHOST:JoystickButton2,JOYGHOST-T:,JOYREWIND:JoystickButton4,JOYREWIND-T:,JOYRESTART:JoystickButton3,JOYRESTART-T:,JOYACTION:JoystickButton5,JOYACTION-T:,JOYSTEER:Axis1,JOYSTEER-INV:False,JOYSTEER-SCALE:1,JOYACCEL:Axis3,JOYACCEL-INV:False,JOYACCEL-SCALE:1\" nickname=\"\" OnlineSaved=\"False\" /><Player Nickname=\"%s\" OnlineSaved=\"false\" SocialId=\"\" SocialName=\"\" SocialNetworkType=\"1\" ShowAgb=\"false\"><Location>-1</Location></Player><TrackList OnlineSaved=\"false\" /><Settings entries=\"Dress:0,DressColor1:0.6009873-0.2179533-0.2472593,DressColor2:0.1788466-0.4515831-0.5623612,PlayerModel:0,Ski:0,Helmet:0,Gloves:0,StartingNumber:68,Boots:0,Bindings:0,SkinTone:2,Jersey:0,Flag:12,SkiDesign:0,SkiSticksBindings:0,Goggles:0\" nickname=\"\" OnlineSaved=\"False\" /></Profile>", nickname);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		Document profileDoc = dBuilder.parse(new InputSource(new StringReader(xml)));
		NodeList profileNodes = profileDoc.getElementsByTagName(this.XML_TAG_PROFILE);

		if(profileNodes.getLength() != 1)
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_PROFILE));
		}

		Element profileElement = (Element) profileNodes.item(0);
		Node importedNode = this.document.importNode(profileElement, true);
		this.profiles.insertBefore(importedNode, this.profiles.getFirstChild());
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
				return;
			}
		}

		throw new ProfileException(String.format("Profile not found: %s", nickname));
	}

	public void renameProfile(String oldNickname, String newNickname) throws Exception
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

	private Node[] getProfiles() throws ProfileException
	{
		NodeList profileNodes = this.document.getElementsByTagName(this.XML_TAG_PROFILE);

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

	private Node getPlayerNode(Element c) throws ProfileException
	{
		NodeList players = c.getElementsByTagName(this.XML_TAG_PLAYER);

		if(players.getLength() > 0)
		{
			return players.item(0);
		}
		else
		{
			throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_PLAYER));
		}
	}

	private boolean compareProfile(Element c, String n) throws ProfileException
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

	private void renamePlayer(Element c, String n) throws Exception
	{
		((Element) this.getPlayerNode(c)).setAttribute("Nickname", n);
	}

	// ACHTUNG: DefaultProfile nicht implementiert!
	public boolean profileExists(String nickname)
	{
		try
		{
			Node[] profileNodes = this.getProfiles();

			for(int i = 0; i < profileNodes.length; i++)
			{
				if(compareProfile((Element) profileNodes[i], nickname))
				{
					return true;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public void saveProfiles() throws Exception
	{
		String xml = FNX.getCleanXML(this.document);

		if(xml == null)
		{
			throw new Exception();
		}

		PrintWriter tmp = new PrintWriter(file);
		tmp.printf("%s", FNX.getWinNL(xml));
		tmp.close();
	}


}
