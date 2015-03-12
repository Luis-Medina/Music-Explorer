/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.pageInterpreters;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import musicchecker.HTML2Text;
import musicchecker.JDBC;
import musicchecker.Main;
import musicchecker.Media;
import musicchecker.Utils;

/**
 *
 * @author Luis
 */
public class Reggaeton94Parser implements Runnable {

    private ArrayList<Media> list;
    private Main main;

    public Reggaeton94Parser(Main ab) {
        main = ab;
        list = new ArrayList<Media>();
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Main.setStatus("Getting Reggaeton94 Chart ...");
                main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
        BufferedReader in = null;
        String url = "http://www.reggaeton947.com/top20.php";
        try {
            URL site = new URL(url);
            URLConnection yc = site.openConnection();
            yc.setConnectTimeout(10000);
            String contentType = yc.getHeaderField("Content-Type");
            String charset = null;
            for (String param : contentType.replace(" ", "").split(";")) {
                if (param.startsWith("charset=")) {
                    charset = param.split("=", 2)[1];
                    break;
                }
            }
            in = new BufferedReader(new InputStreamReader(yc.getInputStream(), charset));
            HTML2Text parser = new HTML2Text();
            parser.parse(in);
            in.close();
            String resultingText = parser.getText();
            int startIndex = parser.getText().indexOf("TOP 20 COUNTDOWN");
            String toParse = resultingText.substring(startIndex+25);
            String[] spans = toParse.split("\n");           
            Media m;
            for(int i=1; i<=20; i++){        
               String title = spans[i].split(" - ")[0];
               String artist = spans[i].split(" - ")[1];
               m = new Media(title);
               m.setArtist(artist);
               m.setType(Media.SONG);
               m.setRank(i);
               m.setBillboardChart("Reggaeton94");
               list.add(m);        
            }      
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
            String error = "Error processing Reggaeton94 chart!" + "\n" + ex.toString();
            Utils.processParseError(error);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                main.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                ArrayList<Media> newList = JDBC.checkDownloaded(list);
                newList = JDBC.checkIgnored(newList);
                JDBC.addListToSearchQueue(newList);
                Main.setSongsTableModel(newList);
                newList = null;
                Main.setStatus("");
                
                list = null;
                main = null;
            }
        });
    }
    
}

