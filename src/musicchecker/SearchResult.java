/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

/**
 *
 * @author Luiso
 */
public class SearchResult {

    private String filename;
    private String size;
    private String extension;

    public SearchResult() {
    }

    public SearchResult(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String type) {
        this.extension = type;
    }
}
