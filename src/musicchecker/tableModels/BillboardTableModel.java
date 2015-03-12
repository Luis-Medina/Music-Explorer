/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package musicchecker.tableModels;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import musicchecker.Media;

/**
 *
 * @author Luiso
 */
public class BillboardTableModel extends AbstractTableModel {

    private ArrayList<Media> datalist;
    private String[] columns = {"Artist", "Title", "Rank"};

    @Override
    public int getRowCount() {
        return datalist.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    public BillboardTableModel() {
        datalist = new ArrayList<Media>();
    }

    public BillboardTableModel(ArrayList l) {
        datalist = l;
    }

    public Object getValueAt(int row, int col) {
        Media v = (Media) datalist.get(row);
        switch (col) {
            case 0:
                return v.getArtist();
            case 1:
                return v.getTitle();
            case 2:
                return v.getRank();
            default:
                return null;
        }
    }

    public Media getSongAt(int row) {
        return (Media) datalist.get(row);
    }

    public Class getColumnClass(int col) {
        switch (col) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            default:
                return Object.class;
        }
    }

    public boolean isCellEditable() {
        return false;
    }

    public void removeSong(Media s){
        datalist.remove(s);
        fireTableDataChanged();
    }

    public void removeRow(int i){
        datalist.remove(i);
        fireTableRowsDeleted(i, i);
    }

    // Assumes rows are sent in reverse order
    public void removeRows(Integer[] array){
        for(int i : array){
            removeRow(i);
        }
    }

    public void removeAllRows(){
        for(int i=datalist.size()-1; i>=0; i--){
            removeRow(i);
        }
    }
}


