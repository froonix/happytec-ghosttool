/**
 * HTGT_FFM_Analyst.java: Fast-Follow-Mode worker class
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

import javax.swing.SwingWorker;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

class HTGT_FFM_Analyst extends SwingWorker<Integer,Integer>
{
	public HTGT_FFM_Analyst()
	{
		HTGT.fastFollowLock();
	}

	protected Integer doInBackground() throws Exception
	{
		FNX.dbg("FFM evaluation thread started.");

		return HTGT.fastFollowEvaluation();
	}

	protected void done()
	{
		try
		{
			Integer result = get();

			if(result < 1)
			{
				HTGT.fastFollowStop();
			}
			else
			{
				HTGT.fastFollowStatus();
			}
		}
		catch(CancellationException e)
		{
			FNX.dbg("FFM evaluation thread stopped.");
		}
		catch(InterruptedException e)
		{
			FNX.dbg("FFM evaluation thread interrupted.");
			e.printStackTrace();
		}
		catch(ExecutionException e)
		{
			FNX.dbg("FFM evaluation thread caused an exception...");

			HTGT.fastFollowStop();

			if(e.getCause() instanceof eSportsAPIException)
			{
				HTGT.APIError((eSportsAPIException) e.getCause());
			}
			else
			{
				HTGT.exceptionHandler(e);
			}
		}

		HTGT.fastFollowUnlock();

		FNX.dbg("FFM evaluation thread finished.");
	}
}
