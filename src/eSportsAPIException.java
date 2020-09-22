/**
 * eSportsAPIException.java: HAPPYTEC-eSports-API exception
 * Copyright (C) 2020 Christian Schrötter <cs@fnx.li>
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

import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class eSportsAPIException extends Exception
{
	private ResourceBundle lang;
	private boolean calmDown = false;

	public eSportsAPIException()
	{
		super();
	}

	public eSportsAPIException(String code)
	{
		super(code);
		this.processCode(code);
	}

	public eSportsAPIException(Throwable e)
	{
		super("INTERNAL_CLIENT_EXCEPTION", e);
		e.printStackTrace();
	}

	public eSportsAPIException(Throwable e, String code)
	{
		super(code, e);
		e.printStackTrace();
		this.processCode(code);
	}

	private void processCode(String code)
	{
		if(code != null)
		{
			switch(code.toUpperCase())
			{
				case "BANNED":
				case "SEASON_OVER":
				case "RESULT_EMPTY":
				case "GHOST_PRIVATE":
				case "PLAYER_SUSPENDED":
				case "ACCOUNT_INACTIVE":
					this.setCalming(true);
					break;
			}
		}
	}

	public Throwable getException()
	{
		return getCause();
	}

	public String getErrorCode()
	{
		if(this.getMessage() == null)
		{
			return "NULL";
		}

		return this.getMessage();
	}

	public String getErrorMessage()
	{
		if(this.getMessage() != null)
		{
			String code = this.getMessage().toUpperCase();
			this.lang = FNX.getLangBundle("eSportsAPI");

			if(this.lang.containsKey(code))
			{
				return FNX.formatLangString(lang, code);
			}
		}

		// Es gibt noch deutlich mehr Fehlercodes, die haben aber
		// keine Bedeutung, wenn die API korrekt benutzt wird...
		return "Unknown API error code.";
	}

	public void setCalming(boolean state)
	{
		this.calmDown = state;
	}

	public boolean getCalming()
	{
		return this.calmDown;
	}
}
