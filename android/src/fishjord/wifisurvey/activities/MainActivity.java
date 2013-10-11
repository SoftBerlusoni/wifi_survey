package fishjord.wifisurvey.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import fishjord.wifisurvey.R;
import fishjord.wifisurvey.WifiServiceReciever;

public class MainActivity extends Activity {
	
	private final WifiServiceReciever receiver = new WifiServiceReciever();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final WifiManager wifiManager = (WifiManager) this
				.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		Button scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				wifiManager.startScan();
			}
		});
		
		Button settings = (Button) findViewById(R.id.settings);
		settings.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				v.getContext().startActivity(new Intent("fishjord.wifisurvey.PREFS"));
			}
		});
		
		this.registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		
	}
	
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		this.unregisterReceiver(receiver);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
