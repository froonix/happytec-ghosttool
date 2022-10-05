/**
 * eSportsAPI.java: HAPPYTEC-eSports-API interface
 * Copyright (C) 2016-2021 Christian Schrötter <cs@fnx.li>
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.lang.IndexOutOfBoundsException;
import java.lang.NullPointerException;
import java.lang.RuntimeException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import java.security.cert.Certificate;

import java.text.ParseException;

import java.time.Instant;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class eSportsAPI
{
	private final static String API_VERSION  = "1.0";
	private final static String API_REQUEST  = "%s/%s/%s/%s";
	private final static String API_MAINURL  = "%s://%s/api";
	private final static String API_PROTOCOL = "https";
	private final static String API_HOST     = "htgt.app";
	private final static int    API_TIMEOUT  = 10000;

	private static boolean verify = true;
	private static String protocol;
	private static String host;

	private String token;
	private String useragent;
	private String osdata;

	private ArrayList<HashMap<String,Object>> lastResultDestinations;
	private final static int RESULT_TYPE_NEXT = 0;
	private final static int RESULT_TYPE_PREV = 1;
	private int[] lastTypeIndex = new int[2];

	private String serverMessageText;
	private int serverMessageVersion;

	public final static int FOS       =  3;
//	public final static int FO_REV    = -1;
	public final static int FO_NONE   =  0;
	public final static int FO_TICKET =  1;
	public final static int FO_SUC    =  2;
//	public final static int FO_ALL    =  3;

	private static String[] serverTracks; // = new String[0];

	private static HostnameVerifier HV = HttpsURLConnection.getDefaultHostnameVerifier();
	private static InsecureHostnameVerifier IHV = new InsecureHostnameVerifier();
	private Certificate[] lastCertificateChain;

	public eSportsAPI(String token)
	{
		this.setToken(token);
		this.resetLastServerMessage();
	}

	public eSportsAPI(String token, String useragent)
	{
		this.setToken(token);
		this.setUseragent(useragent);
		this.resetLastServerMessage();
	}

	public static void enableVerification()
	{
		verify = true;
	}

	public static void disableVerification()
	{
		verify = false;
	}

	public static String getDefaultProtocol()
	{
		return API_PROTOCOL;
	}

	public static String getDefaultHost()
	{
		return API_HOST;
	}

	public static void setProtocol(String proto)
	{
		if(proto == null)
		{
			protocol = getDefaultProtocol();
		}
		else if(!Pattern.matches("^(?i)http[s]?$", proto))
		{
			throw new IllegalArgumentException("Invalid API protocol (not HTTP/HTTPS)");
		}
		else
		{
			protocol = proto;
		}
	}

	public static void setHost(String address)
	{
		if(address == null)
		{
			host = getDefaultHost();
		}
		else if(!Pattern.matches("^(?i)([A-Z0-9-]+\\.)+[A-Z]+(:[0-9]+)?$", address))
		{
			throw new IllegalArgumentException("Invalid API adress (<FQDN> or <FQDN:<PORT> required>)");
		}
		else
		{
			host = address;
		}
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public void setUseragent(String useragent)
	{
		this.useragent = useragent;
	}

	public String[] getServerTracks()
	{
		if(serverTracks == null)
		{
			return new String[0];
		}

		return serverTracks;
	}

	public int getNextResultIndex()
	{
		return this.lastTypeIndex[RESULT_TYPE_NEXT];
	}

	public int getPrevResultIndex()
	{
		return this.lastTypeIndex[RESULT_TYPE_PREV];
	}

	public ArrayList<HashMap<String,Object>> getLastResultDestinations()
	{
		return this.lastResultDestinations;
	}

	public GhostElement getGhostByID(int id) throws eSportsAPIException
	{
		try
		{
			Map<String,Object> args = new HashMap<>();
			args.put("byID", id);

			return new GhostElement(this.request("OFFLINE", "ghost.get", args));
		}
		catch(GhostException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public GhostElement[] getGhostsByIDs(int[] ids) throws eSportsAPIException
	{
		try
		{
			Map<String,Object> args = new HashMap<>();
			StringBuilder value = new StringBuilder();

			for(int i = 0; i < ids.length; i++)
			{
				if(i > 0)
				{
					// Die API erlaubt beliebige nicht-numerische Trennzeichen.
					// Durch den Unterstrich (statt einem Beistrich o.ä.) werden
					// zwei Byte pro Geist eingespart, da die Kodierung entfällt.
					value.append("_");
				}

				value.append(String.format("%d", ids[i]));
			}

			args.put("byIDs", value.toString());
			String result = this.request("OFFLINE", "ghost.get", args);

			return GhostElement.parseGhosts(result);
		}
		catch(GhostException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public GhostElement[] getGhostsByIDs(Integer[] ids) throws eSportsAPIException
	{
		return getGhostsByIDs(Arrays.stream(ids).mapToInt(Integer::intValue).toArray());
	}

	public int[] getGhostIDs(GhostElement[] ghosts) throws eSportsAPIException
	{
		return getSimpleGhostIDs(ghosts);
	}

	public int[] getSimpleGhostIDs(GhostElement[] ghosts) throws eSportsAPIException
	{
		try
		{
			Map<String,Object> args = new HashMap<>();
			StringBuilder data = new StringBuilder();

			for(int i = 0; i < ghosts.length; i++)
			{
				data.append(ghosts[i].toString());
			}

			args.put("XML", data.toString());
			String result = this.request("OFFLINE", "ghost.put", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList GhostNodes = doc.getElementsByTagName("Ghost");
			int[] ghostIDs = new int[GhostNodes.getLength()];

			for(int i = 0; i < GhostNodes.getLength(); i++)
			{
				Element ghost = (Element) GhostNodes.item(i);
				ghostIDs[i] = Integer.parseInt(ghost.getAttribute("ID"));
			}

			return ghostIDs;
		}
		catch(SAXException|ParserConfigurationException|IOException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public List<Map<String,Object>> getExtendedGhostIDs(GhostElement[] ghosts) throws eSportsAPIException
	{
		// TODO: Cache implementieren?
		// ...

		try
		{
			Map<String,Object> args = new HashMap<>();
			StringBuilder data = new StringBuilder();

			for(int i = 0; i < ghosts.length; i++)
			{
				data.append(ghosts[i].toString());
			}

			args.put("XML", data.toString());
			args.put("Applicable", "true");

			String result = this.request("OFFLINE", "ghost.put", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList GhostNodes = doc.getElementsByTagName("Ghost");

			if(ghosts.length != GhostNodes.getLength())
			{
				throw new eSportsAPIException("SERVER_DUMB");
			}

			List<Map<String,Object>> info = new ArrayList<>(GhostNodes.getLength());

			for(int i = 0; i < GhostNodes.getLength(); i++)
			{
				Element ghost = (Element) GhostNodes.item(i);
				Map<String,Object> hm = new HashMap<>(3);

				hm.put("GhostID", Integer.parseInt(ghost.getAttribute("ID")));
				hm.put("Applicable", (!ghost.hasAttribute("Applicable") || !ghost.getAttribute("Applicable").equalsIgnoreCase("false")) ? 1 : 0);

				if(!ghost.hasAttribute("FO"))
				{
					hm.put("FilterOption", ghosts[i].hasTicket() ? FO_TICKET : FO_NONE);
				}
				else
				{
					hm.put("FilterOption", FNX.intval(ghost.getAttribute("FO")));
				}

				info.add(i, hm);
			}

			return info;
		}
		catch(SAXException|ParserConfigurationException|IOException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public boolean applyResultByGhostID(int ghostID) throws eSportsAPIException
	{
		try
		{
			Map<String,Object> args = new HashMap<>();
			args.put("ghostID", Integer.toString(ghostID));
			String result = this.request("OFFLINE", "result.apply", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList GhostNodes = doc.getElementsByTagName("Ghost");

			if(GhostNodes.getLength() > 0)
			{
				Element ghost = (Element) GhostNodes.item(0);
				int id = Integer.parseInt(ghost.getAttribute("ID"));

				if(id == ghostID)
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				throw new ParserConfigurationException("Missing <Ghost> tag in reply");
			}
		}
		catch(SAXException|ParserConfigurationException|IOException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public int applyResultByGhostIDExtended(int ghostID) throws eSportsAPIException
	{
		lastResultDestinations = null;

		try
		{
			Map<String,Object> args = new HashMap<>();
			args.put("ghostID", Integer.toString(ghostID));
			args.put("includePosition", "true");

			String result = this.request("OFFLINE", "result.apply", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList GhostNodes = doc.getElementsByTagName("Ghost");

			if(GhostNodes.getLength() > 0)
			{
				Element ghost = (Element) GhostNodes.item(0);
				int id = Integer.parseInt(ghost.getAttribute("ID"));

				if(id == ghostID)
				{
					NodeList position = doc.getElementsByTagName("ExpectedPosition");
					NodeList destinationsNode = doc.getElementsByTagName("ResultDestinations");

					if(destinationsNode.getLength() > 0)
					{
						Element destinationsElement = (Element) destinationsNode.item(0);
						NodeList destinationsList = destinationsElement.getElementsByTagName("ResultDestination");
						lastResultDestinations = new ArrayList<>(destinationsList.getLength());

						for(int i = 0; i < destinationsList.getLength(); i++)
						{
							String t, n; t = n = null;
							boolean de, ae, ad, pt, r, x, s; de = ae = ad = pt = r = x = s = false;
							int tID, b, a, gID, or, op, nr, np, g, w; tID = gID = b = a = or = op = nr = np = g = w = -1;
							HashMap<String,Object> item = new HashMap<>();

							Element destination = (Element) destinationsList.item(i);
							NodeList trackNode = destination.getElementsByTagName("Track");
							NodeList groupNode = destination.getElementsByTagName("Group");
							NodeList stateNode = destination.getElementsByTagName("ResultState");
							NodeList oldResultNode = destination.getElementsByTagName("OldResult");
							NodeList newResultNode = destination.getElementsByTagName("NewResult");

							if(stateNode.getLength() > 0)
							{
								Element stateElement = (Element) stateNode.item(0);
								NodeList duplicate = stateElement.getElementsByTagName("Duplicate");
								NodeList applicable = stateElement.getElementsByTagName("Applicable");
								NodeList applied = stateElement.getElementsByTagName("Applied");

								if(duplicate.getLength() > 0 && duplicate.item(0).getTextContent().equalsIgnoreCase("true"))
								{
									de = true;
								}

								if(applicable.getLength() > 0 && applicable.item(0).getTextContent().equalsIgnoreCase("true"))
								{
									ae = true;
								}

								if(applied.getLength() > 0 && applied.item(0).getTextContent().equalsIgnoreCase("true"))
								{
									ad = true;
								}
							}

							if(oldResultNode.getLength() > 0)
							{
								Element oldResultElement = (Element) oldResultNode.item(0);
								NodeList oldResult = oldResultElement.getElementsByTagName("Result");
								NodeList oldPosition = oldResultElement.getElementsByTagName("Position");

								if(oldResult.getLength() > 0 && oldPosition.getLength() > 0)
								{
									or = FNX.intval(oldResult.item(0).getTextContent(), true);
									op = FNX.intval(oldPosition.item(0).getTextContent(), true);
								}
							}

							if(newResultNode.getLength() > 0)
							{
								Element newResultElement = (Element) newResultNode.item(0);
								NodeList newResult = newResultElement.getElementsByTagName("Result");
								NodeList newPosition = newResultElement.getElementsByTagName("Position");

								if(newResult.getLength() > 0 && newPosition.getLength() > 0)
								{
									nr = FNX.intval(newResult.item(0).getTextContent(), true);
									np = FNX.intval(newPosition.item(0).getTextContent(), true);
								}
							}

							if(trackNode.getLength() > 0)
							{
								Element trackElement = (Element) trackNode.item(0);
								NodeList trackID = trackElement.getElementsByTagName("TrackID");
								NodeList begin = trackElement.getElementsByTagName("Begin");
								NodeList end = trackElement.getElementsByTagName("End");
								t = trackElement.getAttribute("Track");

								try
								{
									g = gmHelper.parseGameMode(trackElement.getAttribute("GameMode"));
									w = gmHelper.parseWeather(trackElement.getAttribute("Weather"));
								}
								catch(gmException e)
								{
									throw new eSportsAPIException(e);
								}

								if(trackElement.getAttribute("Race").equalsIgnoreCase("true"))
								{
									r = true;
								}

								if(trackElement.getAttribute("Ticket").equalsIgnoreCase("true"))
								{
									x = true;
								}

								if(trackElement.getAttribute("SUC").equalsIgnoreCase("true"))
								{
									s = true;
								}

								if(trackElement.getAttribute("PT").equalsIgnoreCase("true") && groupNode.getLength() > 0)
								{
									Element groupElement = (Element) groupNode.item(0);
									NodeList name = groupElement.getElementsByTagName("Name");
									NodeList groupID = groupElement.getElementsByTagName("GroupID");

									if(groupID.getLength() > 0)
									{
										gID = Integer.parseInt(groupID.item(0).getTextContent());
									}

									if(name.getLength() > 0)
									{
										n = name.item(0).getTextContent();
									}

									pt = true;
								}

								if(trackID.getLength() > 0)
								{
									tID = Integer.parseInt(trackID.item(0).getTextContent());
								}

								if(begin.getLength() > 0)
								{
									b = Integer.parseInt(begin.item(0).getTextContent());
								}

								if(end.getLength() > 0)
								{
									a = Integer.parseInt(end.item(0).getTextContent());
								}
							}

							if(tID < 0)
							{
								throw new ParserConfigurationException("Missing or invalid <TrackID> tag in reply");
							}

							item.put("GhostID", ghostID);
							item.put("Time", Instant.now().toEpochMilli() / 1000);
							item.put("Duplicate", de);
							item.put("Applicable", ae);
							item.put("Applied", ad);

							item.put("TrackID", tID);
							item.put("Begin", b);
							item.put("End", a);

							item.put("GameMode", g);
							item.put("Track", t);
							item.put("Weather", w);

							item.put("Race", r);
							item.put("Ticket", x);
							item.put("SUC", s);
							item.put("PT", pt);

							item.put("GroupID", gID);
							item.put("GroupName", n);

							item.put("OldResult", or);
							item.put("OldPosition", op);

							item.put("NewResult", nr);
							item.put("NewPosition", np);

							//FNX.dbgf("item = %s", item.toString());
							lastResultDestinations.add(i, item);
						}
					}

					if(position.getLength() > 0)
					{
						return Integer.parseInt(position.item(0).getTextContent());
					}
					else
					{
						return 0;
					}
				}
				else
				{
					return -1;
				}
			}
			else
			{
				throw new ParserConfigurationException("Missing <Ghost> tag in reply");
			}
		}
		catch(SAXException|ParserConfigurationException|IOException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public Map<String,Object> getPlayerInfo() throws eSportsAPIException
	{
		try
		{
			Map<String,Object> values = new HashMap<>();
			String result = this.request("OFFLINE", "player.info", null);

			Document doc = FNX.getDOMDocument(result);
			NodeList OfflinePlayer = doc.getElementsByTagName("OfflinePlayer");

			if(OfflinePlayer.getLength() > 0)
			{
				Element OfflinePlayerElement = (Element) OfflinePlayer.item(0);
				// int id = Integer.parseInt(OfflinePlayerElement.getAttribute("ID"));

				values.put("Nickname", OfflinePlayerElement.getElementsByTagName("Nickname").item(0).getTextContent());
				values.put("Useraccount", OfflinePlayerElement.getElementsByTagName("Username").item(0).getTextContent());
				// values.put("CompetitionKey", OfflinePlayerElement.getElementsByTagName("Key").item(0).getTextContent());
				values.put("CompetitionName", OfflinePlayerElement.getElementsByTagName("Title").item(0).getTextContent());

				return values;
			}
			else
			{
				throw new ParserConfigurationException("Missing <OfflinePlayer> tag in reply");
			}
		}
		catch(SAXException|ParserConfigurationException|IOException|NullPointerException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public List<Map<String,Object>> getResultsByCondition(String mode, String track, String weather) throws eSportsAPIException
	{
		return this.getResultsByCondition(mode, track, weather, false);
	}

	public List<Map<String,Object>> getResultsByCondition(String mode, String track, String weather, boolean forceWeather) throws eSportsAPIException
	{
		try
		{
			return this.getResultsByCondition(gmHelper.parseGameMode(mode), track, gmHelper.parseWeather(weather), forceWeather);
		}
		catch(gmException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public List<Map<String,Object>> getResultsByCondition(int mode, String track, int weather) throws eSportsAPIException
	{
		return this.getResultsByCondition(mode, track, weather, false);
	}

	public List<Map<String,Object>> getResultsByCondition(int mode, String track, int weather, boolean forceWeather) throws eSportsAPIException
	{
		try
		{
			Map<String,Object> args = new HashMap<>();
			args.put("byGameModeID", mode);
			args.put("byTrack", track);
			args.put("byWeatherID", weather);
			args.put("forceWeather", (forceWeather) ? 1 : 0);
			String result = this.request("OFFLINE", "result.get", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList OfflineResults = doc.getElementsByTagName("OfflineResult");
			List<Map<String,Object>> values = new ArrayList<>(OfflineResults.getLength());

			if(OfflineResults.getLength() > 0)
			{
				for(int i = 0; i < OfflineResults.getLength(); i++)
				{
					Element OfflineResult = (Element) OfflineResults.item(i);
					Map<String,Object> hm = new HashMap<>(4);

					hm.put("Nickname", OfflineResult.getElementsByTagName("Nickname").item(0).getTextContent());
					hm.put("Result", Integer.parseInt(OfflineResult.getElementsByTagName("Result").item(0).getTextContent()));
					hm.put("Position", Integer.parseInt(OfflineResult.getElementsByTagName("Position").item(0).getTextContent()));
					hm.put("GhostID", Integer.parseInt(((Element) OfflineResult.getElementsByTagName("Ghost").item(0)).getAttribute("ID")));

					switch(OfflineResult.getAttribute("Type").toLowerCase())
					{
						case "next":
							this.lastTypeIndex[RESULT_TYPE_NEXT] = i;
							break;

						case "prev":
							this.lastTypeIndex[RESULT_TYPE_PREV] = i;
							break;

						// "same" not implemented
						// because it's not unique
						// ...
					}

					values.add(i, hm);
				}
			}

			return values;
		}
		catch(SAXException|ParserConfigurationException|IOException|NullPointerException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public int[][][][] getAllResults() throws eSportsAPIException
	{
		return this.getSelectiveResults(null);
	}

	public int[][][][] getSelectiveResults(int[][] filter) throws eSportsAPIException
	{
		int[] modes = gmHelper.getGameModeIDs();
		String[] tracks = gmHelper.getTracks(true);
		int[] weathers = gmHelper.getWeatherIDs();

		// Es ist intern wesentlich einfacher mit numerischen Schlüsseln zu arbeiten.
		// Eine Map würde nur unnötigen Overhead erzeugen, der nicht notwendig ist.
		int results[][][][] = new int[FOS][modes.length][tracks.length][weathers.length];

		for(int o = 0; o < FOS; o++)
		{
			for(int m = 0; m < modes.length; m++)
			{
				for(int t = 0; t < tracks.length; t++)
				{
					for(int w = 0; w < weathers.length; w++)
					{
						results[o][m][t][w] = -1;
					}
				}
			}
		}

		try
		{
			Map<String,Object> args = new HashMap<>();

			// DO NOT USE FALSE!
			args.put("includeTicket", "true");
			args.put("includeSUC", "true");

			if(filter != null)
			{
				for(int i = 0; i < filter.length; i++)
				{
					if(filter[i].length != 3 || filter[i][1] < 0 || filter[i][1] >= tracks.length)
					{
						throw new eSportsAPIException(new IndexOutOfBoundsException());
					}

					String m = gmHelper.getGameMode(filter[i][0], true);
					String t = tracks[filter[i][1]].toUpperCase();
					String w = gmHelper.getWeather(filter[i][2], true);

					args.put(String.format("filter_%d", i + 1), String.format("%s.%s.%s",  m, t, w));
				}
			}

			// Diese Methode liefert aber nur aktive Strecken zurück!
			// Allerdings mit automatischer Training/Rennen Erkennung.
			String result = this.request("OFFLINE", "result.dump", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList OfflineResults = doc.getElementsByTagName("OfflineResult");

			if(OfflineResults.getLength() > 0)
			{
				for(int i = 0; i < OfflineResults.getLength(); i++)
				{
					Element OfflineResult = (Element) OfflineResults.item(i);
					Map<String,Object> hm = new HashMap<>(4);

					String track = OfflineResult.getAttribute("Track").toLowerCase();
					String weather = OfflineResult.getAttribute("Weather").toLowerCase();
					String gamemode = OfflineResult.getAttribute("GameMode").toLowerCase();

					int o = FO_NONE;
					int m = -1;
					int t = -1;
					int w = -1;

					for(int h = 0; h < modes.length; h++)
					{
						if(gmHelper.getGameMode(modes[h]).equals(gamemode))
						{
							m = h;
							break;
						}
					}

					for(int h = 0; h < tracks.length; h++)
					{
						if(tracks[h].equals(track))
						{
							t = h;
							break;
						}
					}

					for(int h = 0; h < weathers.length; h++)
					{
						if(gmHelper.getWeather(weathers[h]).equals(weather))
						{
							w = h;
							break;
						}
					}

					if(OfflineResult.getAttribute("Ticket").equalsIgnoreCase("true"))
					{
						o = FO_TICKET;
					}
					else if(OfflineResult.getAttribute("SUC").equalsIgnoreCase("true"))
					{
						o = FO_SUC;
					}

					if(m == -1 || t == -1 || w == -1 || results[o][m][t][w] != -1)
					{
						throw new eSportsAPIException();
					}

					results[o][m][t][w] = Integer.parseInt(OfflineResult.getElementsByTagName("Result").item(0).getTextContent());
				}
			}
		}
		catch(SAXException|ParserConfigurationException|IOException|NullPointerException|gmException e)
		{
			throw new eSportsAPIException(e);
		}

		return results;
	}

	// ACHTUNG: Das erste Array ist nun für FO_* reserviert!
	public int[][][] getRaceWeather() throws eSportsAPIException
	{
		// TODO: forceOption implementieren!
		// ...

		int[] modes = gmHelper.getGameModeIDs();
		String[] tracks = gmHelper.getTracks(true);
		int[] weathers = gmHelper.getWeatherIDs(true, true, true);
		int results[][][] = new int[FOS][modes.length][tracks.length];

		for(int o = 0; o < FOS; o++)
		{
			for(int m = 0; m < modes.length; m++)
			{
				for(int t = 0; t < tracks.length; t++)
				{
					results[o][m][t] = gmHelper.WEATHER_NONE;
				}
			}
		}

		try
		{
			Map<String,Object> args = new HashMap<>();

			// DO NOT USE FALSE!
			args.put("includeTicket", "true");
			args.put("includeSUC", "true");

			// Diese Methode liefert weit mehr, als aktuell gebraucht wird.
			// Immerhin wird mittlerweile auch die Streckenreihenfolge genutzt.
			String result = this.request("OFFLINE", "track.list", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList Tracks = doc.getElementsByTagName("Track");
			String[] tmpTracks = new String[Tracks.getLength()];

			if(Tracks.getLength() > 0)
			{
				for(int i = 0; i < Tracks.getLength(); i++)
				{
					Element Track = (Element) Tracks.item(i);

					String track = Track.getAttribute("Track").toLowerCase();
					String weather = Track.getAttribute("Weather").toLowerCase();
					String gamemode = Track.getAttribute("GameMode").toLowerCase();

					int o = FO_NONE;
					int m = -1;
					int t = -1;
					int w = -1;

					for(int h = 0; h < modes.length; h++)
					{
						if(gmHelper.getGameMode(modes[h]).equals(gamemode))
						{
							m = h;
							break;
						}
					}

					for(int h = 0; h < tracks.length; h++)
					{
						if(tracks[h].equals(track))
						{
							t = h;
							break;
						}
					}

					for(int h = 0; h < weathers.length; h++)
					{
						if(gmHelper.getWeather(weathers[h]).equals(weather))
						{
							w = weathers[h];
							break;
						}
					}

					if(t != -1)
					{
						int nextTrack = 0;
						boolean foundTrack = false;
						for(int h = 0; h < tmpTracks.length; h++)
						{
							if(tmpTracks[h] == null)
							{
								break;
							}
							else if(tmpTracks[h].equals(track))
							{
								foundTrack = true;
								break;
							}
							else
							{
								nextTrack = h + 1;
							}
						}

						if(!foundTrack)
						{
							tmpTracks[nextTrack] = track;
						}
					}

					if(!Track.getAttribute("Race").equalsIgnoreCase("true"))
					{
						continue;
					}

					if(Track.getAttribute("Ticket").equalsIgnoreCase("true"))
					{
						o = FO_TICKET;
					}
					else if(Track.getAttribute("SUC").equalsIgnoreCase("true"))
					{
						o = FO_SUC;
					}

					if(m == -1 || t == -1 || w == -1 || results[o][m][t] != gmHelper.WEATHER_NONE)
					{
						throw new eSportsAPIException("SERVER_DUMB");
					}

					results[o][m][t] = w;
				}
			}

			int trackCount = tmpTracks.length;
			for(int i = 0; i < tmpTracks.length; i++)
			{
				if(tmpTracks[i] == null)
				{
					trackCount = i;
					break;
				}
			}

			serverTracks = new String[trackCount];
			for(int i = 0; i < trackCount; i++)
			{
				serverTracks[i] = tmpTracks[i];
			}

		}
		catch(SAXException|ParserConfigurationException|IOException|NullPointerException|gmException e)
		{
			throw new eSportsAPIException(e);
		}

		return results;
	}

	public boolean updateAvailable(String app, String version, boolean autocheck, int lastMessageVersion) throws eSportsAPIException
	{
		try
		{
			Map<String,Object> args = new HashMap<>();
			args.put("messageVersion", lastMessageVersion);
			args.put("messageLanguage", Locale.getDefault());
			args.put("autocheck", ((autocheck) ? "true" : "false"));
			args.put("application", app); args.put("version", version);
			String result = this.request("OFFLINE", "update.check", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList MessageNodeList = doc.getElementsByTagName("Message");
			NodeList ResultNodeList = doc.getElementsByTagName("Result");

			if(MessageNodeList.getLength() > 0)
			{
				Element MessageElement = (Element) MessageNodeList.item(0);
				this.serverMessageText = MessageElement.getTextContent().replace("\\n", String.format("%n"));
				this.serverMessageVersion = FNX.intval(MessageElement.getAttribute("Version"));
			}

			if(ResultNodeList.getLength() > 0)
			{
				String ResultElement = ((Element) ResultNodeList.item(0)).getTextContent().toUpperCase().trim();

				if(ResultElement.equals("NO_UPDATES"))
				{
					return false;
				}
				else if(ResultElement.equals("UPDATE_AVAILABLE"))
				{
					return true;
				}
				else
				{
					throw new ParserConfigurationException(String.format("Invalid reply: %s", ResultElement));
				}
			}
			else
			{
				throw new ParserConfigurationException("Missing <Result> tag in reply");
			}
		}
		catch(SAXException|ParserConfigurationException|IOException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public boolean updateAvailable(String app, String version) throws eSportsAPIException
	{
		return this.updateAvailable(app, version, false, -1);
	}

	public int getLastServerMessageVersion()
	{
		return this.serverMessageVersion;
	}

	public String getLastServerMessageText()
	{
		return this.serverMessageText;
	}

	public void resetLastServerMessage()
	{
		this.serverMessageText = null;
		this.serverMessageVersion = -1;
	}

	private String request(String module, String method, Map<?,?> data) throws eSportsAPIException
	{
		lastCertificateChain = null;
		HttpURLConnection connection = null;
		StringBuffer response;
		DataOutputStream tx;
		BufferedReader rx;
		String line;

		if(host == null) { setHost(null); }
		if(protocol == null) { setProtocol(null); }
		String apihost = String.format(API_MAINURL, protocol, host);

		module = module.toLowerCase();
		method = method.toLowerCase();

		String postdata = (data != null) ? FNX.buildQueryString(data) : "";
		String url = String.format(API_REQUEST, apihost, API_VERSION, module, method);
		FNX.dbgf("HTTP POST: %s (%d byte)", url, postdata.length());
		// System.err.printf("POST DATA: %s%n", postdata);

		try
		{
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setReadTimeout(API_TIMEOUT * 3);
			connection.setConnectTimeout(API_TIMEOUT);

			if(connection instanceof HttpsURLConnection)
			{
				FNX.dbgf("Set HV to %s", verify ? HV.getClass().getName() : IHV.getClass().getName());
				((HttpsURLConnection) connection).setHostnameVerifier(verify ? HV : IHV);
			}

			if(this.useragent != null)
			{
				connection.setRequestProperty("User-Agent", this.useragent);
			}

			if(module.equals("offline") && this.token != null)
			{
				connection.setRequestProperty("X-Auth-Token", this.token);
			}

			if(this.osdata == null)
			{
				this.osdata = String.format("%s; %s; %s; %s; %s; %s", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"), System.getProperty("java.runtime.name"), System.getProperty("java.version"), Locale.getDefault());
			}
			connection.setRequestProperty("X-OS-Data", this.osdata);

			connection.setRequestMethod("POST"); connection.setDoOutput(true);
			tx = new DataOutputStream(connection.getOutputStream());
			tx.writeBytes(postdata); tx.flush(); tx.close();
			// System.out.println(postdata);

			if(FNX.getDebugging() && connection instanceof HttpsURLConnection)
			{
				try
				{
					lastCertificateChain = ((HttpsURLConnection) connection).getServerCertificates();
				}
				catch(SSLPeerUnverifiedException n)
				{
					/* ... */
				}
			}

			int code = connection.getResponseCode();
			String msg = connection.getResponseMessage();
			InputStream inputStream;

			if(code < 400)
			{
				inputStream = connection.getInputStream();
			}
			else
			{
				inputStream = connection.getErrorStream();
			}

			rx = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

			response = new StringBuffer();
			while((line = rx.readLine()) != null)
			{
				response.append(line);
			}
			rx.close();

			String content = response.toString();
			FNX.dbgf("HTTP %d: %s (%s; %d byte)", code, url, msg, content.length());

			if(code != 200)
			{
				String body = content.trim();
				if(body.matches("^[a-zA-Z0-9_]{1,32}$"))
				{
					throw new eSportsAPIException(body.toUpperCase());
				}
				else
				{
					throw new eSportsAPIException();
				}
			}
			else
			{
				return content;
			}
		}
		catch(UnknownHostException|SocketTimeoutException e)
		{
			throw new eSportsAPIException("INTERNAL_NETWORK_ERROR", e);
		}
		catch(IOException e)
		{
			throw new eSportsAPIException(e);
		}
	}

	public Certificate[] getCertificateChain()
	{
		return lastCertificateChain;
	}
}
