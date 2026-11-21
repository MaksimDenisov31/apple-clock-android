package com.maksimdenisov.appleclock;
import android.content.*;
import org.json.*;

public class AlarmReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i) {
  JSONObject s=Store.load(c);String action=i.getAction();
  if(Intent.ACTION_TIMEZONE_CHANGED.equals(action)||Intent.ACTION_TIME_CHANGED.equals(action))
   for(JSONObject o:Store.items(s))if(o.optBoolean("alarm")&&o.optBoolean("enabled"))Store.next(o);
  Store.tick(c,s);Store.changed(c,s);
  if(Store.ringing(s)) {
   if("clock.FIRE".equals(action))c.startForegroundService(new Intent(c,RingService.class));
   else if(Store.exact(c)) {
    // Android 15 forbids media-playback FGS launch directly from BOOT_COMPLETED.
    // Deliver recovered overdue signals through a fresh exact alarm instead.
    android.app.AlarmManager am=(android.app.AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    android.app.PendingIntent show=android.app.PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),android.app.PendingIntent.FLAG_UPDATE_CURRENT|android.app.PendingIntent.FLAG_IMMUTABLE);
    am.setAlarmClock(new android.app.AlarmManager.AlarmClockInfo(System.currentTimeMillis()+1500,show),Store.pending(c,0));
   }
  }
 }
}
