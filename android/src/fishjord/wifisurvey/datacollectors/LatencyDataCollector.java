package fishjord.wifisurvey.datacollectors;

import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import fishjord.wifisurvey.ScanLevel;
import fishjord.wifisurvey.client.ConnectionTestClient;
import fishjord.wifisurvey.client.ConnectionTestClient.ConnectionTestPacket;

public class LatencyDataCollector extends DataCollector {
	
	private final short pingCount;
	private static final ConnectionTestClient connTest = new ConnectionTestClient();
	private LatencyData cachedData;

	public LatencyDataCollector(WifiManager wifiManager, short pingCount) {
		super(wifiManager);
		this.pingCount = pingCount;
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
		
		try {
			List<ConnectionTestPacket> result = connTest.runConnectionTest("pacific.cse.msu.edu", pingCount);
			cachedData = new LatencyData(wifiInfo.getSSID(), pingCount, result);
			Log.d(this.getClass().getCanonicalName(), cachedData.toString());
		} catch(Exception e){
			Log.d(this.getClass().getCanonicalName(), "Error when running latency test " + e);
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static class LatencyData extends WifiSurveyData {
		
		private final int packetsSent;
		private final List<ConnectionTestPacket> responses;
				
		public LatencyData(String ssid, int packetsSent, List<ConnectionTestPacket> responses) {
			super(ssid);
			this.packetsSent = packetsSent;
			this.responses = Collections.unmodifiableList(responses);
		}

		@Override
		public JSONObject toJSONObject() throws JSONException {
			JSONObject ret = new JSONObject();
			ret.put("sent", packetsSent);

			JSONArray jsonResponses = new JSONArray();
			for(ConnectionTestPacket response : responses) {
				JSONObject pingResponse = new JSONObject();
				pingResponse.put("packet_num", response.packetNum);
				pingResponse.put("rtt", response.rtt);
				jsonResponses.put(pingResponse);
			}
			
			ret.put("responses", jsonResponses);
			
			return ret;
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

		public List<ConnectionTestPacket> getResponses() {
			return responses;
		}
	}
}
