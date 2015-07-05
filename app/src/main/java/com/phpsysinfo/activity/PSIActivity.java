package com.phpsysinfo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.phpsysinfo.R;
import com.phpsysinfo.ui.HeaderTextView;
import com.phpsysinfo.utils.FormatUtils;
import com.phpsysinfo.xml.PSIDownloadData;
import com.phpsysinfo.xml.PSIErrorCode;
import com.phpsysinfo.xml.PSIHostData;
import com.phpsysinfo.xml.PSIMountPoint;
import com.phpsysinfo.xml.PSINetworkInterface;
import com.phpsysinfo.xml.PSIPrinter;
import com.phpsysinfo.xml.PSIPrinterItem;
import com.phpsysinfo.xml.PSIRaid;
import com.phpsysinfo.xml.PSIRaidDevice;
import com.phpsysinfo.xml.PSISmart;
import com.phpsysinfo.xml.PSITemperature;
import com.phpsysinfo.xml.PSIUps;
import com.phpsysinfo.xml.PSIVoltage;

enum ViewType {
	NONE, LOGO, ERROR, DATA, LOADING
}

public class PSIActivity 
extends ActionBarActivity
implements OnClickListener, View.OnTouchListener, OnNavigationListener
{
	private static Context context;

	private Dialog aboutDialog;
	private ScrollView scrollView;

	private int selectedIndex = 0 ;
	private boolean isReady = true;

	private Menu menu;

	private ViewType viewType = ViewType.NONE;

	private PSIDownloadData task;

	ActionBar actionBar;
	ArrayList<String> dropDown = new ArrayList<String>();
	ArrayAdapter<String> aaDropDown;

	Float firstX = null;

	SharedPreferences pref = null;
	private static final int CODE_HOST = 10;
	private static final int CODE_PREFERENCE = 20;
	int autoRefreshInterval = 0;
	boolean disableSwipe = false;
	Handler handler = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PSIActivity.context = getApplicationContext();

		setContentView(R.layout.main_view);

		scrollView = (ScrollView) findViewById(R.id.svMain);
		scrollView.setOnTouchListener(this);

		//create about dialog
		aboutDialog = new Dialog(this);
		aboutDialog.setContentView(R.layout.about_dialog);
		aboutDialog.setTitle("PSIAndroid");
		TextView text = (TextView) aboutDialog.findViewById(R.id.text);

		try {
			PackageInfo manager = getPackageManager().getPackageInfo(getPackageName(), 0);
			text.setText("PSIAndroid " + manager.versionName + "\n");
		} catch (Exception e) { }

		text.append(Html.fromHtml(
				"<a href=\"https://play.google.com/store/apps/details?id=com.phpsysinfo\">" +
				"Please rate this app on Google Play!</a>"));
		text.setMovementMethod(LinkMovementMethod.getInstance());
		ImageView image = (ImageView) aboutDialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.ic_launcher);
		image.setOnClickListener(this);

		displayLogo();

		handler = new Handler();

		pref = PreferenceManager.getDefaultSharedPreferences(this);
		autoRefreshInterval = Integer.parseInt(pref.getString("autorefresh", 0 + ""));
		disableSwipe = pref.getBoolean("pref_swipe", false);
		PSIConfig.TIMEOUT = Integer.parseInt(pref.getString("timeout", PSIConfig.TIMEOUT + ""));

		//update JSON
		JSONArray allHosts = PSIConfig.getInstance().loadHosts();
		for (int i = 0; i < allHosts.length(); i++) {
			try {
				String url = ((JSONObject)allHosts.get(i)).getString("url");
				String username = ((JSONObject)allHosts.get(i)).getString("username");
				String password = ((JSONObject)allHosts.get(i)).getString("password");
				String alias = "";
				boolean ignoreCert = false;

				//set alias if empty
				if(!((JSONObject)allHosts.get(i)).has("alias")) {
					alias = url;
				}
				else {
					alias = ((JSONObject)allHosts.get(i)).getString("alias");
				}

				//set ignore if empty
				if(!((JSONObject)allHosts.get(i)).has("ignore")) {
					ignoreCert = false;
				}
				else {
					ignoreCert = ((JSONObject)allHosts.get(i)).getBoolean("ignore");
				}

				PSIConfig.getInstance().editHost(i, alias, url, username, password, ignoreCert);

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}



		actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		aaDropDown = new ArrayAdapter<String>(
				actionBar.getThemedContext(),
				android.R.layout.simple_list_item_1,
				android.R.id.text1, dropDown);

		actionBar.setListNavigationCallbacks(aaDropDown, this);

		updateDropDown();

		//load data
		selectedIndex = PSIConfig.getInstance().loadLastIndex();
		actionBar.setSelectedNavigationItem(selectedIndex);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		if(autoRefreshInterval != 0) {
			handler.postDelayed(runAutoUpdate, autoRefreshInterval*1000);
		}
	}

	@Override
	public void onPause(){
		super.onPause();
		if(handler != null) {
			handler.removeCallbacks(runAutoUpdate);
		}
	}

	public void displayLogo(){
		if (this.viewType == ViewType.LOGO) {
			return;
		}

		loadDynamicLayout(R.layout.logo);
	}

	@Override
	public void onClick(View event) {
		if(event.getId() == R.id.image) {
			aboutDialog.hide();
		}
		else if(event.getId() == R.id.tvMemoryUsage) {
			toggleContent((View) findViewById(R.id.tMemory), R.id.tvMemoryUsage);
		}
		else if(event.getId() == R.id.tvMountPoints) {
			toggleContent((View) findViewById(R.id.tMountPoints), R.id.tvMountPoints);
		}
		else if(event.getId() == R.id.tvTemperature) {
			toggleContent((View) findViewById(R.id.llTemperature), R.id.tvTemperature);
		}
		else if(event.getId() == R.id.tvNetwork) {
			toggleContent((View) findViewById(R.id.llNetwork), R.id.tvNetwork);
		}
		else if(event.getId() == R.id.tvProcessStatus) {
			toggleContent((View) findViewById(R.id.llProcessStatus), R.id.tvProcessStatus);
		}
		else if(event.getId() == R.id.tvFans) {
			toggleContent((View) findViewById(R.id.llFans), R.id.tvFans);
		}
		else if(event.getId() == R.id.tvUps) {
			toggleContent((View) findViewById(R.id.llUps), R.id.tvUps);
		}
		else if(event.getId() == R.id.tvSmart) {
			toggleContent((View) findViewById(R.id.llSmart), R.id.tvSmart);
		}
		else if(event.getId() == R.id.tvRaid) {
			toggleContent((View) findViewById(R.id.llRaid), R.id.tvRaid);
		}
		else if(event.getId() == R.id.tvUpdate) {
			toggleContent((View) findViewById(R.id.llUpdate), R.id.tvUpdate);
		}
		else if(event.getId() == R.id.tvPrinter) {
			toggleContent((View) findViewById(R.id.llPrinter), R.id.tvPrinter);
		}
		else if(event.getId() == R.id.tvBat) {
			toggleContent((View) findViewById(R.id.llBat), R.id.tvBat);
		}
	}

	public void toggleContent(final View v, int res){

		v.setVisibility( v.isShown()? View.GONE: View.VISIBLE );

		if(v.isShown()) {
			displayArrow(res, "up");
		}
		else {
			displayArrow(res, "down");
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	public void displayArrow(int label, String state) {
		TextView t = (TextView) findViewById(label);
		Resources res = getResources();
		int resourceId = res.getIdentifier(state, "drawable", getPackageName());
		Drawable img = res.getDrawable(resourceId);
		t.setCompoundDrawablesWithIntrinsicBounds(img, null , null, null);
	}

	public void setPadding(View v) {
		v.setPadding(5, 5, 5, 5);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		llp.setMargins(0, 5, 0, 5);
		v.setLayoutParams(llp);
	}

	/**
	 * fill label with data
	 * @param entry
	 */
	public void displayInfo(PSIHostData entry) {

		isReady = true;

		if (viewType != ViewType.DATA) {
			loadDynamicLayout(R.layout.data_view);
			((TextView) findViewById(R.id.tvMemoryUsage)).setOnClickListener(this);
			((TextView) findViewById(R.id.tvMountPoints)).setOnClickListener(this);
			setPadding((TextView) findViewById(R.id.tvMemoryUsage));
			setPadding((TextView) findViewById(R.id.tvMountPoints));

			viewType = ViewType.DATA;
			scrollView.setOnTouchListener(this);
		}

		//title
		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);

		String url = "";
		try {
			JSONObject host = (JSONObject) PSIConfig.getInstance().loadHosts().get(selectedIndex);
			url = (String) host.get("url");

		} catch (JSONException e) {
			e.printStackTrace();
		}

		txtTitle.setText(
				Html.fromHtml("<a href=\""+url+"\">"+entry.getHostname()+"</a>"));
		txtTitle.setMovementMethod(LinkMovementMethod.getInstance());

		//machine
		if(entry.getMachine() != null) {
			TextView txtMachine = (TextView) findViewById(R.id.txtMachine);
			txtMachine.setText(entry.getMachine());
			findViewById(R.id.trMachine).setVisibility(LinearLayout.VISIBLE);
		}
		else {
			findViewById(R.id.trMachine).setVisibility(LinearLayout.GONE);
		}

		//processes
		if(entry.getProcesses() != -1) {
			TextView txtProcesses = (TextView) findViewById(R.id.txtProcesses);
			txtProcesses.setText(entry.getProcesses()+"");
			findViewById(R.id.trProcesses).setVisibility(LinearLayout.VISIBLE);
		}
		else {
			findViewById(R.id.trProcesses).setVisibility(LinearLayout.GONE);
		}

		if(entry.getProcessesRunning() != -1) {
			TextView txtProcesses = (TextView) findViewById(R.id.txtProcesses);

			txtProcesses.append(" ("+
					entry.getProcessesRunning() + "\u00A0" + getString(R.string.lblProcessesRunning));

			if(entry.getProcessesSleeping() != -1 && entry.getProcessesSleeping() != 0) {
				txtProcesses.append(", " + entry.getProcessesSleeping() + "\u00A0" + getString(R.string.lblProcessesSleeping));
			}

			if(entry.getProcessesStopped() != -1 && entry.getProcessesStopped() != 0 ) {
				txtProcesses.append(", " + entry.getProcessesStopped() + "\u00A0" + getString(R.string.lblProcessesStopped));
			}

			if(entry.getProcessesZombie() != -1 && entry.getProcessesZombie() != 0) {
				txtProcesses.append(", " +  entry.getProcessesZombie() + "\u00A0" + getString(R.string.lblProcessesZombie));
			}

			if(entry.getProcessesWaiting() != -1 && entry.getProcessesWaiting() != 0) {
				txtProcesses.append(", " +  entry.getProcessesWaiting() + "\u00A0" + getString(R.string.lblProcessesWaiting));
			}
			
			if(entry.getProcessesOther() != -1 && entry.getProcessesOther() != 0) {
				txtProcesses.append(", " +  entry.getProcessesOther() + "\u00A0" + getString(R.string.lblProcessesOther));
			}

			txtProcesses.append(")");
		}

		//uptime
		TextView txtUptime = (TextView) findViewById(R.id.txtUptime);
		txtUptime.setText(entry.getUptime());

		//load avg
		TextView txtLoad = (TextView) findViewById(R.id.txtLoad);
		txtLoad.setText(entry.getLoadAvg());

		//load avg percent
		TableRow trLoadPercent = (TableRow) findViewById(R.id.trLoadPercent);
		LayoutInflater inflater = getLayoutInflater();
		ProgressBar pbLoad = (ProgressBar) inflater.inflate(R.layout.pg, null);

		trLoadPercent.removeAllViews();
		if(entry.getCpuUsage() != -1) {
			trLoadPercent.addView(new TextView(this));
			trLoadPercent.addView(pbLoad);
			pbLoad.setProgress(entry.getCpuUsage());
			txtLoad.setText(entry.getCpuUsage() +"%" + " (" + entry.getLoadAvg() + ")");
			trLoadPercent.setVisibility(LinearLayout.VISIBLE);
		}
		else {
			trLoadPercent.setVisibility(LinearLayout.GONE);
		}

		//psi version
		TextView txtVersion = (TextView) findViewById(R.id.txtVersion);
		txtVersion.setText(entry.getPsiVersion());
		if(entry.getPsiVersion() != null && entry.getPsiVersion().matches("3.0-rc(.*)")) {
			txtVersion.setTextColor(getResources().getColor(R.color.state_hard));
		}

		//kernel version
		TextView txtKernel = (TextView) findViewById(R.id.txtKernel);
		txtKernel.setText(entry.getKernel());

		//Cpu
		TextView txtCpu = (TextView) findViewById(R.id.txtCpu);
		txtCpu.setText(entry.getCpu() + " ("+entry.getCpuCore()+")");

		TextView txtUsers = (TextView) findViewById(R.id.txtUsers);
		txtUsers.setText(entry.getUsers());

		//distro name
		TextView txtDistro = (TextView) findViewById(R.id.txtDistro);
		txtDistro.setText(entry.getDistro());

		ImageView ivDistro = (ImageView) findViewById(R.id.ivDistro);

		//distro icon
		try {
			Resources res = getResources();
			String tname = entry.getDistroIcon().toLowerCase();
			int ext = tname.indexOf('.');
			String name = tname.substring(0, ext);
			int resourceId = res.getIdentifier(name, "drawable", getPackageName());

			ivDistro.setImageResource(resourceId);
		}
		catch (Exception e) {
			//clear
			ivDistro.setImageBitmap(null);
		}

		//ip address
		TextView txtIp = (TextView) findViewById(R.id.txtIp);
		txtIp.setText(entry.getIp());

		//init mount point table
		TableLayout tMountPoints = (TableLayout) findViewById(R.id.tMountPoints);	
		tMountPoints.removeAllViews();

		//memory
		ProgressBar pbMemory = (ProgressBar) inflater.inflate(R.layout.pg, null);

		TextView tvNameMemory = new TextView(this);
		pbMemory.setProgress(entry.getAppMemoryPercent());

		if(entry.getAppMemoryFullPercent() != 0) {
			pbMemory.setSecondaryProgress(entry.getAppMemoryFullPercent());
		}

		tvNameMemory.setText(Html.fromHtml(
				"<b>"+getString(R.string.lblMemory) + "</b>" +

		" (" + FormatUtils.getFormatedMemory(entry.getAppMemoryUsed()) + 
		"&nbsp;" + getString(R.string.lblOf) + "&nbsp;" + FormatUtils.getFormatedMemory(entry.getAppMemoryTotal()) + ") <i>" + 
		entry.getAppMemoryPercent()+"%</i>"));


		//text in yellow if memory usage is high
		if(entry.getAppMemoryPercent() > PSIConfig.MEMORY_SOFT_THR) {
			tvNameMemory.setTextColor(getResources().getColor(R.color.state_soft));
		}

		//text in red if memory usage is very high
		if(entry.getAppMemoryPercent() > PSIConfig.MEMORY_HARD_THR) {
			tvNameMemory.setTextColor(getResources().getColor(R.color.state_hard));
		}

		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

		//init memory table
		TableLayout tMemory = (TableLayout) findViewById(R.id.tMemory);	
		tMemory.removeAllViews();

		//add mount point name row
		TableRow trNameMemory = new TableRow(this);
		trNameMemory.setLayoutParams(p);
		trNameMemory.addView(tvNameMemory,p);
		tMemory.addView(trNameMemory,p);

		//add progress bar row
		TableRow trProgressMemory = new TableRow(this);
		trProgressMemory.setLayoutParams(p);
		trProgressMemory.addView(pbMemory,p);
		tMemory.addView(trProgressMemory,p);	


		//fill mount point table
		for (PSIMountPoint psiMp: entry.getMountPoint()) {

			//build row
			ProgressBar pgPercent = (ProgressBar) inflater.inflate(R.layout.pg, null);

			TextView tvName = new TextView(this);
			pgPercent.setProgress(psiMp.getPercentUsed());

			String lblMountText = "<b>" + psiMp.getName() + "</b>";

			lblMountText += " (" + FormatUtils.getFormatedMemory(psiMp.getUsed()) + 
					"&nbsp;" + getString(R.string.lblOf) + "&nbsp;" + FormatUtils.getFormatedMemory(psiMp.getTotal()) + ") <i>"+ psiMp.getPercentUsed()+"%</i>";

			tvName.setText(Html.fromHtml(lblMountText));

			//text in yellow if mount point usage is high
			if(psiMp.getPercentUsed() > PSIConfig.MEMORY_SOFT_THR) {
				tvName.setTextColor(getResources().getColor(R.color.state_soft));
			}

			//text in red if mount point usage is very high
			if(psiMp.getPercentUsed() > PSIConfig.MEMORY_HARD_THR) {
				tvName.setTextColor(getResources().getColor(R.color.state_hard));
			}

			//mount point name row
			TableRow trName = new TableRow(this);
			trName.addView(tvName);
			tMountPoints.addView(trName);

			//progress bar row
			TableRow trProgress = new TableRow(this);
			trProgress.addView(pgPercent);
			tMountPoints.addView(trProgress);
		}


		//cleanup plugins
		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);
		llPlugins.removeAllViews();

		//ipmi section
		showIpmi(entry);

		//mb fans speed
		showFans(entry);

		//network section
		showNetworkInterface(entry);

		//ps status
		showPsStatus(entry);

		//ups section
		showUps(entry);

		//smart section
		showSmart(entry);

		//raid section
		showRaid(entry);

		//update section
		showUpdate(entry);

		//printer section
		showPrinter(entry);

		//bat section
		showBat(entry);

		if(autoRefreshInterval != 0) {
			handler.postDelayed(runAutoUpdate, autoRefreshInterval*1000);
		}
	}

	Runnable runAutoUpdate = new Runnable(){
		public void run(){
			getData(selectedIndex);
		}
	};

	/**
	 * 
	 * @param error
	 */
	public void displayError(String host, PSIErrorCode error, String errorMessage) {

		isReady = true;

		if (this.viewType != ViewType.ERROR) {
			loadDynamicLayout(R.layout.error_view);
			this.viewType = ViewType.ERROR;
		}

		((TextView) findViewById(R.id.errortxt)).setText(getString(R.string.lblError));
		((TextView) findViewById(R.id.errorhost)).setText(host);
		((TextView) findViewById(R.id.errorcode)).setText("[" + error.toString() + "] " + errorMessage);

	}

	public void setRefreshActionButtonState(final boolean refreshing) {
		if (menu != null) {
			final MenuItem refreshItem = menu.findItem(R.id.iRefresh);
			if (refreshItem != null) {
				if (refreshing) {
					//refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
					MenuItemCompat.setActionView(refreshItem,R.layout.actionbar_indeterminate_progress);
				} else {
					//refreshItem.setActionView(null);
					MenuItemCompat.setActionView(refreshItem,null);
				}
			}
		}
	}

	public void refresh() {
		setRefreshActionButtonState(true);
		isReady = false;
	}


	public void completeRefresh() {
		setRefreshActionButtonState(false);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == PSIActivity.CODE_PREFERENCE) {
			autoRefreshInterval = Integer.parseInt(pref.getString("autorefresh", 0+""));	
			disableSwipe = pref.getBoolean("pref_swipe", false);

			if(handler != null) {
				handler.removeCallbacks(runAutoUpdate);
			}
			if(autoRefreshInterval != 0) {
				handler.postDelayed(runAutoUpdate, autoRefreshInterval*1000);
			}

			PSIConfig.TIMEOUT = Integer.parseInt(pref.getString("timeout", PSIConfig.TIMEOUT+""));

			return;
		}
		else if (requestCode == PSIActivity.CODE_HOST) {

			if (resultCode == RESULT_OK) {
				//load new selected host
				displayLoadingMessage(data.getExtras().getInt("host"));
				selectedIndex = data.getExtras().getInt("host");

				//FIXME: onItemSelected not called when selected item remains the same
				actionBar.setSelectedNavigationItem(selectedIndex);
			}
		}
		updateDropDown();
	}


	private void updateDropDown() {
		JSONArray allHosts = PSIConfig.getInstance().loadHosts();
		dropDown.clear();
		for (int i = 0; i < allHosts.length(); i++) {
			try {
				String alias = ((JSONObject)allHosts.get(i)).getString("alias");
				if (!alias.equals("")) {
					dropDown.add(alias);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		aaDropDown.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	} 

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.iAbout:
			aboutDialog.show();
			return true;
		case R.id.iRefresh:
			getData(selectedIndex);
			return true;
		case R.id.iSettings:
			Intent i = new Intent(this, HostListActivity.class);
			startActivityForResult(i, PSIActivity.CODE_HOST);
			return true;
		case R.id.iPreference:
			Intent ip = new Intent(this, PSIPreferencesActivity.class);
			startActivityForResult(ip, PSIActivity.CODE_PREFERENCE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}



	@Override
	public boolean onTouch(View v, MotionEvent event) {

		JSONArray hostsJsonArray = PSIConfig.getInstance().loadHosts();

		if(disableSwipe) {
			return false;
		}

		if(!isReady) {
			return false;
		}

		v.onTouchEvent(event);

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

					//load the previous/next host
					displayLoadingMessage(selectedIndex);
					actionBar.setSelectedNavigationItem(selectedIndex);

					firstX = null;
					return false;
				}
			}	
		}

		return true;
	}

	private void displayLoadingMessage(int index) {

		String hostname = "";
		JSONArray hostsList = PSIConfig.getInstance().loadHosts();
		JSONObject currentHost = null;
		try {
			if(index < hostsList.length()) {
				currentHost = (JSONObject) hostsList.get(index);
				hostname = currentHost.getString("alias");
			}
		}
		catch(Exception e) {
		}

		loadDynamicLayout(R.layout.loading);
		((TextView) findViewById(R.id.tvLoading)).setText(getString(R.string.lblLoading));
		((TextView) findViewById(R.id.tvLoadingHost)).setText(hostname);
		viewType = ViewType.LOADING;
	}

	public void getData(int index) {

		handler.removeCallbacks(runAutoUpdate);

		JSONArray hostsList = PSIConfig.getInstance().loadHosts();
		JSONObject currentHost = null;
		try {
			if(index < hostsList.length()) {

				currentHost = (JSONObject) hostsList.get(index);
				String alias = currentHost.getString("alias");
				String url = currentHost.getString("url");
				String user = currentHost.getString("username");
				String password = currentHost.getString("password");
				Boolean ignoreCert = currentHost.getBoolean("ignore");

				Log.d("PSIAndroid","getData for " + url);
				task = new PSIDownloadData(this);
				this.refresh();
				if(!url.equals("")) {
					task.execute(url + PSIConfig.SCRIPT_NAME, user, password, alias, ignoreCert.toString());
				}
			}

		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}

	public void showNetworkInterface(PSIHostData entry) {

		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);

		if(entry.getNetworkInterface().size() > 0) {

			//header
			HeaderTextView tvNetwork = new HeaderTextView(this);
			tvNetwork.setId(R.id.tvNetwork);
			tvNetwork.setText(getString(R.string.lblNetwork));

			llPlugins.addView(tvNetwork);

			tvNetwork.setOnClickListener(this);

			//content
			TableLayout tNetwork = new TableLayout(this);
			tNetwork.setColumnShrinkable(0, true);
			tNetwork.setColumnStretchable(0, true);
			tNetwork.setColumnStretchable(1, true);
			tNetwork.setColumnStretchable(2, true);
			tNetwork.setColumnStretchable(3, true);
			tNetwork.setId(R.id.tNetwork);

			LinearLayout llNetwork = new LinearLayout(this);
			llNetwork.setId(R.id.llNetwork);
			llNetwork.setOrientation(LinearLayout.VERTICAL);


			//populate
			for (PSINetworkInterface pni : entry.getNetworkInterface()) {
				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml("<b>" + pni.getName() + ": </b>"));

				TextView tvItemValueRx = new TextView(this);
				tvItemValueRx.setText(Html.fromHtml(
						"&darr; " + FormatUtils.getFormatedMemory((int)pni.getRxBytes())));

				TextView tvItemValueTx = new TextView(this);
				tvItemValueTx.setText(Html.fromHtml( 
						"&uarr; " + FormatUtils.getFormatedMemory((int)pni.getTxBytes())));

				TextView tvItemValueErrDrops = new TextView(this);
				tvItemValueErrDrops.setText("(" + pni.getErr() + "/" + pni.getDrops() + ")");

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValueRx);
				trItem.addView(tvItemValueTx);
				trItem.addView(tvItemValueErrDrops);

				tNetwork.addView(trItem);
			}

			llNetwork.addView(tNetwork);
			llPlugins.addView(llNetwork);
		}
	}

	public void showIpmi(PSIHostData entry) {

		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);

		if(entry.getTemperature().size() > 0) {

			//header
			HeaderTextView tvTemperature = new HeaderTextView(this);
			tvTemperature.setId(R.id.tvTemperature);
			tvTemperature.setText(getString(R.string.lblTemperatures));
			llPlugins.addView(tvTemperature);

			tvTemperature.setOnClickListener(this);

			//content
			TableLayout tTemperature = new TableLayout(this);
			tTemperature.setColumnShrinkable(0, true);
			tTemperature.setColumnStretchable(0, true);
			tTemperature.setColumnStretchable(1, true);
			tTemperature.setId(R.id.tTemperature);

			LinearLayout llTemperature = new LinearLayout(this);
			llTemperature.setId(R.id.llTemperature);
			llTemperature.setOrientation(LinearLayout.VERTICAL);

			List<PSITemperature> temperatures = entry.getTemperature();

			//populate
			for (PSITemperature temperature : temperatures) {
				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml("<b>" + temperature.getDescription() + ": </b>"));

				TextView tvItemValue = new TextView(this);
				if(temperature.getMax() != -1) {
					tvItemValue.setText(temperature.getTemp() + "°C (max: " + temperature.getMax()+"°C)");

					if(temperature.getMax() != 0) {
						float percent = temperature.getTemp() * 100 / temperature.getMax();
						if(percent > PSIConfig.TEMP_SOFT_THR) {
							tvItemValue.setTextColor(getResources().getColor(R.color.state_soft));
						}
					}
				}
				else {
					tvItemValue.setText(temperature.getTemp() + "°C");
				}
				tvItemValue.setWidth(0);
				tvItemLabel.setWidth(0);
				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValue);

				tTemperature.addView(trItem);
			}

			llTemperature.addView(tTemperature);
			llPlugins.addView(llTemperature);
		}



		if(entry.getVoltages().size() > 0) {

			//header
			HeaderTextView tvVoltage = new HeaderTextView(this);
			tvVoltage.setId(R.id.tvVoltage);
			tvVoltage.setText(getString(R.string.lblVoltage));
			llPlugins.addView(tvVoltage);

			tvVoltage.setOnClickListener(this);

			//content
			TableLayout tVoltage = new TableLayout(this);
			tVoltage.setColumnShrinkable(0, true);
			tVoltage.setColumnStretchable(0, true);
			tVoltage.setColumnStretchable(1, true);
			tVoltage.setId(R.id.tVoltage);

			LinearLayout llVoltage = new LinearLayout(this);
			llVoltage.setId(R.id.llVoltage);
			llVoltage.setOrientation(LinearLayout.VERTICAL);

			List<PSIVoltage> voltages = entry.getVoltages();

			//populate
			for (PSIVoltage voltage : voltages) {
				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml("<b>" + voltage.getDescription() + ": </b>"));

				TextView tvItemValue = new TextView(this);

				tvItemValue.setText(voltage.getValue() + "");

				tvItemValue.setWidth(0);
				tvItemLabel.setWidth(0);
				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValue);

				tVoltage.addView(trItem);
			}

			llVoltage.addView(tVoltage);
			llPlugins.addView(llVoltage);
		}

	}

	public void showPsStatus(PSIHostData entry) {

		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);

		if(entry.getProcessStatus().size() > 0) {

			//header
			HeaderTextView tvProcessStatus = new HeaderTextView(this);
			tvProcessStatus.setId(R.id.tvProcessStatus);
			tvProcessStatus.setText(getString(R.string.lblProcessStatus));
			llPlugins.addView(tvProcessStatus);

			tvProcessStatus.setOnClickListener(this);

			//content
			TableLayout tProcessStatus = new TableLayout(this);
			tProcessStatus.setColumnShrinkable(0, true);
			tProcessStatus.setId(R.id.tProcessStatus);

			LinearLayout llProcessStatus = new LinearLayout(this);
			llProcessStatus.setId(R.id.llProcessStatus);
			llProcessStatus.setOrientation(LinearLayout.VERTICAL);


			HashMap<String, String> psStatus = entry.getProcessStatus();

			for (String mapKey : psStatus.keySet()) {
				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml("<b>" + mapKey + ": </b>"));

				TextView tvItemValue = new TextView(this);

				String value = psStatus.get(mapKey);
				if(value != null) {
					if(value.equals("1")) {
						tvItemValue.setText("UP");
						tvItemValue.setTextColor(getResources().getColor(R.color.state_ok));
					}
					else {
						tvItemValue.setText("DOWN");
						tvItemValue.setTextColor(getResources().getColor(R.color.state_hard));
					}
				}

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValue);

				tProcessStatus.addView(trItem);
			}

			llProcessStatus.addView(tProcessStatus);
			llPlugins.addView(llProcessStatus);
		}
	}

	public void showFans(PSIHostData entry) {

		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);

		if(entry.getFans().size() > 0) {

			//header
			HeaderTextView tvFans = new HeaderTextView(this);
			tvFans.setId(R.id.tvFans);
			tvFans.setText(getString(R.string.lblFans));

			llPlugins.addView(tvFans);

			tvFans.setOnClickListener(this);

			//content
			TableLayout tFans = new TableLayout(this);
			tFans.setColumnShrinkable(0, true);
			tFans.setColumnStretchable(0, true);
			tFans.setColumnStretchable(1, true);
			tFans.setId(R.id.tFans);

			LinearLayout llFans = new LinearLayout(this);
			llFans.setId(R.id.llFans);
			llFans.setOrientation(LinearLayout.VERTICAL);


			HashMap<String, String> fans = entry.getFans();

			for (String mapKey : fans.keySet()) {
				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml("<b>" + mapKey + ": </b>"));

				TextView tvItemValue = new TextView(this);
				String value = fans.get(mapKey);
				tvItemValue.setText(value);

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);

				tvItemLabel.setWidth(0);
				tvItemValue.setWidth(0);
				trItem.addView(tvItemValue);

				tFans.addView(trItem);
			}

			llFans.addView(tFans);
			llPlugins.addView(llFans);
		}
	}


	public void showUps(PSIHostData entry) {

		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);

		if(entry.getUps().size() > 0) {

			//header
			HeaderTextView tvUps = new HeaderTextView(this);
			tvUps.setId(R.id.tvUps);
			tvUps.setText(getString(R.string.lblUps));
			llPlugins.addView(tvUps);

			tvUps.setOnClickListener(this);

			//content
			TableLayout tUps = new TableLayout(this);
			tUps.setColumnShrinkable(1, true);
			tUps.setId(R.id.tUps);

			LinearLayout llUps = new LinearLayout(this);
			llUps.setId(R.id.llUps);
			llUps.setOrientation(LinearLayout.VERTICAL);


			List<PSIUps> items = entry.getUps();

			for (PSIUps item : items) {

				if(item.getName() != null) {

					TextView tvItemLabel = new TextView(this);
					tvItemLabel.setTextColor(getResources().getColor(R.color.sub_item));
					tvItemLabel.setText(Html.fromHtml("<b>"+item.getName()+"</b>" ));

					TableRow trItem = new TableRow(this);
					trItem.addView(tvItemLabel);

					tUps.addView(trItem);
				}

				if(item.getModel() != null) {

					TextView tvItemLabel = new TextView(this);
					tvItemLabel.setText(Html.fromHtml("<b>"+getString(R.string.lblUpsModel)+" </b>"));

					TextView tvItemValue = new TextView(this);
					tvItemValue.setText(item.getModel());

					TableRow trItem = new TableRow(this);
					trItem.addView(tvItemLabel);
					trItem.addView(tvItemValue);

					tUps.addView(trItem);
				}

				if(item.getBatteryChargePercent() != null) {

					TextView tvItemLabel = new TextView(this);
					tvItemLabel.setText(Html.fromHtml("<b>"+getString(R.string.lblUpsBatteryCharge)+" </b>"));

					TextView tvItemValue = new TextView(this);
					tvItemValue.setText(item.getBatteryChargePercent() + "%");

					TableRow trItem = new TableRow(this);
					trItem.addView(tvItemLabel);
					trItem.addView(tvItemValue);

					tUps.addView(trItem);
				}

				if(item.getLoadPercent() != null) {

					TextView tvItemLabel = new TextView(this);
					tvItemLabel.setText(Html.fromHtml("<b>"+getString(R.string.lblUpsLoad)+" </b>"));

					TextView tvItemValue = new TextView(this);
					tvItemValue.setText(item.getLoadPercent() + "%");

					TableRow trItem = new TableRow(this);
					trItem.addView(tvItemLabel);
					trItem.addView(tvItemValue);

					tUps.addView(trItem);
				}

				if(item.getTimeLeftMinutes() != null) {

					TextView tvItemLabel = new TextView(this);
					tvItemLabel.setText(Html.fromHtml("<b>"+getString(R.string.lblUpsTime)+" </b>"));

					TextView tvItemValue = new TextView(this);
					tvItemValue.setText(item.getTimeLeftMinutes() + "min");

					TableRow trItem = new TableRow(this);
					trItem.addView(tvItemLabel);
					trItem.addView(tvItemValue);

					tUps.addView(trItem);
				}

				if(item.getBatteryVoltage() != null) {

					TextView tvItemLabel = new TextView(this);
					tvItemLabel.setText(Html.fromHtml("<b>"+getString(R.string.lblUpsBattery)+" </b>"));

					TextView tvItemValue = new TextView(this);
					tvItemValue.setText(item.getBatteryVoltage() + "V");

					TableRow trItem = new TableRow(this);
					trItem.addView(tvItemLabel);
					trItem.addView(tvItemValue);

					tUps.addView(trItem);
				}

				if(item.getLineVoltage() != null) {

					TextView tvItemLabel = new TextView(this);
					tvItemLabel.setText(Html.fromHtml("<b>"+getString(R.string.lblUpsLine)+" </b>"));

					TextView tvItemValue = new TextView(this);
					tvItemValue.setText(item.getLineVoltage() + "V");

					TableRow trItem = new TableRow(this);
					trItem.addView(tvItemLabel);
					trItem.addView(tvItemValue);

					tUps.addView(trItem);
				}
			}

			llUps.addView(tUps);
			llPlugins.addView(llUps);
		}
	}	

	public void showSmart(PSIHostData entry) {

		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);

		if(entry.getSmart().size() > 0) {

			//header
			HeaderTextView tvSmart = new HeaderTextView(this);
			tvSmart.setId(R.id.tvSmart);
			tvSmart.setText(R.string.lblSmart);
			llPlugins.addView(tvSmart);

			tvSmart.setOnClickListener(this);

			//content
			TableLayout tSmart = new TableLayout(this);
			tSmart.setColumnShrinkable(1, true);
			tSmart.setId(R.id.tSmart);

			LinearLayout llSmart = new LinearLayout(this);
			llSmart.setId(R.id.llSmart);
			llSmart.setOrientation(LinearLayout.VERTICAL);

			List<PSISmart> items = entry.getSmart();

			String currentDisk = "";

			for (PSISmart item : items) {

				if(!currentDisk.equals(item.getDisk())) {
					currentDisk = item.getDisk();
					TextView tvItemLabel = new TextView(this);
					tvItemLabel.setTextColor(getResources().getColor(R.color.sub_item));
					tvItemLabel.setText(Html.fromHtml("<b>" + item.getDisk() + "</b>"));
					TableRow trItem = new TableRow(this);
					trItem.addView(tvItemLabel);
					tSmart.addView(trItem);
				}

				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml("<b>" + item.getAttribut() + ": </b>"));

				TextView tvItemValue = new TextView(this);

				String value = item.getValue();
				tvItemValue.setText(value);

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValue);

				tSmart.addView(trItem);
			}

			llSmart.addView(tSmart);
			llPlugins.addView(llSmart);
		}
	}

	public void showRaid(PSIHostData entry) {
		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);

		if(entry.getRaid().size() > 0) {

			//header
			HeaderTextView tvRaid = new HeaderTextView(this);
			tvRaid.setId(R.id.tvRaid);
			tvRaid.setText(getString(R.string.lblRaid));
			llPlugins.addView(tvRaid);

			tvRaid.setOnClickListener(this);

			//content
			TableLayout tRaid = new TableLayout(this);

			//tRaid.setColumnStretchable(0, true);
			//tRaid.setColumnStretchable(1, true);
			tRaid.setColumnShrinkable(1, true);
			tRaid.setId(R.id.tRaid);

			LinearLayout llRaid = new LinearLayout(this);
			llRaid.setId(R.id.llRaid);
			llRaid.setOrientation(LinearLayout.VERTICAL);


			//populate
			for (PSIRaid psiRaid : entry.getRaid()) {
				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml(
						"<b>" + psiRaid.getName() + " (" + psiRaid.getLevel() + ")</b>"));
				tvItemLabel.setTextColor(getResources().getColor(R.color.sub_item));

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				tRaid.addView(trItem);


				//disks
				trItem = new TableRow(this);
				tvItemLabel = new TextView(this);
				TextView tvItemValue = new TextView(this);

				String list = "[" + psiRaid.getDisksRegistered() + "/" + psiRaid.getDisksActive() + "]";

				for (PSIRaidDevice dev : psiRaid.getDevices()) {
					if(dev.getStatus().equals(""))
						list = list + " " + dev.getName();
					else {
						int color = getResources().getColor(R.color.state_hard);
						String сolorString = String.format("%X", color).substring(2);
						list = list + " " + String.format(
								"<font color=\"#%s\">"+dev.getName()+"</font>", сolorString);
					}
				}
				tvItemValue.setText(Html.fromHtml(list));

				trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValue);

				//tvItemLabel.setWidth(0);
				//tvItemValue.setWidth(0);

				tRaid.addView(trItem);
			}

			llRaid.addView(tRaid);
			llPlugins.addView(llRaid);
		}
	}

	public void showUpdate(PSIHostData entry) {
		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);

		if(entry.getNormalUpdate() != -1) {

			//header
			HeaderTextView tvUpdate = new HeaderTextView(this);
			tvUpdate.setId(R.id.tvUpdate);
			tvUpdate.setText(getString(R.string.lblUpdate));
			llPlugins.addView(tvUpdate);

			tvUpdate.setOnClickListener(this);

			//content
			TableLayout tUpdate = new TableLayout(this);
			tUpdate.setColumnShrinkable(0, true);
			tUpdate.setId(R.id.tUpdate);

			LinearLayout llUpdate = new LinearLayout(this);
			llUpdate.setId(R.id.llUpdate);
			llUpdate.setOrientation(LinearLayout.VERTICAL);

			//populate

			//normal
			TextView tvItemLabel = new TextView(this);
			tvItemLabel.setText(Html.fromHtml("<b>" + getString(R.string.lblPackages) + " </b>"));

			TextView tvItemValue = new TextView(this);
			tvItemValue.setText(entry.getNormalUpdate()+"");

			TableRow trItem = new TableRow(this);
			trItem.addView(tvItemLabel);
			trItem.addView(tvItemValue);

			tUpdate.addView(trItem);

			//security
			tvItemLabel = new TextView(this);
			tvItemLabel.setText(Html.fromHtml("<b>" + getString(R.string.lblSecurity) + " </b>"));

			tvItemValue = new TextView(this);
			tvItemValue.setText(entry.getSecurityUpdate()+"");

			trItem = new TableRow(this);
			trItem.addView(tvItemLabel);
			trItem.addView(tvItemValue);

			tUpdate.addView(trItem);			

			llUpdate.addView(tUpdate);
			llPlugins.addView(llUpdate);
		}
	}

	public void showPrinter(PSIHostData entry) {

		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);
		LayoutInflater inflater = getLayoutInflater();

		if(entry.getPrinter().size() > 0) {

			//header
			HeaderTextView tvPrinter = new HeaderTextView(this);
			tvPrinter.setId(R.id.tvPrinter);
			tvPrinter.setText(R.string.lblPrinter);
			llPlugins.addView(tvPrinter);

			tvPrinter.setOnClickListener(this);

			//content
			TableLayout tPrinter = new TableLayout(this);
			tPrinter.setColumnStretchable(0, true);
			tPrinter.setColumnStretchable(1, true);
			tPrinter.setColumnShrinkable(0, true);
			tPrinter.setId(R.id.tPrinter);

			LinearLayout llPrinter = new LinearLayout(this);
			llPrinter.setId(R.id.llPrinter);
			llPrinter.setOrientation(LinearLayout.VERTICAL);

			List<PSIPrinter> printers = entry.getPrinter();


			for (PSIPrinter printer : printers) {

				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setTextColor(getResources().getColor(R.color.sub_item));
				tvItemLabel.setText(Html.fromHtml("<b>" + printer.getName() + "</b>"));
				tvItemLabel.setWidth(0);

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				tPrinter.addView(trItem);

				List<PSIPrinterItem> items = printer.getItem();

				//MarkerSupplies
				for (PSIPrinterItem item : items) {
					tvItemLabel = new TextView(this);
					tvItemLabel.setText(Html.fromHtml("<b>"+item.getDescription()+"</b>"));

					TextView tvItemValue = new TextView(this);

					tvItemLabel.setWidth(0);
					tvItemValue.setWidth(0);

					//unit
					String unit = item.getSupplyUnit();
					if(unit.equals("19")) unit = getString(R.string.lblPercent);
					else if(unit.equals("15")) unit = getString(R.string.lblTenthsMl);
					else if(unit.equals("7")) unit = getString(R.string.lblImpressions);
					else if(unit.equals("13")) unit = getString(R.string.lblTenthsGrams);

					//value
					try {
						int level = Integer.parseInt(item.getLevel());
						int max = Integer.parseInt(item.getMaxCapacity());
						int percent = -1;

						if(level >= 0 && max >0 && level <= max) {
							percent = 100*level/max;
						}
						else if (max == -2 && level >= 0 && level <= 100) {
							percent = level;
							max = 100;
						}
						else if (level == -3) {
							percent = -1;
						}
						else {
							percent = -1;
						}

						String value = level + "/" + max + " ("+unit+")";
						tvItemValue.setText(value);
						trItem = new TableRow(this);
						trItem.addView(tvItemLabel);
						trItem.addView(tvItemValue);
						tPrinter.addView(trItem);

						//progressbar
						if(percent >=0 && percent <= 100) {
							ProgressBar pgPercent = (ProgressBar) inflater.inflate(R.layout.pg, null);
							pgPercent.setProgress(percent);
							TableRow trItem2 = new TableRow(this);
							trItem2.addView(new TextView(this));
							trItem2.addView(pgPercent);
							tPrinter.addView(trItem2);	
						}
					}
					catch(Exception e) {

					}
				}

				//Messages
				List<String> messages = printer.getMessages();
				for (String message : messages) {
					TextView tvItemLabelMessage = new TextView(this);
					tvItemLabelMessage.setText("-" + message);
					tvItemLabelMessage.setTextColor(getResources().getColor(R.color.state_soft));

					TableRow.LayoutParams params = new TableRow.LayoutParams();
					params.span = 2;
					tvItemLabelMessage.setLayoutParams(params);

					trItem = new TableRow(this);
					trItem.addView(tvItemLabelMessage);
					tvItemLabel.setWidth(0);

					tPrinter.addView(trItem);
				}
			}

			llPrinter.addView(tPrinter);
			llPlugins.addView(llPrinter);
		}
	}

	public void showBat(PSIHostData entry) {
		LinearLayout llPlugins = (LinearLayout) findViewById(R.id.llPlugins);

		if(entry.getBat() != null) {

			//header
			HeaderTextView tvBat = new HeaderTextView(this);
			tvBat.setId(R.id.tvBat);
			tvBat.setText(getString(R.string.lblBat));
			llPlugins.addView(tvBat);

			tvBat.setOnClickListener(this);

			//content
			TableLayout tBat = new TableLayout(this);
			tBat.setColumnStretchable(0, true);
			tBat.setColumnStretchable(1, true);
			tBat.setId(R.id.tBat);

			LinearLayout llBat = new LinearLayout(this);
			llBat.setId(R.id.llBat);
			llBat.setOrientation(LinearLayout.VERTICAL);

			//populate

			//remaining
			TextView tvItemLabel = new TextView(this);
			tvItemLabel.setText(Html.fromHtml("<b>" + getString(R.string.lblRemaining) + " </b>"));

			int percent = 0;
			try {
				percent = Integer.parseInt(entry.getBat().getCapacity());
			}
			catch (Exception e) {
				int remainingCapacity = 0;
				int designCapacity = 0;
				try {
					remainingCapacity = Integer.parseInt(entry.getBat().getRemainingCapacity());
					designCapacity = Integer.parseInt(entry.getBat().getDesignCapacity());
					percent = (int) (((float)remainingCapacity/designCapacity)*100);
				}
				catch (Exception ex) {
				}
			}

			TextView tvItemValue = new TextView(this);
			tvItemValue.setText(percent+"%");
			tvItemLabel.setWidth(0);
			tvItemValue.setWidth(0);
			TableRow trItem = new TableRow(this);
			trItem.addView(tvItemLabel);
			trItem.addView(tvItemValue);

			tBat.addView(trItem);

			//progressbar
			if(percent >=0 && percent <= 100) {
				LayoutInflater inflater = getLayoutInflater();
				ProgressBar pgPercent = (ProgressBar) inflater.inflate(R.layout.pg, null);
				pgPercent.setProgress(percent);

				TableRow trItemPg = new TableRow(this);
				trItemPg.addView(new TextView(this));
				trItemPg.addView(pgPercent);

				tBat.addView(trItemPg);
			}


			//state
			tvItemLabel = new TextView(this);
			tvItemLabel.setText(Html.fromHtml("<b>" + getString(R.string.lblState) + " </b>"));

			tvItemValue = new TextView(this);
			tvItemValue.setText(entry.getBat().getChargingState()+"");

			trItem = new TableRow(this);
			trItem.addView(tvItemLabel);
			trItem.addView(tvItemValue);

			tBat.addView(trItem);			

			llBat.addView(tBat);
			llPlugins.addView(llBat);
		}
	}

	private void loadDynamicLayout(int layoutID){
		// Get a reference to the main layout object in main.xml
		LinearLayout llmain = (LinearLayout) findViewById(R.id.llMain);
		llmain.removeAllViews();

		LayoutInflater inflater = getLayoutInflater();
		llmain.addView(inflater.inflate(layoutID, null));
	}

	public static Context getAppContext() {
		return PSIActivity.context;
	}


	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

		//load data
		selectedIndex = itemPosition;
		PSIConfig.getInstance().saveLastIndex(selectedIndex);
		getData(selectedIndex);
		Log.d("PSIAndroid","selectedIndex="+selectedIndex);

		return false;
	}


}
