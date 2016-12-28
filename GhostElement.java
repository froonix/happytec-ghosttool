import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.io.StringReader;

import java.util.Base64;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.w3c.dom.Element;

public class GhostElement
{
	private static Pattern GhostPattern;
	private static DateFormat ResultFormat;

	private Element XML;
	private String  Track;
	private int     Weather;
	private int     Time;
	private String  GameMode;
	private String  DataRaw;
	private byte[]  DataBinary;
	private String  Nickname;

	// TODO: UID?
	// ...

	GhostElement()
	{

	}

	GhostElement(Element xml)
	{
		importGhost(xml);
	}

	GhostElement(String xml)
	{
		importGhost(xml);
	}

	public Element getElement()
	{
		return this.XML;
	}

	// todo: regex für mehrere geister! (static)
	// siehe code in htgt.ghostInput
	// ...

	public void importGhost(String xml)
	{
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
			NodeList gdp = doc.getElementsByTagName("GhostDataPair");
			importGhost((Element) gdp.item(0));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void importGhost(Element xml)
	{
		this.XML = xml;

		if(GhostPattern == null)
		{
			// this.GhostPattern = Pattern.compile("\062.\012.(.+)\022\016");
			this.GhostPattern = Pattern.compile("\062.\012.(.{1,32})\022\016(.{1,32})\030.\100.$", Pattern.DOTALL);
		}

		this.Track = xml.getAttribute("Track");
		this.Weather = gmHelper.parseWeather(xml.getAttribute("Weather"));
		this.Time = Integer.parseInt(xml.getAttribute("Time"));
		this.GameMode = xml.getAttribute("GameMode");

		this.DataRaw = xml.getAttribute("Data");
		this.DataBinary = Base64.getDecoder().decode(this.DataRaw);

		try
		{
			// Normalerweise gehört das mit Google's Protocol Buffers extrahiert.
			// Ich habe aber keine Lust das zu implementieren, wenn es auch so geht.
			// Matcher m = this.GhostPattern.matcher(new ByteCharSequence(this.DataBinary));
			Matcher m = this.GhostPattern.matcher(new String(this.DataBinary, "ISO-8859-1"));

			if(m.find())
			{
				/*
				StringBuilder str = new StringBuilder();
				for(int h = m.start() + 4; h < m.end() - 2; h++)
				{
					str.append(Character.toString((char) DataBinary[h]));
				}
				this.Nickname = str.toString();
				*/

				this.Nickname = m.group(1);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// TODO: Fehlerüberprüfung!
		// ...
	}

	public String getNickname()
	{
		return this.Nickname;
	}

	public String getTrack()
	{
		return this.Track;
	}

	public String getTrackName()
	{
		return gmHelper.getTrack(this.Track);
	}

	public int getWeather()
	{
		return this.Weather;
	}

	public String getWeatherName()
	{
		return gmHelper.getWeatherName(this.Weather);
	}

	public int getTime()
	{
		return this.Time;
	}

	public String getResult()
	{
		if(this.ResultFormat == null)
		{
			// Das HAPPYTEC-Format nutzt einen Beistrich.
			// Andere Implementierungen nutzen einen Punkt.
			this.ResultFormat = new SimpleDateFormat("mm:ss,SSS");
		}

		return this.ResultFormat.format(new Date(this.Time));
	}

	public void printDetails()
	{
		System.out.printf("--------------------------------\n");
		System.out.printf(" Nick:    %s\n", this.getNickname());
		// System.out.printf(" Mode:    %s\n", this.GameMode);
		System.out.printf(" Time:    %s (%d)\n", this.getResult(), this.getTime());
		System.out.printf(" Track:   [%s] %s\n", this.getTrack().toUpperCase(), this.getTrackName());
		System.out.printf(" Weather: [%s] %s (%d)\n", gmHelper.getWeather(this.Weather).toUpperCase(), this.getWeatherName(), this.getWeather());
		// System.out.printf("\n%s\n", DataRaw);
		System.out.printf("--------------------------------\n");
	}

	public String toString()
	{
		try
		{
			return FNX.getStringFromDOM(this.XML, false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public void delete()
	{
		this.XML.getParentNode().removeChild(this.XML);
	}
}
