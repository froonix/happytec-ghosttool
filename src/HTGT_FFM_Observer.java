/**
 * HTGT_FFM_Observer.java: Fast-Follow-Mode worker class
 * Copyright (C) 2016-2024 Christian Schrötter <cs@fnx.li>
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
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import java.nio.file.attribute.FileTime;

import java.time.Instant;

import java.util.List;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

class HTGT_FFM_Observer extends SwingWorker<Integer,Integer>
{
	private File fileHandle;
	private boolean initState;
	private boolean queueState;
	private boolean firstRun;
	private FileTime oldTime;
	private FileTime newTime;
	private int currentTime;

	public void setFile(File f)
	{
		fileHandle = f;
	}

	public void firstRun()
	{
		firstRun = true;

		secondRun();
	}

	public void secondRun()
	{
		execute();
	}

	protected Integer doInBackground() throws IOException, InterruptedException
	{
		if(fileHandle == null)
		{
			throw new IllegalStateException("FFM not initialized");
		}

		// Ein schmutziger Hack, damit auf jeden Fall die GUI bereits
		// blockiert ist. Andernfalls könnte das zu unschönen Race-
		// Conditions führen, die noch nicht abgefangen werden.
		Thread.sleep(HTGT.FF_OBSERVER_DELAY);

		// Durch diesen kleinen Hack wird die Datei beim Start
		// auf jeden Fall einmal neu eingelesen. Dadurch sollte
		// es keine Probleme mehr geben, wenn User den FFM erst
		// zu spät starten. Wenn es keine Änderungen gibt, macht
		// das nichts, da das sowieso im Hintergrund passiert...
		oldTime = Files.getLastModifiedTime(fileHandle.toPath());
		FNX.dbgf("FFM background thread started: o=%d", oldTime.toMillis());

		if(firstRun)
		{
			FNX.dbg("This is the first run, triggering now!");
			publish((int) (oldTime.toMillis() / 1000) * -1);

			firstRun = false;
		}
		else
		{
			FNX.dbg("This is not the first run...");
			publish((int) (oldTime.toMillis() / 1000));
		}

		int m;
		while(true)
		{
			if(HTGT.ENABLE_WATCHSERVICE)
			{
				FNX.dbg("Preparing watchservice...");

				WatchService watchService = FileSystems.getDefault().newWatchService();

				File file = HTGT.getFile();
				String basename = file.getName();

				Path path = Paths.get(file.getParent().toString());
				path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

				FNX.dbg("Waiting for first event...");

				WatchKey key;
				Long last = 0L;
				while((key = watchService.take()) != null)
				{
					for(WatchEvent<?> event : key.pollEvents())
					{
						if(basename.equals(event.context().toString()))
						{
							newTime = Files.getLastModifiedTime(fileHandle.toPath());

							if(newTime.toMillis() > last)
							{
								last = newTime.toMillis();

								if(newTime.toInstant().isAfter(Instant.now().minusMillis(HTGT.FF_OBSERVER_DELAY)))
								{
									// publish((int) (newTime.toMillis() / 1000));
									Thread.sleep(HTGT.FF_OBSERVER_DELAY);

									newTime = Files.getLastModifiedTime(fileHandle.toPath());

									if(newTime.toMillis() > last)
									{
										FNX.dbgf("Watchservice event delayed: %s (o=%d n=%d)", event.kind(), oldTime.toMillis(), newTime.toMillis());

										continue;
									}
								}

								FNX.dbgf("Watchservice event received: %s (o=%d n=%d)", event.kind(), oldTime.toMillis(), newTime.toMillis());
								publish((int) (newTime.toMillis() / 1000) * -1);
								oldTime = newTime;
							}
							else
							{
								FNX.dbgf("Watchservice event ignored: %s (o=%d n=%d)", event.kind(), oldTime.toMillis(), newTime.toMillis());
							}
						}
					}
					key.reset();
				}
				break;
			}
			else
			{
				newTime = Files.getLastModifiedTime(fileHandle.toPath());
				m = 1;

				if(newTime.compareTo(oldTime) > 0)
				{
					// Durch diesen Teil sparen wir uns die Wartezeit vor dem Laden der XML-Datei.
					// Dadurch soll sichergestell werden, dass das Spiel mit dem Speichern fertig ist.
					// Klar, das ist auch kein 100% Schutz und es gibt zig andere problematische Stellen.
					// Aber es ist ein grundlegender Schutz, dass wir keine halbfertigen Dateien einlesen.
					if(newTime.toInstant().isAfter(Instant.now().minusMillis(HTGT.FF_OBSERVER_DELAY)))
					{
						FNX.dbgf("File modification time changed, but it's too early: o=%d n=%d d=%d", oldTime.toMillis(), newTime.toMillis(), HTGT.FF_OBSERVER_DELAY);
					}
					else
					{
						FNX.dbgf("File modification time changed: o=%d n=%d d=%d", oldTime.toMillis(), newTime.toMillis(), HTGT.FF_OBSERVER_DELAY);
						oldTime = newTime;
						m = -1;
					}
				}

				publish((int) (newTime.toMillis() / 1000) * m);
				Thread.sleep(HTGT.FF_CHECK_INTERVAL);
			}
		}

		return 0;
	}

	protected void process(List<Integer> chunks)
	{
		int modificationTime = 0;
		boolean invokeCheck = false;

		for(int i = 0; i < chunks.size(); i++)
		{
			int chunk = chunks.get(i);

			if(chunk < 0)
			{
				invokeCheck = true;
				modificationTime = chunk * -1;
			}
			else
			{
				modificationTime = chunk;

				if(currentTime != modificationTime)
				{
					initState = false;
				}
			}

			currentTime = modificationTime;
		}

		if(isCancelled())
		{
			FNX.dbg("Already cancelled!");
		}
		else if(invokeCheck || queueState)
		{
			if(queueState)
			{
				FNX.dbg("Executing earlier queue request(s)...");
			}

			if(!HTGT.fastFollowAnalyze())
			{
				FNX.dbg("Worker thread busy, waiting for next run...");

				queueState = true;
			}
			else
			{
				HTGT.fastFollowStatus(modificationTime);

				queueState = false;
				initState = true;
			}
		}
		else if(!initState)
		{
			HTGT.fastFollowStatus(modificationTime);
			initState = true;
		}
	}

	protected void done()
	{
		try
		{
			get();
		}
		catch(CancellationException e)
		{
			FNX.dbg("FFM background thread stopped.");
		}
		catch(InterruptedException e)
		{
			FNX.dbg("FFM background thread interrupted.");
			e.printStackTrace();
		}
		catch(ExecutionException e)
		{
			HTGT.exceptionHandler(e);
		}

		HTGT.fastFollowStop();

		FNX.dbg("FFM background thread finished.");
	}
}
