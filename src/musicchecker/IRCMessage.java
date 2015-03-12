/*
 * 04/29/2003 - 18:20:51
 *
 * IRCMessage.java
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

import java.util.*;
import java.text.ParseException;

/**
 * IRCMessage 
 * @todo Use an IRCMessageHelper singleton to parse a command to an IRCMessage
 * @author veryfree <very.free@free.fr>
 * @author $Author: veryfree $ (last edit)
 * @version $Revision: 1.6 $
 */
public class IRCMessage {

    private String command = "";
    private String message = "";
    private String IRCLine;
    private String source = "";
    private String src = "";
    private int index = 0;
    private Vector params;
    private StringTokenizer st;
    private String[] parameters;
    private String line;
    private String ip = "";

    private boolean isMalformed = false;

	/**
	* 
	* @param IRCLine la 
	**/
    public IRCMessage(String IRCLine) throws ParseException {

        if (IRCLine == null)
            throw new ParseException("Null message ", 0);

        this.IRCLine = IRCLine.trim();

        if (IRCLine.length() == 0)
            throw new ParseException("Empty message ", 0);

        if (IRCLine.charAt(0) != ':') {
            if (IRCLine.startsWith("PING")) {
                this.command = "PING";
            } else if (IRCLine.startsWith("ERROR")) {
                this.command = "ERROR";
            } else {
                throw new ParseException("Malformed message :" + IRCLine, 0);
            }
        } else {
            index = IRCLine.indexOf(":", 2);

            if (index != -1) {
                this.line = IRCLine.substring(1, index);
                this.message = IRCLine.substring(index + 1);
            } else {
                this.line = IRCLine.substring(1);
                isMalformed = true;
            }

            params = new Vector();

            st = new StringTokenizer(line);

            if (!st.hasMoreTokens())
                throw new ParseException("Malformed message :" + IRCLine, 0);

            source = st.nextToken();

            if ((index = source.indexOf('!')) != -1) {
                src = source.substring(0, index);
                ip = source.substring(index + 1);
            } else
                src = source;
            if (!st.hasMoreTokens())
                throw new ParseException("Command expected :" + IRCLine, 0);

            this.command = st.nextToken();

            while (st.hasMoreTokens()) {
                params.addElement(st.nextToken());
            }

            this.parameters = new String[params.size()];
            params.copyInto(parameters);
        }
    }
	/**
	* 
	**/
    public String getCommand() {
        return command;
    }
	/**
	* 
	**/
    public String getMessage() {
        return message;
    }
	/**
	* 
	* @see #getParameters()
	**/
    public int getSize() {
        return parameters.length;
    }
	/**
	* 
	**/
	public String getSource() {
	    return src;
	}
	/**
	*Retourne la pseudo adresse ip presente dans presque chaque message IRC
	**/
	public String getIp() {
	    return ip;
	}
	/**
	* Retourne un tableau contenant les messages qui suivent la commande IRC du message
	* voir les RFC. 
	**/
	public String[] getParameters() {
	    return parameters;
	}
	/**
	*Retourne true si le message IRC ne contiens pas de suffix (:)
	**/
	public boolean isMalformed() {
	    return isMalformed;
	}
	/**
	* Affiche dans la console toute les valeurs contenues dans IRCMessage
	**/
	public void print() {
		System.out.println("source:" + getSource());
	    if (parameters == null) return;
	    for (int i = 0; i < parameters.length; i++) {
	        System.out.println(i + ":" + parameters[i]);
	    }
	}

    /**
     * Returns a <code>String</code> representation of this <code>IRCMessage</code>.
     * @return the string representation of the IRCMessage.
     */
    public String toString() {
        return "IRC message ["+ IRCLine+"], Code ["+getCommand()+"], message ["+getMessage()+"], " +
                "source ["+getSource()+"]";
    }
}
