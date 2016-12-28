import java.io.*;
import java.util.ArrayList;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;

import java.lang.IndexOutOfBoundsException;

// OfflineProfiles.xml
class OfflineProfiles
{
	final private static String XML_TAG_PROFILE = "OfflineProfile";
	final private static String XML_TAG_GHOSTS  = "TrainingGhosts";
	final private static String XML_TAG_GHOST   = "GhostDataPair";
	final private static String XML_TAG_NICK    = "Nickname";

	private static DocumentBuilderFactory dbFactory;
	private static DocumentBuilder        dBuilder;

	private File     file     = null;
	private Document document = null;
	private boolean  changed  = false;
	private int      profile  = 0;

	private NodeList                OfflineProfiles;
	private Element                 OfflineProfile;
	private Node                    TrainingNode;
	private Element                 TrainingElement;
	private NodeList                TrainingGhosts;
	private ArrayList<GhostElement> GhostElements;

	public OfflineProfiles(String xmlstring) throws Exception
	{
		this.file = null;
		this.document = getParser().parse(new InputSource(new StringReader(xmlstring)));
		this.postParsing();
	}

	public OfflineProfiles(File xmlfile) throws Exception
	{
		this.file = xmlfile;
		this.reload();
	}

	public void updateFile(File xmlfile) throws Exception
	{
		if(this.file == null)
		{
			throw new Exception("OfflineProfiles not initialized with File; updateFile() not possible");
		}

		this.file = xmlfile;
	}

	public void reload() throws Exception
	{
		if(this.file == null)
		{
			throw new Exception("OfflineProfiles not initialized with File; reload() not possible");
		}

		this.document = getParser().parse(this.file);
		this.postParsing();
	}

	private static DocumentBuilder getParser() throws Exception
	{
		if(dbFactory == null)
		{
			dbFactory = DocumentBuilderFactory.newInstance();
		}

		if(dBuilder == null)
		{
			dBuilder = dbFactory.newDocumentBuilder();
		}

		return dBuilder;
	}

	private void postParsing() throws Exception
	{
		this.document.setXmlStandalone(true);

		this.TrainingElement  = null;
		this.OfflineProfile  = null;
		this.OfflineProfiles = document.getElementsByTagName(this.XML_TAG_PROFILE);

		if(this.getProfileCount() == 0)
		{
			throw new Exception(String.format("Missing <%s> tag", XML_TAG_PROFILE));
		}
		else
		{
			this.selectProfile(0);
		}
	}

	public int getProfileCount()
	{
		if(this.OfflineProfiles == null)
		{
			throw new IndexOutOfBoundsException("OfflineProfiles == null");
		}

		return this.OfflineProfiles.getLength();
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

	public void deleteGhost(int index) throws Exception
	{
		this.changed = true;
		this.getGhost(index).delete();
		this.GhostElements.remove(index);

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
		this.GhostElements.add(ghost);
		this.TrainingNode.appendChild(this.document.importNode(ghost.getElement(), false));

		if(this.GhostElements.size() != this.TrainingGhosts.getLength())
		{
			throw new Exception(String.format("GhostElements(%d) != TrainingGhosts(%d)", this.GhostElements.size(), this.TrainingGhosts.getLength()));
		}

		return this.getGhostCount() - 1;
	}

	public String[] getProfiles() throws Exception
	{
		String[] profiles = new String[OfflineProfiles.getLength()];

		for(int i = 0; i < OfflineProfiles.getLength(); i++)
		{
			Element profile = (Element) OfflineProfiles.item(i);
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

		this.OfflineProfile = (Element) OfflineProfiles.item(this.profile);
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
		try
		{
			return FNX.getStringFromDOM(this.document, true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
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

// TODO: Und was ist mit dem DefaultProfile?
//       Das sollte als -1 verarbeitet werden!
// ...
