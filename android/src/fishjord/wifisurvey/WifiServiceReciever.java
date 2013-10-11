package fishjord.wifisurvey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import fishjord.wifisurvey.tasks.PostDataTask;

public class WifiServiceReciever extends BroadcastReceiver {

	private Criteria locCriteria;

	public WifiServiceReciever() {
		Log.i(this.getClass().getCanonicalName(), "Created wifi service reciever");
		locCriteria = new Criteria();
		locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		locCriteria.setAltitudeRequired(true);
		locCriteria.setBearingRequired(false);
		locCriteria.setCostAllowed(false);
		locCriteria.setPowerRequirement(Criteria.POWER_LOW);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.getBestProvider(locCriteria, true);		
		
		SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(context.getString(R.string.pref_bundle_name), 0);

		Log.i(this.getClass().getCanonicalName(), "Posting to " + context.getString(R.string.server_url) + "/wifi_survey.php");
		PostDataTask task = new PostDataTask(prefs.getString(context.getString(R.string.server_url) + "/wifi_survey.php", ""));
		List<ScanResult> scanResults = wifiManager.getScanResults();
		List<Location> locations = new ArrayList();
		Set<String> providers = new HashSet();
		
		for(String provider : locationManager.getProviders(true)) {
			Location loc = locationManager.getLastKnownLocation(provider);
			if(loc != null && !providers.contains(loc.getProvider())) {
				locations.add(loc);
				providers.add(loc.getProvider());
			}
		}

		task.execute(new WifiSurvey(scanResults, locations));
	}

}
