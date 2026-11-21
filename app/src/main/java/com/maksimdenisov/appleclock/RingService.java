package com.maksimdenisov.appleclock;
import android.app.*;
import android.content.*;
import android.media.*;
import android.os.*;
import org.json.*;

public class RingService extends Service {
 MediaPlayer player;PowerManager.WakeLock lock;AudioFocusRequest focus;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent i,int flags,int id) {
  JSONObject s=Store.load(this);
  if(!Store.ringing(s)){stopForeground(true);stopSelf();return START_NOT_STICKY;}
  NotificationManager nm=getSystemService(NotificationManager.class);
  NotificationChannel channel=new NotificationChannel("alarm-signal","Сигналы часов",NotificationManager.IMPORTANCE_HIGH);channel.setSound(null,null);channel.setDescription("Будильники, таймеры и фокусировка");channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);nm.createNotificationChannel(channel);
  Intent open=new Intent(this,RingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
  PendingIntent pi=PendingIntent.getActivity(this,1,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  String title="Время вышло";for(JSONObject o:Store.items(s))if(o.optBoolean("ringing")){title=o.optString("name");break;}
  Notification n=new Notification.Builder(this,"alarm-signal").setSmallIcon(com.maksimdenisov.appleclock.R.drawable.app_icon).setContentTitle(title).setContentText("Нажмите, чтобы остановить сигнал").setCategory(Notification.CATEGORY_ALARM).setVisibility(Notification.VISIBILITY_PUBLIC).setOngoing(true).setContentIntent(pi).setFullScreenIntent(pi,true).build();
  startForeground(100,n);
  if(lock==null){lock=((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"AppleClock:signal");lock.acquire(10*60*1000L);}
  if(player==null && s.optBoolean("sound",true) && s.optInt("volume",100)>0) {
   try {
    AudioAttributes attrs=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
    focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attrs).setOnAudioFocusChangeListener(change->{}).build();getSystemService(AudioManager.class).requestAudioFocus(focus);
    player=new MediaPlayer();player.setWakeMode(this,PowerManager.PARTIAL_WAKE_LOCK);player.setAudioAttributes(attrs);
    android.content.res.AssetFileDescriptor fd=getResources().openRawResourceFd(R.raw.timer_chime);player.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());fd.close();player.setLooping(true);player.prepare();player.start();
   }catch(Exception e){if(player!=null)player.release();player=null;}
  }
  if(player!=null){float v=s.optBoolean("sound",true)?s.optInt("volume",100)/100f:0;player.setVolume(v,v);}
  return START_STICKY;
 }
 @Override public void onDestroy(){if(player!=null){player.release();player=null;}if(focus!=null)getSystemService(AudioManager.class).abandonAudioFocusRequest(focus);if(lock!=null&&lock.isHeld())lock.release();super.onDestroy();}
}
