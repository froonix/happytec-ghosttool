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

	private String Track;
	private int    Weather;
	private int    Time;
	private String GameMode;
	private String DataRaw;
	private byte[] DataBinary;
	private String Nickname;

	// TODO: UID?
	// ...

	GhostElement()
	{

	}

	GhostElement(Element xml)
	{
		importGhost(xml);
	}

	public void importGhost(Element xml)
	{
		if(GhostPattern == null)
		{
			this.GhostPattern = Pattern.compile("\062.\012.(.*?)\022\016");
		}

		this.Track = xml.getAttribute("Track");
		this.Weather = gmHelper.parseWeather(xml.getAttribute("Weather"));
		this.Time = Integer.parseInt(xml.getAttribute("Time"));
		this.GameMode = xml.getAttribute("GameMode");

		this.DataRaw = xml.getAttribute("Data");
		this.DataBinary = Base64.getDecoder().decode(this.DataRaw);

		// Normalerweise gehört das mit Google's Protocol Buffers extrahiert.
		// Ich habe aber keine Lust das zu implementieren, wenn es auch so geht.
		// Sonst kommen ein paar Scherzkekse noch auf blöde Ideen. Muss nicht sein.
		Matcher m = this.GhostPattern.matcher(new ByteCharSequence(this.DataBinary));

		if(m.find())
		{
			this.Nickname = "";
			for(int h = m.start() + 4; h < m.end() - 2; h++)
			{
				// Das ist höchst ineffizient! Sind aber nur maximal 20 Byte.
				this.Nickname = this.Nickname + Character.toString((char) DataBinary[h]);
			}
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
			// Das HAPPYTEC-Format nutzt ein en Beistrich.
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
		System.out.printf(" Track:   %s (%s)\n", this.getTrackName(), this.getTrack());
		System.out.printf(" Weather: %d (%s; %s)\n", this.getWeather(), gmHelper.getWeather(this.Weather), this.getWeatherName());
		// System.out.printf("\n%s\n", DataRaw);
		System.out.printf("--------------------------------\n");
	}
}
