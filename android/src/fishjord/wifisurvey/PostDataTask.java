package fishjord.wifisurvey;

import java.util.Collection;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.util.Log;

public class PostDataTask extends AsyncTask<WifiSurvey, Void, Void> {
	private final DefaultHttpClient httpClient = new DefaultHttpClient();
	private final Lock lock = new ReentrantLock();
	private final String postUrl;

	public PostDataTask(String postUrl) {
		this.postUrl = postUrl;
	}

	private static JSONArray locationToJSON(Collection<Location> locations)
			throws JSONException {
		JSONArray array = new JSONArray();

		for (Location location : locations) {
			if (location == null) {
				continue;
			}
			JSONObject loc = new JSONObject();
			loc.put("lat", location.getLatitude());
			loc.put("long", location.getLongitude());
			loc.put("acc", location.getAccuracy());

			if (location.hasAltitude()) {
				loc.put("alt", location.getAltitude());
			}

			if (location.hasBearing()) {
				loc.put("bearing", location.getBearing());
			}

			if (location.hasSpeed()) {
				loc.put("speed", location.getSpeed());
			}

			loc.put("last_fix", new Date(location.getTime()));
			loc.put("comp_time", location.getElapsedRealtimeNanos());
			loc.put("provider", location.getProvider());
			array.put(loc);
		}

		return array;
	}

	private static JSONObject surveyToJSON(WifiSurvey survey)
			throws JSONException {
		final JSONObject wrapper = new JSONObject();
		final JSONArray scanResults = new JSONArray();

		wrapper.put("scan_time", new Date());
		wrapper.put("scan_results", scanResults);
		wrapper.put("loc", locationToJSON(survey.getLocation()));

		for (ScanResult result : survey.getScanResults()) {
			JSONObject net = new JSONObject();

			net.put("ssid", result.SSID);
			net.put("bssid", result.BSSID);
			net.put("freq", result.frequency);
			net.put("level", result.level);
			net.put("cap", result.capabilities);
			net.put("timestamp", result.timestamp);

			scanResults.put(net);
		}

		return wrapper;
	}

	@Override
	protected Void doInBackground(WifiSurvey... surveys) {
		lock.lock();
		try {
			for (WifiSurvey survey : surveys) {
				HttpPut putReq = new HttpPut(postUrl);
				StringEntity entity = new StringEntity(surveyToJSON(survey)
						.toString());
				entity.setContentType("application/json;charset=UTF-8");
				entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
						"application/json;charset=UTF-8"));
				putReq.setEntity(entity);

				Log.d(this.getClass().getCanonicalName(), "Sending request");
				HttpResponse response = httpClient.execute(putReq);
				Log.d(this.getClass().getCanonicalName(), "Status line: "
						+ response.getStatusLine());
				Log.d(this.getClass().getCanonicalName(),
						EntityUtils.toString(response.getEntity()));
			}

		} catch (Exception e) {
			Log.e(this.getClass().getCanonicalName(), "Failed to post data", e);
		} finally {
			lock.unlock();
		}

		return null;
	}

}
