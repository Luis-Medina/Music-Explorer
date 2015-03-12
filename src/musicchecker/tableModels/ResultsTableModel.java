/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.tableModels;

import java.util.ArrayList;
import musicchecker.Media;
import musicchecker.XWeaselResult;

/**
 *
 * @author Luiso
 */
public class ResultsTableModel extends ResultsModel {

    private ArrayList<XWeaselResult> datalist;
    private String[] columns = {"Filename", "Size", "Speed", "Type", "Pack", "Bot", "Channel", "Network"};

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
    
    public ArrayList<XWeaselResult> getResults(){
        return datalist;
    }

    public ResultsTableModel() {
        datalist = new ArrayList<XWeaselResult>();
    }

    public ResultsTableModel(ArrayList l) {
        datalist = l;
    }

    @Override
    public Object getValueAt(int row, int col) {
        XWeaselResult v = (XWeaselResult) datalist.get(row);
        switch (col) {
            case 0:
                return v.getFilename();
            case 1:
                return Double.parseDouble(v.getSize().split(" ")[0]);
            case 2:
                return v.getSpeed();
            case 3:                
                return v.getExtension();
            case 4:
                return v.getPack();
            case 5:
                return v.getBot();
            case 6:
                return v.getChannel();
            case 7:
                return v.getNetwork();
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
                return Double.class;
            case 2:
                return Integer.class;
            case 3:
                return String.class;
            case 4:
                return Integer.class;
            case 5:
                return String.class;
            case 6:
                return String.class;
            case 7:
                return String.class;
            default:
                return Object.class;
        }
    }

    public boolean isCellEditable() {
        return false;
    }

    public void removeSong(Media s) {
        datalist.remove(s);
        fireTableDataChanged();
    }

    public XWeaselResult getResultAt(int row) {
        return (XWeaselResult) datalist.get(row);
    }

    public void clear(){
        datalist = new ArrayList<XWeaselResult>();
        fireTableDataChanged();
    }
    
}
