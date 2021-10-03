/**
 * OfflineProfiles.java: Representation of OfflineProfiles.xml
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import java.lang.IndexOutOfBoundsException;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class OfflineProfiles
{
	final private static String XML_TAG_PROFILES = "ProfilList";
	final private static String XML_TAG_DEFAULT  = "DefaultProfile";
	final private static String XML_TAG_PROFILE  = "OfflineProfile";
	final private static String XML_TAG_GHOSTS   = "TrainingGhosts";
	final private static String XML_TAG_GHOST    = "GhostDataPair";
	final private static String XML_TAG_NICK     = "Nickname";
	final private static String XML_TAG_TOKEN    = "Token";

	final private static String DEFAULT_TOKEN    = "DefaultToken";

	private File     file     = null;
	private Document document = null;
	private boolean  changed  = false;
	private int      profile  = 0;

	private NodeList                OfflineProfiles;
	private Element                 OfflineProfile;
	private Element                 DefaultProfile;
	private Node                    TrainingNode;
	private Element                 TrainingElement;
	private NodeList                TrainingGhosts;
	private ArrayList<GhostElement> GhostElements;

	public OfflineProfiles(String xmlstring) throws ProfileException
	{
		try
		{
			this.file = null;
			this.document = FNX.getDOMDocument(xmlstring);
			this.postParsing();
		}
		catch(ParserConfigurationException|SAXException|IOException e)
		{
			throw new ProfileException("Could not parse XML string", e);
		}
	}

	public OfflineProfiles(File xmlfile) throws ProfileException,  FileNotFoundException
	{
		try
		{
			this.checkFile(xmlfile);
			this.file = xmlfile;
			this.reload();
		}
		catch(IOException e)
		{
			throw new ProfileException(String.format("Could not read XML file: %s", xmlfile), e);
		}
	}

	private void checkFile(File xmlfile) throws FileNotFoundException
	{
		if(xmlfile == null || !xmlfile.exists() || !xmlfile.isFile())
		{
			throw new FileNotFoundException(xmlfile.getAbsolutePath());
		}
	}

	public void updateFile(File xmlfile) throws ProfileException, FileNotFoundException
	{
		if(this.file == null)
		{
			throw new IllegalStateException("OfflineProfiles not initialized with File; updateFile() not possible");
		}

		this.checkFile(xmlfile);
		this.file = xmlfile;
	}

	public void reload() throws ProfileException
	{
		if(this.file == null)
		{
			throw new IllegalStateException("OfflineProfiles not initialized with File; reload() not possible");
		}

		try
		{
			this.changed = false;
			this.document = FNX.getDOMDocument(this.file);
			this.postParsing();
		}
		catch(ParserConfigurationException|SAXException|IOException e)
		{
			throw new ProfileException("Could not reload XML file", e);
		}
	}

	private void postParsing()
	{
		this.document.setXmlStandalone(true);

		this.TrainingElement  = null;
		this.OfflineProfile  = null;

		// xsi:type="GameOfflineProfile"
		this.OfflineProfiles = document.getElementsByTagName(XML_TAG_PROFILE);
		NodeList DefaultProfiles = document.getElementsByTagName(XML_TAG_DEFAULT);

		if(this.getProfileCount() == 0 && DefaultProfiles.getLength() == 0)
		{
			throw new ProfileException(String.format("Missing <%s> and <%s> tags", XML_TAG_PROFILE, XML_TAG_DEFAULT));
		}
		else if(DefaultProfiles.getLength() > 1)
		{
			throw new ProfileException(String.format("Too many <%s> tags", XML_TAG_DEFAULT));
		}

		if(DefaultProfiles.getLength() > 0)
		{
			this.DefaultProfile = (Element) DefaultProfiles.item(0);
		}
		else
		{
			this.DefaultProfile = null;
		}

		this.selectProfile(0);
	}

	public int defaultProfile()
	{
		if(this.DefaultProfile == null)
		{
			return -1;
		}
		else if(this.getProfileCount() == 0)
		{
			return 0;
		}

		return this.getProfileCount() - 1;
	}

	public int getProfileCount()
	{
		if(this.OfflineProfiles == null)
		{
			throw new IndexOutOfBoundsException("OfflineProfiles == null");
		}

		return this.OfflineProfiles.getLength() + ((this.DefaultProfile != null) ? 1 : 0);
	}

	public int getGhostCount()
	{
		if(this.TrainingGhosts != null)
		{
			return this.TrainingGhosts.getLength();
		}

		return 0;
	}

	public GhostElement getGhost(int index)
	{
		if(index >= getGhostCount())
		{
			throw new IndexOutOfBoundsException(String.format("Ghost #%d", index));
		}

		return this.GhostElements.get(index);
	}

	public int[] getGhostsByCondition(int mode, String track, int weather)
	{
		ArrayList<Integer> ghosts = new ArrayList<>();

		for(int i = 0; i < this.getGhostCount(); i++)
		{
			GhostElement ghost = this.getGhost(i);

			if(ghost.getGameMode() == mode && ghost.getTrack().equalsIgnoreCase(track) && ghost.getWeather() == weather)
			{
				ghosts.add(i);
			}
		}

		return ghosts.stream().mapToInt(i->i).toArray();
	}

	public int[] getGhostsByCondition(GhostElement ghost)
	{
		return this.getGhostsByCondition(ghost.getGameMode(), ghost.getTrack(), ghost.getWeather());
	}

	public GhostElement[][][] getAllGhosts()
	{
		return this.getAllGhosts(false);
	}

	// ACHTUNG: Falls es mehrere Geister je Bedingung gibt, wird nur
	// der erste Geist zurückgegeben! Das Array ist somit immer sauber.
	public GhostElement[][][] getAllGhosts(boolean warn)
	{
		int[] modes = gmHelper.getGameModeIDs();
		String[] tracks = gmHelper.getTracks(true);
		int[] weathers = gmHelper.getWeatherIDs();

		GhostElement result[][][] = new GhostElement[modes.length][tracks.length][weathers.length];

		for(int m = 0; m < modes.length; m++)
		{
			for(int t = 0; t < tracks.length; t++)
			{
				for(int w = 0; w < weathers.length; w++)
				{
					int[] ghosts = this.getGhostsByCondition(modes[m], tracks[t], weathers[w]);

					if(ghosts.length > 1 && warn)
					{
						return null;
					}
					else if(ghosts.length > 0)
					{
						result[m][t][w] = this.getGhost(ghosts[0]);
					}
					else
					{
						result[m][t][w] = null;
					}
				}
			}
		}

		return result;
	}

	// Gibt im Gegensatz zu getAllGhosts() wirklich alle Geister zurück, auch mehrere je Bedingung.
	public ArrayList<ArrayList<ArrayList<ArrayList<GhostElement>>>> getGhostList()
	{
		int[] modes = gmHelper.getGameModeIDs();
		String[] tracks = gmHelper.getTracks(true);
		int[] weathers = gmHelper.getWeatherIDs();

		ArrayList<ArrayList<ArrayList<ArrayList<GhostElement>>>> result = new ArrayList<ArrayList<ArrayList<ArrayList<GhostElement>>>>(modes.length);

		for(int m = 0; m < modes.length; m++)
		{
			ArrayList<ArrayList<ArrayList<GhostElement>>> mi = new ArrayList<ArrayList<ArrayList<GhostElement>>>(tracks.length);
			result.add(mi);

			for(int t = 0; t < tracks.length; t++)
			{
				ArrayList<ArrayList<GhostElement>> ti = new ArrayList<ArrayList<GhostElement>>(weathers.length);
				mi.add(ti);

				for(int w = 0; w < weathers.length; w++)
				{
					int[] ghosts = this.getGhostsByCondition(modes[m], tracks[t], weathers[w]);
					ArrayList<GhostElement> wi = new ArrayList<>(ghosts.length);
					ti.add(wi);

					for(int i = 0; i < ghosts.length; i++)
					{
						wi.add(this.getGhost(ghosts[i]));
					}
				}
			}
		}

		return result;
	}

	public void deleteGhost(int index)
	{
		this.changed = true;
		this.GhostElements.remove(index);
		Element GhostElement = (Element) this.TrainingGhosts.item(index);
		GhostElement.getParentNode().removeChild(GhostElement);

		if(this.GhostElements.size() != this.TrainingGhosts.getLength())
		{
			throw new ProfileException(String.format("GhostElements(%d) != TrainingGhosts(%d)", this.GhostElements.size(), this.TrainingGhosts.getLength()));
		}
	}

	public int addGhost(String ghost)
	{
		return this.addGhost(new GhostElement(ghost));
	}

	public int addGhost(GhostElement ghost)
	{
		this.changed = true;
		Node importedNode = this.document.importNode(ghost.getElement(), false);
		this.TrainingNode.appendChild(importedNode);
		ghost = new GhostElement(importedNode);
		this.GhostElements.add(ghost);

		if(this.GhostElements.size() != this.TrainingGhosts.getLength())
		{
			throw new ProfileException(String.format("GhostElements(%d) != TrainingGhosts(%d)", this.GhostElements.size(), this.TrainingGhosts.getLength()));
		}

		return this.getGhostCount() - 1;
	}

	public String[] getProfiles()
	{
		String[] profiles = new String[this.getProfileCount()];

		for(int i = 0; i < this.getProfileCount(); i++)
		{
			Element profile;

			if(i == this.defaultProfile())
			{
				profile = DefaultProfile;
			}
			else
			{
				profile = (Element) OfflineProfiles.item(i);
			}

			profiles[i] = ((Element) this.getNickNode(profile)).getTextContent();
		}

		return profiles;
	}

	private Node getNickNode(Element profile)
	{
		NodeList nick = profile.getElementsByTagName(XML_TAG_NICK);

		if(nick.getLength() > 0)
		{
			return nick.item(0);
		}

		throw new ProfileException(String.format("No <%s> tag in profile", XML_TAG_NICK));
	}

	public int getProfileByNick(String nickname)
	{
		return this.getProfileByNick(nickname, false);
	}

	public int getProfileByNick(String nickname, boolean includeDefault)
	{
		if(nickname != null && nickname.length() > 0)
		{
			int defaultIndex = defaultProfile();
			String[] profiles = this.getProfiles();

			for(int i = 0; i < profiles.length; i++)
			{
				if(!includeDefault && i == defaultIndex)
				{
					continue;
				}

				if(profiles[i].equalsIgnoreCase(nickname))
				{
					return i;
				}
			}
		}

		return -1;
	}

	public void addProfile(String nickname)
	{
		// TODO: In eigene Datei auslagern, einlesen und Nicknamen ersetzen.
		// Siehe auch die Verwendung von VERSION_FILE in HTGT.getVersion()!
		// ...

		String xml = String.format("<OfflineProfile xsi:type=\"GameOfflineProfile\"><Nickname>%s</Nickname><Token>%s</Token><ReceivedAchievements>false</ReceivedAchievements><TrainingGhosts /><DuelReplays /><DuelNicks /><IgnoredFriendDuels /><IgnoredOpenFriends /><PendingPts /><PendingDuelSelection><DuelSelection /></PendingDuelSelection></OfflineProfile>", nickname, DEFAULT_TOKEN);

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

			NodeList profilesNodes = this.document.getElementsByTagName(XML_TAG_PROFILES);

			if(profilesNodes.getLength() == 0)
			{
				throw new ProfileException(String.format("Missing <%s> tag", XML_TAG_PROFILES));
			}

			Node profiles = profilesNodes.item(0);

			this.changed = true;
			profiles.insertBefore(importedNode, profiles.getFirstChild());
		}
		catch(ParserConfigurationException|SAXException|IOException e)
		{
			throw new ProfileException("Could not parse XML template for new OfflineProfile", e);
		}
	}

	public void deleteProfile(String nickname) throws ProfileException
	{
		for(int i = 0; i < this.getProfileCount(); i++)
		{
			if(i == this.defaultProfile())
			{
				continue;
			}

			if(nickname.equals(((Element) this.getNickNode((Element) OfflineProfiles.item(i)))))
			{
				this.deleteProfile(i);
				return;
			}
		}

		throw new ProfileException(String.format("Profile not found: %s", nickname));
	}

	public void deleteProfile(int index) throws ProfileException
	{
		if(index >= 0 && index < this.getProfileCount() && index != this.defaultProfile())
		{
			this.changed = true;
			Element profile = (Element) OfflineProfiles.item(index);
			profile.getParentNode().removeChild(profile);
			return;
		}

		throw new ProfileException(String.format("Profile not found: #%d", index));
	}

	public void renameProfile(String nickname)
	{
		this.renameProfile(this.profile, nickname);
	}

	public void renameProfile(String oldNickname, String newNickname) throws ProfileException
	{
		for(int i = 0; i < this.getProfileCount(); i++)
		{
			if(i == this.defaultProfile())
			{
				continue;
			}

			if(oldNickname.equals(((Element) this.getNickNode((Element) OfflineProfiles.item(i))).getTextContent()))
			{
				this.renameProfile(i, newNickname);
				return;
			}
		}

		throw new ProfileException(String.format("Profile not found: %s", oldNickname));
	}

	public void renameProfile(int index, String nickname) throws ProfileException
	{
		if(index >= 0 && index < this.getProfileCount() && index != this.defaultProfile())
		{
			this.changed = true;
			this.getNickNode((Element) OfflineProfiles.item(index)).setTextContent(nickname);

			return;
		}

		throw new ProfileException(String.format("Profile not found: #%d", index));
	}

	public String getToken()
	{
		NodeList token = this.OfflineProfile.getElementsByTagName(XML_TAG_TOKEN);

		if(token.getLength() > 0)
		{
			String t = ((Element) token.item(0)).getTextContent();

			if(!t.equalsIgnoreCase(DEFAULT_TOKEN))
			{
				return t;
			}
		}

		return null;
	}

	public void setToken(String token)
	{
		try
		{
			NodeList TokenNode = this.OfflineProfile.getElementsByTagName(XML_TAG_TOKEN);
			Element TokenElement = null;
			boolean create = false;

			if(TokenNode.getLength() > 0)
			{
				TokenElement = (Element) TokenNode.item(0);
			}

			if(TokenElement == null)
			{
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

				Document doc = dBuilder.newDocument();
				TokenElement = doc.createElement(XML_TAG_TOKEN);

				create = true;
			}

			token = (token != null) ? token : DEFAULT_TOKEN;
			TokenElement.setTextContent(token);
			this.changed = true;

			if(create)
			{
				this.OfflineProfile.appendChild(this.document.importNode(TokenElement, true));
			}
		}
		catch(ParserConfigurationException e)
		{
			throw new ProfileException("Could not update token", e);
		}
	}

	public void deleteToken()
	{
		this.setToken(null);
	}

	public void selectProfile(int index)
	{
		if(index >= this.getProfileCount())
		{
			throw new IndexOutOfBoundsException(String.format("OfflineProfile #%d", index));
		}
		else if(this.profile == index && this.OfflineProfile != null)
		{
			return;
		}

		this.profile = index;
		this.GhostElements = null;
		this.TrainingElement = null;

		if(this.profile == this.defaultProfile())
		{
			this.OfflineProfile = DefaultProfile;
		}
		else
		{
			this.OfflineProfile = (Element) OfflineProfiles.item(this.profile);
		}

		NodeList GhostNodes = this.OfflineProfile.getElementsByTagName(XML_TAG_GHOSTS);
		this.GhostElements = new ArrayList<>(0);

		if(GhostNodes.getLength() > 0)
		{
			this.TrainingNode = GhostNodes.item(0);
			this.TrainingElement = (Element) this.TrainingNode;
			this.TrainingGhosts = this.TrainingElement.getElementsByTagName(XML_TAG_GHOST);

			if(this.getGhostCount() > 0)
			{
				this.GhostElements = new ArrayList<>(this.getGhostCount());

				for(int i = 0; i < this.getGhostCount(); i++)
				{
					try
					{
						// Damit ein Geist nur einmal verarbeitet wird, werden alle bereits hier eingelesen.
						// Somit erfolgt nur noch beim Profilwechsel eine aufwändige erneute Verarbeitung.
						// Das ist allerdings gewollt, damit nicht unnötig Arbeitsspeicher belegt wird.
						this.GhostElements.add(i, new GhostElement((Element) this.TrainingGhosts.item(i)));
					}
					catch(GhostException e)
					{
						throw new GhostException(i, e);
					}
				}
			}
		}
	}

	public String toString()
	{
		return FNX.getWinNL(FNX.getCleanXML(this.document));
	}

	public boolean changed()
	{
		return this.changed;
	}

	public void saved()
	{
		this.changed = false;
	}
}
