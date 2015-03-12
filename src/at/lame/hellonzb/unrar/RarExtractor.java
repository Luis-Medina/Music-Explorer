/**
 * *****************************************************************************
 * HelloNzb -- The Binary Usenet Tool Copyright (C) 2010-2011 Matthias F.
 * Brandstetter https://sourceforge.net/projects/hellonzb/
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
package at.lame.hellonzb.unrar;

import java.io.*;
import java.util.Vector;
import at.lame.hellonzb.*;
import at.lame.hellonzb.parser.*;
import at.lame.hellonzb.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import musicchecker.Main;

/**
 * This class is used as background thread to extract a RAR archive.
 *
 * @author Matthias F. Brandstetter
 */
public class RarExtractor implements Runnable {

    /**
     * main application object
     */
    private HelloNzb mainApp;
    /**
     * parser object to process
     */
    private NzbParser parser;
    /**
     * download directory from application preferences
     */
    private File dlDir;

    public RarExtractor(HelloNzb mainApp, NzbParser parser) throws RuntimeException {
        this.mainApp = mainApp;
        this.parser = parser;

        // download directory
        String nzbFilename = parser.getName();
        nzbFilename = HelloNzbToolkit.getLastFilename(nzbFilename);
        dlDir = new File(parser.getDownloadDir().toString());
        if (!dlDir.isDirectory()) {
            throw new RuntimeException("Invalid directory for RAR extraction");
        }
    }

    public void run() {
        String rarFilename = "";

        // search all files within destination directory
        String filenames[] = dlDir.list();
        if (filenames == null) {
            return;
        } else {
            java.util.Arrays.sort(filenames);
        }

        for (String filename : filenames) {
            if (filename.toLowerCase().endsWith(".rar")) {
                rarFilename = filename;
                break;
            }
        }

        // valid par2 filename found?
        try {
            if (!rarFilename.isEmpty()) {
                // create target extraction directory
                Main.getLogger().log(Level.INFO, "Start to extract RAR archive to {0}", dlDir);

                // use the following lines for the unrar lib (seems to be broken)
                //dlDir = new File(dlDir + File.separator + UNRAR_DIR);
                //File rarFile = new File(dlDir + File.separator + rarFilename);
                //Unrar.extractArchive(logger, rarFile, dlDir);

                // use the following line for the Unrar command line utility
                callUnrarCmdLine(rarFilename, dlDir);
            } else {
                Main.getLogger().log(Level.INFO, "No .rar file for extraction found");
            }
        } catch (Exception ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        } finally {
            mainApp.rarExtractDone(parser);
        }
    }

    private void callUnrarCmdLine(String rarFilename, File destination)
            throws IOException, InterruptedException {
        Vector<String> cmd = new Vector<String>();

        // yes, do the unrar extract!
        String execLocation = Main.getAppDir() + File.separator + "External\\unrar.exe";

        // create command to execute
        cmd.add(execLocation);
        cmd.add("e");
        cmd.add("-p-");
        cmd.add("-y");
        cmd.add("-r");
        cmd.add("-idp");
        cmd.add("-kb");
        cmd.add(rarFilename);

        Main.getLogger().log(Level.INFO, "Starting Unrar extraction: {0}", cmd);

        // execute command
        String[] cmdArray = cmd.toArray(new String[]{});
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmdArray, null, destination);
        StreamGobbler errGobbler = new StreamGobbler(
                proc.getErrorStream(), "ERR");
        StreamGobbler outGobbler = new StreamGobbler(
                proc.getInputStream(), "OUT");

        // fetch command's STDOUT and STDERR
        errGobbler.start();
        outGobbler.start();

        // wait until program has finished
        int exitVal = proc.waitFor();
        Main.getLogger().log(Level.INFO, "Unrar command exit value: {0}", exitVal);
        if (exitVal == 0) {
            for (String filename : parser.getOriginalFiles()) {
                if (filename.toLowerCase().endsWith(".rar") || filename.toLowerCase().endsWith(".par2")
                        || filename.toLowerCase().endsWith(filename)) {
                    File fileToDelete = new File(dlDir + File.separator + filename);
                    if (fileToDelete.delete()) {
                        Main.getLogger().log(Level.INFO, "File {0} deleted", filename);
                    } else {
                        Main.getLogger().log(Level.INFO, "Unable to delete {0}", filename);
                    }
                } else {
                    Pattern pattern = Pattern.compile(".*?\\.r\\d+$");
                    Matcher matcher = pattern.matcher(filename.toLowerCase());
                    if (matcher.find()) {
                        File fileToDelete = new File(dlDir + File.separator + filename);
                        if (fileToDelete.delete()) {
                            Main.getLogger().log(Level.INFO, "File {0} deleted", filename);
                        } else {
                            Main.getLogger().log(Level.INFO, "Unable to delete {0}", filename);
                        }
                    }
                }
            }
        }
    }
}
