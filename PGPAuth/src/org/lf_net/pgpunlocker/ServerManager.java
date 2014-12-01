package org.lf_net.pgpunlocker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ServerManager {
	static public class ServerManagerObserver {
		public void onServerAdded(Server server) {
		}
		
		public void onServerReplaced(int serverIndex, Server newServer) {
		}
		
		public void onServerDeleted(int serverIndex) {
		}
		
		public void onSomethingChanged() {
		}
	}
	
	private static List<Server> _servers = new ArrayList<Server>();
	private static ServerManagerObserver _observer;
	
	public static void loadFromFile(Context context) {
		try {
			_servers.clear();
			
			FileInputStream fileStream = context.openFileInput("Servers");
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
			
			String line;
			
			do
			{
				line = reader.readLine();
				
				if(line == null) {
					break;
				}
				
				Server server = Server.deserialize(line);
				
				if(server != null) {
					_servers.add(server);
					
					if(_observer != null) {
						_observer.onServerAdded(server);
					}
				}
				
			} while (line != null);
			
			reader.close();
			fileStream.close();
			
			if(_observer != null) {
				_observer.onSomethingChanged();
			}
			
		}
		catch (FileNotFoundException e) {
			// maybe there was an older version of PGPAuth installed ...
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String server = prefs.getString("pref_server", "");
			
			if(server != "") {
				// there was an old version \o/
				_servers.add(new Server("Migrated from old version", server));
			}
		}
		catch (Exception e) {
			// shit happens
		}
	}
	
	public static void saveToFile(Context context) {
		FileOutputStream fileStream;
		try {
			fileStream = context.openFileOutput("Servers", Context.MODE_PRIVATE);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileStream));
			
			for(int i = 0; i < _servers.size(); i++) {
				String line = _servers.get(i).serialize();
				writer.write(line);
				
				if(i < _servers.size() - 1) {
					writer.newLine();
				}
			}
			
			writer.close();
			fileStream.close();
			
		} catch (Exception e) {
			// more shit happens
		}
	}
	
	public static int count() {
		return _servers.size();
	}
	
	public static Server serverAtIndex(int index) {
		return _servers.get(index);
	}
	
	public static void addServer() {
		_servers.add(new Server("", ""));
		
		if(_observer != null) {
			_observer.onServerAdded(_servers.get(count() - 1));
			_observer.onSomethingChanged();
		}
	}
	
	public static void replaceServer(int index, Server server) {
		_servers.set(index, server);
		
		if(_observer != null) {
			_observer.onServerReplaced(index, server);
			_observer.onSomethingChanged();
		}
	}
	
	public static void deleteServer(int index) {
		_servers.remove(index);
		
		if(_observer != null) {
			_observer.onServerDeleted(index);
			_observer.onSomethingChanged();
		}
	}
	
	public static void setObserver(ServerManagerObserver observer) {
		_observer = observer;
	}
}
