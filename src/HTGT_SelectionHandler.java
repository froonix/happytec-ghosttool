/**
 * HTGT_SelectionHandler.java: Custom table selection
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

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class HTGT_SelectionHandler implements ListSelectionListener
{
	public void valueChanged(ListSelectionEvent e)
	{
		ListSelectionModel lsm = (ListSelectionModel) e.getSource();

		if(!e.getValueIsAdjusting())
		{
			if(lsm.isSelectionEmpty())
			{
				FNX.dbg("No selection available - disabling menus...");
				HTGT.updateSelectionMenuItems(false);
			}
			else
			{
				FNX.dbg("Selection available - enabling menus...");
				HTGT.updateSelectionMenuItems(true);
			}
		}
	}
}
