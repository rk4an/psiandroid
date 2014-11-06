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
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.phpsysinfo.R;

public class HostListActivity extends ActionBarActivity implements OnItemClickListener,
OnItemLongClickListener {

	private static JSONArray allHosts = null;
	private static JSONObject editHost = null;
	private static boolean editMode = false;
	private static ListView lvHosts = null;
	private static List<String> lHosts = null;
	private static ArrayAdapter<String> aaHosts = null;
	private static int position = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hosts_view);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		lvHosts = (ListView) findViewById(R.id.lvHostsList);
		lvHosts.setOnItemClickListener(this);
		lvHosts.setOnItemLongClickListener(this);

		allHosts = PSIConfig.getInstance().loadHosts();

		lHosts = new ArrayList<String>();
		for (int i = 0; i < allHosts.length(); i++) {
			try {
				String alias = ((JSONObject)allHosts.get(i)).getString("alias");
				if (!alias.equals("")) {
					lHosts.add(alias);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		aaHosts = new ArrayAdapter<String>(PSIActivity.getAppContext(), R.layout.hosts_list, lHosts);
		lvHosts.setAdapter(aaHosts);

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
		MenuInflater inflater = getMenuInflater();
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

				PSIConfig.getInstance().removeHost(position);
				allHosts = PSIConfig.getInstance().loadHosts();

				lHosts.remove(position);
				aaHosts.notifyDataSetChanged();

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

	public static class AddHostDialog extends DialogFragment {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			LayoutInflater inflater = getActivity().getLayoutInflater();
			View dialogView = inflater.inflate(R.layout.host_form, null);
			final EditText alias = ((EditText) dialogView.findViewById(R.id.txtAlias));
			final EditText url = ((EditText) dialogView.findViewById(R.id.txtUrl));
			final EditText username = ((EditText) dialogView.findViewById(R.id.txtUsername));
			final EditText password = ((EditText) dialogView.findViewById(R.id.txtPassword));
			final CheckBox certificate = ((CheckBox) dialogView.findViewById(R.id.cbCertificate));

			if(HostListActivity.editMode) {
				try {
					alias.setText(HostListActivity.editHost.getString("alias"));
					url.setText(HostListActivity.editHost.getString("url"));
					username.setText(HostListActivity.editHost.getString("username"));
					password.setText(HostListActivity.editHost.getString("password"));
					certificate.setChecked(HostListActivity.editHost.getBoolean("ignore"));
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
					String aliasName = alias.getText().toString();
					boolean ignoreCert = certificate.isChecked();
					
					if(aliasName.equals("")) {
						aliasName = hostUrl;
					}

					//default prefix with http
					if (!hostUrl.startsWith("http://") && !hostUrl.startsWith("https://")) {
						hostUrl = "http://" + hostUrl;
					}

					if (hostUrl.length() > 8 && URLUtil.isValidUrl(hostUrl)) {
						if(editMode) {
							if(PSIConfig.getInstance().editHost(
									position,
									aliasName,
									hostUrl,
									username.getText().toString(),
									password.getText().toString(),
									ignoreCert)) {

								lHosts.set(position, aliasName);
								aaHosts.notifyDataSetChanged();
							}
						}
						else {
							if(PSIConfig.getInstance().addHost(
									aliasName,
									hostUrl,
									username.getText().toString(),
									password.getText().toString(),
									ignoreCert)) {
								lHosts.add(aliasName);
								aaHosts.notifyDataSetChanged();
							}
						}
						allHosts = PSIConfig.getInstance().loadHosts();
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
