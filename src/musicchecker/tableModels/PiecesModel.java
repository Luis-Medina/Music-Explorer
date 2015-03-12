/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.tableModels;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import at.lame.hellonzb.parser.DownloadFile;
import java.util.Vector;

/**
 *
 * @author Luis
 */
public class PiecesModel extends AbstractTableModel {

    private Vector<DownloadFile> datalist;
    private Boolean[] selected;
    private String[] columns = {"Select", "Filename", "Size"};

    public int getRowCount() {
        return datalist.size();
    }

    public int getColumnCount() {
        return columns.length;
    }

    public String getColumnName(int col) {
        return columns[col];
    }

    public PiecesModel() {
        datalist = new Vector<DownloadFile>();
    }

    public PiecesModel(Vector l) {
        datalist = l;
        for(DownloadFile df : datalist){
            df.setSelected(true);
        }
    }

    public void add(DownloadFile s) {
        datalist.add(s);
        fireTableDataChanged();
    }

    @Override
    public Object getValueAt(int row, int col) {
        DownloadFile v = (DownloadFile) datalist.get(row);
        switch (col) {
            case 0:
                return v.isSelected();
            case 1:
                return v.getFilename();
            case 2:
                long size = v.getTotalFileSize();
                return (size == -1) ? "" : processSize(size);
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case 0:
                if (value.toString().equals("true")) {
                    getResultAt(row).setSelected(true);
                } else {
                    getResultAt(row).setSelected(false);
                }
                return;
            default:
                return;
        }
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            default:
                return Object.class;
        }
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
    public boolean isCellEditable(int row, int column) {
        if (column == 0) {
            return true;
        }
        return false;
    }

    public void removeDownloadFiles(DownloadFile s) {
        datalist.remove(s);
        fireTableDataChanged();
    }

    public DownloadFile getResultAt(int row) {
        return (DownloadFile) datalist.get(row);
    }

    public void clear() {
        datalist = new Vector<DownloadFile>();
        fireTableDataChanged();
    }
    
    public Vector<DownloadFile> getFiles(){
        return datalist;
    }
    
}
