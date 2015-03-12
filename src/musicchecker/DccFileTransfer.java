/* 
Copyright Paul James Mutton, 2001-2009, http://www.jibble.org/

This file is part of PircBot.

This software is dual-licensed, allowing you to choose between the GNU
General Public License (GPL) and the www.jibble.org Commercial License.
Since the GPL may be too restrictive for use in a proprietary application,
a commercial license is also provided. Full license information can be
found at http://www.jibble.org/licenses/

 */
package musicchecker;

import java.net.*;
import java.io.*;
import java.util.List;
import javax.swing.SwingWorker;
import at.lame.hellonzb.HelloNzbToolkit;

/**
 * This class is used to administer a DCC file transfer.
 *
 * @since   1.2.0
 * @author  Paul James Mutton,
 *          <a href="http://www.jibble.org/">http://www.jibble.org/</a>
 * @version    1.5.0 (Build time: Mon Dec 14 20:07:17 2009)
 */
public class DccFileTransfer extends SwingWorker<Boolean, Double> {

    /**
     * The default buffer size to use when sending and receiving files.
     */
    public static final int BUFFER_SIZE = 1024;
    private boolean resume;
    private Media song;
    private Download d;

    /**
     * Constructor used for receiving files.
     */
    DccFileTransfer(String nick, String type, String filename, long address, int port, long size, Media s, boolean res) {
        _nick = nick;
        //_login = login;
        //_hostname = hostname;
        _type = type;
        _file = new File(Main.getDownDir() + "\\" + filename);
        _address = address;
        _port = port;
        _size = size;
        _received = false;
        _incoming = true;
        song = s;
        resume = res;
        d = new Download(song, HelloNzbToolkit.getRandomID());
        if (port == 0) {
            d.setStatus(4);
        }
        d.setSize((int) _size);
        Main.addDownload(d);
    }

    public String getBot() {
        return _nick;
    }

    /**
     * Constructor used for sending files.
     */
    /**
     * Receives a DccFileTransfer and writes it to the specified file.
     * Resuming allows a partial download to be continue from the end of
     * the current file contents.
     * 
     * @param file The file to write to.
     * @param resume True if you wish to try and resume the download instead
     *               of overwriting an existing file.
     * 
     */
    public synchronized void receive(File file, boolean resume) {
        if (!_received) {
            _received = true;
            _file = file;

            if (_type.equals("SEND") && resume) {
                _progress = file.length();
                if (_progress == 0) {
                    //doReceive(file, false);
                }
            } else {
                _progress = file.length();
                //doReceive(file, resume);
            }
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

    /**
     * Package mutator for setting the progress of the file transfer.
     */
    void setProgress(long progress) {
        _progress = progress;
    }

    /**
     *  Delay between packets.
     */
    private void delay() {
        if (_packetDelay > 0) {
            try {
                Thread.sleep(_packetDelay);
            } catch (InterruptedException e) {
                // Do nothing.
            }
        }
    }

    /**
     * Returns the nick of the other user taking part in this file transfer.
     * 
     * @return the nick of the other user.
     * 
     */
    public String getNick() {
        return _nick;
    }

    /**
     * Returns the login of the file sender.
     * 
     * @return the login of the file sender. null if we are sending.
     * 
     */
    public String getLogin() {
        return _login;
    }

    /**
     * Returns the hostname of the file sender.
     * 
     * @return the hostname of the file sender. null if we are sending.
     * 
     */
    public String getHostname() {
        return _hostname;
    }

    /**
     * Returns the suggested file to be used for this transfer.
     * 
     * @return the suggested file to be used.
     * 
     */
    public File getFile() {
        return _file;
    }

    /**
     * Returns the port number to be used when making the connection.
     * 
     * @return the port number.
     * 
     */
    public int getPort() {
        return _port;
    }

    /**
     * Returns true if the file transfer is incoming (somebody is sending
     * the file to us).
     * 
     * @return true if the file transfer is incoming.
     * 
     */
    public boolean isIncoming() {
        return _incoming;
    }

    /**
     * Returns true if the file transfer is outgoing (we are sending the
     * file to someone).
     * 
     * @return true if the file transfer is outgoing.
     * 
     */
    public boolean isOutgoing() {
        return !isIncoming();
    }

    /**
     * Sets the delay time between sending or receiving each packet.
     * Default is 0.
     * This is useful for throttling the speed of file transfers to maintain
     * a good quality of service for other things on the machine or network.
     *
     * @param millis The number of milliseconds to wait between packets.
     * 
     */
    public void setPacketDelay(long millis) {
        _packetDelay = millis;
    }

    /**
     * returns the delay time between each packet that is send or received.
     * 
     * @return the delay between each packet.
     * 
     */
    public long getPacketDelay() {
        return _packetDelay;
    }

    /**
     * Returns the size (in bytes) of the file being transfered.
     * 
     * @return the size of the file. Returns -1 if the sender did not
     *         specify this value.
     */
    public long getSize() {
        return _size;
    }

    /**
     * Returns the progress (in bytes) of the current file transfer.
     * When resuming, this represents the total number of bytes in the
     * file, which may be greater than the amount of bytes resumed in
     * just this transfer.
     * 
     * @return the progress of the transfer.
     */
    public long getFileProgress() {
        return _progress;
    }

    /**
     * Returns the progress of the file transfer as a percentage.
     * Note that this should never be negative, but could become
     * greater than 100% if you attempt to resume a larger file
     * onto a partially downloaded file that was smaller.
     * 
     * @return the progress of the transfer as a percentage.
     */
    public double getProgressPercentage() {
        return 100 * (getProgress() / (double) getSize());
    }

    /**
     * Stops the DCC file transfer by closing the connection.
     */
    public void close() {
        try {
            _socket.close();
        } catch (Exception e) {
            // Let the DCC manager worry about anything that may go wrong.
        }
    }

    /**
     * Returns the rate of data transfer in bytes per second.
     * This value is an estimate based on the number of bytes
     * transfered since the connection was established.
     *
     * @return data transfer rate in bytes per second.
     */
    public long getTransferRate() {
        long time = (System.currentTimeMillis() - _startTime) / 1000;
        if (time <= 0) {
            return 0;
        }
        return getProgress() / time;
    }

    /**
     * Returns the address of the sender as a long.
     *
     * @return the address of the sender as a long.
     */
    public long getNumericalAddress() {
        return _address;
    }
    private String _nick;
    private String _login = null;
    private String _hostname = null;
    private String _type;
    private long _address;
    private int _port;
    private long _size;
    private boolean _received;
    private Socket _socket = null;
    private long _progress = 0;
    private File _file = null;
    private boolean _incoming;
    private long _packetDelay = 0;
    private long _startTime = 0;

    @Override
    protected Boolean doInBackground() throws Exception {


        System.out.println("Starting..");
        BufferedOutputStream foutput = null;
        BufferedInputStream input = null;

        try {

            // Convert the integer address to a proper IP address.
            int[] ip = longToIp(_address);
            String ipStr = ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
            System.out.println("File: " + _file);
            System.out.println("IP: " + ipStr);
            System.out.println("Port: " + _port);
            System.out.println("Size: " + _size);
            System.out.println("File Can. Path: " + _file.getCanonicalPath());
            // Connect the socket and set a timeout.
            _socket = new Socket(ipStr, _port);
            _socket.setSoTimeout(30 * 1000);
            _startTime = System.currentTimeMillis();


            input = new BufferedInputStream(_socket.getInputStream());
            BufferedOutputStream output = new BufferedOutputStream(_socket.getOutputStream());
            System.out.println("Created first two outputs");
            // Following line fixed for jdk 1.1 compatibility.
            foutput = new BufferedOutputStream(new FileOutputStream(_file.getCanonicalPath(), resume));
            System.out.println("Created all outputs");

            byte[] inBuffer = new byte[BUFFER_SIZE];
            byte[] outBuffer = new byte[4];
            int bytesRead = 0;
            /*
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    double rate = (_progress/1024)/1000;
                     System.out.println("Rate is: " + rate);
                }
            };
            timer.scheduleAtFixedRate(task, 1000, 1000);
             *
             */
            while ((bytesRead = input.read(inBuffer, 0, inBuffer.length)) != -1) {
                foutput.write(inBuffer, 0, bytesRead);
                _progress += bytesRead;
                // Send back an acknowledgement of how many bytes we have got so far.
                outBuffer[0] = (byte) ((_progress >> 24) & 0xff);
                outBuffer[1] = (byte) ((_progress >> 16) & 0xff);
                outBuffer[2] = (byte) ((_progress >> 8) & 0xff);
                outBuffer[3] = (byte) ((_progress) & 0xff);
                output.write(outBuffer);
                output.flush();
                delay();
                d.setDownloaded((int) _progress);
                Main.getDownloadsModel().fireTableDataChanged();
            }
            foutput.flush();
            d.setStatus(Download.COMPLETE);
            d.getMedia().setDownloaded(true);
            System.out.println("Finished transfer");
            Main.currentDownloadSuccessful = true;
            Main.getDownloadsModel().fireTableDataChanged();
            Main.getSongsTableModel().fireTableDataChanged();
            if (!JDBC.checkDownloaded(song)) {
                JDBC.setDownloaded(song);
            }
        } catch (Exception e) {
            System.out.println("Exception in transfer " + e.getMessage());
            d.setStatus(Download.BADPORT);
            Main.getDownloadsModel().fireTableDataChanged();
            return false;
        } finally {
            //DELETED CHANGE TO SINGLE PROCESS
            //Main.getLatch().countDown();
            try {
                input.close();
                foutput.close();
                _socket.close();
            } catch (Exception anye) {
                // Do nothing.
            }
            System.out.println("Closed streams");
        }
        return true;
    }

    @Override
    protected void process(List<Double> doubles) {
        for (double number : doubles) {
            System.out.println(Double.toString(number));
        }
    }

    @Override
    protected void done() {
    }

    protected class MyDownload {

        private double progress;
        private long rate;

        MyDownload(double p, long r) {
            progress = p;
            rate = r;
        }

        double getProgress() {
            return progress;
        }

        long getRate() {
            return rate;
        }
    }
}
