package com.phpsysinfo.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.phpsysinfo.R;

public class PSIUrlActivity extends Activity implements OnItemClickListener,
OnItemLongClickListener, OnClickListener {

	List<String> listStringUrls = null;
	ListView listViewUrls = null;
	ArrayAdapter<String> arrayAdapterUrlList = null;
	int pos = 0;

	private JSONArray hostsJsonArray = null;

	final public static String HOSTS_JSON_STORE = "HOSTS_JSON_STORE";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.url);

		listViewUrls = (ListView) findViewById(R.id.listView1);
		listViewUrls.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listViewUrls.setSelection(0);
		listViewUrls.setOnItemClickListener(this);
		listViewUrls.setOnItemLongClickListener(this);

		listStringUrls = new ArrayList<String>();

		Button btnAdd = (Button) findViewById(R.id.btnAdd);
		btnAdd.setOnClickListener(this);

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		String dataStore = "";
		try {
			dataStore = pref.getString(HOSTS_JSON_STORE, "");
			Log.d("LOAD",dataStore);
			if (dataStore.equals("")) {
				hostsJsonArray = new JSONArray();
			}
			else {
				JSONTokener tokener = new JSONTokener(dataStore);
				hostsJsonArray = new JSONArray(tokener);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < hostsJsonArray.length(); i++) {
			try {
				String url = ((JSONObject)hostsJsonArray.get(i)).getString("url");
				if (!url.equals("")) {
					listStringUrls.add(url);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		arrayAdapterUrlList = new ArrayAdapter<String>(this, R.layout.mylist,
				listStringUrls);
		listViewUrls.setAdapter(arrayAdapterUrlList);

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {

		Intent i = new Intent();
		try {
			i.putExtra("host", hostsJsonArray.get(position).toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		setResult(Activity.RESULT_OK, i);
		finish();
	}

	/**
	 * 
	 */
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				// delete item and save the list

				boolean find = false;
				for (int i = 0; i < hostsJsonArray.length() && !find; i++) {
					try {
						if (((JSONObject)hostsJsonArray
								.get(i))
								.getString("url")
								.equals((String) listViewUrls
										.getItemAtPosition(pos))) {
							//TODO: hostList.remove(i);

							find = true;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				arrayAdapterUrlList.remove((String) listViewUrls
						.getItemAtPosition(pos));

				saveList();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};

	@Override
	public boolean onItemLongClick(AdapterView<?> a, View v, int position,
			long id) {
		pos = position;

		// create and display the remove dialog
		AlertDialog.Builder adb = new AlertDialog.Builder(PSIUrlActivity.this);
		adb.setMessage("Remove " + listViewUrls.getItemAtPosition(position)
				+ " ?");
		adb.setPositiveButton("Yes", dialogClickListener);
		adb.setNegativeButton("No", dialogClickListener);
		adb.show();

		return true;
	}

	@Override
	public void onClick(View arg0) {
		EditText txtUrl = (EditText) findViewById(R.id.txtUrl);
		EditText txtUser = (EditText) findViewById(R.id.txtUser);
		EditText txtPasword = (EditText) findViewById(R.id.txtPassword);

		// add URL to the list
		if (!txtUrl.getText().toString().equals("")) {
			arrayAdapterUrlList.add(txtUrl.getText().toString());

			try {
				JSONObject host = new JSONObject();
				host.put("url", txtUrl.getText().toString());
				host.put("username", txtUser.getText().toString());
				host.put("password", txtPasword.getText().toString());
					
				hostsJsonArray.put(host);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}

			saveList();
		}
	}

	/**
	 * save the list into a string in the preference
	 */
	public void saveList() {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		String dataStore = "";
		try {
			dataStore = hostsJsonArray.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.d("SAVE",dataStore);
		Editor editor = pref.edit();
		editor.putString(HOSTS_JSON_STORE, dataStore);
		editor.commit();
	}

}
