/**
 * FNX_LookAndFeel.java: Context menu for everything
 * Copyright (C) 2016-2025 Christian Schrötter <cs@fnx.li>
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

import javax.swing.LookAndFeel;

public class FNX_LookAndFeel extends LookAndFeel
{
	private final FNX_UIDefaults defaults = new FNX_UIDefaults();

	@Override
	public FNX_UIDefaults getDefaults()
	{
		return defaults;
	};

	@Override
	public String getID()
	{
		return "FNX_ContextMenu";
	}

	@Override
	public String getName()
	{
		return getID();
	}

	@Override
	public String getDescription()
	{
		return getID();
	}

	@Override
	public boolean isNativeLookAndFeel()
	{
		return false;
	}

	@Override
	public boolean isSupportedLookAndFeel()
	{
		return true;
	}
}
