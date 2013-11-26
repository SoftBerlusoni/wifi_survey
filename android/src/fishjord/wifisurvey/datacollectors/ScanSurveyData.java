package fishjord.wifisurvey.datacollectors;

import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.ScanResult;

public class ScanSurveyData extends WifiSurveyData {
	private final List<ScanResult> scanResults;
	
	public ScanSurveyData(List<ScanResult> scanResults) {
		super(null);
		this.scanResults = Collections.unmodifiableList(scanResults);
	}
	
	public String getDataLabel() {
		return "scan";
	}
	
	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		for(ScanResult result : scanResults) {
			if(result.SSID.length() == 0) {
				ret.append("\n").append(result.BSSID).append("=").append(result.level);
			} else {
				ret.append("\n").append(result.SSID).append("=").append(result.level);					
			}
		}
					
		return ret.substring(1);
	}
	
	public List<ScanResult> getScanResults() {
		return scanResults;
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONArray aps = new JSONArray();
		
		for(ScanResult result : scanResults) {
			JSONObject ap = new JSONObject();

			ap.put("ssid", result.SSID);
			ap.put("bssid", result.BSSID);
			ap.put("rssi", result.level);
			//ap.put("timestamp", result.timestamp);
			ap.put("freq", result.frequency);
			ap.put("capabilities", result.capabilities);
			
			aps.put(ap);
		}

		JSONObject ret = new JSONObject();
		ret.put("access_points", aps);
		
		return ret;
	}
}