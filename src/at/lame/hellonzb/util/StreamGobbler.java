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
 *****************************************************************************
 */
package at.lame.hellonzb.util;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import musicchecker.Main;

public class StreamGobbler extends Thread {

    private InputStream is;
    private String type;

    public StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                //Main.getLogger().log(Level.INFO, "{0}> {1}", new Object[]{type, line});
            }
        } catch (IOException e) {
            Main.getLogger().log(Level.SEVERE, null, e);
        }
    }
}
