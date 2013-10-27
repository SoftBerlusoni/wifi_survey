package fishjord.wifisurvey.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import fishjord.wifisurvey.R;
import fishjord.wifisurvey.ScanLevel;
import fishjord.wifisurvey.WifiDataManager;
import fishjord.wifisurvey.WifiDataManager.WifiDataRecord;
import fishjord.wifisurvey.datacollectors.WifiScanCollector;
import fishjord.wifisurvey.datacollectors.WifiSurveyData;
import fishjord.wifisurvey.tasks.TrainingUploadTask;

public class TrainingActivity extends Activity {

	private WifiDataManager dataManager;
	private final UUID trainingId = UUID.randomUUID();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training);

		WifiManager wifiManager = (WifiManager) this.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);

		dataManager = new WifiDataManager(new WifiScanCollector(wifiManager,
				MainActivity.scanLock));
		PreferenceManager.setDefaultValues(this.getApplicationContext(),
				R.xml.wifi_survey_preferences, false);

		EditText locNumber = (EditText) this.findViewById(R.id.location_number);
		locNumber.setText("1");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.training, menu);
		return true;
	}

	public void showPreferences(View view) {
		Intent intent = new Intent();
		intent.setClass(this, PreferencesActivity.class);
		startActivityForResult(intent, 0);
	}

	public void collectTrainingSample(View view) {
		final WifiDataManager finalDataManager = dataManager;
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(view.getContext());
		final int NUM_TRAINING = Integer.valueOf(prefs.getString(
				"training_count", ""));

		final EditText locNumber = (EditText) this
				.findViewById(R.id.location_number);
		final ProgressBar progress = (ProgressBar) findViewById(R.id.sampling_progress);
		progress.setMax(NUM_TRAINING);
		progress.setProgress(0);

		final int location = Integer.valueOf(locNumber.getText().toString());
		Log.d(this.getClass().getCanonicalName(),
				"Starting taking training readings for location " + location
						+ " and sending to " + prefs.getString("base_url", ""));
		final TrainingUploadTask uploadTask = new TrainingUploadTask(prefs.getString("base_url", "")
						+ "/upload_training.php", location, trainingId);

		AsyncTask<Void, Void, List<WifiDataRecord>> task = new AsyncTask<Void, Void, List<WifiDataRecord>>() {
			
			private boolean sendOk;

			@Override
			protected List<WifiDataRecord> doInBackground(Void... arg0) {
				List<WifiDataRecord> ret = new ArrayList<WifiDataRecord>();
				for (int index = 0; index < NUM_TRAINING; index++) {
					Log.d(this.getClass().getCanonicalName(),
							"Training sample " + index);
					progress.setProgress(index + 1);
					// dialog.setMessage("Training sample " + (index + 1));
					ret.add(dataManager.refreshData(ScanLevel.TRAINING, 0));
				}
				sendOk = uploadTask.go(ret.toArray(new WifiDataRecord[ret.size()]));
				return ret;
			}

			protected void onPostExecute(List<WifiDataRecord> results) {
				TextView textView = (TextView) findViewById(R.id.textView1);
				textView.setMovementMethod(new ScrollingMovementMethod());
				StringBuilder builder = new StringBuilder();
				builder.append("Current training data\n----------------------\n\n");

				for (WifiDataRecord result : results) {
					builder.append("Scan time: " + result.getScanTime())
							.append("\n\n");

					for (WifiSurveyData data : result.getScanResults()) {
						builder.append(
								data.getDataLabel() + ": " + data.toString())
								.append("\n\n");
					}
				}

				textView.setText(uploadTask.getErrorMessage() + "\n\n" + builder.toString());
				if (sendOk) {
					locNumber.setText((location + 1) + "");
				}
			}
		};

		task.execute();
	}

}
