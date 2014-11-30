package org.lf_net.pgpunlocker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.webkit.URLUtil;

public class Logic {
	
	OpenPgpServiceConnection _serviceConnection;
	Context _context;
	String _signedData;
	AutoResetEvent _event;
	
	public abstract class GuiHelper {
		public abstract void startActivityForResult(Intent intent, int requestCode);
		
		public abstract void startIntentSenderForResult(IntentSender intentSender, int requestCode);
	}
	
	public Logic(Context context, boolean forceAPG) {
		_context = context;
		
		boolean hasAPG = detectApg();
		boolean hasOpenKeychain = detectOpenKeyChain();
		
		if(hasOpenKeychain && !forceAPG) {
			_serviceConnection = new OpenPgpServiceConnection(context, "org.sufficientlysecure.keychain");
		    _serviceConnection.bindToService();
		}
		
		if(!hasAPG && !hasOpenKeychain) {
			throw new RuntimeException((String)context.getText(R.string.apg_and_opengpg_keychain_not_installed));
		}
		
		_event = new AutoResetEvent(false);
	}
	
	public void close() {
		if (_serviceConnection != null) {
            _serviceConnection.unbindFromService();
        }
	}
	
	public String doActionOnServer(String action, String server, String keyAPG, GuiHelper helper) {
		if(server == null || server == "" || !URLUtil.isValidUrl(server))
		{
			throw new IllegalArgumentException();
		}
		
		long timestamp = System.currentTimeMillis() / 1000;
		String request = action + ":" + timestamp;
				
		if(_serviceConnection == null) {
			Intent intent = new Intent("org.thialfihar.android.apg.intent.ENCRYPT_AND_RETURN");
			intent.putExtra("intentVersion", 1);
			intent.setType("text/plain");
			intent.putExtra("text", request);
			intent.putExtra("ascii_armor", true);
			
			if(keyAPG != "")
			{
				intent.putExtra("signatureKeyId", Long.parseLong(keyAPG));
			}
			
			helper.startActivityForResult(intent, 1);
		}
		else {
			Intent intent = new Intent();
			intent.setAction(OpenPgpApi.ACTION_SIGN);
			intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
			intent.putExtra("PGPAuth_SIGNDATA", request);
			
			try {
				sendOpenKeychainIntent(intent, helper);
			} catch (UnsupportedEncodingException e) {
				return e.getLocalizedMessage();
			}
		}
		
		try {
			_event.waitOne();
		} catch (InterruptedException e1) {
			return e1.getLocalizedMessage();
		}
		
		if(_signedData == null) {
			return "Unknown error while signing the request!";
		}
		
		String[] params = new String[]{server, _signedData};
		
		HttpPostTask task = new HttpPostTask();
		try {
			String ret = task.execute(params).get();
			
			if(ret == null) {
				ret = _context.getResources().getString(android.R.string.ok);
			}
			
			return ret;
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
	public void postResult(int requestCode, int resultCode, Intent data, GuiHelper helper) {
		switch(requestCode) {
			case 1:
			{
				if(data != null && data.hasExtra("encryptedMessage"))
				{
					_signedData = data.getStringExtra("encryptedMessage");
				}
				else
				{
					_signedData = null;
				}
				
				_event.set();
				
				break;
			}
			case 2:
			{
				try {
					sendOpenKeychainIntent(data, helper);
				} catch (UnsupportedEncodingException e) {
					_signedData = null;
					_event.set();
				}
			}
		}
	}
	
	private boolean detectApg() {		
		try {
            PackageInfo pi = _context.getPackageManager().getPackageInfo("org.thialfihar.android.apg", 0);
            if (pi.versionCode >= 16) {
                return true;
            }
        } catch (NameNotFoundException e) {
            // not found
        }

        return false;
	}
	
	private boolean detectOpenKeyChain() {		
		try {
			PackageInfo pi = _context.getPackageManager().getPackageInfo("org.sufficientlysecure.keychain", 0);
			
			if(pi.versionCode > 20000) {
				return true;
			}
		} catch(NameNotFoundException e) {
			// not found
		}
		
		return false;
	}
	
	private void sendOpenKeychainIntent(Intent data, GuiHelper helper) throws UnsupportedEncodingException
	{
		InputStream is = new ByteArrayInputStream(data.getExtras().getString("PGPAuth_SIGNDATA").getBytes());
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		OpenPgpApi api = new OpenPgpApi(_context, _serviceConnection.getService());
		Intent result = api.executeApi(data, is, os);
		
		switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
		    case OpenPgpApi.RESULT_CODE_SUCCESS: {
		    	_signedData = os.toString("UTF-8");
		    	_event.set();
		    	break;
		    }
		    case OpenPgpApi.RESULT_CODE_USER_INTERACTION_REQUIRED: {
		        PendingIntent pi = result.getParcelableExtra(OpenPgpApi.RESULT_INTENT);
		        helper.startIntentSenderForResult(pi.getIntentSender(), 2);
		        break;
		    }
		    case OpenPgpApi.RESULT_CODE_ERROR: {
		        OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
		        throw new RuntimeException(error.getMessage());
		    }
		}
	}
}
