/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Luiso
 */
public class IRCConnection implements Runnable {

    private static BufferedWriter out;
    private static final HashMap commands;
    private static String host;
    private static String channel;
    private static String bot;
    private static String pack;
    private static int port;
    private Socket socket;
    private static BufferedReader in;
    private final String fileName;
    private static Output output;
    private Media song;
    private ArrayList<String> connectedChannels = new ArrayList<String>();
    public static String currentServerNick;
    private boolean done = false;
    private XWeaselResult result;

    public IRCConnection(XWeaselResult result, Media s) {
        host = result.getNetwork();
        bot = result.getBot();
        channel = result.getChannel();
        pack = result.getPack().substring(1);
        fileName = result.getFilename();
        song = s;
        this.result = result;
    }

    public void stopThread() {
        done = true;
    }

    public ArrayList<String> getConnectedChannels() {
        return connectedChannels;
    }

    public Media getSong() {
        return song;
    }

    public void setSong(Media song) {
        this.song = song;
    }

    public String getNetwork() {
        return host;
    }

    public String getChannel() {
        return channel;
    }

    public String getBot() {
        return bot;
    }

    public String getPack() {
        return pack;
    }
   
    public static void writeLine(String line) {
        try {
            line = line + "\r\n";
            out.write(line);
            out.flush();
        } catch (IOException ioexception) {

            output.printLine("You are not connected to this server");
            output.printLine("Failed to write line [" + line + "]: " + ioexception.getMessage());
        }
    }

    public void specialWriteLine(String line) {
        try {
            line = line + "\r\n";
            out.write(line);
            out.flush();
        } catch (IOException ioexception) {
            output.printLine("You are not connected to this server");
            output.printLine("Failed to write line [" + line + "]: " + ioexception.getMessage());
        }
    }

    private void identify() {
        writeLine("NickServ IDENTIFY " + Main.prefs.get("ircpass"));
        output.printLine("sent indentify!");
    }

    private void register() {
        writeLine("NickServ REGISTER " + Main.prefs.get("ircpass") + " " + Main.prefs.get("ircemail"));
        output.printLine("sent registered!");
    }

    private static void joinChannel() {
        System.out.println(channel);
        writeLine("JOIN " + channel);
    }

    public void joinChannel(String chan) {
        channel = chan;
        joinChannel();
    }

    private static boolean userJoined(String line) {
        String userPart = line.substring(0, line.indexOf("@"));
        System.out.println(userPart);
        if (userPart.contains(currentServerNick)) {
            return true;
        }
        return false;
    }

    public void requestFile(String nick, String pack) {
        try {
            writeLine("PRIVMSG " + nick + " :\u0001" + "XDCC SEND " + pack + "\u0001");
            output.printLine("Requested file!!! ");
        } catch (Exception ex) {
            output.printLine("Couldn't request file!!! ");
        }
    }

    public void disconnect() {
        try {
            out.close();
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
        try {
            in.close();
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private int[] longToIp(long address) {
        int[] ip = new int[4];
        for (int i = 3; i >= 0; i--) {
            ip[i] = (int) (address % 256);
            address = address / 256;
        }
        return ip;
    }

    private void processIRCLine(String line) throws IOException {
        output.printLine("**** [" + line + ']');
        IRCMessage ircmess = null;

        if (line == null) {
            throw new IOException("Stream closed");
        }

        try {
            ircmess = new IRCMessage(line);
        } catch (ParseException e) {
            return;
        }

        String message = ircmess.getMessage();
        String src = ircmess.getSource();
        String params[] = ircmess.getParameters();
        Integer cmd = (Integer) commands.get(ircmess.getCommand());
        if (cmd == null) {
            cmd = new Integer(002);
        }
        /*
        if (line.toLowerCase().contains("/msg nickserv identify")) {
        identify();
        }
        if (line.toLowerCase().contains("End of /MOTD")) {
        cmd = new Integer(376);
        }
        if (line.toLowerCase().contains("not a registered nickname") || line.toLowerCase().contains("nick isn't registered")) {
        register();
        }
        if (line.toLowerCase().contains("you are now identified")) {
        if (onChan == false) {
        joinChannel();
        }
        }
        if(line.toLowerCase().contains("no such nick")){
        String tempBot = line.substring(line.lastIndexOf(src), line.lastIndexOf(":")).trim();
        changeDownloadStatus(Download.ERROR, tempBot);
        }
         * 
         */
        output.printLine(cmd.toString());
        switch (cmd.intValue()) {
            case 2:
                if (line.matches("(?i).*need to be identified to a registered account.*")) {
                    register();
                    requestFile(bot, pack);
                }
                break;
            case -1: // PING
            {
                writeLine("PONG " + line.split(":")[1]);
                output.printLine("Sent pong");
                break;
            }
            case -9: //PRIVMSG
            {
                if (CTCPMessage.isCTCPMessage(message)) {
                    CTCPMessage ctcp = new CTCPMessage(message);
                    output.printLine(ctcp.getCommandString());
                    output.printLine("Param is: " + ctcp.getParameter());
                    switch (ctcp.getCommand()) {
                        case CTCPMessage.ACTION:
                            break;
                        case CTCPMessage.VERSION:
                            //sendVersion(src);
                            break;
                        case CTCPMessage.SOURCE:
                            //sendSource(src);
                            break;
                        case CTCPMessage.CLIENTINFO:
                            //sendClientInfo(src);
                            break;
                        case CTCPMessage.DCC_ACCEPT:
                            /*
                            DccFileTransfer trans1 = new DccFileTransfer(bot, "SEND", filename, iip, filePort, size, song);
                            trans1.execute();
                            trans1.addPropertyChangeListener(
                            new PropertyChangeListener() {

                            public void propertyChange(PropertyChangeEvent evt) {
                            if ("progress".equals(evt.getPropertyName())) {
                            Main.setStatus(((Integer) evt.getNewValue()).toString());
                            }
                            }
                            });
                             *
                             */
                            break;
                        case CTCPMessage.DCC_SEND:
                            String filename = ctcp.getFile().getName();
                            long iip = new Long(ctcp.getFile().getIp()).longValue();
                            int[] ip = longToIp(iip);
                            String ipStr = ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
                            System.out.println(ipStr);
                            int filePort = ctcp.getFile().getPort();
                            long size = ctcp.getFile().getSize();
                            System.out.println(filename);
                            System.out.println(iip);
                            System.out.println(filePort);
                            System.out.println(size);
                            //DCCFileReceived dfc = new DCCFileReceived(filename, ctcp.getFile().getIp(), filePort, (int)size);
                            //dfc.execute();
                            DccFileTransfer trans = new DccFileTransfer(bot, "SEND", filename, iip, filePort, size, song, true);
                            trans.execute();
                            /*
                            trans.addPropertyChangeListener(
                            new PropertyChangeListener() {

                            public void propertyChange(PropertyChangeEvent evt) {
                            if ("progress".equals(evt.getPropertyName())) {
                            //Main.setStatus(((Integer) evt.getNewValue()).toString());
                            }
                            }
                            });
                             * 
                             */
                            break;
                        case CTCPMessage.PING:
                            sendPingReply(src);
                            output.printLine("Sent CTCP pong");
                            break;
                    }
                }
                break;
            }
            case -3: //JOIN
            {
                if (userJoined(line)) {
                    output.printLine("Connected to " + channel);
                    connectedChannels.add(channel);
                    //writeLine("MODE " + channel);
                    writeLine("WHO " + src);
                    requestFile(bot, pack);
                }
                break;
            }
            case -4:
                output.printLine("Entered case -4");
                break;
            case -6: //QUIT
                output.printLine(line + " :QUIT from server " + host);
                /*
                disconnect();
                Main.getConnections().remove(this);
                System.out.println("Active servers are: ");
                for(IRCConnection conn : Main.getConnections()){
                System.out.println(conn.getNetwork());
                }
                //output.setVisible(false);
                //output.dispose();
                 * 
                 */
                break;
            case -5: //PART
                String theChannel = "";
                for (int i = 0; i < connectedChannels.size(); i++) {
                    if (connectedChannels.get(i).equalsIgnoreCase(theChannel)) {
                        connectedChannels.remove(i);
                        break;
                    }
                }
                output.printLine("Quit " + theChannel);
                break;
            case -7: //KICK
                break;
            case -2: //NICK
                break;
            case -10: //NOTICE
            {
                if (line.matches("(?i).*is already registered.*")) {
                    identify();
                }
                if (line.matches("(?i).*/msg NickServ IDENTIFY password.*")) {
                    identify();
                }
                if(line.matches("(?i).*invalid pack number.*")){
                    Main.setStatus("Invalid pack number for most recent download");
                    //Main.getLatch().countDown();
                }
                if(line.matches("(?i).*closing connection.*")){
                    //Main.getLatch().countDown();
                }
                break;
            }
            case -11: //ERROR
                break;
            case 376: // end of MOTD
            {
                writeLine("MODE " + currentServerNick + " +i");
                writeLine("PROTOCTL NAMESX");
                joinChannel();
                break;
                /*
                register();
                identify();
                join();
                output.printLine("Sent the JOIN command");
                output.printLine("Sent the JOIN command");
                output.printLine("Sent the JOIN command");
                break;
                 * *
                 */
            }
            case 422:	// ERR_NOMOTD
                output.printLine("ERR NOMOTD 422 " + host);
                // joinChannel();
                break;
            case 366: // end of /list
                //writeLine("WHO " + params[1]);
                output.printLine("entered case 366");
                break;
            case 318:
                /* replies of the whois message */
                break;
            case 319:
                break;
            case 321: // RPL_LISTSTART
                break;
            case 322: // RPL_LIST
            {
                if (params[1].equals("*")) {
                    break;
                }
                try {
                    
                } catch (Exception ex) {
                }
                break;
            }
            case 315: // RPL_ENDOFWHO
                break;
            case 323: // RPL_LISTEND
                break;
            case 352: // RPL_WHOREPLY
            {
                if (params.length < 6) {
                    return;
                }
                break;
            }
            case 334: {
                break;
            }
            case 401:
                if (line.contains(bot)) {
                    String temp = line.substring(line.indexOf(currentServerNick), line.indexOf(" :No such nick"));
                    String tokens[] = temp.split(" ");
                    String noBot = tokens[1];
                    if (!noBot.equals("testing")) {
                        Main.setStatus("Bot " + noBot + " is not in channel for " + fileName);
                        //Main.getLatch().countDown();
                    }
                }
                break;

            case 432:	// ERR_ERRONEUSNICKNAME
                break;
            case 406: // ERR_WASNOSUCHNICK
            {
                String altnick = (String) Main.prefs.get("ircalt");
                if (altnick.equals(getNick())) {
                    altnick = altnick + Math.random();
                }
                writeLine("NICK " + altnick);
                currentServerNick = altnick;
                break;
            }
            case 433: {
                output.printLine("WARNING: Nick name already in use, using alternate...");
                String altnick = (String) Main.prefs.get("ircalt");
                if (altnick.equals(getNick())) {
                    String tmp = JOptionPane.showInputDialog(null, "Enter new nick:", "Nick name already in use", JOptionPane.QUESTION_MESSAGE);
                    if (tmp != null && !tmp.equals("")) {
                        altnick = tmp;
                    }
                }
                writeLine("NICK " + altnick);
                currentServerNick = altnick;
                break;
            }
        }
        return;
    }

    static {
        commands = new HashMap(50, 1);
        commands.put("PING", new Integer(-1));
        commands.put("NICK", new Integer(-2));
        commands.put("JOIN", new Integer(-3));
        commands.put("MODE", new Integer(-4));
        commands.put("PART", new Integer(-5));
        commands.put("QUIT", new Integer(-6));
        commands.put("KICK", new Integer(-7));
        commands.put("TOPIC", new Integer(-8));
        commands.put("PRIVMSG", new Integer(-9));
        commands.put("NOTICE", new Integer(-10));
        commands.put("ERROR", new Integer(-11));
        commands.put("WALLOPS", new Integer(-12));
        commands.put("INVITE", new Integer(-13));
        commands.put("001", new Integer(001));
        commands.put("232", new Integer(232));
        commands.put("255", new Integer(255));
        commands.put("256", new Integer(256));
        commands.put("257", new Integer(257));
        commands.put("258", new Integer(258));
        commands.put("259", new Integer(259));
        commands.put("301", new Integer(301));
        commands.put("305", new Integer(305));
        commands.put("306", new Integer(306));
        commands.put("307", new Integer(307));
        commands.put("310", new Integer(310));
        commands.put("311", new Integer(311));
        commands.put("312", new Integer(312));
        commands.put("313", new Integer(313));
        commands.put("314", new Integer(314));
        commands.put("315", new Integer(315));
        commands.put("317", new Integer(317));
        commands.put("318", new Integer(318)); // No action needed.
        commands.put("319", new Integer(319));
        commands.put("320", new Integer(320));
        commands.put("321", new Integer(321));
        commands.put("322", new Integer(322));
        commands.put("323", new Integer(323));
        commands.put("324", new Integer(324));
        commands.put("328", new Integer(328));
        commands.put("329", new Integer(329)); // No action needed.
        commands.put("330", new Integer(330));
        commands.put("331", new Integer(331));
        commands.put("332", new Integer(332));
        commands.put("333", new Integer(333));
        commands.put("335", new Integer(335));
        commands.put("352", new Integer(352));
        commands.put("353", new Integer(353));
        commands.put("366", new Integer(366));
        commands.put("367", new Integer(367));
        commands.put("368", new Integer(368));
        commands.put("371", new Integer(371));
        commands.put("372", new Integer(372));
        commands.put("375", new Integer(375));
        commands.put("376", new Integer(376));
        commands.put("378", new Integer(378));
        commands.put("381", new Integer(381));
        commands.put("401", new Integer(401));
        commands.put("404", new Integer(404));
        commands.put("406", new Integer(406));
        commands.put("421", new Integer(421));
        commands.put("422", new Integer(422));
        commands.put("432", new Integer(432));
        commands.put("433", new Integer(433));
        commands.put("437", new Integer(437));
        commands.put("438", new Integer(438));
        commands.put("447", new Integer(447));
        commands.put("451", new Integer(451));
        commands.put("461", new Integer(461));
        commands.put("464", new Integer(464));
        commands.put("465", new Integer(465));
        commands.put("471", new Integer(471));
        commands.put("473", new Integer(473));
        commands.put("474", new Integer(474));
        commands.put("475", new Integer(475));
        commands.put("478", new Integer(478));
        commands.put("481", new Integer(481));
        commands.put("482", new Integer(482));
        commands.put("491", new Integer(491));
        commands.put("600", new Integer(600));
        commands.put("601", new Integer(601));
        commands.put("604", new Integer(604));
        commands.put("605", new Integer(605));
    }

    private static String getNick() {
        return (String) Main.prefs.get("ircnick");
    }

    public void sendPingReply(String nick) {
        writeLine("NOTICE " + nick + " :\001PING " + (new Date()).getTime() + "\001");
    }

    public void run() {
        //Main.setStatus("Connecting...");
        in = null;
        socket = null;
        port = 6667;
        try {
            socket = new Socket(host, port);
        } catch (UnknownHostException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
            Main.setStatus("Unable to connect to server " + result.getNetwork() + " : " + ex.getMessage());
            Main.removeConnection(this);
            //Main.getLatch().countDown();
        } catch (IOException ex) {
            Main.getLogger().log(Level.SEVERE, null, ex);
            Main.setStatus("Unable to connect to server " + result.getNetwork() + " : " + ex.getMessage());
            Main.removeConnection(this);
            //Main.getLatch().countDown();
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            socket.setSoTimeout(150000);
            String newLine = "";
            output = new Output(host, channel, this);
            output.setVisible(true);
            output.printLine("Sending nick");
            writeLine("NICK " + (String) Main.prefs.get("ircnick"));
            output.printLine("Sent " + (String) Main.prefs.get("ircnick"));
            currentServerNick = (String) Main.prefs.get("ircnick");
            output.printLine("Sending user");
            writeLine("USER " + Main.prefs.get("ircuser") + " 8 * :" + Main.prefs.get("ircuser"));
            processIRCLine(in.readLine());
            output.printLine("Sent user");
            //Main.setStatus("Connected");
            Main.getConnections().add(this);
            while ((newLine = in.readLine()) != null || !done) {
                processIRCLine(newLine);
            }
            Main.removeConnection(this);
            output.printLine("Finished reading lines from " + host);
        } catch (IOException e) {
            System.out.println("Failed to connect to IRC server " + host + ":" + port);
            output.printLine("Failed to connect to IRC server " + host + ":" + port);
            Main.setStatus("Failed to connect to IRC server " + host + ":" + port);
            Main.removeConnection(this);
            //Main.getLatch().countDown();
        }
    }

}
