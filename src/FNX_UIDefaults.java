/**
 * FNX_UIDefaults.java: Context menu for everything
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

import javax.swing.JComponent;
import javax.swing.UIDefaults;

import javax.swing.plaf.ComponentUI;

import javax.swing.text.JTextComponent;

@SuppressWarnings("serial")
class FNX_UIDefaults extends UIDefaults
{
	@Override
	public ComponentUI getUI(JComponent c)
	{
		if(c instanceof JTextComponent)
		{
			if(c.getClientProperty(this) == null)
			{
				c.setComponentPopupMenu(FNX_ContextMenu.INSTANCE);
				c.putClientProperty(this, Boolean.TRUE);
			}
		}

		return null;
	}
}
