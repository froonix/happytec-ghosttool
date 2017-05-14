/**
 * eSportsAPI.java: HAPPYTEC-eSports-API interface
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.lang.NullPointerException;
import java.lang.RuntimeException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URL;

import java.text.ParseException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

public class eSportsAPI
{
	private final static String API_VERSION = "1.0";
	private final static String API_REQUEST = "%s/%s/%s/%s";
	private final static String API_MAINURL = "https://%s/api";
	private final static String API_HOST    = "www.esports.happytec.at";
	private final static int    API_TIMEOUT = 10000;

	private static String host;

	private String token;
	private String useragent;
	private String osdata;

	private final static int RESULT_TYPE_NEXT = 0;
	private final static int RESULT_TYPE_PREV = 1;
	private int[] lastTypeIndex = new int[2];

	public eSportsAPI(String token)
	{
		this.setToken(token);
	}

	public eSportsAPI(String token, String useragent)
	{
		this.setToken(token);
		this.setUseragent(useragent);
	}

	public static void setHost(String fqdn)
	{
		if(fqdn == null)
		{
			host = API_HOST;
		}
		else
		{
			// TODO: Validation hinzufügen?
			// (host oder host:port Format!)
			host = fqdn;
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

	public int getNextResultIndex()
	{
		return this.lastTypeIndex[RESULT_TYPE_NEXT];
	}

	public int getPrevResultIndex()
	{
		return this.lastTypeIndex[RESULT_TYPE_PREV];
	}

	public GhostElement getGhostByID(int id) throws eSportsAPIException
	{
		try
		{
			Map<String,Object> args = new HashMap<String,Object>();
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
			Map<String,Object> args = new HashMap<String,Object>();
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
		try
		{
			Map<String,Object> args = new HashMap<String,Object>();
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

	public boolean applyResultByGhostID(int ghostID) throws eSportsAPIException
	{
		try
		{
			Map<String,Object> args = new HashMap<String,Object>();
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

	public Map<String,Object> getPlayerInfo() throws eSportsAPIException
	{
		try
		{
			Map<String,Object> values = new HashMap<String,Object>();
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
			Map<String,Object> args = new HashMap<String,Object>();
			args.put("byGameModeID", mode);
			args.put("byTrack", track);
			args.put("byWeatherID", weather);
			args.put("forceWeather", (forceWeather) ? 1 : 0);
			String result = this.request("OFFLINE", "result.get", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList OfflineResults = doc.getElementsByTagName("OfflineResult");
			List<Map<String,Object>> values = new ArrayList<Map<String,Object>>(OfflineResults.getLength());

			if(OfflineResults.getLength() > 0)
			{
				for(int i = 0; i < OfflineResults.getLength(); i++)
				{
					Element OfflineResult = (Element) OfflineResults.item(i);
					Map<String,Object> hm = new HashMap<String,Object>(4);

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

	public int[][][] getAllResults() throws eSportsAPIException
	{
		int[] modes = gmHelper.getGameModeIDs();
		String[] tracks = gmHelper.getTracks(true);
		int[] weathers = gmHelper.getWeatherIDs();

		// Es ist intern wesentlich einfacher mit numerischen Schlüsseln zu arbeiten.
		// Eine Map würde nur unnötigen Overhead erzeugen, der nicht notwendig ist.
		int results[][][] = new int[modes.length][tracks.length][weathers.length];

		for(int m = 0; m < modes.length; m++)
		{
			for(int t = 0; t < tracks.length; t++)
			{
				for(int w = 0; w < weathers.length; w++)
				{
					results[m][t][w] = -1;
				}
			}
		}

		try
		{
			// Diese Methode liefert aber nur aktive Strecken zurück!
			// Allerdings mit automatischer Qualifikation/Rennen Erkennung.
			String result = this.request("OFFLINE", "result.dump", null);

			Document doc = FNX.getDOMDocument(result);
			NodeList OfflineResults = doc.getElementsByTagName("OfflineResult");

			if(OfflineResults.getLength() > 0)
			{
				for(int i = 0; i < OfflineResults.getLength(); i++)
				{
					Element OfflineResult = (Element) OfflineResults.item(i);
					Map<String,Object> hm = new HashMap<String,Object>(4);

					String track = OfflineResult.getAttribute("Track").toLowerCase();
					String weather = OfflineResult.getAttribute("Weather").toLowerCase();
					String gamemode = OfflineResult.getAttribute("GameMode").toLowerCase();

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

					if(m == -1 || t == -1 || w == -1 || results[m][t][w] != -1)
					{
						throw new eSportsAPIException();
					}

					results[m][t][w] = Integer.parseInt(OfflineResult.getElementsByTagName("Result").item(0).getTextContent());
				}
			}
		}
		catch(SAXException|ParserConfigurationException|IOException|NullPointerException|gmException e)
		{
			throw new eSportsAPIException(e);
		}

		return results;
	}

	public int[][] getRaceWeather() throws eSportsAPIException
	{
		int[] modes = gmHelper.getGameModeIDs();
		String[] tracks = gmHelper.getTracks(true);
		int[] weathers = gmHelper.getWeatherIDs(true);
		int results[][] = new int[modes.length][tracks.length];

		for(int m = 0; m < modes.length; m++)
		{
			for(int t = 0; t < tracks.length; t++)
			{
				results[m][t] = gmHelper.WEATHER_NONE;
			}
		}

		try
		{
			// Diese Methode liefert weit mehr, als aktuell gebraucht wird.
			String result = this.request("OFFLINE", "track.list", null);

			Document doc = FNX.getDOMDocument(result);
			NodeList Tracks = doc.getElementsByTagName("Track");

			if(Tracks.getLength() > 0)
			{
				for(int i = 0; i < Tracks.getLength(); i++)
				{
					Element Track = (Element) Tracks.item(i);

					if(!Track.getAttribute("Race").toLowerCase().equals("true"))
					{
						continue;
					}

					String track = Track.getAttribute("Track").toLowerCase();
					String weather = Track.getAttribute("Weather").toLowerCase();
					String gamemode = Track.getAttribute("GameMode").toLowerCase();

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

					if(m == -1 || t == -1 || w == -1 || results[m][t] != gmHelper.WEATHER_NONE)
					{
						throw new eSportsAPIException();
					}

					results[m][t] = w;
				}
			}

		}
		catch(SAXException|ParserConfigurationException|IOException|NullPointerException|gmException e)
		{
			throw new eSportsAPIException(e);
		}

		return results;
	}

	public boolean updateAvailable(String app, String version, boolean autocheck) throws eSportsAPIException
	{
		try
		{
			Map<String,Object> args = new HashMap<String,Object>();
			args.put("autocheck", ((autocheck) ? "true" : "false"));
			args.put("application", app); args.put("version", version);
			String result = this.request("OFFLINE", "update.check", args);

			Document doc = FNX.getDOMDocument(result);
			NodeList ResultNodeList = doc.getElementsByTagName("Result");

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
		return this.updateAvailable(app, version, false);
	}

	private String request(String module, String method, Map<?,?> data) throws eSportsAPIException
	{
		HttpsURLConnection connection;
		StringBuffer response;
		DataOutputStream tx;
		BufferedReader rx;
		String line;

		if(host == null) { setHost(null); }
		String apihost = String.format(API_MAINURL, host);

		module = module.toLowerCase();
		method = method.toLowerCase();

		String postdata = (data != null) ? FNX.buildQueryString(data) : "";
		String url = String.format(API_REQUEST, apihost, API_VERSION, module, method);
		System.err.printf("HTTP POST: %s (%d byte)%n", url, postdata.length());
		// System.err.printf("POST DATA: %s%n", postdata);

		try
		{
			connection = (HttpsURLConnection) new URL(url).openConnection();
			connection.setReadTimeout(API_TIMEOUT * 3);
			connection.setConnectTimeout(API_TIMEOUT);

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
				this.osdata = String.format("%s; %s; %s; %s; %s", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"), System.getProperty("java.runtime.name"), System.getProperty("java.version"));
			}
			connection.setRequestProperty("X-OS-Data", this.osdata);

			connection.setRequestMethod("POST"); connection.setDoOutput(true);
			tx = new DataOutputStream(connection.getOutputStream());
			tx.writeBytes(postdata); tx.flush(); tx.close();
			// System.out.println(postdata);

			int code = connection.getResponseCode();
			String msg = connection.getResponseMessage();

			if(code < 400)
			{
				rx = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			}
			else
			{
				rx = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			}

			response = new StringBuffer();
			while((line = rx.readLine()) != null)
			{
				response.append(line);
			}
			rx.close();

			String content = response.toString();
			System.err.printf("HTTP %d: %s (%s; %d byte)%n", code, url, msg, content.length());

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
			throw new eSportsAPIException(e, "INTERNAL_NETWORK_ERROR");
		}
		catch(IOException e)
		{
			throw new eSportsAPIException(e);
		}
	}
}
