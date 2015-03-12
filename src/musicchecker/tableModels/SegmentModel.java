/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.tableModels;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import at.lame.hellonzb.parser.DownloadFileSegment;

/**
 *
 * @author Luis
 */
public class SegmentModel extends AbstractTableModel {

    private ArrayList<DownloadFileSegment> datalist;
    private String[] columns = {"Filename", "Article ID"};

    public int getRowCount() {
        return datalist.size();
    }

    public int getColumnCount() {
        return columns.length;
    }

    public String getColumnName(int col) {
        return columns[col];
    }

    public SegmentModel() {
        datalist = new ArrayList<DownloadFileSegment>();
    }

    public SegmentModel(ArrayList l) {
        datalist = l;
    }
    
    public void add(DownloadFileSegment s){
        datalist.add(s);
        this.fireTableDataChanged();
    }
    
    public ArrayList<DownloadFileSegment> getAll(){
        return datalist;
    }

    public Object getValueAt(int row, int col) {
        DownloadFileSegment v = (DownloadFileSegment) datalist.get(row);
        switch (col) {
            case 0:
                return v.toString();
            case 1:
                return v.getArticleId();
            default:
                return null;
        }
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 0:
                return String.class;
            case 1:
                return Integer.class;
            default:
                return Object.class;
        }
    }

    public boolean isCellEditable() {
        return false;
    }

    public void removeDownloadFiles(DownloadFileSegment s) {
        datalist.remove(s);
        fireTableDataChanged();
    }

    public DownloadFileSegment getResultAt(int row) {
        return (DownloadFileSegment) datalist.get(row);
    }

    public void clear(){
        datalist = new ArrayList<DownloadFileSegment>();
        fireTableDataChanged();
    }
    
}


