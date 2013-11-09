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
import java.util.Map;
import java.util.HashMap;

/**
 * 
 * @author fishjord
 */
public class ConnectionTestClient extends WifiServerClient {
	private Socket sock;

	public static class ConnectionTestPacket {
		public short packetNum;
		public long rtt;
	}

	private static class DatagramGenerator {
		private ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		private DataOutputStream dataOut = new DataOutputStream(byteOut);
		private final DatagramSocket sock;
		private final InetAddress dest;
		private final int port;
		private Map<Short, Long> sendTimeMap = new HashMap();

		public DatagramGenerator(String serverAddress, int udpPort)
				throws IOException {
			sock = new DatagramSocket();
			dest = InetAddress.getByName(serverAddress);
			port = udpPort;
		}

		public void send(short packetNum) throws IOException {
			byteOut.reset();
			dataOut.writeShort(packetNum);
			long time = System.currentTimeMillis();
			sendTimeMap.put(packetNum, time);
			DatagramPacket packet = new DatagramPacket(byteOut.toByteArray(),
					byteOut.size(), dest, port);
			sock.send(packet);
		}

		public long getSendTime(short packetNum) {
			return sendTimeMap.get(packetNum);
		}

		public void close() throws IOException {
			dataOut.close();
			byteOut.close();
		}
	}

	public List<ConnectionTestPacket> runConnectionTest(String serverAddress,
			short numPackets) throws IOException, UnknownHostException {
		sock = new Socket(serverAddress, WifiSurveyServer.DEFAULT_PORT);

		DataOutputStream out = new DataOutputStream(sock.getOutputStream());
		DataInputStream in = new DataInputStream(sock.getInputStream());

		this.setupConnection(in, out, ClientConnection.CONNECTION_TEST);

		out.writeInt(numPackets);
		int udpPort = in.readInt();
		System.out.println("Sending to port " + udpPort);

		DatagramGenerator sender = new DatagramGenerator(serverAddress, udpPort);
		for (short index = 0; index < numPackets; index++) {
			System.out.println("Sending packet " + index);
			sender.send(index);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		sender.close();

		List<ConnectionTestPacket> ret = new ArrayList();
		long processingTime;
		short packetNum;

		for (short index = 0; index < numPackets; index++) {
			packetNum = in.readShort();
			processingTime = in.readLong();
			System.out.println(packetNum);
			
			if(processingTime == -1) { //packet was lost
				continue;
			}
			
			ConnectionTestPacket packet = new ConnectionTestPacket();
			packet.packetNum = packetNum;
			packet.rtt = System.currentTimeMillis()
					- (processingTime + sender.getSendTime(packetNum));
			ret.add(packet);
		}

		return ret;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err
					.println("USAGE: ConnectionTestClient <remote_host> <num_packets>");
			System.exit(1);
		}

		for (ConnectionTestPacket packet : new ConnectionTestClient()
				.runConnectionTest(args[0], Short.valueOf(args[1]))) {
			System.err.println(packet.packetNum + " " + packet.rtt);
		}
	}
}
