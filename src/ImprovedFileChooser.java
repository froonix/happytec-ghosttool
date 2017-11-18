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
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ImprovedFileChooser extends JFileChooser
{
	private static ResourceBundle lang = FNX.getLangBundle("ImprovedFileChooser");

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
			if(overwriteFile(f))
			{
				super.approveSelection();
			}

			//super.cancelSelection();
			return;
		}

		super.approveSelection();
	}

	public static boolean overwriteFile(File f)
	{
		switch(JOptionPane.showConfirmDialog(null, FNX.formatLangString(lang, "overwrite", f), null, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE))
		{
			case JOptionPane.YES_OPTION:
				return true;

			/*
			case JOptionPane.NO_OPTION:
			case JOptionPane.CLOSED_OPTION:
				return false;
			*/

			default:
				return false;
		}
	}
}

// TODO: Bei Bedarf Dateiendung erzwingen.
//       Siehe Code in HTGT.exportFile()!
// ...
