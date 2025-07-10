/**
 * HTGT_Background.java: Legacy thread implementation
 * Copyright (C) 2016-2025 Christian Schrötter <cs@fnx.li>
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

class HTGT_Background implements Runnable
{
	public static final int EXEC_UPDATECHECK = 1;
	public static final int EXEC_DLLCHECK    = 3;

	private int exec;

	public HTGT_Background(int exec)
	{
		this.exec = exec;
	}

	@Override
	public void run()
	{
		switch(this.exec)
		{
			case EXEC_UPDATECHECK:
				HTGT.updateCheck(false, true);
				break;

			case EXEC_DLLCHECK:
				HTGT.updateCheckDLL(false, true);
				break;
		}
	}
}

// TODO: Migrate to SwingWorker?
// ...
