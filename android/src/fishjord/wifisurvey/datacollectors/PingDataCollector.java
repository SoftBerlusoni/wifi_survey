package fishjord.wifisurvey.datacollectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import fishjord.wifisurvey.ScanLevel;
import fishjord.wifisurvey.utils.InetUtils;

public class PingDataCollector extends DataCollector {
	
	private static final Pattern pingRegex = Pattern.compile("icmp_seq=(\\d+)\\s+ttl=(\\d+)\\s+time=([\\d\\.]+)");
	
	private final int pingCount;
	private PingData cachedData;

	public PingDataCollector(WifiManager wifiManager, int pingCount) {
		super(wifiManager);
		this.pingCount = pingCount;
	}
	
	private static List<String> consume(InputStream in) {
		BufferedReader is = new BufferedReader(new InputStreamReader(in));
		String line;
		List<String> lines = new ArrayList();
		
		try {
			while((line = is.readLine()) != null) {
				lines.add(line);
			}
		} catch(IOException ignore) {
			
		}
		
		return lines;
	}

	@Override
	public WifiSurveyData getLastUpdate() {
		return cachedData;
	}

	@Override
	public int getScanLevel() {
		return ScanLevel.ACTIVE;
	}

	@Override
	public boolean doRefreshData() {
		if(!wifiManager.isWifiEnabled()) {
			return false;
		}
		
		DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		
		if(dhcpInfo == null || wifiInfo == null) {
			return false;
		}
		
		String sourceAddress = InetUtils.inet4IntToString(wifiInfo.getIpAddress());
		String destAddress = InetUtils.inet4IntToString(dhcpInfo.ipAddress);
		
		try {
			ProcessBuilder builder = new ProcessBuilder("ping", "-c", pingCount + "", "-i", "0.2", "-I", sourceAddress, "-l", "3", "-U", "-r", "-n", destAddress);
			Log.i(PingDataCollector.class.getCanonicalName(), "Running ping command: " + builder.command());
			Process p = builder.start();
			
			List<String> stdout = consume(p.getInputStream());
			List<String> stderr = consume(p.getErrorStream());
			p.waitFor();
			
			if(!stderr.isEmpty()) {
				Log.d(this.getClass().getCanonicalName(), stderr.toString());
				return false;
			}
			
			List<PingResponse> pings = new ArrayList();
			for(String line : stdout) {
				Matcher matcher = pingRegex.matcher(line);
				if(matcher.find()) {
					int icmpReq = Integer.parseInt(matcher.group(1));
					int ttl = Integer.parseInt(matcher.group(2));
					float time = Float.parseFloat(matcher.group(3));
					
					pings.add(new PingResponse(icmpReq, ttl, time));
				}
			}
			
			cachedData = new PingData(wifiInfo.getSSID(), pingCount, pings);
			
			return true;
		} catch(Exception e) {
			Log.d(this.getClass().getCanonicalName(), e.toString());
			return false;
		}
		
	}public static class PingResponse {
		private final int icmpSeq;
		private final int ttl;
		private final float timeMs;
		
		public PingResponse(int icmpSeq, int ttl, float timeMs) {
			super();
			this.icmpSeq = icmpSeq;
			this.ttl = ttl;
			this.timeMs = timeMs;
		}
		
		public int getIcmpSeq() {
			return icmpSeq;
		}
		
		public int getTtl() {
			return ttl;
		}
		
		public float getTimeMs() {
			return timeMs;
		}
	}
	
	public static class PingData extends WifiSurveyData {
		
		private final int packetsSent;
		private final List<PingResponse> responses;
				
		public PingData(String ssid, int packetsSent, List<PingResponse> responses) {
			super(ssid);
			this.packetsSent = packetsSent;
			this.responses = Collections.unmodifiableList(responses);
		}

		@Override
		public JSONObject toJSONObject() throws JSONException {
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getDataLabel() {
			return "ping";
		}
		
		public String toString() {
			return "total sent= " + packetsSent + ", recieved= " + responses.size() + ", drop rate= " + (1 - ((float)packetsSent) / responses.size());
		}

		public int getPacketsSent() {
			return packetsSent;
		}

		public List<PingResponse> getResponses() {
			return responses;
		}
	}
}
