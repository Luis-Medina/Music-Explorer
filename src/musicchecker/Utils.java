/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 *
 * @author Luiso
 */
public class Utils {

    public static void openParNRar(String dir) {
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("C:\\Program Files (x86)\\ParNRar\\ParNRar.exe");
        cmd.add("/min");
        cmd.add("/m \"" + dir + "\"");
        cmd.add("/go");
        String[] cmdArray = cmd.toArray(new String[]{});

        // execute command
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec(cmdArray, null, new File(Main.getAppDir()));
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public static boolean areFromSameMedia(BinsearchResult result1, BinsearchResult result2) {
        String file1 = result1.getFilename().trim().replaceAll("[^a-zA-Z0-9]", "");
        String file2 = result2.getFilename().trim().replaceAll("[^a-zA-Z0-9]", "");
        boolean same = false;
        int differences = 0;
        if (result1.getPoster().equals(result2.getPoster())) {
            if (file1.length() == file2.length()) {
                for (int i = 0; i < file1.length(); i++) {
                    if (file1.charAt(i) != file2.charAt(i)) {
                        differences++;
                    }
                }
                if (differences == 1) {
                    same = true;
                }
            }
        }
        return same;
    }

    public static String capitalizeFirstLetters(String string) {
        String tokens[] = string.split(" ");
        String toReturn = "";
        for (String s : tokens) {
            if (s.equalsIgnoreCase("DJ") || s.equalsIgnoreCase("TV")
                    || s.equalsIgnoreCase("TV") || s.matches("s\\d+e\\d+")) {
                toReturn += s.toUpperCase() + " ";
            } else {
                toReturn += s.substring(0, 1).toUpperCase();
                toReturn += s.substring(1, s.length()) + " ";
            }
        }
        return toReturn.trim();
    }

    public static String normalizeForFilesystem(String s) {
        String temp = s.replaceAll("<,>,\",/,\\,|,?,*", "");
        return temp.replace(":", "-");
    }

    private static void runCommand(String file, String params) {
        try {
            new ProcessBuilder(file).start();
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public static void shutdownWinpreXP() {
        runCommand("RUNDLL32.EXE", "user32,ExitWindowsEx");
    }

    public static void shutdownWinXP() {
        runCommand("shutdown.exe", "-s -f");
    }

    public static void shutdownWindows() {
        try {
            shutdownWinXP();
        } catch (Exception e) {
            shutdownWinpreXP();
        }
    }

    public static void shutdownLinux() {
        runCommand("shutdown", "now");
    }

    public static boolean calculateWeekPassed() {
        Date lastWeekChecked = JDBC.getLastWeekChecked();
        if (lastWeekChecked != null) {
            Calendar last = Calendar.getInstance();
            last.setTime(lastWeekChecked);
            last.add(Calendar.DAY_OF_MONTH, 7);
            if (last.before(Calendar.getInstance())) {
                return true;
            }
        }

        return false;
    }

    public static String getLastWeek(String currentWeek) {
        return addToDate(currentWeek, -7);
    }

    public static String addToDate(String currentWeek, int toAdd) {
        Date date = null;
        try {
            date = new SimpleDateFormat("MMM d", Locale.ENGLISH).parse(currentWeek);
        } catch (ParseException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
        cal.add(Calendar.DAY_OF_MONTH, toAdd);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        String syear = Integer.toString(year).substring(2, 4);
        return month + "/" + day + "/" + syear;

    }

    public static Object intToBool(Object o) {
        if (o.getClass() == Integer.class) {
            if ((Integer) o == 0) {
                return false;
            }
            if ((Integer) o == 1) {
                return true;
            }
        }
        return o;
    }

    public static String intToName(int number) {
        switch (number) {
            case 1:
                return "One";
            case 2:
                return "Two";
            case 3:
                return "Three";
            case 4:
                return "Four";
            case 5:
                return "Five";
            case 6:
                return "Six";
            case 7:
                return "Seven";
            case 8:
                return "Eight";
            case 9:
                return "Nine";
            case 10:
                return "Ten";
            default:
                return "";
        }
    }
    public static String[] seasonNumbersList = {"First", "Second", "Third", "Fourth", "Fifth",
        "Sixth", "Seventh", "Eight", "Ninth", "Tenth"};

    public static void processParseError(String error) {
        if (!(Boolean) Main.prefs.get("ignoreerrors")) {
            JOptionPane.showMessageDialog(null, error);
        } else {
            Main.addError(error + "\n\n");
        }
    }
}
