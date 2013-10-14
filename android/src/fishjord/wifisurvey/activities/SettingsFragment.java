package fishjord.wifisurvey.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import fishjord.wifisurvey.R;

public class SettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.wifi_survey_preferences);
	}
}
