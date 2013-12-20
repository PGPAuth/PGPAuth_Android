package org.lf_net.pgpunlocker;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.util.*;
import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.*;
import java.util.concurrent.*;

public class MainActivity extends Activity
{
	RadioButton _radioButtonClose;
	
	RadioButton _radioButtonOpen;
	
	EditText _editTextDataset;
	
	EditText _editTextSignedData;
	
	ProgressBar _progressBar;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		
		if(!detectApg())
		{
			Toast.makeText(getApplicationContext(), "APG muss installiert sein!", Toast.LENGTH_LONG).show();
			finish();
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		if(prefs.getBoolean("pref_expert", false))
        	setContentView(R.layout.main);
		else
			setContentView(R.layout.easy);
		
		_radioButtonClose = (RadioButton)findViewById(R.id.mainRadioButtonActionClose);
		_radioButtonOpen = (RadioButton)findViewById(R.id.mainRadioButtonActionOpen);
		_editTextDataset = (EditText)findViewById(R.id.mainEditTextDataset);
		_editTextSignedData =(EditText)findViewById(R.id.mainEditTextSignedData);
		_progressBar = (ProgressBar)findViewById(R.id.easyProgressBarRunning);
		
		if(_progressBar != null)
			_progressBar.setVisibility(View.INVISIBLE);
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
	
	private boolean detectApg() {
		Context context = getApplicationContext();
		
		try {
            PackageInfo pi = context.getPackageManager().getPackageInfo("org.thialfihar.android.apg", 0);
            if (pi.versionCode >= 16) {
                return true;
            } else {
                Toast.makeText(context, "Unsupported APG version!",Toast.LENGTH_SHORT).show();
            }
        } catch (NameNotFoundException e) {
            // not found
        }

        return false;
	}
	
	public void actionOpenClicked(View view) {
		_radioButtonClose.setChecked(false);
	}
	
	public void actionCloseClicked(View view) {
		_radioButtonOpen.setChecked(false);
	}
	
	public void generateDatasetClicked(View view) {
		String action;
		
		if(_radioButtonOpen.isChecked())
			action = "open";
		else
			action = "close";
			
		_editTextDataset.setText(makeRequest(action));
	}
	
	public void signClicked(View view) {
		makeSignature(_editTextDataset.getText().toString());
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
			case 0x0000A001:
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.setMessage("Einige Einstellungen erfordern einen Neustart der App.");
				dialog.setPositiveButton("OK",new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish(); 
						}
					});
				dialog.create().show();
				break;
				
			case 0x0000A002:
				String signedData;
				if(data != null && data.hasExtra("encryptedMessage"))
					signedData = data.getStringExtra("encryptedMessage");
				else
					signedData = "";
				
				if(_editTextSignedData != null)
					_editTextSignedData.setText(signedData);
				else
					sendToServer(signedData);
				break;
			
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}									
	}
	
	public void sendToServerClicked(View view) {
		String data = _editTextSignedData.getText().toString();
		sendToServer(data);
	}
	
	public void onOpenClicked(View view) {
		_progressBar.setVisibility(View.VISIBLE);
		makeSignature(makeRequest("open"));
	}
	
	public void onCloseClicked(View view) {
		_progressBar.setVisibility(View.VISIBLE);
		makeSignature(makeRequest("close"));
	}
	
	private String makeRequest(String action) {
		long ts = System.currentTimeMillis() / 1000;
		return action + ":" + String.valueOf(ts);
	}
	
	private void makeSignature(String signData) {
		Intent intent = new Intent("org.thialfihar.android.apg.intent.ENCRYPT_AND_RETURN");
		intent.putExtra("intentVersion", 1);
		intent.setType("text/plain");
		intent.putExtra("text", signData);
		startActivityForResult(intent, 0x0000A002);
	}
	
	private void sendToServer(String data) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		String server = prefs.getString("pref_server", "");
		String[] params = new String[]{server, data};

		HttpPostTask task = new HttpPostTask();
		try {
			Toast.makeText(getApplicationContext(), task.execute(params).get(), Toast.LENGTH_LONG).show();
		}
		catch(InterruptedException e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
		catch(ExecutionException e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
		
		if(_progressBar != null)
			_progressBar.setVisibility(View.INVISIBLE);
	}
}
