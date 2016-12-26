import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

public class eSportsAPI
{
	private final static String API_VERSION = "1.0";
	private final static String API_REQUEST = "%s/%s/%s/%s";
	private final static String API_MAINURL = "https://www.esports.happytec.at/api";

	private String token;
	private String useragent;

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

			if(responseCode != 200)
			{
				// errmsg speichern
				// ...

				return null;
			}

			BufferedReader in = new BufferedReader(
					new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null)
			{
				response.append(inputLine);
			}
			in.close();

			return response.toString();
		}
		catch(Exception e)
		{
			// ...
			// ...
		}

		return "";
	}
}
