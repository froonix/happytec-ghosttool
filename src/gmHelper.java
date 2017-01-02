

abstract class gmHelper
{
	// Interne Wetter-IDs von HAPPYTEC.
	// Die von GT waren immer schon anders!
	// Das ist leider historisch bedingt...
	public final static int WEATHER_NONE = 0;
	public final static int WEATHER_SUN  = 2;
	public final static int WEATHER_SNOW = 3;
	public final static int WEATHER_ICE  = 4;
	//                     QUALIFICATION = 1
	//                     RACE          = 5

	public static String getWeather(int weatherType, boolean uppercase)
	{
		String weatherString = getWeather(weatherType);

		if(uppercase)
		{
			weatherString = weatherString.toUpperCase();
		}

		return weatherString;
	}

	public static String getWeather(int weatherType)
	{
		String weatherString = "";

		switch(weatherType)
		{
			case WEATHER_SUN:
				weatherString = "sun";
				break;

			case WEATHER_SNOW:
				weatherString = "snow";
				break;

			case WEATHER_ICE:
				weatherString = "ice";
				break;

			default:
				System.err.printf("Invalid weather type: %d%n", weatherType);
				break;
		}

		return weatherString;
	}

	public static String getWeatherName(int weatherType)
	{
		String weatherName = "";

		switch(weatherType)
		{
			case WEATHER_SUN:
				weatherName = "Sonne";
				break;

			case WEATHER_SNOW:
				weatherName = "Schnee";
				break;

			case WEATHER_ICE:
				weatherName = "Eis";
				break;

			default:
				System.err.printf("Invalid weather type: %d%n", weatherType);
				break;
		}

		return weatherName;
	}

	public static int parseWeather(String weatherString)
	{
		int weatherType = WEATHER_NONE;

		switch(weatherString.toLowerCase())
		{
			case "sun":
				weatherType = WEATHER_SUN;
				break;

			case "snow":
				weatherType = WEATHER_SNOW;
				break;

			case "ice":
				weatherType = WEATHER_ICE;
				break;

			default:
				System.err.printf("Invalid weather string: %s%n", weatherString);
				break;
		}

		return weatherType;
	}

	// Die Kurzbezeichnungen der Strecken bleiben unverändert.
	// Diese sind auf HAPPYTEC sowieso in der DB vorhanden.
	// Es muss also nur eine Richtung implementiert werden.
	public static String getTrack(String trackKey)
	{
		String trackName = "";

		// TODO: Locales?
		// ...

		switch(trackKey.toLowerCase())
		{
			case "bcr":
				trackName = "Beaver Creek";
				break;

			case "gro":
				trackName = "Gröden";
				break;

			case "bor":
				trackName = "Bormio";
				break;

			case "wen":
				trackName = "Wengen";
				break;

			case "kiz":
				trackName = "Kitzbühel";
				break;

			case "gar":
				trackName = "Garmisch";
				break;

			case "mor":
				trackName = "St. Moritz";
				break;

			case "vdi":
				trackName = "Val d'Isère";
				break;

			case "soc":
				trackName = "Sotschi";
				break;

			case "schl":
				trackName = "Schladming";
				break;

			default:
				System.err.printf("Invalid track key: %s%n", trackKey);
				break;
		}

		return trackName;
	}
}
