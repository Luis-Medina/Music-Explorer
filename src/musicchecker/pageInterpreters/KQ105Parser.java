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
public class KQ105Parser implements Runnable {

    private ArrayList<Media> list;
    private Main main;

    public KQ105Parser(Main ab) {
        main = ab;
        list = new ArrayList<Media>();
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Main.setStatus("Getting KQ105 Chart ...");
                main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
        BufferedReader in = null;
        String url = "http://kq105.univision.com/";
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
            //System.out.println(resultingText);
            int startIndex = parser.getText().indexOf("TOP 10");
            int endIndex = parser.getText().indexOf("TRANSMISIONES EN VIVO");
            System.out.println("From: " + startIndex + " to " + endIndex);
            String toParse = resultingText.substring(startIndex, endIndex);
            String[] spans = toParse.split("div id=\"top");
            Media m;
            String[] toExtract = spans[0].split("\n");
            ArrayList<String> tempList = new ArrayList<String>();
            for(String s : toExtract){               
               if(!s.trim().equals("")){
                   tempList.add(s);
               }
            }
            for(int i=1; i<tempList.size()-1; i+=2){
               String title = tempList.get(i);
               String artist = tempList.get(i+1);
               m = new Media(title);
               m.setArtist(artist);
               m.setType(Media.SONG);
               m.setRank(i);
               m.setBillboardChart("KQ105");
               list.add(m);                  
               //System.out.println(m);
            }      
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
            String error = "Error processing KQ105 chart!" + "\n" + ex.toString();
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
                /*
                for(Media m : list){
                    System.out.println("Title: " + m.getTitle());
                    System.out.println("Artist: " + m.getArtist());
                }
                 * 
                 */
                main.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                ArrayList<Media> newList = JDBC.checkDownloaded(list);
                newList = JDBC.checkIgnored(newList);
                JDBC.addListToSearchQueue(newList);
                Main.setSongsTableModel(newList);
                newList = null;
                Main.setStatus("");
                main = null;
                list = null;
            }
        });
    }
    
}
