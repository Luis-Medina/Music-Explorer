/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.tableModels;

import java.util.ArrayList;
import musicchecker.Media;

/**
 *
 * @author Luiso
 */
public class AlbumsTableModel extends MediaTableModel {
 
    public AlbumsTableModel() {
        datalist = new ArrayList<Media>();
        columns = new String[]{"Artist", "Title", "Downloaded", "Rank", "Weeks"};
    }

    public AlbumsTableModel(ArrayList l) {
        datalist = l;
        columns = new String[]{"Artist", "Title", "Downloaded", "Rank", "Weeks"};
    }

    @Override
    public Object getValueAt(int row, int col) {
        Media v = (Media) datalist.get(row);
        switch (col) {
            case 0:
                return v.getArtist();
            case 1:
                return v.getTitle();
            case 2:
                return v.isDownloaded();
            case 3:
                return v.getRank();
            case 4:
                return v.getWeeksOnChart();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case 2:
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

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 2:
                return Boolean.class;
            case 3:
                return Integer.class;
                case 4:
            return Integer.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (column == 2)
            return true;
        return false;
    }
   
}
