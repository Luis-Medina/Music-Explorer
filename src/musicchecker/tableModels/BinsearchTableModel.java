/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.tableModels;

import java.util.ArrayList;
import musicchecker.BinsearchResult;
import musicchecker.Media;
import musicchecker.PartsAvailable;

/**
 *
 * @author Luiso
 */
public class BinsearchTableModel extends ResultsModel {

    private ArrayList<BinsearchResult> datalist;
    private String[] columns = {"Filename", "Size", "Parts Available", "Poster"};

    @Override
    public int getRowCount() {
        return datalist.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }
    
    public ArrayList<BinsearchResult> getResults(){
        return datalist;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    public BinsearchTableModel() {
        datalist = new ArrayList<BinsearchResult>();
    }

    public BinsearchTableModel(ArrayList<BinsearchResult> l) {
        datalist = new ArrayList<BinsearchResult>();
        for (BinsearchResult br : l) {
            if (!br.isPasswordRequired()) {
                datalist.add(br);
            }
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        BinsearchResult v = (BinsearchResult) datalist.get(row);
        switch (col) {
            case 0:
                return v.getFilename();
            case 1:
                return v.getSize();
            case 2:
                return v.getPartsAvailable();
            case 3:
                return v.getPoster();
            default:
                return null;
        }
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 2:
                return PartsAvailable.class;
            default:
                return String.class;
        }
    }

    public boolean isCellEditable() {
        return false;
    }

    public void removeSong(Media s) {
        datalist.remove(s);
        fireTableDataChanged();
    }

    public BinsearchResult getResultAt(int row) {
        return (BinsearchResult) datalist.get(row);
    }

    public void clear() {
        datalist = new ArrayList<BinsearchResult>();
        fireTableDataChanged();
    }

    public void addResult(BinsearchResult br) {
        if (!br.isPasswordRequired()) {
            datalist.add(br);
        }
    }

}
