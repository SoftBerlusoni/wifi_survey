package fishjord.wifisurvey.activities;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import fishjord.wifisurvey.R;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.wifi_survey_preferences);
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs,
			String key) {
		if (getString(R.string.server_url).equals(key)) {
			Log.i(this.getClass().getCanonicalName(), "Server url changed to " + sharedPrefs.getString(key, ""));
		} else if (getString(R.string.survey_mode).equals(key)) {
			Log.i(this.getClass().getCanonicalName(), "Survey mode changed to " + sharedPrefs.getString(key, ""));

		}
	}
}
