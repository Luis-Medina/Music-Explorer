/*******************************************************************************
 * HelloNzb -- The Binary Usenet Tool
 * Copyright (C) 2010-2011 Matthias F. Brandstetter
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package at.lame.hellonzb.parser;

import java.io.*;
import javax.swing.JOptionPane;
import javax.xml.stream.XMLStreamException;
import at.lame.hellonzb.*;
import musicchecker.Main;
import java.util.logging.Level;
import java.util.logging.Logger;
import musicchecker.Media;

public class NzbParserCreator extends Thread {

    private HelloNzb mainApp;
    private String filename;
    private NzbParser parser;
    private Media media;
    private String id;

    public NzbParserCreator(String id, HelloNzb mainApp, String filename) {
        this.mainApp = mainApp;
        this.filename = filename;
        this.id = id;
    }

    public NzbParserCreator(String id, HelloNzb mainApp, String filename, Media media) {
        this.mainApp = mainApp;
        this.filename = filename;
        this.media = media;
        this.id = id;
    }

    @Override
    public void run() {
        // set background progress bar
        //mainApp.getTaskManager().loadNzb(true);
        try {
            parser = new NzbParser(mainApp, filename);
            parser.setMedia(media);
            parser.setId(id);
            mainApp.addNzbToQueue(parser);
            /*
            if (exists(parser)) {
                String title = Main.localer.getBundleText("PopupErrorTitle");
                String msg = Main.localer.getBundleText("PopupNzbFilenameAlreadyInQueue");
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                mainApp.addNzbToQueue(parser);
            }
             * 
             */
        } catch (IOException e) {
            String msg = Main.localer.getBundleText("PopupCannotOpenNzb");
            String title = Main.localer.getBundleText("PopupErrorTitle");
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
            Main.getLogger().log(Level.SEVERE, null, e);
        } catch (XMLStreamException e) {
            String msg = Main.localer.getBundleText("PopupXMLParserError");
            String title = Main.localer.getBundleText("PopupErrorTitle");
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
            Main.getLogger().log(Level.SEVERE, null, e);
        } catch (java.text.ParseException e) {
            String msg = e.getMessage();
            String title = Main.localer.getBundleText("PopupErrorTitle");
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
            Main.getLogger().log(Level.SEVERE, null, e);
        } finally {
            // delete loaded nzb file?
            try {
                if ((Boolean) Main.prefs.get("deletenzb")) {
                    File file = new File(filename);
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
                Main.getLogger().log(Level.SEVERE, null, e);
            }

            // unset background progress bar           
            mainApp.getTaskManager().loadNzb(false);
            if (Main.getStatus().contains("Loading NZB for")) {
                Main.setStatus("");
            }
        }
    }
    
    private static boolean exists(NzbParser parser){
        String id = parser.getId();
        for(int i=0; i<Main.nzbFileQueueTabModel.getRowCount(); i++){
            if(id.equals(Main.nzbFileQueueTabModel.getNzbParser(i).getId())){
                return true;
            }
        }
        return false;
    }
}
