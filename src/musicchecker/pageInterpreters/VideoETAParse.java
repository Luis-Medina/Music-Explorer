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
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

/**
 *
 * @author Luiso
 */
public class VideoETAParse extends SwingWorker<ArrayList<Media>, Void> {

    private String url;
    private Main ab;
    private static String responseBody;
    private JProgressBar progressBar;

    public VideoETAParse(Main ab, JProgressBar bar) {
        //this.url = url;        
        this.ab = ab;
        progressBar = bar;
    }

    @Override
    protected ArrayList<Media> doInBackground() throws Exception {
        ArrayList<Media> tempList = new ArrayList<Media>();
        if (Main.stopSearch) {
            return tempList;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Main.setStatus("Getting Video ETA List...");
                ab.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                progressBar.setVisible(true);
            }
        });

        for (int j = -1; j <= 2; j++) {
            HttpClient httpclient = MyHttpClientFactory.getDefaultHttpClient();
            try {
                url = Main.videoetaURL;
                String realURL = url.replace("interval=0", "interval=" + j);
                HttpGet httpget = new HttpGet(realURL);

                // Create a response handler
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                responseBody = httpclient.execute(httpget, responseHandler);
                System.out.println("----------------------------------------");
            } catch (IOException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
                String error = "Error processing VideoETA chart!" + "\n" + ex.toString();
                Utils.processParseError(error);
            } finally {
                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                httpclient.getConnectionManager().shutdown();
                int start = responseBody.indexOf("<table class=\"listing ruled\">");
                int end = responseBody.indexOf("</table>", start);
                String located = responseBody.substring(start, end);
                int dateStart = responseBody.indexOf("<h1>Week of ");
                int dateEnd = responseBody.indexOf("</h1>", dateStart);
                String rawDate = responseBody.substring(dateStart, dateEnd + 5);
                String date = rawDate.substring(12, rawDate.indexOf("</h1>"));
                String[] tokens = located.split("<td><a href=\"/movie/");
                //String located2;
                for (int i = 1; i < tokens.length; i++) {
                    int start1 = tokens[i].indexOf("<b>") + 3;
                    String title = tokens[i].substring(start1, tokens[i].lastIndexOf("</b>")).trim();
                    if (!shouldIgnore(title)) {
                        Media media = new Media(title);
                        media.setType(Media.MOVIE);
                        media.setArtist("(MOVIE)");
                        media.setWeekReleased(Utils.addToDate(date, 0));
                        //if (JDBC.checkIfNextWeek(media)) {
                        tempList.add(media);
                        //}
                    }
                }
            }
        }
        return tempList;
    }

    private static boolean containsNone(int start) {
        int startNoneIndex = responseBody.indexOf(">more</a>", start);
        //50 is arbitrary to include none<br
        String checkForNone = responseBody.substring(startNoneIndex, startNoneIndex + 50);
        String[] noneChecks = checkForNone.split("\n");
        if (noneChecks[1].indexOf("None<") > -1) {
            return true;
        }
        return false;
    }

    private static boolean shouldIgnore(String title) {
        for (String seasonNumber : Utils.seasonNumbersList) {
            if (title.endsWith(": The Complete " + seasonNumber + " Season")) {
                return true;
            }
            if (title.endsWith(": The " + seasonNumber + " Season")) {
                return true;
            }
            if (title.endsWith(": " + seasonNumber + " Season")) {
                return true;
            }
        }
        for (int i = 1; i <= 10; i++) {
            if (title.contains(": Season " + Utils.intToName(i))) {
                return true;
            }
            if (title.contains(": Season " + i)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void done() {
        ab.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        System.out.println("I'm done for VIDEOETA");
        Main.setStatus("");
        ArrayList<Media> mediaList;
        try {
            mediaList = get();
            ArrayList<Media> newList = JDBC.checkDownloaded(mediaList);
            ArrayList<Media> gameList = new ArrayList<Media>();
            ArrayList<Media> movieList = new ArrayList<Media>();
            for (Media s : newList) {
                if (!JDBC.checkIgnored(s)) {
                    if (s.getArtist().equals("(GAME)")) {
                        gameList.add(s);
                    } else {
                        movieList.add(s);
                    }
                }
            }
            JDBC.addListToSearchQueue(movieList);
            Main.setMoviesTableModel(movieList);

            //  Main.setGamesTableModel(gameList);
            gameList = null;
            movieList = null;
            newList = null;
            Main.setStatus("");
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
            String error = "Error processing VideoETA chart!" + "\n" + e.toString();
            Utils.processParseError(error);
        }
        progressBar.setVisible(false);
    }
}
