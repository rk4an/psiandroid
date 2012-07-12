package com.phpsysinfo.activity;

import java.text.NumberFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.phpsysinfo.R;
import com.phpsysinfo.xml.PSIDownloadData;
import com.phpsysinfo.xml.PSIErrorCode;
import com.phpsysinfo.xml.PSIHostData;
import com.phpsysinfo.xml.PSIMountPoint;

public class PSIActivity 
extends Activity
implements OnClickListener, View.OnTouchListener
{
	private SharedPreferences pref;
	private static final String SCRIPT_NAME = "/xml.php";
	private static final int MEMORY_SOFT_THR = 80;
	private static final int MEMORY_HARD_THR = 90;

	private final String JSON_CURRENT_HOST = "JSON_CURRENT_HOST";

	private JSONArray hostsJsonArray;
	
	private ImageView ivLogo = null;
	boolean ivLogoDisplay = true;
	private Dialog aboutDialog = null;
	private Dialog errorDialog = null;
	private TextView textError = null;
	private ScrollView scrollView;

	//current selected url
	private String currentHost = "";
	private String url = "";
	private String user = "";
	private String password = "";
	private int selectedIndex = 0 ;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		LinearLayout llContent = (LinearLayout) findViewById(R.id.llContent);
		scrollView = (ScrollView) findViewById(R.id.scrollView1);

		ivLogo = new ImageView(this);
		ivLogo.setImageResource(R.drawable.psilogo);
		llContent.addView(ivLogo,0);

		
		//get preference
		if(currentHost.equals("")) {
			pref = PreferenceManager.getDefaultSharedPreferences(this);
			currentHost = pref.getString(JSON_CURRENT_HOST, "");
		}

		//check if a current selected url is already set
		if(!currentHost.equals("")) {
			getData(currentHost);
		}
		
		loadHostsArray();

		scrollView.setOnTouchListener(this);
		
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

		NumberFormat nf = NumberFormat.getInstance();

		//hostname
		TextView txtHostname = (TextView) findViewById(R.id.txtHostname);
		txtHostname.setText(Html.fromHtml("<a href=\""+currentHost+"\">"+entry.getHostname()+"</a>"));
		txtHostname.setMovementMethod(LinkMovementMethod.getInstance());

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
		tvNameMemory.setText(Html.fromHtml("<b>"+getString(R.string.lblMemory) + "</b> (" + nf.format(entry.getAppMemoryUsed()) + 
				"/" + nf.format(entry.getAppMemoryTotal()) + getString(R.string.lblMio) +") "+ entry.getAppMemoryPercent()+"%"));

		//text in yellow if memory usage is high
		if(entry.getAppMemoryPercent() > PSIActivity.MEMORY_SOFT_THR) {
			tvNameMemory.setTextColor(0xFFFFFF00);
		}

		//text in red if memory usage is very high
		if(entry.getAppMemoryPercent() > PSIActivity.MEMORY_HARD_THR) {
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
		for (PSIMountPoint psiMp: entry.getMountPoint()) {

			//build row
			ProgressBar pgPercent = new ProgressBar(
					this,null,android.R.attr.progressBarStyleHorizontal);

			TextView tvName = new TextView(this);
			pgPercent.setProgress(psiMp.getPercentUsed());

			final int MP_MAX_LENGTH = 12;
			String lblMountText = "<b>";
			if(psiMp.getName().length() > MP_MAX_LENGTH) {
				lblMountText += psiMp.getName().substring(0, MP_MAX_LENGTH) + "</b> ";
			}
			else {
				lblMountText += psiMp.getName() + "</b>";
			}

			lblMountText += " (" + nf.format(psiMp.getUsed()) + 
					"/" + nf.format(psiMp.getTotal()) + getString(R.string.lblMio) +") "+ psiMp.getPercentUsed()+"%";

			tvName.setText(Html.fromHtml(lblMountText));

			//text in yellow if mount point usage is high
			if(psiMp.getPercentUsed() > PSIActivity.MEMORY_SOFT_THR) {
				tvName.setTextColor(0xFFFFFF00);
			}

			//text in red if mount point usage is very high
			if(psiMp.getPercentUsed() > PSIActivity.MEMORY_HARD_THR) {
				tvName.setTextColor(0xFFFF0000);
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
			String currentHost = data.getExtras().getString("host");

			getData(currentHost);
			loadHostsArray();

			//save last selected host
			Editor editor = pref.edit();
			editor.putString(JSON_CURRENT_HOST,currentHost);
			editor.commit();
		}
		else {
			loadHostsArray();
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
			if(!url.equals("")) {
				PSIDownloadData task = new PSIDownloadData(this);
				task.execute(url + SCRIPT_NAME, user, password);
			}
			return true;
		case R.id.iSettings:
			Intent i = new Intent(this, PSIUrlActivity.class);
			startActivityForResult(i,0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	Float firstX = null;

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		scrollView.onTouchEvent(event);

		if (hostsJsonArray.length() <= 1) {
			return true;
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			firstX = event.getX();
		} else {
			if (firstX != null) {
				float x = event.getX();
				float diff = firstX - x;
				if (Math.abs(diff) > 150) {

					if (diff > 0) {
						selectedIndex++;
						if (selectedIndex >= hostsJsonArray.length()) {
							selectedIndex = 0;
						}
					}

					if (diff < 0) {
						selectedIndex--;
						if (selectedIndex < 0) {
							selectedIndex = hostsJsonArray.length() -1;
						}
					}

					try {
						currentHost = hostsJsonArray.get(selectedIndex).toString();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					getData(currentHost);

					firstX = null;
					return false;
				}
			}	
		}

		return true;
	}


	/**
	 * 
	 */
	public void loadHostsArray() {
		String dataStore = "";
		try {
			dataStore = pref.getString(PSIUrlActivity.HOSTS_JSON_STORE, "");
			Log.d("dataStored",dataStore);
			if (dataStore.equals("")) {
				hostsJsonArray = new JSONArray();
			}
			else {
				JSONTokener tokener = new JSONTokener(dataStore);
				hostsJsonArray = new JSONArray(tokener);
			}
			
			//get index of current selected host
			for(int i=0; i<hostsJsonArray.length(); i++) {
				String u = ((JSONObject)hostsJsonArray.get(i)).getString("url");
				
				if(u.equals(url)) {
					selectedIndex = i;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param currentHost
	 */
	public void getData(String currentHost) {
		PSIDownloadData task = new PSIDownloadData(this);

		JSONObject sHost = null;
		try {

			JSONTokener tokener = new JSONTokener(currentHost);
			sHost = new JSONObject(tokener);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			url = sHost.getString("url");
			user = sHost.getString("username");
			password = sHost.getString("password");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		task.execute(url + SCRIPT_NAME, user, password);

	}
}