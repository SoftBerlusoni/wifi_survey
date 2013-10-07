package fishjord.wifisurvey.activities;

import fishjord.wifisurvey.R;
import fishjord.wifisurvey.R.id;
import fishjord.wifisurvey.R.layout;
import fishjord.wifisurvey.R.menu;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
