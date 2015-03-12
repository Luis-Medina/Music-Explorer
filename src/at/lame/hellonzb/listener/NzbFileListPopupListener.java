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
package at.lame.hellonzb.listener;

import at.lame.hellonzb.*;
import at.lame.hellonzb.listener.actions.*;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import musicchecker.Main;

public class NzbFileListPopupListener extends MouseAdapter {

    /** The table this listener is used for */
    private final JTable table;

    public NzbFileListPopupListener(JTable tab) {
        this.table = tab;
    }

    public void mouseClicked(MouseEvent me) {
        final int row = table.rowAtPoint(me.getPoint());
        final int col = table.columnAtPoint(me.getPoint());

        if (row < 0 || col < 0) {
            return;
        }

        // right-click?
        if ((me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            // was the right-clicked row already selected while right-clicked?
            if (!table.isCellSelected(row, col)) {
                table.setRowSelectionInterval(row, row);
            }

            popup(me, row, col, table.getSelectedRows());
        }
    }

    private void popup(MouseEvent me, int row, int col, int[] selectedRows) {
        // generate popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        if (Main.nzb.isDownloadActive()) {
            popupMenu.add(Main.localer.getBundleText("PopupErrorStopDownloadBeforeAction"));
            MenuElement[] elements = popupMenu.getSubElements();
            elements[0].getComponent().setEnabled(false);
            table.clearSelection();
        } else {
            popupMenu.add(new NzbFileListPopupDelRowAction(
                    "ContextMenuRemoveFromList", Main.nzb, selectedRows));
            popupMenu.add(new NzbFileListPopupDelRowAndDataAction(
                    "ContextMenuRemoveAndDelete", Main.nzb, selectedRows));
            popupMenu.add(new NzbFileListPopupMoveRowAction(
                    "ContextMenuMoveUp", Main.nzb, selectedRows,
                    NzbFileListPopupMoveRowAction.DIRECTION_UP));
            popupMenu.add(new NzbFileListPopupMoveRowAction(
                    "ContextMenuMoveDown", Main.nzb, selectedRows,
                    NzbFileListPopupMoveRowAction.DIRECTION_DOWN));
            if (table.getSelectedRowCount() == 1) {               
                popupMenu.add(new NzbFileListPopupMoveTopAction(Main.nzb, table.getSelectedRow()));
                popupMenu.add(new NzbFileListPopupMoveBottomAction(Main.nzb, table.getSelectedRow()));
            }

            MenuElement[] elements = popupMenu.getSubElements();

            if (selectedRows[0] == 0) {                
                elements[2].getComponent().setEnabled(false);
                if(elements[4] != null){
                    elements[4].getComponent().setEnabled(false);
                }
            }
            if (selectedRows[selectedRows.length - 1] == (table.getRowCount() - 1)) {
                elements[3].getComponent().setEnabled(false);
                if(elements[5] != null){
                    elements[5].getComponent().setEnabled(false);
                }
            }
        }

        // show popup menu
        popupMenu.show(table, me.getX(), me.getY());
    }
}
