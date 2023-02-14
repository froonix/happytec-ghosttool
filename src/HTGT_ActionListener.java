/**
 * HTGT_ActionListener.java: Hotkeys without menu bindings
 * Copyright (C) 2016-2023 Christian Schrötter <cs@fnx.li>
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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

@SuppressWarnings("serial")
class HTGT_ActionListener extends AbstractAction
{
	private String action;
	private Object[] args;

	public void setPrivateAction(String m)
	{
		action = m;
	}

	public void setPrivateArguments(Object[] a)
	{
		args = a;
	}

	public void actionPerformed(ActionEvent e)
	{
		if(action != null)
		{
			FNX.actionCallback("HTGT", action, args);
		}
	}
}
