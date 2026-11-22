package com.maksimdenisov.appleclock;

import android.content.*;
import android.hardware.display.DisplayManager;
import android.os.*;
import android.view.*;

/** Window-local hints only. Android retains control of refresh rate and power limits. */
final class SmoothWindow implements DisplayManager.DisplayListener {
 private final Window window;private final View decor;private final Handler handler=new Handler(Looper.getMainLooper());
 private final DisplayManager displays;private final PowerManager power;
 private boolean listening,waiting;private float maximum;private long lastMotion;
 private final Runnable idle=this::onIdle;
 private void onIdle(){waiting=false;if(SystemClock.uptimeMillis()-lastMotion<1200&&decor.hasWindowFocus())scheduleIdle();else apply(false);}
 private final BroadcastReceiver saver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){apply(SystemClock.uptimeMillis()-lastMotion<1200);}};
 private SmoothWindow(Window w){
  window=w;decor=w.getDecorView();displays=decor.getContext().getSystemService(DisplayManager.class);power=decor.getContext().getSystemService(PowerManager.class);
  decor.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View v){focus(decor.hasWindowFocus());}public void onViewDetachedFromWindow(View v){focus(false);}});
  decor.getViewTreeObserver().addOnWindowFocusChangeListener(this::focus);
  focus(decor.hasWindowFocus());
 }
 static void install(Window w){if(w.getDecorView().getTag(R.id.refresh_policy)==null)w.getDecorView().setTag(R.id.refresh_policy,new SmoothWindow(w));}
 static void motion(View v){Object policy=v.getRootView().getTag(R.id.refresh_policy);if(policy instanceof SmoothWindow)((SmoothWindow)policy).pulse();}
 static void touch(Window w,MotionEvent e){install(w);if(e.getActionMasked()!=MotionEvent.ACTION_CANCEL)motion(w.getDecorView());}
 private void focus(boolean focused){
  if(focused&&!listening){listening=true;displays.registerDisplayListener(this,handler);Context c=decor.getContext();IntentFilter filter=new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);if(Build.VERSION.SDK_INT>=33)c.registerReceiver(saver,filter,Context.RECEIVER_NOT_EXPORTED);else c.registerReceiver(saver,filter);readDisplay();pulse();}
  else if(!focused){if(listening){displays.unregisterDisplayListener(this);decor.getContext().unregisterReceiver(saver);listening=false;}handler.removeCallbacks(idle);waiting=false;apply(false);}
 }
 private void readDisplay(){
  Display display=decor.getDisplay();if(display==null){maximum=0;return;}Display.Mode current=display.getMode();maximum=current.getRefreshRate();
  for(Display.Mode mode:display.getSupportedModes()){
   maximum=RefreshRatePolicy.include(maximum,current.getPhysicalWidth(),current.getPhysicalHeight(),mode.getPhysicalWidth(),mode.getPhysicalHeight(),mode.getRefreshRate());
   if(Build.VERSION.SDK_INT>=31)for(float rate:mode.getAlternativeRefreshRates())maximum=RefreshRatePolicy.include(maximum,current.getPhysicalWidth(),current.getPhysicalHeight(),mode.getPhysicalWidth(),mode.getPhysicalHeight(),rate);
  }
 }
 private void pulse(){if(!decor.hasWindowFocus())return;lastMotion=SystemClock.uptimeMillis();if(!waiting){apply(true);scheduleIdle();}}
 private void scheduleIdle(){if(!waiting){waiting=true;handler.postDelayed(idle,1200);}}
 private void apply(boolean moving){
  float rate=RefreshRatePolicy.request(maximum,decor.hasWindowFocus(),moving,power.isPowerSaveMode());WindowManager.LayoutParams attrs=window.getAttributes();
  if(Math.abs(attrs.preferredRefreshRate-rate)>.01f){attrs.preferredRefreshRate=rate;window.setAttributes(attrs);}
  if(Build.VERSION.SDK_INT>=35&&Math.abs(decor.getRequestedFrameRate()-rate)>.01f)decor.setRequestedFrameRate(rate);
 }
 @Override public void onDisplayChanged(int id){Display d=decor.getDisplay();if(d!=null&&d.getDisplayId()==id){readDisplay();apply(SystemClock.uptimeMillis()-lastMotion<1200);}}
 @Override public void onDisplayAdded(int id){}
 @Override public void onDisplayRemoved(int id){}
}
