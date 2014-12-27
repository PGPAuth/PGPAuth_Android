package org.lf_net.pgpunlocker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
	
	OpenPgpServiceConnection _serviceConnection = null;
	Context _context;
	String _signedData = null;
	AutoResetEvent _event = new AutoResetEvent(false);
	GuiHelper _guiHelper;
	
	public static Logic Logic;
	
	public abstract class GuiHelper {
		public abstract void startActivityForResult(Intent intent, int requestCode);
		
		public abstract void startIntentSenderForResult(IntentSender intentSender, int requestCode);
		
		public abstract void showUserFeedback(String feedback);
	}
	
	public Logic(Context context, boolean forceAPG) {
		_context = context;
		
		boolean hasAPG = detectApg();
		boolean hasOpenKeychain = detectOpenKeyChain();
		
		if(hasOpenKeychain && !forceAPG) {
			_serviceConnection = new OpenPgpServiceConnection(context, "org.sufficientlysecure.keychain");
		    _serviceConnection.bindToService();
		}
		
		if(!hasAPG && forceAPG) {
			throw new RuntimeException((String)context.getText(R.string.apg_forced_but_not_installed));
		}
		
		if(!hasAPG && !hasOpenKeychain) {
			throw new RuntimeException((String)context.getText(R.string.apg_and_opengpg_keychain_not_installed));
		}
		
		_event = new AutoResetEvent(false);
	}
	
	public void close() {
		if (_serviceConnection != null && _serviceConnection.isBound()) {
            _serviceConnection.unbindFromService();
        }
	}
	
	public void doActionOnServerWithFeedback(String action, String server, String keyAPG) {
		String ret = doActionOnServer(action, server, keyAPG);
		_guiHelper.showUserFeedback(ret);
	}
	
	public String doActionOnServer(String action, String server, String keyAPG) {
		if(server == "" || !URLUtil.isValidUrl(server))
		{
			if(server == "") {
				return _context.getString(R.string.no_server_set);
			}
			
			else if(!URLUtil.isValidUrl(server)) {
				return _context.getString(R.string.invalid_server_set);
			}
			
			else {
				return _context.getString(R.string.unknown_error);
			}
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
			
			_guiHelper.startActivityForResult(intent, 1);
		}
		else {
			Intent intent = new Intent();
			intent.setAction(OpenPgpApi.ACTION_SIGN);
			intent.putExtra(OpenPgpApi.EXTRA_REQUEST_ASCII_ARMOR, true);
			intent.putExtra("PGPAuth_SIGNDATA", request);
			
			try {
				sendOpenKeychainIntent(intent);
			} catch (Exception e) {
				return e.getLocalizedMessage();
			}
		}
		
		try {
			_event.waitOne();
		} catch (InterruptedException e1) {
			return e1.getLocalizedMessage();
		}
		
		if(_signedData == null) {
			return _context.getString(R.string.unknown_error);
		}
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost req = new HttpPost(server);
			List<NameValuePair> httpParams = new ArrayList<NameValuePair>();
			httpParams.add(new BasicNameValuePair("data", _signedData));
			req.setEntity(new UrlEncodedFormEntity(httpParams));

			HttpResponse response = client.execute(req);
			
			if(response.getStatusLine().getStatusCode() != 200) {
				return response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
			}
			else {
				return _context.getResources().getString(android.R.string.ok);
			}
		}
		catch(Exception e) {
			return e.getMessage();
		}
		finally {
			_signedData = null;
			_event.reset();
		}
	}
	
	public void postResult(int requestCode, int resultCode, Intent data) {
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
					sendOpenKeychainIntent(data);
				} catch (Exception e) {
					_signedData = null;
					_event.set();
				}
				break;
			}
		}
	}
	
	public void setGuiHelper(GuiHelper guiHelper) {
		_guiHelper = guiHelper;
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
	
	private void sendOpenKeychainIntent(Intent data) throws Exception
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
		        _guiHelper.startIntentSenderForResult(pi.getIntentSender(), 2);
		        break;
		    }
		    case OpenPgpApi.RESULT_CODE_ERROR: {
		        OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
		        throw new RuntimeException(error.getMessage());
		    }
		}
	}
}
