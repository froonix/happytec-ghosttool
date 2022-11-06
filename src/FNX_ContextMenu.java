/**
 * FNX_ContextMenu.java: Cut, Copy, Paste, Delete, Mark all, …
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

import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import javax.swing.text.JTextComponent;

import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class FNX_ContextMenu extends JPopupMenu implements ActionListener
{
	public static final FNX_ContextMenu INSTANCE = new FNX_ContextMenu();

	private final JMenuItem itemCut;
	private final JMenuItem itemCopy;
	private final JMenuItem itemPaste;
	private final JMenuItem itemDelete;
	private final JMenuItem itemSelectAll;

	private FNX_ContextMenu()
	{
		ResourceBundle lang = FNX.getLangBundle("ContextMenu");

		itemCut =       newItem(FNX.getLangString(lang, "cut"),     'X');
		itemCopy =      newItem(FNX.getLangString(lang, "copy"),    'C');
		itemPaste =     newItem(FNX.getLangString(lang, "paste"),   'V');
		itemDelete =    newItem(FNX.getLangString(lang, "delete"));
		addSeparator(); // ----------------------------
		itemSelectAll = newItem(FNX.getLangString(lang, "markAll"), 'A');
	}

	private JMenuItem newItem(String t)
	{
		return this.newItem(t, null);
	}

	private JMenuItem newItem(String t, char c)
	{
		return this.newItem(t, KeyStroke.getKeyStroke(c, FNX.getCtrlMask()));
	}

	private JMenuItem newItem(String t, KeyStroke k)
	{
		JMenuItem i = new JMenuItem(t);
		i.addActionListener(this);

		if(k != null)
		{
			i.setAccelerator(k);
		}

		return add(i);
	}

	@Override
	public void show(Component i, int x, int y)
	{
		JTextComponent c = (JTextComponent) i;
		boolean changeable = c.isEditable() && c.isEnabled();

		itemCut.setVisible(changeable);
		itemPaste.setVisible(changeable);
		itemDelete.setVisible(changeable);

		super.show(i, x, y);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		JTextComponent t = (JTextComponent) getInvoker();
		t.requestFocus();

		boolean s = t.getSelectionStart() != t.getSelectionEnd();

		if(e.getSource() == itemCut)
		{
			if(!s)
			{
				t.selectAll();
			}

			t.cut();
		}
		else if(e.getSource() == itemCopy)
		{
			if(!s)
			{
				t.selectAll();
			}

			t.copy();
		}
		else if(e.getSource() == itemPaste)
		{
			t.paste();
		}
		else if(e.getSource() == itemDelete)
		{
			if(!s)
			{
				t.selectAll();
			}

			t.replaceSelection("");
		}
		else if(e.getSource() == itemSelectAll)
		{
			t.selectAll();
		}
	}
}
