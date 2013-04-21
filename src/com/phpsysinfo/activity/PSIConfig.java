package com.phpsysinfo.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PSIConfig {

	public final static String SCRIPT_NAME = "/xml.php?plugin=complete";
	public final static String HOSTS_JSON_STORE = "HOSTS_JSON_STORE";
	public final static String LAST_INDEX = "LAST_INDEX";
	public final static int MEMORY_SOFT_THR = 80;
	public final static int MEMORY_HARD_THR = 90;

	private static PSIConfig instance = null;

	public static PSIConfig getInstance() {
		if (instance == null) {
			instance = new PSIConfig();
		}
		return instance;
	}


	private PSIConfig() {

	}


	/**
	 * 
	 * @return
	 */
	public JSONArray loadHostsList() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(PSIActivity.getAppContext());

		JSONArray hostsList = null;
		try {
			String dataStore = pref.getString(PSIConfig.HOSTS_JSON_STORE, "");

			if (dataStore.equals("")) {
				hostsList = new JSONArray();
			}
			else {
				JSONTokener tokener = new JSONTokener(dataStore);
				hostsList = new JSONArray(tokener);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return hostsList;
	}


	/**
	 * 
	 * @param hostsList
	 */
	public void saveList(JSONArray hostsList) {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(PSIActivity.getAppContext());

		String dataStore = "";
		try {
			dataStore = hostsList.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Editor editor = pref.edit();
		editor.putString(PSIConfig.HOSTS_JSON_STORE, dataStore);
		editor.commit();
	}

	/**
	 * 
	 * @return
	 */
	public int loadLastIndex() {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(PSIActivity.getAppContext());

		int index = pref.getInt(PSIConfig.LAST_INDEX, 0);

		return index;
	}
	
	public void saveLastIndex(int index) {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(PSIActivity.getAppContext());
		
		//save new selected host
		Editor editor = pref.edit();
		editor.putInt(PSIConfig.LAST_INDEX, index);
		editor.commit();
	}
}
