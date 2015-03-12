/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package musicchecker;

/**
 *
 * @author Luiso
 */
public class XWeaselResult extends SearchResult{

    private String filename;
    private String bot;
    private String network;
    private String channel;
    private String speed;
    private String pack;

    public XWeaselResult(){
        super();
    }

    public XWeaselResult(String filename, String size, String bot, String network, String channel, String speed, String pack) {
        super(filename);
        setSize(size);
        this.bot = bot;
        this.network = network;
        this.channel = channel;
        this.speed = speed;
        this.pack = pack;
        setExtension(filename.substring(filename.length() - 3, filename.length()));
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public String getBot() {
        return bot;
    }

    public void setBot(String bot) {
        this.bot = bot;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    @Override
    public String toString(){
        return filename + ", " + getSize() + ", " + pack + ", " + speed + ", " + bot + ", " + network + ", " + channel;
    }

}
