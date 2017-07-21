package org.lf_net.pgpunlocker;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

public class Server {
    final static String ServerConfigBaseURL = "https://pgpauth.lf-net.org/serverConfig?config=";

    String _name;
    String _url;
    String _apgKey;
    String _openpgpgKey;
    boolean _saveKey;

    public Server(String name, String url) {
        this(name, url, "", "", false);
    }

    public Server(String name, String url, String apgKey, String openpgpgKey, boolean saveKey) {
        _name = name;
        _url = url;
        _apgKey = apgKey;
        _openpgpgKey = openpgpgKey;
        _saveKey = saveKey;
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

    public String openpgpgKey() {
        return _openpgpgKey;
    }

    public boolean saveKey() {
        return _saveKey;
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

    public void setOpenpgpgKey(String openpgpgKey) {
        _openpgpgKey = openpgpgKey;
    }

    public void keepKey() {
        _saveKey = true;
    }

    public void forgetKey() {
        _saveKey = false;
    }

    public boolean isEmpty() {
        return _name == "" && _url == "";
    }

    @Deprecated
    public static Server deserialize(String serialized) {
        String[] parts = serialized.split("\t");

        if (parts.length == 2) {
            return new Server(parts[0], parts[1]);
        }

        if (parts.length == 5) {
            return new Server(parts[0], parts[1], parts[2], parts[3], Boolean.parseBoolean(parts[4]));
        }

        return null;
    }

    public JSONObject serializeJSON() {
        JSONObject ret = new JSONObject();

        try {
            ret.put("name", _name);
            ret.put("url", _url);
            ret.put("apgKey", _apgKey);
            if (saveKey()) {
                ret.put("openpgpgKey", _openpgpgKey);
            }
            ret.put("saveKey", _saveKey);
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
            boolean saveTheKey = obj.getBoolean("saveKey");
            String openpgpgKey = "";
            if (saveTheKey) {
                openpgpgKey = obj.getString("openpgpgKey");
            }
            return new Server(name, url, apgKey, openpgpgKey, saveTheKey);
        } catch (JSONException e) {
            // should not happen as we just serialize a bunch of strings
            throw e;
        }
    }

    public String serializeForURL() {
        JSONObject ret = new JSONObject();

        try {
            ret.put("name", _name);
            ret.put("url", _url);

            String jsonString = ret.toString();
            String encoded = URLEncoder.encode(jsonString, "utf-8");

            return ServerConfigBaseURL + encoded;
        } catch (Exception e) {
            return null;
        }
    }

    public static Server deserializeFromURL(String configUrl) {
        try {
            String encoded = configUrl.substring(ServerConfigBaseURL.length());
            String jsonString = URLDecoder.decode(encoded, "utf-8");

            JSONObject obj = new JSONObject(jsonString);

            String name = obj.getString("name");
            String url = obj.getString("url");

            return new Server(name, url);
        } catch (Exception e) {
            return null;
        }
    }
}
