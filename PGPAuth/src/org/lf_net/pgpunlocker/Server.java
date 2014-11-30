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
}
