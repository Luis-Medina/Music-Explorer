/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package musicchecker.tableModels;

import java.util.ArrayList;
import java.util.Calendar;
import musicchecker.Media;

/**
 *
 * @author Luiso
 */
public class MoviesTableModel extends MediaTableModel {

    public MoviesTableModel() {
        datalist = new ArrayList<Media>();
        columns = new String[]{"Title", "Downloaded", "Release Date"};
    }

    public MoviesTableModel(ArrayList l) {
        datalist = l;
        columns = new String[]{"Title", "Downloaded", "Release Date"};
    }

    @Override
    public Object getValueAt(int row, int col) {
        Media v = (Media) datalist.get(row);
        switch (col) {
            case 0:
                return v.getTitle();
            case 1:
                return v.isDownloaded();
            case 2:
                return v.getWeekReleased();
            default:
                return null;
        }
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 1:
                return Boolean.class;
            case 2:
                return Calendar.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 1)
            return true;
        return false;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case 1:
                if (value.toString().equals("true")) {
                    getMediaAt(row).setDownloaded(true);
                } else {
                    getMediaAt(row).setDownloaded(false);
                }
                return;
            default:
                return;
        }
    }

}

