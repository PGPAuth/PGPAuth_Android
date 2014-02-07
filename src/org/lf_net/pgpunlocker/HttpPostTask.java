package org.lf_net.pgpunlocker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;

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
