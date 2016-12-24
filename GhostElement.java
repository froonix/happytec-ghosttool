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

	String TrackKey;
	String TrackName;

	String WeatherString;
	int    WeatherType;
	String WeatherName;

	String DataRaw;
	byte[] DataBinary;

	String GameMode;
	String Nickname;

	int    Time;
	String Result;

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
			GhostPattern = Pattern.compile("\062.\012.(.*?)\022\016");
		}

		if(ResultFormat == null)
		{
			ResultFormat = new SimpleDateFormat("mm:ss,SSS");
		}

		GameMode = xml.getAttribute("GameMode");

		TrackKey = xml.getAttribute("Track");
		TrackName = gmHelper.getTrack(TrackKey);

		WeatherString = xml.getAttribute("Weather");
		WeatherType = gmHelper.parseWeather(WeatherString);
		WeatherName = gmHelper.getWeatherName(WeatherType);

		Time = Integer.parseInt(xml.getAttribute("Time"));
		Result = ResultFormat.format(new Date(Time));

		DataRaw = xml.getAttribute("Data");
		DataBinary = Base64.getDecoder().decode(DataRaw);

		// Normalerweise gehört das mit Google's Protocol Buffers extrahiert.
		// Ich habe aber keine Lust das zu implementieren, wenn es auch so geht.
		// Sonst kommen ein paar Scherzkekse noch auf blöde Ideen. Muss nicht sein.
		String BinaryString = new String(DataBinary, StandardCharsets.UTF_8);
		Matcher m = GhostPattern.matcher(new ByteCharSequence(DataBinary));

		if(m.find())
		{
			Nickname = "";
			for(int h = m.start() + 4; h < m.end() - 2; h++)
			{
				// Das ist höchst ineffizient! Sind aber nur maximal 20 Byte.
				Nickname = Nickname + Character.toString((char) DataBinary[h]);
			}
		}

		// TODO: Fehlerüberprüfung!
		// ...
	}

	public String getNickname()
	{
		return Nickname;
	}

	public String getTrackName()
	{
		// TODO: On-the-fly?
		return TrackName;
	}

	public String getWeatherName()
	{
		// TODO: On-the-fly?
		return WeatherName;
	}

	public int getTime()
	{
		return Time;
	}

	public String getResult()
	{
		// TODO: On-the-fly?
		return Result;
	}

	public void printDetails()
	{
		System.out.printf("--------------------------------\n");
		System.out.printf(" Nick:    %s\n", Nickname);
		System.out.printf(" Mode:    %s\n", GameMode);
		System.out.printf(" Time:    %s (%d)\n", Result, Time);
		System.out.printf(" Track:   %s (%s)\n", TrackName, TrackKey);
		System.out.printf(" Weather: %d (%s; %s)\n", WeatherType, WeatherString, WeatherName);
		// System.out.printf("\n%s\n", DataRaw);
		System.out.printf("--------------------------------\n");
	}
}
