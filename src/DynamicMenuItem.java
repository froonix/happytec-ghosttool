/**
 * DynamicMenuItem.java: Avoid anonymous inner classes
 * Copyright (C) 2019 Christian Schrötter <cs@fnx.li>
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
import javax.swing.KeyStroke;

public class DynamicMenuItem extends JMenuItem implements ActionListener
{
	String className;
	String methodName;

	public DynamicMenuItem(String textLabel, String className, String methodName)
	{
		super(textLabel);
		DynamicMenuItemWorker(textLabel, className, methodName, null);
	}

	public DynamicMenuItem(String textLabel, String className, String methodName, KeyStroke keyStroke)
	{
		super(textLabel);
		DynamicMenuItemWorker(textLabel, className, methodName, keyStroke);
	}

	private void DynamicMenuItemWorker(String textLabel, String className, String methodName, KeyStroke keyStroke)
	{
		this.className = className;
		this.methodName = methodName;

		if(keyStroke != null)
		{
			this.setAccelerator(keyStroke);
		}

		addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent a)
	{
		FNX.actionCallback(this.className, this.methodName);
	}
}
