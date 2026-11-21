package com.maksimdenisov.appleclock;

import android.content.*;
import android.app.*;
import android.os.Build;
import org.json.*;
import java.util.*;

final class Store {
 static JSONObject obj(Object... pairs) {JSONObject o=new JSONObject(); for(int i=0;i<pairs.length;i+=2) put(o,(String)pairs[i],pairs[i+1]); return o;}
 static void put(JSONObject o,String k,Object v) {try{o.put(k,v);}catch(JSONException e){throw new IllegalStateException(e);}}
 static JSONObject timer(int id,String name,long duration) {return obj("id",id,"name",name,"duration",duration,"remaining",duration,"running",false,"ringing",false);}
 static JSONObject load(Context c) {
  String raw=c.getSharedPreferences("clock",0).getString("state",null);
  try {if(raw!=null)return new JSONObject(raw);}catch(JSONException e){throw new IllegalStateException("Не удалось прочитать сохранённые часы",e);}
  return obj("nextId",12,"timers",new JSONArray().put(timer(10,"Чай",180000)).put(timer(11,"Обратный отсчёт",300000)),"alarms",new JSONArray(),"cities",new JSONArray().put(0).put(1).put(3),"focus",timer(1,"Фокусировка",1500000),"work",25,"rest",5,"volume",100,"sound",true,"step",10,"laps",new JSONArray());
 }
 static void save(Context c,JSONObject s) {if(!c.getSharedPreferences("clock",0).edit().putString("state",s.toString()).commit())throw new IllegalStateException("Не удалось сохранить часы");}
 static int id(JSONObject s) {int id=s.optInt("nextId",12); put(s,"nextId",id+1); return id;}
 static List<JSONObject> items(JSONObject s) {
  List<JSONObject> out=new ArrayList<>(); out.add(s.optJSONObject("focus"));
  for(String k:new String[]{"timers","alarms"}) {JSONArray a=s.optJSONArray(k);for(int i=0;i<a.length();i++)out.add(a.optJSONObject(i));} return out;
 }
 static JSONObject find(JSONObject s,int id) {for(JSONObject o:items(s))if(o.optInt("id")==id)return o; return null;}
 static long remaining(JSONObject t) {return ClockLogic.remaining(t.optBoolean("running"),t.optLong("end"),t.optLong("remaining"),System.currentTimeMillis());}
 static void start(JSONObject t) {long r=remaining(t); if(r<=0)r=t.optLong("duration");put(t,"remaining",r);put(t,"end",System.currentTimeMillis()+r);put(t,"running",true);}
 static void pause(JSONObject t) {put(t,"remaining",remaining(t));put(t,"running",false);}
 static void reset(JSONObject t) {put(t,"remaining",t.optLong("duration"));put(t,"running",false);}
 static void next(JSONObject a) {put(a,"end",ClockLogic.nextAlarm(a.optInt("hour"),a.optInt("minute"),a.optInt("days"),System.currentTimeMillis(),TimeZone.getDefault()));}
 static boolean tick(Context c,JSONObject s) {
  boolean changed=false; long now=System.currentTimeMillis();
  for(JSONObject o:items(s)) {
   boolean alarm=o.optBoolean("alarm");
   if((alarm?o.optBoolean("enabled"):o.optBoolean("running")) && o.optLong("end")<=now) {
    changed=true;
    if(!alarm||now-o.optLong("end")<600000)put(o,"ringing",true);
    if(alarm){if(o.optInt("days")==0)put(o,"enabled",false);else next(o);}
    else {put(o,"running",false);put(o,"remaining",0);}
   }
  }
  if(changed){save(c,s);schedule(c,s);}return changed;
 }
 static boolean ringing(JSONObject s) {for(JSONObject o:items(s))if(o.optBoolean("ringing"))return true;return false;}
 static PendingIntent pending(Context c,int id) {return PendingIntent.getBroadcast(c,id,new Intent(c,AlarmReceiver.class).setAction("clock.FIRE").putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static boolean exact(Context c) {return Build.VERSION.SDK_INT<31 || ((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms();}
 static void schedule(Context c,JSONObject s) {
  AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  for(JSONObject o:items(s)) {
   PendingIntent p=pending(c,o.optInt("id")); am.cancel(p);
   boolean active=o.optBoolean("alarm")?o.optBoolean("enabled"):o.optBoolean("running");
   if(active && exact(c)) {
    PendingIntent show=PendingIntent.getActivity(c,0,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    am.setAlarmClock(new AlarmManager.AlarmClockInfo(Math.max(System.currentTimeMillis()+50,o.optLong("end")),show),p);
   }
  }
 }
 static void changed(Context c,JSONObject s) {save(c,s);schedule(c,s);}
 static void dismiss(Context c,JSONObject s,int id,boolean snooze) {
  JSONObject o=find(s,id);if(o==null)return;put(o,"ringing",false);
  if(o.optBoolean("alarm")) {if(snooze){put(o,"enabled",true);put(o,"end",System.currentTimeMillis()+300000);}}
  else if(id==1) {
   boolean rest=s.optBoolean("onBreak");String day=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());
   if(!day.equals(s.optString("focusDay"))){put(s,"focusDay",day);put(s,"completed",0);}
   if(!rest)put(s,"completed",s.optInt("completed")+1);put(s,"onBreak",!rest);
   put(o,"duration",s.optInt(rest?"work":"rest",rest?25:5)*60000L);reset(o);
  } else reset(o);
  changed(c,s);c.startService(new Intent(c,RingService.class).setAction("refresh"));
 }
}
