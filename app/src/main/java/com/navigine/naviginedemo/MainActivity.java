package com.navigine.naviginedemo;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.ImageView.*;
import android.util.*;
import java.io.*;
import java.lang.*;
import java.util.*;

import com.navigine.naviginesdk.*;

public class MainActivity extends Activity
{
  private static final String   TAG                     = "NAVIGINE.Demo";
  private static final int      UPDATE_TIMEOUT          = 100;  // milliseconds
  private static final int      ADJUST_TIMEOUT          = 5000; // milliseconds
  private static final int      ERROR_MESSAGE_TIMEOUT   = 5000; // milliseconds
  private static final boolean  ORIENTATION_ENABLED     = true; // Show device orientation?

  // UI Parameters
  private LocationView  mLocationView             = null;
  private Button        mPrevFloorButton          = null;
  private Button        mNextFloorButton          = null;
  private View          mBackView                 = null;
  private View          mPrevFloorView            = null;
  private View          mNextFloorView            = null;
  private View          mZoomInView               = null;
  private View          mZoomOutView              = null;
  private View          mAdjustModeView           = null;
  private TextView      mCurrentFloorLabel        = null;
  private TextView      mErrorMessageLabel        = null;
  private TimerTask     mTimerTask                = null;
  private Timer         mTimer                    = new Timer();
  private Handler       mHandler                  = new Handler();

  private boolean       mAdjustMode               = false;
  private long          mErrorMessageTime         = 0;

  // Map parameters
  private long          mAdjustTime               = 0;

  // Location parameters
  private Location      mLocation                 = null;
  private int           mCurrentSubLocationIndex  = -1;

  // Device parameters
  private DeviceInfo    mDeviceInfo               = null; // Current device
  private LocationPoint mPinPoint                 = null; // Potential device target
  private LocationPoint mTargetPoint              = null; // Current device target
  private RectF         mPinPointRect             = null;

  private Bitmap  mVenueBitmap    = null;
  private Venue   mTargetVenue    = null;
  private Venue   mSelectedVenue  = null;
  private RectF   mSelectedVenueRect = null;
  
  @Override protected void onCreate(Bundle savedInstanceState)
  {
    Log.d(TAG, "MainActivity started");
    
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_main);

    getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
      WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    // Setting up GUI parameters
    mLocationView = (LocationView)findViewById(R.id.navigation__location_view);
    mBackView = (View)findViewById(R.id.navigation__back_view);
    mPrevFloorButton = (Button)findViewById(R.id.navigation__prev_floor_button);
    mNextFloorButton = (Button)findViewById(R.id.navigation__next_floor_button);
    mPrevFloorView = (View)findViewById(R.id.navigation__prev_floor_view);
    mNextFloorView = (View)findViewById(R.id.navigation__next_floor_view);
    mCurrentFloorLabel = (TextView)findViewById(R.id.navigation__current_floor_label);
    mZoomInView  = (View)findViewById(R.id.navigation__zoom_in_view);
    mZoomOutView = (View)findViewById(R.id.navigation__zoom_out_view);
    mAdjustModeView = (View)findViewById(R.id.navigation__adjust_mode_view);
    mErrorMessageLabel = (TextView)findViewById(R.id.navigation__error_message_label);

    mLocationView.setBackgroundColor(0xffebebeb);

    mBackView.setVisibility(View.INVISIBLE);
    mPrevFloorView.setVisibility(View.INVISIBLE);
    mNextFloorView.setVisibility(View.INVISIBLE);
    mCurrentFloorLabel.setVisibility(View.INVISIBLE);
    mZoomInView.setVisibility(View.INVISIBLE);
    mZoomOutView.setVisibility(View.INVISIBLE);
    mAdjustModeView.setVisibility(View.INVISIBLE);
    mErrorMessageLabel.setVisibility(View.GONE);
    
    mVenueBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.elm_venue);

    // Setting up listener
    mLocationView.setListener
    (
      new LocationView.Listener()
      {
        @Override public void onClick     (float x, float y) { handleClick(x, y);     }
        @Override public void onLongClick (float x, float y) { handleLongClick(x, y); }
        @Override public void onScroll    (float x, float y) { mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT; }
        @Override public void onZoom      (float ratio)      { mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT; }
        
        @Override public void onDraw(Canvas canvas)
        {
          drawPoints(canvas);
          drawVenues(canvas);
          drawDevice(canvas);
        }
      }
    );
    
    loadMap();

    // Starting interface updates
    mTimerTask = new TimerTask()
    {
      @Override public void run()
      {
        mHandler.post(mRunnable);
      }
    };
    mTimer.schedule(mTimerTask, UPDATE_TIMEOUT, UPDATE_TIMEOUT);
  }

  @Override public void onDestroy()
  {
    DemoApp.finish();
    mTimerTask.cancel();
    mTimerTask = null;
    super.onDestroy();
  }

  @Override public void onBackPressed()
  {
    moveTaskToBack(true);
  }

  public void toggleAdjustMode(View v)
  {
    mAdjustMode = !mAdjustMode;
    mAdjustTime = 0;
    Button adjustModeButton = (Button)findViewById(R.id.navigation__adjust_mode_button);
    adjustModeButton.setBackgroundResource(mAdjustMode ?
                                           R.drawable.btn_adjust_mode_on :
                                           R.drawable.btn_adjust_mode_off);
    mHandler.post(mRunnable);
  }

  public void onNextFloor(View v)
  {
    if (loadNextSubLocation())
      mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
  }

  public void onPrevFloor(View v)
  {
    if (loadPrevSubLocation())
      mAdjustTime = NavigineSDK.currentTimeMillis() + ADJUST_TIMEOUT;
  }

  public void onZoomIn(View v)
  {
    mLocationView.zoomBy(1.25f);
  }

  public void onZoomOut(View v)
  {
    mLocationView.zoomBy(0.8f);
  }

  public void onMakeRoute(View v)
  {
    if (DemoApp.Navigation == null)
      return;

    if (mPinPoint == null)
      return;

    mTargetPoint  = mPinPoint;
    mTargetVenue  = null;
    mPinPoint     = null;
    mPinPointRect = null;

    DemoApp.Navigation.setTarget(mTargetPoint);
    mBackView.setVisibility(View.VISIBLE);
    mHandler.post(mRunnable);
  }

  public void onCancelRoute(View v)
  {
    if (DemoApp.Navigation == null)
      return;

    mTargetPoint  = null;
    mTargetVenue  = null;
    mPinPoint     = null;
    mPinPointRect = null;

    DemoApp.Navigation.cancelTargets();
    mBackView.setVisibility(View.GONE);
    mHandler.post(mRunnable);
  }

  public void onCloseMessage(View v)
  {
    mErrorMessageLabel.setVisibility(View.GONE);
    mErrorMessageTime = 0;
  }
  
  private void setErrorMessage(String message)
  {
    mErrorMessageLabel.setText(message);
    mErrorMessageLabel.setVisibility(View.VISIBLE);
    mErrorMessageTime = NavigineSDK.currentTimeMillis();
  }
  
  private void handleClick(float x, float y)
  {
    Log.d(TAG, String.format(Locale.ENGLISH, "Click at (%.2f, %.2f)", x, y));
    
    if (mPinPoint != null)
    {
      if (mPinPointRect.contains(x, y))
      {
        mTargetPoint  = mPinPoint;
        mTargetVenue  = null;
        mPinPoint     = null;
        mPinPointRect = null;
        DemoApp.Navigation.setTarget(mTargetPoint);
        mBackView.setVisibility(View.VISIBLE);
        return;
      }
      cancelPin();
      return;
    }
    
    if (mSelectedVenue != null)
    {
      if (mSelectedVenueRect != null && mSelectedVenueRect.contains(x, y))
      {
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;
        
        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
        if (subLoc == null)
          return;
        
        mTargetVenue = mSelectedVenue;
        mTargetPoint = null;
        
        DemoApp.Navigation.setTarget(new LocationPoint(subLoc.id, mTargetVenue.kx * subLoc.width, mTargetVenue.ky * subLoc.height));
        mBackView.setVisibility(View.VISIBLE);
      }
      cancelVenue();
      return;
    }
    
    // Check if we touched venue
    mSelectedVenue = getVenueAt(x, y);
    mSelectedVenueRect = new RectF();
    mHandler.post(mRunnable);
  }
  
  private void handleLongClick(float x, float y)
  {
    Log.d(TAG, String.format(Locale.ENGLISH, "Long click at (%.2f, %.2f)", x, y));
    makePin(mLocationView.getAbsCoordinates(x, y));
    cancelVenue();
  }
  
  private boolean mMapLoaded = false;
  private boolean loadMap()
  {
    if (mMapLoaded)
      return false;
    mMapLoaded = true;

    if (DemoApp.Navigation == null)
    {
      Log.e(TAG, "Can't load map! Navigine SDK is not available!");
      return false;
    }

    mLocation = DemoApp.Navigation.getLocation();
    mCurrentSubLocationIndex = -1;

    if (mLocation == null)
    {
      Log.e(TAG, "Loading map failed: no location");
      return false;
    }

    if (mLocation.subLocations.size() == 0)
    {
      Log.e(TAG, "Loading map failed: no sublocations");
      mLocation = null;
      return false;
    }

    if (!loadSubLocation(0))
    {
      Log.e(TAG, "Loading map failed: unable to load default sublocation");
      mLocation = null;
      return false;
    }

    if (mLocation.subLocations.size() >= 2)
    {
      mPrevFloorView.setVisibility(View.VISIBLE);
      mNextFloorView.setVisibility(View.VISIBLE);
      mCurrentFloorLabel.setVisibility(View.VISIBLE);
    }
    mZoomInView.setVisibility(View.VISIBLE);
    mZoomOutView.setVisibility(View.VISIBLE);
    mAdjustModeView.setVisibility(View.VISIBLE);
    
    mHandler.post(mRunnable);
    DemoApp.Navigation.setMode(NavigationThread.MODE_NORMAL);
    return true;
  }

  private boolean loadSubLocation(int index)
  {
    if (DemoApp.Navigation == null)
      return false;

    if (mLocation == null || index < 0 || index >= mLocation.subLocations.size())
      return false;

    SubLocation subLoc = mLocation.subLocations.get(index);
    Log.d(TAG, String.format(Locale.ENGLISH, "Loading sublocation %s (%.2f x %.2f)", subLoc.name, subLoc.width, subLoc.height));

    if (subLoc.width < 1.0f || subLoc.height < 1.0f)
    {
      Log.e(TAG, String.format(Locale.ENGLISH, "Loading sublocation failed: invalid size: %.2f x %.2f", subLoc.width, subLoc.height));
      return false;
    }

    if (!mLocationView.loadSubLocation(subLoc))
    {
      Log.e(TAG, "Loading sublocation failed: invalid image");
      return false;
    }

    mAdjustTime = 0;
    
    mCurrentSubLocationIndex = index;
    mCurrentFloorLabel.setText(String.format(Locale.ENGLISH, "%d", mCurrentSubLocationIndex));

    if (mCurrentSubLocationIndex > 0)
    {
      mPrevFloorButton.setEnabled(true);
      mPrevFloorView.setBackgroundColor(Color.parseColor("#90aaaaaa"));
    }
    else
    {
      mPrevFloorButton.setEnabled(false);
      mPrevFloorView.setBackgroundColor(Color.parseColor("#90dddddd"));
    }

    if (mCurrentSubLocationIndex + 1 < mLocation.subLocations.size())
    {
      mNextFloorButton.setEnabled(true);
      mNextFloorView.setBackgroundColor(Color.parseColor("#90aaaaaa"));
    }
    else
    {
      mNextFloorButton.setEnabled(false);
      mNextFloorView.setBackgroundColor(Color.parseColor("#90dddddd"));
    }
    
    cancelVenue();
    mHandler.post(mRunnable);
    return true;
  }

  private boolean loadNextSubLocation()
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return false;
    return loadSubLocation(mCurrentSubLocationIndex + 1);
  }

  private boolean loadPrevSubLocation()
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return false;
    return loadSubLocation(mCurrentSubLocationIndex - 1);
  }

  private void makePin(PointF P)
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;

    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    if (subLoc == null)
      return;

    if (P.x < 0.0f || P.x > subLoc.width ||
        P.y < 0.0f || P.y > subLoc.height)
    {
      // Missing the map
      return;
    }

    if (mTargetPoint != null || mTargetVenue != null)
    {
      //setErrorMessage("Unable to make route: you must cancel the previous route first!");
      return;
    }
    
    if (mDeviceInfo.errorCode != 0)
    {
      //setErrorMessage("Unable to make route: navigation is not available!");
      return;
    }

    mPinPoint = new LocationPoint(subLoc.id, P.x, P.y);
    mPinPointRect = new RectF();
    mHandler.post(mRunnable);
  }

  private void cancelPin()
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;

    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    if (subLoc == null)
      return;

    if (mTargetPoint != null || mTargetVenue != null || mPinPoint == null)
      return;

    mPinPoint = null;
    mPinPointRect = null;
    mHandler.post(mRunnable);
  }
  
  private void cancelVenue()
  {
    mSelectedVenue = null;
    mHandler.post(mRunnable);
  }
  
  private Venue getVenueAt(float x, float y)
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return null;

    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    if (subLoc == null)
      return null;

    Venue v0 = null;
    float d0 = 1000.0f;
    
    for(int i = 0; i < subLoc.venues.size(); ++i)
    {
      Venue v = subLoc.venues.get(i);
      if (v.subLocation != subLoc.id)
        continue;
      PointF P = mLocationView.getScreenCoordinates(v.kx * subLoc.width, v.ky * subLoc.height);
      float d = Math.abs(x - P.x) + Math.abs(y - P.y);
      if (d < 30.0f * DemoApp.DisplayDensity && d < d0)
      {
        v0 = new Venue(v);
        d0 = d;
      }
    }
    
    return v0;
  }
  
  private void drawPoints(Canvas canvas)
  {
    // Check if location is loaded
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;

    // Get current sublocation displayed
    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

    if (subLoc == null)
      return;

    final int solidColor  = Color.argb(255, 64, 163, 205);  // Light-blue color
    final int circleColor = Color.argb(127, 64, 163, 205);  // Semi-transparent light-blue color
    final int arrowColor  = Color.argb(255, 255, 255, 255); // White color
    final float dp        = DemoApp.DisplayDensity;
    final float textSize  = 16 * dp;
    
    // Preparing paints
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setTextSize(textSize);
    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

    // Drawing pin point (if it exists and belongs to the current sublocation)
    if (mPinPoint != null && mPinPoint.subLocation == subLoc.id)
    {
      final PointF T = mLocationView.getScreenCoordinates(mPinPoint);
      final float tRadius = 10 * dp;

      paint.setARGB(255, 0, 0, 0);
      paint.setStrokeWidth(4 * dp);
      canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

      paint.setColor(solidColor);
      paint.setStrokeWidth(0);
      canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);
      
      final String text = "Make route";
      final float textWidth = paint.measureText(text);
      final float h  = 50 * dp;
      final float w  = Math.max(120 * dp, textWidth + h/2);
      final float x0 = T.x;
      final float y0 = T.y - 75 * dp;
      
      mPinPointRect.set(x0 - w/2, y0 - h/2, x0 + w/2, y0 + h/2);
      
      paint.setColor(solidColor);
      canvas.drawRoundRect(mPinPointRect, h/2, h/2, paint);
      
      paint.setARGB(255, 255, 255, 255);
      canvas.drawText(text, x0 - textWidth/2, y0 + textSize/4, paint);
    }
    
    // Drawing target point (if it exists and belongs to the current sublocation)
    if (mTargetPoint != null && mTargetPoint.subLocation == subLoc.id)
    {
      final PointF T = mLocationView.getScreenCoordinates(mTargetPoint);
      final float tRadius = 10 * dp;

      paint.setARGB(255, 0, 0, 0);
      paint.setStrokeWidth(4 * dp);
      canvas.drawLine(T.x, T.y, T.x, T.y - 3 * tRadius, paint);

      paint.setColor(solidColor);
      canvas.drawCircle(T.x, T.y - 3 * tRadius, tRadius, paint);
    }
  }

  private void drawVenues(Canvas canvas)
  {
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;
    
    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
    
    final float dp = DemoApp.DisplayDensity;
    final float textSize  = 16 * dp;
    final float venueSize = 30 * dp;
    final int   venueColor = Color.argb(255, 0xCD, 0x88, 0x50); // Venue color
    
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setStrokeWidth(0);
    paint.setColor(venueColor);
    paint.setTextSize(textSize);
    paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    
    for(int i = 0; i < subLoc.venues.size(); ++i)
    {
      Venue v = subLoc.venues.get(i);
      if (v.subLocation != subLoc.id)
        continue;
      
      final PointF P = mLocationView.getScreenCoordinates(v.kx * subLoc.width, v.ky * subLoc.height);
      final float x0 = P.x - venueSize/2;
      final float y0 = P.y - venueSize/2;
      final float x1 = P.x + venueSize/2;
      final float y1 = P.y + venueSize/2;
      canvas.drawBitmap(mVenueBitmap, null, new RectF(x0, y0, x1, y1), paint);
    }
    
    if (mSelectedVenue != null)
    {
      final PointF T = mLocationView.getScreenCoordinates(mSelectedVenue.kx * subLoc.width, mSelectedVenue.ky * subLoc.height);
      final float textWidth = paint.measureText(mSelectedVenue.name);
      
      final float h  = 50 * dp;
      final float w  = Math.max(120 * dp, textWidth + h/2);
      final float x0 = T.x;
      final float y0 = T.y - 50 * dp;
      mSelectedVenueRect.set(x0 - w/2, y0 - h/2, x0 + w/2, y0 + h/2);
      
      paint.setColor(venueColor);
      canvas.drawRoundRect(mSelectedVenueRect, h/2, h/2, paint);
      
      paint.setARGB(255, 255, 255, 255);
      canvas.drawText(mSelectedVenue.name, x0 - textWidth/2, y0 + textSize/4, paint);
    }
  }
  
  private void drawDevice(Canvas canvas)
  {
    // Check if location is loaded
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;
    
    // Check if navigation is available
    if (mDeviceInfo.errorCode != 0)
      return;

    // Check if device belongs to the location loaded
    if (mDeviceInfo.location != mLocation.id)
      return;

    // Get current sublocation displayed
    SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

    if (subLoc == null)
      return;

    final int solidColor  = Color.argb(255, 64,  163, 205); // Light-blue color
    final int circleColor = Color.argb(127, 64,  163, 205); // Semi-transparent light-blue color
    final int arrowColor  = Color.argb(255, 255, 255, 255); // White color
    final float dp = DemoApp.DisplayDensity;

    // Preparing paints
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(Paint.Style.FILL_AND_STROKE);
    paint.setStrokeCap(Paint.Cap.ROUND);

    /// Drawing device path (if it exists)
    if (mDeviceInfo.paths != null && mDeviceInfo.paths.size() > 0)
    {
      DevicePath p = mDeviceInfo.paths.get(0);
      if (p.path.length >= 2)
      {
        paint.setColor(solidColor);

        for(int j = 1; j < p.path.length; ++j)
        {
          LocationPoint P = p.path[j-1];
          LocationPoint Q = p.path[j];
          if (P.subLocation == subLoc.id && Q.subLocation == subLoc.id)
          {
            paint.setStrokeWidth(3 * dp);
            PointF P1 = mLocationView.getScreenCoordinates(P);
            PointF Q1 = mLocationView.getScreenCoordinates(Q);
            canvas.drawLine(P1.x, P1.y, Q1.x, Q1.y, paint);
          }
        }
      }
    }
    
    paint.setStrokeCap(Paint.Cap.BUTT);

    // Check if device belongs to the current sublocation
    if (mDeviceInfo.subLocation != subLoc.id)
      return;

    final float x  = mDeviceInfo.x;
    final float y  = mDeviceInfo.y;
    final float r  = mDeviceInfo.r;
    final float angle = mDeviceInfo.azimuth;
    final float sinA = (float)Math.sin(angle);
    final float cosA = (float)Math.cos(angle);
    final float radius  = mLocationView.getScreenLengthX(r);  // External radius: navigation-determined, transparent
    final float radius1 = 25 * dp;                            // Internal radius: fixed, solid

    PointF O = mLocationView.getScreenCoordinates(x, y);
    PointF P = new PointF(O.x - radius1 * sinA * 0.22f, O.y + radius1 * cosA * 0.22f);
    PointF Q = new PointF(O.x + radius1 * sinA * 0.55f, O.y - radius1 * cosA * 0.55f);
    PointF R = new PointF(O.x + radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y + radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);
    PointF S = new PointF(O.x - radius1 * cosA * 0.44f - radius1 * sinA * 0.55f, O.y - radius1 * sinA * 0.44f + radius1 * cosA * 0.55f);

    // Drawing transparent circle
    paint.setStrokeWidth(0);
    paint.setColor(circleColor);
    canvas.drawCircle(O.x, O.y, radius, paint);

    // Drawing solid circle
    paint.setColor(solidColor);
    canvas.drawCircle(O.x, O.y, radius1, paint);

    if (ORIENTATION_ENABLED)
    {
      // Drawing arrow
      paint.setColor(arrowColor);
      Path path = new Path();
      path.moveTo(Q.x, Q.y);
      path.lineTo(R.x, R.y);
      path.lineTo(P.x, P.y);
      path.lineTo(S.x, S.y);
      path.lineTo(Q.x, Q.y);
      canvas.drawPath(path, paint);
    }
  }

  private void adjustDevice()
  {
    // Check if location is loaded
    if (mLocation == null || mCurrentSubLocationIndex < 0)
      return;

    // Check if navigation is available
    if (mDeviceInfo.errorCode != 0)
      return;

    // Check if device belongs to the location loaded
    if (mDeviceInfo.location != mLocation.id)
      return;

    long timeNow = NavigineSDK.currentTimeMillis();

    // Adjust map, if necessary
    if (timeNow >= mAdjustTime)
    {
      // Firstly, set the correct sublocation
      SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);
      if (mDeviceInfo.subLocation != subLoc.id)
      {
        for(int i = 0; i < mLocation.subLocations.size(); ++i)
          if (mLocation.subLocations.get(i).id == mDeviceInfo.subLocation)
            loadSubLocation(i);
      }
      
      // Secondly, adjust device to the center of the screen
      PointF center = mLocationView.getScreenCoordinates(mDeviceInfo.x, mDeviceInfo.y);
      float deltaX  = mLocationView.getWidth()  / 2 - center.x;
      float deltaY  = mLocationView.getHeight() / 2 - center.y;
      mAdjustTime   = timeNow;
      mLocationView.scrollBy(deltaX, deltaY);
    }
  }
  
  final Runnable mRunnable =
    new Runnable()
    {
      public void run()
      {
        if (DemoApp.Navigation == null)
        {
          Log.d(TAG, "Sorry, navigation is not supported on your device!");
          return;
        }

        final long timeNow = NavigineSDK.currentTimeMillis();
        
        if (mErrorMessageTime > 0 && timeNow > mErrorMessageTime + ERROR_MESSAGE_TIMEOUT)
        {
          mErrorMessageTime = 0;
          mErrorMessageLabel.setVisibility(View.GONE);
        }

        // Check if location is loaded
        if (mLocation == null || mCurrentSubLocationIndex < 0)
          return;

        // Get current sublocation displayed
        SubLocation subLoc = mLocation.subLocations.get(mCurrentSubLocationIndex);

        // Start navigation if necessary
        if (DemoApp.Navigation.getMode() == NavigationThread.MODE_IDLE)
          DemoApp.Navigation.setMode(NavigationThread.MODE_NORMAL);
        
        // Get device info from NavigationThread
        mDeviceInfo = DemoApp.Navigation.getDeviceInfo();

        if (mDeviceInfo.errorCode == 0)
        {
          mErrorMessageTime = 0;
          mErrorMessageLabel.setVisibility(View.GONE);
          
          if (mAdjustMode)
            adjustDevice();

          if (mTargetPoint != null || mTargetVenue != null)
            mBackView.setVisibility(View.VISIBLE);
          else
            mBackView.setVisibility(View.GONE);
        }
        else
        {
          switch (mDeviceInfo.errorCode)
          {
            case 4:
              setErrorMessage("You are out of navigation zone! Please, check that your bluetooth is enabled!");
              break;

            case 8:
            case 30:
              setErrorMessage("Not enough beacons on the location! Please, add more beacons!");
              break;

            default:
              setErrorMessage(String.format(Locale.ENGLISH,
                              "Something is wrong with location '%s' (error code %d)! " +
                              "Please, contact technical support!",
                              mLocation.name, mDeviceInfo.errorCode));
              break;
          }
          mBackView.setVisibility(View.GONE);
        }
        
        // This causes map redrawing
        mLocationView.redraw();
      }
    };
}
