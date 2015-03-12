/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package musicchecker;

/**
 *
 * @author Luiso
 */
public class FinderUtils {

    public static IRCConnection getChannelConnection(String network, String channel) {
        for (IRCConnection conn : Main.getConnections()) {
            if (conn.getNetwork().equals(network)) {
                if (conn.getChannel().equals(channel)) {
                    return conn;
                }
            }
        }
        return null;
    }

    public static IRCConnection getChannelConnection(String network) {
        for (IRCConnection conn : Main.getConnections()) {
            if (conn.getNetwork().equals(network)) {
                return conn;
            }
        }
        return null;
    }
}
