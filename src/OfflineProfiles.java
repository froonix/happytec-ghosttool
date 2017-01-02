import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.InputSource;
import javax.xml.xpath.*;
import org.w3c.dom.*;

import java.lang.IndexOutOfBoundsException;

// OfflineProfiles.xml
class OfflineProfiles
{
	final private static String XML_TAG_DEFAULT = "DefaultProfile";
	final private static String XML_TAG_PROFILE = "OfflineProfile";
	final private static String XML_TAG_GHOSTS  = "TrainingGhosts";
	final private static String XML_TAG_GHOST   = "GhostDataPair";
	final private static String XML_TAG_NICK    = "Nickname";

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

	public OfflineProfiles(String xmlstring) throws Exception
	{
		this.file = null;
		this.document = FNX.getDOMDocument(xmlstring);
		this.postParsing();
	}

	public OfflineProfiles(File xmlfile) throws Exception
	{
		this.checkFile(xmlfile);
		this.file = xmlfile;
		this.reload();
	}

	private void checkFile(File xmlfile) throws Exception
	{
		if(xmlfile == null || !xmlfile.exists() || !xmlfile.isFile())
		{
			throw new FileNotFoundException(xmlfile.getAbsolutePath());
		}
	}

	public void updateFile(File xmlfile) throws Exception
	{
		if(this.file == null)
		{
			throw new Exception("OfflineProfiles not initialized with File; updateFile() not possible");
		}

		this.checkFile(xmlfile);
		this.file = xmlfile;
	}

	public void reload() throws Exception
	{
		if(this.file == null)
		{
			throw new Exception("OfflineProfiles not initialized with File; reload() not possible");
		}

		this.changed = false;
		this.document = FNX.getDOMDocument(this.file);
		this.postParsing();
	}

	private void postParsing() throws Exception
	{
		this.document.setXmlStandalone(true);

		this.TrainingElement  = null;
		this.OfflineProfile  = null;

		// xsi:type="GameOfflineProfile"
		this.OfflineProfiles = document.getElementsByTagName(this.XML_TAG_PROFILE);
		NodeList DefaultProfiles = document.getElementsByTagName(this.XML_TAG_DEFAULT);

		/*
		if(this.getProfileCount() == 0)
		{
			throw new Exception(String.format("Missing <%s> tag", XML_TAG_PROFILE));
		}

		if(DefaultProfiles.getLength() > 1)
		{
			throw new Exception(String.format("Too many <%s> tags", XML_TAG_DEFAULT));
		}
		*/

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

	public int[] getGhostsByCondition(String track, int weather)
	{
		track = track.toLowerCase();
		ArrayList<Integer> ghosts = new ArrayList<Integer>();

		for(int i = 0; i < this.getGhostCount(); i++)
		{
			GhostElement ghost = this.getGhost(i);

			if(ghost.getTrack().toLowerCase().equals(track) && ghost.getWeather() == weather)
			{
				ghosts.add(i);
			}
		}

		return ghosts.stream().mapToInt(i->i).toArray();
	}

	public int[] getGhostsByCondition(GhostElement ghost)
	{
		return this.getGhostsByCondition(ghost.getTrack(), ghost.getWeather());
	}

	public void deleteGhost(int index) throws Exception
	{
		this.changed = true;
		this.GhostElements.remove(index);
		Element GhostElement = (Element) this.TrainingGhosts.item(index);
		GhostElement.getParentNode().removeChild(GhostElement);

		if(this.GhostElements.size() != this.TrainingGhosts.getLength())
		{
			throw new Exception(String.format("GhostElements(%d) != TrainingGhosts(%d)", this.GhostElements.size(), this.TrainingGhosts.getLength()));
		}
	}

	public int addGhost(String ghost) throws Exception
	{
		return this.addGhost(new GhostElement(ghost));
	}

	public int addGhost(GhostElement ghost) throws Exception
	{
		this.changed = true;
		Node importedNode = this.document.importNode(ghost.getElement(), false);
		this.TrainingNode.appendChild(importedNode);
		ghost = new GhostElement(importedNode);
		this.GhostElements.add(ghost);

		if(this.GhostElements.size() != this.TrainingGhosts.getLength())
		{
			throw new Exception(String.format("GhostElements(%d) != TrainingGhosts(%d)", this.GhostElements.size(), this.TrainingGhosts.getLength()));
		}

		return this.getGhostCount() - 1;
	}

	public String[] getProfiles() throws Exception
	{
		String[] profiles = new String[this.getProfileCount()];

		for(int i = 0; i < this.getProfileCount(); i++)
		{
			Element profile;

			if(i == this.defaultProfile())
			{
				profile = (Element) DefaultProfile;
			}
			else
			{
				profile = (Element) OfflineProfiles.item(i);
			}

			NodeList nick = profile.getElementsByTagName(this.XML_TAG_NICK);

			if(nick.getLength() > 0)
			{
				Element nickname = (Element) nick.item(0);
				profiles[i] = nickname.getTextContent();
			}
			else
			{
				throw new Exception(String.format("No <%s> tag in profile #%d", this.XML_TAG_NICK, i));
			}
		}

		return profiles;
	}

	public void selectProfile(int index)
	{
		if(index >= this.getProfileCount())
		{
			throw new IndexOutOfBoundsException(String.format("OfflineProfile #%d", index));
		}

		this.profile = index;
		this.GhostElements = null;
		this.TrainingElement = null;

		if(this.profile == this.defaultProfile())
		{
			this.OfflineProfile = (Element) DefaultProfile;
		}
		else
		{
			this.OfflineProfile = (Element) OfflineProfiles.item(this.profile);
		}

		NodeList GhostNodes = this.OfflineProfile.getElementsByTagName(this.XML_TAG_GHOSTS);
		this.GhostElements = new ArrayList<GhostElement>(0);

		if(GhostNodes.getLength() > 0)
		{
			this.TrainingNode = GhostNodes.item(0);
			this.TrainingElement = (Element) this.TrainingNode;
			this.TrainingGhosts = this.TrainingElement.getElementsByTagName(this.XML_TAG_GHOST);

			if(this.getGhostCount() > 0)
			{
				this.GhostElements = new ArrayList<GhostElement>(this.getGhostCount());

				for(int i = 0; i < this.getGhostCount(); i++)
				{
					// Damit ein Geist nur einmal verarbeitet wird, werden alle bereits hier eingelesen.
					// Somit erfolgt nur noch beim Profilwechsel eine aufwändige erneute Verarbeitung.
					// Das ist allerdings gewollt, damit nicht unnötig Arbeitsspeicher belegt wird.
					this.GhostElements.add(i, new GhostElement((Element) this.TrainingGhosts.item(i)));
				}
			}
		}
	}

	public String toString()
	{
		String XML;

		try
		{
			this.document.getDocumentElement().normalize();
			XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//text()[normalize-space(.) = '']");
			NodeList blankTextNodes = (NodeList) xpath.evaluate(this.document, XPathConstants.NODESET);

			for(int i = 0; i < blankTextNodes.getLength(); i++)
			{
				blankTextNodes.item(i).getParentNode().removeChild(blankTextNodes.item(i));
			}

			return FNX.getWinNL(FNX.getStringFromDOM(this.document, true));
		}
		catch(Exception e)
		{
			e.printStackTrace();

		}

		return null;
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
