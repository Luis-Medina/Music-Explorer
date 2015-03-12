/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package musicchecker;

/**
 *
 * @author Luiso
 */
public class BinsearchResult extends SearchResult{

    private String poster;
    private String group;
    private PartsAvailable partsAvailable;
    private boolean isCollection;
    private boolean passwordRequired;
    private String id;
    private int age;
    private String query;
    private Media media;
    private boolean isForeign;

    public BinsearchResult(String filename) {
        super(filename);
    }

    public BinsearchResult(String filename, String size) {
        super(filename);
        setSize(size);
    }

    public BinsearchResult(String id, String filename, String type) {
        super(filename);
        this.id = id;
        setExtension(type);
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isIsCollection() {
        return isCollection;
    }

    public void setIsCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }

    public PartsAvailable getPartsAvailable() {
        return partsAvailable;
    }

    public void setPartsAvailable(String partsAvailable) {
        this.partsAvailable = new PartsAvailable(partsAvailable);
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public boolean isPasswordRequired() {
        return passwordRequired;
    }

    public void setPasswordRequired(boolean requiresPassword) {
        this.passwordRequired = requiresPassword;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString(){
        return id + " - " + getFilename();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public boolean isIsForeign() {
        return isForeign;
    }

    public void setIsForeign(boolean isForeign) {
        this.isForeign = isForeign;
    }    
    
}
