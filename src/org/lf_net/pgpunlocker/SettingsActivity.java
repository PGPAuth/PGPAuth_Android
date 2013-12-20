
package org.lf_net.pgpunlocker;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.util.*;
import android.content.*;
import android.preference.*;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
