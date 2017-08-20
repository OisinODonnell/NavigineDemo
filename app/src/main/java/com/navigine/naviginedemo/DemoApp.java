package com.navigine.naviginedemo;
import com.navigine.naviginesdk.*;

import android.app.*;
import android.content.*;
import android.os.AsyncTask;
import android.util.*;
import android.widget.Toast;

import java.lang.*;
import java.util.*;

import static com.navigine.naviginesdk.LocationView.TAG;

public class DemoApp extends Application
{
  public static final String      TAG           = "NAVIGINE.Demo";
  public static final String      SERVER_URL    = "https://api.navigine.com";
  public static final String      USER_HASH     = "6DED-703A-4FEB-B87B"; //6DED-703A-4FEB-B87B // default was 0000-0000-0000-0000
  public static final int         LOCATION_ID   = 2038; // 64019 is DIT version 24 // 64533 is version 25 // DEFAULT 1603 // Alexey suggested 2038

  
  public static NavigationThread  Navigation    = null;
  
  // Screen parameters
  public static float DisplayWidthPx            = 0.0f;
  public static float DisplayHeightPx           = 0.0f;
  public static float DisplayWidthDp            = 0.0f;
  public static float DisplayHeightDp           = 0.0f;
  public static float DisplayDensity            = 0.0f;

  @Override public void onCreate()
  {
    super.onCreate();
  }

  
  public static boolean initialize(Context context)
  {

    NavigineSDK.setParameter(context, "debug_level", 2);
    NavigineSDK.setParameter(context, "apply_server_config_enabled",  false);
    NavigineSDK.setParameter(context, "actions_updates_enabled",      false);
    NavigineSDK.setParameter(context, "location_updates_enabled",     true);
    NavigineSDK.setParameter(context, "location_update_timeout",      30);
    NavigineSDK.setParameter(context, "post_beacons_enabled",         true);
    NavigineSDK.setParameter(context, "post_messages_enabled",        true);
    if (!NavigineSDK.initialize(context, USER_HASH, SERVER_URL))
      return false;
    
    Navigation = NavigineSDK.getNavigation();
    DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    DisplayWidthPx  = displayMetrics.widthPixels;
    DisplayHeightPx = displayMetrics.heightPixels;
    DisplayDensity  = displayMetrics.density;
    DisplayWidthDp  = DisplayWidthPx  / DisplayDensity;
    DisplayHeightDp = DisplayHeightPx / DisplayDensity;
    
    Log.d(TAG, String.format(Locale.ENGLISH, "Display size: %.1fpx x %.1fpx (%.1fdp x %.1fdp, density=%.2f)",
                             DisplayWidthPx, DisplayHeightPx,
                             DisplayWidthDp, DisplayHeightDp,
                             DisplayDensity));


    return true;
  }
  
  public static void finish()
  {
    if (Navigation == null)
      return;
    
    NavigineSDK.finish();
    Navigation = null;
  }

}



