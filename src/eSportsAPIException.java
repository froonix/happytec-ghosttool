/**
 * eSportsAPIException.java: HAPPYTEC-eSports-API exception
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

public class eSportsAPIException extends Exception
{
	private Exception primaryException;

	public eSportsAPIException()
	{
		super();
	}

	public eSportsAPIException(String code)
	{
		super(code);
	}

	public eSportsAPIException(Exception e)
	{
		super("INTERNAL_CLIENT_EXCEPTION");
		this.setException(e);
	}

	public eSportsAPIException(Exception e, String code)
	{
		super(code);
		this.setException(e);
	}

	private void setException(Exception e)
	{
		this.primaryException = e;
		e.printStackTrace();
	}

	public Exception getException()
	{
		return this.primaryException;
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
			switch(this.getMessage())
			{
				// Es gibt noch deutlich mehr Fehlercodes, die haben aber
				// keine reale Bedeutung, wenn die API korrekt benutzt wird.
				case "RESULT_EMPTY":              return "Die Rangliste ist (noch) leer.";
				case "PLAYER_SUSPENDED":          return "Dein Spieler wurde suspendiert!";
				case "GHOST_UNKNOWN":             return "Es wurden keine Geister gefunden.";
				case "GHOST_PRIVATE":             return "Dieser Geist ist nicht öffentlich.";
				case "GHOST_DUPLICATE":           return String.format("Der Geist wurde schon einmal verwendet.%n%nEin anderer Teilnehmer verwendete diesen Geist schon einmal für die Rangliste.%nBitte wende dich an die eSports-Moderatoren im Forum, falls das ein Fehler ist.");
				case "GHOST_DOPING":              return String.format("Die Dopingkontrolle verlief positiv!%n%nFür einen fairen Bewerb sind bestimmte Aktionen z.B. beim Rennen verboten.%nDazu kann u.a. das Zurücksetzen, Stürzen oder Auslassen der Tore zählen.%n%nFür weitere Informationen wende dich bitte an die eSports-Moderatoren im Forum.");
				case "RESULT_WORSE":              return String.format("Die neue Zeit ist nicht schneller.%n%nDu kannst nur Ergebnisse übernehmen, die besser als dein existierendes sind.");
				case "NO_ACTIVE_TRACK":           return String.format("Die gewünschte Strecke ist nicht aktiv.%n%nWahrscheinlich startet die Strecke erst in einiger Zeit.");
				case "TRACK_UNKNOWN":             return String.format("Die gewünschte Strecke existiert nicht.%n%nWahrscheinlich wurde die Rangliste noch nicht angelegt.");
				case "TOKEN_UNKNOWN":             return String.format("Unbekannter API-Token!%n%nBitte kontrolliere den API-Token.");
				case "TOKEN_INVALID":             return String.format("Ungültiges Format des API-Tokens!%n%nBitte kontrolliere den API-Token.");
				case "SEASON_OVER":               return String.format("Die Saison ist schon beendet.%n%nSchau ins Forum, wann es wieder los geht!");
				case "INTERNAL_CLIENT_EXCEPTION": return String.format("Interne Exception im Java-Programm.%n%nSiehe Stacktrace in der Konsolenausgabe.");
				case "INTERNAL_NETWORK_ERROR":    return "Überprüfe deine Internetverbindung.";
			}
		}

		return "Unbekannter Fehler, siehe Fehlercode.";
	}
}
