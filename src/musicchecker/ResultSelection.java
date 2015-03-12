/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author Luiso
 */
public class ResultSelection {

    private static ArrayList<PriorityRow> prioritizedList;

    public static ArrayList sortBestSongResults(ArrayList<SearchResult> res, Search search) {
        ArrayList toReturn;
        prioritizedList = new ArrayList<PriorityRow>();
        if (res.get(0) instanceof BinsearchResult) {
            toReturn = new ArrayList<BinsearchResult>();
        } else {
            toReturn = new ArrayList<SearchResult>();
        }
        if (search.getMedia().isMovie()) {
            applyMovieFilters(res);
        } else if (search.getMedia().isTV()) {
            applyTVFilters(res);
        } else if (search.getMedia().isSong()) {
            applySongFilters(res);
        } else if (search.getMedia().isAlbum()) {
            applyAlbumFilters(res);
        }
        Collections.sort(prioritizedList);
        for (PriorityRow pr : prioritizedList) {
            toReturn.add(pr.getResult());
        }
        return toReturn;
    }

    private static void printList() {
        for (PriorityRow res : prioritizedList) {
            System.out.println(res.priority + ":" + res.getResult().getFilename().substring(0, 15) + "; "
                    + res.getResult().getPartsAvailable() + "; "
                    + res.getResult().getSize() + "; " + res.getResult().getPoster());
        }
    }

    private static ArrayList<SearchResult> moveToBottom(ArrayList<SearchResult> results) {
        for (int i = 0; i < results.size(); i++) {
            BinsearchResult res = (BinsearchResult) results.get(i);
            if (res.getPoster().toLowerCase().contains("yenc-pp-a&a")
                    || res.getPoster().toLowerCase().contains("yencbin")) {
                results.set(results.size() - 1, res);
            }
            /*
             * else{ BinsearchResult removed = (BinsearchResult) results.remove(i); results.add(0,
             * removed); }
             *
             */
        }
        return results;
    }

    private static ArrayList<SearchResult> moveToTop(ArrayList<SearchResult> results, int limit) {
        for (int i = 0; i < results.size(); i++) {
            BinsearchResult res = (BinsearchResult) results.get(i);
            if (res.getPoster().toLowerCase().equals("moovee")
                    || res.getPoster().toLowerCase().equals("teevee")) {
                results.set(limit, res);
            }
        }
        return results;
    }

    private static void applyMovieFilters(ArrayList<SearchResult> results) {
        if (results.get(0) instanceof BinsearchResult) {
            for (SearchResult s : results) {
                BinsearchResult res = (BinsearchResult) s;
                double sizeDouble;
                int priority = 0;
                if (res.getSize() != null) {
                    String[] tokens = res.getSize().split(" ");
                    sizeDouble = Double.parseDouble(tokens[0]);
                    if (sizeDouble > 650 && tokens[1].equalsIgnoreCase("MB")) {
                        priority--;
                    }
                    if (sizeDouble <= 1.6 && tokens[1].equalsIgnoreCase("GB")) {
                        priority--;
                    }
                }
                if (res.getFilename().toLowerCase().contains("dvdrip")
                        || res.getFilename().toLowerCase().contains("bdrip")) {
                    priority--;
                }
                if (res.isIsCollection()) {
                    priority--;
                }
                if (res.getPoster().toLowerCase().equals("moovee")) {
                    priority--;
                }
                PriorityRow pr = new PriorityRow(res, priority);
                prioritizedList.add(applyCommonFilter(pr));
            }
        }
    }

    private static PriorityRow applyCommonFilter(PriorityRow pr) {
        BinsearchResult res = pr.getResult();
        if (res.getPartsAvailable() != null) {
            if (res.getPartsAvailable().isComplete()) {
                pr.setPriority(pr.getPriority() - 2);
            } else if (res.getPartsAvailable().isOkay()) {
                pr.setPriority(pr.getPriority() - 1);;
            }
        }
        if (res.getPoster().toLowerCase().contains("yenc-pp-a&a")
                || res.getPoster().toLowerCase().contains("yencbin")) {
            pr.setPriority(1);
        }
        return pr;
    }

    private static void applyTVFilters(ArrayList<SearchResult> results) {
        if (results.get(0) instanceof BinsearchResult) {
            for (SearchResult s : results) {
                BinsearchResult res = (BinsearchResult) s;
                double sizeDouble;
                int priority = 0;
                if (res.getSize() != null) {
                    String[] tokens = res.getSize().split(" ");
                    sizeDouble = Double.parseDouble(tokens[0]);
                    if (sizeDouble > 100 && tokens[1].equalsIgnoreCase("MB")) {
                        priority--;
                    }
                    if (sizeDouble <= 400 && tokens[1].equalsIgnoreCase("MB")) {
                        priority--;
                    }
                }
                if (res.getPoster().toLowerCase().equals("teevee")) {
                    priority--;
                }
                if (res.isIsCollection()) {
                    priority--;
                }
                PriorityRow pr = new PriorityRow(res, priority);
                prioritizedList.add(applyCommonFilter(pr));
            }
        }
    }

    private static ArrayList<SearchResult> applySongFilters(ArrayList<SearchResult> results) {
        ArrayList<SearchResult> toReturn = new ArrayList<SearchResult>();
        if (results.get(0) instanceof BinsearchResult) {
            for (SearchResult s : results) {
                BinsearchResult res = (BinsearchResult) s;
                int priority = 0;
                double sizeDouble;
                if (res.getSize() != null) {
                    String[] tokens = res.getSize().split(" ");
                    sizeDouble = Double.parseDouble(tokens[0]);
                    if (sizeDouble > 3 && tokens[1].equalsIgnoreCase("MB")) {
                        priority--;
                    }
                    if (sizeDouble <= 15 && tokens[1].equalsIgnoreCase("MB")) {
                        priority--;
                    }
                }
                if (res.getFilename().toLowerCase().contains("remix")
                        && !res.getMedia().getTitle().toLowerCase().contains("remix")) {
                    priority++;
                } else {
                    if (res.getFilename().toLowerCase().contains("mix")
                            && !res.getMedia().getTitle().toLowerCase().contains("mix")) {
                        priority++;
                    }
                }
                if (!res.getMedia().extensionMatchesMedia(res.getExtension(), Media.SONG)) {
                    priority++;
                }
                PriorityRow pr = new PriorityRow(res, priority);
                if (res.isIsCollection()) {
                    pr.setPriority(1);
                    prioritizedList.add(pr);
                } else {
                    prioritizedList.add(applyCommonFilter(pr));
                }
            }
        }
        return toReturn;
    }

    private static ArrayList<SearchResult> applyAlbumFilters(ArrayList<SearchResult> results) {
        ArrayList<SearchResult> toReturn = new ArrayList<SearchResult>();
        if (results.get(0) instanceof BinsearchResult) {
            for (SearchResult s : results) {
                BinsearchResult res = (BinsearchResult) s;
                int priority = 0;
                double sizeDouble;
                if (res.getSize() != null) {
                    String[] tokens = res.getSize().split(" ");
                    sizeDouble = Double.parseDouble(tokens[0]);
                    if (sizeDouble > 40 && tokens[1].equalsIgnoreCase("MB")) {
                        priority--;
                    }
                    if (sizeDouble <= 120 && tokens[1].equalsIgnoreCase("MB")) {
                        priority--;
                    }
                }
                PriorityRow pr = new PriorityRow(res, priority);
                prioritizedList.add(applyCommonFilter(pr));
            }
        }
        return toReturn;
    }

    private static class ResultSpeedComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            XWeaselResult r1 = (XWeaselResult) o1;
            XWeaselResult r2 = (XWeaselResult) o2;
            if (toNormalizedSpeed(r2.getSpeed()) > toNormalizedSpeed(r1.getSpeed())) {
                return 1;
            } else if (toNormalizedSpeed(r2.getSpeed()) < toNormalizedSpeed(r1.getSpeed())) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private static int toNormalizedSpeed(String speed) {
        if (speed.contains("MB")) {
            String[] tokens = speed.split(" ");
            double speedNumber = Double.parseDouble(tokens[0].trim());
            return (int) (speedNumber * 1000000);
        }
        if (speed.contains("KB")) {
            String[] tokens = speed.split(" ");
            double speedNumber = Double.parseDouble(tokens[0].trim());
            return (int) (speedNumber * 1000);
        } else {
            return 100 * 1000;
        }
    }

    private static ArrayList<SearchResult> getAppropiateTypes(ArrayList<SearchResult> res, Search search) {
        ArrayList<SearchResult> res2 = new ArrayList<SearchResult>();
        if (search.getMedia().getType() == Media.SONG) {
            for (SearchResult s : res) {
                if (s instanceof BinsearchResult) {
                    if (sizeInRange(s.getSize(), search)) {
                        res2.add(s);
                    }
                } else {
                    if (s.getFilename().endsWith("mp3") && sizeInRange(s.getSize(), search)) {
                        res2.add(s);
                    }
                }
            }
        } else if (search.getMedia().getType() == Media.ALBUM) {
            for (SearchResult s : res) {
                if (s instanceof XWeaselResult) {
                    if (s.getFilename().endsWith("rar") || s.getFilename().endsWith("tar") || s.getFilename().endsWith("zip")) {
                        if (sizeInRange(s.getSize(), search)) {
                            res2.add(s);
                        }
                    }
                } else {
                    if (sizeInRange(s.getSize(), search)) {
                        res2.add(s);
                    }
                }
            }
        } else if (search.getMedia().getType() == Media.MOVIE) {
            for (SearchResult s : res) {
                if (sizeInRange(s.getSize(), search)) {
                    res2.add(s);
                }
            }
        }
        return res2;
    }

    private static boolean sizeInRange(String size, Search search) {
        String[] tokens = size.split(" ");
        double sizeDouble = Double.parseDouble(tokens[0]);
        if (search.getMedia().getType() == Media.SONG) {
            if (sizeDouble > 3 && sizeDouble < 15 && tokens[1].equalsIgnoreCase("MB")) {
                return true;
            }
        } else if (search.getMedia().getType() == Media.ALBUM) {
            if (sizeDouble > 25 && sizeDouble < 175 && tokens[1].equalsIgnoreCase("MB")) {
                return true;
            }
        } else if (search.getMedia().getType() == Media.MOVIE) {
            if (sizeDouble > 650 && tokens[1].equalsIgnoreCase("MB")) {
                return true;
            } else if (sizeDouble <= 1.6 && tokens[1].equalsIgnoreCase("GB")) {
                return true;
            }
        }
        return false;
    }

    static class PriorityRow implements Comparable {

        private BinsearchResult result;
        private int priority;

        PriorityRow(BinsearchResult res) {
            result = res;
        }

        PriorityRow(BinsearchResult res, int p) {
            result = res;
            priority = p;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public BinsearchResult getResult() {
            return result;
        }

        public void setResult(BinsearchResult result) {
            this.result = result;
        }

        @Override
        public int compareTo(Object o) {
            PriorityRow pr = (PriorityRow) o;
            if (this.getPriority() > pr.getPriority()) {
                return 1;
            } else if (this.getPriority() < pr.getPriority()) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
