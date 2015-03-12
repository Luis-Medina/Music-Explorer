/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Luiso
 */
public class JDBC {

    private final static String[] list = new String[]{"closetotray", "autocheck", "shutdownafterfinish",
        "closeafterfinish", "hot100", "latin", "pop", "rock", "hiphop", "danceclub", "country", "lamega",
        "tocadeto", "fidelity", "reggaeton94", "estereotempo", "kq105", "deletenzb", "par2check", "unrar",
        "autogroup", "usenetssl", "ignoreerrors"};

    private static boolean isBooleanOption(String s) {
        for (String str : list) {
            if (str.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static QueryResult doQuery(String query) {
        PreparedStatement ps = null;
        QueryResult qr = null;
        try {
            ps = Main.getDBConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();
            qr = new QueryResult(rsm.getColumnCount());
            for (int i = 1; i <= rsm.getColumnCount(); i++) {
                qr.setColumn(rsm.getColumnName(i), i - 1);
            }
            while (rs.next()) {
                ArrayList tempList = new ArrayList();
                for (int i = 1; i <= rsm.getColumnCount(); i++) {
                    tempList.add(rs.getObject(i));
                }
                qr.addResultRow(tempList);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    Main.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        }
        return qr;
    }

    public static ArrayList<String> getTables() {
        ArrayList<String> toReturn = new ArrayList<String>();
        try {
            DatabaseMetaData md = Main.getDBConnection().getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                if (!rs.getString(3).startsWith("SYS")) {
                    toReturn.add(rs.getString(3));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(JDBC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return toReturn;
    }

    public static java.util.Date getLastWeekChecked() {
        PreparedStatement ps = null;
        Date date = null;
        try {
            ps = Main.getDBConnection().prepareStatement("SELECT LastWeekChecked FROM APP.SETTINGS");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                date = rs.getDate("LastWeekChecked");
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    Main.getLogger().log(Level.SEVERE, null, ex);
                }
            }
        }
        return date;
    }

    public static void setLastWeekChecked(java.util.Date date) {
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("UPDATE APP.SETTINGS "
                    + " SET LastWeekChecked = ? ");
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            ps.setDate(1, sqlDate);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
    }

    public static void setIgnored(Media s, boolean allFromArtist) {
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("INSERT INTO APP.IGNORED "
                    + " (ARTIST, TITLE, TYPE, ALLFROMARTIST) VALUES (?, ?, ?, ?) ");
            if (allFromArtist) {
                ps.setString(2, "");
                ps.setInt(4, 1);
            } else {
                ps.setString(2, s.getTitle());
                ps.setInt(4, 0);
            }
            ps.setString(1, s.getArtist());
            ps.setInt(3, s.getType());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
    }

    public static ArrayList<Media> checkIgnored(ArrayList<Media> list) {
        ArrayList<Media> toReturn = new ArrayList<Media>();
        for (Media media : list) {
            if (!checkIgnored(media)) {
                toReturn.add(media);
            }
        }
        return toReturn;
    }

    //Returns true if media is in ignore table
    public static boolean checkIgnored(Media media) {
        boolean ignored = false;
        ResultSet rs = null;
        PreparedStatement ps;
        try {
            ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.IGNORED "
                    + "WHERE LOWER(ARTIST) = ? AND ALLFROMARTIST = 1", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ps.setString(1, media.getArtist().toLowerCase());
            rs = ps.executeQuery();
            //FOUND AT LEAST ONE ROW
            if (rs.first()) {
                ignored = true;
            } else {
                ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.IGNORED "
                        + "WHERE LOWER(ARTIST) = ? AND LOWER(TITLE) = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ps.setString(1, media.getArtist().toLowerCase());
                ps.setString(2, media.getTitle().toLowerCase());
                rs = ps.executeQuery();

                //FOUND AT LEAST ONE ROW
                if (rs.first()) {
                    ignored = true;
                }
            }
            rs.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Main.getLogger().log(Level.SEVERE, null, ex);
                }
            }
            return ignored;
        }
    }

    public static boolean checkIfNextWeek(Media media) {
        boolean exists = false;
        ResultSet rs = null;
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.SEARCHQUEUE where type = 2 and weekreleased = 'Next Week'");
            rs = ps.executeQuery();
            while (rs.next()) {
                String title = rs.getString("TITLE");
                if (title.equalsIgnoreCase(media.getTitle())) {
                    return true;
                }
            }
            rs.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Main.getLogger().log(Level.SEVERE, null, ex);
                }
            }
            return exists;
        }
    }

    public static void setDownloaded(Media s) {
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("INSERT INTO APP.DOWNLOADED "
                    + " (ARTIST, TITLE, DOWNTYPE) VALUES (?, ?, ?) ");
            ps.setString(1, s.getArtist());
            ps.setString(2, s.getTitle());
            ps.setInt(3, s.getType());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
    }

    public static ArrayList<Media> checkDownloaded(ArrayList<Media> list) {
        Collections.sort(list);
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.DOWNLOADED");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String artist = rs.getString("ARTIST");
                String title = rs.getString("TITLE");
                Media s = new Media(artist, title);
                //System.out.println("Song in db is: " + s);
                int result = Collections.binarySearch(list, s);
                //System.out.println(result);
                if (result >= 0) {
                    list.remove(result);
                    //System.out.println("List size is: " + list.size());
                }
            }
            rs.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
        return list;
    }

    public static boolean checkDownloaded(Media d) {
        boolean found = false;
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.DOWNLOADED");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String artist = rs.getString("ARTIST");
                String title = rs.getString("TITLE");
                Media s = new Media(artist, title);
                //System.out.println("Song in db is: " + s);
                if (s.equals(d)) {
                    found = true;
                    rs.close();
                    break;
                }
            }
            if (!found) {
                rs.close();
            }
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
        return found;
    }

    public static void setIRCOptions(String user, String nick, String email, String alt, String pass) {
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("UPDATE APP.SETTINGS "
                    + " SET IRCUSER = ?, "
                    + "     IRCNICK = ?, "
                    + "     IRCEMAIL = ?, "
                    + "     IRCALT = ?, "
                    + "     IRCPASS = ? ");
            ps.setString(1, user);
            ps.setString(2, nick);
            ps.setString(3, email);
            ps.setString(4, alt);
            ps.setString(5, pass);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
    }

    public static void setUsenetOptions(String host, String user, String pass, String port) {
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("UPDATE APP.SETTINGS "
                    + " SET USENETHOST = ?, "
                    + "     USENETUSER = ?, "
                    + "     USENETPASS = ?, "
                    + "     USENETPORT = ?, ");
            ps.setString(1, host);
            ps.setString(2, user);
            ps.setString(3, pass);
            ps.setString(4, port);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
    }

    public static void getProgramOptions() {
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.SETTINGS");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (isBooleanOption(rs.getMetaData().getColumnName(i).toLowerCase())) {
                        Main.prefs.put(rs.getMetaData().getColumnName(i).toLowerCase(), Utils.intToBool(rs.getObject(i)));
                    } else {
                        Main.prefs.put(rs.getMetaData().getColumnName(i).toLowerCase(), rs.getObject(i));
                    }
                }
            }
            rs.close();
            rs = null;
            ps = null;
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);;
        }
    }

    public static void setProgramOptions() {
        Iterator it = Main.prefs.keySet().iterator();
        if (it != null) {
            try {
                //PreparedStatement ps1 = Main.getDBConnection().prepareStatement("UPDATE APP.SETTINGS "
                //        + " SET AUTOCHECK = ?, "
                //        + "     CLOSETOTRAY = ?");
                //ps.setBoolean(1, autoCheck);
                //ps.setBoolean(2, closeToTray);
                //ps.set
                String sql = "UPDATE APP.SETTINGS SET ";
                ArrayList valueList = new ArrayList();
                if (it.hasNext()) {
                    String key = it.next().toString();
                    sql += key + " = ?";
                    valueList.add(Main.prefs.get(key));
                } else {
                    it = null;
                    return;
                }
                while (it.hasNext()) {
                    String key = it.next().toString();
                    sql += " , " + key + " = ?";
                    valueList.add(Main.prefs.get(key));
                }
                System.out.println(sql);
                PreparedStatement ps = Main.getDBConnection().prepareStatement(sql);
                for (int i = 0; i < valueList.size(); i++) {
                    ps.setObject(i + 1, valueList.get(i));
                }
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
            } catch (NullPointerException e) {
                Main.getLogger().log(Level.SEVERE, null, e);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Unable to get preferences.");
        }


    }

    public static ArrayList<String> getIRCOptions() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.SETTINGS");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("IRCUSER"));
                list.add(rs.getString("IRCNICK"));
                list.add(rs.getString("IRCEMAIL"));
                list.add(rs.getString("IRCALT"));
                list.add(rs.getString("IRCPASS"));
            }
            rs.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
        return list;
    }

    public static ArrayList<String> getAllOptions() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.SETTINGS");
            ResultSet rs = ps.executeQuery();
            ResultSetMetaData rsm = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= rsm.getColumnCount(); i++) {
                    list.add(rs.getString(i));
                }
            }
            rs.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
        return list;
    }

    public static void setDownloadDir(String path) {
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("UPDATE APP.SETTINGS "
                    + " SET DOWNPATH = ? ");
            ps.setString(1, path);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
    }

    public static ArrayList<String> getUsenetOptions() {
        ArrayList<String> list = new ArrayList<String>();
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.SETTINGS");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("USENETHOST"));
                list.add(rs.getString("USENETUSER"));
                list.add(rs.getString("USENETPASS"));
                list.add(rs.getString("USENETPORT"));
            }
            rs.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
        return list;
    }

    public static void addToSearchQueue(Media media) {
        PreparedStatement ps;
        try {
            ps = Main.getDBConnection().prepareStatement("INSERT INTO APP.SEARCHQUEUE "
                    + " (ARTIST, TITLE, DOWNLOADED, RANK, TYPE, WEEKRELEASED, "
                    + "  DATE, NOTE, ALBUM, CHART, WEEKS) VALUES (?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?) ");
            ps.setString(1, media.getArtist());
            ps.setString(2, media.getTitle());
            int downloaded = 0;
            if (media.isDownloaded()) {
                downloaded = 1;
            }
            ps.setInt(3, downloaded);
            ps.setInt(4, media.getRank());
            ps.setInt(5, media.getType());
            ps.setString(6, media.getWeekReleased());
            ps.setString(7, media.getBillboardDate());
            ps.setString(8, media.getBillboardNote());
            ps.setString(9, media.getBillboardAlbum());
            ps.setString(10, media.getBillboardChart());
            ps.setInt(11, media.getWeeksOnChart());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }

    }

    public static void addListToSearchQueue(ArrayList<Media> list) {
        PreparedStatement ps;
        for (Media s : list) {
            try {
                ps = Main.getDBConnection().prepareStatement("INSERT INTO APP.SEARCHQUEUE "
                        + " (ARTIST, TITLE, DOWNLOADED, RANK, TYPE, WEEKRELEASED, "
                        + "  DATE, NOTE, ALBUM, CHART, WEEKS) VALUES (?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?) ");
                ps.setString(1, s.getArtist());
                ps.setString(2, s.getTitle());
                int downloaded = 0;
                if (s.isDownloaded()) {
                    downloaded = 1;
                }
                ps.setInt(3, downloaded);
                ps.setInt(4, s.getRank());
                ps.setInt(5, s.getType());
                ps.setString(6, s.getWeekReleased());
                ps.setString(7, s.getBillboardDate());
                ps.setString(8, s.getBillboardNote());
                ps.setString(9, s.getBillboardAlbum());
                ps.setString(10, s.getBillboardChart());
                ps.setInt(11, s.getWeeksOnChart());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
            } catch (NullPointerException e) {
                Main.getLogger().log(Level.SEVERE, null, e);
            }
        }
    }

    public static void setSearchQueue(ArrayList<Media> list) {
        PreparedStatement ps;
        try {
            ps = Main.getDBConnection().prepareStatement("DELETE FROM APP.SEARCHQUEUE");
            ps.executeUpdate();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
        for (Media s : list) {
            try {
                ps = Main.getDBConnection().prepareStatement("INSERT INTO APP.SEARCHQUEUE "
                        + " (ARTIST, TITLE, DOWNLOADED, RANK, TYPE, WEEKRELEASED, "
                        + "  DATE, NOTE, ALBUM, CHART, WEEKS) VALUES (?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?) ");
                ps.setString(1, s.getArtist());
                ps.setString(2, s.getTitle());
                int downloaded = 0;
                if (s.isDownloaded()) {
                    downloaded = 1;
                }
                ps.setInt(3, downloaded);
                ps.setInt(4, s.getRank());
                ps.setInt(5, s.getType());
                ps.setString(6, s.getWeekReleased());
                ps.setString(7, s.getBillboardDate());
                ps.setString(8, s.getBillboardNote());
                ps.setString(9, s.getBillboardAlbum());
                ps.setString(10, s.getBillboardChart());
                ps.setInt(11, s.getWeeksOnChart());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
            } catch (NullPointerException e) {
                Main.getLogger().log(Level.SEVERE, null, e);
            }
        }
    }

    public static ArrayList<Media> loadSearchQueue() {
        ArrayList<Media> list = new ArrayList<Media>();
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("SELECT * FROM APP.SEARCHQUEUE");
            ResultSet rs = ps.executeQuery();
            Media m;
            while (rs.next()) {
                String title = rs.getString("TITLE");
                m = new Media(title);
                m.setArtist(rs.getString("ARTIST"));
                boolean downloaded = false;
                if (rs.getInt("DOWNLOADED") == 1) {
                    downloaded = true;
                }
                m.setDownloaded(downloaded);
                m.setRank(rs.getInt("RANK"));
                m.setType(rs.getInt("TYPE"));

                m.setWeekReleased(rs.getString("WEEKRELEASED"));
                m.setWeeksOnChart(rs.getInt("WEEKS"));
                m.setBillboardDate(rs.getString("DATE"));
                m.setBillboardAlbum(rs.getString("ALBUM"));
                m.setBillboardNote(rs.getString("NOTE"));
                m.setBillboardChart(rs.getString("CHART"));
                list.add(m);
            }
            rs.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
        return list;
    }

    public static ArrayList<Media> getNextWeekDVDs() {
        ArrayList<Media> toReturn = new ArrayList<Media>();
        try {
            PreparedStatement ps = Main.getDBConnection().prepareStatement("SELECT * from APP.SEARCHQUEUE where type = 2 and weekreleased = 'Next Week'");
            ResultSet rs = ps.executeQuery();
            Media m;
            while (rs.next()) {
                String title = rs.getString("TITLE");
                m = new Media(title);
                m.setArtist(rs.getString("ARTIST"));
                boolean downloaded = false;
                if (rs.getInt("DOWNLOADED") == 1) {
                    downloaded = true;
                }
                m.setDownloaded(downloaded);
                m.setRank(rs.getInt("RANK"));
                m.setType(rs.getInt("TYPE"));
                m.setWeekReleased(rs.getString("WEEKRELEASED"));
                m.setBillboardDate(rs.getString("DATE"));
                m.setBillboardAlbum(rs.getString("ALBUM"));
                m.setBillboardNote(rs.getString("NOTE"));
                m.setBillboardChart(rs.getString("CHART"));
                m.setWeeksOnChart(rs.getInt("WEEKS"));
                toReturn.add(m);
            }
            rs.close();
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
        return toReturn;
    }

    public static boolean createDatabase() {
        try {
            Statement s = Main.getDBConnection().createStatement();
            String createTable = "create table DOWNLOADED"
                    + " (ID INTEGER not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + "	ARTIST VARCHAR(80),"
                    + "	TITLE VARCHAR(80),"
                    + "	DOWNTYPE SMALLINT)";
            s.addBatch(createTable);

            createTable = "create table IGNORED"
                    + " (ARTIST VARCHAR(100) not null,"
                    + "	TITLE VARCHAR(100),"
                    + "	TYPE INTEGER,"
                    + "	ALLFROMARTIST SMALLINT default 0 not null)";
            s.addBatch(createTable);

            createTable = "create table SEARCHQUEUE"
                    + " (ARTIST VARCHAR(80),"
                    + "	TITLE VARCHAR(80),"
                    + "	RANK INTEGER default 0 not null,"
                    + "	TYPE INTEGER,"
                    + "	WEEKRELEASED VARCHAR(50),"
                    + "	DATE VARCHAR(25),"
                    + "	NOTE VARCHAR(25),"
                    + "	ALBUM VARCHAR(100),"
                    + "	CHART VARCHAR(50),"
                    + "	DOWNLOADED SMALLINT default 0 not null,"
                    + "	WEEKS INTEGER default 0 not null)";
            s.addBatch(createTable);

            createTable = "create table SETTINGS"
                    + " (IRCUSER VARCHAR(40),"
                    + "	IRCNICK VARCHAR(40),"
                    + "	IRCEMAIL VARCHAR(75),"
                    + "	IRCALT VARCHAR(40) not null,"
                    + "	IRCPASS VARCHAR(40),"
                    + "	DOWNPATH VARCHAR(500),"
                    + "	USENETHOST VARCHAR(100),"
                    + "	USENETUSER VARCHAR(50),"
                    + "	USENETPASS VARCHAR(50),"
                    + "	LASTWEEKCHECKED DATE,"
                    + "	AUTOCHECK SMALLINT default 1 not null,"
                    + "	CLOSETOTRAY SMALLINT default 0 not null,"
                    + "	MAXDOWNLOADS SMALLINT default 20 not null,"
                    + "	SHUTDOWNAFTERFINISH SMALLINT default 0 not null,"
                    + "	CLOSEAFTERFINISH SMALLINT default 0 not null,"
                    + "	DOWNLOADMODE SMALLINT default 0 not null,"
                    + "	HOT100 SMALLINT default 1 not null,"
                    + "	LATIN SMALLINT default 1 not null,"
                    + "	HIPHOP SMALLINT default 1 not null,"
                    + "	COUNTRY SMALLINT default 1 not null,"
                    + "	ROCK SMALLINT default 1 not null,"
                    + "	POP SMALLINT default 1 not null,"
                    + "	DANCECLUB SMALLINT default 1 not null,"
                    + "	LAMEGA SMALLINT default 0 not null,"
                    + "	REGGAETON94 SMALLINT default 0 not null,"
                    + "	ESTEREOTEMPO SMALLINT default 0 not null,"
                    + "	FIDELITY SMALLINT default 0 not null,"
                    + "	KQ105 SMALLINT default 0 not null,"
                    + "	TOCADETO SMALLINT default 0 not null,"
                    + "	DELETENZB SMALLINT default 0 not null,"
                    + "	PAR2CHECK SMALLINT default 1 not null,"
                    + "	UNRAR SMALLINT default 1 not null,"
                    + "	SPEEDLIMIT SMALLINT default 0 not null,"
                    + "	USENETPORT VARCHAR(5),"
                    + "	AUTOGROUP SMALLINT default 1 not null,"
                    + "	MAXCONNECTIONSPEED INTEGER default 0 not null,"
                    + "	USENETSSL SMALLINT default 0 not null,"
                    + "	IGNOREERRORS SMALLINT default 1 not null)";
            s.addBatch(createTable);
            int[] result = s.executeBatch();
            for (int i = 0; i < result.length; i++) {
                System.out.println(result[i]);
            }
            s.close();
            return true;
        } catch (SQLException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } catch (NullPointerException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
        return false;
    }
}
