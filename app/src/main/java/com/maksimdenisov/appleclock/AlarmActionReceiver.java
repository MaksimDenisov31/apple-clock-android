package com.maksimdenisov.appleclock;
import android.content.*;
import org.json.JSONObject;

/** Notification buttons operate directly on alarms, without opening or unlocking the phone. */
public final class AlarmActionReceiver extends BroadcastReceiver {
 static final String STOP="clock.STOP",SNOOZE="clock.SNOOZE";
 @Override public void onReceive(Context c,Intent intent){
  String action=intent.getAction();if(!STOP.equals(action)&&!SNOOZE.equals(action))return;
  JSONObject state=Store.load(c),item=Store.find(state,intent.getIntExtra("id",-1));
  if(item==null||!item.optBoolean("ringing"))return;
  Store.dismiss(c,state,item.optInt("id"),SNOOZE.equals(action)&&item.optBoolean("alarm"));
 }
}
