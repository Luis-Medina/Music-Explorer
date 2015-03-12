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


public class NzbFileListPopupMoveTopAction extends AbstractAction 
{	
	
	/** main application object */
	private final HelloNzb mainApp;

	/** selected rows (zero-based) */
	private int selectedRow;
	
	
	
	public NzbFileListPopupMoveTopAction(HelloNzb f, int selectedRow)
	{
		this.mainApp = f;
		this.selectedRow = selectedRow;
		
		putValue(Action.NAME, "Move to Top");
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		mainApp.moveRowsToTopNzbQueue(selectedRow);
		mainApp.clearNzbQueueSelection();
	}
}
