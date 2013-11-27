package fishjord.wifisurvey.datacollectors;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import fishjord.wifisurvey.ScanLevel;

public class WifiScanCollector {	
	public static class ScanLock extends BroadcastReceiver{
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
	
	private WifiManager wifiManager;
	private WifiSurveyData cachedData;
	private final ScanLock scanLock;
	
	public WifiScanCollector(WifiManager wifiManager, ScanLock scanLock) {
		this.wifiManager = wifiManager;
		this.scanLock = scanLock;
	}
	
	public boolean doRefreshData() {
		try {
			scanLock.runSerialScan(this.wifiManager);
		} catch(Exception ignore) {
			return false;
		}
		
		cachedData = new ScanSurveyData(wifiManager.getScanResults());
		return true;
	}
	
	public WifiSurveyData getScanData() {
		return cachedData;
	}
}
