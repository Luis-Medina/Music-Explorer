/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.pageInterpreters;

import at.lame.hellonzb.parser.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import musicchecker.BinsearchResult;
import musicchecker.Main;
import musicchecker.Media;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

/**
 *
 * @author Luiso
 */
public class BinsearchParse implements Runnable {

    //private String url;
    private Media media;
    private String responseBody;
    private String url;
    private ArrayList<BinsearchResult> results;
    private boolean doMore = true;
    private int pageNumber;
    private boolean ignoreForeign = true;
    private volatile boolean stopRequested = false;
    private HttpClient httpclient;
    private HttpGet httpget;

    public BinsearchParse(Media m, int number, String url) {
        //this.url = url;
        media = m;
        pageNumber = number;
        this.url = url;
    }

    public void requestStop() {
        stopRequested = true;
    }

    private void getResults() throws IllegalStateException {
        results = new ArrayList<BinsearchResult>();
        httpclient = MyHttpClientFactory.getDefaultHttpClient();
        if (!stopRequested) {
            try {
                if (pageNumber != 0) {
                    String toAdd = Integer.toString(pageNumber * 25);
                    url += "&min=" + toAdd;
                }
                if (pageNumber >= 20){
                    stopRequested = true;
                }
                System.out.println(url);
                httpget = new HttpGet(url);

                // Create a response handler
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                try {
                    responseBody = httpclient.execute(httpget, responseHandler);
                } catch (IOException ex) {
                    Main.getLogger().log(Level.SEVERE, null, ex);
                    if (ex.toString().toLowerCase().contains("timeout")) {
                        stopRequested = true;
                    }
                    JOptionPane.showMessageDialog(null, ex.toString());
                }
            } finally {
                if (responseBody != null) {
                    // When HttpClient instance is no longer needed,
                    // shut down the connection manager to ensure
                    // immediate deallocation of all system resources
                    httpclient.getConnectionManager().shutdown();
                    String tokens[] = responseBody.split("[\\r\\n]+");
                    String[] tokens2 = null;
                    for (String inputLine : tokens) {
                        if (inputLine.contains("checkbox") && inputLine.contains("<td><input type=") && !inputLine.contains("name=\"00000001\"")) {
                            tokens2 = inputLine.split("<input type=\"checkbox\"");
                            //results = getResults(tokens2);
                            break;
                        }
                    }
                    if (tokens2 != null) {
                        for (int i = 1; i < tokens2.length; i++) {
                            String s = tokens2[i];
                            String[] divs = s.split("\" ><td><span class=\"s\">");
                            String id = divs[0].substring(divs[0].lastIndexOf("\"") + 1);
                            String[] spanSplits = divs[1].split("</span>");
                            String rawFilename = spanSplits[0];
                            String extension;
                            try {
                                extension = rawFilename.substring(rawFilename.lastIndexOf("."), rawFilename.lastIndexOf(".") + 4);
                            } catch (Exception e) {
                                extension = "N/A";
                            }
                            String subject;
                            int endIndex;
                            if ((endIndex = rawFilename.lastIndexOf("&lt;")) > 0) {
                                rawFilename = rawFilename.substring(endIndex + 4);
                            }
                            if ((endIndex = rawFilename.lastIndexOf("&gt;")) > 0) {
                                rawFilename = rawFilename.substring(endIndex + 4);
                            }
                            if ((endIndex = rawFilename.lastIndexOf(".")) > 0) {
                                try {
                                    rawFilename = rawFilename.substring(0, endIndex + 4);
                                } catch (Exception e) {
                                }
                            }
                            subject = rawFilename.replaceAll("&quot;", "");

                            BinsearchResult result = new BinsearchResult(id, subject, extension);
                            media.setExtension(extension);
                            result.setQuery(url.substring(url.indexOf("index.php?q=") + 12));
                            result.setMedia(media);
                            String filename = removeFluff(result, StringUtils.getFilenameFromSubject(subject));
                            result.setFilename(filename);
                            if (s.contains("requires password")) {
                                result.setPasswordRequired(true);
                            }
                            if (s.contains(";max=250\">")) {
                                int index = s.indexOf(";max=250\">");
                                if (s.startsWith("collection", index + 10)) {
                                    result.setIsCollection(true);
                                }
                            }
                            String posterString;
                            if (spanSplits.length >= 3) {
                                String infoString = spanSplits[1];
                                int sizeIndex;
                                if ((sizeIndex = infoString.indexOf("</a> size: ")) != -1) {
                                    int partsAvailableIndex = infoString.indexOf("parts available") - 2;
                                    String size = infoString.substring(sizeIndex + 11, partsAvailableIndex);
                                    result.setSize(size.replace("&nbsp;", " "));
                                    String partsAvailable = infoString.substring(partsAvailableIndex + 19, infoString.indexOf("<", partsAvailableIndex));
                                    if (partsAvailable.contains("[")) {
                                        String parts2 = partsAvailable.replaceAll("\\[", "");
                                        result.setPartsAvailable(parts2);
                                    } else {
                                        // TO account for different formatting on Binsearch
                                        if (partsAvailable.trim().equals("")) {
                                            int start = infoString.indexOf(">", partsAvailableIndex + 19);
                                            int end = infoString.indexOf("<", start);
                                            String partsAvailable2 = infoString.substring(start + 1, end);
                                            result.setPartsAvailable(partsAvailable2.trim());
                                        } else {
                                            result.setPartsAvailable(partsAvailable.trim());
                                        }
                                    }
                                }
                                posterString = spanSplits[2];
                            } else {
                                posterString = spanSplits[1];
                            }
                            int hrefStart = posterString.indexOf("<a href=");
                            int posterStart = posterString.indexOf(">", hrefStart);
                            String poster = posterString.substring(posterStart + 1, posterString.indexOf("<", posterStart));
                            result.setPoster(poster);
                            if (ignoreForeign) {
                                if (!result.isIsForeign()) {
                                    results.add(result);
                                }
                            } else //if (media.extensionMatchesMedia(extension, media.getType())) {
                            {
                                results.add(result);
                            }
                            //}
                        }
                    }
                    if (responseBody.indexOf("25+ records") > -1) {
                        doMore = true;
                    } else {
                        doMore = false;
                    }
                }
            }
        }

        Main.setResults(results, doMore, ++pageNumber);
        Main.setResultsTotal();
    }

    @Override
    public void run() {
        while (doMore && !Thread.currentThread().isInterrupted() && !stopRequested) {
            //try {
            //Thread.sleep(500L);
            getResults();
            //Main.getLogger().log(Level.INFO, "Got results");

            //} 
            // catch(Exception e){
            //       Main.getLogger().log(Level.SEVERE, null, e);
            // }
        }

        // } catch (InterruptedException iex) {
        //Main.getLogger().log(Level.INFO, "Setting stop requested");
        stopRequested = true;
        media = null;
        responseBody = null;
        url = null;
        results = null;
        //   Main.getLogger().log(Level.INFO, null, iex);
        Thread.currentThread().interrupt();
        //Main.getLogger().log(Level.INFO, "Stopping thread");
        if (httpget != null) {
            httpget.abort();
        }
        if (httpclient != null) {
            httpclient.getConnectionManager().shutdown();
        }
        Main.resetSearchButtons();
        Main.searchDone();
        /*
         * SwingUtilities.invokeLater(new Runnable() {
         *
         * @Override public void run() { if (Main.stopSearch) { doMore = false;
         * } Main.setResults(results, doMore, ++pageNumber);
         * Main.setResultsTotal(); //Main.getLatch2().countDown();
         *
         * media = null; responseBody = null; url = null; results = null; } });
         *
         */
    }

    private static String removeFluff(BinsearchResult result, String s) {
        String[] tokens = s.split("\\[\\d+/\\d+]");
        if (tokens.length > 1) {
            String name1;
            if (tokens[0].indexOf("[") != -1 && tokens[0].indexOf("]") != -1) {
                if (tokens[0].lastIndexOf("[") > tokens[0].lastIndexOf("]")) {
                    name1 = tokens[0].substring(0, tokens[0].lastIndexOf("[") - 1);
                } else {
                    name1 = tokens[0].substring(tokens[0].lastIndexOf("[") + 1, tokens[0].lastIndexOf("]")).trim();
                }
            } else {
                name1 = tokens[0];
            }
            String splits[] = name1.toLowerCase().split("german");
            if (splits.length > 2) {
                result.setIsForeign(true);
            } else if (splits.length == 2) {
                if (result.getMedia().toString().toLowerCase().contains("german")) {
                    result.setIsForeign(false);
                } else {
                    result.setIsForeign(true);
                }
            } else {
                splits = name1.toLowerCase().split("french");
                if (splits.length > 2) {
                    result.setIsForeign(true);
                } else if (splits.length == 2) {
                    if (result.getMedia().toString().toLowerCase().contains("french")) {
                        result.setIsForeign(false);
                    } else {
                        result.setIsForeign(true);
                    }
                }
            }
            return trimEnd(name1) + " - " + trimStart(tokens[1]);
        } else {
            if (s.toLowerCase().contains("german") || s.toLowerCase().contains("french")) {
                result.setIsForeign(true);
            } else {
                result.setIsForeign(false);
            }
        }
        return s;
    }

    private static String trimStart(String s) {
        String temp;
        if (s.startsWith(" - ")) {
            temp = s.substring(2, s.length());
        } else if (s.startsWith("- ") || s.startsWith(" -")) {
            temp = s.substring(1, s.length());
        } else {
            temp = s;
        }
        return temp.trim();
    }

    private static String trimEnd(String s) {
        String temp;
        if (s.endsWith(" - ")) {
            temp = s.substring(0, s.length() - 2);
        } else if (s.endsWith("- ") || s.endsWith(" -")) {
            temp = s.substring(0, s.length() - 1);
        } else {
            temp = s;
        }
        return temp.trim();
    }
}
