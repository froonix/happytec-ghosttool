import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

abstract class Helper
{
	private static DateFormat debugDate;

	public static void dbg(String msg, int trace)
	{
		if(debugDate == null)
		{
			debugDate = new SimpleDateFormat("HH:mm:ss.SSS");
		}

		String threadType;
		if(Thread.currentThread().getId() == 1)
		{
			threadType = "Root";
		}
		else
		{
			threadType = javax.swing.SwingUtilities.isEventDispatchThread() ? "Event" : "Worker";
		}

		System.out.printf("[%4$s] %2$s Thread ID %1$d: %5$s - %3$s%n", Thread.currentThread().getId(), threadType, Thread.currentThread().getStackTrace()[2 + trace].toString(), debugDate.format(new Date()), msg);
	}

	public static void dbgf(String msg, Object... args)
	{
		dbg(String.format(msg, args), 1);
	}

	public static void dbg(String msg)
	{
		dbg(msg, 1);
	}
}
