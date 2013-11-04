/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishjord.wifisurvey.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fishjord
 */
public class WifiSurveyServer implements Runnable {
    private static final Logger log = Logger.getLogger(WifiSurveyServer.class.getCanonicalName());
    private final int listenPort;
    private final Executor threadPool;
    private boolean running = true;
    
    public static int DEFAULT_PORT = 25252;
    
    public WifiSurveyServer(int listenPort, int threadCount) {
        this.listenPort = listenPort;
        this.threadPool = Executors.newFixedThreadPool(threadCount);
    }

    @Override
    public void run() {
        try {
        ServerSocket sock = new ServerSocket(listenPort);
        log.log(Level.INFO, "Wifi Survey Server started up, listening on {0}", listenPort);
        
        while(running) {
            Socket client = sock.accept();
            log.log(Level.INFO, "Connection from {0}", client);
            threadPool.execute(new ClientConnection(client));
        }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void main(String[] args) {
        new WifiSurveyServer(DEFAULT_PORT, 5).run();
    }
}
