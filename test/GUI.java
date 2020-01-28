import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

abstract class GUI
{
	private static JLabel statusLabel;
	private static JFrame mainFrame;

	private static SwingWorker worker;
	private static boolean state = false;

	public static void quit()
	{
		Helper.dbg("Good bye!");
		System.exit(0);
	}

	public static void setupGUI()
	{
		Helper.dbg("Init GUI");

		JButton btn;

		mainFrame = new JFrame("GUI");

		mainFrame.addWindowListener(new GUI_WindowAdapter());

		mainFrame.setSize(400, 400);
		mainFrame.setLayout(new GridLayout(2, 1));

		statusLabel = new JLabel("Hello!", JLabel.CENTER);
		mainFrame.add(statusLabel);

		btn = new JButton("Foo Bar");
		btn.setPreferredSize(new Dimension(5,5));
		btn.addActionListener(new FlexibleActionHandler(GUI.class.getName(), "doSomethingElse"));
		mainFrame.add(btn);

		btn = new JButton("Start counter");
		btn.setPreferredSize(new Dimension(5,5));
		btn.addActionListener(new FlexibleActionHandler(GUI.class.getName(), "startThread"));
		mainFrame.add(btn);

		btn = new JButton("Stop counter");
		btn.setPreferredSize(new Dimension(5,5));
		btn.addActionListener(new FlexibleActionHandler(GUI.class.getName(), "stopThread"));
		mainFrame.add(btn);

		mainFrame.setVisible(true);
	}

	public static boolean getState()
	{
		return state;
	}

	public static void setState(boolean value)
	{
		state = value;
	}

	public static boolean lockState()
	{
		if(!getState())
		{
			setState(true);
			return false;
		}

		return true;
	}

	public static void updateStatus(Object status)
	{
		if(!javax.swing.SwingUtilities.isEventDispatchThread())
		{
			throw new RuntimeException("Fatal error. Method called from worker thread!");
		}

		Helper.dbg(String.format("Updating status: %s", status), 1);
		statusLabel.setText(String.valueOf(status));
	}

	public static void startThread()
	{
		if(lockState())
		{
			Helper.dbg("Already started!");
			return;
		}

		GUI.updateStatus("Here we go...");

		worker = new Worker();
		worker.execute();
	}

	public static void stopThread()
	{
		if(!getState())
		{
			Helper.dbg("Not started!");
			return;
		}

		worker.cancel(true);
	}

	public static void doSomethingElse()
	{
		updateStatus("Oops!");
	}
}

class GUI_WindowAdapter extends java.awt.event.WindowAdapter
{
	@Override
	public void windowClosing(java.awt.event.WindowEvent windowEvent)
	{
		GUI.quit();
	}
}
