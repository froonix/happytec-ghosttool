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

	public static void main(String[] args) throws Exception
	{
		// OfflineProfiles test = new OfflineProfiles("<test>du</test>");
		OfflineProfiles test = new OfflineProfiles(new File("./example/test.xml"));

		test.test();

		/*
		Thread.sleep(5000);
		test.reload();
		test.test();
		*/
	}

	public void test() throws Exception
	{
		/*
		for(int i = 0; i < this.getGhostCount(); i++)
		{
			System.out.printf("%s\n", this.GhostElements.get(i).getResult());
		}
		*/

		// this.deleteGhost(this.getGhostCount() - 1);
		// this.addGhost("<GhostDataPair Data=\"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\" GameMode=\"DEFAULT\" Time=\"127385\" Track=\"Bor\" Weather=\"SUN\" />");

		// System.out.printf("%s", this.toString());

		String[] tmp = this.getProfiles();
		for(int i = 0; i < tmp.length; i++)
		{
			System.out.printf("[%02d] %s\n", i, tmp[i]);
		}
	}

	/*
	public OfflineProfiles(InputSource xmlsource) throws Exception
	{
		this.file = null;
		this.document = getParser().parse(xmlsource);
		this.postParsing();
	}
	*/

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

// TODO: XML-Parser-Fehler abfangen?
//       org.xml.sax.SAXParseException
// ...

// TODO: Und was ist mit dem DefaultProfile?
//       Das sollte als -1 verarbeitet werden!
// ...
