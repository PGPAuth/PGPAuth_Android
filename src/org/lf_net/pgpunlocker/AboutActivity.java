package org.lf_net.pgpunlocker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}

	public void onWebsiteClick(View view) {
		Intent showWebsite = new Intent(Intent.ACTION_VIEW);
		showWebsite.setData(Uri.parse((String)view.getTag()));
		startActivity(showWebsite);
	}
}
