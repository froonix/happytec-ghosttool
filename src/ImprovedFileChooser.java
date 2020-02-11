/**
 * ImprovedFileChooser.java: Handle existing files, …
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

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class ImprovedFileChooser extends JFileChooser
{
	private static ResourceBundle lang = FNX.getLangBundle("ImprovedFileChooser");

	public ImprovedFileChooser(String currentDirectoryPath)
	{
		super(currentDirectoryPath);
	}

	public static void setLanguageStrings()
	{
		String[] opts = new String[]{
			"FileChooser.acceptAllFileFilterText",
			"FileChooser.byDateText",
			"FileChooser.byNameText",
			"FileChooser.cancelButtonText",
			"FileChooser.cancelButtonToolTipText",
			"FileChooser.chooseButtonText",
			"FileChooser.createButtonText",
			"FileChooser.deleteFileButtonText",
			"FileChooser.detailsViewButtonToolTipText",
			"FileChooser.directoryDescriptionText",
			"FileChooser.directoryOpenButtonText",
			"FileChooser.fileDateHeaderText",
			"FileChooser.fileDescriptionText",
			"FileChooser.fileNameHeaderText",
			"FileChooser.fileNameLabelText",
			"FileChooser.fileSizeHeaderText",
			"FileChooser.filesOfTypeLabelText",
			"FileChooser.filterLabelText",
			"FileChooser.helpButtonText",
			"FileChooser.homeFolderToolTipText",
			"FileChooser.listViewButtonToolTipText",
			"FileChooser.lookInLabelText",
			"FileChooser.newFolderButtonText",
			"FileChooser.newFolderErrorText",
			"FileChooser.newFolderExistsErrorText",
			"FileChooser.newFolderPromptText",
			"FileChooser.newFolderTitleText",
			"FileChooser.newFolderToolTipText",
			"FileChooser.openButtonText",
			"FileChooser.openButtonToolTipText",
			"FileChooser.openDialogText",
			"FileChooser.openDialogTitleText",
			"FileChooser.openTitleText",
			"FileChooser.renameFileButtonText",
			"FileChooser.saveButtonText",
			"FileChooser.saveDialogFileNameLabelText",
			"FileChooser.saveDialogTitleText",
			"FileChooser.saveInLabelText",
			"FileChooser.saveTitleText",
			"FileChooser.updateButtonText",
			"FileChooser.upFolderToolTipText"
		};

		for(int i = 0; i < opts.length; i++)
		{
			String newopt = opts[i].replaceAll("^.+\\.", "").replaceAll("Text$", "");

			if(FNX.checkLangStringExists(lang, newopt))
			{
				UIManager.put(opts[i], FNX.getLangString(lang, newopt));
			}
		}
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
