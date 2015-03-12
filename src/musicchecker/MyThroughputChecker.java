/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Luiso
 */
public class MyThroughputChecker implements Runnable {

    private Download download;
    private long sleepTime = 1000;  //100ms
    private long lastDownloaded = 0;

    public MyThroughputChecker(Download d) {
        download = d;
    }

    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }


    public void run() {
        while (download.isDownloading()) {
            long downloaded = download.getDownloaded();
            long diff = downloaded - lastDownloaded;
            lastDownloaded = downloaded;
            double rate1 = (double) diff / sleepTime;
            download.setRate(rate1);
            long bytesLeft = download.getSize() - downloaded;
            bytesLeft = bytesLeft / 1024;
            long timeLeft = (long) (bytesLeft / rate1);
            String eta;
            String format = String.format("%%0%dd", 2);
            String seconds = String.format(format, timeLeft % 60);
            String minutes = String.format(format, (timeLeft % 3600) / 60);
            String hours = String.format(format, timeLeft / 3600);
            if (hours.equals("00")) {
                eta = minutes + ":" + seconds;
            } else {
                eta = hours + ":" + minutes + ":" + seconds;
            }
            if(rate1 < 0.05){
                eta = "";
            }
            download.setEta(eta);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("I'M DYING!!!!!!");
    }
}
