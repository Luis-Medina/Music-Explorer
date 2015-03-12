/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Luiso
 */
public class PartsRenderer extends JLabel implements TableCellRenderer {

    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;
    private static Color rowColor = UIManager.getColor("Table.alternateRowColor");

    // Constructor for JLabel.
    public PartsRenderer(boolean isBordered) {
        this.isBordered = isBordered;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        if (value != null) {
            String text = value.toString();
            String numbers[] = text.split(" / ");
            if (Integer.parseInt(numbers[0].trim()) == Integer.parseInt(numbers[1].trim())) {
                setForeground(Color.getHSBColor((float) 0.3, (float) 0.97, (float) 0.97));
            } 
            else if (Integer.parseInt(numbers[0].trim()) > Integer.parseInt(numbers[1].trim())) {
                setForeground(Color.BLUE);
            }else {
                setForeground(Color.RED);
            }
            setText(value.toString());
        }
        else{
            setText("");
        }
        /*
        if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                            table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                            table.getBackground());
                }
                setBorder(unselectedBorder);
            }
        } 
         */
        //else {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                //setBackground(Color.BLACK);
                if(row % 2 == 1){
                    setBackground(rowColor);
                }
                else{
                    setBackground(Color.WHITE);
                }
            }
        //}
        return this;
    }
}
