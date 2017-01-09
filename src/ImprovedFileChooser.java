/**
 * ImprovedFileChooser.java: Handle existing files, …
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

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ImprovedFileChooser extends JFileChooser
{
	public ImprovedFileChooser(String currentDirectoryPath)
	{
		super(currentDirectoryPath);
	}

	@Override
	public void approveSelection()
	{
		File f = getSelectedFile();
		if(f.exists() && getDialogType() == SAVE_DIALOG)
		{
			int result = JOptionPane.showConfirmDialog(null, "Soll die existierende Datei überschrieben werden?", null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			switch(result)
			{
				case JOptionPane.YES_OPTION:
					super.approveSelection();
					return;

				case JOptionPane.NO_OPTION:
				case JOptionPane.CLOSED_OPTION:
					return;
			}

			super.cancelSelection();
		}

		super.approveSelection();
	}
}
