package com.phpsysinfo.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.phpsysinfo.R;

public class PSIPreferencesActivity extends PreferenceActivity {

		@Override
		public void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.prefs);
		}
}
