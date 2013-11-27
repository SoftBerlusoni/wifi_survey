package fishjord.wifisurvey.datacollectors;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiInfo;
import fishjord.wifisurvey.utils.InetUtils;

public class APData extends WifiSurveyData {
	private final WifiInfo info;

	public APData(WifiInfo info) {
		super(info.getSSID());
		this.info = info;
	}
	
	public String getDataLabel() {
		return "ap";
	}
	
	@Override
	public String toString() {
		if(info == null) {
			return "na";
		}
		return "ssid=" + info.getSSID() + " rssi=" + info.getRssi() + " link speed=" + info.getLinkSpeed() + " ipaddr=" + InetUtils.inet4IntToString(info.getIpAddress()) + " mac=" + info.getMacAddress();
	}

	@Override
	public JSONObject toJSONObject() throws JSONException {
		JSONObject ret = new JSONObject();
		
		ret.put("ssid", info.getSSID());
		ret.put("bssid", info.getBSSID());
		ret.put("rssi", info.getRssi());
		ret.put("hidden_ssid", info.getHiddenSSID());
		ret.put("link_speed", info.getLinkSpeed());
		
		return ret;
	}
	
}