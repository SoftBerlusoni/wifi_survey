package fishjord.wifisurvey.datacollectors;

import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fishjord.wifisurvey.client.ConnectionTestClient.ConnectionTestPacket;

public class LatencyData extends WifiSurveyData {
	
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