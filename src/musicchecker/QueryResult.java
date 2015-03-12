/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.util.ArrayList;

/**
 *
 * @author Luis
 */
public class QueryResult {
    
    private String[] columns;
    private ArrayList<ArrayList> rowData;

    public QueryResult(int size) {
        columns = new String[size];
        rowData = new ArrayList<ArrayList>();
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public ArrayList<ArrayList> getRowData() {
        return rowData;
    }

    public void setRowData(ArrayList<ArrayList> rowData) {
        this.rowData = rowData;
    }
    
    public void addResultRow(ArrayList list){
        rowData.add(list);
    }   
    
    public void setColumn(String name, int index){
        columns[index] = name;
    }
    
}
