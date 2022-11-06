/**
 * HTGT_FFM_KeyListener.java: Control VK_ESCAPE binding
 * Copyright (C) 2016-2022 Christian Schrötter <cs@fnx.li>
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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class HTGT_FFM_KeyListener extends KeyAdapter
{
	private boolean disable;

	public void enable()
	{
		disable = false;
	}

	public void disable()
	{
		disable = true;
	}

	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			if(disable)
			{
				FNX.dbg("Ignoring VK_ESCAPE event");
				e.consume();
			}
			else
			{
				FNX.dbg("VK_ESCAPE event triggered");
				HTGT.fastFollowStop(false);
			}
		}
	}
}
