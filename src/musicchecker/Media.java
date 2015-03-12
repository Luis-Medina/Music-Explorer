/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.io.Serializable;

/**
 *
 * @author Luiso
 */
public class Media implements Serializable, Comparable<Media> {

    private String artist;
    private String title;
    private boolean downloaded;
    private int rank;
    private int type;
    public static final int SONG = 1;
    public static final int MOVIE = 2;
    public static final int TVSHOW = 3;
    public static final int ALBUM = 4;
    public static final int GAME = 5;
    public static final int OTHER = 6;
    private static String[] types = {"SONG", "MOVIE", "TV SHOW", "ALBUM", "GAME", "OTHER"};
    public static String[] songTypes = new String[]{".mp3", ".mp4", "m4a.", ".fla", ".wma"};
    public static String[] movieTypes = new String[]{".avi", ".wmv", ".rar", ".zip", ".tar"};
    public static String[] albumTypes = new String[]{".rar", ".zip", ".tar"};
    private String extension;
    private String weekReleased;
    private String billboardDate;
    private String billboardNote;
    private String billboardAlbum;
    private String billboardChart;
    private int weeksOnChart;
    public static final int minSongSize = 0;
    public static final int maxSongSize = 15;
    public static final int minAlbumSize = 15;
    public static final int maxAlbumSize = 200;
    public static final int minMovieSize = 100;
    public static final int maxMovieSize = 4500;
    public static final int minTVSize = 50;
    public static final int maxTVSize = 1000;
    public static final int minOtherSize = 0;
    public static final int maxOtherSize = 50000;
    public static final int minGameSize = 500;
    public static final int maxGameSize = 20000;

    public Media(String title) {
        this.title = title.trim();
    }

    public Media(String artist, String title) {
        this.artist = artist.trim();
        this.title = title.trim();
    }

    public Media(String artist, String title, boolean downloaded) {
        this.artist = artist.trim();
        this.title = title.trim();
        this.downloaded = downloaded;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setType(String type) {
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(type)) {
                this.type = i + 1;
                return;
            }
        }
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist.trim();
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        if (isAlbum() || isSong()) {
            if (title.equals("")) {
                return artist;
            }
            if (artist.equals("")) {
                return title;
            }
            return artist + " - " + title;
        }
        return title;
    }

    public boolean equals(Media s) {
        if (compareTo(s) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Media s) {
        String toCompare1 = s.toString();
        String toCompare2 = this.toString();
        if (toCompare2.toLowerCase().trim().compareTo(toCompare1.toLowerCase().trim()) > 0) {
            return 1;
        } else if (toCompare2.toLowerCase().trim().compareTo(toCompare1.toLowerCase().trim()) < 0) {
            return -1;
        } else {
            return 0;
        }
    }

    public boolean isSong() {
        return type == SONG;
    }

    public boolean isMovie() {
        return type == MOVIE;
    }

    public boolean isTV() {
        return type == TVSHOW;
    }

    public boolean isAlbum() {
        return type == ALBUM;
    }

    public boolean extensionMatchesMedia(String ext, int mediaType) {
        if (mediaType == SONG) {
            for (String s : songTypes) {
                if (s.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
        } else if (mediaType == MOVIE) {
            for (String s : movieTypes) {
                if (s.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
        } else if (mediaType == TVSHOW) {
            for (String s : movieTypes) {
                if (s.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
        } else if (mediaType == ALBUM) {
            for (String s : albumTypes) {
                if (s.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getWeekReleased() {
        return weekReleased;
    }

    public void setWeekReleased(String weekReleased) {
        this.weekReleased = weekReleased;
    }

    public String getBillboardDate() {
        return billboardDate;
    }

    public void setBillboardDate(String billboardDate) {
        this.billboardDate = billboardDate;
    }

    public String getBillboardNote() {
        return billboardNote;
    }

    public void setBillboardNote(String billboardNote) {
        this.billboardNote = billboardNote;
    }

    public String getBillboardAlbum() {
        return billboardAlbum;
    }

    public void setBillboardAlbum(String billboardAlbum) {
        this.billboardAlbum = billboardAlbum;
    }

    public boolean isGame() {
        return type == GAME;
    }

    public static String[] getTypes() {
        return types;
    }

    public String getBillboardChart() {
        return billboardChart;
    }

    public void setBillboardChart(String billboardChart) {
        this.billboardChart = billboardChart;
    }

    public int getWeeksOnChart() {
        return weeksOnChart;
    }

    public void setWeeksOnChart(int weeksOnChart) {
        this.weeksOnChart = weeksOnChart;
    }
    
}
