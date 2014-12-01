package org.lf_net.pgpunlocker;

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
	
	public String serialize() {
		String serialized = _name + "\t" + _url;
		
		if(_apgKey != null && _apgKey.length() > 0) {
			serialized += "\t" + _apgKey;
		}
		
		return serialized;
	}
	
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
}
