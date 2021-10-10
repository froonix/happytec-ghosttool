/**
 * GhostException.java: GhostElement exception
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

@SuppressWarnings("serial")
public class GhostException extends RuntimeException
{
	public GhostException(String message)
	{
		super(message);
	}

	public GhostException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public GhostException(int index, String message)
	{
		super(String.format("[%d] %s", index, message));
	}

	public GhostException(int index, String message, Throwable cause)
	{
		super(String.format("[%d] %s", index, message), cause);
	}

	public GhostException(int index, Throwable cause)
	{
		super(String.format("[%d] %s", index, cause.getMessage()), cause);
	}
}
