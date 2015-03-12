/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package musicchecker;

/**
 *
 * @author Luiso
 */
public class Search {

    private Media media;
    //private ArrayList<BinsearchResult> results;

    public Search(Media s) {
        this.media = s;
    }

    /*
    public ArrayList<BinsearchResult> getResults() {
        return results;
    }

    public void setResults(ArrayList<BinsearchResult> results) {
        this.results = results;
    }
    
    public void addResults(ArrayList<BinsearchResult> results){
        this.results.addAll(results);
    }
     * 
     */

    public Media getMedia() {
        return media;
    }

    public void setmedia(Media s) {
        this.media = s;
    }
    
    
    
}
