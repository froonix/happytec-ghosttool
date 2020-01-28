import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CancellationException;
import javax.swing.SwingWorker;

class Worker extends SwingWorker
{
	protected String doInBackground() throws Exception
	{
		//GUI.updateStatus("This should throw an exception...");
		Helper.dbg("Starting background task...");

		for(int i = 10; i >= 0; i--)
		{
			Thread.sleep(1000);
			publish(i);
		}

		return "Finished Execution";
	}

	protected void process(List chunks)
	{
		int val = (Integer) chunks.get(chunks.size()-1);

		GUI.updateStatus(String.valueOf(val));
	}

	protected void done()
	{
		try
		{
			GUI.updateStatus((String) get());
		}
		catch(InterruptedException e)
		{
			GUI.updateStatus("InterruptedException");
		}
		catch(ExecutionException e)
		{
			GUI.updateStatus("ExecutionException");
		}
		catch(CancellationException e)
		{
			GUI.updateStatus("CancellationException");
		}

		GUI.setState(false);
	}
}
