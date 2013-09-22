package com.phpsysinfo.activity;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.phpsysinfo.R;
import com.phpsysinfo.ui.HeaderTextView;
import com.phpsysinfo.utils.FormatUtils;
import com.phpsysinfo.xml.PSIDownloadData;
import com.phpsysinfo.xml.PSIErrorCode;
import com.phpsysinfo.xml.PSIHostData;
import com.phpsysinfo.xml.PSIMountPoint;
import com.phpsysinfo.xml.PSINetworkInterface;
import com.phpsysinfo.xml.PSIPrinter;
import com.phpsysinfo.xml.PSIRaid;
import com.phpsysinfo.xml.PSISmart;
import com.phpsysinfo.xml.PSIUps;

enum ViewType {
	NONE, LOGO, ERROR, DATA, LOADING
}

public class PSIActivity 
extends SherlockActivity
implements OnClickListener, View.OnTouchListener
{
	private static Context context;

	private Dialog aboutDialog = null;
	private ScrollView scrollView;

	private int selectedIndex = 0 ;
	private boolean isReady = false;

	private MenuItem refreshItem;
	private ImageView iv;
	private Animation rotation;

	private ViewType viewType = ViewType.NONE;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PSIActivity.context = getApplicationContext();

		setContentView(R.layout.main_view);

		scrollView = (ScrollView) findViewById(R.id.svMain);

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		iv = (ImageView) inflater.inflate(R.layout.action_refresh, null);

		rotation = AnimationUtils.loadAnimation(this, R.anim.clockwise_refresh);
		rotation.setAnimationListener(rotateListener);

		scrollView.setOnTouchListener(this);

		//create about dialog
		aboutDialog = new Dialog(this);
		aboutDialog.setContentView(R.layout.about_dialog);
		aboutDialog.setTitle("PSIAndroid");
		TextView text = (TextView) aboutDialog.findViewById(R.id.text);
		text.setText("http://phpsysinfo.sf.net");
		text.append("\nPlease rate this app");
		ImageView image = (ImageView) aboutDialog.findViewById(R.id.image);
		image.setImageResource(R.drawable.ic_launcher);
		image.setOnClickListener(this);

		displayLogo();

		//load data
		int selectedIndex = PSIConfig.getInstance().loadLastIndex();
		getData(selectedIndex);
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	public void onResume() {
		super.onResume();
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
			toggleContent((View) findViewById(R.id.tMemory));
		}
		else if(event.getId() == R.id.tvMountPoints) {
			toggleContent((View) findViewById(R.id.tMountPoints));
		}
		else if(event.getId() == R.id.tvTemperature) {
			toggleContent((View) findViewById(R.id.llTemperature));
		}
		else if(event.getId() == R.id.tvNetwork) {
			toggleContent((View) findViewById(R.id.llNetwork));
		}
		else if(event.getId() == R.id.tvProcessStatus) {
			toggleContent((View) findViewById(R.id.llProcessStatus));
		}
		else if(event.getId() == R.id.tvFans) {
			toggleContent((View) findViewById(R.id.llFans));
		}
		else if(event.getId() == R.id.tvUps) {
			toggleContent((View) findViewById(R.id.llUps));
		}
		else if(event.getId() == R.id.tvSmart) {
			toggleContent((View) findViewById(R.id.llSmart));
		}
		else if(event.getId() == R.id.tvRaid) {
			toggleContent((View) findViewById(R.id.llRaid));
		}
		else if(event.getId() == R.id.tvUpdate) {
			toggleContent((View) findViewById(R.id.llUpdate));
		}
		else if(event.getId() == R.id.tvPrinter) {
			toggleContent((View) findViewById(R.id.llPrinter));
		}
	}

	public void toggleContent(final View v){

		v.setVisibility( v.isShown()? View.GONE: View.VISIBLE );

		/*Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
		Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

		slideUp.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				v.setVisibility(View.GONE);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
		});

		slideDown.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {
				v.setVisibility(View.VISIBLE);
			}
		});


		if(v.isShown()) {
			v.startAnimation(slideUp);
		}
		else {
			v.startAnimation(slideDown);
		}*/
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

		isReady = true;

		if (viewType != ViewType.DATA) {
			loadDynamicLayout(R.layout.data_view);
			((TextView) findViewById(R.id.tvMemoryUsage)).setOnClickListener(this);
			((TextView) findViewById(R.id.tvMountPoints)).setOnClickListener(this);
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

		//uptime
		TextView txtUptime = (TextView) findViewById(R.id.txtUptime);
		txtUptime.setText(entry.getUptime());

		//load avg
		TextView txtLoad = (TextView) findViewById(R.id.txtLoad);
		txtLoad.setText(entry.getLoadAvg());

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
		//TextView txtCpu = (TextView) findViewById(R.id.txtCpu);
		//txtCpu.setText(entry.getCpu());

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
		LayoutInflater inflater = getLayoutInflater();

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

		LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

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
	}

	/**
	 * 
	 * @param error
	 */
	public void displayError(String host, PSIErrorCode error) {

		isReady = true;

		if (this.viewType != ViewType.ERROR) {
			loadDynamicLayout(R.layout.error_view);
		}

		TextView txtHostname = (TextView) findViewById(R.id.errortxt);
		txtHostname.setText(Html.fromHtml(host));

		TextView errorCode = (TextView) findViewById(R.id.errorcode);
		errorCode.setText("Error: " + error.toString());
	}


	public void refresh() {
		iv.startAnimation(rotation);
		isReady = false;
		if(refreshItem != null) {
			refreshItem.setActionView(iv);
		}
	}


	public void completeRefresh() {
		if(refreshItem != null) {
			if(refreshItem.getActionView() != null) {
				refreshItem.getActionView().clearAnimation();
				refreshItem.setActionView(null);
			}
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {

			//load new selected host
			displayLoadingMessage();
			selectedIndex = data.getExtras().getInt("host");
			getData(selectedIndex);
			PSIConfig.getInstance().saveLastIndex(selectedIndex);
		}
		else {

		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);
		refreshItem = menu.findItem(R.id.iRefresh);
		return true;
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
			startActivityForResult(i,0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	Float firstX = null;

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		JSONArray hostsJsonArray = PSIConfig.getInstance().loadHosts();


		if(!isReady) {
			Log.d("PSIAndroid","Cancel swipe");
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
					displayLoadingMessage();
					getData(selectedIndex);

					PSIConfig.getInstance().saveLastIndex(selectedIndex);

					firstX = null;
					return false;
				}
			}	
		}

		return true;
	}


	private void displayLoadingMessage() {
		loadDynamicLayout(R.layout.loading);
		viewType = ViewType.LOADING;
	}


	public void getData(int index) {

		JSONArray hostsList = PSIConfig.getInstance().loadHosts();
		JSONObject currentHost = null;
		try {
			if(index < hostsList.length()) {

				currentHost = (JSONObject) hostsList.get(index);
				String url = currentHost.getString("url");
				String user = currentHost.getString("username");
				String password = currentHost.getString("password");

				PSIDownloadData task = new PSIDownloadData(this);
				this.refresh();
				if(!url.equals("")) {
					task.execute(url + PSIConfig.SCRIPT_NAME, user, password);
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

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValueRx);
				trItem.addView(tvItemValueTx);

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
			tTemperature.setId(R.id.tTemperature);

			LinearLayout llTemperature = new LinearLayout(this);
			llTemperature.setId(R.id.llTemperature);
			llTemperature.setOrientation(LinearLayout.VERTICAL);

			TreeSet<String> keys = new TreeSet<String>(entry.getTemperature().keySet());

			//populate
			for (String mapKey : keys) {
				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml("<b>" + mapKey + ": </b>"));

				TextView tvItemValue = new TextView(this);
				tvItemValue.setText(entry.getTemperature().get(mapKey));

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValue);

				tTemperature.addView(trItem);
			}

			llTemperature.addView(tTemperature);
			llPlugins.addView(llTemperature);
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
			tRaid.setColumnShrinkable(0, true);
			tRaid.setId(R.id.tRaid);

			LinearLayout llRaid = new LinearLayout(this);
			llRaid.setId(R.id.llRaid);
			llRaid.setOrientation(LinearLayout.VERTICAL);

			//header
			/*TextView tvItemLabelHeader = new TextView(this);
			tvItemLabelHeader.setText(Html.fromHtml(
					"<b><i>"+getString(R.string.lblRaidDevice)+" </i></b>"));

			TextView tvItemValueHeader = new TextView(this);
			tvItemValueHeader.setText(Html.fromHtml(
					"<i>"+getString(R.string.lblRaidActiveRegistered)+"</i>"));

			TableRow trItemHeader = new TableRow(this);
			trItemHeader.addView(tvItemLabelHeader);
			trItemHeader.addView(tvItemValueHeader);

			tRaid.addView(trItemHeader);*/


			//populate
			for (PSIRaid psiRaid : entry.getRaid()) {
				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml("<b>" + psiRaid.getName() + ": </b>"));

				TextView tvItemValue = new TextView(this);
				tvItemValue.setText(psiRaid.getDisks_active()+"/"+psiRaid.getDisks_registered());

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValue);

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

		if(entry.getPrinter().size() > 0) {

			//header
			HeaderTextView tvPrinter = new HeaderTextView(this);
			tvPrinter.setId(R.id.tvPrinter);
			tvPrinter.setText(R.string.lblPrinter);
			llPlugins.addView(tvPrinter);

			tvPrinter.setOnClickListener(this);

			//content
			TableLayout tPrinter = new TableLayout(this);
			tPrinter.setColumnShrinkable(1, true);
			tPrinter.setId(R.id.tPrinter);

			LinearLayout llSmart = new LinearLayout(this);
			llSmart.setId(R.id.llPrinter);
			llSmart.setOrientation(LinearLayout.VERTICAL);

			List<PSIPrinter> items = entry.getPrinter();

			String currentDisk = "";

			for (PSIPrinter item : items) {

				if(!currentDisk.equals(item.getPrinter())) {
					currentDisk = item.getPrinter();
					TextView tvItemLabel = new TextView(this);
					tvItemLabel.setTextColor(getResources().getColor(R.color.sub_item));
					tvItemLabel.setText(Html.fromHtml("<b>" + item.getPrinter() + "</b>"));
					TableRow trItem = new TableRow(this);
					trItem.addView(tvItemLabel);
					tPrinter.addView(trItem);
				}

				TextView tvItemLabel = new TextView(this);
				tvItemLabel.setText(Html.fromHtml("<b>"+item.getDescription()+"</b>"));

				TextView tvItemValue = new TextView(this);

				String unit = item.getSupplyUnit();
				if(unit.equals("19")) unit = getString(R.string.lblPercent);
				else if(unit.equals("15")) unit = getString(R.string.lblTenthsMl);
				else if(unit.equals("7")) unit = getString(R.string.lblImpressions);

				String value = getString(R.string.lblSupplyUnit) + " " + unit + " \n" 
						+ getString(R.string.lblLevel) + " " + item.getLevel() + " " + getString(R.string.lblOf) + " " + item.getMaxCapacity();
				tvItemValue.setText(value);

				TableRow trItem = new TableRow(this);
				trItem.addView(tvItemLabel);
				trItem.addView(tvItemValue);

				tPrinter.addView(trItem);
			}

			llSmart.addView(tPrinter);
			llPlugins.addView(llSmart);
		}
	}

	private void loadDynamicLayout(int layoutID){
		// Get a reference to the main layout object in main.xml
		LinearLayout llmain = (LinearLayout) findViewById(R.id.llMain);
		llmain.removeAllViews();

		LayoutInflater inflater = getLayoutInflater();
		llmain.addView(inflater.inflate(layoutID, null));
	}

	private AnimationListener rotateListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {}

		@Override
		public void onAnimationRepeat(Animation animation) {}

		@Override
		public void onAnimationEnd(Animation animation) {
			if(isReady) {
				iv.clearAnimation();
				refreshItem.setActionView(null);
			}
			else { 
				iv.startAnimation(rotation);
			}
		}
	};

	public static Context getAppContext() {
		return PSIActivity.context;
	}
}
