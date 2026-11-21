package com.maksimdenisov.appleclock;
import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.*;
import android.os.*;
import org.json.*;

public class RingService extends Service {
 static final String CHANNEL="alarm-signal";
 static void ensureChannel(Context c){
  NotificationChannel channel=new NotificationChannel(CHANNEL,"Сигналы часов",NotificationManager.IMPORTANCE_HIGH);channel.setSound(null,null);channel.setDescription("Будильники, таймеры и фокусировка");channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);c.getSystemService(NotificationManager.class).createNotificationChannel(channel);
 }
 static Notification.Action control(Context c,int id,String action,String label){
  PendingIntent p=PendingIntent.getBroadcast(c,id,new Intent(c,AlarmActionReceiver.class).setAction(action).putExtra("id",id),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification.Action.Builder b=new Notification.Action.Builder(0,label,p);
  if(Build.VERSION.SDK_INT>=31)b.setAuthenticationRequired(false);
  return b.build();
 }
 MediaPlayer player;PowerManager.WakeLock lock;AudioFocusRequest focus;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent i,int flags,int id) {
  JSONObject s=Store.load(this);
  if(!Store.ringing(s)){stopForeground(true);stopSelf();return START_NOT_STICKY;}
  ensureChannel(this);
  Intent open=new Intent(this,RingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
  PendingIntent pi=PendingIntent.getActivity(this,1,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  JSONObject current=null;for(JSONObject o:Store.items(s))if(o.optBoolean("ringing")){current=o;break;}
  Notification.Builder notification=new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.app_icon).setContentTitle(current.optString("name")).setContentText("Управление сигналом без разблокировки").setCategory(Notification.CATEGORY_ALARM).setPriority(Notification.PRIORITY_MAX).setVisibility(Notification.VISIBILITY_PUBLIC).setOngoing(true).setOnlyAlertOnce(true).setContentIntent(pi).setFullScreenIntent(pi,true);
  notification.addAction(control(this,current.optInt("id"),AlarmActionReceiver.STOP,"Остановить"));
  if(current.optBoolean("alarm"))notification.addAction(control(this,current.optInt("id"),AlarmActionReceiver.SNOOZE,"Через 2 минуты"));
  Notification n=notification.build();
  // On Android 14+ exact-alarm apps use the dedicated alarm-service exemption.
  // A mediaPlayback service is rejected during Android 15's boot allowlist window,
  // even when a fresh alarm PendingIntent delivers the recovered signal.
  if(Build.VERSION.SDK_INT>=34)startForeground(100,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED);
  else if(Build.VERSION.SDK_INT>=29)startForeground(100,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
  else startForeground(100,n);
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
