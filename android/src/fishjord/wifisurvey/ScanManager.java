package fishjord.wifisurvey;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import fishjord.wifisurvey.client.ConnectionTestClient;
import fishjord.wifisurvey.client.ConnectionTestClient.ConnectionTestPacket;
import fishjord.wifisurvey.datacollectors.APData;
import fishjord.wifisurvey.datacollectors.LatencyData;
import fishjord.wifisurvey.datacollectors.ScanSurveyData;
import fishjord.wifisurvey.datacollectors.WifiSurveyData;
import fishjord.wifisurvey.tasks.UploadTask;

public class ScanManager extends BroadcastReceiver {

	public static interface ScanResultUpdate {
		void onNewScan(WifiDataRecord newData);
	}

	private WifiManager wifiManager;
	private static final ConnectionTestClient connTest = new ConnectionTestClient();
	private ScanResultUpdate listener = null;

	private Timer timer = new Timer();
	private short pingCount;
	private String pingHost;
	private String postUrl;
	private long taskDelay;

	private Lock lock = new ReentrantLock();

	public ScanManager(WifiManager wifiManager, String postUrl, int pingCount,
			String pingHost, long taskDelay) {
		this.wifiManager = wifiManager;
		this.pingCount = (short) pingCount;
		this.pingHost = pingHost;
		this.postUrl = postUrl;
		this.taskDelay = taskDelay;
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				runAtInterval();
			}
		}, taskDelay);
	}

	public void setScanResultUpdateListener(ScanResultUpdate listener) {
		this.listener = listener;
	}

	public void setPingCount(int pingCount) {
		this.pingCount = (short) pingCount;
	}

	public void setPingHost(String pingHost) {
		this.pingHost = pingHost;
	}

	public void setPostUrl(String postUrl) {
		this.postUrl = postUrl;
	}

	public void setTaskDelay(long taskDelay) {
		this.taskDelay = taskDelay;
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				forceScan();
				return null;
			}
		}.execute();
	}

	public void forceScan() {
		lock.lock();
		try {
			updateInternalNotThreadSafe();
		} finally {
			lock.unlock();
		}
	}

	private WifiDataRecord scan() {
		int scanLevel = ScanLevel.PASSIVE;
		List<WifiSurveyData> ret = new ArrayList();
		ret.add(new ScanSurveyData(wifiManager.getScanResults()));

		if (wifiManager.isWifiEnabled()
				&& wifiManager.getConnectionInfo() != null) {
			ret.add(new APData(wifiManager.getConnectionInfo()));
			scanLevel++;

			DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();

			if (dhcpInfo != null && wifiInfo != null) {
				try {
					List<ConnectionTestPacket> result = connTest
							.runConnectionTest(pingHost, pingCount);
					ret.add(new LatencyData(wifiInfo.getSSID(), pingCount,
							result));
					scanLevel++;
				} catch (Exception e) {
					Log.d(this.getClass().getCanonicalName(),
							"Error when running latency test " + e);
					e.printStackTrace();
				}
			}
		}

		return new WifiDataRecord(ret, scanLevel);
	}

	private void update(WifiDataRecord data) {
		UploadTask task = new UploadTask(postUrl);
		task.doUploadNow(data);
	}

	private void updateInternalNotThreadSafe() {
		final WifiDataRecord data = scan();
		if (listener != null) {
			listener.onNewScan(data);
		}
		update(data);
	}

	public void runAtInterval() {
		if (lock.tryLock()) {
			try {
				updateInternalNotThreadSafe();
			} finally {
				lock.unlock();
			}
		}
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				runAtInterval();
			}
		}, taskDelay);
	}

}
