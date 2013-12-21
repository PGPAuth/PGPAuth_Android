package org.lf_net.pgpunlocker;

import android.content.*;
import android.os.*;
import android.preference.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.apache.http.client.entity.*;

public class HttpPostTask extends AsyncTask<String,Long,String>
{
	@Override
	protected String doInBackground(String... texts)
	{
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost req = new HttpPost(texts[0]);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("data", texts[1]));
			req.setEntity(new UrlEncodedFormEntity(params));

			HttpResponse response = client.execute(req);

			if(response.getStatusLine().getStatusCode() != 200) {
				return response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();
			}
		}
		catch (IOException e) {
			return e.toString();
		}
		
		return null;
	}
}
