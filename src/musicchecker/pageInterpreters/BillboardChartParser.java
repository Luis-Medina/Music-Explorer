/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.pageInterpreters;

import java.awt.Cursor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
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
 * @author Luis
 */
public class BillboardChartParser implements Runnable {

    private ArrayList<Media> list;
    private Main main;
    private int results;
    private String chartName;
    private String chartUrl;

    public BillboardChartParser(Main ab, int entries, String name, String url) {
        main = ab;
        list = new ArrayList<Media>();
        results = entries;
        chartName = name;
        chartUrl = url;
    }

    @Override
    public void run() {

        if(Main.stopSearch){
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main.setStatus("Getting " + chartName + " Chart ...");
                main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });

        try {
            Document doc = Jsoup.connect(chartUrl)
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
                media.setType(Media.SONG);
                media.setBillboardChart(chartName);
                try {
                    media.setWeeksOnChart(Integer.parseInt(weeksInChart));
                } catch (Exception e) {
                }
                
                list.add(media);
            }
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
            String error = "Error processing Billboard " + chartName + " chart!" + "\n" + ex.toString();
            Utils.processParseError(error);
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
                chartName = null;
                chartUrl = null;
            }
        });
    }
}
