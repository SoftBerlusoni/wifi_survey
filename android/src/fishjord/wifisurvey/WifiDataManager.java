package fishjord.wifisurvey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import fishjord.wifisurvey.datacollectors.DataCollector;
import fishjord.wifisurvey.datacollectors.PingDataCollector;
import fishjord.wifisurvey.datacollectors.WifiSurveyData;
import fishjord.wifisurvey.utils.InetUtils;

public class WifiDataManager {

	public static class WifiDataRecord {
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
	
	private List<DataCollector> dataCollectors;

	public WifiDataManager(DataCollector ... collectors) {
		this.dataCollectors = Arrays.asList(collectors);
	}

	public WifiDataRecord refreshData(int scanLevel, long cacheTime) {
		List<WifiSurveyData> data = new ArrayList();
		
		long now = System.currentTimeMillis();		
		for(DataCollector collector : dataCollectors) {
			if(collector.getScanLevel() <= scanLevel) {
				WifiSurveyData surveyData = collector.getLastUpdate();
				if(surveyData == null || now - surveyData.getCollectionDate().getTime() > cacheTime) {
					if(!collector.doRefreshData()) {
						continue;
					}
					surveyData = collector.getLastUpdate();
				}
				
				data.add(surveyData);
			}
		}
		
		return new WifiDataRecord(data, scanLevel);
	}
}
