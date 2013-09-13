package fishjord.wifisurvey;

import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	public static class WifiScanListener extends BroadcastReceiver {
		private final WifiManager wifiManager;
		private final DefaultHttpClient httpClient = new DefaultHttpClient();

		public WifiScanListener(WifiManager manager) {
			this.wifiManager = manager;
		}

		@Override
		public void onReceive(Context context, Intent arg1) {
			final JSONObject wrapper = new JSONObject();
			final JSONArray scanResults = new JSONArray();

			try {
				wrapper.put("scan_time", new Date());
				wrapper.put("scan_results", scanResults);
			} catch(JSONException ignore) {}
			
			for (ScanResult result : wifiManager.getScanResults()) {
				try {
					JSONObject net = new JSONObject();

					net.put("ssid", result.SSID);
					net.put("bssid", result.BSSID);
					net.put("freq", result.frequency);
					net.put("level", result.level);
					net.put("cap", result.capabilities);
					net.put("timestamp", result.timestamp);
					net.put("normalized",
							wifiManager.calculateSignalLevel(result.level, 100));

					scanResults.put(net);
				} catch (JSONException ignore) {

				}
			}

			AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {

				@Override
				protected Void doInBackground(String... params) {
					try {
						HttpPut putReq = new HttpPut(
								"https://www.cse.msu.edu/~fishjord/cse824/wifi_survey.php");
						StringEntity entity = new StringEntity(wrapper.toString());
						entity.setContentType("application/json;charset=UTF-8");
						entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
								"application/json;charset=UTF-8"));
						putReq.setEntity(entity);
						
						Log.d(this.getClass().getCanonicalName(), "Sending request");
						HttpResponse response = httpClient.execute(putReq);
						Log.d(this.getClass().getCanonicalName(), "Status line: " + response.getStatusLine());
						Log.d(this.getClass().getCanonicalName(), EntityUtils.toString(response.getEntity()));
						
					} catch (Exception e) {
						Log.d(this.getClass().getCanonicalName(),
								"Failed to post data", e);
					}
					return null;
				}
				
			};
			
			task.execute("https://www.cse.msu.edu/~fishjord/cse824/wifi_survey.php");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final WifiManager wifiManager = (WifiManager) this
				.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		IntentFilter intent = new IntentFilter();
		intent.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		this.registerReceiver(new WifiScanListener(wifiManager), intent);

		Button scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				wifiManager.startScan();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
