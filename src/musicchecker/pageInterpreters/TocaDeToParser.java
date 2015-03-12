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
public class TocaDeToParser implements Runnable {

    private ArrayList<Media> list;
    private Main main;

    public TocaDeToParser(Main ab) {
        main = ab;
        list = new ArrayList<Media>();
    }

    @Override
    public void run() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Main.setStatus("Getting Toca de To Chart ...");
                main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
        BufferedReader in = null;
        
        String url = "http://www.hot102pr.com/Hot102/20Online.html";
                //"http://www.hot102pr.com/Hot102/20Online.html";
        try {
            URL site = new URL(url);
            URLConnection yc = site.openConnection();
            yc.setConnectTimeout(10000);
            String contentType = yc.getHeaderField("Content-Type");
            String charset = null;
            if (contentType != null) {
                for (String param : contentType.replace(" ", "").split(";")) {
                    if (param.startsWith("charset=")) {
                        charset = param.split("=", 2)[1];
                        break;
                    }
                }
                if (yc.getInputStream() != null) {
                    if (charset == null) {
                        charset = "UTF-8";
                    }
                    in = new BufferedReader(new InputStreamReader(yc.getInputStream(), charset));
                    HTML2Text parser = new HTML2Text();
                    parser.parse(in);
                    in.close();
                    String resultingText = parser.getText();
                    String[] spans = resultingText.split("\\d. ");
                    Media m;
                    for (int i = 3; i < spans.length; i++) {
                        String[] real = spans[i].split("\n");
                        String title = real[0].split("-")[1];
                        String artist = real[0].split("-")[0];
                        m = new Media(title.trim());
                        m.setArtist(artist.replaceAll("<", "").trim());
                        m.setType(Media.SONG);
                        m.setRank(i);
                        m.setBillboardChart("Toca de To");
                        list.add(m);
                        System.out.println(m);
                    }
                }
            }
            else{
                System.err.println("Unable to reach website!");
            }
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
            String error = "Error processing Toca de To chart!" + "\n" + ex.toString();
            Utils.processParseError(error);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Main.getLogger().log(Level.SEVERE, null, ex);
                }
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

                list = null;
                main = null;
            }
        });
    }
}
