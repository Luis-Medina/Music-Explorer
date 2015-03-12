/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package musicchecker;

import java.awt.Component;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Luiso
 */
class MyProgressRenderer extends JProgressBar
        implements TableCellRenderer {

    // Constructor for ProgressRenderer.
    public MyProgressRenderer(int min, int max) {
        super(min, max);
    }

  /* Returns this JProgressBar as the renderer
     for the given table cell. */
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        // Set JProgressBar's percent complete value.
        setValue((int) ((Float) value).floatValue());
        return this;
    }
}


