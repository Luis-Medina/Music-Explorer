/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package musicchecker.tableModels;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Observable;
import javax.swing.table.AbstractTableModel;
import musicchecker.Download;

/**
 *
 * @author Luiso
 */
public class DownloadedTableModel  extends AbstractTableModel {

    // These are the names for the table's columns.
    private static final String[] columnNames = {"Artist", "Title", "Size"};
    // These are the classes for each column's values.
    private static final Class[] columnClasses = {String.class, String.class,
        Integer.class};
    // The table's list of downloads.
    private ArrayList<Download> downloadList = new ArrayList();

    public DownloadedTableModel() {
        downloadList = new ArrayList<Download>();
    }

    public DownloadedTableModel(ArrayList<Download> list) {
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
            if (((Download) downloadList.get(i)).equals(d)) {
                //System.out.println(i);
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

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 3) {
            Download d = (Download) downloadList.get(row);
            d.setDownloaded((Integer) value);
            d.setProgress(d.getProgress());
            d.setRate(d.getRate());
            d.setEta(d.getEta());
        }
    }

    public Download getDownload(int id, boolean b){
        for(Download d : downloadList){
            if(d.getDownId() == id){
                return d;
            }
        }
        return null;
    }
}

