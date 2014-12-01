package org.lf_net.pgpunlocker;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ServerDeleteActivity extends Activity {

	int _serverIndex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_delete);
		
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			_serverIndex = extras.getInt("ServerIndex");
		}
	}
	
	public void onDeleteClicked(View view) {
		ServerManager.deleteServer(_serverIndex);
		finish();
	}
	
	public void onCancelClicked(View view) {
		finish();
	}
}
