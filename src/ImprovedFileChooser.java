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
