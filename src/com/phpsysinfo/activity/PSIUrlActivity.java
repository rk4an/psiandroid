package com.phpsysinfo.activity;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

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
import com.phpsysinfo.xml.Host;
import com.phpsysinfo.xml.Hosts;

public class PSIUrlActivity extends Activity implements OnItemClickListener,
OnItemLongClickListener, OnClickListener {

	List<String> listStringUrls = null;
	ListView listViewUrls = null;
	ArrayAdapter<String> arrayAdapterUrlList = null;
	int pos = 0;

	private ObjectMapper objectMapper = null;

	private Hosts hostList = null;

	final private String JSON_HOSTS = "JSON_HOSTS";

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

		objectMapper = new ObjectMapper();

		String data = "";
		try {
			data = pref.getString(JSON_HOSTS, "");
			if (data.equals("")) {
				hostList = new Hosts();
			}

			hostList = objectMapper.readValue(data, Hosts.class);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		for (int i = 0; i < hostList.getHost().size(); i++) {
			if (!hostList.getHost().get(i).getUrl().equals("")) {
				listStringUrls.add(hostList.getHost().get(i).getUrl());
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
			i.putExtra("host", objectMapper.writeValueAsString(hostList.getHost().get(position)));
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
				for (int i = 0; i < hostList.getHost().size() && !find; i++) {
					if (hostList
							.getHost()
							.get(i)
							.getUrl()
							.equals((String) listViewUrls
									.getItemAtPosition(pos))) {
						hostList.getHost().remove(i);
						find = true;
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

			Host psiHostUrl = new Host(txtUrl.getText().toString(), txtUser
					.getText().toString(), txtPasword.getText().toString());
			hostList.getHost().add(psiHostUrl);

			saveList();
		}
	}

	/**
	 * save the list into a string in the preference
	 */
	public void saveList() {

		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		String data = "";
		try {
			data = objectMapper.writeValueAsString(hostList);
		} catch (Exception e) {

			e.printStackTrace();
		}

		Editor editor = pref.edit();
		editor.putString(JSON_HOSTS, data);
		editor.commit();
	}

}
