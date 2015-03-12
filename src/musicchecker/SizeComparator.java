/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.util.Comparator;

/**
 *
 * @author Luis
 */
public class SizeComparator implements Comparator {

    public SizeComparator(){
        
    }
    @Override
    public int compare(Object o1, Object o2) {
        String[] tokens1 = o1.toString().split(" ");
        String[] tokens2 = o2.toString().split(" ");
        double i1 = Double.parseDouble(tokens1[0]);
        double i2 = Double.parseDouble(tokens2[0]);
        int unit1 = toNumber(tokens1[1]);
        int unit2 = toNumber(tokens2[1]);
        if (unit1 > unit2) {
            return 1;
        } else if (unit1 < unit2) {
            return -1;
        } else {
            if (i1 > i2) {
                return 1;
            } else if (i1 < i2) {
                return -1;
            }
            return 0;
        }
    }

    private static int toNumber(String unit) {
        if (unit.contains("GB")) {
            return 3;
        }
        if (unit.contains("MB")) {
            return 2;
        } else {
            return 1;
        }
    }
}
