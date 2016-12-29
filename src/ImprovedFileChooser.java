import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class ImprovedFileChooser extends JFileChooser
{
	public ImprovedFileChooser(String currentDirectoryPath)
	{
		super(currentDirectoryPath);
	}

	public void approveSelection()
	{
		File f = getSelectedFile();
		if(f.exists() && getDialogType() == SAVE_DIALOG)
		{
			int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_OPTION);

			switch(result)
			{
				case JOptionPane.YES_OPTION:
					super.approveSelection();
					return;

				default:
					cancelSelection();
					return;
			}
		}

		super.approveSelection();
	}
}
