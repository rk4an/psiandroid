package com.phpsysinfo;

import android.app.Application;
import android.content.Context;

public class PSIAndroid extends Application
{
    private static Context context = null;
    
    public static Context getContext()
    {
        return context;
    }
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        if (PSIAndroid.context == null)
            PSIAndroid.context = getApplicationContext();
        
        // Initialize configuration
        PSIConfig.getInstance();
    }
}
