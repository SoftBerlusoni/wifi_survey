package fishjord.wifisurvey.tasks;

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

import android.os.AsyncTask;
import android.util.Log;
import fishjord.wifisurvey.R;
import fishjord.wifisurvey.WifiDataManager.WifiDataRecord;
import fishjord.wifisurvey.datacollectors.WifiSurveyData;

public class UploadTask extends AsyncTask<WifiDataRecord, Void, Void> {

	private final String url;

	public UploadTask(String targetUrl) {
		this.url = targetUrl;
	}

	private JSONObject toJSON(WifiDataRecord... params) throws JSONException {
		JSONObject trainingData = new JSONObject();
		JSONArray trainingSamples = new JSONArray();

		trainingData.put("time", new Date());
		
		for(WifiDataRecord record : params) {
			JSONObject entry = new JSONObject();
			entry.put("scan_time", record.getScanTime());
			entry.put("scan_level", record.getScanLevel());
			for(WifiSurveyData data : record.getScanResults()) {
				entry.put(data.getDataLabel(), data.toJSONObject());
			}
			trainingSamples.put(entry);
		}
		
		trainingData.put("samples", trainingSamples);

		return trainingData;
	}

	@Override
	protected Void doInBackground(WifiDataRecord... params) {
		try {
			JSONObject toSend = toJSON(params);
			Log.d(this.getClass().getCanonicalName(), "sending " + toSend);

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPut putReq = new HttpPut(url);
			StringEntity entity = new StringEntity(toSend.toString());
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
			
		} catch(Exception e) {
			Log.d(this.getClass().getCanonicalName(), "sending failed " + e.toString());			
		}

		return null;
	}
}
