import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.*;

abstract class FNX
{
	public static int intval(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch(NumberFormatException e)
		{
			return 0;
		}
}

	public static String urlencode(String s)
	{
        try
        {
			return URLEncoder.encode(s, "UTF-8");
		}
		catch(UnsupportedEncodingException e)
		{
			throw new UnsupportedOperationException(e);
		}
	}

	public static String buildQueryString(Map<?,?> map)
	{
		StringBuilder qs = new StringBuilder();
		for(Map.Entry<?,?> entry : map.entrySet())
		{
			if(qs.length() > 0)
			{
				qs.append("&");
			}

			qs.append(String.format("%s=%s", urlencode(entry.getKey().toString()), urlencode(entry.getValue().toString())));
		}

		return qs.toString();
	}
}
