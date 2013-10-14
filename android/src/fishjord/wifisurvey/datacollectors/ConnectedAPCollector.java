package fishjord.wifisurvey.datacollectors;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import fishjord.wifisurvey.ScanLevel;
import fishjord.wifisurvey.utils.InetUtils;

public class ConnectedAPCollector extends DataCollector {
	
	private APData cachedData;

	public ConnectedAPCollector(WifiManager wifiManager) {
		super(wifiManager);
	}

	@Override
	public WifiSurveyData getLastUpdate() {
		return cachedData;
	}

	@Override
	public int getScanLevel() {
		return ScanLevel.PASSIVE;
	}

	@Override
	public boolean doRefreshData() {
		if(!wifiManager.isWifiEnabled() || wifiManager.getConnectionInfo() == null) {
			return false;
		}
		
		cachedData = new APData(wifiManager.getConnectionInfo());
		
		return true;
	}

	public static class APData extends WifiSurveyData {
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
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
