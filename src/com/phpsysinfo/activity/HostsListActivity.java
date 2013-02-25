package com.phpsysinfo.activity;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.phpsysinfo.PSIConfig;
import com.phpsysinfo.R;

public class HostsListActivity
    extends SherlockActivity 
    implements OnItemClickListener, OnItemLongClickListener 
{
	ListView listViewUrls = null;
	ArrayAdapter<String> arrayAdapterUrlList = null;
	int pos = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hosts_list);
		
	    ActionBar bar = getSupportActionBar();
	    bar.setTitle(getString(R.string.lblHosts));

		listViewUrls = (ListView) findViewById(R.id.listView1);
		listViewUrls.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listViewUrls.setSelection(0);
		listViewUrls.setOnItemClickListener(this);
		listViewUrls.setOnItemLongClickListener(this);

		arrayAdapterUrlList = new ArrayAdapter<String>(this, R.layout.mylist);
	}
	/**
	 * onResume, reset the list of saved hosts
	 */
	@Override
	protected void onResume()
	{
	    super.onResume();
	    
	    arrayAdapterUrlList.clear();
	    
        JSONArray hostsJsonArray = PSIConfig.getInstance().getHostsList();
        
        for (int i = 0; i < hostsJsonArray.length(); i++) 
        {
            try 
            {
                String url = hostsJsonArray.getJSONObject(i).getString("url");
                arrayAdapterUrlList.add(url);
            }
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        }

        
        listViewUrls.setAdapter(arrayAdapterUrlList);
        
        // Check the currently used host
        int enabledHost = PSIConfig.getInstance().getEnabledHost();
        if (enabledHost != -1)
        {
            listViewUrls.setItemChecked(enabledHost, true);
        }

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) 
	{
	    // Save selected host
	    PSIConfig.getInstance().setEnabledHost(position);
		finish();
	}

	/**
	 * 
	 */
	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) 
		{
			switch (which)
			{
        		case DialogInterface.BUTTON_POSITIVE:
        			// delete item and save the list
                    PSIConfig.getInstance().deleteHostAtIndex(pos);
                    arrayAdapterUrlList.remove((String) listViewUrls.getItemAtPosition(pos));
                    
                    // If we remove an enabled host, unselect it
                    int enabled = PSIConfig.getInstance().getEnabledHost();
                    if (enabled == pos)
                        PSIConfig.getInstance().setEnabledHost(-1);
        			break;
        			
        		case DialogInterface.BUTTON_NEUTRAL:
                    // Edit item
                    Intent i = new Intent(getApplicationContext(), HostEditActivity.class);
                    i.putExtra(HostEditActivity.ENTRY_EDIT_ID_KEY, pos);
                    startActivityForResult(i, 0);
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
		AlertDialog.Builder adb = new AlertDialog.Builder(getApplicationContext());
		adb.setMessage("Action for " + listViewUrls.getItemAtPosition(position)
				+ " ?");
		adb.setPositiveButton("Remove", dialogClickListener);
		adb.setNegativeButton("Cancel", dialogClickListener);
		adb.setNeutralButton("Edit", dialogClickListener);
		adb.show();

		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.hosts_list, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.aiAddHost)
        {
            Intent i = new Intent(this, HostEditActivity.class);
            startActivityForResult(i,0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
