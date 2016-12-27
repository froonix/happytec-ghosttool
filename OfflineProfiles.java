import java.io.*;
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

	private static DocumentBuilderFactory dbFactory;
	private static DocumentBuilder        dBuilder;

	private File     file     = null;
	private Document document = null;
	private boolean  changed  = false;
	private int      profile  = 0;

	private NodeList       OfflineProfiles;
	private Element        OfflineProfile;
	private Element        TrainingGhosts;
	private NodeList       TrainingGhost;
	private GhostElement[] GhostElements;

	public static void main(String[] args) throws Exception
	{
		// OfflineProfiles test = new OfflineProfiles("<test>du</test>");
		OfflineProfiles test = new OfflineProfiles(new File("./example/OfflineProfiles.error.xml"));

		test.test();
		Thread.sleep(5000);
		test.reload();
		test.test();
	}

	public void test() throws Exception
	{
		for(int i = 0; i < this.getGhostCount(); i++)
		{
			System.out.printf("%s\n", this.GhostElements[i].getResult());
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

		this.TrainingGhosts  = null;
		this.OfflineProfile  = null;
		this.OfflineProfiles = document.getElementsByTagName(this.XML_TAG_PROFILE);

		if(this.getProfileCount() == 0)
		{
			throw new Exception("Missing <" + XML_TAG_PROFILE + "> tag");
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
		if(this.TrainingGhost != null)
		{
			return this.TrainingGhost.getLength();
		}

		return 0;
	}

	public void selectProfile(int index)
	{
		if(index > (this.getProfileCount() - 1))
		{
			throw new IndexOutOfBoundsException(String.format("OfflineProfile #%d", index));
		}

		this.profile = index;
		this.GhostElements = null;
		this.TrainingGhosts = null;

		this.OfflineProfile = (Element) OfflineProfiles.item(this.profile);
		NodeList GhostNodes = this.OfflineProfile.getElementsByTagName(this.XML_TAG_GHOSTS);

		if(GhostNodes.getLength() > 0)
		{
			this.TrainingGhosts = (Element) GhostNodes.item(0);
			this.TrainingGhost = this.TrainingGhosts.getElementsByTagName(this.XML_TAG_GHOST);

			if(this.getGhostCount() > 0)
			{
				this.GhostElements = new GhostElement[this.getGhostCount()];

				for(int i = 0; i < this.getGhostCount(); i++)
				{
					// Damit ein Geist nur einmal verarbeitet wird, werden alle bereits hier eingelesen.
					// Somit erfolgt nur noch beim Profilwechsel eine aufwändige erneute Verarbeitung.
					// Das ist allerdings gewollt, damit nicht unnötig Arbeitsspeicher belegt wird.
					this.GhostElements[i] = new GhostElement((Element) this.TrainingGhost.item(i));
				}
			}
		}
	}
}

// TODO: XML-Parser-Fehler abfangen?
//       org.xml.sax.SAXParseException
// ...

// TODO: Und was ist mit dem DefaultProfile?
//       Das sollte als -1 verarbeitet werden!
// ...
