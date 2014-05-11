package org.lf_net.pgpunlocker;

import java.io.*;
import java.util.concurrent.ExecutionException;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.*;

import android.app.*;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity
{
	RadioButton _radioButtonClose;
	
	RadioButton _radioButtonOpen;
	
	EditText _editTextDataset;
	
	EditText _editTextSignedData;
	
	ProgressBar _progressBar;
	
	boolean _expertMode;
	
	boolean _useOpenPGPKeyChain;
	
	OpenPgpServiceConnection _serviceConnection;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		_expertMode = prefs.getBoolean("pref_expert", false);
		
		if(_expertMode)
        	setContentView(R.layout.main);
		else
		{
			setContentView(R.layout.easy);
		}
		
		if(!detectApg() && ! detectOpenPGPKeyChain())
		{
			Toast.makeText(getApplicationContext(), getText(R.string.apg_and_opengpg_keychain_not_installed), Toast.LENGTH_LONG).show();
			finish();
		}
		
		_useOpenPGPKeyChain = prefs.getBoolean("pref_openpgpkeychain", detectOpenPGPKeyChain());
		
		if(_useOpenPGPKeyChain)
		{
			_serviceConnection = new OpenPgpServiceConnection(this, "org.sufficientlysecure.keychain");
		    _serviceConnection.bindToService();
		}
		
		_radioButtonClose = (RadioButton)findViewById(R.id.mainRadioButtonActionClose);
		_radioButtonOpen = (RadioButton)findViewById(R.id.mainRadioButtonActionOpen);
		_editTextDataset = (EditText)findViewById(R.id.mainEditTextDataset);
		_editTextSignedData =(EditText)findViewById(R.id.mainEditTextSignedData);
		_progressBar = (ProgressBar)findViewById(R.id.easyProgressBarRunning);
		
		if(_progressBar != null)
			_progressBar.setVisibility(View.INVISIBLE);
    }
    
    @Override
    public void onDestroy() {
        if (_serviceConnection != null) {
            _serviceConnection.unbindFromService();
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
	
	private boolean detectApg() {
		Context context = getApplicationContext();
		
		try {
            PackageInfo pi = context.getPackageManager().getPackageInfo("org.thialfihar.android.apg", 0);
            if (pi.versionCode >= 16) {
                return true;
            } else {
                Toast.makeText(context, getText(R.string.apt_invalid_version),Toast.LENGTH_SHORT).show();
            }
        } catch (NameNotFoundException e) {
            // not found
        }

        return false;
	}
	
	private boolean detectOpenPGPKeyChain() {
		Context context = getApplicationContext();
		
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo("org.sufficientlysecure.keychain", 0);
			
			if(pi.versionCode > 20000) {
				return true;
			}
		} catch(NameNotFoundException e) {
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
			{
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				if(_expertMode != prefs.getBoolean("pref_expert",  false))
				{
					AlertDialog.Builder dialog = new AlertDialog.Builder(this);
					dialog.setMessage(getText(R.string.settings_restart_required));
					dialog.setTitle(getText(R.string.settings_restart_required_title));
					dialog.setPositiveButton(android.R.string.ok ,new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								finish();
							}
						});
					dialog.setCancelable(false);
					dialog.create().show();
				}
				break;
			}
				
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
			
			case 0x0000A003:
                sendOpenKeychainIntent(data);
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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		String prefKey = prefs.getString("pref_key", "");
		
		if(!_useOpenPGPKeyChain) {			
			Intent intent = new Intent("org.thialfihar.android.apg.intent.ENCRYPT_AND_RETURN");
			intent.putExtra("intentVersion", 1);
			intent.setType("text/plain");
			intent.putExtra("text", signData);
			intent.putExtra("ascii_armor", true);
			
			if(prefKey != "")
			{
				intent.putExtra("signatureKeyId", Long.parseLong(prefKey));
			}
			
			startActivityForResult(intent, 0x0000A002);
		} else {
			Intent intent = new Intent();
			intent.setAction(OpenPgpApi.ACTION_SIGN);
			intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
			intent.putExtra("PGPAuth_SIGNDATA", signData);
			
			sendOpenKeychainIntent(intent);
		}		
	}
	
	private void sendOpenKeychainIntent(Intent data)
	{
		InputStream is = new ByteArrayInputStream(data.getExtras().getString("PGPAuth_SIGNDATA").getBytes());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		OpenPgpApi api = new OpenPgpApi(this, _serviceConnection.getService());
		Intent result = api.executeApi(data, is, os);
		
		switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
	    case OpenPgpApi.RESULT_CODE_SUCCESS: {
	    	try {
	    		String signature = os.toString("UTF-8");
	            
	            if(_editTextSignedData != null)
					_editTextSignedData.setText(signature);
				else
					sendToServer(signature);
	        } catch (UnsupportedEncodingException e) {
	            Toast.makeText(this, "UnsupportedEncodingException\n\n" + e.getMessage(), Toast.LENGTH_LONG).show();
	        }
	        break;
	    }
	    case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED: {
	        PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
	        try {
	            startIntentSenderForResult(pi.getIntentSender(), 0x0000A003, null, 0, 0, 0);
	        } catch (IntentSender.SendIntentException e) {
	            Toast.makeText(this, "SendIntentException\n\n" + e.getMessage(), Toast.LENGTH_LONG).show();
	        }
	        break;
	    }
	    case OpenPgpApi.RESULT_CODE_ERROR: {
	        OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
	        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
	        break;
	    }
	}
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
