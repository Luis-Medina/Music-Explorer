/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package musicchecker;

import java.util.Calendar;
import java.util.Locale;

/**
 *
 * @author Luiso
 */
public class CheckedDate {

    private int day;
    private int week;

    public CheckedDate(int day, int week) {
        this.day = day;
        this.week = week;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public String getStringDay(){
        return Calendar.getInstance().getDisplayName(day, Calendar.LONG, Locale.getDefault());
    }

}
