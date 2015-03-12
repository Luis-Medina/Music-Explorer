/*
 * 04/29/2003 - 18:20:51
 *
 * DCCFile.java
 * Copyright (C) 2000 veryfree
 * very.free@free.fr
 * http://jchatirc.free.fr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package musicchecker;

public class DCCFileItem {

    private String filename;
    private String ip;
    private int port;
    private long size;
    private int position;

    public DCCFileItem(String filename, int port, int position) {
        this.filename = filename;
        this.port = port;
        this.position = position;
    }

    public DCCFileItem(String filename, String ip, int port, long size) {
        this.filename = filename;
        this.ip = ip;
        this.port = port;
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return filename;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }
}
