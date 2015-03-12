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

public class NzbFileListPopupDelRowAction extends AbstractAction {

    /** main application object */
    private final HelloNzb mainApp;
    /** central logger object */
    /** selected rows (zero-based) */
    int[] selectedRows;

    public NzbFileListPopupDelRowAction(String name, HelloNzb f, int[] selectedRows) {
        this.mainApp = f;
        this.selectedRows = selectedRows;

        putValue(Action.NAME, Main.localer.getBundleText(name));
    }

    public void actionPerformed(ActionEvent e) {
        int count = 0;
        for (int row : selectedRows) {
            mainApp.removeNzbFileQueueRow(row - count, false);
            count++;
        }
    }
}
