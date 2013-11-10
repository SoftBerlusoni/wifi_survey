package fishjord.wifisurvey.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import fishjord.wifisurvey.R;
import fishjord.wifisurvey.ScanManager;
import fishjord.wifisurvey.ScanManager.ScanResultUpdate;
import fishjord.wifisurvey.WifiDataRecord;
import fishjord.wifisurvey.datacollectors.WifiSurveyData;

public class MainActivity extends Activity implements
		OnSharedPreferenceChangeListener, ScanResultUpdate {
	private ScanManager scanManager;
	private WifiManager wifiManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		wifiManager = (WifiManager) this.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		
		PreferenceManager.setDefaultValues(this.getApplicationContext(),
				R.xml.wifi_survey_preferences, true);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		
		scanManager = new ScanManager(wifiManager, prefs.getString("base_url",
				"") + "/upload.php", Integer.valueOf(prefs.getString("ping_count", "-1")),
				prefs.getString("ping_host", ""),
				Long.valueOf(prefs.getString("delay", "600000")));
		scanManager.setScanResultUpdateListener(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.registerReceiver(scanManager, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		wifiManager.startScan();
	}

	public void onNewScan(final WifiDataRecord result) {
		this.runOnUiThread(new Runnable() {

			public void run() {
				TextView textView = (TextView) findViewById(R.id.textView1);
				textView.setMovementMethod(new ScrollingMovementMethod());
				StringBuilder builder = new StringBuilder();
				builder.append("Current scan data\n----------------------\n\n");
				builder.append("Scan time: " + result.getScanTime()).append(
						"\n\n");

				for (WifiSurveyData data : result.getScanResults()) {
					builder.append(data.getDataLabel() + ": " + data.toString())
							.append("\n\n");
				}

				textView.setText(builder.toString());
			}
		});

	}

	public void refreshReadings(View view) {
		uploadData(view);
	}

	public void uploadData(View view) {
		wifiManager.startScan();
	}

	public void showPreferences(View view) {
		Intent intent = new Intent();
		intent.setClass(this, PreferencesActivity.class);
		startActivityForResult(intent, 0);
	}

	public void showTrainingActivity(View view) {
		Intent intent = new Intent();
		intent.setClass(this, TrainingActivity.class);
		startActivityForResult(intent, 0);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.i(this.getClass().getCanonicalName(), key + " changed");
		if (key.equals("base_url")) {
			scanManager.setPostUrl(prefs.getString("base_url", "")
					+ "/upload.php");
		} else if (key.equals("ping_count")) {
			scanManager.setPingCount(Integer.valueOf(prefs.getString("ping_count", "-1")));
		} else if (key.equals("ping_host")) {
			scanManager.setPingHost(prefs.getString("ping_host", ""));
		} else if (key.equals("delay")) {
			scanManager.setTaskDelay(Long.valueOf(prefs.getString("delay", "")));
		}
	}

}
