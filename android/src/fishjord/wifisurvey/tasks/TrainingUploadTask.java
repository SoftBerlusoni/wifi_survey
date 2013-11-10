package fishjord.wifisurvey.tasks;

import java.util.Date;
import java.util.UUID;

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
import fishjord.wifisurvey.WifiDataRecord;
import fishjord.wifisurvey.datacollectors.WifiSurveyData;

public class TrainingUploadTask extends AsyncTask<WifiDataRecord, Void, Boolean> {

	private final String url;
	private final int location;
	private String errorMessage;
	private UUID trainingId;

	public TrainingUploadTask(String targetUrl, int location, UUID trainingId) {
		this.url = targetUrl;
		this.location = location;
		this.trainingId = trainingId;
	}

	private JSONObject toJSON(WifiDataRecord... params) throws JSONException {
		JSONObject trainingData = new JSONObject();
		JSONArray trainingSamples = new JSONArray();

		trainingData.put("type", "training_sample");
		trainingData.put("location", location);
		trainingData.put("time", new Date());
		trainingData.put("training_session_id", trainingId);
		
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

	public Boolean go(WifiDataRecord... params) {
		return doInBackground(params);
	}
	
	@Override
	protected Boolean doInBackground(WifiDataRecord... params) {
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
			
			if(response.getStatusLine().getStatusCode() != 200) {
				throw new Exception(response.getStatusLine().toString());
			}
			
			this.errorMessage = response.getStatusLine().toString();
			
		} catch(Exception e) {
			Log.d(this.getClass().getCanonicalName(), "sending failed " + e.toString());
			this.errorMessage = e.toString();
			return false;
		}

		return true;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
}
