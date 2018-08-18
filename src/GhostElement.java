/**
 * GhostElement.java: Representation of GhostDataPair
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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import java.lang.IllegalArgumentException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GhostElement
{
	private static Pattern GhostPattern;
	private static Pattern GhostsPattern;

	private Element XML;
	private String  Hash;
	private boolean Ticket;
	private int     GameMode;
	private String  Track;
	private int     Weather;
	private int     Time;
	private String  DataRaw;
	private byte[]  DataBinary;
	private String  Nickname;
	private int[]   Ski;

	GhostElement()
	{

	}

	GhostElement(Node xml) throws GhostException
	{
		this.importGhost(xml);
	}

	GhostElement(Element xml) throws GhostException
	{
		this.importGhost(xml);
	}

	GhostElement(String xml) throws GhostException
	{
		this.importGhost(xml);
	}

	public static GhostElement[] parseGhosts(File file) throws GhostException
	{
		try
		{
			return parseGhosts(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())), StandardCharsets.UTF_8));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static GhostElement[] parseGhosts(String xml) throws GhostException
	{
		if(GhostsPattern == null)
		{
			GhostsPattern = Pattern.compile("(<GhostDataPair[^>]+>)", Pattern.CASE_INSENSITIVE);
		}

		ArrayList<GhostElement> Ghosts = new ArrayList<GhostElement>();
		Matcher GhostsMatcher = GhostsPattern.matcher(xml);

		while(GhostsMatcher.find())
		{
			Ghosts.add(new GhostElement(GhostsMatcher.group(1)));
		}

		return Ghosts.toArray(new GhostElement[Ghosts.size()]);
	}

	public void importGhost(String xml) throws GhostException
	{
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
			NodeList gdp = doc.getElementsByTagName("GhostDataPair");

			if(gdp.getLength() == 0)
			{
				throw new GhostException("Missing <GhostDataPair> tag");
			}
			else if(gdp.getLength() > 1)
			{
				throw new GhostException(String.format("Too many <GhostDataPair>(%s) tags! Use parseGhosts() instead...", gdp.getLength()));
			}
			else
			{
				importGhost((Element) gdp.item(0));
			}
		}
		catch(SAXException|ParserConfigurationException|IOException e)
		{
			e.printStackTrace();
			throw new GhostException(e.getMessage());
		}
	}

	public void importGhost(Element xml) throws GhostException
	{
		this.XML = xml;

		if(GhostPattern == null)
		{
			this.GhostPattern = Pattern.compile("\030.\042.(?:\010(?<c>.))?(?:\020(?<g>.))?(?:\030(?<s>.))?\042..+?\\\050.\060.(?:\010(?<b>.{1,5})\\\052)?.*?\062.\012.(?<n>.{1,32})\022\016(?<e>.{1,32})\030(?<v>.)\100.$", Pattern.DOTALL);
		}

		try
		{
			String weatherString = xml.getAttribute("Weather").toUpperCase();
			String gameModeString = xml.getAttribute("GameMode").toUpperCase();

			this.Track = xml.getAttribute("Track").toLowerCase();
			this.Time = FNX.intval(xml.getAttribute("Time"), true);
			this.DataRaw = xml.getAttribute("Data");

			if(this.Time <= 0 || this.Time > 660000)
			{
				throw new GhostException("Attribute \"Time\" is missing, empty or invalid value.");
			}
			else if(this.DataRaw.length() == 0)
			{
				throw new GhostException("Attribute \"Data\" is missing or empty.");
			}
			else if(weatherString.length() == 0)
			{
				throw new GhostException("Attribute \"Weather\" is missing or empty.");
			}
			else if(gameModeString.length() == 0)
			{
				throw new GhostException("Attribute \"GameMode\" is missing or empty.");
			}
			else if(this.Track.length() == 0 || !Arrays.asList(gmHelper.getTracks(true)).contains(this.Track))
			{
				throw new GhostException("Attribute \"Track\" is missing, empty or value is unknown.");
			}

			this.Weather = gmHelper.parseWeather(weatherString);
			this.GameMode = gmHelper.parseGameMode(gameModeString);
			this.DataBinary = Base64.getDecoder().decode(this.DataRaw);

			// Normalerweise gehört das mit Google's Protocol Buffers extrahiert.
			// Ich habe aber keine Lust das zu implementieren, wenn es auch so geht.
			Matcher m = this.GhostPattern.matcher(new String(this.DataBinary, "ISO-8859-1"));

			if(m.find())
			{
				this.Ticket = (m.group("b") != null) ? true : false;

				this.Nickname = m.group("n");
				// this.edition = m.group("e");
				// this.flag = m.group("v");

				this.Ski = new int[]{
					(m.group("c") != null) ? (int) m.group("c").charAt(0) : 0,
					(m.group("g") != null) ? (int) m.group("g").charAt(0) : 0,
					(m.group("s") != null) ? (int) m.group("s").charAt(0) : 0
				};
			}

			if(this.Nickname == null || !this.Nickname.matches("^(?i)[A-Z0-9_]{1,20}$"))
			{
				throw new GhostException("GhostData: Missing or invalid nickname");
			}
			else if(this.Ski == null || this.Ski.length != 3 || (this.Ski[0] + this.Ski[1] + this.Ski[2]) > 100)
			{
				throw new GhostException("GhostData: Missing or invalid ski");
			}
		}
		catch(gmException e)
		{
			throw new GhostException(e.getMessage());
		}
		catch(IllegalArgumentException|UnsupportedEncodingException e)
		{
			// z.B. fehlerhafte Base64 Daten
			throw new GhostException(e.getMessage());
		}
	}

	public void importGhost(Node xml) throws GhostException
	{
		this.importGhost((Element) xml);
	}

	public Element getElement()
	{
		return this.XML;
	}

	public int getGameMode()
	{
		return this.GameMode;
	}

	public String getGameModeName()
	{
		try
		{
			return gmHelper.getGameModeName(this.GameMode);
		}
		catch(gmException e)
		{
			e.printStackTrace();

			return null;
		}
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
		try
		{
			return gmHelper.getTrack(this.Track);
		}
		catch(gmException e)
		{
			return null;
		}
	}

	public int getWeather()
	{
		return this.Weather;
	}

	public String getWeatherName()
	{
		try
		{
			return gmHelper.getWeatherName(this.Weather);
		}
		catch(gmException e)
		{
			e.printStackTrace();

			return null;
		}
	}

	public int getTime()
	{
		return this.Time;
	}

	public String getResult()
	{
		return gmHelper.getResult(this.Time);
	}

	public int[] getSki()
	{
		return this.Ski;
	}

	public boolean hasTicket()
	{
		return this.Ticket;
	}

	public String getConditions()
	{
		try
		{
			return String.format("%s.%s.%s", gmHelper.getGameMode(this.GameMode).toUpperCase(), gmHelper.getWeather(this.Weather).toUpperCase(), this.getTrack().toUpperCase());
		}
		catch(gmException e)
		{
			e.printStackTrace();
			return "undefined";
		}
	}

	public String getHash()
	{
		if(this.Hash == null)
		{
			try
			{
				byte[] bytes = MessageDigest.getInstance("SHA-512").digest(this.DataBinary);
				StringBuilder hash = new StringBuilder();

				for(int i=0; i< bytes.length ;i++)
				{
					hash.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
				}

				this.Hash = String.format("%08x%s", this.DataBinary.length, hash.toString());
			}
			catch(NoSuchAlgorithmException e)
			{
				e.printStackTrace();
				this.Hash = null;
			}
		}

		return this.Hash;
	}

	public void printDetails()
	{
		try
		{
			System.out.printf("--------------------------------%n");
			System.out.printf(" Nick:    %s%n", this.getNickname());
			System.out.printf(" Time:    %s (%d)%n", this.getResult(), this.getTime());
			System.out.printf(" Track:   [%s] %s%n", this.getTrack().toUpperCase(), this.getTrackName());
			System.out.printf(" Weather: [%s] %s (%d)%n", gmHelper.getWeather(this.Weather).toUpperCase(), this.getWeatherName(), this.getWeather());
			System.out.printf(" Mode:    [%s] %s (%d)%n", gmHelper.getGameMode(this.GameMode).toUpperCase(), this.getGameModeName(), this.getGameMode());
			System.out.printf(" Ski:     %d-%d-%d%n", this.Ski[0], this.Ski[1], this.Ski[2]);
			System.out.printf(" Ticket:  %s%n", this.Ticket ? "Yes" : "No");
			System.out.printf(" Hash:    %s%n", this.getHash());
			// System.out.printf("%n%s%n", DataRaw);
			System.out.printf("--------------------------------%n");
		}
		catch(gmException e)
		{
			e.printStackTrace();

			return;
		}
	}

	public String getDebugDetails()
	{
		try
		{
			return String.format("%s @ %s (%s/%s) - %s (%d-%d-%d)", this.getNickname(), this.getTrack().toUpperCase(), gmHelper.getGameMode(this.getGameMode()).toUpperCase(), gmHelper.getWeather(this.Weather).toUpperCase(), this.getTime(), this.Ski[0], this.Ski[1], this.Ski[2]);
		}
		catch(gmException e)
		{
			e.printStackTrace();

			return null;
		}
	}

	public String toString()
	{
		try
		{
			return FNX.getWinNL(FNX.getStringFromDOM(this.XML, false));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
