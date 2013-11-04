/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fishjord.wifisurvey.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fishjord
 */
public class ClientConnection implements Runnable {

    private static final Logger log = Logger.getLogger(ClientConnection.class.getCanonicalName());
    private final Socket sock;
    public static final int VERSION = 1;
    public static final int MAGIC_NUMBER = 63383234;
    public static final byte RECORD_SAMPLE = 0;
    public static final byte RECORD_TRAINING = 1;
    public static final byte CONNECTION_TEST = 2;

    public ClientConnection(Socket sock) {
        this.sock = sock;
    }

    private byte initConnection(DataInputStream in) throws IOException {
        if (in.readInt() != MAGIC_NUMBER) {
            throw new IOException("Invalid magic number");
        }

        if (in.readByte() != VERSION) {
            throw new IOException("Invalid version number");
        }

        return in.readByte();
    }

    private static class CachedPacket {

        DatagramPacket packet = new DatagramPacket(new byte[16], 16);
        long recievedTime;
    }

    private void runConnectionTest(DataInputStream in, DataOutputStream out) throws IOException {
        DatagramSocket udpSock = new DatagramSocket();

        int numPackets = in.readInt();

        if (numPackets <= 0 || numPackets > 1000) {
            throw new IOException("Number of packets must be in the range (0-1000]");
        }
        log.log(Level.INFO, "Client connection {0} established, waiting for {1} packets", new Object[]{sock, numPackets});

        List<CachedPacket> packets = new ArrayList();
        for (int index = 0; index < numPackets; index++) {
            packets.add(new CachedPacket());
        }

        udpSock.setSoTimeout(5000);
        out.writeInt(udpSock.getLocalPort());

        CachedPacket packet;
        try {
            for (int index = 0; index < numPackets; index++) {
                packet = packets.get(index);
                udpSock.receive(packet.packet);
                packet.recievedTime = System.currentTimeMillis();
            }
        } catch (SocketTimeoutException ignore) { //5 seconds is quite a liberal time out, assume there are no packets left to recieve if we timeout
            log.log(Level.WARNING, "Socket timeout from {0}", sock);
        }

        DataInputStream dis;
        int packetNumber;
        long sentTime;
        BitSet recieved = new BitSet(numPackets);

        for (int index = 0; index < numPackets; index++) {
            packet = packets.get(index);
            if(packet.recievedTime == 0) {
                break;
            }
            dis = new DataInputStream(new ByteArrayInputStream(packet.packet.getData()));
            packetNumber = dis.readInt();
            recieved.set(packetNumber - 1);
            sentTime = dis.readLong();

            log.log(Level.INFO, "Recieved packet {0} from {1}, rtt {2}", new Object[]{packetNumber, sock, System.currentTimeMillis() - sentTime});
            out.writeInt(packetNumber);
            out.writeLong(System.currentTimeMillis() - sentTime);
        }

        for (int index = 1; index <= numPackets; index++) {
            if (!recieved.get(index - 1)) {
                log.log(Level.INFO, "Packet {0} from {1} lost", new Object[]{index, sock});
                out.writeInt(index);
                out.writeLong(-1);
            }
        }
    }

    public void run() {
        DataInputStream in = null;
        DataOutputStream out = null;
        try {
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
            switch (initConnection(in)) {
                case RECORD_SAMPLE:
                    break;
                case RECORD_TRAINING:
                    break;
                case CONNECTION_TEST:
                    runConnectionTest(in, out);
                    break;
                default:
                    throw new IOException("Uknown message type");
            }

        } catch (IOException e) {
            log.log(Level.WARNING, "Error when processing request from client " + sock, e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ignore) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignore) {
            }
            try {
                sock.close();
            } catch (IOException ignore) {
            }
        }
    }
}
