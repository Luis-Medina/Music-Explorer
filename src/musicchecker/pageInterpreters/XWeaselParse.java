/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker.pageInterpreters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import musicchecker.Main;
import musicchecker.XWeaselResult;

/**
 *
 * @author Luiso
 */
public class XWeaselParse extends SwingWorker<ArrayList<XWeaselResult>, String> {

    private String url;
    private ArrayList<XWeaselResult> results;

    public XWeaselParse(String url) {
        this.url = url;
    }

    @Override
    protected ArrayList<XWeaselResult> doInBackground() throws Exception {
        //publish("j");
        results = new ArrayList<XWeaselResult>();
        BufferedReader in = null;
        try {
            URL site = new URL(url);
            URLConnection yc = site.openConnection();
            in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("xdcc://")) {
                    //System.out.println(inputLine);
                    String[] strings = inputLine.split("/");
                    String network = strings[3];
                    String channel = strings[4];
                    String bot = strings[5];
                    String pack = strings[6];
                    String filename = strings[7];
                    inputLine = in.readLine();
                    String[] sizes = inputLine.split("&nbsp;");
                    String size = sizes[0].substring(sizes[0].lastIndexOf(">") + 1, sizes[0].length()) + " " + sizes[1].substring(0, sizes[1].indexOf("<"));
                    in.readLine();
                    in.readLine();
                    inputLine = in.readLine();
                    String speed;
                    try {
                        String[] speeds = inputLine.split("&nbsp;");
                        speed = speeds[0].substring(speeds[0].lastIndexOf(">") + 1, speeds[0].length()) + " " + speeds[1].substring(0, speeds[1].indexOf("<"));
                        System.out.println(speed);
                    } catch (Exception e) {
                        speed = "N/A";
                    }
                    XWeaselResult result = new XWeaselResult(filename, size, bot, network, channel, speed, pack);
                    results.add(result);                    
                }
            }
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        return results;
    }

    @Override
    protected void process(List<String> strings) {
        Main.setStatus("Searching...");
    }

    @Override
    protected void done() {
        try {
            results = get();
            //Main.setResults(results);
        } catch (InterruptedException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
        Main.setStatus(results.size() + " result(s) found.");
        //Main.getLatch2().countDown();
        
        results = null;
        url = null;
    }
}
