/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luiso
 */
public class Commands {

    private static void runCommand(String file, String[] params) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(file);
        if (params != null) {
            for (String s : params) {
                list.add(s);
            }
        }
        ProcessBuilder pb = new ProcessBuilder(list);
        try {
            pb.start();
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public static void openMirc(String argumentString) {
        String[] params = argumentString.split(" ");
        runCommand("C:\\Program Files (x86)\\mIRC\\mirc.exe", params);
    }

    public static void openScript(XWeaselResult result) {
        String[] params = result.toString().split(", ");
        for(String s : params){
            System.out.println(s);
        }
        runCommand("F:\\Windows 7 Documents and Downloads\\Desktop\\test.exe", params);
    }
}
