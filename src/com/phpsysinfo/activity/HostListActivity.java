package com.phpsysinfo.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

		listViewUrls = (ListView) findViewById(R.id.lvHostsList);
		listViewUrls.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listViewUrls.setSelection(0);
		listViewUrls.setOnItemClickListener(this);
		listViewUrls.setOnItemLongClickListener(this);

		listStringUrls = new ArrayList<String>();

		Button btnAdd = (Button) findViewById(R.id.btnAdd);
		btnAdd.setOnClickListener(this);

		hostsJsonArray = PSIConfig.getInstance().loadHostsList();
	
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

		arrayAdapterUrlList = new ArrayAdapter<String>(this, R.layout.hosts_list,
				listStringUrls);
		listViewUrls.setAdapter(arrayAdapterUrlList);

		
		int lastIndex = PSIConfig.getInstance().loadLastIndex();
		
		try {
			hostsJsonArray.get(lastIndex);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		listViewUrls.setItemChecked(lastIndex, true);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {

		Intent i = new Intent();
		try {
			i.putExtra("host", position);
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

				PSIConfig.getInstance().saveList(hostsJsonArray);
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
		adb.setMessage(getString(R.string.lblActionFor, listViewUrls.getItemAtPosition(position)));
		adb.setPositiveButton(getString(R.string.lblRemove), dialogClickListener);
		adb.setNegativeButton(getString(R.string.lblCancel), dialogClickListener);
		adb.setNeutralButton(getString(R.string.lblEdit), dialogClickListener);
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
			
			PSIConfig.getInstance().saveList(hostsJsonArray);
		}
	}

}
