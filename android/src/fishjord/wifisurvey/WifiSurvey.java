package fishjord.wifisurvey;

import java.util.Collection;

import android.location.Location;
import android.net.wifi.ScanResult;

public class WifiSurvey {
	private Collection<ScanResult> scanResults;
	private Collection<Location> location;
	
	public WifiSurvey(Collection<ScanResult> scanResults, Collection<Location> location) {
		super();
		this.scanResults = scanResults;
		this.location = location;
	}

	public Collection<ScanResult> getScanResults() {
		return scanResults;
	}
	
	public Collection<Location> getLocation() {
		return location;
	}
}
