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
 * ****************************************************************************
 */
package at.lame.hellonzb.parser;

import at.lame.hellonzb.*;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.stream.*;
import org.apache.commons.lang.*;
import org.apache.commons.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import musicchecker.Main;
import musicchecker.Media;

/**
 * This class is used to parse a NZB file. Since NZB files are standard XML
 * files, they can be parsed with Java XML stream reader.
 *
 * @author Matthias F. Brandstetter
 */
public class NzbParser {

    /**
     * The name of the nzb file
     */
    private String name;
    /**
     * A list of files specified by the parsed nzb file
     */
    private Vector<DownloadFile> downloadFiles;
    /**
     * The original total file size of this NZB file
     */
    private long origTotalSize;
    /**
     * The amount of bytes already downloaded
     */
    private long downloadedBytes;
    private static Media media;
    private File downloadDir;
    private String[] originalFiles;
    private static String id;

    /**
     * This is the constructor of the class. It parses the given XML file.
     *
     * @param mainApp The main application object
     * @param file The file name of the nzb file to parse
     * @throws XMLStreamException
     * @throws IOException
     */
    public NzbParser(HelloNzbCradle mainApp, String file)
            throws XMLStreamException, IOException, ParseException {

        DownloadFile currentFile = null;
        DownloadFileSegment currentSegment = null;
        boolean groupFlag = false;
        boolean segmentFlag = false;
        boolean idFlag = false;

        this.name = file.trim();
        this.name = file.substring(0, file.length() - 4);
        this.downloadFiles = new Vector<DownloadFile>();

        this.origTotalSize = 0;
        this.downloadedBytes = 0;

        // create XML parser
        String string = reformatInputStream(file);
        InputStream in = new ByteArrayInputStream(string.getBytes());
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = factory.createXMLStreamReader(in);

        // parse nzb file with a Java XML parser
        while (parser.hasNext()) {
            try {
                switch (parser.getEventType()) {
                    // parser has reached the end of the xml file
                    case XMLStreamConstants.END_DOCUMENT:
                        parser.close();
                        break;

                    // parser has found a new element
                    case XMLStreamConstants.START_ELEMENT:
                        String elemName = parser.getLocalName().toLowerCase();
                        if (elemName.equals("file")) {
                            currentFile = newDownloadFile(parser);
                            boolean found = false;
                            for (DownloadFile dlf : downloadFiles) {
                                if (dlf.getFilename().equals(currentFile.getFilename())) {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                downloadFiles.add(currentFile);
                            }
                        } else if (elemName.equals("group")) {
                            groupFlag = true;
                        } else if (elemName.equals("id")) {
                            idFlag = true;
                        } else if (elemName.equals("segment")) {
                            currentSegment = newDownloadFileSegment(parser, currentFile);
                            currentFile.addSegment(currentSegment);
                            segmentFlag = true;
                        }
                        break;

                    // end of element
                    case XMLStreamConstants.END_ELEMENT:
                        groupFlag = false;
                        segmentFlag = false;
                        break;

                    // get the elements value(s)
                    case XMLStreamConstants.CHARACTERS:
                        if (!parser.isWhiteSpace()) {
                            if (groupFlag && (currentFile != null)) {
                                currentFile.addGroup(parser.getText());
                            } else if (segmentFlag && (currentSegment != null)) {
                                currentSegment.setArticleId(parser.getText());
                            } else if (idFlag) {
                                id = parser.getText();
                            }
                        }
                        break;
                    case XMLStreamConstants.COMMENT:
                        if (parser.getText().startsWith(" Title: ")) {
                            String[] tokens = parser.getText().split("Title: ");
                            media = new Media(tokens[1].trim());
                        } else if (parser.getText().startsWith(" Artist: ")) {
                            String[] tokens = parser.getText().split("Artist: ");
                            media.setArtist(tokens[1].trim());
                        } else if (parser.getText().startsWith(" Type: ")) {
                            String[] tokens = parser.getText().split("Type: ");
                            media.setType(Integer.parseInt(tokens[1].trim()));
                        }
                    // any other parser event?
                    default:
                        break;
                }
                parser.next();
            } catch (XMLStreamException e) {
                Main.getLogger().log(Level.SEVERE, null, e);
            }
        }

        checkFileSegments();
        this.origTotalSize = getCurrTotalSize();
        originalFiles = new String[downloadFiles.size()];
        for (int i = 0; i < originalFiles.length; i++) {
            originalFiles[i] = downloadFiles.get(i).getFilename();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getOriginalFiles() {
        return originalFiles;
    }

    public void setOriginalFiles(String[] originalFiles) {
        this.originalFiles = originalFiles;
    }

    public File getDownloadDir() {
        return downloadDir;
    }

    public void setDownloadDir(File downloadDir) {
        this.downloadDir = downloadDir;
    }

    /**
     * Check all download files within this parser for consecutive index numbers
     * without any gaps. Throw exception if a gap was found.
     */
    private void checkFileSegments() //throws ParseException
    {
        for (DownloadFile df : this.downloadFiles) {
            Vector<DownloadFileSegment> segs = df.getAllOriginalSegments();
            for (int currIdx = 0; currIdx < segs.size(); currIdx++) {
                if ((segs.get(currIdx) == null) || (segs.get(currIdx).getIndex() != (currIdx + 1))) {
//					throw new ParseException("XML error: missing segment " + (currIdx + 1) + " in file '" + 
//							df.getFilename() + "'", currIdx);

                    // create new dummy segment
                    DownloadFileSegment dummySeg =
                            new DownloadFileSegment(df, 1, currIdx + 1, df.getGroups());
                    dummySeg.setArticleId("dummy");
                    df.addSegment(dummySeg);
                }
            }
        }
    }

    /**
     * This method is called to save the currently loaded parser data to a file
     * (to be loaded later on).
     *
     * @param logger The central logger object
     * @param counter File(name) counter
     * @param filename File name to use
     * @param dlFiles The vector of DownloadFile to write
     * @return Success status (true or false)
     */
    public static boolean saveParserData(
            int counter, String filename, Vector<DownloadFile> dlFiles) {
        String newline = System.getProperty("line.separator");

        if (dlFiles.size() < 1) {
            return true;
        }

        try {
            String datadirPath = Main.getAppDir() + "Data";

            // create home directory
            File datadir = new File(datadirPath);
            if (datadir.exists()) {
                if (datadir.isFile()) {
                    Main.getLogger().log(Level.SEVERE, "Can''t create data directory: {0}", datadirPath);
                    return false;
                }
            } else {
                datadir.mkdirs();
            }

            //File file = new File(datadirPath, counter + "-" + filename + ".nzb");
            File file = new File(datadirPath, filename + ".nzb");
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            // XML header
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
            writer.write(newline);

            // XML doctype
            writer.write("<!DOCTYPE nzb PUBLIC \"-//newzBin//DTD NZB 1.0//EN\" "
                    + "\"http://www.newzbin.com/DTD/nzb/nzb-1.0.dtd\">");
            writer.write(newline);

            // HelloNzb signature line
            writer.write("<!-- NZB generated by Music Checker -->");
            writer.write(newline);

            writer.write("<!-- Title: " + media.getTitle() + " -->");
            writer.write(newline);

            writer.write("<!-- Artist: " + media.getArtist() + " -->");
            writer.write(newline);

            writer.write("<!-- Type: " + String.valueOf(media.getType()) + " -->");
            writer.write(newline);

            // XML namespace
            writer.write("<nzb xmlns=\"http://www.newzbin.com/DTD/2003/nzb\">");
            writer.write(newline);

            writer.write("<id>" + id + "</id>");
            writer.write(newline);
            writer.write(newline);

            // now write all files passed to this method
            for (DownloadFile dlFile : dlFiles) {
                writeDlFileToXml(writer, dlFile);
            }

            // end <nzb> element
            writer.write(newline);
            writer.write("</nzb>");

            // flush and close file
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    /**
     * This static method is used to pre-format the contents of the given file.
     * It is used to avoid ignorable fatal XML parsing errors.
     *
     * @param filename The name of the XML file to parse
     * @return The re-formated string
     * @throws IOException
     */
    private static String reformatInputStream(String filename) throws IOException {
        File file = new File(filename);
        ArrayList<String> lines = (ArrayList<String>) FileUtils.readLines(file);
        for (int i = 0; i < 20; i++) {
            if (lines.get(i).toUpperCase().startsWith("<!DOCTYPE")) {
                lines.remove(i);
                break;
            }
        }
        String formatted = "";
        for (String s : lines) {
            formatted += s + "\n";
        }
        // avoid XML errors with '>' and '<' signs in "poster" attribute
        // of the XML "<nzb poster=..." tag       
        return formatted.replaceAll("\\s+poster=\".*?\"", " poster=\"dummy\"");
    }

    /**
     * This method writes out the content (segments) of a DownloadFile object to
     * the given OutputStreamWriter object.
     *
     * @param writer The stream writer object to use
     * @param dlFile The download file to use
     * @throws IOException
     */
    private static void writeDlFileToXml(OutputStreamWriter writer, DownloadFile dlFile)
            throws IOException {
        String newline = System.getProperty("line.separator");
        String poster = StringEscapeUtils.escapeHtml(dlFile.getPoster());
        String date = StringEscapeUtils.escapeHtml(dlFile.getCreationDate());
        String subject = StringEscapeUtils.escapeHtml(dlFile.getSubject());

        // <file ...> element
        writer.write("<file poster=\"" + poster + "\" ");
        writer.write("date=\"" + date + "\" ");
        writer.write("subject=\"" + subject + "\">");
        writer.write(newline);

        // <group> elements
        writer.write("<groups>");
        writer.write(newline);
        for (String group : dlFile.getGroups()) {
            writer.write("<group>" + group + "</group>");
            writer.write(newline);
        }
        writer.write("</groups>");
        writer.write(newline);

        // <segment> elements
        writer.write("<segments>");
        writer.write(newline);
        for (DownloadFileSegment seg : dlFile.getAllOriginalSegments()) {
            if (seg == null) {
                continue;
            }

            String aID = StringEscapeUtils.escapeXml(seg.getArticleId());

            writer.write("<segment bytes=\"" + seg.getSize() + "\" "
                    + "number=\"" + seg.getIndex() + "\">"
                    + aID + "</segment>");
            writer.write(newline);
        }
        writer.write("</segments>");
        writer.write(newline);

        // end <file> element
        writer.write("</file>");
        writer.write(newline);
    }

    /**
     * This private method is used to create a new instance of DownloadFile.
     *
     * @param parser The parser object to query (XMLStreamReader)
     * @return The new DownloadFile object
     */
    private DownloadFile newDownloadFile(XMLStreamReader parser) {
        String poster = "";
        String date = "";
        String subject = "";

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attName = parser.getAttributeLocalName(i).toLowerCase();
            if (attName.equals("poster")) {
                poster = parser.getAttributeValue(i);
            } else if (attName.equals("date")) {
                date = parser.getAttributeValue(i);
            } else if (attName.equals("subject")) {
                subject = parser.getAttributeValue(i);
            }
        }

        return new DownloadFile(poster, date, subject, HelloNzbToolkit.getLastFilename(name));
    }

    /**
     * This private method is used to create a new instance of
     * DownloadFileSegment.
     *
     * @param parser The parser object to query (XMLStreamReader)
     * @param file The current DownloadFile object
     * @return The new DownloadFileSegment object
     */
    private DownloadFileSegment newDownloadFileSegment(XMLStreamReader parser, DownloadFile file) {
        long bytes = 0;
        int index = 0;

        for (int i = 0; i < parser.getAttributeCount(); i++) {
            String attName = parser.getAttributeLocalName(i).toLowerCase();
            if (attName.equals("bytes")) {
                bytes = Long.parseLong(parser.getAttributeValue(i));
            } else if (attName.equals("number")) {
                index = Integer.parseInt(parser.getAttributeValue(i));
            }
        }

        return new DownloadFileSegment(file, bytes, index, file.getGroups());
    }

    /**
     * This method returns the name of this file.
     *
     * @return The name of this file
     */
    public String getName() {
        return name;
    }

    /**
     * This method returns a vector of all files to download.
     *
     * @return A Vector<DownloadFile> object of the files
     */
    public Vector<DownloadFile> getFiles() {
        return downloadFiles;
    }

    /**
     * This method returns the total size of all segments currently in this nzb
     * file.
     *
     * @return The total size
     */
    public long getCurrTotalSize() {
        long size = 0;

        for (int i = 0; i < downloadFiles.size(); i++) {
            size += downloadFiles.get(i).getTotalFileSize();
        }

        return size;
    }

    /**
     * This method returns the total size of all segments originally in this nzb
     * file.
     *
     * @return The total size
     */
    public long getOrigTotalSize() {
        return origTotalSize;
    }

    /**
     * Remove the DownloadFile object at the given vector's index.
     *
     * @param index The download file to remove is identified by this index
     * value
     * @throws ArrayIndexOutOfBoundsException
     */
    public void removeFileAt(int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index > (downloadFiles.size() - 1)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        downloadFiles.remove(index);
    }

    /**
     * Set the value of the attribute "downloadedBytes".
     *
     * @param bytes The new value to set
     */
    public void setDownloadedBytes(long bytes) {
        downloadedBytes = bytes;
    }

    /**
     * Get the current value of the attribute "downloadedBytes".
     *
     * @return The value currently set
     */
    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }
}
