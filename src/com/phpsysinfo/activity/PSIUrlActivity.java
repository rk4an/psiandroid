package com.phpsysinfo.activity;

import java.util.ArrayList;
import java.util.List;

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

import com.phpsysinfo.R;

public class PSIUrlActivity extends Activity implements OnItemClickListener, OnItemLongClickListener, OnClickListener {

	List<String> urls = null;
	ListView lvUrl = null;
	ArrayAdapter<String> aaUrls = null;
	int pos = 0;
	String selectedUrl = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.url);

		lvUrl = (ListView) findViewById(R.id.listView1);
		lvUrl.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lvUrl.setSelection(0);
		lvUrl.setOnItemClickListener(this);
		lvUrl.setOnItemLongClickListener(this);
		
		urls = new ArrayList<String>();

		Button btnAdd = (Button) findViewById(R.id.btnAdd);
		btnAdd.setOnClickListener(this);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		String listUrl = pref.getString("listUrl", "");
		String selectedUrl = pref.getString("selectedUrl", "");
		
		String[] iUrl = listUrl.split(";");

		for (int i = 0; i<iUrl.length; i++) {
			if(!iUrl[i].equals("")) {
					urls.add(iUrl[i]);
			}
		}

		aaUrls = new ArrayAdapter<String>(this, R.layout.mylist, urls);
		lvUrl.setAdapter(aaUrls);
		
		//select the current URL
		for(int i = 0; i<urls.size(); i++) {
			if(urls.get(i).equals(selectedUrl)) {
				lvUrl.setItemChecked(i, true);
			}
		}
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

		selectedUrl = lvUrl.getItemAtPosition(position).toString();

		Intent i = new Intent();
		i.putExtra("url", selectedUrl);

		setResult(Activity.RESULT_OK, i);
		finish();
	}

	/**
	 * 
	 */
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
			case DialogInterface.BUTTON_POSITIVE:
				aaUrls.remove((String)lvUrl.getItemAtPosition(pos));
				saveList();
				break;
			case DialogInterface.BUTTON_NEGATIVE:
				break;
			}
		}
	};


	@Override
	public boolean onItemLongClick(AdapterView<?> a, View v, int position, long id) {
		pos = position;
		AlertDialog.Builder adb = new AlertDialog.Builder(PSIUrlActivity.this);
		adb.setMessage("Remove " + lvUrl.getItemAtPosition(position) + " ?");
		adb.setPositiveButton("Yes", dialogClickListener);
		adb.setNegativeButton("No", dialogClickListener);
		adb.show();

		return true;
	}


	@Override
	public void onClick(View arg0) {
		EditText txtUrl = (EditText) findViewById(R.id.txtUrl);

		if(!txtUrl.getText().toString().equals("")) {
			aaUrls.add(txtUrl.getText().toString());
			saveList();
		}
	}

	/**
	 * 
	 */
	public void saveList() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		String listUrl = "";

		for (int i = 0; i < aaUrls.getCount(); i++) {
			listUrl =  listUrl+";"+aaUrls.getItem(i);
		}

		Editor editor = pref.edit();
		editor.putString("listUrl", listUrl);
		editor.commit();
	}

}
