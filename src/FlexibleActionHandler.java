/**
 * FlexibleActionHandler.java: Avoid anonymous inner classes
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class FlexibleActionHandler extends JMenuItem implements ActionListener
{
	String className;
	String methodName;

	public FlexibleActionHandler(String className, String methodName)
	{
		super();

		this.className = className;
		this.methodName = methodName;

		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent a)
	{
		FNX.actionCallback(this.className, this.methodName);
	}
}

// TODO: Diese Klasse sollte nicht JMenuItem erweitern.
//       Ich bin jetzt aber zu faul die passende zu suchen,
//       bei der addActionListener() implementiert ist.
// ...
