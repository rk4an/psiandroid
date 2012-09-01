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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
	private JSONArray hostsJsonArray = new JSONArray();

	private ImageView ivLogo = null;
	boolean ivLogoDisplay = true;
	private Dialog aboutDialog = null;
	private Dialog errorDialog = null;
	private TextView textError = null;
	private ScrollView scrollView;

	//current selected url
	private String currentHost = "";
	private int selectedIndex = 0 ;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);


		scrollView = (ScrollView) findViewById(R.id.scrollView1);
		ivLogo = new ImageView(this);
		ivLogo.setImageResource(R.drawable.psilogo);

		LinearLayout llLogo = (LinearLayout) findViewById(R.id.llLogo);
		llLogo.addView(ivLogo);


		//get preference
		pref = PreferenceManager.getDefaultSharedPreferences(this);


		/***************************************************************/
		//convert old format of storing
		String oldUrl = pref.getString("listUrl", "");
		if(!oldUrl.equals("")) {
			String[] ou = oldUrl.split(";");

			for (int i = 0; i<ou.length; i++) {
				if(!ou[i].equals("")) {
					try {
						JSONObject host = new JSONObject();
						host.put("url",ou[i]);
						host.put("username", "");
						host.put("password", "");
						hostsJsonArray.put(host);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			//save new store and erase old
			Editor editor = pref.edit();
			editor.putString(PSIConfig.HOSTS_JSON_STORE, hostsJsonArray.toString());
			editor.putString("listUrl", "");
			editor.commit();
		}
		/***************************************************************/


		//get preference
		currentHost = pref.getString(PSIConfig.JSON_CURRENT_HOST, "");

		//load data
		getData(currentHost);
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

		((TextView) findViewById(R.id.tvMemoryUsage)).setOnClickListener(this);
		((TextView) findViewById(R.id.tvMountPoints)).setOnClickListener(this);

	}

	@Override
	public void onClick(View event) {
		if(event.getId() == R.id.image) {
			aboutDialog.hide();
		}
		else if(event.getId() == R.id.imageError) {
			errorDialog.hide();
		}
		else if(event.getId() == R.id.tvMemoryUsage) {
			TableLayout tMemory = (TableLayout) findViewById(R.id.tMemory);
			if(tMemory.getVisibility() == TableLayout.VISIBLE) {
				tMemory.setVisibility(TableLayout.GONE);
			}
			else {
				tMemory.setVisibility(TableLayout.VISIBLE);
			}
		}
		else if(event.getId() == R.id.tvMountPoints) {
			TableLayout tMountPoints = (TableLayout) findViewById(R.id.tMountPoints);
			if(tMountPoints.getVisibility() == TableLayout.VISIBLE) {
				tMountPoints.setVisibility(TableLayout.GONE);
			}
			else {
				tMountPoints.setVisibility(TableLayout.VISIBLE);
			}
		}
		else if(event.getId() == R.id.tvIpmi) {
			LinearLayout llIpmi = (LinearLayout) findViewById(R.id.llIpmi);
			if(llIpmi.getVisibility() == LinearLayout.VISIBLE) {
				llIpmi.setVisibility(LinearLayout.GONE);
			}
			else {
				llIpmi.setVisibility(LinearLayout.VISIBLE);
			}
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

		String url = "";
		try {
			JSONTokener tokener = new JSONTokener(currentHost);
			JSONObject sHost = new JSONObject(tokener);
			url = sHost.getString("url");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		txtHostname.setText(
				Html.fromHtml("<a href=\""+url+"\">"+entry.getHostname()+"</a>"));
		txtHostname.setMovementMethod(LinkMovementMethod.getInstance());

		//uptime
		TextView txtUptime = (TextView) findViewById(R.id.txtUptime);
		txtUptime.setText(entry.getUptime());

		//load avg
		TextView txtLoad = (TextView) findViewById(R.id.txtLoad);
		txtLoad.setText(entry.getLoadAvg());

		//psi version
		TextView txtVersion = (TextView) findViewById(R.id.txtVersion);
		txtVersion.setText(entry.getPsiVersion());
		
		//kernel version
		TextView txtKernel = (TextView) findViewById(R.id.txtKernel);
		txtKernel.setText(entry.getKernel());

		//distro name
		TextView txtDistro = (TextView) findViewById(R.id.txtDistro);
		txtDistro.setText(entry.getDistro());

		//ip address
		TextView txtIp = (TextView) findViewById(R.id.txtIp);
		txtIp.setText(entry.getIp());

		//init mount point table
		TableLayout tMountPoints = (TableLayout) findViewById(R.id.tMountPoints);	
		tMountPoints.removeAllViews();

		//memory
		ProgressBar pbMemory = new ProgressBar(
				this,null,android.R.attr.progressBarStyleHorizontal);

		TextView tvNameMemory = new TextView(this);
		pbMemory.setProgress(entry.getAppMemoryPercent());
		tvNameMemory.setText(Html.fromHtml(
				"<b>"+getString(R.string.lblMemory) + "</b>" +

		" (" + getFormatedMemory(entry.getAppMemoryUsed()) + 
		" of " + getFormatedMemory(entry.getAppMemoryTotal()) + ") <i>" + 
		entry.getAppMemoryPercent()+"%</i>"));


		//text in yellow if memory usage is high
		if(entry.getAppMemoryPercent() > PSIConfig.MEMORY_SOFT_THR) {
			tvNameMemory.setTextColor(PSIConfig.COLOR_SOFT);
		}

		//text in red if memory usage is very high
		if(entry.getAppMemoryPercent() > PSIConfig.MEMORY_HARD_THR) {
			tvNameMemory.setTextColor(PSIConfig.COLOR_HARD);
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
			ProgressBar pgPercent = new ProgressBar(
					this,null,android.R.attr.progressBarStyleHorizontal);

			TextView tvName = new TextView(this);
			pgPercent.setProgress(psiMp.getPercentUsed());

			String lblMountText = "<b>" + psiMp.getName() + "</b>";

			lblMountText += " (" + getFormatedMemory(psiMp.getUsed()) + 
					"&nbsp;of&nbsp;" + getFormatedMemory(psiMp.getTotal()) + ")&nbsp;<i>"+ psiMp.getPercentUsed()+"%</i>";

			tvName.setText(Html.fromHtml(lblMountText));

			//text in yellow if mount point usage is high
			if(psiMp.getPercentUsed() > PSIConfig.MEMORY_SOFT_THR) {
				tvName.setTextColor(PSIConfig.COLOR_SOFT);
			}

			//text in red if mount point usage is very high
			if(psiMp.getPercentUsed() > PSIConfig.MEMORY_HARD_THR) {
				tvName.setTextColor(PSIConfig.COLOR_HARD);
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
		
		
		//IMPI section
		if(entry.getImpi().size() > 0) {
			//header
			TextView tvIpmi = new TextView(this);
			tvIpmi.setId(R.id.tvIpmi);
			tvIpmi.setText("IPMI");
			tvIpmi.setTypeface(null,Typeface.BOLD);
			tvIpmi.setPadding(5, 5, 5, 5);
		
			LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			llp.setMargins(0, 5, 0, 5);
			tvIpmi.setLayoutParams(llp);
			
			tvIpmi.setBackgroundColor(Color.parseColor("#444242"));
			llPlugins.addView(tvIpmi);
			
			tvIpmi.setOnClickListener(this);
			
			//content
			LinearLayout llImpi = new LinearLayout(this);
			llImpi.setId(R.id.llIpmi);
			llImpi.setOrientation(LinearLayout.VERTICAL);
			llPlugins.addView(llImpi);
			
			//populate IMPI content
			for (String mapKey : entry.getImpi().keySet()) {
				TextView tvItem = new TextView(this);
				tvItem.setText(Html.fromHtml("<b>" + mapKey + "</b>: " + entry.getImpi().get(mapKey)));
				llImpi.addView(tvItem);
			}
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
			LinearLayout llLogo = (LinearLayout) findViewById(R.id.llLogo);
			llLogo.setVisibility(LinearLayout.GONE);

			LinearLayout llContent = (LinearLayout) findViewById(R.id.llContent);
			llContent.setVisibility(LinearLayout.VISIBLE);

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

			//load new selected host
			currentHost = data.getExtras().getString("host");
			getData(currentHost);
			loadHostsArray();

			//save new selected host
			Editor editor = pref.edit();
			editor.putString(PSIConfig.JSON_CURRENT_HOST,currentHost);
			editor.commit();
		}
		else {
			//just update list of hosts
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
			getData(currentHost);
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

					//load the previous/next host
					getData(currentHost);

					Editor editor = pref.edit();
					editor.putString(PSIConfig.JSON_CURRENT_HOST,currentHost);
					editor.commit();

					firstX = null;
					return false;
				}
			}	
		}

		return true;
	}


	/**
	 * load list of hosts
	 */
	public void loadHostsArray() {
		try {
			String dataStore = pref.getString(PSIConfig.HOSTS_JSON_STORE, "");

			if (dataStore.equals("")) {
				hostsJsonArray = new JSONArray();
			}
			else {
				JSONTokener tokener = new JSONTokener(dataStore);
				hostsJsonArray = new JSONArray(tokener);
			}

			JSONTokener tokener = new JSONTokener(currentHost);
			JSONObject sHost = new JSONObject(tokener);

			//get index of current selected host
			for(int i=0; i<hostsJsonArray.length(); i++) {
				String u = ((JSONObject)hostsJsonArray.get(i)).getString("url");

				String url = sHost.getString("url");
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

		try {
			JSONTokener tokener = new JSONTokener(currentHost);
			JSONObject sHost = new JSONObject(tokener);
			String url = sHost.getString("url");
			String user = sHost.getString("username");
			String password = sHost.getString("password");

			if(!url.equals("")) {
				task.execute(url + PSIConfig.SCRIPT_NAME, user, password);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 * @param memory
	 * @return
	 */
	public String getFormatedMemory(int memory) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		String value = "0";

		if(memory > 1024) {
			value = nf.format((float)memory/1024) + "&nbsp;" + getString(R.string.lblGio);
		}
		else {
			value = nf.format(memory) + "&nbsp;" + getString(R.string.lblMio);
		}

		return value;
	}


}
