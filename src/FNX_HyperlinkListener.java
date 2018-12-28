/**
 * FNX_HyperlinkListener: Open HTML hyperlinks in browser
 * Copyright (C) 2019 Christian Schrötter <cs@fnx.li>
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

import java.awt.Desktop;

import java.net.URI;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class FNX_HyperlinkListener implements HyperlinkListener
{
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e)
	{
		if(e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
		{
			if(Desktop.isDesktopSupported())
			{
				try
				{
					Desktop.getDesktop().browse(new URI(e.getURL().toString()));
				}
				catch(Exception err)
				{
					err.printStackTrace();
				}
			}
		}
	}
}
