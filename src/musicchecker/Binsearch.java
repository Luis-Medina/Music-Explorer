/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.io.*;
import at.lame.hellonzb.HelloNzbToolkit;
import at.lame.hellonzb.parser.NzbParserCreator;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.swing.JProgressBar;

/**
 *
 * @author Luiso
 */
public class Binsearch implements Runnable {

    private static File file;
    private static BinsearchResult searchResult;
    //private static Download download;
    private static String filename;
    private static ArrayList<BinsearchResult> results;
    private boolean shouldAppend = false;
    private JProgressBar progressBar;

    public Binsearch(BinsearchResult result, JProgressBar progressBar) {
        searchResult = result;
        //download = new Download(searchResult.getMedia(), HelloNzbToolkit.getRandomID());
        //Main.addDownload(download);
        results = new ArrayList<BinsearchResult>();
        results.add(result);
        this.progressBar = progressBar;
    }

    public Binsearch(BinsearchResult res, ArrayList<BinsearchResult> results, JProgressBar progressBar) {
        searchResult = res;
        //download = new Download(searchResult.getMedia(), HelloNzbToolkit.getRandomID());
        //Main.addDownload(download);
        Binsearch.results = results;
        results.add(0, res);
        shouldAppend = true;
        this.progressBar = progressBar;
    }

    public String getFilename() {
        return filename;
    }

    /*
    public static void readNZB() {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException x) {
            System.err.println(x);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Main.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    * 
    */

    public void getNZB() {
        filename = Utils.normalizeForFilesystem(searchResult.getMedia().toString());
        file = new File(Main.getDownDir() + filename + ".nzb");
        //System.out.println(file);
        URL url = null;
        BufferedOutputStream foutput = null;
        try {
            // URL of CGI-Bin script.
            url = new URL("http://www.binsearch.info/fcgi/nzb.fcgi?q=" + searchResult.getQuery());
        } catch (MalformedURLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
        HttpURLConnection connection = null;
        for (BinsearchResult res : results) {
            String urlParameters = res.getId() + "=on" + "&action=" + "nzb";
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");

                connection.setRequestProperty("Content-Length", ""
                        + Integer.toString(urlParameters.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setRequestProperty("Accept-Encoding", "gzip, deflate");

                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();

                //Get Response
                byte[] inBuffer = new byte[1024];
                int bytesRead = 0;

                InputStream input1 = connection.getInputStream();
                GZIPInputStream input2 = null;
                if ("gzip".equals(connection.getContentEncoding())) {
                    input2 = new GZIPInputStream(input1);
                }
                if (shouldAppend) {
                    foutput = new BufferedOutputStream(new FileOutputStream(file.getCanonicalPath(), true));
                } else {
                    foutput = new BufferedOutputStream(new FileOutputStream(file.getCanonicalPath()));
                }
                while ((bytesRead = input2.read(inBuffer, 0, inBuffer.length)) != -1) {
                    foutput.write(inBuffer, 0, bytesRead);
                }
                foutput.flush();
            } catch (Exception e) {
                Main.getLogger().log(Level.SEVERE, null, e);
            } finally {
                if (foutput != null) {
                    try {
                        foutput.close();
                    } catch (IOException ex) {
                        Main.getLogger().log(Level.SEVERE, null, ex);
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        cleanNZB();
    }

    private static void cleanNZB() {
        FileInputStream fis = null;
        String strLine = "";
        ArrayList<String> readFile = new ArrayList<String>();
        try {
            fis = new FileInputStream(file.getCanonicalPath());
            InputStreamReader in = new InputStreamReader(fis, Charset.forName("ISO8859_1"));
            BufferedReader br = new BufferedReader(in);
            while ((strLine = br.readLine()) != null) {
                readFile.add(strLine);
            }
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                fis.close();
            } catch (IOException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        
        /*
        File file1 = null;
        try {
            file1 = new File(file.getCanonicalPath());
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
         * 
         */
        file.delete();
        
        FileOutputStream fos = null;
        try {
            
            fos = new FileOutputStream(file.getCanonicalPath());
            OutputStreamWriter out = new OutputStreamWriter(fos);
            BufferedWriter bw1 = new BufferedWriter(out);
            for (int i = 0; i < 4; i++) {
                bw1.write(readFile.get(i));
                bw1.newLine();
            }
            for (int i = 4; i < readFile.size(); i++) {
                if(readFile.get(i).startsWith("<file") || readFile.get(i).startsWith("<groups")
                        || readFile.get(i).startsWith("<segment") || readFile.get(i).startsWith("</segments>")
                        || readFile.get(i).startsWith("</file>")){
                   bw1.write(readFile.get(i)); 
                   bw1.newLine();
                }
            }
            bw1.write("</nzb>");
            bw1.newLine();
            bw1.flush();
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        //file = file1;
    }

    /*
    public static void parseNZB() throws XMLStreamException, IOException, ParseException {
    Main.getDownloadsModel().setDownloadStatus(download.getDownId(), Download.PARSINGNZB);
    NzbParserCreator pc = new NzbParserCreator(Main.nzb, file.getCanonicalPath() + searchResult.getMedia().getExtension());
    Main.downloadExecutor.submit(pc);
    }
     * 
     */
    @Override
    public void run() {
//        Main.getDownloadsModel().setDownloadStatus(download.getDownId(), Download.GETTINGNZB);
        getNZB();
        openNzb();
    }

    private static void openNzb() {
        try {
            String filename1 = file.getCanonicalPath();
            //String id = "2";
            String id = searchResult.getFilename() + searchResult.getPoster();
            Main.printQueue();
            NzbParserCreator pc = new NzbParserCreator(id, Main.nzb, filename1, searchResult.getMedia());
            pc.start();
            //Main.nzb.clearNzbQueueSelection();
            Main.printQueue();
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
    }
}
