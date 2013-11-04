/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishjord.wifisurvey.client;

import fishjord.wifisurvey.server.ClientConnection;
import fishjord.wifisurvey.server.WifiServerClient;
import fishjord.wifisurvey.server.WifiSurveyServer;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fishjord
 */
public class ConnectionTestClient extends WifiServerClient {
    private Socket sock;
    
    public static class ConnectionTestPacket {
        public int packetNum;
        public long rtt;
    }
    
    private static class DatagramGenerator {
        private ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        private DataOutputStream dataOut = new DataOutputStream(byteOut);
        private final DatagramSocket sock;
        private final InetAddress dest;
        private final int port;
        
        public DatagramGenerator(String serverAddress, int udpPort) throws IOException {
            sock = new DatagramSocket();
            dest = InetAddress.getByName(serverAddress);
            port = udpPort;
        }
        
        public void send(int packetNum) throws IOException {
            byteOut.reset();
            dataOut.writeInt(packetNum);
            dataOut.writeLong(System.currentTimeMillis());
            DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(), byteOut.size(), dest, port);
            sock.send(packet);
        }
        
        public void close() throws IOException {
            dataOut.close();
            byteOut.close();
        }
    }
    
    public List<ConnectionTestPacket> runConnectionTest(String serverAddress, int numPackets) throws IOException, UnknownHostException {
        sock = new Socket(serverAddress, WifiSurveyServer.DEFAULT_PORT);
        
        DataOutputStream out = new DataOutputStream(sock.getOutputStream());
        DataInputStream in = new DataInputStream(sock.getInputStream());
        
        this.setupConnection(out, ClientConnection.CONNECTION_TEST);
        
        out.writeInt(numPackets);
        int udpPort = in.readInt();
        System.out.println("Sending to port " + udpPort);
        
        DatagramGenerator sender = new DatagramGenerator(serverAddress, udpPort);
        for(int index = 0;index < numPackets;index++) {
            System.out.println("Sending packet " + index);
            sender.send(index + 1);
        }
        sender.close();
                
        List<ConnectionTestPacket> ret = new ArrayList();
        
        for(int index = 0;index < numPackets;index++) {
            ConnectionTestPacket packet = new ConnectionTestPacket();
            packet.packetNum = in.readInt();
            packet.rtt = in.readLong();
            ret.add(packet);
        }
        
        return ret;
    }
    
    public static void main(String[] args) throws Exception {
        for(ConnectionTestPacket packet : new ConnectionTestClient().runConnectionTest("localhost", 1000)) {
            System.err.println(packet.packetNum + " " + packet.rtt);
        }
    }
}
