/**
 * gmHelper.java: GentleMagic / Greentube / HAPPYTEC
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class gmHelper
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

	private static DateFormat ResultFormat;

	public static String getWeather(int weatherType, boolean uppercase) throws gmException
	{
		String weatherString = getWeather(weatherType);

		if(uppercase)
		{
			weatherString = weatherString.toUpperCase();
		}

		return weatherString;
	}

	public static String getWeather(int weatherType) throws gmException
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
				throw new gmException(String.format("Invalid weather type: %d", weatherType));
		}

		return weatherString;
	}

	public static String getWeatherName(int weatherType) throws gmException
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
				throw new gmException(String.format("Invalid weather type: %d", weatherType));
		}

		return weatherName;
	}

	public static int parseWeather(String weatherString) throws gmException
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
				throw new gmException(String.format("Invalid weather string: %s", weatherString));
		}

		return weatherType;
	}

	// Die Kurzbezeichnungen der Strecken bleiben unverändert.
	// Diese sind auf HAPPYTEC sowieso in der DB vorhanden.
	// Es muss also nur eine Richtung implementiert werden.
	public static String getTrack(String trackKey) throws gmException
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
				throw new gmException(String.format("Invalid track key: %s", trackKey));
		}

		return trackName;
	}

	public static String getResult(int ms)
	{
		if(ResultFormat == null)
		{
			// Das HAPPYTEC-Format nutzt einen Beistrich.
			// Andere Implementierungen nutzen einen Punkt.
			ResultFormat = new SimpleDateFormat("mm:ss,SSS");
		}

		return ResultFormat.format(new Date(ms));
	}

	public static String formatSki(int[] ski)
	{
		if(ski == null || ski.length != 3)
		{
			return "??-??-??";
		}

		return String.format("%02d-%02d-%02d", ski[0], ski[1], ski[2]);
	}

	public static String[] getTracks()
	{
		return getTracks(false);
	}

	public static String[] getTracks(boolean lc)
	{
		String[] values = {
			"Bcr", "Gro", "Bor", "Wen", "Kiz", "Gar", "Mor",
			"Vdi", "Soc", "Schl"
		};

		if(lc)
		{
			for(int i = 0; i < values.length; i++)
			{
				values[i] = values[i].toLowerCase();
			}
		}

		return values;
	}

	public static int[] getWeatherIDs()
	{
		int[] values = {WEATHER_SUN, WEATHER_SNOW, WEATHER_ICE};

		return values;
	}
}
