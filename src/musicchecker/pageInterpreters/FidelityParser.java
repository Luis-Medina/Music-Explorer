/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.pageInterpreters;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import musicchecker.JDBC;
import musicchecker.Main;
import musicchecker.Media;
import musicchecker.Utils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;

/**
 *
 * @author Luis
 */
public class FidelityParser implements Runnable {

    private ArrayList<Media> list;
    private Main main;
    private String responseBody;

    public FidelityParser(Main ab) {
        main = ab;
        list = new ArrayList<Media>();
    }

    @Override
    public void run() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main.setStatus("Getting Fidelity Chart ...");
                main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
        list = new ArrayList<Media>();

        HttpClient httpclient = MyHttpClientFactory.getDefaultHttpClient();
        BufferedReader in;
        try {
            String url = "http://www.fidelitypr.com/";
            HttpGet httpget = new HttpGet(url);
            try {
                HttpResponse response = httpclient.execute(httpget);

                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() >= 300) {
                    throw new HttpResponseException(statusLine.getStatusCode(),
                            statusLine.getReasonPhrase());
                }
                HttpEntity entity = response.getEntity();
                in = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF8"));
                String r;
                while ((r = in.readLine()) != null) {
                    responseBody += r;
                }
            } catch (IOException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
                String error = "Error processing Fidelity chart!" + "\n" + ex.toString();
                Utils.processParseError(error);
            }

        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
            String[] spans = responseBody.split(">\\d. ");
            Media m;
            for (int i = 1; i < 6; i++) {
                String artist = spans[i].split(": ")[0];
                String title = spans[i].split(": ")[1];
                String realTitle = title.substring(0, title.indexOf("<br"));
                m = new Media(realTitle.trim());
                m.setArtist(artist.trim());
                m.setType(Media.SONG);
                m.setRank(i);
                m.setBillboardChart("Fidelity");
                list.add(m);
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

                main = null;
                list = null;
                responseBody = null;
            }
        });
    }
}
