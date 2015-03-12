/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.io.Serializable;
import java.net.URL;
import java.util.Observable;

/**
 *
 * @author Luiso
 */
public class Download extends Observable implements Serializable {

    // Max size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024;
    // These are the status names.
    public static final String STATUSES[] = {"Downloading",
        "Paused", "Complete", "Cancelled", "Error", "Queued", "Bot Not Found", "Invalid Port", "Decoding",
        "Getting NZB", "Parsing NZB"};
    // These are the status codes.
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    public static final int QUEUED = 5;
    public static final int NOBOT = 6;
    public static final int BADPORT = 7;
    public static final int DECODING = 8;
    public static final int GETTINGNZB = 9;
    public static final int PARSINGNZB = 10;
    private URL url; // download URL
    private long size; // size of download in bytes
    private long downloaded; // number of bytes downloaded
    private int status; // current status of download
    private Media media;
    private int downId;
    private String bot;
    private float progress;
    private double rate;
    private String eta;
    private MyThroughputChecker checker;

    public MyThroughputChecker getChecker() {
        return checker;
    }

    public void setChecker(MyThroughputChecker checker) {
        this.checker = checker;
    }

    public String getBot() {
        return bot;
    }

    public boolean isDownloading() {
        if (status == QUEUED || status == DOWNLOADING) {
            return true;
        }
        return false;
    }

    public void setBot(String bot) {
        this.bot = bot;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media song) {
        this.media = song;
    }

    public int getDownId() {
        return downId;
    }

    public void setDownId(int downId) {
        this.downId = downId;
    }

    public void setStatus(int i) {
        status = i;
        stateChanged();
    }

    public Download(Media s, int i) {
        size = -1;
        downloaded = 0;
        status = QUEUED;
        media = s;
        downId = i;
        checker = new MyThroughputChecker(this);
    }

    public Download(Media s) {
        size = -1;
        downloaded = 0;
        status = QUEUED;
        media = s;
        downId = 0;
        checker = new MyThroughputChecker(this);
    }

    public String getArtist() {
        return media.getArtist();
    }

    public void setArtist(String artist) {
        media.setArtist(artist);
    }

    public String getTitle() {
        return media.getTitle();
    }

    public void setTitle(String title) {
        media.setTitle(title);
    }

    // Get this download's URL.
    public String getUrl() {
        return url.toString();
    }

    // Get this download's size.
    public long getSize() {
        return size;
    }

    public void setSize(long i) {
        size = i;
    }

    public void setDownloaded(long i) {
        downloaded = i;
        float downloaded1 = downloaded;
        float size1 = size;
        progress = (downloaded1 / size1) * 100;
    }

    // Get this download's progress.
    public float getProgress() {
        return progress;
        //return ((float) downloaded / size) * 100;
    }

    // Get this download's status.
    public int getStatus() {
        return status;
    }

    public void setQueued() {
        status = QUEUED;
        //stateChanged();
    }

    public void queue() {
        status = QUEUED;
        stateChanged();
    }

    // Pause this download.
    public void pause() {
        status = PAUSED;
        stateChanged();
    }

    // Resume this download.
    public void resume() {
        status = DOWNLOADING;
        stateChanged();
    }

    // Cancel this download.
    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }

    // Mark this download as having an error.
    private void error() {
        status = ERROR;
        stateChanged();
    }

    // Notify observers that this download's status has changed.
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }

    public void addBytesDownloaded(long bytes) {
        downloaded += bytes;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public long getDownloaded() {
        return downloaded;
    }

    @Override
    public String toString() {
        return media.toString() + " " + downId;
    }
}
