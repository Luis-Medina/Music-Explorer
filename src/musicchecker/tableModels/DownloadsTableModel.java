/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.tableModels;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import musicchecker.Download;
import musicchecker.Main;
import musicchecker.Media;

/**
 *
 * @author Luiso
 */
public class DownloadsTableModel extends AbstractTableModel {

    // These are the names for the table's columns.
    private static final String[] columnNames = {"Artist", "Title", "Size",
        "Progress", "ETA", "Rate", "Status"};
    // These are the classes for each column's values.
    private static final Class[] columnClasses = {String.class, String.class,
        Integer.class, JProgressBar.class, String.class, String.class, String.class};
    // The table's list of downloads.
    private ArrayList<Download> downloadList = new ArrayList();

    public DownloadsTableModel() {
        downloadList = new ArrayList<Download>();
    }

    public DownloadsTableModel(ArrayList<Download> list) {
        downloadList = list;
    }

    // Add a new download to the table.
    public void addDownload(Download download) {
        downloadList.add(download);
        // Fire table row insertion notification to table.
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    // Get a download for the specified row.
    public Download getDownload(int row) {
        return (Download) downloadList.get(row);
    }

    // Remove a download from the list.
    public Download clearDownload(int row) {
        Download d = (Download) downloadList.remove(row);
        // Fire table row deletion notification to table.
        fireTableRowsDeleted(row, row);
        return d;
    }

    // Remove a download from the list.
    public void clearDownload(Download d) {
        for (int i = 0; i < downloadList.size(); i++) {
            if (downloadList.get(i).getDownId() == d.getDownId()) {
                System.out.println("Found download to delete " + d);
                clearDownload(i);
                break;
            }
        }

    }

    public ArrayList<Download> getList() {
        return downloadList;
    }

    // Get table's column count.
    public int getColumnCount() {
        return columnNames.length;
    }

    // Get a column's name.
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    // Get a column's class.
    @Override
    public Class getColumnClass(int col) {
        return columnClasses[col];
    }

    // Get table's row count.
    public int getRowCount() {
        return downloadList.size();
    }

    // Get value for a specific row and column combination.
    public Object getValueAt(int row, int col) {

        Download download = (Download) downloadList.get(row);
        switch (col) {
            case 0: // Artist
                return download.getArtist();
            case 1: // Title
                return download.getTitle();
            case 2: // Size
                long size = download.getSize();
                return (size == -1) ? "" : processSize(size);
            case 3: // Progress
                return download.getProgress();
            case 4: // ETA
                return download.getEta();
            case 5: // Rate
                if(download.getStatus() == Download.COMPLETE){
                    return "";
                }
                double rate = download.getRate();
                return String.format("%1$.1f", rate);
            case 6: // Status
                return Download.STATUSES[download.getStatus()];
        }
        return "";
    }

    /*Update is called when a Download notifies its
    observers of any changes */
    public void update(Observable o, Object arg) {
        int index = downloadList.indexOf(o);
        // Fire table row update notification to table.
        fireTableRowsUpdated(index, index);
    }

    private String processSize(long i) {
        BigDecimal file = new BigDecimal(i);
        BigDecimal megabyte = new BigDecimal("1048576");
        BigDecimal result = file.divide(megabyte, new MathContext(6));
        DecimalFormat df = new DecimalFormat("0.00 MB");
        String formatted = df.format(result);
        return formatted;
    }

    public void setDownloadStatus(int id, int status) {
        for (int i = 0; i < downloadList.size(); i++) {
            Download d = downloadList.get(i);
            if (d.getDownId() == id) {
                d.setStatus(status);
                if (status == Download.COMPLETE) {
                    d.setProgress(100);
                    d.getMedia().setDownloaded(true);
                    if (d.getMedia().getType() == Media.SONG) {
                        Main.getSongsTableModel().updateRow(d.getMedia());
                    } else if (d.getMedia().getType() == Media.MOVIE) {
                        Main.getMoviesTableModel().updateRow(d.getMedia());
                    } else if (d.getMedia().getType() == Media.ALBUM) {
                        Main.getAlbumsTableModel().updateRow(d.getMedia());
                    } else if (d.getMedia().getType() == Media.GAME) {
                        Main.getGamesTableModel().updateRow(d.getMedia());
                    }
                    d.setEta("");
                }
                fireTableRowsUpdated(i, i);
                break;
            }
        }
    }
    
    private static void clearRate(){
        
    }

    public void updateProgress(int id) {
        for (int i = 0; i < downloadList.size(); i++) {
            if (downloadList.get(i).getDownId() == id) {
                fireTableRowsUpdated(i, i);
                break;
            }
        }
    }

    public void setDownloadedSoFar(int id, long bytes) {
        for (int i = 0; i < downloadList.size(); i++) {
            Download d = downloadList.get(i);
            if (d.getDownId() == id) {
                //if (d.getStatus() != Download.CANCELLED) {
                    setValueAt(bytes, i, 3);
                    fireTableRowsUpdated(i, i);
                //}
                break;
            }
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 3) {
            Download d = (Download) downloadList.get(row);
            d.setDownloaded((Long)value);
            d.setProgress(d.getProgress());
            d.setRate(d.getRate());
            d.setEta(d.getEta());
        }
    }

    public Download getDownload(int id, boolean b) {
        for (Download d : downloadList) {
            if (d.getDownId() == id) {
                return d;
            }
        }
        return null;
    }
}

/*
class DownloadsTableModel extends AbstractTableModel
implements Observer {

// These are the names for the table's columns.
private static final String[] columnNames = {"Artist", "Title", "Size",
"Progress", "Status"};
// These are the classes for each column's values.
private static final Class[] columnClasses = {String.class, String.class,
String.class, JProgressBar.class, String.class};
// The table's list of downloads.
private ArrayList downloadList = new ArrayList();

public DownloadsTableModel() {
}

public DownloadsTableModel(ArrayList list) {
downloadList = list;
}

// Add a new download to the table.
public void addDownload(Download download) {

// Register to be notified when the download changes.
download.addObserver(this);

downloadList.add(download);

// Fire table row insertion notification to table.
fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
}

// Get a download for the specified row.
public Download getDownload(int row) {
return (Download) downloadList.get(row);
}

// Remove a download from the list.
public Download clearDownload(int row) {
Download d = (Download) downloadList.remove(row);

// Fire table row deletion notification to table.
fireTableRowsDeleted(row, row);
return d;
}

// Remove a download from the list.
public void clearDownload(Download d) {
for (int i = 0; i < downloadList.size(); i++) {
if (((Download)downloadList.get(i)).equals(d)) {
System.out.println(i);
clearDownload(i);
break;
}
}

}

public ArrayList getList() {
return downloadList;
}

// Get table's column count.
public int getColumnCount() {
return columnNames.length;
}

// Get a column's name.
public String getColumnName(int col) {
return columnNames[col];
}

// Get a column's class.
public Class getColumnClass(int col) {
return columnClasses[col];
}

// Get table's row count.
public int getRowCount() {
return downloadList.size();
}

// Get value for a specific row and column combination.
public Object getValueAt(int row, int col) {

Download download = (Download) downloadList.get(row);
switch (col) {
case 0: // Artist
return download.getSong().getArtist();
case 1: // Title
return download.getSong().getTitle();
case 2: // Size
int size = download.getSize();
return (size == -1) ? "" : processSize(size);
case 3: // Progress
return new Float(download.getProgress());
case 4: // Status
return Download.STATUSES[download.getStatus()];
}
return "";
}

Update is called when a Download notifies its
observers of any changes 
public void update(Observable o, Object arg) {
int index = downloadList.indexOf(o);
// Fire table row update notification to table.
fireTableRowsUpdated(index, index);
}

private String processSize(int i) {
BigDecimal file = new BigDecimal(i);
BigDecimal megabyte = new BigDecimal("1048576");
BigDecimal result = file.divide(megabyte, new MathContext(6));
DecimalFormat df = new DecimalFormat("0.00 MB");
String formatted = df.format(result);
return formatted;
}

}
 */
