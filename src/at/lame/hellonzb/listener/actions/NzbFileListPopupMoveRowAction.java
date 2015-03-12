/*******************************************************************************
 * HelloNzb -- The Binary Usenet Tool
 * Copyright (C) 2010-2011 Matthias F. Brandstetter
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.lame.hellonzb.listener.actions;

import at.lame.hellonzb.*;

import java.awt.event.*;
import javax.swing.*;
import musicchecker.Main;


public class NzbFileListPopupMoveRowAction extends AbstractAction 
{	
	public static final int DIRECTION_UP   = 1;
	public static final int DIRECTION_DOWN = 2;
	
	/** main application object */
	private final HelloNzb mainApp;

	/** selected rows (zero-based) */
	private int [] selectedRows;
	
	/** move into which direction, up or down? */
	private int direction; 
	
	
	public NzbFileListPopupMoveRowAction(String name, HelloNzb f, int [] selectedRows, int dir)
	{
		this.mainApp = f;
		this.selectedRows = selectedRows;
		this.direction = dir;
		
		putValue(Action.NAME, Main.localer.getBundleText(name));
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		mainApp.moveRowsInNzbQueue(selectedRows, direction);
		mainApp.clearNzbQueueSelection();
	}
}
