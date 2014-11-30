package org.lf_net.pgpunlocker;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class ServerAdapter extends ArrayAdapter<Server> {
	Context _context;
	int _layoutResourceId;
	Server _data[] = null;
	
	public ServerAdapter(Context context, int layoutResourceId, Server[] data) {
		super(context, layoutResourceId, data);
		
		_context = context;
		_layoutResourceId = layoutResourceId;
		_data = data;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ServerHolder holder = null;
		
		if(row == null) {
			LayoutInflater inflater = ((Activity)_context).getLayoutInflater();
			row = inflater.inflate(_layoutResourceId, parent, false);
			
			holder = new ServerHolder();
			holder.nameView = (TextView)row.findViewById(R.id.textViewServerName);
			holder.urlView = (TextView)row.findViewById(R.id.textViewServerURL);
			holder.lockButton = (Button)row.findViewById(R.id.buttonLock);
			holder.unlockButton = (Button)row.findViewById(R.id.buttonUnlock);
			
			row.setTag(holder);
		}
		else {
			holder = (ServerHolder)row.getTag();
		}
		
		Server server = _data[position];
		holder.nameView.setText(server.name());
		holder.urlView.setText(server.url());
		holder.lockButton.setTag(server);
		holder.unlockButton.setTag(server);
		
		holder.lockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Server server = (Server)v.getTag();
				Logic.Logic.doActionOnServer("close", server.url(), server.apgKey());
			}
		});
		
		holder.unlockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Server server = (Server)v.getTag();
				Logic.Logic.doActionOnServer("open", server.url(), server.apgKey());
			}
		});
		
		return row;
	}
	
	public static class ServerHolder
	{
		TextView nameView;
		TextView urlView;
		Button lockButton;
		Button unlockButton;
	}
}
