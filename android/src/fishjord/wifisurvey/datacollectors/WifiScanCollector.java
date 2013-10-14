package fishjord.wifisurvey.datacollectors;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import fishjord.wifisurvey.ScanLevel;

public class WifiScanCollector extends DataCollector {
	private WifiSurveyData cachedData;
	private final ScanManager scanLock;
	
	public static class ScanManager extends BroadcastReceiver{
		private boolean scanDone;
		private Lock lock = new ReentrantLock();
		private static final long scanTimeout = 30 * 1000; //30 seconds

		public void runSerialScan(WifiManager manager) throws InterruptedException {
			lock.lock();
			try {
				scanDone = false;
			} finally {
				lock.unlock();
			}
			
			if(!manager.startScan()) {
				throw new IllegalArgumentException("Unable to start scan");
			}
			
			long startTime = System.currentTimeMillis();
			while(true) {
				lock.lock();
				try {
					if(scanDone) {
						break;
					}
				} finally {
					lock.unlock();
				}
				
				if(System.currentTimeMillis() - startTime > scanTimeout) {
					throw new InterruptedException();
				}
			}
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			lock.lock();
			try {
				scanDone = true;
			} finally {
				lock.unlock();
			}
		}
		
	}
	
	public WifiScanCollector(WifiManager wifiManager, ScanManager scanLock) {
		super(wifiManager);
		this.scanLock = scanLock;
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
		try {
			scanLock.runSerialScan(this.wifiManager);
		} catch(Exception ignore) {
			return false;
		}
		
		cachedData = new ScanSurveyData(null, wifiManager.getScanResults());
		return true;
	}

	public static class ScanSurveyData extends WifiSurveyData {
		private final List<ScanResult> scanResults;
		
		public ScanSurveyData(String ssid, List<ScanResult> scanResults) {
			super(ssid);
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
			return null;
		}
	}
}
