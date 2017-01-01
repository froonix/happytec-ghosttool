import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import org.w3c.dom.*;



import javax.net.ssl.HttpsURLConnection;

public class eSportsAPI
{
	private final static String API_VERSION = "1.0";
	private final static String API_REQUEST = "%s/%s/%s/%s";
	private final static String API_MAINURL = "https://www.esports.happytec.at/api";

	private String token;
	private String useragent;
	private String errmsg;

	public eSportsAPI(String token)
	{
		setToken(token);
	}

	public eSportsAPI(String token, String useragent)
	{
		setToken(token);
		setUseragent(useragent);
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public void setUseragent(String useragent)
	{
		this.useragent = useragent;
	}

	public String getGhostByID(int id)
	{
		Map<String,Object> args = new HashMap<String,Object>();
		args.put("byID", id);

		return this.request("OFFLINE", "ghost.get", args);
	}

	public String getGhostsByIDs(int[] ids)
	{
		Map<String,Object> args = new HashMap<String,Object>();
		StringBuilder value = new StringBuilder();

		for(int i = 0; i < ids.length; i++)
		{
			if(i > 0)
			{
				value.append(",");
			}

			value.append(String.format("%d", ids[i]));
		}

		args.put("byIDs", value.toString());
		return this.request("OFFLINE", "ghost.get", args);
	}

	public String getGhostsByIDs(Integer[] ids)
	{
		int[] values = Arrays.stream(ids).mapToInt(Integer::intValue).toArray();
		return getGhostsByIDs(values);
	}

	public int[] getGhostIDs(GhostElement[] ghosts)
	{
		Map<String,Object> args = new HashMap<String,Object>();
		StringBuilder data = new StringBuilder();

		for(int i = 0; i < ghosts.length; i++)
		{
			data.append(ghosts[i].toString());
		}

		args.put("XML", data.toString());
		String result = this.request("OFFLINE", "ghost.put", args);

		if(result != null)
		{
			try
			{
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
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	public boolean applyResultByGhostID(int ghostID)
	{
		Map<String,Object> args = new HashMap<String,Object>();

		args.put("ghostID", Integer.toString(ghostID));
		String result = this.request("OFFLINE", "result.apply", args);

		if(result != null)
		{
			try
			{
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
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	private String request(String module, String method, Map<?,?> data)
	{
		module = module.toLowerCase();
		method = method.toLowerCase();

		String url = String.format(API_REQUEST, API_MAINURL, API_VERSION, module, method);
		String postdata = FNX.buildQueryString(data);

		// System.out.println(url);
		// System.out.println(postdata);

		try
		{

			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", this.useragent);

			if(module.equals("offline"))
			{
				con.setRequestProperty("X-Auth-Token", this.token);
			}

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(postdata);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + postdata);
			System.out.println("Response Code : " + responseCode);

			InputStream _is;
			if(responseCode < 400)
			{
				_is = con.getInputStream();
			}
			else
			{
				_is = con.getErrorStream();
			}

			BufferedReader in = new BufferedReader(
					new InputStreamReader(_is));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			in.close();

			if(responseCode != 200)
			{
				String body = response.toString().trim();
				if(body.matches("^[a-zA-Z0-9_]{1,32}$"))
				{
					this.errmsg = body.toUpperCase();
				}
				else
				{
					this.errmsg = null;
				}

				return null;
			}

			return response.toString();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			this.errmsg = "INTERNAL_CLIENT_EXCEPTION";
		}

		return null;
	}

	public String getErrorCode()
	{
		return this.errmsg;
	}

	public String getErrorMessage()
	{
		if(this.errmsg != null)
		{
			switch(this.errmsg)
			{
				// Es gibt noch deutlich mehr Fehlercodes, die haben aber
				// keine reale Bedeutung, wenn die API korrekt benutzt wird.
				case "PLAYER_SUSPENDED":          return "Dein Spieler wurde suspendiert!";
				case "GHOST_UNKNOWN":             return "Es wurden keine Geister gefunden.";
				case "GHOST_PRIVATE":             return "Dieser Geist ist nicht öffentlich.";
				case "TOKEN_UNKNOWN":             return "Unbekannter API-Token!\n\nBitte kontrolliere den API-Token.";
				case "TOKEN_INVALID":             return "Ungültiges Format des API-Tokens!\n\nBitte kontrolliere den API-Token.";
				case "SEASON_OVER":               return "Die Saison ist schon beendet.\n\nSchau ins Forum, wann es wieder los geht!";
				case "INTERNAL_CLIENT_EXCEPTION": return "Interne Exception im Java-Programm.\n\nSiehe Stacktrace in der Konsolenausgabe.";
			}
		}

		return "Unbekannter Fehler, siehe Fehlercode.";
	}
}
