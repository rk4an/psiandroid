package com.phpsysinfo.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.phpsysinfo.R;
import com.phpsysinfo.xml.PSIDownloadData;
import com.phpsysinfo.xml.PSIErrorCode;
import com.phpsysinfo.xml.PSIHostData;

public class PSIActivity 
extends Activity
implements OnClickListener
{
	private SharedPreferences pref;
	private static final String SCRIPT_NAME = "/xml.php";

	private PSIHostData entry = null;
	private ImageView ivLogo = null;
	boolean ivLogoDisplay = true;

	private String selectedUrl = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		LinearLayout llContent = (LinearLayout) findViewById(R.id.llContent);

		ivLogo = new ImageView(this);
		ivLogo.setImageResource(R.drawable.psilogo);
		llContent.addView(ivLogo,0);

		//get preference
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		selectedUrl = pref.getString("selectedUrl", "");

		if(selectedUrl.equals("")) {
			//TODO: disable refresh button
		}
	}

	@Override
	public void onClick(View event) {
		if(event.getId() == R.id.pgMemory) {
			Toast.makeText(
					this, getString(R.string.lblUsed) + 
					" " + entry.getAppMemoryUsed() + getString(R.string.lblMio) + 
					" / " + entry.getAppMemoryTotal() + getString(R.string.lblMio), 
					3000).show();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * fill label with data
	 * @param entry
	 */
	public void displayInfo(PSIHostData entry) {

		this.enableButton();

		this.entry = entry;

		//hostname
		TextView txtHostname = (TextView) findViewById(R.id.txtHostname);
		txtHostname.setText(entry.getHostname());

		//uptime
		TextView txtUptime = (TextView) findViewById(R.id.txtUptime);
		txtUptime.setText(entry.getUptime());

		//load avg
		TextView txtLoad = (TextView) findViewById(R.id.txtLoad);
		txtLoad.setText(entry.getLoadAvg());

		//kernel version
		TextView txtKernel = (TextView) findViewById(R.id.txtKernel);
		if(entry.getKernel().length() > 17) {
			txtKernel.setText(entry.getKernel().substring(0, 17));
		}
		else {
			txtKernel.setText(entry.getKernel());
		}

		//distro name
		TextView txtDistro = (TextView) findViewById(R.id.txtDistro);
		txtDistro.setText(entry.getDistro());

		//ip address
		TextView txtIp = (TextView) findViewById(R.id.txtIp);
		txtIp.setText(entry.getIp());

		//memory
		ProgressBar pbMemory = (ProgressBar) findViewById(R.id.pgMemory);
		pbMemory.setProgress(entry.getAppMemoryPercent());
		pbMemory.setOnClickListener(this);


		TextView txtMemory = (TextView) findViewById(R.id.txtMemory);
		txtMemory.setText(entry.getAppMemoryPercent()+"%");

		//display
		TableLayout tlVital = (TableLayout) findViewById(R.id.tableVitals);
		tlVital.setVisibility(View.VISIBLE);

		//init mount point table
		TableLayout tlMount = (TableLayout) findViewById(R.id.tableMount);		
		tlMount.removeAllViews();

		//fill mount point table
		for (String mountPointName : entry.getMountPoint().keySet()) {

			//build row
			ProgressBar pgPercent = new ProgressBar(
					this,null,android.R.attr.progressBarStyleHorizontal);

			TextView tvName = new TextView(this);
			pgPercent.setProgress(entry.getMountPoint().get(mountPointName));
			if(mountPointName.length() > 10) {
				tvName.setText(mountPointName.substring(0, 10));
			}
			else {
				tvName.setText(mountPointName);
			}

			TextView tvPercent = new TextView(this);
			tvPercent.setText(entry.getMountPoint().get(mountPointName)+"%");

			TableRow tr = new TableRow(this);
			tr.setPadding(0, 0, 0, 3);
			tr.addView(tvName);
			tvName.setWidth(100);
			tr.addView(pgPercent);
			tvPercent.setPadding(5, 0, 0, 0);
			tr.addView(tvPercent);

			// add row to table
			tlMount.addView(tr);
		}
	}

	/**
	 * 
	 * @param error
	 */
	public void displayError(PSIErrorCode error) {
		Toast.makeText(getApplicationContext(), "Error: " + error, Toast.LENGTH_SHORT).show();

		this.enableButton();
	}

	/**
	 * enable refresh button
	 */
	public void enableButton() {

		ProgressBar pgLoading = (ProgressBar) findViewById(R.id.pgLoading);
		pgLoading.setVisibility(View.INVISIBLE);	

		if(ivLogoDisplay) {
			LinearLayout llContent = (LinearLayout) findViewById(R.id.llContent);
			llContent.removeView(ivLogo);
			ivLogoDisplay = false;
		}
	}

	/**
	 * disable refresh button
	 */
	public void disableButton() {

		ProgressBar pgLoading = (ProgressBar) findViewById(R.id.pgLoading);
		pgLoading.setVisibility(View.VISIBLE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			selectedUrl = data.getExtras().getString("url");

			Editor editor = pref.edit();
			editor.putString("selectedUrl", selectedUrl);
			editor.commit();
			
			PSIDownloadData task = new PSIDownloadData(this);
			task.execute(selectedUrl + SCRIPT_NAME);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	} 

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.iAbout:
			Toast.makeText(
					this, "PSIAndroid\nhttp://phpsysinfo.sf.net/psiandroid", 
					3000).show();
			return true;
		case R.id.iRefresh:
			PSIDownloadData task = new PSIDownloadData(this);
			task.execute(selectedUrl + SCRIPT_NAME);
			return true;
		case R.id.iSettings:
			Intent i = new Intent(this, PSIUrlActivity.class);
			startActivityForResult(i,0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}