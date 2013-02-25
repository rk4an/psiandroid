package com.phpsysinfo.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.phpsysinfo.PSIConfig;
import com.phpsysinfo.R;

public class HostEditActivity extends SherlockActivity
{
    public static final String ENTRY_EDIT_ID_KEY = "entry_edit_id_key";
    
    private int entry_position = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_edit);
        
        ActionBar bar = getSupportActionBar();
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) 
        {
            this.entry_position = extras.getInt(HostEditActivity.ENTRY_EDIT_ID_KEY);
            
            JSONObject host = null;
            
            try
            {
                host = PSIConfig.getInstance().getHostsList()
                        .getJSONObject(this.entry_position);
                
                ((EditText) findViewById(R.id.txtUrl)).setText(
                        host.get("url").toString());
                ((EditText) findViewById(R.id.txtUser)).setText(
                        host.get("username").toString());
                ((EditText) findViewById(R.id.txtPassword)).setText(
                        host.get("password").toString());
                
                bar.setTitle("Edit host");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            bar.setTitle("New host");
            entry_position = -1;
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.host_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.aiSaveHost)
        {
            if (saveAction())
            {
                // Host stored, redirect to hosts list
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private boolean saveAction()
    {
        EditText txtUrl = (EditText) findViewById(R.id.txtUrl);
        EditText txtUser = (EditText) findViewById(R.id.txtUser);
        EditText txtPasword = (EditText) findViewById(R.id.txtPassword);
        
        String hostUrl = txtUrl.getText().toString().trim();
        
        if (!hostUrl.startsWith("http://") && !hostUrl.startsWith("https://"))
        {
            hostUrl = "http://" + hostUrl;
        }
        
        // add URL to the list
        if (hostUrl.length() > 8 && URLUtil.isValidUrl(hostUrl))
        {
            Log.v("talalla", "Mon url est valide ...");
            //arrayAdapterUrlList.add(txtUrl.getText().toString());
            
            boolean res = false;
            
            if (this.entry_position == -1)
            {
                res = PSIConfig.getInstance().addHost(hostUrl, 
                        txtUser.getText().toString(), 
                        txtPasword.getText().toString()); 
            }
            else
            {
                // Update
                res = PSIConfig.getInstance().editHostAtIndex(
                        this.entry_position, hostUrl, 
                        txtUser.getText().toString(), 
                        txtPasword.getText().toString());
            }
            
            if (!res)
            {
                Toast.makeText(getApplicationContext(), 
                        getString(R.string.errSaveHost), 
                        Toast.LENGTH_SHORT).show();
            }
            else
                return true;
        }
        else
        {
            txtUrl.setError(getString(R.string.errPleaseEnterUrl));
        }
        
        return false;
    }
}
