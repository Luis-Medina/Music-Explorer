/*******************************************************************************
 * HelloNzb -- The Binary Usenet Tool
 * Copyright (C) 2010-2011 Matthias F. Brandstetter
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.lame.hellonzb;

import at.lame.hellonzb.nntpclient.*;
import at.lame.hellonzb.nntpclient.nioengine.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import musicchecker.Main;

//import com.jgoodies.forms.builder.*;
//import com.jgoodies.forms.layout.*;
/**
 * This is the base class of the main application class HelloNzb.
 * It contains private/protected members and methods for this main
 * application class. These are mainly "background" and low-level
 * functionalities.
 * 
 * @author Matthias F. Brandstetter
 * @see at.lame.hellonzb.HelloNzb
 */
public abstract class HelloNzbCradle implements HelloNzbConstants {

    /** The main JFrame object of the application window */
    protected JFrame jframe;
    protected SpeedGraphPanel speedGraph;
    /** NIO client */
    protected NettyNioClient nioClient;
    /** A pointer to the file downloader currently active */
    protected NntpFileDownloader currentFileDownloader;
    /** A pointer to the nzb parser currently active */
    protected NzbParser currentNzbParser;
    /** the total amount of bytes loaded for the current file download */
    protected long totalBytesLoaded;
    /** background worker for misc. background tasks */
    protected BackgroundWorker bWorker;
    /** task manager responsible for controlling the background progress bar */
    protected TaskManager taskMgr;
    /** last known count of active threads */
    protected int lastActThreadCount;
    /** if set to true then we can start another parser content saver */
    protected Boolean contentSaverDone;
    public static final int MEM_MAP_BUFFER_SIZE = 1024;

    /** Class constructor. */
    public HelloNzbCradle(JFrame frame, String title) {


        this.jframe = frame;

        // String localer and JVM default locale
        Main.localer = new StringLocaler();
        Locale.setDefault(Main.localer.getLocale());
        JOptionPane.setDefaultLocale(Main.localer.getLocale());
        JFileChooser.setDefaultLocale(Main.localer.getLocale());


        // dynamically set the widgets while window size is changed
        Toolkit.getDefaultToolkit().setDynamicLayout(true);

        // some default values
        this.currentFileDownloader = null;
        this.currentNzbParser = null;
        this.lastActThreadCount = 0;
        this.totalBytesLoaded = 0;
        this.contentSaverDone = true;

        speedGraph = new SpeedGraphPanel(Main.jPanel6.getWidth() - 2);
        Main.jPanel6.add(speedGraph);

    }

    /**
     * Create a share memory mapping so that a second instance of this
     * program can later on pass new nzb files to this first instance here.
     * This method also starts a background thread that "listens" to this
     * shared memory for new data (nzb file locations) to read.
     */
    protected void createMemoryMapping() {
        try {
            bWorker = new BackgroundWorker(this, MEM_MAP_BUFFER_SIZE);
            bWorker.start();
        } catch (IOException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
    }

    /**
     * Set the status bar to a "Running threads: <count>" message.
     * 
     * @param threads The count of running download threads
     */
    public void updStatusBar(int threads) {
        /*
        if (!Main.startToggleButton.isSelected()) {
            return;
        }
        if (!Main.startToggleButton.isSelected() && threads >= lastActThreadCount) {
            return;
        }

*/
        String text = Main.localer.getBundleText("StatusBarRunningThreads") + " " + threads;
        String tc = Main.prefs.get("maxdownloads").toString();

        final String statusText = text + "/" + tc;
        lastActThreadCount = threads;

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Main.setStatus(statusText);
            }
        });
    }

    /**
     * This method is called if either
     *   a) the last file in download queue has been downloaded, or
     *   b) the user has manually removed the first entry in the nzb file queue.
     *   
     * It reads the next item in the nzb file queue, retrieves all items from
     * it and updates the download file queue.
     */
    protected void loadNextNzbFile() {
        if (Main.nzbFileQueueTabModel.getRowCount() > 0) {
            // start to download first file of the next nzb file in queue
            NzbParser parser = Main.nzbFileQueueTabModel.getNzbParser(0);
            if (parser != null) {
                Main.filesToDownloadTabModel.addNzbParserContents(parser);
                currentNzbParser = parser;
            }
        }
    }

    /**
     * This method adds the content of a nzb file (i.e. a NzbParser object)
     * to the download queue. It first checks if another nzb file with that
     * name already exists in the nzb queue.
     * 
     * @param parser The NzbParser object to add
     */
    public void addNzbToQueue(NzbParser parser) {
        // check if an nzb file with this name already exists in queue
        /*
        synchronized (Main.nzbFileQueueTabModel) {
            if (Main.nzbFileQueueTabModel.containsNzb(parser)) {
                String title = Main.localer.getBundleText("PopupErrorTitle");
                String msg = Main.localer.getBundleText("PopupNzbFilenameAlreadyInQueue");
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        /*
        if (parser.getMedia().isSong()) {
        downloadDir = new File(Main.getDownDir() + File.separator + "Music");
        } else if (parser.getMedia().isAlbum()) {
        downloadDir = new File(Main.getDownDir() + File.separator + "Music" + File.separator + nzbFilename);
        } else if (parser.getMedia().isVideo()) {
        downloadDir = new File(Main.getDownDir() + File.separator + "Video" + File.separator + nzbFilename);
        } else if (parser.getMedia().isGame()) {
        downloadDir = new File(Main.getDownDir() + File.separator + "Games" + File.separator + parser.getMedia().getTitle());
        } else {
        downloadDir = new File(Main.getDownDir() + File.separator + "Other" + File.separator + parser.getMedia().getTitle());
        }
         * 
         */

        // no, so add the new nzb file to the queue
        Main.nzbFileQueueTabModel.addRow(parser);

        if (Main.filesToDownloadTabModel.getRowCount() == 0) {
            Main.filesToDownloadTabModel.addNzbParserContents(parser);
            currentNzbParser = parser;
            totalBytesLoaded = currentNzbParser.getDownloadedBytes();
        }     
        Main.getProgressBar().setVisible(false);
        Main.setDefaultCursor();
        Main.getLogger().log(Level.INFO, "Added content of {0}.nzb to queue", parser.getName());
        //saveOpenParserData();
    }

    /**
     * Load the last session (NZB files that were open).
     */
    protected void loadLastSession() {
        // does data directory exists?
        String datadirPath = Main.getAppDir() + "Data";
        File datadir = new File(datadirPath);

        if (!datadir.isDirectory()) {
            Main.getLogger().log(Level.SEVERE, "Could not load last session, specified folder is no directory");
            return;
        }

        // set background status bar
        taskMgr.loadSession(true);

        // directory exists, so load all nzb files from it
        File[] files = datadir.listFiles();
        NzbParser parser;
        for (File file : files) {
            try {
                // directory
                if (file.isDirectory()) {
                    continue;
                }
                String filename = file.getCanonicalPath();
                if (filename.substring(filename.length() - 4, filename.length()).equals(".nzb")) {
                    System.out.println(file);
                    parser = new NzbParser(this, file.getAbsolutePath());
                    addNzbToQueue(parser);
                }
                // nzb file?
                /*
                if (!filename.substring(filename.length() - 4, filename.length()).equals(".nzb")) {
                if (file.canWrite()) {
                Main.getLogger().log(Level.INFO, "Deleting non-NZB file {0}", file.getName());
                file.delete();
                }
                continue;
                }
                 * 
                 */

                /*
                // yes, so remove index part of the filename (e.g. "1-filename.nzb")
                while (!filename.startsWith("-")) {
                filename = filename.substring(1, filename.length());
                }
                filename = filename.substring(1, filename.length());
                
                // tmp. rename original source file
                tmpFile = new File(datadirPath + System.getProperty("file.separator") + filename);
                file.renameTo(tmpFile);
                 * 
                 */

            } catch (Exception e) {
                Main.getLogger().log(Level.SEVERE, null, e);
            }
        }
        for (File file : datadir.listFiles()) {
            file.delete();
        }

        // unset background status bar       
        taskMgr.loadSession(false);
    }

    /**
     * Called to save the currently open and unused parser data.
     */
    public void saveOpenParserData() {
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                Main.setStatus("Saving Pending NZBs...");
            }
            
        });
        
        Main.getLogger().log(Level.INFO, "Saving parser data");
        synchronized (contentSaverDone) {
            if (contentSaverDone) {
                contentSaverDone = false;
                ParserContentSaver pcs = new ParserContentSaver(this,
                        Main.nzbFileQueueTabModel.copyQueue(), Main.filesToDownloadTabModel.getDownloadFileVector());
                pcs.start();
            }
        }
        Main.getLogger().log(Level.INFO, "Parser data saved.");
        SwingUtilities.invokeLater(new Runnable(){

            @Override
            public void run() {
                Main.setStatus("Save NZBs done...");
            }
            
        });
    }

    /**
     * Sets the contentSaverDone flag to true;
     */
    public void contentSaverDone() {
        synchronized (contentSaverDone) {
            contentSaverDone = true;
        }
    }

    /**
     * Return the current contentSaverDone value (either true or false).
     */
    public boolean isContentSaverDone() {
        boolean ret;

        synchronized (contentSaverDone) {
            ret = contentSaverDone;
        }

        return ret;
    }

    /**
     * Load the specified nzb file (passed via command line at application call).
     * 
     * @param filename The file to load, quit if null
     */
    protected void loadFileFromCmdLine(String filename) {
        // filename passed at command line?
        if (filename == null) {
            return;
        }

        // set background status bar
        taskMgr.loadNzb(true);

        try {
            File file = new File(filename);
            addNzbToQueue(new NzbParser(this, filename));
            //file.delete();
        } catch (IOException e) {
            System.err.println("Error while reading file '" + filename + "'!");
            System.exit(8);
        } catch (XMLStreamException e) {
            System.err.println("Given file '" + filename + "' is not a valid NZB file!");
            System.exit(8);
        } catch (java.text.ParseException e) {
            System.err.println(e.getMessage());
            System.exit(8);
        }

        // unset background status bar
        taskMgr.loadNzb(false);
    }

    /**
     * Shutdown the computer now (after all downloads have been finished).
     */
    public void shutdownNow() {
        String shutdownCmd = "";
        String shutdownDir = "";

        if (System.getProperty("os.name").contains("Windows")) {
            shutdownCmd = "shutdown.exe /s /f";
            shutdownDir = System.getenv("SystemRoot") + "/system32";
        } else if (System.getProperty("os.name").contains("Linux")) {
            shutdownCmd = "shutdown -h -q";
            shutdownDir = "/sbin";
        }

        File dir = new File(shutdownDir);

        // execute shutdown command
        try {
            taskMgr.shutdown();
            bWorker.shutdown();
            Runtime rt = Runtime.getRuntime();
            rt.exec(shutdownCmd, null, dir);
        } catch (IOException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }

        System.exit(0);
    }

    /**
     * Set the connection speed limit label on the status bar 
     * according to value in preferences.
     */
    public void setConnSpeedLimit() {
        String pref = Main.prefs.get("speedlimit").toString();
        try {
            @SuppressWarnings("unused")
            long tmp = Long.parseLong(pref);
        } catch (Exception ex) {
            pref = "0";
            Main.prefs.put("speedlimit", 0);
        }

    }

    /**
     * Use this method to set the ETA and total file size label.
     * Also set the tooltip of the system tray icon accordingly.
     * 
     * @param text The text to set on the label
     */
    public void setEtaAndTotalLabel(final String text) {
        // set label in status bar
        if (Main.etaAndTotalText != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Main.etaAndTotalText.setText(text);
                }
            });
        }

        // set system tray icon
        if (Main.trayIcon != null) {
            if (text == null || text.isEmpty()) {
                Main.trayIcon.setToolTip("Media Checker 2.0 -- " + Main.localer.getBundleText("SystemTrayTooltipNoFiles"));
            } else {
                Main.trayIcon.setToolTip("Music Checker 2.0 -- " + text);
            }
        }
    }

    /**
     * Use this method to set the current download speed label.
     * 
     * @param bps The text to set on the label
     */
    public void setCurrDlSpeedLabel(final long bps) {
        if (Main.currDlSpeed != null) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Main.currDlSpeed.setText(HelloNzbToolkit.prettyPrintBps(bps));
                }
            });
        }
    }

    /**
     * Check whether or not the system tray icon is set for the running program.
     * 
     * @return true if we have set the tray icon, false otherwise
     */
    public boolean hasTrayIcon() {
        return (Main.trayIcon != null) ? true : false;
    }

    /**
     * Returns true if there is currently a download active.
     * 
     * @return true/false
     */
    public boolean isDownloadActive() {
        return (currentFileDownloader == null ? false : true);
    }

    /**
     * Get the JFrame object, i.e. the applications main window.
     * 
     * @return The JFrame object
     */
    public JFrame getJFrame() {
        return jframe;
    }

    /**
     * Return the count of files in the download queue.
     * 
     * @return The count of files
     */
    public int getDownloadFileCount() {
        return Main.filesToDownloadTabModel.getRowCount();
    }

    /**
     * Globally disconnect from server, if currently connected.
     * 
     * @param block Whether or not to wait until connection has closed
     */
    public void globalDisconnect(boolean block) {
        if (currentFileDownloader != null) {
            currentFileDownloader.shutdown();
            currentFileDownloader = null;
        }

        if (nioClient != null) {
            nioClient.shutdown(true, 1);
            nioClient = null;
        }

    }

    /**
     * Returns the task manager object.
     * 
     * @return The task manager object
     */
    public TaskManager getTaskManager() {
        return taskMgr;
    }

    /**
     * Returns the current NettyNioClient object (or null)
     * 
     * @return The current NettyNioClient object (or null)
     */
    public NettyNioClient getCurrNioClient() {
        return nioClient;
    }

    /**
     * Returns the total amount of bytes loaded (counted for the currently
     * downloaded NZB file and all its segments).
     * 
     * @return The total amount of loaded bytes
     */
    public long getTotalBytesLoaded() {
        return totalBytesLoaded;
    }

    /**
     * Returns all NzbParser objects currently in the NZB file queue.
     * 
     * @return All NzbParser objects in a vector
     */
    public Vector<NzbParser> getNzbQueue() {
        Vector<NzbParser> vector = new Vector<NzbParser>();

        // get all NzbParser objects from table model
        try {
            int size = Main.nzbFileQueueTabModel.getRowCount();
            for (int i = 0; i < size; i++) {
                vector.add(Main.nzbFileQueueTabModel.getNzbParser(i));
            }
        } catch (Exception e) {
        }

        return vector;
    }

    /**
     * Returns the speed history graph panel object.
     * @return The SpeedGraphPanel object
     */
    public SpeedGraphPanel getSpeedGraphPanel() {
        return speedGraph;
    }
}
