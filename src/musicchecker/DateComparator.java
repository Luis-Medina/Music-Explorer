/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luis
 */
public class DateComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        if(o1.toString().contains("eek"))
            return -1;
        if(o2.toString().contains("eek")){
            return -1;
        }
        Date date1 = null;
        try {
            date1 = new SimpleDateFormat("M/d/yy", Locale.ENGLISH).parse(o1.toString());
        } catch (ParseException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
        Date date2 = null;
        try {
            date2 = new SimpleDateFormat("M/d/yy", Locale.ENGLISH).parse(o2.toString());
        } catch (ParseException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
        return date1.compareTo(date2);
    }
    
    
    
}
