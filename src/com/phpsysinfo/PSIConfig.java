package com.phpsysinfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;


public class PSIConfig {

    public final static String SCRIPT_NAME = "/xml.php?plugin=complete";
    public final static String HOSTS_JSON_STORE = "HOSTS_JSON_STORE";
    public final static String HOSTS_ENABLED_JSON_STORE = "HOSTS_ENABLED_JSON_STORE";
    public final static String JSON_CURRENT_HOST = "JSON_CURRENT_HOST";
    public final static int MEMORY_SOFT_THR = 80;
    public final static int MEMORY_HARD_THR = 90;
    public final static int COLOR_SOFT = 0xFFFF530D;
    public final static int COLOR_HARD = 0xFFE80E0C;
    public final static int COLOR_OK = 0xFF0CE88E;
    
    /****/
    
    private static PSIConfig instance = null;
    
    public static PSIConfig getInstance()
    {
        if (instance == null)
            instance = new PSIConfig();
        return instance;
    }
    
    /****/
    private SharedPreferences pref;

    private JSONArray hostsList = null;
    private JSONArray enabledHosts = null;
    
    private PSIConfig()
    {
        this.pref = PreferenceManager.getDefaultSharedPreferences(PSIAndroid.getContext());
        
        configurationUpdater();
        
        loadHostsList();
        loadEnabledHostsList();
    }
    
    public JSONArray getHostsList()
    {
        return this.hostsList;
    }
    
    public JSONObject getHost(int index)
    {
        Log.v("PSIConfig", "Try to get host with index:" + index + " (count: " + this.hostsList.length() + ")");
        return this.hostsList.optJSONObject(index);
    }
    
    public boolean addHost(String url, String username, String password)
    {
        try
        {
            JSONObject host = new JSONObject();
            host.put("url", url);
            host.put("username", username);
            host.put("password", password);
            
            this.hostsList.put(host);
            saveHostsList();
            
            return true;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }

    public boolean editHostAtIndex(int index, String url, String username, String password)
    {
        try
        {
            JSONObject host = (JSONObject) this.hostsList.get(index);
            host.put("url", url);
            host.put("username", username);
            host.put("password", password);
            
            saveHostsList();
            
            return true;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return false;
    }
    
    public void deleteHostAtIndex(int index)
    {
        try
        {
            JSONArray newArray = new JSONArray();
            
            // No way to remove entry from JSONArray, so place good entries into
            // a new JSONArray
            
            for (int i = 0; i < this.hostsList.length(); i++) 
            {
                if (i != index) 
                {
                    newArray.put(this.hostsList.get(i));
                }
            }
            
            this.hostsList = newArray;
            saveHostsList();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    
    private void loadHostsList()
    {
        this.hostsList = null;
        
        String dataStore = pref.getString(PSIConfig.HOSTS_JSON_STORE, "");

        if (dataStore.equals("")) 
        {
            this.hostsList = new JSONArray();
        }
        else 
        {
            try
            {
                Log.v("PSIconfig", dataStore);
                this.hostsList = new JSONArray(new JSONTokener(dataStore));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                this.hostsList = new JSONArray();
            }
        }
    }
    
    private void saveHostsList()
    {
        String dataStore = "";
        try 
        {
            dataStore = this.hostsList.toString();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }

        Editor editor = pref.edit();
        editor.putString(PSIConfig.HOSTS_JSON_STORE, dataStore);
        editor.commit();
    }
    
    /****/
    
    /**
     * Returns the enabled host index
     * @return the enabled host index
     */
    public int getEnabledHost()
    {
        // Already managing an array for future developments
        return this.enabledHosts.optInt(0, -1);
    }
    
    /**
     * Sets the enabled host index
     * @param index the enabled host index
     */
    public void setEnabledHost(int index)
    {
        try
        {
            this.enabledHosts.put(0, index);
            saveEnabledHostsList();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    
    private void loadEnabledHostsList()
    {
        this.enabledHosts = null;
        
        String dataStore = pref.getString(PSIConfig.HOSTS_ENABLED_JSON_STORE, "");

        if (dataStore.equals("")) 
        {
            this.enabledHosts = new JSONArray();
        }
        else 
        {
            try
            {
                this.enabledHosts = new JSONArray(new JSONTokener(dataStore));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                this.enabledHosts = new JSONArray();
            }
        }
    }
    

    
    private void saveEnabledHostsList()
    {
        String dataStore = "";
        try 
        {
            dataStore = this.enabledHosts.toString();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }

        Editor editor = pref.edit();
        editor.putString(PSIConfig.HOSTS_ENABLED_JSON_STORE, dataStore);
        editor.commit();
    }
    
    
    /*************/
    
    /**
     * Convert old configuration format to current format
     * NOTE: this method should be removed in a near future
     */
    private void configurationUpdater()
    {
        // Note this part should be remove on future versions
        String oldUrl = pref.getString("listUrl", "");
        if(!oldUrl.equals("")) 
        {
            String[] ou = oldUrl.split(";");
            PSIConfig config = PSIConfig.getInstance();

            for (int i = 0; i<ou.length; i++) 
            {
                if(!ou[i].equals("")) 
                {
                    config.addHost(ou[i], "", "");
                }
            }

            // Remove old entry
            Editor editor = pref.edit();
            editor.remove("listUrl");
            editor.commit();
        }
    }
}
