/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.pageInterpreters;

import java.awt.Cursor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import musicchecker.JDBC;
import musicchecker.Main;
import musicchecker.Media;
import musicchecker.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Luiso
 */
public class BillboardAlbumParse extends SwingWorker<ArrayList<Media>, Void> {

    //private String url;
    private Main ab;
    private JProgressBar progressBar;

    public BillboardAlbumParse(Main main, JProgressBar bar) {
        //this.url = url;
        ab = main;
        progressBar = bar;
    }

    @Override
    protected ArrayList<Media> doInBackground() throws Exception {
        ArrayList<Media> tempList = new ArrayList<Media>();
        if(Main.stopSearch){
            return tempList;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main.setStatus("Getting Billboard 200 List...");
                ab.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                progressBar.setVisible(true);
            }
        });

        
        String url = "http://www.billboard.com/charts/billboard-200?begin=1&order=position&decorator=service&confirm=true";
        try {
            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .get();
            Elements albums = doc.select("article.chart-row");
            for (Element album : albums) {
                String rank = album.select("span.this-week").text();
                String title = album.select("div.row-title > h2").text();
                String artist = album.select("div.row-title a").first().text();
                String weeksInChart = album.select("div.stats-weeks-on-chart span.value").text();

                Media media = new Media(artist, title);
                media.setRank(Integer.parseInt(rank));
                media.setType(Media.ALBUM);
                try {
                    media.setWeeksOnChart(Integer.parseInt(weeksInChart));
                } catch (Exception e) {
                }

                boolean found = false;
                for (Media m : tempList) {
                    if (m.compareTo(media) == 0) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    tempList.add(media);
                }
            }

        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
            String error = "Error processing Billboard200 chart!" + "\n" + ex.toString();
            Utils.processParseError(error);
        }

        return tempList;
    }

    @Override
    public void done() {
        ab.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        ArrayList<Media> mediaList;
        Main.setStatus("");
        try {
            mediaList = get();
            for (Media m : mediaList) {
                System.out.println(m);
            }
            ArrayList<Media> newList = JDBC.checkDownloaded(mediaList);
            newList = JDBC.checkIgnored(newList);
            JDBC.addListToSearchQueue(newList);
            Main.setAlbumsTableModel(newList);
            Main.setStatus("");
            newList = null;
        } catch (InterruptedException ignore) {
        } catch (java.util.concurrent.ExecutionException e) {
            String why = null;
            Throwable cause = e.getCause();
            if (cause != null) {
                why = cause.getMessage();
            } else {
                why = e.getMessage();
            }
            System.err.println("Error retrieving file: " + why);
            JOptionPane.showMessageDialog(null, "Error processing BillboardAlbums page!");
        } finally {
            ab = null;
            progressBar.setVisible(false);
        }

    }
}
