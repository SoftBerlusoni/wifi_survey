package fishjord.wifisurvey.activities;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import fishjord.wifisurvey.R;
import fishjord.wifisurvey.ScanLevel;
import fishjord.wifisurvey.WifiDataManager;
import fishjord.wifisurvey.WifiDataManager.WifiDataRecord;
import fishjord.wifisurvey.datacollectors.ConnectedAPCollector;
import fishjord.wifisurvey.datacollectors.PingDataCollector;
import fishjord.wifisurvey.datacollectors.WifiScanCollector;
import fishjord.wifisurvey.datacollectors.WifiScanCollector.ScanManager;
import fishjord.wifisurvey.datacollectors.WifiSurveyData;

public class MainActivity extends Activity implements
		OnSharedPreferenceChangeListener {

	private WifiDataManager dataManager;
	private WifiDataRecord lastRecord;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ScanManager scanLock = new ScanManager();
		
		WifiManager wifiManager = (WifiManager) this.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		this.registerReceiver(scanLock, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.wifi_survey_preferences, false);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		dataManager = new WifiDataManager(
				new WifiScanCollector(wifiManager, scanLock),
				new ConnectedAPCollector(wifiManager),
				new PingDataCollector(wifiManager, 10)
				);
	}

	public void refreshReadings(View view) {
		Log.d(this.getClass().getCanonicalName(), "Starting readings refresh");
		final WifiDataManager finalDataManager = dataManager;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());

		final int scanLevel = Integer.parseInt(prefs.getString("mode", ScanLevel.OFF + ""));
		final long cacheTime = Integer.parseInt(prefs.getString("delay", "1000"));
		final ProgressDialog dialog = ProgressDialog.show(this, "", "Refreshing...", true, false);
		
		AsyncTask<Void, Void, WifiDataRecord> task = new AsyncTask<Void, Void, WifiDataRecord>() {

			@Override
			protected WifiDataRecord doInBackground(Void... arg0) {
				return finalDataManager.refreshData(ScanLevel.ACTIVE, 0);
			}

			protected void onPostExecute(WifiDataRecord result) {
				dialog.dismiss();
				TextView textView = (TextView) findViewById(R.id.textView1);
				textView.setMovementMethod(new ScrollingMovementMethod());
				StringBuilder builder = new StringBuilder();
				builder.append("Current scan data\n----------------------\n\n");
				builder.append("Scan Level: ").append(scanLevel).append("\n");
				builder.append("Max cache time: " + cacheTime / 1000).append("\n");
				builder.append("Scan time: " + result.getScanTime()).append("\n\n");
				
				for(WifiSurveyData data : result.getScanResults()) {
					builder.append(data.getDataLabel() + ": " + data.toString()).append("\n\n");
				}
				
				textView.setText(builder.toString());
			}
		};

		task.execute();
	}

	public void uploadData(View view) {
		Log.d(this.getClass().getCanonicalName(), "Starting upload");
		final WifiDataManager finalDataManager = dataManager;
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Log.d(this.getClass().getCanonicalName(), "URL: " + prefs.getString(
				getString(R.string.server_url), ""));
		Log.d(this.getClass().getCanonicalName(), "URL: " + getString(R.string.server_url));
		
		AsyncTask<Void, Void, WifiDataRecord> task = new AsyncTask<Void, Void, WifiDataRecord>() {

			@Override
			protected WifiDataRecord doInBackground(Void... arg0) {
				WifiDataRecord record = null;

				try {
					DefaultHttpClient httpClient = new DefaultHttpClient();
					HttpPut putReq = new HttpPut(prefs.getString(
							getString(R.string.server_url), ""));
					StringEntity entity = new StringEntity(record.toString());
					entity.setContentType("text/plain;charset=UTF-8");
					entity.setContentEncoding(new BasicHeader(
							HTTP.CONTENT_TYPE, "text/plain;charset=UTF-8"));
					putReq.setEntity(entity);

					Log.d(this.getClass().getCanonicalName(), "Sending request");
					HttpResponse response = httpClient.execute(putReq);
					Log.d(this.getClass().getCanonicalName(), "Status line: "
							+ response.getStatusLine());
					Log.d(this.getClass().getCanonicalName(),
							EntityUtils.toString(response.getEntity()));
				} catch (Exception e) {
					Log.e(this.getClass().getCanonicalName(), e.toString());
				}
				return record;
			}

			protected void onPostExecute(WifiDataRecord result) {
				Log.i(this.getClass().getCanonicalName(), result.toString());
				TextView textView = (TextView) findViewById(R.id.textView1);
				textView.setText(result.toString());
			}
		};
		
		task.execute();
	}

	public void showPreferences(View view) {
		Intent intent = new Intent();
		intent.setClass(this, PreferencesActivity.class);
		startActivityForResult(intent, 0);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.i(this.getClass().getCanonicalName(), key + " changed");

	}

}
