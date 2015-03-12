/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Main.java
 *
 * Created on Dec 28, 2009, 7:39:14 PM
 */
package musicchecker;

import javax.swing.border.Border;
import javax.xml.stream.XMLStreamException;
import musicchecker.tableModels.*;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.logging.Logger;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import at.lame.hellonzb.HelloNzb;
import at.lame.hellonzb.listener.DownloadFileListPopupListener;
import at.lame.hellonzb.listener.NzbFileListPopupListener;
import at.lame.hellonzb.parser.NzbParser;
import at.lame.hellonzb.parser.NzbParserCreator;
import at.lame.hellonzb.renderer.*;
import at.lame.hellonzb.tablemodels.FilesToDownloadTableModel;
import at.lame.hellonzb.tablemodels.NzbFileQueueTableModel;
import at.lame.hellonzb.util.StringLocaler;
import java.awt.datatransfer.*;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import javax.swing.filechooser.FileNameExtensionFilter;
import musicchecker.pageInterpreters.*;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author Luiso
 */
public class Main extends javax.swing.JFrame implements ClipboardOwner {

    public static boolean failed = false;
    //private static ArrayList<SearchResult> results;
    private static DownloadsTableModel downloadsTableModel;
    private static DownloadedTableModel downloadedTableModel;
    private static SongsTableModel songsTableModel;
    private static MoviesTableModel moviesTableModel;
    private static AlbumsTableModel albumsTableModel;
    private static GamesTableModel gamesTableModel;
    private static AbstractTableModel resultsTableModel;
    //private final static String appDir = System.getProperty("user.dir") + "\\";
    public static final String hot100BillboardURL = "http://www.billboard.com/charts/hot-100?begin=1&order=position&decorator=service&confirm=true";
    public static final String latinBillboardURL = "http://www.billboard.com/charts/latin-songs?begin=1&order=position&decorator=service&confirm=true";
    public static final String rbHipHopBillboardURL = "http://www.billboard.com/charts/r-b-hip-hop-songs?begin=1&order=position&decorator=service&confirm=true";
    public static final String countryBillboardURL = "http://www.billboard.com/charts/country-songs?begin=1&order=position&decorator=service&confirm=true";
    public static final String rockBillboardURL = "http://www.billboard.com/charts/rock-songs?begin=1&order=position&decorator=service&confirm=true";
    public static final String popBillboardURL = "http://www.billboard.com/charts/pop-songs?begin=1&order=position&decorator=service&confirm=true";
    public static final String danceBillboardURL = "http://www.billboard.com/charts/dance-club-play-songs?begin=1&order=position&decorator=service&confirm=true";
    public static final String xweaselURL = "http://www.xweasel.org/Search.php?Description=";
    public static final String videoetaURL = "http://videoeta.com/dvdbd/index.html?monthly=&interval=0&sort=1&view=1&genre=featured";
    // Currently selected download.
    private static Download selectedDownload;
    private static JTable selectedTable;
    public static String timeout = "120";
    public static boolean deleteNZB = true;
    public static String speedLimit = "0";
    public static String maxThreads = "19";
    public static String checkSegments = "false";
    public static String par2Check = "false";
    //public static String par2Dir = System.getProperty("user.dir") + "\\" + "External\\";
    public static HelloNzb nzb;
    public static int currentDownloads = 0;
    public static int downloadMode = 0;
    private static int port = 6667;
    private static ArrayList<IRCConnection> connections;
    private static Search currentSearch;
    private static Connection dbConnection;
    private static PiecesModel model;
    private static boolean closedOnce;
    private JPopupMenu popupMenu;
    //private static CountDownLatch latch = new CountDownLatch(1);
    //private static CountDownLatch latch2 = new CountDownLatch(1);
    public static boolean usingLatch = false;
    public static boolean currentDownloadSuccessful = false;
    private static PartsRenderer partRenderer;
    private Border defaultMoviesBorder;
    private Border defaultAlbumsBorder;
    private Border defaultSongsBorder;
    private static Main main;
    public static TrayIcon trayIcon;
    public static boolean stopSearch = false;
    public static ExecutorService downloadExecutor;
    private static SizeComparator sizeComparator = new SizeComparator();
    public static HashMap<String, Object> prefs = new HashMap<String, Object>();
    //private static ArrayList<String> connectedChannels = new ArrayList<String>();
    private static SegmentModel segModel;
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    //These are HelloNzb fields
    public static NzbFileQueueTableModel nzbFileQueueTabModel;
    public static FilesToDownloadTableModel filesToDownloadTabModel;
    public static StringLocaler localer;
    private static PartsWindow partsWindow;
    private static ServerSocket oneInstanceSocket;
    public static final int SERVER_SOCKET = 23473;
    private Thread searchThread;
    private JPopupMenu editPopupMenu;
    private BinsearchParse parseTask;
    private static String errors = "";
    private static boolean shouldStop;
    private static CountDownLatch startSignal = new CountDownLatch(1);
    private static boolean autoDownloadRunning = false;

    /**
     * Creates new form Main
     */
    public Main() {
        BasicConfigurator.configure();
        FileHandler fileTxt;
        SimpleFormatter formatterTxt;
        try {
            fileTxt = new FileHandler(getAppDir() + "\\Log.txt", true);
            // Create txt Formatter
            formatterTxt = new SimpleFormatter();
            fileTxt.setFormatter(formatterTxt);
            logger.addHandler(fileTxt);
            logger.setLevel(Level.ALL);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");

        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");

        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");


        localer = new StringLocaler();
        Locale.setDefault(localer.getLocale());
        setLookAndFeel();
        initComponents();
        errorsLabel.setVisible(false);
        createEditMenu();
        jProgressBar1.setVisible(false);
        setLocationRelativeTo(null);
        createEmptyModels();
        initTray();
        initTypeCombo();
        initPreferences();
        setProgramIcon();
        connectToDatabase();
        if (dbConnection != null) {
            JDBC.getProgramOptions();
            loadData();
            if (Utils.calculateWeekPassed()) {
                trayIcon.displayMessage("Week Passed", "New media has not been checked in a week. Right click on the"
                        + " program icon and select \"Update Media\" to update now.", TrayIcon.MessageType.WARNING);
            }
        }
        initTables();
        nzb = new HelloNzb(this, "Music Checker 2.0", "");
        partRenderer = new PartsRenderer(false);
        minSizeField.setText(Integer.toString(Media.minSongSize));
        maxSizeField.setText(Integer.toString(Media.maxSongSize));

        if ((Boolean) prefs.get("autogroup")) {
            autoGroup.setSelected(true);
        } else {
            autoGroup.setSelected(true);
        }

        ShutdownHook shutdownHook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        //TocaDeToParser parser = new TocaDeToParser(this);
        //parser.run();
        //parser = null;
        //IdentdServer ads = new IdentdServer();
        //ads.execute();

        //AutomaticWorker aw = new AutomaticWorker();
        //aw.execute();  
    }

    public static boolean shouldStop() {
        return shouldStop;
    }

    public static Logger getLogger() {
        return logger;
    }

    private static void startProgressBar() {
        jProgressBar1.setVisible(true);
    }

    private static void stopProgressBar() {
        jProgressBar1.setVisible(false);
    }

    public static void setLookAndFeel() {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public static void addError(String error) {
        errors += error;
        errorsLabel.setVisible(true);
    }

    private void initPreferences() {
        prefs.put("ircuser", "");
        prefs.put("ircnick", "");
        prefs.put("ircemail", "");
        prefs.put("ircalt", "");
        prefs.put("ircpass", "");
        prefs.put("usenethost", "");
        prefs.put("usenetuser", "");
        prefs.put("usenetpass", "");
        prefs.put("usenetport", "119");
        prefs.put("downpath", getAppDir());
        prefs.put("maxdownloads", 20);
        prefs.put("shutdownafterfinish", false);
        prefs.put("closeafterfinish", false);
        prefs.put("downloadmode", 0);
        prefs.put("autocheck", true);
        prefs.put("closetotray", true);
        prefs.put("deletenzb", true);
        prefs.put("hot100", true);
        prefs.put("latin", true);
        prefs.put("hiphop", false);
        prefs.put("country", false);
        prefs.put("rock", false);
        prefs.put("pop", false);
        prefs.put("danceclub", false);
        prefs.put("lamega", false);
        prefs.put("tocadeto", false);
        prefs.put("kq105", false);
        prefs.put("reggaeton94", false);
        prefs.put("fidelity", false);
        prefs.put("estereotempo", false);
        prefs.put("par2check", true);
        prefs.put("unrar", true);
        prefs.put("speedlimit", 0);
        prefs.put("maxconnectionspeed", 0);
        prefs.put("autogroup", true);
        prefs.put("usenetssl", false);
        prefs.put("ignoreerrors", true);
    }

    private void setProgramIcon() {
        URL url = ClassLoader.getSystemResource("resources/icons/Music.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        setIconImage(img);
        url = null;
        kit = null;
        img = null;
    }

    private static void initTypeCombo() {
        for (String s : Media.getTypes()) {
            typeCombo.addItem(s);
        }
        ItemListener comboListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //System.out.println(e.getItemSelectable().getSelectedObjects()[0]);
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (e.getItem().equals("MOVIE")) {
                        artistField.setEnabled(false);
                        minSizeField.setText(Integer.toString(Media.minMovieSize));
                        maxSizeField.setText(Integer.toString(Media.maxMovieSize));
                    } else if (e.getItem().equals("OTHER")) {
                        artistField.setEnabled(false);
                        minSizeField.setText(Integer.toString(Media.minOtherSize));
                        maxSizeField.setText(Integer.toString(Media.maxOtherSize));
                    } else if (e.getItem().equals("GAME")) {
                        artistField.setEnabled(false);
                        minSizeField.setText(Integer.toString(Media.minGameSize));
                        maxSizeField.setText(Integer.toString(Media.maxGameSize));
                    } else if (e.getItem().equals("ALBUM")) {
                        artistField.setEnabled(true);
                        minSizeField.setText(Integer.toString(Media.minAlbumSize));
                        maxSizeField.setText(Integer.toString(Media.maxAlbumSize));
                    } else if (e.getItem().equals("SONG")) {
                        artistField.setEnabled(true);
                        minSizeField.setText(Integer.toString(Media.minSongSize));
                        maxSizeField.setText(Integer.toString(Media.maxSongSize));
                    } else if (e.getItem().equals("TV SHOW")) {
                        artistField.setEnabled(false);
                        minSizeField.setText(Integer.toString(Media.minTVSize));
                        maxSizeField.setText(Integer.toString(Media.maxTVSize));
                    }
                }
            }
        };
        typeCombo.addItemListener(comboListener);
    }

    public static String getAppDir() {
        return System.getProperty("user.dir") + "\\";
    }

    private static void createEmptyModels() {
        songsTableModel = new SongsTableModel();
        moviesTableModel = new MoviesTableModel();
        gamesTableModel = new GamesTableModel();
        albumsTableModel = new AlbumsTableModel();
        downloadsTableModel = new DownloadsTableModel();
        downloadedTableModel = new DownloadedTableModel();
        resultsTableModel = new BinsearchTableModel();
    }

    /*
     * private static void getOptionsFromDB() { ArrayList<String> list = JDBC.getAllOptions(); if
     * (!list.isEmpty()) { IRCuser = list.get(0); IRCnick = list.get(1); IRCemail = list.get(2);
     * IRCalternate = list.get(3); IRCpass = list.get(4); prefs.put("downpath", list.get(5));
     * UsenetHost = list.get(6); UsenetUser = list.get(7); UsenetPass = list.get(8); } else {
     * IRCuser = ""; IRCnick = ""; IRCemail = ""; IRCalternate = ""; IRCpass = "";
     * prefs.put("downpath", appDir); UsenetHost = ""; UsenetUser = ""; UsenetPass = ""; } }
     *
     */
    /*
     * public static CountDownLatch getLatch() { return latch; }
     *
     */

    /*
     * public static CountDownLatch getLatch2() { return latch2; }
     *
     */
    public static void updateDownloadsTable(int id, long bytes) {
        synchronized (downloadsTableModel) {
            downloadsTableModel.setDownloadedSoFar(id, bytes);
        }
    }

    /*
     * private class AutomaticWorker extends SwingWorker<Void, Void> {
     *
     * @Override protected Void doInBackground() throws Exception { usingLatch = true;
     * ListIterator<Media> medias = songsTableModel.getMedia().listIterator(); while
     * (medias.hasNext()) { Media media = medias.next(); if (!media.isDownloaded()) {
     * resolveSearchURL(media); currentSearch = new Search(media); XWeaselParse parseTask = new
     * XWeaselParse(toSend); parseTask.execute(); latch2.await(); latch2 = new CountDownLatch(1); if
     * (!currentSearch.getResults().isEmpty()) { //ArrayList<XWeaselResult> sortedResults =
     * ResultSelection.sortBestSongResults(currentSearch.getResults(), currentSearch);
     * ArrayList<XWeaselResult> sortedResults = new ArrayList<XWeaselResult>(); for (XWeaselResult
     * toDown : sortedResults) { IRCConnection connection =
     * getServerConnection(toDown.getNetwork()); if (connection == null) { IRCConnection ircconn =
     * new IRCConnection(toDown, currentSearch.getMedia()); Thread thread = new Thread(ircconn);
     * thread.start(); } else { if (!connectedToChannel(toDown.getChannel(), toDown.getNetwork())) {
     * connection.joinChannel(toDown.getChannel()); } connection.requestFile(toDown.getBot(),
     * toDown.getPack()); } latch.await(); latch = new CountDownLatch(1); if
     * (currentDownloadSuccessful) { currentDownloadSuccessful = false; break; } } latch = new
     * CountDownLatch(1); } } } return null; } }
     *
     */
    class ShutdownHook extends Thread {

        @Override
        public void run() {
            if (!closedOnce) {
                System.out.println("Shutting down!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                logger.log(Level.WARNING, "System sent the shutdown signal.\n Closing app...");
                saveData();
                //nzb.saveOpenParserData();
                //doClose();
                closedOnce = true;
                Runtime.getRuntime().halt(1);
            }
        }
    }

    private static void unlockFile() {
        // unbind server socket
        try {
            oneInstanceSocket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    private static void doClose() {
        boolean doClose = true;
        synchronized (downloadsTableModel) {
            if (nzb.isDownloadActive()) {
                int option = JOptionPane.showConfirmDialog(null, "There are files downloading."
                        + "\nDo you wish to close the program?");
                if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.NO_OPTION) {
                    doClose = false;
                }
            }
        }
        if (doClose && !closedOnce) {
            logger.log(Level.WARNING, "Entered doClose routine: " + closedOnce);
            saveData();
            nzb.saveOpenParserData();
            closeAllConnections();
            //unlockFile();
            closedOnce = true;
            try {
                System.exit(0);
            } catch (Exception e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
    }

    private void initTray() {

        if (SystemTray.isSupported()) {

            ActionListener exitListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    doClose();
                }
            };

            ActionListener showListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (e.getActionCommand().equals("Show")) {
                        setVisible(true);
                        main.setState(JFrame.NORMAL);
                        main.toFront();
                    } else if (e.getActionCommand().equals("Hide")) {
                        setVisible(false);
                        dispose();
                    }
                }
            };

            ActionListener updateListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(true);
                    main.setState(JFrame.NORMAL);
                    main.toFront();
                    createUpdateParsers(true);
                    JDBC.setLastWeekChecked(Calendar.getInstance().getTime());
                }
            };

            final PopupMenu popup = new PopupMenu();

            MenuItem updateItem = new MenuItem("Update Media");
            updateItem.addActionListener(updateListener);
            popup.add(updateItem);

            final MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(showListener);
            popup.add(showItem);


            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(exitListener);
            popup.add(exitItem);

            SystemTray tray = SystemTray.getSystemTray();

            URL url = ClassLoader.getSystemResource("resources/icons/Music.png");
            Toolkit kit = Toolkit.getDefaultToolkit();
            Image image = kit.createImage(url);

            //Image image = Toolkit.getDefaultToolkit().getImage("music1.gif");


            MouseListener mouseListener = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        if (!main.isVisible() || main.getExtendedState() == JFrame.ICONIFIED) {
                            showItem.setLabel("Show");
                        } else {
                            showItem.setLabel("Hide");
                        }
                    }
                    if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                        if (!main.isVisible() || main.getExtendedState() == JFrame.ICONIFIED) {
                            setVisible(true);
                            Main.main.setState(JFrame.NORMAL);
                            Main.main.toFront();
                        } else {
                            setVisible(false);
                            dispose();
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }
            };

            trayIcon = new TrayIcon(image, "Music Checker", popup);

            /*
             * ActionListener actionListener = new ActionListener() {
             *
             * public void actionPerformed(ActionEvent e) { setVisible(true); } };
             *
             */

            trayIcon.setImageAutoSize(true);
            //trayIcon.addActionListener(actionListener);
            trayIcon.addMouseListener(mouseListener);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                getLogger().log(Level.SEVERE, null, e);
                trayIcon = null;
            }
            image = null;
        } else {
            //  System Tray is not supported
        }
    }

    private static void connectToDatabase() {
        String fileSep = System.getProperty("file.separator");
        String dbLocation = getAppDir() + fileSep + "DB";
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
        dbConnection = null;
        String dbURI = "jdbc:derby:" + dbLocation + fileSep + "MusicChecker";
        Properties props = new Properties();
        props.put("user", "app");
        //props.put("password", "615489");

        props.put("create", "true");

        try {
            dbConnection = DriverManager.getConnection(dbURI, props);
            ResultSet res = dbConnection.getMetaData().getTables(null, null, null, new String[]{"TABLE"});
            if (!res.next()) {
                if (!JDBC.createDatabase()) {
                    JOptionPane.showMessageDialog(null, "Unable to create database!");
                    System.exit(1);
                }
            }
            res.close();
            //createTable();
            //insertDummy();
        } catch (SQLException sqle) {
            getLogger().log(Level.SEVERE, null, sqle);
            JOptionPane.showMessageDialog(null, "Unable to connect to database at " + dbURI);
            //sqle.printStackTrace();
            dbConnection = null;
        }
        fileSep = null;
        dbLocation = null;
        props = null;
        dbURI = null;

    }

    public static Connection getDBConnection() {
        return dbConnection;
    }

    public static SongsTableModel getSongsTableModel() {
        return songsTableModel;
    }

    public static AlbumsTableModel getAlbumsTableModel() {
        return albumsTableModel;
    }

    public static GamesTableModel getGamesTableModel() {
        return gamesTableModel;
    }

    public static MoviesTableModel getMoviesTableModel() {
        return moviesTableModel;
    }

    public static DownloadedTableModel getDownloadedTableModel() {
        return downloadedTableModel;
    }

    public static void setMoviesTableModel(ArrayList<Media> list) {
        if (moviesTableModel == null) {
            moviesTableModel = new MoviesTableModel(list);
        } else {
            moviesTableModel.addMediaList(list);
        }
        moviesTable.setModel(moviesTableModel);
        moviesTableModel.fireTableDataChanged();
    }

    /*
     * public static void setGamesTableModel(ArrayList<Media> list) { if (gamesTableModel == null) {
     * gamesTableModel = new GamesTableModel(list); } else { gamesTableModel.addMediaList(list); }
     * gamesTable.setModel(gamesTableModel); gamesTableModel.fireTableDataChanged(); }
     *
     */
    public static void setSongsTableModel(ArrayList<Media> list) {
        if (songsTableModel == null) {
            songsTableModel = new SongsTableModel(list);
        } else {
            songsTableModel.addMediaList(list);
        }
        songsTable.setModel(songsTableModel);
        songsTableModel.fireTableDataChanged();
    }

    public static void setAlbumsTableModel(ArrayList<Media> list) {
        if (albumsTableModel == null) {
            albumsTableModel = new AlbumsTableModel(list);
        } else {
            albumsTableModel.addMediaList(list);
        }
        albumsTable.setModel(albumsTableModel);
        songsTableModel.fireTableDataChanged();
    }

    private void initTables() {
        //downloadedTable.setModel(downloadedTableModel);
        moviesTable.setModel(moviesTableModel);
        try {
            jbInit();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
        // Set up ProgressBar as renderer for progress column.
        MyProgressRenderer renderer = new MyProgressRenderer(0, 100);
        renderer.setStringPainted(true); // show progress text

        songsTable.setModel(songsTableModel);
        //songsTable.getColumnModel().getColumn(3).setCellRenderer(songsTable.getDefaultRenderer(Boolean.class));       
        albumsTable.setModel(albumsTableModel);

        /*
         * gamesTable.setModel(gamesTableModel);
         * gamesTable.getColumnModel().getColumn(1).setMaxWidth(50);
         * gamesTable.getColumnModel().getColumn(2).setMaxWidth(55);
         *
         */

        //Set table column widths
        songsTable.getColumnModel().getColumn(0).setPreferredWidth(145);
        songsTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        songsTable.getColumnModel().getColumn(2).setMaxWidth(40);
        songsTable.getColumnModel().getColumn(3).setMaxWidth(75);
        songsTable.getColumnModel().getColumn(4).setMaxWidth(70);
        songsTable.getColumnModel().getColumn(5).setMaxWidth(70);

        albumsTable.getColumnModel().getColumn(2).setMaxWidth(85);
        albumsTable.getColumnModel().getColumn(3).setMaxWidth(85);
        albumsTable.getColumnModel().getColumn(4).setMaxWidth(85);

        moviesTable.getColumnModel().getColumn(1).setMaxWidth(75);
        moviesTable.getColumnModel().getColumn(2).setMaxWidth(95);
        TableRowSorter sorter = new TableRowSorter(moviesTableModel);
        sorter.setComparator(2, new DateComparator());
        moviesTable.setRowSorter(sorter);

        //defaultGamesBorder = gamesPanel.getBorder();

        defaultMoviesBorder = moviesPanel.getBorder();
        defaultAlbumsBorder = albumsPanel.getBorder();
        defaultSongsBorder = songsPanel.getBorder();

        sorter = null;
        renderer = null;

        ToolTipManager.sharedInstance().unregisterComponent(moviesTable);
        ToolTipManager.sharedInstance().unregisterComponent(moviesTable.getTableHeader());
        ToolTipManager.sharedInstance().unregisterComponent(songsTable);
        ToolTipManager.sharedInstance().unregisterComponent(songsTable.getTableHeader());
        ToolTipManager.sharedInstance().unregisterComponent(albumsTable);
        ToolTipManager.sharedInstance().unregisterComponent(albumsTable.getTableHeader());
        ToolTipManager.sharedInstance().unregisterComponent(moviesTable);
        ToolTipManager.sharedInstance().unregisterComponent(moviesTable.getTableHeader());


        ((JComponent) albumsTable.getDefaultRenderer(Boolean.class)).setOpaque(true);
        ((JComponent) resultsTable.getDefaultRenderer(Boolean.class)).setOpaque(true);
        ((JComponent) moviesTable.getDefaultRenderer(Boolean.class)).setOpaque(true);
        ((JComponent) songsTable.getDefaultRenderer(Boolean.class)).setOpaque(true);

        createDataTables();
    }

    /**
     * This method creates the data vectors for the left and right tables (left
     * = nzb files, right = download file queue).
     */
    protected void createDataTables() {
        DownloadFileListPopupListener dlFileListener = null;

        nzbFileQueueTabModel = new NzbFileQueueTableModel(localer);
        filesToDownloadTabModel = new FilesToDownloadTableModel(localer);

        // create left JTable (nzb files)
        nzbListTab.setModel(nzbFileQueueTabModel);
        nzbListTab.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        nzbListTab.getTableHeader().setReorderingAllowed(false);
        nzbListTab.addMouseListener(new NzbFileListPopupListener(nzbListTab));

        // set table cell renderers (left)
        ProgressRenderer cellRenderer = new ProgressRenderer();
        nzbListTab.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);


        // create right JTable (files to download)
        filesToDownloadTab.setModel(filesToDownloadTabModel);
        filesToDownloadTab.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dlFileListener = new DownloadFileListPopupListener(filesToDownloadTab);
        filesToDownloadTab.addMouseListener(dlFileListener);
        filesToDownloadTab.getSelectionModel().addListSelectionListener(dlFileListener);
        filesToDownloadTab.getColumnModel().getColumn(0).setMinWidth(333);
        filesToDownloadTab.getColumnModel().getColumn(1).setMaxWidth(100);
        filesToDownloadTab.getColumnModel().getColumn(2).setMaxWidth(75);
        filesToDownloadTab.getColumnModel().getColumn(3).setMinWidth(50);
        filesToDownloadTab.getTableHeader().setReorderingAllowed(false);

        // set table cell renderers (right)
        filesToDownloadTab.getColumnModel().getColumn(0).setCellRenderer(new FilenameRenderer());
        filesToDownloadTab.getColumnModel().getColumn(1).setCellRenderer(new FilesizeRenderer());
        filesToDownloadTab.getColumnModel().getColumn(2).setCellRenderer(new SegCountRenderer());
        filesToDownloadTab.getColumnModel().getColumn(3).setCellRenderer(new ProgressRenderer());


        // set table header height
        nzbListTab.getTableHeader().setPreferredSize(
                new Dimension(nzbListTab.getColumnModel().getTotalColumnWidth(), 20));
        filesToDownloadTab.getTableHeader().setPreferredSize(
                new Dimension(filesToDownloadTab.getColumnModel().getTotalColumnWidth(), 20));

        //NZBListener listener = new NZBListener(nzbListTab);
        //nzbListTab.getSelectionModel().addListSelectionListener(listener);
        //nzbListTab.getColumnModel().getSelectionModel().addListSelectionListener(listener);
    }

    private void jbInit() throws Exception {
        // add the listener to the jtable
        MouseListener popupListener = new PopupListener();
        // add the listener specifically to the header
        moviesTable.addMouseListener(popupListener);

        //   gamesTable.addMouseListener(popupListener);

        songsTable.addMouseListener(popupListener);
        albumsTable.addMouseListener(popupListener);
        //jTable1.getTableHeader().addMouseListener(popupListener);
        //MouseListener parListener = new DownloadedTablePopup();
        //downloadedTable.addMouseListener(parListener);

        popupListener = null;
        //parListener = null;
    }

    private void createEditMenu() {
        JMenuItem menuItem;
        JPopupMenu popup = new JPopupMenu();

        menuItem = new JMenuItem("Cut");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField textField = (JTextField) editPopupMenu.getInvoker();
                String wholeString = textField.getText();
                StringSelection stringSelection = new StringSelection(textField.getSelectedText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, main);
                textField.setText(wholeString.replace(textField.getSelectedText(), ""));
            }
        });
        popup.add(menuItem);

        menuItem = new JMenuItem("Copy");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField textField = (JTextField) editPopupMenu.getInvoker();
                StringSelection stringSelection = new StringSelection(textField.getSelectedText());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, main);
            }
        });
        popup.add(menuItem);

        menuItem = new JMenuItem("Paste");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField textField = (JTextField) editPopupMenu.getInvoker();
                Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                String clipString = null;
                try {
                    clipString = (String) contents.getTransferData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException ex) {
                    getLogger().log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    getLogger().log(Level.SEVERE, null, ex);
                }
                if (clipString != null) {
                    textField.setText("");
                    textField.setText(clipString);
                }
            }
        });
        popup.add(menuItem);
        editPopupMenu = popup;
    }

    class PopupListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {

            if (e.isPopupTrigger()) {
                // get the coordinates of the mouse click
                Point p = e.getPoint();

                if (selectedTable != null) {
                    // get the row index that contains that coordinate
                    final int rowNumber = selectedTable.rowAtPoint(p);

                    // Get the ListSelectionModel of the JTable
                    ListSelectionModel model = selectedTable.getSelectionModel();

                    // set the selected interval of rows. Using the "rowNumber"
                    // variable for the beginning and end selects only that one row.
                    model.setSelectionInterval(rowNumber, rowNumber);
                    popupMenu = new JPopupMenu();

                    JMenuItem menuItem = new JMenuItem("Search in Google");
                    menuItem.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                            String url = "http://www.google.com/search?q=";
                            int modelRow = selectedTable.convertRowIndexToModel(rowNumber);
                            Media media = ((MediaTableModel) selectedTable.getModel()).getMediaAt(modelRow);
                            String title = media.getTitle();
                            if (media.isAlbum() || media.isSong()) {
                                url += media.getArtist().replaceAll(" ", "+") + "+";
                            }
                            url += title.replaceAll(" ", "+");
                            displayInBrowser(url.replaceAll("&", "and"));
                        }
                    });
                    popupMenu.add(menuItem);

                    if (selectedTable == moviesTable) {
                        JMenuItem menuItem2 = new JMenuItem("Go to VideoETA");
                        menuItem2.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                String url = "http://www.videoeta.com/";
                                displayInBrowser(url);
                            }
                        });
                        popupMenu.add(menuItem2);
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }

    public static void printQueue() {
        for (int i = 0; i < nzbFileQueueTabModel.getRowCount(); i++) {
            System.out.println(i + ") " + nzbFileQueueTabModel.getNzbParser(i).getId());
        }
    }

    /*
     * class DownloadedTablePopup extends MouseAdapter {
     *
     * @Override public void mousePressed(MouseEvent e) {
     *
     *
     * showPopup(e); }
     *
     * @Override public void mouseReleased(MouseEvent e) { showPopup(e); }
     *
     *
     * private void showPopup(MouseEvent e) {
     *
     * if (e.isPopupTrigger()) { // get the coordinates of the mouse click Point p = e.getPoint();
     *
     * // get the row index that contains that coordinate //final int rowNumber =
     * downloadedTable.rowAtPoint(p);
     *
     * // Get the ListSelectionModel of the JTable ListSelectionModel model =
     * downloadedTable.getSelectionModel();
     *
     * // set the selected interval of rows. Using the "rowNumber" // variable for the beginning
     * and end selects only that one row. model.setSelectionInterval(rowNumber, rowNumber);
     * popupMenu = new JPopupMenu();
     *
     * JMenuItem menuItem = new JMenuItem("Send to Par N Rar"); menuItem.addActionListener(new
     * java.awt.event.ActionListener() {
     *
     * public void actionPerformed(java.awt.event.ActionEvent evt) { ArrayList<String> cmd = new
     * ArrayList<String>(); cmd.add("C:\\Program Files (x86)\\ParNRar\\ParNRar.exe");
     * cmd.add("/min"); cmd.add("/m \"" + prefs.get("downlpath").toString() + "\""); cmd.add("/go");
     * String[] cmdArray = cmd.toArray(new String[]{});
     *
     * // execute command Runtime rt = Runtime.getRuntime(); try { Process proc = rt.exec(cmdArray,
     * null, new File(getAppDir())); } catch (IOException ex) { getLogger().log(Level.SEVERE, null,
     * ex); } } });
     *
     *
     *
     * popupMenu.add(menuItem); //popupMenu.add(menuItem2); popupMenu.show(e.getComponent(),
     * e.getX(), e.getY()); } } }
     *
     */
    public static DownloadsTableModel getDownloadsModel() {
        synchronized (downloadsTableModel) {
            return downloadsTableModel;
        }
    }

    public static void addDownload(Download d) {
        System.out.println("Download added with id " + d.getDownId());
        synchronized (downloadsTableModel) {
            downloadsTableModel.addDownload(d);
        }
    }

    public static void setResults(ArrayList<BinsearchResult> res, boolean doMore, int pageNumber) {
        if (res != null && res.size() > 0) {
            if (downloadMode == 0) {
                if (resultsTableModel.getRowCount() == 0) {
                    resultsTableModel = new BinsearchTableModel(res);
                } else {
                    for (BinsearchResult br : res) {
                        ((BinsearchTableModel) resultsTableModel).addResult(br);
                    }
                }
            } else {
                resultsTableModel = new ResultsTableModel(res);
            }
            resultsTable.setModel(resultsTableModel);
            resultsTableModel.fireTableDataChanged();
            resultsTable.getColumnModel().getColumn(0).setPreferredWidth(550);
            TableRowSorter sorter = new TableRowSorter(resultsTableModel);
            sorter.setComparator(1, sizeComparator);
            resultsTable.setRowSorter(sorter);
            if (downloadMode == 0) {
                resultsTable.getColumnModel().getColumn(2).setCellRenderer(partRenderer);
            }
            //titleField.setText(res.get(0).getMedia().getTitle());
            if (res.get(0).getMedia().isAlbum() || res.get(0).getMedia().isSong()) {
                //artistField.setText(res.get(0).getMedia().getArtist());
            } else {
                //artistField.setText("");
            }
            //typeCombo.setSelectedIndex(res.get(0).getMedia().getType() - 1);
        }
        /*
         * if (doMore) { Media m = res.get(0).getMedia(); String url = resolveBinsearchURL(m);
         * BinsearchParse parseTask = new BinsearchParse(m, pageNumber, url); Thread thread = new
         * Thread(parseTask); thread.start();
         *
         */
        //  } else {
        //main.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        //Main.setStatus(resultsTableModel.getRowCount() + " result(s) found");
        //currentSearch = null;
        // }
    }

    public static void searchDone() {
        main.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        resultsTable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        resultsTable.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        jScrollPane3.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        jPanel9.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        toolbarSearchButton.setEnabled(true);
        searchButton.setEnabled(true);
        stopSearchButton.setEnabled(false);
        stopProgressBar();
        setStatus("");
        try {
            startSignal.countDown();
        } catch (Exception e) {
        }
    }

    public static void setResultsTotal() {
        Main.setStatus(resultsTableModel.getRowCount() + " result(s) found");
    }

    public static ArrayList<IRCConnection> getConnections() {
        return connections;
    }

    public static void setIRCInfo(String name, String nick, String email, String alternate, String pass) {
        JDBC.setIRCOptions(name, nick, email, alternate, pass);
    }

    /*
     * public static void setUsenetInfo(String host, String user, String pass) { UsenetHost = host;
     * UsenetUser = user; UsenetPass = pass; //JDBC.setUsenetOptions(host, user, pass); }
     *
     */
    public static void removeConnection(IRCConnection irc) {
        for (int i = 0; i < connections.size(); i++) {
            if (irc.getNetwork().equals(connections.get(i).getNetwork())) {
                connections.remove(i);
                break;
            }
        }
    }

    public static void resetSearchButtons() {
        stopSearchButton.setEnabled(false);
        searchButton.setEnabled(true);
    }

    /*
     * // Called when table row selection changes. private void tableSelectionChanged() {
     * Unregister from receiving notifications from the last selected download. if (selectedDownload
     * != null) { selectedDownload.deleteObserver(Main.this); }
     *
     * If not in the middle of clearing a download, set the selected download and register to
     * receive notifications from it. if (!clearing) { try { int viewRow =
     * downloadsTable.getSelectedRow(); if (viewRow != -1) { int modelRow =
     * downloadsTable.convertRowIndexToModel(viewRow); selectedDownload =
     * downloadsTableModel.getDownload(modelRow); updateButtons(); } } catch (Exception e) {
     * selectedDownload = null; } } }
     */
    public static void setMaxDownloads(int num) {
        prefs.put("maxdownloads", num);
    }

    public static String getDownDir() {
        return prefs.get("downpath").toString();
    }

    public static void setDownDir(String s) {
        prefs.put("downpath", s);
        //JDBC.setDownloadDir(downloadDir);
    }

    private static String checkSpecialWords(String s) {
        if (s.equalsIgnoreCase("P!nk")) {
            return "Pink";
        } else if (s.equalsIgnoreCase("F*ck") || s.equalsIgnoreCase("F**k")) {
            return "Fuck";
        } else if (s.equalsIgnoreCase("Far*East")) {
            return "Far+East";
        } else if (s.equalsIgnoreCase("Ke$ha")) {
            return "Kesha";
        } else if (s.endsWith("?") || s.endsWith("!")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String resolveBinsearchURL(Media s) {
        String searchQuery;
        String min;
        String max;
        if (s.getType() == Media.SONG || s.getType() == Media.ALBUM) {
            String[] artists = s.getArtist().split("Featuring");
            String[] title = s.getTitle().split(" ");
            String query = "";
            String[] firstArtist = artists[0].trim().split(" ");
            for (String str : firstArtist) {
                query += checkSpecialWords(str) + "+";
            }
            for (String str : title) {
                query += checkSpecialWords(str) + "+";
            }
            searchQuery = query;
            if (s.getType() == Media.SONG) {
                min = Integer.toString(Media.minSongSize);
                max = Integer.toString(Media.maxSongSize);
            } else {
                min = Integer.toString(Media.minAlbumSize);
                max = Integer.toString(Media.maxAlbumSize);
            }
        } else {
            String[] title = s.getTitle().split(" ");
            String query = "";
            for (String str : title) {
                query += checkSpecialWords(str) + "+";
            }
            searchQuery = query;
            if (s.getType() == Media.MOVIE) {
                min = Integer.toString(Media.minMovieSize);
                max = Integer.toString(Media.maxMovieSize);
            } else if (s.getType() == Media.GAME) {
                min = Integer.toString(Media.minGameSize);
                max = Integer.toString(Media.maxGameSize);
            } else if (s.getType() == Media.TVSHOW) {
                min = Integer.toString(Media.minTVSize);
                max = Integer.toString(Media.maxTVSize);
            } else {
                min = Integer.toString(Media.minOtherSize);
                max = Integer.toString(Media.maxOtherSize);
            }
        }
        if (!minSizeField.getText().equals("")) {
            min = minSizeField.getText();
        } else {
            minSizeField.setText(min);
        }
        if (!maxSizeField.getText().equals("")) {
            max = maxSizeField.getText();
        } else {
            maxSizeField.setText(max);
        }
        String sizeRange = "minsize=" + min + "&maxsize=" + max;
        String host = "http://www.binsearch.info/";
        String query = "index.php?q=" + searchQuery.replaceAll("\"", "") + "&m=&max=25&adv_g=&adv_age=999&adv_sort=date&" + sizeRange;
        String binSearchURL = null;
        try {
            binSearchURL = host + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return binSearchURL;
    }

    private void displayInBrowser(String searchURL) {
        try {
            if (!java.awt.Desktop.isDesktopSupported()) {
                System.err.println("Desktop is not supported (fatal)");
            }
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                System.err.println("Desktop doesn't support the browse action (fatal)");
            }
            URL url = new URL(searchURL);
            URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
            desktop.browse(uri);
        } catch (URISyntaxException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    /*
     * private String getAndSelectFirstResult(String url) { String newURL = "N/A"; boolean found =
     * false; try { URL site = new URL(url); System.out.println(site); URLConnection yc =
     * site.openConnection(); BufferedReader in = new BufferedReader(new
     * InputStreamReader(yc.getInputStream())); String inputLine; while ((inputLine = in.readLine())
     * != null) { System.out.println(inputLine); if (inputLine.contains("No results containing
     * all")) { found = false; } else { if (inputLine.contains("Save to quick list")) { int start =
     * inputLine.indexOf("Save to quick list"); int urlStart = inputLine.indexOf("http", start); int
     * urlEnd = inputLine.indexOf("html", urlStart); newURL = inputLine.substring(urlStart, urlEnd +
     * 4); found = true; jLabel1.setText(""); break; } } } if (!found) { jLabel1.setText("No results
     * found for " + downSong.getArtist() + " - " + downSong.getTitle()); newURL = "N/A"; }
     * in.close(); } catch (IOException ex) { getLogger().log(Level.SEVERE, null, ex);
     * jLabel1.setText("No results found for " + downSong.getArtist() + " - " +
     * downSong.getTitle()); newURL = "N/A"; } return newURL; }
     *
     */
    public void addSong(String artist, String song) {
        if (!exists(artist, song)) {
            Media toAdd = new Media(artist, song, false);
            toAdd.setType(Media.SONG);
            songsTableModel.addMedia(toAdd);
            updateTable();
        }
    }

    private static void saveData() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main.setStatus("Saving information to database...");
            }
        });
        logger.log(Level.INFO, "Saving queue data");
        ArrayList<Media> list = new ArrayList<Media>();
        list.addAll(songsTableModel.getMedia());
        list.addAll(moviesTableModel.getMedia());
        list.addAll(gamesTableModel.getMedia());
        list.addAll(albumsTableModel.getMedia());
        JDBC.setSearchQueue(list);

        logger.log(Level.INFO, "Queue data saved");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main.setStatus("Saving done!!!...");
            }
        });
    }

    private void loadData() {
        ArrayList<Media> list = new ArrayList<Media>();
        list = JDBC.loadSearchQueue();
        songsTableModel = new SongsTableModel();
        moviesTableModel = new MoviesTableModel();
        gamesTableModel = new GamesTableModel();
        albumsTableModel = new AlbumsTableModel();
        for (Media s : list) {
            if (s.isSong()) {
                songsTableModel.addMedia(s);
            } else if (s.isMovie()) {
                moviesTableModel.addMedia(s);
            } else if (s.isAlbum()) {
                albumsTableModel.addMedia(s);
            } else if (s.isGame()) {
                gamesTableModel.addMedia(s);
            }
        }
        list = null;
    }

    private boolean exists(String a, String t) {
        boolean found = false;
        for (Media s : songsTableModel.getMedia()) {
            if (a.equals(s.getArtist()) && t.equals(s.getTitle())) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void updateTable() {
        try {
            songsTableModel.fireTableDataChanged();
        } catch (Exception e) {
            System.out.println("Unable to update table");
        }
    }

    public static String getStatus() {
        return jLabel1.getText();
    }

    public static void setStatus(String text) {
        jLabel1.setText("    " + text);
    }

    private static void closeAllConnections() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main.setStatus("Closing connections...");
            }
        });
        if (connections != null) {
            for (IRCConnection connection : connections) {
                try {
                    connection.disconnect();
                    connection.stopThread();
                } catch (Exception e) {
                    connection.stopThread();
                }
            }
        }
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        }

    }

    private static class MyCallable implements Callable {

        private boolean doOtherCharts;

        public MyCallable(boolean doOthers) {
            doOtherCharts = doOthers;
        }

        @Override
        public Object call() throws Exception {
            BillboardChartParser parser = null;
            ExecutorService executor = Executors.newSingleThreadExecutor();
            LinkedList<Callable<Object>> callables = new LinkedList<Callable<Object>>();

            if ((Boolean) prefs.get("hot100")) {
                parser = new BillboardChartParser(Main.main, 100, "Hot 100", hot100BillboardURL);
                callables.add(Executors.callable(parser));
            }
            if ((Boolean) prefs.get("latin")) {
                parser = new BillboardChartParser(Main.main, 25, "Latin", latinBillboardURL);
                callables.add(Executors.callable(parser));
            }
            if ((Boolean) prefs.get("hiphop")) {
                parser = new BillboardChartParser(Main.main, 50, "R&B/Hip-Hop", rbHipHopBillboardURL);
                callables.add(Executors.callable(parser));
            }
            if ((Boolean) prefs.get("country")) {
                parser = new BillboardChartParser(Main.main, 30, "Country", countryBillboardURL);
                callables.add(Executors.callable(parser));
            }
            if ((Boolean) prefs.get("rock")) {
                parser = new BillboardChartParser(Main.main, 25, "Rock", rockBillboardURL);
                callables.add(Executors.callable(parser));
            }
            if ((Boolean) prefs.get("pop")) {
                parser = new BillboardChartParser(Main.main, 20, "Pop", popBillboardURL);
                callables.add(Executors.callable(parser));
            }
            if ((Boolean) prefs.get("danceclub")) {
                parser = new BillboardChartParser(Main.main, 25, "Dance/Club Play", danceBillboardURL);
                callables.add(Executors.callable(parser));
            }
            if ((Boolean) prefs.get("estereotempo")) {
                callables.add(Executors.callable(new EstereoTempoParser(Main.main)));
            }
            if ((Boolean) prefs.get("fidelity")) {
                callables.add(Executors.callable(new FidelityParser(Main.main)));
            }
            if ((Boolean) prefs.get("kq105")) {
                callables.add(Executors.callable(new KQ105Parser(Main.main)));
            }
            if ((Boolean) prefs.get("lamega")) {
                callables.add(Executors.callable(new LaMegaParser(Main.main)));
            }
            if ((Boolean) prefs.get("reggaeton94")) {
                callables.add(Executors.callable(new Reggaeton94Parser(Main.main)));
            }
            if ((Boolean) prefs.get("tocadeto")) {
                callables.add(Executors.callable(new TocaDeToParser(Main.main)));
            }
            if (doOtherCharts) {
                BillboardAlbumParse bap = new BillboardAlbumParse(Main.main, jProgressBar1);
                callables.add(Executors.callable(bap));
                VideoETAParse eta = new VideoETAParse(Main.main, jProgressBar1);
                callables.add(Executors.callable(eta));
            }
            if (parser != null) {
                JDBC.setLastWeekChecked(Calendar.getInstance().getTime());
            }
            try {
                executor.invokeAll(callables);
            } catch (InterruptedException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
            stopProgressBar();
            Main.setStatus("");
            setDefaultCursor();
            updateButton.setEnabled(true);
            updateAllButton.setEnabled(true);
            //stopButton.setEnabled(false);
            executor.shutdown();
            return 0;
        }
    }

    private void createUpdateParsers(boolean doOtherCharts) {
        updateButton.setEnabled(false);
        updateAllButton.setEnabled(false);
        ExecutorService executor1 = Executors.newSingleThreadExecutor();
        MyCallable mine = new MyCallable(doOtherCharts);
        startProgressBar();
        executor1.submit(mine);
        executor1.shutdown();
    }

    public static JProgressBar getProgressBar() {
        return jProgressBar1;
    }

    public static void setDefaultCursor() {
        main.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        resultsTable.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        songsTable.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        albumsTable.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        moviesTable.getTableHeader().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /*
     * public static boolean connectedToServer(String server) { for (IRCConnection conn :
     * connections) { if (conn.getNetwork().equalsIgnoreCase(server)) { try {
     * conn.sendPingReply("testing"); System.out.println("connected to network " + server); return
     * true; } catch (Exception e) { System.out.println("not connected to network " + server); try {
     * connections.remove(conn); } catch (Exception ee) { } return false; } } } return false; }
     *
     */
    public static boolean connectedToChannel(String chan, String network) {
        for (IRCConnection conn : connections) {
            if (conn.getNetwork().equals(network)) {
                for (String channel : conn.getConnectedChannels()) {
                    if (channel.equals(chan)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static IRCConnection getServerConnection(String server) {
        for (IRCConnection conn : connections) {
            if (conn.getNetwork().equals(server)) {
                return conn;
            }
        }
        return null;
    }

    private static void saveDownloadedInDB(ArrayList<Media> list) {
        for (Media d : list) {
            if (d.isDownloaded()) {
                //if (!JDBC.checkDownloaded(d)) {
                JDBC.setDownloaded(d);
                //}
            }
        }
    }

    private static ArrayList<BinsearchResult> hasMoreCDs(BinsearchResult result) {
        ArrayList<BinsearchResult> toReturn = new ArrayList<BinsearchResult>();
        for (int i = 0; i < resultsTableModel.getRowCount(); i++) {
            BinsearchResult res = ((BinsearchTableModel) resultsTableModel).getResultAt(i);
            if (Utils.areFromSameMedia(result, res)) {
                toReturn.add(res);
            }
        }
        return toReturn;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        songsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        songsTable = new javax.swing.JTable();
        albumsPanel = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        albumsTable = new javax.swing.JTable();
        moviesPanel = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        moviesTable = new javax.swing.JTable();
        jButton7 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        toolbarSearchButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        updateAllButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();
        jButton10 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jPanel12 = new javax.swing.JPanel();
        typeCombo = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        titleField = new javax.swing.JTextField();
        artistField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        searchButton = new javax.swing.JButton();
        stopSearchButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        minSizeField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        maxSizeField = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        autoGroup = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        nzbListTab = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        filesToDownloadTab = new javax.swing.JTable();
        startToggleButton = new javax.swing.JToggleButton();
        jPanel6 = new javax.swing.JPanel();
        currDlSpeed = new javax.swing.JLabel();
        jButton5 = new javax.swing.JButton();
        exitCheckbox = new javax.swing.JCheckBox();
        shutdownCheckbox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        etaAndTotalText = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        errorsLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Music Checker 2.0");
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(1010, 580));
        setPreferredSize(new java.awt.Dimension(1010, 620));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanel4.setPreferredSize(new java.awt.Dimension(953, 731));

        songsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Songs", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 255)));

        jScrollPane1.setBackground(new java.awt.Color(255, 255, 255));

        songsTable.setAutoCreateRowSorter(true);
        songsTable.setFillsViewportHeight(true);
        songsTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        songsTable.setShowHorizontalLines(false);
        songsTable.setShowVerticalLines(false);
        songsTable.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                songsTableFocusGained(evt);
            }
        });
        jScrollPane1.setViewportView(songsTable);

        javax.swing.GroupLayout songsPanelLayout = new javax.swing.GroupLayout(songsPanel);
        songsPanel.setLayout(songsPanelLayout);
        songsPanelLayout.setHorizontalGroup(
            songsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(songsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 973, Short.MAX_VALUE)
                .addContainerGap())
        );
        songsPanelLayout.setVerticalGroup(
            songsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
        );

        albumsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Albums", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 255)));

        jScrollPane7.setBackground(new java.awt.Color(255, 255, 255));

        albumsTable.setAutoCreateRowSorter(true);
        albumsTable.setFillsViewportHeight(true);
        albumsTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        albumsTable.setShowHorizontalLines(false);
        albumsTable.setShowVerticalLines(false);
        albumsTable.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                albumsTableFocusGained(evt);
            }
        });
        jScrollPane7.setViewportView(albumsTable);

        javax.swing.GroupLayout albumsPanelLayout = new javax.swing.GroupLayout(albumsPanel);
        albumsPanel.setLayout(albumsPanelLayout);
        albumsPanelLayout.setHorizontalGroup(
            albumsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(albumsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
                .addContainerGap())
        );
        albumsPanelLayout.setVerticalGroup(
            albumsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
        );

        moviesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Movies", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 255)));

        jScrollPane6.setBackground(new java.awt.Color(255, 255, 255));

        moviesTable.setAutoCreateRowSorter(true);
        moviesTable.setFillsViewportHeight(true);
        moviesTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        moviesTable.setShowHorizontalLines(false);
        moviesTable.setShowVerticalLines(false);
        moviesTable.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                moviesTableFocusGained(evt);
            }
        });
        jScrollPane6.setViewportView(moviesTable);

        javax.swing.GroupLayout moviesPanelLayout = new javax.swing.GroupLayout(moviesPanel);
        moviesPanel.setLayout(moviesPanelLayout);
        moviesPanelLayout.setHorizontalGroup(
            moviesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(moviesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                .addContainerGap())
        );
        moviesPanelLayout.setVerticalGroup(
            moviesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        jButton7.setText("Ignore Media");
        jButton7.setToolTipText("Ignore selected media (won't show up again, even after update)");
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton1.setText("Clear Downloaded");
        jButton1.setToolTipText("Clear all downloaded media in selected table");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton6.setText("Ignore Artist");
        jButton6.setToolTipText("Ignore all media from the selected media's artist");
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton23.setText("Clear All");
        jButton23.setToolTipText("Clear selected table's contents");
        jButton23.setFocusable(false);
        jButton23.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton23.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });

        jButton21.setText("Remove");
        jButton21.setToolTipText("Remove selected media");
        jButton21.setFocusable(false);
        jButton21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton21.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton21ActionPerformed(evt);
            }
        });

        toolbarSearchButton.setText("Search");
        toolbarSearchButton.setToolTipText("Search for selected media");
        toolbarSearchButton.setFocusable(false);
        toolbarSearchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarSearchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarSearchButtonActionPerformed(evt);
            }
        });

        updateButton.setText("Update");
        updateButton.setToolTipText("Update selected table");
        updateButton.setFocusable(false);
        updateButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        updateAllButton.setText("Update All");
        updateAllButton.setToolTipText("Update All Tables");
        updateAllButton.setFocusable(false);
        updateAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        updateAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateAllButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(albumsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(moviesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(songsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(updateAllButton)
                .addGap(2, 2, 2)
                .addComponent(updateButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopButton)
                .addGap(50, 50, 50)
                .addComponent(toolbarSearchButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton23)
                .addGap(61, 61, 61)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6)
                .addGap(61, 61, 61)
                .addComponent(jButton21)
                .addGap(19, 19, 19))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(updateAllButton)
                    .addComponent(updateButton)
                    .addComponent(toolbarSearchButton)
                    .addComponent(jButton1)
                    .addComponent(jButton23)
                    .addComponent(jButton7)
                    .addComponent(jButton6)
                    .addComponent(jButton21)
                    .addComponent(stopButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(songsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(albumsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(moviesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        jTabbedPane1.addTab("Media", jPanel4);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Search Results", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 255)));

        resultsTable.setAutoCreateRowSorter(true);
        resultsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        resultsTable.setFillsViewportHeight(true);
        jScrollPane3.setViewportView(resultsTable);

        jButton10.setText("Download");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton8.setText("Channel Communication");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jButton2.setText("Disconnect all");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton9.setText("Print string");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        jButton15.setText("Sort Best");
        jButton15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton15ActionPerformed(evt);
            }
        });

        jButton3.setText("View Parts");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 953, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addGap(75, 75, 75)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton15)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton10)
                    .addComponent(jButton3)
                    .addComponent(jButton2)
                    .addComponent(jButton8)
                    .addComponent(jButton9)
                    .addComponent(jButton15)))
        );

        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "New Search", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 51, 204)));

        jLabel2.setText("Type:");

        jLabel3.setText("Title: ");

        titleField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                titleFieldMouseClicked(evt);
            }
        });
        titleField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                titleFieldFocusLost(evt);
            }
        });

        artistField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                artistFieldMouseClicked(evt);
            }
        });
        artistField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                artistFieldFocusLost(evt);
            }
        });

        jLabel4.setText("Artist: ");

        searchButton.setText("Search");
        searchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        stopSearchButton.setText("Stop Search");
        stopSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopSearchButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Min Size (MB):");

        jLabel6.setText("Max Size (MB):");

        jButton4.setText("Clear Fields");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(artistField)
                            .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel12Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel12Layout.createSequentialGroup()
                        .addComponent(searchButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(stopSearchButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 170, Short.MAX_VALUE)
                        .addComponent(jButton4))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(typeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 255, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel12Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {artistField, titleField});

        jPanel12Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {maxSizeField, minSizeField});

        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(minSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(maxSizeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(typeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(titleField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(artistField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4)
                    .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(searchButton)
                        .addComponent(stopSearchButton)))
                .addContainerGap())
        );

        jPanel12Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {artistField, titleField});

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(51, 0, 255)));

        autoGroup.setText("Group multiple parts of same media into 1 NZB");
        autoGroup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoGroupActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(autoGroup)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(autoGroup)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 251, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(102, 102, 102))
        );

        jTabbedPane1.addTab("Search Results", jPanel9);

        nzbListTab.setFillsViewportHeight(true);
        nzbListTab.setRowHeight(23);
        nzbListTab.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(nzbListTab);

        filesToDownloadTab.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        filesToDownloadTab.setFillsViewportHeight(true);
        jScrollPane5.setViewportView(filesToDownloadTab);

        startToggleButton.setText("Start");
        startToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startToggleButtonActionPerformed(evt);
            }
        });

        jPanel6.setLayout(new javax.swing.BoxLayout(jPanel6, javax.swing.BoxLayout.LINE_AXIS));

        currDlSpeed.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jButton5.setText("Open Download Folder");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        exitCheckbox.setText("Exit App after downloads complete");

        shutdownCheckbox.setText("Shutdown after downloads complete");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton5)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 738, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(startToggleButton)
                        .addGap(122, 122, 122)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(shutdownCheckbox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(currDlSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(16, 16, 16))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(exitCheckbox)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(currDlSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                            .addComponent(jButton5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(shutdownCheckbox)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(startToggleButton)
                                .addComponent(exitCheckbox)))
                        .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Download", jPanel1);

        jLabel1.setBackground(new java.awt.Color(204, 204, 255));

        etaAndTotalText.setBackground(new java.awt.Color(204, 204, 255));
        etaAndTotalText.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        jProgressBar1.setIndeterminate(true);
        jProgressBar1.setMinimumSize(new java.awt.Dimension(10, 10));
        jProgressBar1.setOpaque(true);
        jProgressBar1.setPreferredSize(new java.awt.Dimension(146, 10));

        errorsLabel.setBackground(new java.awt.Color(255, 51, 51));
        errorsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        errorsLabel.setText("View errors");
        errorsLabel.setOpaque(true);
        errorsLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                errorsLabelMouseClicked(evt);
            }
        });

        jMenu1.setText("File");

        jMenuItem5.setText("Load NZB");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem5);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Tools");

        jMenuItem3.setText("Auto Download");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem2.setText("Database");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);
        jMenu2.add(jSeparator1);

        jMenuItem4.setText("Options");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuItem1.setText("IRC Settings");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 416, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(errorsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(etaAndTotalText, javax.swing.GroupLayout.PREFERRED_SIZE, 318, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 534, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(etaAndTotalText, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(errorsLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if ((Boolean) prefs.get("closetotray")) {
            main.setVisible(false);
            main.dispose();
        } else {
            doClose();
        }
    }//GEN-LAST:event_formWindowClosing

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        int viewRow = resultsTable.getSelectedRow();
        if (viewRow != -1) {
            File downloadDirectory = new File(prefs.get("downpath").toString());
            if (downloadDirectory.exists()) {
                jTabbedPane1.setSelectedIndex(2);
                int modelRow = resultsTable.convertRowIndexToModel(viewRow);
                startProgressBar();
                if (downloadMode == 1) {
                    XWeaselResult toDown = ((ResultsTableModel) resultsTableModel).getResultAt(modelRow);
                    System.out.println("Requested server is: " + toDown.getNetwork());
                    System.out.println("Active servers are: ");
                    for (IRCConnection conn : connections) {
                        System.out.println(conn.getNetwork());
                    }
                    IRCConnection connection = getServerConnection(toDown.getNetwork());
                    if (connection == null) {
                        IRCConnection ircconn = new IRCConnection(toDown, currentSearch.getMedia());
                        Thread thread = new Thread(ircconn);
                        if (connections == null) {
                            connections = new ArrayList<IRCConnection>();
                        }
                        connections.add(ircconn);
                        thread.start();
                    } else {
                        if (!connectedToChannel(toDown.getChannel(), toDown.getNetwork())) {
                            connection.joinChannel(toDown.getChannel());
                        }
                        connection.requestFile(toDown.getBot(), toDown.getPack());
                    }
                } else {
                    BinsearchResult res = ((BinsearchTableModel) resultsTableModel).getResultAt(modelRow);
                    ArrayList<BinsearchResult> results1 = null;
                    if (autoGroup.isSelected()) {
                        results1 = hasMoreCDs(res);
                    }
                    setStatus("Loading NZB for \"" + res.getMedia().toString() + "\"");
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    Thread thread;
                    if (results1 != null && !results1.isEmpty()) {
                        thread = new Thread(new Binsearch(res, results1, jProgressBar1));
                    } else {
                        thread = new Thread(new Binsearch(res, jProgressBar1));
                    }
                    downloadExecutor = Executors.newSingleThreadExecutor();
                    downloadExecutor.submit(thread);
                    Runnable start = new Runnable() {
                        @Override
                        public void run() {
                            nzb.startDownload();
                        }
                    };
                    //downloadExecutor.submit(start);




                    /*
                     * try { if (!nzb.isDownloadActive()) { latch.await(); latch = new
                     * CountDownLatch(1); nzb.startDownload(); } else { latch = new
                     * CountDownLatch(1); } } catch (InterruptedException ex) {
                     * getLogger().log(Level.SEVERE, null, ex); }
                     *
                     */

                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid download directory. "
                        + "Please change it in the Options window");
            }
            /*
             * if (!connectedToServer(toDown.getNetwork())) { IRCConnection ircconn = new
             * IRCConnection(toDown, currentSearch.getMedia()); Thread thread = new Thread(ircconn);
             * thread.start(); connections.add(ircconn); } else { IRCConnection connection =
             * getServerConnection(toDown.getNetwork()); if
             * (!connectedToChannel(toDown.getChannel(), toDown.getNetwork())) {
             * connection.joinChannel(toDown.getChannel()); }
             * connection.requestFile(toDown.getBot(), toDown.getPack()); }
             *
             */
        }
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        Options ops = new Options();
        ops.setVisible(true);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        IRCSettings ircs = new IRCSettings();
        ircs.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        Disconnect disconnect = new Disconnect(connections);
        disconnect.setVisible(true);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        closeAllConnections();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        int viewRow = resultsTable.getSelectedRow();
        if (viewRow != -1) {
            int modelRow = resultsTable.convertRowIndexToModel(viewRow);
            if (downloadMode == 1) {
                XWeaselResult toDown = ((ResultsTableModel) resultsTableModel).getResultAt(modelRow);
                //Commands.openScript(toDown);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                String request = "/msg " + toDown.getBot() + " xdcc send " + toDown.getPack();
                StringSelection stringSelection = new StringSelection(request);
                clipboard.setContents(stringSelection, this);
                System.out.println(request);
                Commands.openMirc("-s" + toDown.getNetwork() + " -j" + toDown.getChannel());
            }
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void sortBestResults() {
        BinsearchTableModel btModel = (BinsearchTableModel) resultsTable.getModel();
        if (btModel.getRowCount() > 0) {
            ArrayList<SearchResult> results = new ArrayList<SearchResult>();
            for (BinsearchResult result : btModel.getResults()) {
                results.add(result);
            }
            ArrayList<BinsearchResult> sortResults;
            if ((sortResults = ResultSelection.sortBestSongResults(results, currentSearch)).size() > 0) {
                resultsTableModel = new BinsearchTableModel();
                Main.setResults(sortResults, false, 0);
            }
        }
    }

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        //try {
        sortBestResults();
        /*
         * } catch (Exception e) { System.out.println(e); ResultsTableModel btModel =
         * (ResultsTableModel) resultsTable.getModel(); ArrayList<SearchResult> results = new
         * ArrayList<SearchResult>(); for (XWeaselResult result : btModel.getResults()) {
         * results.add(result); } ResultSelection.sortBestSongResults(results, currentSearch); }
         *
         */
    }//GEN-LAST:event_jButton15ActionPerformed

    private void songsTableFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_songsTableFocusGained
        //    gamesTable.clearSelection();
        moviesTable.clearSelection();
        albumsTable.clearSelection();
        selectedTable = songsTable;
        //     gamesPanel.setBorder(defaultGamesBorder);
        moviesPanel.setBorder(defaultMoviesBorder);
        albumsPanel.setBorder(defaultAlbumsBorder);
        Border redLine = BorderFactory.createLineBorder(Color.RED);
        Border title = BorderFactory.createTitledBorder(redLine, "Songs");
        songsPanel.setBorder(title);
        jButton6.setEnabled(true);
        redLine = null;
        title = null;
    }//GEN-LAST:event_songsTableFocusGained

    private void moviesTableFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_moviesTableFocusGained
        //     gamesTable.clearSelection();
        songsTable.clearSelection();
        albumsTable.clearSelection();
        selectedTable = moviesTable;
        //     gamesPanel.setBorder(defaultGamesBorder);
        albumsPanel.setBorder(defaultAlbumsBorder);
        songsPanel.setBorder(defaultSongsBorder);
        Border redLine = BorderFactory.createLineBorder(Color.RED);
        Border title = BorderFactory.createTitledBorder(redLine, "Movies");
        moviesPanel.setBorder(title);
        jButton6.setEnabled(false);
        redLine = null;
        title = null;
    }//GEN-LAST:event_moviesTableFocusGained

    private void albumsTableFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_albumsTableFocusGained
        selectedTable = albumsTable;
        //     gamesTable.clearSelection();
        moviesTable.clearSelection();
        songsTable.clearSelection();
        //     gamesPanel.setBorder(defaultGamesBorder);
        moviesPanel.setBorder(defaultMoviesBorder);
        songsPanel.setBorder(defaultSongsBorder);
        Border redLine = BorderFactory.createLineBorder(Color.RED);
        Border title = BorderFactory.createTitledBorder(redLine, "Albums");
        albumsPanel.setBorder(title);
        jButton6.setEnabled(true);
        redLine = null;
        title = null;
    }//GEN-LAST:event_albumsTableFocusGained

    private void toolbarSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarSearchButtonActionPerformed
        if (selectedTable.getSelectedRowCount() > 1) {
            JOptionPane.showMessageDialog(this, "Can only select one row when searching");
        } else {
            int viewRow = selectedTable.getSelectedRow();
            if (viewRow != -1) {
                minSizeField.setText("");
                maxSizeField.setText("");
                stopSearch = false;
                Main.setStatus("Searching...");
                jTabbedPane1.setSelectedIndex(1);
                main.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                startProgressBar();
                try {
                    if (downloadMode == 0) {
                        ((BinsearchTableModel) resultsTableModel).clear();
                    } else {
                        ((ResultsTableModel) resultsTableModel).clear();
                    }
                } catch (Exception e) {
                }
                toolbarSearchButton.setEnabled(false);
                searchButton.setEnabled(false);
                stopSearchButton.setEnabled(true);
                int modelRow = selectedTable.convertRowIndexToModel(viewRow);
                Media s;
                if (selectedTable == songsTable) {
                    s = songsTableModel.getMediaAt(modelRow);
                    artistField.setText(s.getArtist());
                    artistField.setCaretPosition(0);
                } else if (selectedTable == moviesTable) {
                    s = moviesTableModel.getMediaAt(modelRow);
                    artistField.setEnabled(false);
                } else if (selectedTable == albumsTable) {
                    s = albumsTableModel.getMediaAt(modelRow);
                    artistField.setText(s.getArtist());
                    artistField.setCaretPosition(0);
                } else {
                    s = gamesTableModel.getMediaAt(modelRow);
                    artistField.setEnabled(false);
                }
                titleField.setText(s.getTitle());
                titleField.setCaretPosition(0);
                typeCombo.setSelectedIndex(s.getType() - 1);
                String url = resolveBinsearchURL(s);
                currentSearch = new Search(s);
                parseTask = new BinsearchParse(s, 0, url);
                searchThread = new Thread(parseTask);
                searchThread.start();
            }
        }
    }//GEN-LAST:event_toolbarSearchButtonActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        int[] x = selectedTable.getSelectedRows();
        if (x.length > 0) {
            Integer[] temp = new Integer[x.length];
            for (int i = 0; i < x.length; i++) {
                temp[i] = new Integer(selectedTable.convertRowIndexToModel(x[i]));
            }
            Arrays.sort(temp, Collections.reverseOrder());
            ((MediaTableModel) selectedTable.getModel()).removeRows(temp);
            temp = null;
            x = null;
        }
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        int option = JOptionPane.showConfirmDialog(this, "Do you want to clear the selected table?");
        if (option == JOptionPane.OK_OPTION) {
            if (selectedTable == songsTable) {
                songsTableModel.clear();
            } else if (selectedTable == moviesTable) {
                moviesTableModel.clear();
            } else if (selectedTable == albumsTable) {
                albumsTableModel.clear();
            }
            /*
             * else if (selectedTable == gamesTable) { gamesTableModel.clear(); }
             *
             */
        }
    }//GEN-LAST:event_jButton23ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int option = JOptionPane.showConfirmDialog(this, "Do you want to clear downloaded?");
        if (option == JOptionPane.OK_OPTION) {
            if (selectedTable == songsTable) {
                songsTableModel.clearDownloaded();
            } else if (selectedTable == moviesTable) {
                moviesTableModel.clearDownloaded();
            } else if (selectedTable == albumsTable) {
                albumsTableModel.clearDownloaded();
            }
            /*
             * else if (selectedTable == gamesTable) { gamesTableModel.clearDownloaded(); }
             *
             */
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        //updateAllButton.setEnabled(false);
        //updateButton.setEnabled(false);
        if (selectedTable == songsTable) {
            createUpdateParsers(false);
            //lastParser = LatinBillboardParser.class.toString();
        } else if (selectedTable == moviesTable) {
            VideoETAParse eta = new VideoETAParse(this, jProgressBar1);
            eta.execute();
        } else if (selectedTable == albumsTable) {
            BillboardAlbumParse eta = new BillboardAlbumParse(this, jProgressBar1);
            eta.execute();
        } else {
            //updateAllButton.setEnabled(true);
            //updateButton.setEnabled(true);
        }
    }//GEN-LAST:event_updateButtonActionPerformed

    private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        if (!titleField.getText().equals("")) {
            stopSearch = false;
            Main.setStatus("Searching...");
            jPanel9.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            startProgressBar();
            try {
                if (downloadMode == 0) {
                    ((BinsearchTableModel) resultsTableModel).clear();
                } else {
                    ((ResultsTableModel) resultsTableModel).clear();
                }
            } catch (Exception e) {
            }
            toolbarSearchButton.setEnabled(false);
            searchButton.setEnabled(false);
            stopSearchButton.setEnabled(true);
            String title = titleField.getText();
            String artist = artistField.getText();
            String type = (String) typeCombo.getSelectedItem();
            Media s = new Media(title);
            if (type.equals("SONG") || type.equals("ALBUM")) {
                s.setArtist(artist);
            } else {
                s.setArtist("(" + type + ")");
            }
            s.setType(type);
            String url = resolveBinsearchURL(s);
            currentSearch = new Search(s);
            parseTask = new BinsearchParse(s, 0, url);
            searchThread = new Thread(parseTask);
            searchThread.start();
            //if (searchThread.isAlive()) {
            //   stopSearchButton.setEnabled(true);
            //}
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to ignore the all the artists in the selected table?");
        if (option == JOptionPane.OK_OPTION) {
            int[] x = selectedTable.getSelectedRows();
            if (x.length > 0) {
                Integer[] temp = new Integer[x.length];
                for (int i = 0; i < x.length; i++) {
                    temp[i] = new Integer(selectedTable.convertRowIndexToModel(x[i]));
                    JDBC.setIgnored(((MediaTableModel) selectedTable.getModel()).getMediaAt(temp[i]), true);
                }
                Arrays.sort(temp, Collections.reverseOrder());
                ((MediaTableModel) selectedTable.getModel()).removeRows(temp);
            }
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        int[] x = selectedTable.getSelectedRows();
        if (x.length > 0) {
            Integer[] temp = new Integer[x.length];
            for (int i = 0; i < x.length; i++) {
                temp[i] = new Integer(selectedTable.convertRowIndexToModel(x[i]));
                JDBC.setIgnored(((MediaTableModel) selectedTable.getModel()).getMediaAt(temp[i]), false);
            }
            Arrays.sort(temp, Collections.reverseOrder());
            ((MediaTableModel) selectedTable.getModel()).removeRows(temp);
            temp = null;
            x = null;
        }
        /*
         * for (Media m : ((MediaTableModel) selectedTable.getModel()).getMedia()) {
         * System.out.println(m); }
         *
         */
    }//GEN-LAST:event_jButton7ActionPerformed

    private void updateAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAllButtonActionPerformed
        //updateAllButton.setEnabled(false);
        //updateButton.setEnabled(false);
        createUpdateParsers(true);
    }//GEN-LAST:event_updateAllButtonActionPerformed

    private void stopSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopSearchButtonActionPerformed
        stopSearch = true;
        stopSearchButton.setEnabled(false);
        if (searchThread != null) {
            logger.log(Level.INFO, "Stopping search thread...");
            setStatus("Stopping search thread...");
            parseTask.requestStop();
            parseTask = null;
            searchThread.interrupt();
            searchThread = null;
        }
        //stopSearchButton.setEnabled(false);
    }//GEN-LAST:event_stopSearchButtonActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        titleField.setText("");
        artistField.setText("");
        minSizeField.setText("");
        maxSizeField.setText("");
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        //System.gc();
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        String msg;
        String title;
        String filename = "";

        // first check if preferences are complete
        String hostname = (String) prefs.get("usenethost");
        String port1 = prefs.get("usenetport").toString();

        if (hostname.length() == 0 || port1.length() == 0) {
            msg = localer.getBundleText("PopupServerNotSet");
            title = localer.getBundleText("PopupErrorTitle");
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
            return;
        }

        FileNameExtensionFilter filter = new FileNameExtensionFilter("NZB files", "nzb");
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(filter);

        // set file chooser to current directory
        String lastNzbPath = Main.getAppDir();
        if (!"".equals(lastNzbPath)) {
            File lastNzb = new File(lastNzbPath);
            File lastDir = new File(lastNzb.getParent());
            fc.setCurrentDirectory(lastDir);
        }

        int returnVal = fc.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File nzbFile = fc.getSelectedFile();
            try {
                filename = nzbFile.getCanonicalPath();
                NzbParserCreator pc = new NzbParserCreator(nzbFile.getAbsolutePath(), nzb, filename);
                pc.start();
                nzb.clearNzbQueueSelection();
                jTabbedPane1.setSelectedIndex(2);
            } catch (IOException ex) {
                msg = localer.getBundleText("PopupCannotOpenNzb");
                title = localer.getBundleText("PopupErrorTitle");
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
                getLogger().log(Level.SEVERE, null, ex);
            }

        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void startToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startToggleButtonActionPerformed
        //if (nzbFileQueueTabModel.getRowCount() > 0) {
        nzb.startDownload();
        //}
    }//GEN-LAST:event_startToggleButtonActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        int viewRow = resultsTable.getSelectedRow();
        if (viewRow != -1) {
            Runnable showWindow = new Runnable() {
                @Override
                public void run() {
                    partsWindow = new PartsWindow();
                    partsWindow.setVisible(true);
                }
            };

            //ONLY WORKING FOR USENET!!!!!!!!!!!
            int modelRow = resultsTable.convertRowIndexToModel(viewRow);
            final BinsearchResult res = ((BinsearchTableModel) resultsTableModel).getResultAt(modelRow);
            final Binsearch search = new Binsearch(res, jProgressBar1);

            downloadExecutor = Executors.newSingleThreadExecutor();
            downloadExecutor.submit(showWindow);

            Runnable getDownloadFiles = new Runnable() {
                @Override
                public void run() {
                    NzbParser parser = null;
                    try {
                        try {
                            search.getNZB();
                            parser = new NzbParser(nzb, getDownDir()
                                    + System.getProperty("file.separator") + search.getFilename() + ".nzb");
                            PiecesModel pieces = new PiecesModel(parser.getFiles());
                            partsWindow.setPieces(pieces);
                            partsWindow.setReady();
                        } catch (XMLStreamException ex) {
                            getLogger().log(Level.SEVERE, null, ex);
                        } catch (ParseException ex) {
                            getLogger().log(Level.SEVERE, null, ex);
                        }
                    } catch (IOException ex) {
                        getLogger().log(Level.SEVERE, null, ex);
                    }
                }
            };
            downloadExecutor.submit(getDownloadFiles);

            Runnable deleteNZB1 = new Runnable() {
                @Override
                public void run() {
                    File file = new File(getDownDir()
                            + System.getProperty("file.separator") + search.getFilename() + ".nzb");
                    file.delete();
                }
            };
        }

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        String path = "\"" + prefs.get("downpath").toString() + "\"";
        try {
            Runtime.getRuntime().exec("explorer.exe " + path);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void autoGroupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoGroupActionPerformed
        if (autoGroup.isSelected()) {
            prefs.put("autogroup", true);
        } else {
            prefs.put("autogroup", true);
        }
    }//GEN-LAST:event_autoGroupActionPerformed

    private void titleFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_titleFieldFocusLost
        int start = titleField.getSelectionStart();
        int end = titleField.getSelectionEnd();
        if (!titleField.getText().equals("")) {
            titleField.setText(Utils.capitalizeFirstLetters(titleField.getText()));
            titleField.setSelectionStart(start);
            titleField.setSelectionEnd(end);
        }

    }//GEN-LAST:event_titleFieldFocusLost

    private void artistFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_artistFieldFocusLost
        int start = artistField.getSelectionStart();
        int end = artistField.getSelectionEnd();
        if (!artistField.getText().equals("")) {
            artistField.setText(Utils.capitalizeFirstLetters(artistField.getText()));
            artistField.setSelectionStart(start);
            artistField.setSelectionEnd(end);
        }
    }//GEN-LAST:event_artistFieldFocusLost

    private void titleFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_titleFieldMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            editPopupMenu.show(titleField, evt.getX(), evt.getY());
            if (titleField.getSelectedText() == null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        titleField.requestFocusInWindow();
                        titleField.selectAll();
                    }
                });
            }
        }
    }//GEN-LAST:event_titleFieldMouseClicked

    private void artistFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_artistFieldMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            editPopupMenu.show(artistField, evt.getX(), evt.getY());
            if (artistField.getSelectedText() == null) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        artistField.requestFocusInWindow();
                        artistField.selectAll();
                    }
                });
            }
        }
    }//GEN-LAST:event_artistFieldMouseClicked

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        DatabaseView dv = new DatabaseView();
        dv.setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void errorsLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_errorsLabelMouseClicked
        JOptionPane.showMessageDialog(null, errors);
        errors = "";
        errorsLabel.setVisible(false);
    }//GEN-LAST:event_errorsLabelMouseClicked

    private class AutoDownloadCallable implements Callable {

        public AutoDownloadCallable() {
        }

        @Override
        public Object call() throws Exception {
            ListIterator<Media> medias = songsTableModel.getMedia().listIterator();
            int count = 0;
            resultsTable.setModel(resultsTableModel);
            while (medias.hasNext()) {
                if (shouldStop) {
                    break;
                }
                Media media = medias.next();
                if (!media.isDownloaded()) {
                    try {
                        startSignal = new CountDownLatch(1);
                        ((BinsearchTableModel) resultsTableModel).clear();
                        String url = resolveBinsearchURL(media);
                        currentSearch = new Search(media);
                        parseTask = new BinsearchParse(media, 0, url);
                        searchThread = new Thread(parseTask);
                        searchThread.start();
                        startSignal.await();
                        sortBestResults();

                        BinsearchTableModel model = (BinsearchTableModel) resultsTableModel;
                        if (model.getRowCount() > 0) {
                            media.setDownloaded(true);
                            //Main.getSongsTableModel().updateRow(media);  
                            BinsearchResult res = model.getResultAt(0);
                            Thread mine = new Thread(new Binsearch(res, jProgressBar1));
                            mine.start();
                        }
                        count++;
                        //shouldStop = true;
                    } catch (Exception e) {
                        getLogger().log(Level.SEVERE, null, e);
                    }
                }
            }
            shouldStop = false;
            autoDownloadRunning = false;
            stopButton.setEnabled(false);
            stopProgressBar();
            setStatus("");
            /*
             * if (autoExecutor != null) { autoExecutor.shutdownNow(); autoExecutor = null; }
             *
             */
            return 0;
        }
    }

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        if (!autoDownloadRunning) {
            stopButton.setEnabled(true);
            autoDownloadRunning = true;
            ExecutorService executor1 = Executors.newSingleThreadExecutor();
            AutoDownloadCallable mine = new AutoDownloadCallable();
            //startProgressBar();
            executor1.submit(mine);
            executor1.shutdown();
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        if(autoDownloadRunning){
            stopButton.setEnabled(false);
            shouldStop = true;
            Main.setStatus("Stopping auto download...");
            startProgressBar();
        }
        
    }//GEN-LAST:event_stopButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    oneInstanceSocket = new ServerSocket(
                            SERVER_SOCKET, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));

                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Application already opened!");
                    System.exit(1);
                }
                try {
                    //if(true){
                    if (args[0].equals("-hide")) {
                        main = new Main();
                        main.setVisible(false);
                    } else {
                        main = new Main();
                        main.setVisible(true);
                    }
                } catch (Exception e) {
                    main = new Main();
                    main.setVisible(true);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel albumsPanel;
    private static javax.swing.JTable albumsTable;
    private static javax.swing.JTextField artistField;
    private javax.swing.JCheckBox autoGroup;
    private javax.swing.ButtonGroup buttonGroup1;
    public static javax.swing.JLabel currDlSpeed;
    private static javax.swing.JLabel errorsLabel;
    public static javax.swing.JLabel etaAndTotalText;
    public static javax.swing.JCheckBox exitCheckbox;
    public static javax.swing.JTable filesToDownloadTab;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private static javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    public static javax.swing.JPanel jPanel6;
    private static javax.swing.JPanel jPanel9;
    private static javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private static javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private static javax.swing.JTabbedPane jTabbedPane1;
    private static javax.swing.JTextField maxSizeField;
    private static javax.swing.JTextField minSizeField;
    private javax.swing.JPanel moviesPanel;
    private static javax.swing.JTable moviesTable;
    public static javax.swing.JTable nzbListTab;
    private static javax.swing.JTable resultsTable;
    private static javax.swing.JButton searchButton;
    public static javax.swing.JCheckBox shutdownCheckbox;
    private javax.swing.JPanel songsPanel;
    private static javax.swing.JTable songsTable;
    public static javax.swing.JToggleButton startToggleButton;
    private static javax.swing.JButton stopButton;
    private static javax.swing.JButton stopSearchButton;
    private static javax.swing.JTextField titleField;
    private static javax.swing.JButton toolbarSearchButton;
    private static javax.swing.JComboBox typeCombo;
    private static javax.swing.JButton updateAllButton;
    private static javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        System.err.println("Lost clipboard ownership");
    }
}
