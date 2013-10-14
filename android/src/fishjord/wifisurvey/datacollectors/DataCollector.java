package fishjord.wifisurvey.datacollectors;

import android.net.wifi.WifiManager;


public abstract class DataCollector {
	protected final WifiManager wifiManager;
	
	public DataCollector(WifiManager wifiManager) {
		this.wifiManager = wifiManager;
	}
	
	public abstract WifiSurveyData getLastUpdate();
	
	public abstract int getScanLevel();
	
	public abstract boolean doRefreshData();
}
