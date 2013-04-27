package com.phpsysinfo.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.phpsysinfo.R;

public class HostListActivity extends SherlockFragmentActivity implements OnItemClickListener,
OnItemLongClickListener {

	private List<String> tmp = null;
	private JSONArray allHosts = null;
	private static JSONObject editHost = null;
	private static boolean editMode = false;
	private static ListView lvHosts = null;
	private static ArrayAdapter<String> arrayAdapterUrlList = null;
	private static int position = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hosts_view);

		lvHosts = (ListView) findViewById(R.id.lvHostsList);
		lvHosts.setOnItemClickListener(this);
		lvHosts.setOnItemLongClickListener(this);

		tmp = new ArrayList<String>();

		allHosts = PSIConfig.getInstance().loadHostsList();

		for (int i = 0; i < allHosts.length(); i++) {
			try {
				String url = ((JSONObject)allHosts.get(i)).getString("url");
				if (!url.equals("")) {
					tmp.add(url);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		arrayAdapterUrlList = new ArrayAdapter<String>(this, R.layout.hosts_list, tmp);
		lvHosts.setAdapter(arrayAdapterUrlList);

		int lastIndex = PSIConfig.getInstance().loadLastIndex();

		try {
			allHosts.get(lastIndex);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		lvHosts.setItemChecked(lastIndex, true);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.host, menu);
		return true;
	} 

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.iAdd:
			editMode = false;
			AddHostDialog addDialog = new AddHostDialog();
			addDialog.show(getSupportFragmentManager(), getString(R.string.lblHost));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				// delete item and save the list

				JSONArray temp = new JSONArray();
				for (int i = 0; i < allHosts.length(); i++) {
					try {
						if (i != position) {
							temp.put(allHosts.get(i));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				allHosts = temp;

				arrayAdapterUrlList.remove((String) lvHosts
						.getItemAtPosition(position));

				PSIConfig.getInstance().saveList(allHosts);
				break;
			case DialogInterface.BUTTON_NEUTRAL:

				try {
					editHost = (JSONObject) allHosts.get(position);
					editMode = true;

					AddHostDialog editDialog = new AddHostDialog();
					editDialog.show(getSupportFragmentManager(), getString(R.string.lblHost));

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
	public boolean onItemLongClick(AdapterView<?> a, View v, int pos,
			long id) {
		position = pos;

		// create and display the remove dialog
		AlertDialog.Builder adb = new AlertDialog.Builder(HostListActivity.this);
		adb.setMessage(getString(R.string.lblActionFor, lvHosts.getItemAtPosition(position)));
		adb.setPositiveButton(getString(R.string.lblRemove), dialogClickListener);
		adb.setNegativeButton(getString(R.string.lblCancel), dialogClickListener);
		adb.setNeutralButton(getString(R.string.lblEdit), dialogClickListener);
		adb.show();

		return true;
	}

	public static class AddHostDialog extends SherlockDialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			LayoutInflater inflater = getActivity().getLayoutInflater();
			View dialogView = inflater.inflate(R.layout.host_form, null);
			final EditText url = ((EditText)dialogView.findViewById(R.id.txtUrl));
			final EditText username = ((EditText)dialogView.findViewById(R.id.txtUsername));
			final EditText password = ((EditText)dialogView.findViewById(R.id.txtPassword));

			if(HostListActivity.editMode) {
				try {
					url.setText(HostListActivity.editHost.getString("url"));
					username.setText(HostListActivity.editHost.getString("username"));
					password.setText(HostListActivity.editHost.getString("password"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			builder.setView(dialogView);
			builder.setTitle(getString(R.string.lblHost));

			builder.setPositiveButton(getString(R.string.lblSave), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int whichButton) {

					String hostUrl = url.getText().toString().trim();

					//default prefix with http
					if (!hostUrl.startsWith("http://") && !hostUrl.startsWith("https://")) {
						hostUrl = "http://" + hostUrl;
					}

					if (hostUrl.length() > 8 && URLUtil.isValidUrl(hostUrl)) {
						if(PSIConfig.getInstance().add(
								url.getText().toString(),
								username.getText().toString(),
								password.getText().toString())) {
							arrayAdapterUrlList.add(hostUrl);
						}
					}
				}
			});

			builder.setNegativeButton(getString(R.string.lblCancel), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});

			return builder.create();
		}
	}
}
