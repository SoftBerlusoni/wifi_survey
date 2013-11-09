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
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author fishjord
 */
public class ClientConnection implements Runnable {

	private static final Logger log = Logger.getLogger(ClientConnection.class
			.getCanonicalName());
	private final Socket sock;
	public static final int VERSION = 1;
	public static final int MAGIC_NUMBER = 63383234;
	public static final byte RECORD_SAMPLE = 0;
	public static final byte RECORD_TRAINING = 1;
	public static final byte CONNECTION_TEST = 2;

	public ClientConnection(Socket sock) {
		this.sock = sock;
	}

	private byte initConnection(DataInputStream in, DataOutputStream out)
			throws IOException {
		if (in.readInt() != MAGIC_NUMBER) {
			throw new IOException("Invalid magic number");
		}

		if (in.readByte() != VERSION) {
			throw new IOException("Invalid version number");
		}

		return in.readByte();
	}

	private void runConnectionTest(DataInputStream in, DataOutputStream out)
			throws IOException {
		DatagramSocket udpSock = new DatagramSocket();

		int numPackets = in.readInt();

		if (numPackets <= 0 || numPackets > 1000) {
			throw new IOException(
					"Number of packets must be in the range (0-1000]");
		}
		log.log(Level.INFO,
				"Client connection {0} established, waiting for {1} packets",
				new Object[] { sock, numPackets });

		udpSock.setSoTimeout(5000);
		out.writeInt(udpSock.getLocalPort());

		short packetNum;

		byte[] buf = new byte[16];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		Map<Short, Long> recievedTimes = new HashMap();
		try {
			for (int index = 0; index < numPackets; index++) {
				udpSock.receive(packet);
				packetNum = (short) ((buf[0] << 8) + (buf[1] << 0));
				recievedTimes.put(packetNum, System.currentTimeMillis());
			}
		} catch (SocketTimeoutException ignore) { // 5 seconds is quite a
													// liberal time out, assume
													// there are no packets left
													// to recieve if we timeout
			log.log(Level.WARNING, "Socket timeout from {0}", sock);
		}

		for (short index = 0; index < numPackets; index++) {
			out.writeShort(index);
			if (recievedTimes.containsKey(index)) {
				out.writeLong(System.currentTimeMillis()
						- recievedTimes.get(index));
			} else {
				out.writeLong(-1);
			}

			log.log(Level.INFO,
					"Recieved packet {0} from {1}, lost?: {2}",
					new Object[] { index, sock,
							recievedTimes.containsKey(index) });
		}
	}

	public void run() {
		DataInputStream in = null;
		DataOutputStream out = null;
		try {
			in = new DataInputStream(sock.getInputStream());
			out = new DataOutputStream(sock.getOutputStream());
			switch (initConnection(in, out)) {
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
			log.log(Level.WARNING, "Error when processing request from client "
					+ sock, e);
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
