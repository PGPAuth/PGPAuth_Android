package org.lf_net.pgpunlocker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ServerAdapter extends ArrayAdapter<Server> {
	Context _context;
	int _layoutResourceId;
	
	public ServerAdapter(Context context, int layoutResourceId) {
		super(context, layoutResourceId);
		
		_context = context;
		_layoutResourceId = layoutResourceId;
		
		ServerManager.setObserver(new ServerManager.ServerManagerObserver() {
			@Override
			public void onSomethingChanged() {
				ServerAdapter.this.notifyDataSetChanged();
			}
		});
	}
	
	@Override
	public int getCount() {
		return ServerManager.count();
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
			row.setClickable(true);
			row.setLongClickable(true);
		}
		else {
			holder = (ServerHolder)row.getTag();
		}
		
		Server server = ServerManager.serverAtIndex(position);
		holder.nameView.setText(server.name());
		holder.urlView.setText(server.url());
		holder.lockButton.setTag(server);
		holder.unlockButton.setTag(server);
		holder.index = position;
		
		holder.lockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Server server = (Server)v.getTag();
				
				Thread runThread = new Thread(new Runnable() {			
					@Override
					public void run() {
						Logic.Logic.doActionOnServerWithFeedback("close", server.url(), server.apgKey());
					}
				});
				
				runThread.start();
			}
		});
		
		holder.unlockButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Server server = (Server)v.getTag();
				
				Thread runThread = new Thread(new Runnable() {			
					@Override
					public void run() {
						Logic.Logic.doActionOnServerWithFeedback("open", server.url(), server.apgKey());
					}
				});
				
				runThread.start();
			}
		});
		
		row.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ServerHolder holder = (ServerHolder)v.getTag();
				
				Intent i = new Intent(_context, ServerEditActivity.class);
				i.putExtra("ServerIndex", holder.index);
				_context.startActivity(i);
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
		
		int index;
	}
}
