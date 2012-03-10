package com.phpsysinfo.activity;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

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
	private static final int MEMORY_THR = 75;

	private ImageView ivLogo = null;
	boolean ivLogoDisplay = true;
	Dialog aboutDialog = null;
	Dialog errorDialog = null;
	TextView textError = null;

	//current selected url
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

		//check if a current selected url is already set
		if(selectedUrl.equals("")) {
			//TODO: disable refresh button
		}
		else {
			PSIDownloadData task = new PSIDownloadData(this);
			task.execute(selectedUrl + SCRIPT_NAME);
		}


		//create about dialog
		aboutDialog = new Dialog(this);
		aboutDialog.setContentView(R.layout.about_dialog);
		aboutDialog.setTitle("About PSIAndroid");
		TextView text = (TextView) aboutDialog.findViewById(R.id.text);
		text.setText("http://phpsysinfo.sf.net");
		ImageView image = (ImageView) aboutDialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.ic_launcher);
		image.setOnClickListener(this);

		//create error dialog
		errorDialog = new Dialog(this);
		errorDialog.setContentView(R.layout.error_dialog);
		errorDialog.setTitle("Error");
		textError = (TextView) errorDialog.findViewById(R.id.textError);
		textError.setText("");
		ImageView imageError = (ImageView) errorDialog.findViewById(R.id.imageError);
		imageError.setImageResource(R.drawable.ic_launcher);
		imageError.setOnClickListener(this);	

	}

	@Override
	public void onClick(View event) {
		if(event.getId() == R.id.image) {
			aboutDialog.hide();
		}
		else if(event.getId() == R.id.imageError) {
			errorDialog.hide();
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


		//display
		TableLayout tlVital = (TableLayout) findViewById(R.id.tableVitals);
		tlVital.setVisibility(View.VISIBLE);

		//init mount point table
		TableLayout tlMount = (TableLayout) findViewById(R.id.tableMount);	
		tlMount.removeAllViews();


		//memory
		ProgressBar pbMemory = new ProgressBar(
				this,null,android.R.attr.progressBarStyleHorizontal);

		TextView tvNameMemory = new TextView(this);
		pbMemory.setProgress(entry.getAppMemoryPercent());
		tvNameMemory.setText(getString(R.string.lblMemory) + " " + entry.getAppMemoryUsed() + 
				"/" + entry.getAppMemoryTotal() + getString(R.string.lblMio) +" ("+ entry.getAppMemoryPercent()+"%)");

		//text in red if memory usage is high
		if(entry.getAppMemoryPercent() > PSIActivity.MEMORY_THR) {
			tvNameMemory.setTextColor(0xFFFF0000);
		}


		LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

		//add mount point name row
		TableRow trNameMemory = new TableRow(this);
		trNameMemory.setLayoutParams(p);
		trNameMemory.addView(tvNameMemory,p);
		tlMount.addView(trNameMemory,p);

		//add progress bar row
		TableRow trProgressMemory = new TableRow(this);
		trProgressMemory.setLayoutParams(p);
		trProgressMemory.addView(pbMemory,p);
		tlMount.addView(trProgressMemory,p);	


		//fill mount point table
		for (String mountPointName : entry.getMountPoint().keySet()) {

			//build row
			ProgressBar pgPercent = new ProgressBar(
					this,null,android.R.attr.progressBarStyleHorizontal);

			TextView tvName = new TextView(this);
			pgPercent.setProgress(entry.getMountPoint().get(mountPointName));
			if(mountPointName.length() > 10) {
				tvName.setText(mountPointName.substring(0, 10) + ": ");
			}
			else {
				tvName.setText(mountPointName + ":");
			}

			//mount point name row
			TableRow trName = new TableRow(this);
			trName.addView(tvName);
			tlMount.addView(trName);

			//progress bar row
			TableRow trProgress = new TableRow(this);
			trProgress.addView(pgPercent);
			tlMount.addView(trProgress);
		}
	}

	/**
	 * 
	 * @param error
	 */
	public void displayError(PSIErrorCode error) {
		textError.setText(error.toString());
		this.errorDialog.show();

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
			aboutDialog.show();
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