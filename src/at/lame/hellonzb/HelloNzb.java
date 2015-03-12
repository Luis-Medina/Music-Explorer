/**
 * *****************************************************************************
 * HelloNzb -- The Binary Usenet Tool Copyright (C) 2010-2011 Matthias F.
 * Brandstetter
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************
 */
package at.lame.hellonzb;

import at.lame.hellonzb.listener.actions.*;
import at.lame.hellonzb.nntpclient.*;
import at.lame.hellonzb.nntpclient.nioengine.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.unrar.RarExtractor;
import at.lame.hellonzb.util.*;

import java.awt.TrayIcon.MessageType;
import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import musicchecker.Main;
import musicchecker.Media;
import musicchecker.Utils;

/**
 * This is the main class of the JavaNzb application.
 *
 * @author Matthias F. Brandstetter
 */
public class HelloNzb extends HelloNzbCradle {

    /**
     * Default window title string
     */
    private String winTitle;
    /**
     * vars for one instance check
     */
    private static ServerSocket oneInstanceSocket;
    /**
     * set to true if the user has stopped download before it was finished
     */
    private boolean downloadStopped;
    public static final int SERVER_SOCKET = 23472;
    public static boolean ONLY_ONE = true;

    /**
     * This is the main method of the HelloNzb application.
     *
     * @param args String array of command line arguments
     *
     * public static void main(String [] args) { String nzbFileToLoad =
     * HelloNzbToolkit.parseCmdLineArgs(args);
     *
     * // only one instance of the application is allowed to run simultaneously
     * if(ONLY_ONE) { // try to open the socket try { oneInstanceSocket = new
     * ServerSocket( SERVER_SOCKET, 0, InetAddress.getByAddress(new byte[]
     * {127,0,0,1}));
     *
     * }
     * catch(IOException e) { String newNzbFile =
     * HelloNzbToolkit.parseCmdLineArgs(args); if(newNzbFile != null)
     * HelloNzbToolkit.writeToMappedBuffer(newNzbFile); else
     * System.out.println("Only one instance of HelloNzb allowed!");
     *
     * System.exit(1); } catch(Exception e) { e.printStackTrace();
     * System.exit(1); }
     *
     * // create runtime shutdown hook ShutdownHook shutdownHook = new
     * ShutdownHook(); Runtime.getRuntime().addShutdownHook(shutdownHook); }
     *
     * // init GUI look&feel HelloNzbToolkit.initializeLookAndFeel();
     *
     * // start application JFrame frame = new JFrame(); new HelloNzb(frame,
     * "HelloNzb - The Binary Usenet Tool", nzbFileToLoad); }
     *
     *
     */
    /**
     * This is the constructor of the class.
     *
     * @param frame The JFrame object to use as window
     * @param title The string used as window title
     */
    public HelloNzb(JFrame frame, String title, String nzbFileToLoad) {
        super(frame, title);

        this.jframe = frame;

        // create the background task manager
        taskMgr = new TaskManager(this);
        JProgressBar progBar = taskMgr.getProgressBar();
        taskMgr.start();

        createMemoryMapping();

        // update speed graph panel
        this.speedGraph.setSize(Main.jPanel6.getHeight());
        //this.jframe.validate();
        //this.jframe.repaint();

        // load last session (NZB files that were open)
        loadLastSession();
        setConnSpeedLimit();
        this.downloadStopped = false;
    }

    /**
     * Called at app shutdown to delete lock file
     */
    private static void unlockFile() {
        // unbind server socket
        try {
            oneInstanceSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method starts to download the first file in the queue.
     */
    public void startDownload() {
        String rootDir = Main.prefs.get("downpath").toString();
        String msg, title;


        // check if we are running already
        if (currentFileDownloader != null) {
            // yes, so stop download immediately
            globalDisconnect(true);
            Main.filesToDownloadTabModel.resetAllSegCounts(false);
            Vector<DownloadFile> dlFiles = currentNzbParser.getFiles();
            for (DownloadFile file : dlFiles) {
                file.resetSegments();
            }
            dlFiles = Main.filesToDownloadTabModel.getDownloadFileVector();
            for (DownloadFile file : dlFiles) {
                updateDownloadQueue(file.getFilename(), 0);
            }

            // and reset actions (menus and actions)
            updStatusBar(0);

            downloadStopped = true;

            return;
        }




        try {
            // start NIO client
            if (nioClient == null) {

                nioClient = new NettyNioClient();

                long pref = Long.valueOf(Main.prefs.get("speedlimit").toString());

                System.out.println(pref);

                nioClient.setSpeedLimit(pref);

                // start daemon thread
                Thread t = new Thread(nioClient);
                t.setDaemon(true);
                t.start();
            }
        } catch (UnknownHostException e) {
            msg = Main.localer.getBundleText("PopupUnknownServer");
            title = Main.localer.getBundleText("PopupErrorTitle");
            JOptionPane.showMessageDialog(jframe, msg, title, JOptionPane.ERROR_MESSAGE);
            Main.startToggleButton.setSelected(false);
            return;
        } catch (IOException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
            Main.startToggleButton.setSelected(false);
            return;
        }

        // create download directory if necessary
        String nzbFilename = Main.nzbFileQueueTabModel.getNzbParser(0).getName();

        nzbFilename = HelloNzbToolkit.getLastFilename(nzbFilename);

        //File downloadDir = new File(rootDir + File.separator + nzbFilename);
        File downloadDir;
        NzbParser parser = Main.nzbFileQueueTabModel.getNzbParser(0);
        if (parser.getMedia().isSong()) {
            downloadDir = new File(Main.getDownDir() + File.separator + "Music");
        } else if (parser.getMedia().isAlbum()) {
            downloadDir = new File(Main.getDownDir() + File.separator + "Music" + File.separator + nzbFilename);
        } else if (parser.getMedia().isMovie()) {
            downloadDir = new File(Main.getDownDir() + File.separator + "Movie" + File.separator + Utils.capitalizeFirstLetters(nzbFilename));
        } else if (parser.getMedia().isTV()) {
            downloadDir = new File(Main.getDownDir() + File.separator + "TV" + File.separator + Utils.capitalizeFirstLetters(nzbFilename));
        } else if (parser.getMedia().isGame()) {
            downloadDir = new File(Main.getDownDir() + File.separator + "Games" + File.separator + Utils.capitalizeFirstLetters(parser.getMedia().getTitle()));
        } else {
            downloadDir = new File(Main.getDownDir() + File.separator + "Other" + File.separator + Utils.capitalizeFirstLetters(parser.getMedia().getTitle()));
        }
        parser.setDownloadDir(downloadDir);

        // start file downloader thread
        NntpFileDownloader downloader = new NntpFileDownloader(nioClient,
                new SegmentQueue(Main.filesToDownloadTabModel), downloadDir, this);
        downloader.setPaused(!Main.startToggleButton.isSelected());
        Thread thread = new Thread(downloader);
        thread.setDaemon(true);
        thread.start();

        // enable/disable toolbar and menu action
        //AbstractAction action = actions.get("MenuServerStartDownload");
        //action.setEnabled(true);
        //action = actions.get("MenuServerPauseDownload");
        //action.setEnabled(true);
        Main.startToggleButton.setSelected(true);

        currentFileDownloader = downloader;

        // reset data counter?
        if (downloadStopped) {
            downloadStopped = false;
        } else {
            totalBytesLoaded = currentNzbParser.getDownloadedBytes();
        }
    }

    /**
     * Called from action listener when download should be paused.
     */
    public void pauseDownload() {
        if (currentFileDownloader == null) {
            return;
        }

        // check if we are currently in paused state
        if (!currentFileDownloader.isPaused()) {
            currentFileDownloader.setPaused(true);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    Main.setStatus(Main.localer.getBundleText("StatusBarDownloadPaused"));
                }
            });
        } else {
            currentFileDownloader.setPaused(false);
        }
    }

    /**
     * Returns whether or not the download is paused.
     *
     * @return true/false
     */
    public boolean isDownloadPaused() {
        return !Main.startToggleButton.isSelected();
    }

    /**
     * This method is called by a NntpFileDownloader object when it has finished
     * downloading the file's data.
     */
    public void fileDownloadFinished(String filename) {
        // update total progress bar in the nzb file queue table, also the application window title
        totalBytesLoaded += Main.filesToDownloadTabModel.getDownloadFile(filename).getTotalFileSize();
        currentNzbParser.setDownloadedBytes(totalBytesLoaded);
        int p = (int) (totalBytesLoaded * 100 / currentNzbParser.getOrigTotalSize());
        synchronized (Main.nzbFileQueueTabModel) {
            Main.nzbFileQueueTabModel.setRowProgress(currentNzbParser, p);
        }
    }

    /**
     * This method is called by a NntpFileDownloader object when it has finished
     * decoding the file's data. It the updates the download queues and menu
     * items if necessary.
     */
    public void fileDecodingFinished(String filename) {
        // remove according row from file download queue table
        synchronized (Main.filesToDownloadTabModel) {
            for (int i = 0; i < Main.filesToDownloadTabModel.getRowCount(); i++) {
                DownloadFile file = Main.filesToDownloadTabModel.getDownloadFile(i);

                // check if an error occurred during file download
                // remove line from table if no error occurred
                if (file.getFilename().equals(filename) && !file.downloadError()) {
                    // remove from table
                    Main.filesToDownloadTabModel.removeRow(i);
                    currentNzbParser.removeFileAt(i);
                    break;
                }
            }
        }

        synchronized (Main.nzbFileQueueTabModel) {
            // remove first entry from nzb file list, if last seg. was downloaded
            if (Main.filesToDownloadTabModel.getRowCount() == 0) {
                // par2 check?
                Boolean parCheck = (Boolean) Main.prefs.get("par2check");
                if (parCheck) {
                    par2Check(currentNzbParser);
                } else if (Main.trayIcon != null) {
                    // show tray icon message
                    String msg = Main.localer.getBundleText("SystemTrayMsgDownloadFinished");
                    String name = "\"" + HelloNzbToolkit.getLastFilename(currentNzbParser.getName()) + "\"";
                    msg = msg.replaceAll("_", name);
                    Main.trayIcon.displayMessage(null, msg, MessageType.INFO);
                }

                if (currentNzbParser.getMedia() != null) {
                    Media media = currentNzbParser.getMedia();
                    media.setDownloaded(true);
                    
                    /*
                    if (media.getType() == Media.SONG) {
                        Main.getSongsTableModel().updateRow(media);
                    } else if (media.getType() == Media.MOVIE) {
                        Main.getMoviesTableModel().updateRow(media);
                    } else if (media.getType() == Media.ALBUM) {
                        Main.getAlbumsTableModel().updateRow(media);
                    } else if (media.getType() == Media.GAME) {
                        Main.getGamesTableModel().updateRow(media);
                    }
                    */
                }
                // begin next download or disconnect globally
                Main.nzbFileQueueTabModel.removeRow(currentNzbParser);
                startNextNzbDownload();
            }

            // shutdown computer after all downloads have finished
            if (Main.shutdownCheckbox.isSelected() && Main.nzbFileQueueTabModel.getRowCount() == 0) {
                if (!taskMgr.activeTask()) {
                    shutdownNow();
                }
            }

        }

        saveOpenParserData();
    }

    /**
     * Called from background thread when par2 check is done.
     *
     * @param parser The parser object that is finished being checked
     */
    public void par2CheckDone(NzbParser parser) {
        // update task manager
        taskMgr.par2Done(parser);

        // get RAR extraction setting
        Boolean unrar = (Boolean) Main.prefs.get("unrar");
        if (unrar) {
            rarExtract(parser); // start RAR archive extraction
        } else if (Main.trayIcon != null) {
            // show tray icon message
            String msg = Main.localer.getBundleText("SystemTrayMsgDownloadFinished");
            String name = "\"" + HelloNzbToolkit.getLastFilename(parser.getName()) + "\"";
            msg = msg.replaceAll("_", name);
            Main.trayIcon.displayMessage(null, msg, MessageType.INFO);
        }
    }

    /**
     * Called from background thread when RAR extract is done.
     *
     * @param parser The parser object that is finished being checked
     */
    public void rarExtractDone(NzbParser parser) {
        // update task manager
        taskMgr.rarDone(parser);

        if (Main.trayIcon != null) {
            // show tray icon message
            String msg = Main.localer.getBundleText("SystemTrayMsgDownloadFinished");
            String name = "\"" + HelloNzbToolkit.getLastFilename(parser.getName()) + "\"";
            msg = msg.replaceAll("_", name);
            Main.trayIcon.displayMessage(null, msg, MessageType.INFO);
        }
    }

    /**
     * Check whether or not we should shutdown the computer. Depends on the
     * toggle button and an empty NZB file queue.
     *
     * @return either true or false
     */
    public boolean shouldShutdown() {
        if (!Main.shutdownCheckbox.isSelected() || Main.nzbFileQueueTabModel == null) {
            return false;
        }

        return (Main.shutdownCheckbox.isSelected() && Main.nzbFileQueueTabModel.getRowCount() == 0);
    }

    /**
     * Called when the next nzb file in queue should be downloaded.
     */
    public void startNextNzbDownload() {
        if (Main.filesToDownloadTabModel.getRowCount() > 0) {
            return;
        }

        // is there at least one more nzb file to download?
        currentFileDownloader = null;
        if (Main.nzbFileQueueTabModel.getRowCount() > 0) {
            loadNextNzbFile();
            startDownload();
        } else {
            globalDisconnect(false);
            updStatusBar(0);

            //AbstractAction action = actions.get("MenuServerStartDownload");
            //action.setEnabled(false);
            //action = actions.get("MenuServerPauseDownload");
            //action.setEnabled(false);
            Main.startToggleButton.setSelected(false);

            // call garbage collector
            Runtime.getRuntime().gc();
        }
    }

    /**
     * Remove the specified row from the nzb queue.
     *
     * @param parser The parser object of the row to remove
     */
    public void removeRowFromNzbQueue(NzbParser parser) {
        Main.nzbFileQueueTabModel.removeRow(parser);
        saveOpenParserData();
    }

    /**
     * This method is called when a background file downloader thread has
     * finished file decoding and started writing file data to hard disk.
     */
    public void fileWritingStarted(String filename) {
        synchronized (Main.filesToDownloadTabModel) {
            Main.filesToDownloadTabModel.setRowToWriting(filename);
        }
    }

    /**
     * Update the download file (for progress bar redrawing).
     *
     * @param filename The name of the download file row to update
     * @param value The absolute value of the progress bar to set
     */
    public void updateDownloadQueue(String filename, int value) {
        // update download file progress bar
        synchronized (Main.filesToDownloadTabModel) {
            Main.filesToDownloadTabModel.setValueAt(value, filename, 3);
        }
    }

    /**
     * Decrease the segment counter of this file by one.
     *
     * @param filename The name of the download file row to update
     */
    public void decrSegCount(String filename) {
        synchronized (Main.filesToDownloadTabModel) {
            Main.filesToDownloadTabModel.decrSegCount(filename);
        }
    }

    /**
     * This method sets the progress bar of the row identified by its filename
     * to the "decoding" status, including the maximum value of the progress
     * bar.
     *
     * @param filename The name of the download file row to update
     * @param value The new maximum value of the progress bar
     */
    public void setProgBarToDecoding(String filename, int value) {
        synchronized (Main.filesToDownloadTabModel) {
            Main.filesToDownloadTabModel.setRowToDecoding(filename, value);
        }
    }

    /**
     * Called when a NNTP client thread catches an IO exception.
     */
    public void nntpConnectIoException() {
        // show error popup
        String msg = Main.localer.getBundleText("PopupSocketError");
        String title = Main.localer.getBundleText("PopupErrorTitle");
        JOptionPane.showMessageDialog(jframe, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Called when a NNTP client thread could not find the article to download.
     *
     * @param filename The filename of the file missing the article
     */
    public void articleNotFound(String filename) {
        synchronized (Main.filesToDownloadTabModel) {
            // set the according row to "article not found" state
            Main.filesToDownloadTabModel.setArticleNotFound(filename);
        }
    }

    /**
     * Called when a NNTP client thread receives a malformed server reply.
     *
     * @param filename The filename of the file missing the article
     */
    public void malformedServerReply(String filename) {
        synchronized (Main.filesToDownloadTabModel) {
            // set the according row to "malformed server reply" state
            Main.filesToDownloadTabModel.setMalformedServerReply(filename);
        }
    }

    /**
     * Called when a NNTP client thread encounters a crc32 error during data
     * decoding.
     *
     * @param filename The filename of the file encountering the crc32 error
     */
    public void crc32Error(String filename) {
        synchronized (Main.filesToDownloadTabModel) {
            // set the according row to "crc32 error" state
            Main.filesToDownloadTabModel.setCrc32Error(filename);
        }
    }

    /**
     * Called when a NNTP client thread has not found any suitable decoder..
     *
     * @param filename The filename of the file encountering the error
     */
    public void noDecoderFound(String filename) {
        synchronized (Main.filesToDownloadTabModel) {
            // set the according row to "no decoder found" state
            Main.filesToDownloadTabModel.setNoDecoderFound(filename);
        }
    }

    /**
     * Called when a file that has been downloaded is corrupted.
     */
    public void downloadDataCorrupted() {
        // show error popup
        String msg = Main.localer.getBundleText("PopupDownloadDataCorrupted");
        String title = Main.localer.getBundleText("PopupErrorTitle");
        JOptionPane.showMessageDialog(jframe, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Clear all selections on the NZB file queue table.
     */
    public void clearNzbQueueSelection() {
        Main.nzbListTab.clearSelection();
    }

    /**
     * This method is called when the user wants to move one or more row(s) in
     * the NZB file queue table up or down, via the according context menu.
     *
     * @param selectedRows The row(s) to move
     * @param direction The direction to move the row(s), up or down
     */
    public void moveRowsInNzbQueue(int[] selectedRows, int direction) {
        if (selectedRows.length == 0) {
            return;
        }

        // direction value valid?
        if ((direction != NzbFileListPopupMoveRowAction.DIRECTION_UP)
                && (direction != NzbFileListPopupMoveRowAction.DIRECTION_DOWN)) {
            return;
        }

        // now move row(s) and compare first row before and after operation
        NzbParser pBefore = Main.nzbFileQueueTabModel.getNzbParser(0);
        Main.nzbFileQueueTabModel.moveRows(selectedRows, direction);
        NzbParser pAfter = Main.nzbFileQueueTabModel.getNzbParser(0);
        if (pBefore != pAfter) {
            // row (parser object) in first row has changed
            Main.filesToDownloadTabModel.clearTableData();
            Main.filesToDownloadTabModel.addNzbParserContents(pAfter);
            currentNzbParser = pAfter;
            totalBytesLoaded = currentNzbParser.getDownloadedBytes();
        }
    }

    /**
     * Called when the user has clicked on the "remove row" menu item in context
     * popup menu of the nzb files table.
     *
     * @param row The row number to delete (zero-based)
     * @param delLocalData When true then also delete local data on disk
     */
    public void removeNzbFileQueueRow(int row, boolean delLocalData) {
        if (row < 0 || row > (Main.nzbFileQueueTabModel.getRowCount() - 1)) {
            return;
        }


        String dirname = Main.nzbFileQueueTabModel.getNzbParser(row).getName();
        synchronized (Main.filesToDownloadTabModel) {
            // remove row from nzb file queue table
            Main.nzbFileQueueTabModel.removeRow(row);
            if (row > 0) {
                return;
            } else {
                // also remove according download files from right table
                Main.filesToDownloadTabModel.clearTableData();
            }

            // load next file from queue
            if (Main.filesToDownloadTabModel.getRowCount() == 0) {
                loadNextNzbFile();
            }
        }

        // delete local content
//        if (delLocalData) {
//            String rootDir = Main.prefs.get("downpath").toString();
//            dirname = HelloNzbToolkit.getLastFilename(dirname);
//            File downloadDir = new File(rootDir + File.separator + dirname);
//            if (downloadDir.isDirectory()) {
//                HelloNzbToolkit.deleteNonEmptyDir(downloadDir);
//            }
//        }

        // disable toolbar and menu action
        if (Main.nzbFileQueueTabModel.getRowCount() == 0) {
        } else {
            saveOpenParserData();
        }
    }

    /**
     * Called when the user has clicked on the "remove row" menu item in context
     * popup menu of the files to download table.
     *
     * @param row The row number to delete (zero-based)
     */
    public void removeDownloadFileQueueRow(int row) {
        if (row < 0 || row > (Main.filesToDownloadTabModel.getRowCount() - 1)) {
            return;
        }

        // subtract the amount of bytes of the removed file from 
        // the total nzb file size progress bar on the left side
        totalBytesLoaded += Main.filesToDownloadTabModel.getDownloadFile(row).getTotalFileSize();
        int p = (int) (totalBytesLoaded * 100 / currentNzbParser.getOrigTotalSize());
        Main.nzbFileQueueTabModel.setRowProgress(currentNzbParser, p);

        // then remove the row from the table
        synchronized (Main.filesToDownloadTabModel) {
            // remove item from download file queue
            Main.filesToDownloadTabModel.removeRow(row);
            currentNzbParser.removeFileAt(row);

            // if this was the last item in download queue, then load the next nzb file 
            if (Main.filesToDownloadTabModel.getRowCount() == 0) {
                removeNzbFileQueueRow(0, false);
            }
        }
    }

    /**
     * Returns the error status of a download file in a specific row.
     *
     * @param row The row in the table
     * @return error yes/no
     */
    public boolean errorAtDownloadFile(int row) {
        return Main.filesToDownloadTabModel.getDownloadFile(row).downloadError();
    }

    /**
     * If set via program config, do a par2 check and automatically repair if
     * necessary.
     *
     * @param parser The parser object to check
     */
    private void par2Check(NzbParser parser) {
        // wait until all download files are finished
        if (Main.filesToDownloadTabModel.getRowCount() > 1) {
            return;
        }

        // do par2 check
        taskMgr.par2Check(parser);
        Par2Check par2 = new Par2Check(this, parser);
        Thread t = new Thread(par2);
        t.start();
    }

    /**
     * If set via program config, do a RAR archive extraction after download.
     *
     * @param parser The parser object to use
     */
    private void rarExtract(NzbParser parser) {
        // do rar archive extracr
        taskMgr.rarExtract(parser);
        RarExtractor unrar = new RarExtractor(this, parser);
        Thread t = new Thread(unrar);
        t.start();
    }

    /**
     * Returns the currently set bps value (on status bar).
     *
     * @return Last bps value
     */
    public long lastBpsValue() {
        if (nioClient == null) {
            return 0;
        } else {
            return nioClient.getDlTraffic();
        }

        /*
         * if(currentFileDownloader == null || (pauseToggleButton != null &&
         * pauseToggleButton.isSelected())) { return 0L; } else return
         * lastBpsValue;
         */    }

    public void moveRowsToTopNzbQueue(int selectedRow) {
        if (selectedRow < 0) {
            return;
        }


        // now move row(s) and compare first row before and after operation
        NzbParser pBefore = Main.nzbFileQueueTabModel.getNzbParser(0);
        Main.nzbFileQueueTabModel.moveToTop(selectedRow);
        NzbParser pAfter = Main.nzbFileQueueTabModel.getNzbParser(0);
        if (pBefore != pAfter) {
            // row (parser object) in first row has changed
            Main.filesToDownloadTabModel.clearTableData();
            Main.filesToDownloadTabModel.addNzbParserContents(pAfter);
            currentNzbParser = pAfter;
            totalBytesLoaded = currentNzbParser.getDownloadedBytes();
        }
    }

    public void moveRowsToBottomNzbQueue(int selectedRow) {
        if (selectedRow < 0) {
            return;
        }


        // now move row(s) and compare first row before and after operation
        NzbParser pBefore = Main.nzbFileQueueTabModel.getNzbParser(0);
        Main.nzbFileQueueTabModel.moveToBottom(selectedRow);
        NzbParser pAfter = Main.nzbFileQueueTabModel.getNzbParser(0);
        if (pBefore != pAfter) {
            // row (parser object) in first row has changed
            Main.filesToDownloadTabModel.clearTableData();
            Main.filesToDownloadTabModel.addNzbParserContents(pAfter);
            currentNzbParser = pAfter;
            totalBytesLoaded = currentNzbParser.getDownloadedBytes();
        }
    }
    // used at application shutdown
}
