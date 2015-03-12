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
public class SongsTableModel extends MediaTableModel {
    

    public SongsTableModel() {
        datalist = new ArrayList<Media>();
        columns = new String[]{"Artist", "Title", "Rank", "Weeks", "Chart", "Downloaded"};
    }

    public SongsTableModel(ArrayList l) {
        datalist = l;
        columns = new String[]{"Artist", "Title", "Rank", "Weeks", "Chart", "Downloaded"};
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
                return v.getRank();
            case 3:
                return v.getWeeksOnChart();
            case 4:
                return v.getBillboardChart();
            case 5:
                return v.isDownloaded();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case 0:
                getMediaAt(row).setArtist((String) value);
                return;
            case 1:
                getMediaAt(row).setTitle((String) value);
                return;
            case 5:
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
                return Integer.class;
            case 3:
                return Integer.class;
            case 5:
                return Boolean.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if(column == 5) {
            return true;
        }
        return false;
    }

}
