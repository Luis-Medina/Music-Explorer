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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 *
 * @author Luiso
 */
public class IdentdServer extends SwingWorker{

    private ServerSocket socket;

    public IdentdServer() {
        try {
            socket = new ServerSocket(113);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 113.");
        }    
    }

    @Override
    protected Object doInBackground() throws Exception {
        while (true) {
            // Accept incoming connections.
            Socket clientSocket = null;
            try {
                clientSocket = socket.accept();
            } catch (IOException ex) {
                Main.getLogger().log(Level.SEVERE, null, ex);
            }
            IdentdServerThread cliThread = new IdentdServerThread(clientSocket);
            cliThread.start();
        }
    }

    class IdentdServerThread extends Thread {

        private Socket socket;

        public IdentdServerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream()));
                String msg = br.readLine();
                // should be in format <port-on-server> , <port-on-client>
                System.out.println("Request: " + msg);
                if (msg != null) {
                    // send response back to server
                    msg += " : USERID : UNIX : "
                            + ("luiso85");
                    System.out.println("Response: " + msg);
                    wr.write(msg);
                    wr.newLine();
                    wr.flush();
                    System.out.println("Response sent, waiting from server");
                    System.out.println("server:" + br.readLine());
                }
                br.close();
                wr.close();
                socket.close();
                System.out.println("Done.");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
