package org.lf_net.pgpunlocker;

import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.preference.*;
import android.util.AttributeSet;

//Why I'm using a RingtonePreference? Because all the others don't have onActivityResult ...
//http://yenliangl.blogspot.de/2011/04/preference-that-receives-activity.html
public class PGPKeyPreference extends RingtonePreference {

	Context _context;
	
	public PGPKeyPreference(Context context) {
		super(context);
		_context = context;
	}
	
	public PGPKeyPreference(Context context, AttributeSet attr) {
		super(context, attr);
		_context = context;
	}
	
	public PGPKeyPreference(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
		_context = context;
	}

	@Override
	protected void onPrepareRingtonePickerIntent(Intent intent) {
		// Remove all extras already placed by RingtonePreference
        intent.setAction(null);
        intent.removeExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI);
        intent.removeExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT);
        intent.removeExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI);
        intent.removeExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT);
        intent.removeExtra(RingtoneManager.EXTRA_RINGTONE_TYPE);
        
        intent.setAction("org.thialfihar.android.apg.intent.SELECT_SECRET_KEY");
        intent.putExtra("intentVersion", 1);
	}
	
	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if(super.onActivityResult(requestCode, resultCode, data)) {
			if(data != null && data.hasExtra("keyId")) {
				persistString(String.valueOf(data.getLongExtra("keyId", 0)));
			}
		}
		
		return false;
	}
	
}
