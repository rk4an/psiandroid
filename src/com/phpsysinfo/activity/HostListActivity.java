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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.phpsysinfo.R;

public class HostListActivity extends SherlockActivity implements OnItemClickListener,
OnItemLongClickListener, OnClickListener {

	List<String> listStringUrls = null;
	ListView listViewUrls = null;
	ArrayAdapter<String> arrayAdapterUrlList = null;
	int pos = 0;

	private JSONArray hostsJsonArray = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hosts_view);

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

		try {
			String dataStore = pref.getString(PSIConfig.HOSTS_JSON_STORE, "");

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

		//FIXME: select the current URL
		String currentHost = pref.getString(PSIConfig.JSON_CURRENT_HOST, "");
		String currentHostUrl = "";
		try {
			JSONTokener tokener = new JSONTokener(currentHost);
			JSONObject sHost = new JSONObject(tokener);
			currentHostUrl = sHost.getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		for(int i = 0; i<listStringUrls.size(); i++) {
			if(listStringUrls.get(i).equals(currentHostUrl)) {
				listViewUrls.setItemChecked(i, true);
			}
		}
		
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
				JSONArray temp = new JSONArray();
				for (int i = 0; i < hostsJsonArray.length() && !find; i++) {
					try {
						if (i != pos) {
							temp.put(hostsJsonArray.get(i));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				hostsJsonArray = temp;

				arrayAdapterUrlList.remove((String) listViewUrls
						.getItemAtPosition(pos));

				saveList();
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				
				//get json at position pos
				
				JSONObject editHost = null;
				try {
					editHost = (JSONObject) hostsJsonArray.get(pos);
					
					((EditText) findViewById(R.id.txtUrl)).setText(
							editHost.get("url").toString());
					((EditText) findViewById(R.id.txtUser)).setText(
							editHost.get("username").toString());
					((EditText) findViewById(R.id.txtPassword)).setText(
							editHost.get("password").toString());
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
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
		AlertDialog.Builder adb = new AlertDialog.Builder(HostListActivity.this);
		adb.setMessage("Action for " + listViewUrls.getItemAtPosition(position)
				+ " ?");
		adb.setPositiveButton("Remove", dialogClickListener);
		adb.setNegativeButton("Cancel", dialogClickListener);
		adb.setNeutralButton("Edit", dialogClickListener);
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

			//clear
			txtUrl.setText("http://");
			txtUser.setText("");
			txtPasword.setText("");
			
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

		Editor editor = pref.edit();
		editor.putString(PSIConfig.HOSTS_JSON_STORE, dataStore);
		editor.commit();
	}

}
