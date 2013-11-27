package fishjord.wifisurvey;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import fishjord.wifisurvey.datacollectors.WifiSurveyData;

public class WifiDataRecord {
	private final List<WifiSurveyData> scanResults;
	private final int scanLevel;
	private final Date scanTime = new Date();
	
	public WifiDataRecord(List<WifiSurveyData> data,
			int scanLevel) {			
		this.scanResults = Collections.unmodifiableList(data);
		this.scanLevel = scanLevel;
	}

	public List<WifiSurveyData> getScanResults() {
		return scanResults;
	}

	public Date getScanTime() {
		return scanTime;
	}
	
	public int getScanLevel() {
		return scanLevel;
	}
}