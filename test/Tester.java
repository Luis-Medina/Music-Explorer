
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import musicchecker.CheckedDate;
import musicchecker.HTML2Text;
import musicchecker.Media;
import musicchecker.Utils;
import org.apache.commons.io.*;
import musicchecker.pageInterpreters.Reggaeton94Parser;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author Luiso
 */
public class Tester {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException{
        String regex = "r[/d/d]";
        //while (true) {
            String result = ".*?\\.r\\d+$";
            Pattern pattern = Pattern.compile(result);

            String result1 = "exvid.r02-movie43.r0d";
            Matcher matcher =  pattern.matcher(result1);

            boolean found = false;
            if (matcher.find()) {
                System.out.format("I found the text" +
                    " \"%s\" starting at " +
                    "index %d and ending at index %d.%n",
                    matcher.group(),
                    matcher.start(),
                    matcher.end());
                found = true;
            }
            if(!found){
                System.out.println("No match found.%n");
            }
        //}
    }
}
