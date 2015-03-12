/*
 * 04/29/2003 - 18:20:51
 *
 * CTCPMessage.java
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

import org.apache.regexp.*;

/**
 * CTCPMessage permet de g�rer les commandes CTCP en decomposant le message pass� en argument.
 * nous pouvons ainsi recup�rer la command (ACTION, VERSION,PING) ainsi que le parametre du message.
 **/
public class CTCPMessage {

    public static final int UNKNOWN = 0;
    public static final int ACTION = 1;
    public static final int PING = 2;
    public static final int DCC = 3;
    public static final int VERSION = 4;
    public static final int SOURCE = 5;
    public static final int CLIENTINFO = 6;
    public static final int DCC_SEND = 7;
    public static final int DCC_RECEIVE = 8;
    public static final int DCC_ACCEPT = 9;
    private int command;
    private String parameter;
    private String key;
    private int endKey;
    private int end;
    private static final HashMap CTCPCommand;
    private static RE infoRE;

    static {
        CTCPCommand = new HashMap();
        CTCPCommand.put("action", new Integer(ACTION));
        CTCPCommand.put("ping", new Integer(PING));
        CTCPCommand.put("dcc", new Integer(DCC));
        CTCPCommand.put("version", new Integer(VERSION));
        CTCPCommand.put("source", new Integer(SOURCE));
        CTCPCommand.put("dcc send", new Integer(DCC_SEND));
        CTCPCommand.put("dcc accept", new Integer(DCC_ACCEPT));
        CTCPCommand.put("clientinfo", new Integer(CLIENTINFO));
        try {
            infoRE = new RE("CLIENTINFO|SOURCE|VERSION|ACTION|DCC SEND");
        } catch (Exception e) {
        }

    }

    /**
     * Construit un nouvel objet CTCPMessage avec le message msg pass� en argument
     * @param msg le message suppos� CTCP.
     **/
    public CTCPMessage(String msg) {
        if (infoRE.match(msg)) {

            end = msg.length();
            key = infoRE.getParen(0).trim();
            endKey = infoRE.getParenEnd(0);

            if (msg.endsWith("\001")) {
                end--;
            }

            Integer cmd = (Integer) CTCPCommand.get(key.toLowerCase());
            this.command = (null != cmd) ? cmd.intValue() : UNKNOWN;
            this.parameter = msg.substring(endKey, end);
        }
    }

    /**
     * Retourne l'entier correspondant a la commande du message CTCP
     **/
    public int getCommand() {
        return command;
    }

    /**
     * Retourne la commande CTCP du message (ACTION, VERSION,PING,...)
     **/
    public String getCommandString() {
        return key;
    }

    /**
     * Renvois le parametre du message CTCP
     **/
    public String getParameter() {
        return parameter;
    }

    /**
     * Detecte si le message s est une commande CTCP
     **/
    public static boolean isCTCPMessage(String s) {
        return (s.charAt(0) == '\001');
    }

    /**
     *en construction...
     *du moins jamais test�
     *@TODO prendre en charge l'envois et la reception de fichier via la commande DCC
     * \001DCC SEND CTCP_and_DCC 3406736986 2097 4509\001
     **/
    public DCCFileItem getFile() {
        String file = "";
        String ip = "";
        int port = 1024;
        int size = 0;
        StringTokenizer st = new StringTokenizer(parameter);
        if (st.hasMoreTokens()) {
            file = st.nextToken().trim();
        }
        if (st.hasMoreTokens()) {
            ip = st.nextToken().trim();
        }
        if (st.hasMoreTokens()) {
            port = Integer.parseInt(st.nextToken().trim());
        }
        if (st.hasMoreTokens()) {
            size = Integer.parseInt(st.nextToken().trim());
        }
        DCCFileItem dccfile = new DCCFileItem(file, ip, port, size);
        return dccfile;
    }

    public DCCFileItem getResumeFile() {
        String file = "";
        int port = 1024;
        int position = 0;
        StringTokenizer st = new StringTokenizer(parameter);
        if (st.hasMoreTokens()) {
            file = st.nextToken().trim();
        }
        if (st.hasMoreTokens()) {
            port = Integer.parseInt(st.nextToken().trim());
        }
        if (st.hasMoreTokens()) {
            position = Integer.parseInt(st.nextToken().trim());
        }
        DCCFileItem dccfile = new DCCFileItem(file, port, position);
        return dccfile;
    }
}
