package org.lf_net.pgpunlocker;

import org.json.JSONException;
import org.json.JSONObject;

public class Server {
	String _name;
	String _url;
	String _apgKey;
	
	public Server(String name, String url) {
		this(name, url, "");
	}
	
	public Server(String name, String url, String apgKey) {
		_name = name;
		_url = url;
		_apgKey = apgKey;
	}
	
	public String name() {
		return _name;
	}
	
	public String url() {
		return _url;
	}
	
	public String apgKey() {
		return _apgKey;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public void setUrl(String url) {
		_url = url;
	}
	
	public void setApgKey(String apgKey) {
		_apgKey = apgKey;
	}
	
	@Deprecated
	public static Server deserialize(String serialized) {
		String[] parts = serialized.split("\t");
		
		if(parts.length == 2) {
			return new Server(parts[0], parts[1]);
		}
		
		if(parts.length == 3) {
			return new Server(parts[0], parts[1], parts[2]);
		}
		
		return null;
	}
	
	public JSONObject serializeJSON() {
		JSONObject ret = new JSONObject();
		
		try {
			ret.put("name", _name);
			ret.put("url", _url);
			ret.put("apgKey", _apgKey);
		} catch (JSONException e) {
			// should not happen as we just serialize a bunch of strings
			return null;
		}
		
		return ret;
	}
	
	public static Server deserializeJSON(JSONObject obj) throws JSONException {
		try {
			String name = obj.getString("name");
			String url = obj.getString("url");
			String apgKey = obj.getString("apgKey");
			
			return new Server(name, url, apgKey);
		} catch (JSONException e) {
			// should not happen as we just serialize a bunch of strings
			throw e;
		}
	}
}
