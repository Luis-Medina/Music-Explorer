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
package at.lame.hellonzb.tablemodels;

import at.lame.hellonzb.HelloNzbToolkit;
import at.lame.hellonzb.listener.actions.NzbFileListPopupMoveRowAction;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.StringLocaler;

import java.awt.Color;
import java.util.*;

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;

public class NzbFileQueueTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    /** The nzb files in the queue */
    protected Vector<NzbParser> nzbFileQueue;
    /** Column names vecotr */
    private Vector<String> columnNames;
    /** Column data vector<vector> */
    private Vector<Vector<Object>> tableData;
    /** String localer */
    private StringLocaler localer;

    public NzbFileQueueTableModel(StringLocaler localer) {
        this.localer = localer;

        this.nzbFileQueue = new Vector<NzbParser>();
        this.columnNames = new Vector<String>();
        this.tableData = new Vector<Vector<Object>>();

        // set column headers               
        this.columnNames.add("File");
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames.elementAt(columnIndex);
    }

    @Override
    public int getRowCount() {
        return tableData.size();
    }

    public Object getValueAt(int row, int col) {
        return tableData.get(row).get(col);
    }

    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public void moveRows(int[] selectedRows, int direction) {
        JProgressBar bar = null;

        // direction value valid?
        if ((direction != NzbFileListPopupMoveRowAction.DIRECTION_UP)
                && (direction != NzbFileListPopupMoveRowAction.DIRECTION_DOWN)) {
            return;
        }

        // first evaluate which rows we want to move (by name,
        // because row indices will change during this operation
        Vector<String> rows = new Vector<String>();
        for (int row : selectedRows) {
            bar = (JProgressBar) tableData.get(row).get(0);
            rows.add(bar.getName());
        }

        // move all these rows into the given direction
        int oldRow = -1;
        int newRow = -1;
        for (String name : rows) {
            oldRow = getRowByFilename(name);
            if (oldRow == 0 && direction == NzbFileListPopupMoveRowAction.DIRECTION_UP) {
                continue;
            }
            if (oldRow == (tableData.size() - 1) && direction == NzbFileListPopupMoveRowAction.DIRECTION_DOWN) {
                continue;
            }

            if (direction == NzbFileListPopupMoveRowAction.DIRECTION_UP) {
                newRow = oldRow - 1;
            } else if (direction == NzbFileListPopupMoveRowAction.DIRECTION_DOWN) {
                newRow = oldRow + 1 + selectedRows.length;
            } else {
                newRow = oldRow;
            }

            // move table data (JProgressBar object)
            Vector<Object> data = tableData.get(oldRow);
            tableData.insertElementAt(data, newRow);
            if (direction == NzbFileListPopupMoveRowAction.DIRECTION_UP) {
                tableData.remove(oldRow + 1);
            } else if (direction == NzbFileListPopupMoveRowAction.DIRECTION_DOWN) {
                tableData.remove(oldRow);
            }

            // move content (NzbParser)
            NzbParser parser = nzbFileQueue.get(oldRow);
            nzbFileQueue.insertElementAt(parser, newRow);
            if (direction == NzbFileListPopupMoveRowAction.DIRECTION_UP) {
                nzbFileQueue.remove(oldRow + 1);
            } else if (direction == NzbFileListPopupMoveRowAction.DIRECTION_DOWN) {
                nzbFileQueue.remove(oldRow);
            }
        }

        // to finish update the table's display
        fireTableRowsUpdated(0, tableData.size() - 1);
    }

    public void setValueAt(Object value, int row, int col) {
        JProgressBar progBar = (JProgressBar) tableData.get(row).get(col);
        progBar.setValue((Integer) value);
        fireTableCellUpdated(row, col);
    }

    /**
     * Increase the progress bar on the first line by one.
     * 
     * @param progress The new progress value to set
     */
    public void setRowProgress(NzbParser p, int progress) {
        for (int i = 0; i < nzbFileQueue.size(); i++) {
            NzbParser parser = nzbFileQueue.get(i);
            if (p == parser) {
                JProgressBar progBar = (JProgressBar) tableData.get(i).get(0);
                progBar.setValue(progress);
                fireTableRowsUpdated(i, i);
                break;
            }
        }
    }

    /**
     * Check whether or not the nzb file queue already contains
     * another nzb file with the same name as the passed parser.
     * 
     * @param parser The parser to check
     * @return true if the queue already contains a nzb file with that name
     */
    public boolean containsNzb(NzbParser parser) {
        for (int i = 0; i < nzbFileQueue.size(); i++) {
            String tmpId = nzbFileQueue.get(i).getId();
            System.out.println("In list " + tmpId);
        }
        
        
        String id = parser.getId();
        System.out.println("Checking " + id);
        for (int i = 0; i < nzbFileQueue.size(); i++) {
            String tmpId = nzbFileQueue.get(i).getId();
            System.out.println("In list " + tmpId);
            if (tmpId.equals(id)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method is called in order to add a new row to the end of the
     * table models data. The data to display for this new row is extracted
     * from a download file container.
     * 
     * @param parser The NzbParser object to add as new row
     */
    public void addRow(NzbParser parser) {
        Vector<Object> innerVector = new Vector<Object>();

        String filename = HelloNzbToolkit.getLastFilename(parser.getName());
        //filename += " (" + HelloNzbToolkit.prettyPrintFilesize(parser.getTotalSize()) + ")";

        int max = 100; // 100 %
        JProgressBar bar = new JProgressBar(0, max);
        //if(getRowCount() % 2 == 0){
        //    bar.setBackground(UIManager.getColor("Table.alternateRowColor"));
        //}
        //else{
        bar.setBackground(Color.WHITE);
        //}  
        bar.setName(filename);
        bar.setString(filename);
        bar.setStringPainted(false);
        bar.setValue(0);
        bar.setOpaque(true);
        bar.setFocusable(true);
        innerVector.add(bar);

        // add this new row to the table data
        nzbFileQueue.add(parser);
        tableData.add(innerVector);
        fireTableRowsInserted(0, tableData.size());
    }

    /**
     * This method removes the specified row in the table model.
     * 
     * @param parser The parser object to remove
     */
    public void removeRow(NzbParser parser) {
        for (int i = 0; i < nzbFileQueue.size(); i++) {
            if (parser == nzbFileQueue.get(i)) {
                removeRow(i);
                break;
            }
        }
    }

    /**
     * This method removes a row from the table model.
     * 
     * @param The row to remove identified by its filename value
     */
    public void removeRow(String filename) {
        int row = getRowByFilename(filename);

        if (row >= 0) {
            removeRow(row);
        }
    }

    /**
     * This method removes a row from the table model.
     * 
     * @param The row to remove identified by its index number
     */
    public void removeRow(int index) {
        if (index >= 0 && index < tableData.size()) {
            nzbFileQueue.remove(index);
            tableData.remove(index);
            fireTableRowsDeleted(0, tableData.size());
        }
    }

    /**
     * Return the NzbParser object at the given index.
     * 
     * @param idx The index of the item to retreive
     * @return The according NzbParser object, null if none found
     */
    public NzbParser getNzbParser(int idx) throws IllegalArgumentException {
        if (idx < 0 || idx > nzbFileQueue.size()) {
            throw new IllegalArgumentException();
        }

        return nzbFileQueue.get(idx);
    }

    /**
     * Return the whole vector of nzb parsers.
     * 
     * @return A vector/copy containing all NzbParser objects
     */
    @SuppressWarnings("unchecked")
    public Vector<NzbParser> copyQueue() {
        return (Vector<NzbParser>) nzbFileQueue.clone();
    }

    /**
     * Update the progress bar in the specified row.
     * 
     * @param filename The row to update is identified by this parameter
     */
    public void fireTableRowUpdated(String filename) {
        int row = getRowByFilename(filename);
        if (row > -1) {
            fireTableRowsUpdated(row, row);
        }
    }

    /**
     * Receives the filename of a row and returns the row number within
     * the data vector. If the filename is not found in this vecotr, the
     * method returns -1.
     * 
     * @param filename The filename to search for
     * @return The zero-based row number if row was found, -1 otherwise
     */
    private int getRowByFilename(String filename) {
        int row = 0;
        for (; row < tableData.size(); row++) {
            JProgressBar progBar = (JProgressBar) tableData.get(row).get(0);
            String cellText = progBar.getString();
            if (cellText.equals(filename)) {
                break;
            }
        }

        if (row == tableData.size()) {
            return -1;
        } else {
            return row;
        }
    }

    public void moveToTop(int selectedRow) {
        // move table data (JProgressBar object)
        Vector<Object> data = tableData.get(selectedRow);
        tableData.insertElementAt(data, 0);
        tableData.remove(selectedRow + 1);

        // move content (NzbParser)
        NzbParser parser = nzbFileQueue.get(selectedRow);
        nzbFileQueue.insertElementAt(parser, 0);
        nzbFileQueue.remove(selectedRow + 1);

        // to finish update the table's display
        fireTableDataChanged();
    }
    
    public void moveToBottom(int selectedRow) {
        
         // move table data (JProgressBar object)
        Vector<Object> data = tableData.get(selectedRow);
        tableData.remove(selectedRow);
        tableData.insertElementAt(data, tableData.size());

        // move content (NzbParser)
        NzbParser parser = nzbFileQueue.get(selectedRow);
        nzbFileQueue.remove(selectedRow);
        nzbFileQueue.insertElementAt(parser, nzbFileQueue.size());

        // to finish update the table's display
        fireTableDataChanged();
    }
    
}
