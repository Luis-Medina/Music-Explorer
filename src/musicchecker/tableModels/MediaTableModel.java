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
public class MediaTableModel extends AbstractTableModel {

    protected ArrayList<Media> datalist;
    protected String[] columns;


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

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return datalist.get(rowIndex);
    }

    public void removeMedia(Media s) {
        for(int i=datalist.size()-1; i>=0; i--){
            if(datalist.get(i).equals(s)){
                removeRow(i);
                return;
            }
        }
    }

    public ArrayList<Media> getMedia() {
        return datalist;
    }

    public Media getMediaAt(int row) {
        return (Media) datalist.get(row);
    }

    public void removeRows(Integer[] array) {
        for (int i : array) {
            removeRow(i);
        }
    }

    public void removeRow(int i) {
        datalist.remove(i);
        fireTableRowsDeleted(i, i);
    }

    public void addMedia(Media m) {
        removeMedia(m);
        datalist.add(m);
    }

    public void addMediaList(ArrayList<Media> list) {
        for (Media m : list) {
            //System.out.println(m);
            addMedia(m);
        }
        fireTableDataChanged();
    }

    public void updateRow(Media media){
        for(int i=0; i<datalist.size(); i++){
            if(media.equals(datalist.get(i))){
                fireTableRowsUpdated(i, i);
                return;
            }
        }
    }

    public void clear(){
        datalist = new ArrayList<Media>();
        fireTableDataChanged();
    }

    public void clearDownloaded() {
        for (int i = datalist.size() - 1; i >= 0; i--) {
            if (datalist.get(i).isDownloaded()) {
                datalist.remove(i);
                fireTableRowsDeleted(i, i);
            }
        }
    }

}
