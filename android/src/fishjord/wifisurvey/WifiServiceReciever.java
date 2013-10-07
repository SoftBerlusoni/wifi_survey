package fishjord.wifisurvey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiServiceReciever extends BroadcastReceiver {

	private String postUrl;
	private Criteria locCriteria;

	public WifiServiceReciever() {
		locCriteria = new Criteria();
		locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
		locCriteria.setAltitudeRequired(true);
		locCriteria.setBearingRequired(false);
		locCriteria.setCostAllowed(false);
		locCriteria.setPowerRequirement(Criteria.POWER_LOW);

		this.postUrl = "https://www.cse.msu.edu/~fishjord/cse824/wifi_survey.php";
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.getBestProvider(locCriteria, true);		

		PostDataTask task = new PostDataTask(postUrl);
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
