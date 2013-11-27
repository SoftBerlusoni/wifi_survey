package fishjord.wifisurvey.datacollectors;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class WifiSurveyData {
	private final Date collectionDate;
	private final String ssid;
	
	public WifiSurveyData(String ssid) {
		super();
		this.collectionDate = new Date();
		this.ssid = ssid;
	}

	public Date getCollectionDate() {
		return collectionDate;
	}

	public String getSsid() {
		return ssid;
	}
	
	public abstract String getDataLabel();
	
	public abstract String toString();
	
	public abstract JSONObject toJSONObject() throws JSONException;
}
