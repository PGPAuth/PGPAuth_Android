package org.lf_net.pgpunlocker;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity
{	
	Logic _logic;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		setContentView(R.layout.activity_main);
		
		Server servers[] = new Server[] {
			new Server("ChaosChemnitz", "http://door.chch.lan.ffc/cgi-bin/pgpauth_cgi"),
			new Server("PGPAuth Testinstance", "https://lf-net.org/cgi-bin/pgpauth_cgi")
		};
		
		ListView listViewServers = (ListView)findViewById(R.id.listViewServers);
		ServerAdapter adapter = new ServerAdapter(this, R.layout.listview_item_server, servers);
		listViewServers.setAdapter(adapter);
		
		try {
			_logic = new Logic(this, prefs.getBoolean("pref_forceapg", false));
			
			Logic.GuiHelper guiHelper = _logic.new GuiHelper() {
				public void startActivityForResult(Intent intent, int requestCode) {
					MainActivity.this.startActivityForResult(intent, requestCode + 0x0000B000);
				}
				
				public void startIntentSenderForResult(IntentSender intentSender, int requestCode) {
					try {
						MainActivity.this.startIntentSenderForResult(intentSender, requestCode + 0x0000B000, null, 0, 0, 0);
					} catch (SendIntentException e) {
						// just fuck you
					}
				}
			};
			
			_logic.setGuiHelper(guiHelper);
			
			Logic.Logic = _logic;
		}
		catch(Exception e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, 0x0000A001);
			
			Toast.makeText(this, "UngŸltige Einstellungen erkannt. Bitte ŸberprŸfen Sie die Einstellungen.", Toast.LENGTH_LONG).show();
		}
    }
    
    @Override
    public void onDestroy() {
        if(_logic != null) {
        	_logic.close();
        }
        
        super.onDestroy();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	public void menuSettingsClicked(MenuItem item) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivityForResult(intent, 0x0000A001);
	}
	
	public void menuAboutClicked(MenuItem item) {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == 0x0000A001) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
			if(_logic != null) {
				_logic.close();
			}
			
			try {
				_logic = new Logic(this, prefs.getBoolean("pref_forceapg", false));
				Logic.Logic = _logic;
			}
			catch(Exception e) {
				_logic = null;
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivityForResult(intent, 0x0000A001);
				
				Toast.makeText(this, "UngŸltige Einstellungen erkannt. Bitte ŸberprŸfen Sie die Einstellungen.", Toast.LENGTH_LONG).show();
			}
		}
		else if(requestCode > 0x0000B000 && requestCode < 0x0000C0000) {
			_logic.postResult(requestCode - 0x0000B000, resultCode, data);
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}									
	}
	
	public void onOpenClicked(View view) {
		doAction("open");
	}
	
	public void onCloseClicked(View view) {
		doAction("close");
	}
	
	private void doAction(final String action) {
		
		Thread runThread = new Thread(new Runnable() {			
			@Override
			public void run() {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				_logic.doActionOnServer(action, prefs.getString("pref_server", ""), prefs.getString("pref_key", ""));
			}
		});
		
		runThread.start();
	}
}
