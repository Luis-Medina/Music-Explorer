/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

/**
 *
 * @author Luiso
 */
public class PartsAvailable implements Comparable<PartsAvailable> {

    private int total;
    private int found;

    public PartsAvailable(int total, int found) {
        this.total = total;
        this.found = found;
    }

    public PartsAvailable(String parts) {
        String numbers[] = parts.split(" / ");
        found = Integer.parseInt(numbers[0].trim());
        total = Integer.parseInt(numbers[1].trim());
    }

    @Override
    public String toString() {
        return found + " / " + total;
    }

    public boolean isComplete() {
        if (found == total) {
            return true;
        }
        return false;
    }
    
    public boolean isOkay() {
        if (found >= total) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(PartsAvailable pa) {
        if (!pa.isComplete() && isComplete()) {
            return 1;
        } else if (pa.isComplete() && !isComplete()) {
            return -1;
        } else {
            if (this.total < pa.total) {
                return -1;
            } else if (this.total >= pa.total) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
