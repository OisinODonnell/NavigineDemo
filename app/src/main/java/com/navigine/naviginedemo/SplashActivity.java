package com.navigine.naviginedemo;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.util.*;
import java.lang.*;
import java.io.*;
import java.util.*;

import com.navigine.naviginesdk.*;

public class SplashActivity extends Activity
{
  private static final String TAG = "NAVIGINE.Demo";


  
  private TextView mErrorLabel = null;
  
  class InitTask extends AsyncTask<Void, Void, Boolean>
  {
    private Context mContext  = null;
    private String  mErrorMsg = null;
    
    public InitTask(Context context)
    {
      mContext = context.getApplicationContext();
    }
    
    @Override protected Boolean doInBackground(Void... params)

//      @Override protected Boolean doInBackground(Void... params) {
//        return NavigineSDK.loadLocation(LOCATION_NAME, 30) ?
//                Boolean.TRUE : Boolean.FALSE;
//      }

    {
      try { Thread.sleep(1000); } catch ( Throwable e) { }
      if (!DemoApp.initialize(getApplicationContext()))
      {
        mErrorMsg = "Error downloading location 'Navigine Demo'! Please, try again later or contact technical support";
        return Boolean.FALSE;
      }
      Log.d(TAG, "Initialized!");
      if (!NavigineSDK.loadLocation(DemoApp.LOCATION_ID, 30))
      {
        mErrorMsg = "Error downloading location 'Navigine Demo'! Please, try again later or contact technical support";
        return Boolean.FALSE;
      }
      return Boolean.TRUE;
    }
    
    @Override protected void onPostExecute(Boolean result)
    {
      if (result.booleanValue())
      {
        // Starting main activity
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
      }
      else
      {
        Log.d(TAG, mErrorMsg);
        mErrorLabel.setText(mErrorMsg);
        mErrorLabel.setVisibility(View.VISIBLE);
      }
    }
  }
  
  @Override public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_splash);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                         WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    mErrorLabel = (TextView)findViewById(R.id.splash__error_label);
    mErrorLabel.setVisibility(View.GONE);
    
    (new InitTask(this)).execute();
  }

  @Override public void onBackPressed()
  {
    moveTaskToBack(true);
  }
}
