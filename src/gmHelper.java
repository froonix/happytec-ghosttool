/**
 * gmHelper.java: GentleMagic / Greentube / HAPPYTEC
 * Copyright (C) 2018 Christian Schrötter <cs@fnx.li>
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
import java.util.ResourceBundle;
import java.util.Date;

public abstract class gmHelper
{
	// Interne Wetter-IDs von HAPPYTEC.
	// Die von GT waren immer schon anders!
	// Das ist leider historisch bedingt...
	public final static int WEATHER_NONE =  0;
	//    QUALIFICATION                  =  1
	public final static int WEATHER_SUN  =  2;
	public final static int WEATHER_SNOW =  3;
	public final static int WEATHER_ICE  =  4;
	public final static int WEATHER_RACE =  5;

	// Wird für eSportsAPI.FO_* benötigt.
	// Bitte niemals "-1" dafür verwenden!
	public final static int WEATHER_TICKET = -6;
	public final static int WEATHER_SUC    = -7;

	// ----------------------- //
	// GentleMagic Wetter IDs: //
	//   2=ice; 3=sun; 4=snow  //
	// ----------------------- //

	// Interne Minimode-IDs von HAPPYTEC.
	public final static int GAMEMODE_DEFAULT          = 0;
	public final static int GAMEMODE_MM_ROWDY         = 1;
	public final static int GAMEMODE_MM_TIMEATTACK    = 2;
	public final static int GAMEMODE_MM_ARCADE        = 3;
	public final static int GAMEMODE_MM_EXTREME       = 4;
	public final static int GAMEMODE_MM_EXTREMEICE    = 5; // ACHTUNG: Nur bei Eis verfügbar!
	public final static int GAMEMODE_MM_LASTDOWNSWING = 6;

	// ----------------------------------- //
	// GentleMagic Spielmodus IDs:         //
	//   1=default; 4=rowdy; 5=extremeice  //
	//   6=timeattack; 8=arcade; 9=extreme //
	//   ???=lastdownswing                 //
	// ----------------------------------- //

	private static DateFormat ResultFormat;
	private static ResourceBundle lang;
	private static String[] TrackKeys;

	public static boolean isReverseGameMode(int gameModeType)
	{
		return (gameModeType == GAMEMODE_MM_TIMEATTACK);
	}

	public static String getGameMode(int gameModeType, boolean uppercase) throws gmException
	{
		String gameModeString = getGameMode(gameModeType);

		if(uppercase)
		{
			gameModeString = gameModeString.toUpperCase();
		}

		return gameModeString;
	}

	public static String getGameMode(int gameModeType) throws gmException
	{
		String gameModeString = "";

		switch(gameModeType)
		{
			case GAMEMODE_DEFAULT:
				gameModeString = "default";
				break;

			case GAMEMODE_MM_ROWDY:
				gameModeString = "minimode_rowdy";
				break;

			case GAMEMODE_MM_TIMEATTACK:
				gameModeString = "minimode_timeattack";
				break;

			case GAMEMODE_MM_ARCADE:
				gameModeString = "minimode_arcade";
				break;

			case GAMEMODE_MM_EXTREME:
				gameModeString = "minimode_extreme";
				break;

			case GAMEMODE_MM_EXTREMEICE:
				gameModeString = "minimode_extremeice";
				break;

			case GAMEMODE_MM_LASTDOWNSWING:
				gameModeString = "minimode_lastdownswing";
				break;

			default:
				throw new gmException(String.format("Invalid game mode type: %d", gameModeType));
		}

		return gameModeString;
	}

	public static String getGameModeName(int gameModeType) throws gmException
	{
		String gameModeName = getGameMode(gameModeType);

		if(getLangBundle().containsKey(gameModeName))
		{
			return FNX.getLangString(getLangBundle(), gameModeName);
		}

		throw new gmException(String.format("Invalid game mode type: %d", gameModeType));
	}

	public static int parseGameMode(String gameModeString) throws gmException
	{
		int gameModeType = -1;

		switch(gameModeString.toLowerCase())
		{
			case "default":
				gameModeType = GAMEMODE_DEFAULT;
				break;

			case "minimode_rowdy":
				gameModeType = GAMEMODE_MM_ROWDY;
				break;

			case "minimode_timeattack":
				gameModeType = GAMEMODE_MM_TIMEATTACK;
				break;

			case "minimode_arcade":
				gameModeType = GAMEMODE_MM_ARCADE;
				break;

			case "minimode_extreme":
				gameModeType = GAMEMODE_MM_EXTREME;
				break;

			case "minimode_extremeice":
				gameModeType = GAMEMODE_MM_EXTREMEICE;
				break;

			case "minimode_lastdownswing":
				gameModeType = GAMEMODE_MM_LASTDOWNSWING;
				break;

			default:
				throw new gmException(String.format("Invalid game mode string: %s", gameModeString));
		}

		return gameModeType;
	}

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

			case WEATHER_RACE:
				weatherString = "race";
				break;

			case WEATHER_TICKET:
				weatherString = "xtc";
				break;

			case WEATHER_SUC:
				weatherString = "suc";
				break;

			default:
				throw new gmException(String.format("Invalid weather type: %d", weatherType));
		}

		return weatherString;
	}

	public static String getWeatherName(int weatherType) throws gmException
	{
		String weatherName = String.format("weather_%s", getWeather(weatherType));

		if(getLangBundle().containsKey(weatherName))
		{
			return FNX.getLangString(getLangBundle(), weatherName);
		}

		throw new gmException(String.format("Invalid weather type: %d", weatherType));
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

			case "race":
				weatherType = WEATHER_RACE;
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
		String trackName = String.format("track_%s", trackKey.toLowerCase());

		if(getLangBundle().containsKey(trackName))
		{
			return FNX.getLangString(getLangBundle(), trackName);
		}

		throw new gmException(String.format("Invalid track key: %s", trackKey));
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
		return formatSki(ski, false);
	}

	public static String formatSki(int[] ski, boolean raw)
	{
		if(ski == null || ski.length != 3)
		{
			return "??-??-??";
		}
		else if(raw || ski[0] == 100 || ski[1] == 100 || ski[2] == 100)
		{
			return String.format("%d-%d-%d", ski[0], ski[1], ski[2]);
		}
		else
		{
			return String.format("%02d-%02d-%02d", ski[0], ski[1], ski[2]);
		}
	}

	public static String[] getTracks()
	{
		return getTracks(false);
	}

	public static String[] getTracks(boolean lc)
	{
		return getTracksByGameMode(GAMEMODE_DEFAULT, lc, false);
	}

	public static String[] getTracksByGameMode(int mode)
	{
		return getTracksByGameMode(mode, false, false);
	}

	public static String[] getTracksByGameMode(int mode, boolean lc, boolean filtered)
	{
		String[] values;

		if(mode == GAMEMODE_MM_TIMEATTACK || mode == GAMEMODE_MM_ARCADE)
		{
			values = new String[]{
				"Bcr", "Gro", "Bor", "Wen", "Kiz", "Gar", /*"Mor",*/
				"Vdi", "Soc", "Schl"
			};
		}
		else
		{
			values = new String[]{
				"Bcr", "Gro", "Bor", "Wen", "Kiz", "Gar", "Mor",
				"Vdi", "Soc", "Schl"
			};
		};

		if(lc)
		{
			for(int i = 0; i < values.length; i++)
			{
				values[i] = values[i].toLowerCase();
			}
		}

		if(filtered && TrackKeys != null && TrackKeys.length > 0)
		{
			int trackCount = TrackKeys.length;
			String tmp[] = new String[trackCount];

			for(int i = 0; i < TrackKeys.length; i++)
			{
				boolean trackFound = false;
				for(int h = 0; h < values.length; h++)
				{
					if((!lc && TrackKeys[i].toLowerCase().equals(values[h])) || (lc && TrackKeys[i].equals(values[h])))
					{
						trackFound = true;
						break;
					}
				}

				if(trackFound)
				{
					tmp[i] = TrackKeys[i];
				}
				else
				{
					trackCount--;
				}
			}

			if(trackCount != tmp.length)
			{
				values = new String[trackCount];

				for(int i = 0, h = 0; i < tmp.length; i++)
				{
					if(tmp[i] != null)
					{
						values[h++] = tmp[i];
					}
				}
			}
			else
			{
				values = tmp;
			}
		}

		return values;
	}

	public static int[] getWeatherIDs()
	{
		int[] values = {WEATHER_SUN, WEATHER_SNOW, WEATHER_ICE};

		return values;
	}

	public static int[] getWeatherIDs(boolean race)
	{
		return getWeatherIDs(race, false);
	}

	public static int[] getWeatherIDs(boolean race, boolean ticket)
	{
		return getWeatherIDs(race, ticket, false);
	}

	public static int[] getWeatherIDs(boolean race, boolean ticket, boolean suc)
	{
		if(race)
		{
			int[] values;

			// TODO: Use ArrayList and convert it to an array at the end!
			// ...

			if(ticket && suc)
			{
				values = new int[] {WEATHER_SUN, WEATHER_SNOW, WEATHER_ICE, WEATHER_RACE, WEATHER_TICKET, WEATHER_SUC};
			}
			else if(!ticket && suc)
			{
				values = new int[] {WEATHER_SUN, WEATHER_SNOW, WEATHER_ICE, WEATHER_RACE, WEATHER_SUC};
			}
			else if(ticket && !suc)
			{
				values = new int[] {WEATHER_SUN, WEATHER_SNOW, WEATHER_ICE, WEATHER_RACE, WEATHER_TICKET};
			}
			else
			{
				values = new int[] {WEATHER_SUN, WEATHER_SNOW, WEATHER_ICE, WEATHER_RACE};
			}

			return values;
		}

		return getWeatherIDs();
	}

	public static int[] getGameModeIDs()
	{
		int[] values = {GAMEMODE_DEFAULT, GAMEMODE_MM_ROWDY, GAMEMODE_MM_TIMEATTACK, GAMEMODE_MM_ARCADE, GAMEMODE_MM_EXTREME, GAMEMODE_MM_EXTREMEICE /*, GAMEMODE_MM_LASTDOWNSWING*/};

		return values;
	}

	private static ResourceBundle getLangBundle()
	{
		if(lang == null)
		{
			lang = FNX.getLangBundle("gmHelper");
		}

		return lang;
	}

	public static void setTrackOrder(String[] tracks) throws Exception
	{
		TrackKeys = new String[tracks.length];

		for(int i = 0; i < tracks.length; i++)
		{
			// Simple check...
			getTrack(tracks[i]);

			TrackKeys[i] = tracks[i].toLowerCase();
		}
	}
}
