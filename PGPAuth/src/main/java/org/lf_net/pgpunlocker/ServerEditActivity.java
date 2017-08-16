package org.lf_net.pgpunlocker;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ServerEditActivity extends Activity {

    EditText _editTextName;
    EditText _editTextURL;

    Server _server;
    int _serverIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_edit);

        _editTextName = (EditText) findViewById(R.id.editTextName);
        _editTextURL = (EditText) findViewById(R.id.editTextURL);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _serverIndex = extras.getInt("ServerIndex");
            _server = ServerManager.serverAtIndex(_serverIndex);
        }

        Uri serverConfig = getIntent().getData();
        if (serverConfig != null) {
            ServerManager.addServer();
            _serverIndex = ServerManager.count() - 1;
            _server = Server.deserializeFromURL(serverConfig.toString());
        }

        if (_server != null) {
            _editTextName.setText(_server.name());
            _editTextURL.setText(_server.url());
        }
    }

    public void saveClicked(View view) {
        _server.setName(_editTextName.getText().toString());
        _server.setUrl(_editTextURL.getText().toString());

        ServerManager.replaceServer(_serverIndex, _server);
        ServerManager.saveToFile(this);

        finish();
    }
}

