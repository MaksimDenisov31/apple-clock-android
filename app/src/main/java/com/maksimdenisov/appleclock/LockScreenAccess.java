package com.maksimdenisov.appleclock;

import android.app.*;
import android.content.*;
import android.os.Build;
import android.provider.Settings;

final class LockScreenAccess {
 static String issue(Context c){
  NotificationManager n=c.getSystemService(NotificationManager.class);
  if(!n.areNotificationsEnabled())return "Разрешите уведомления часов, чтобы управлять сигналом без открытия приложения.";
  NotificationChannel channel=n.getNotificationChannel(RingService.CHANNEL);
  if(channel!=null&&channel.getImportance()<NotificationManager.IMPORTANCE_HIGH)return "Включите важные уведомления сигналов и их показ на экране блокировки.";
  if(Build.VERSION.SDK_INT>=34&&!n.canUseFullScreenIntent())return "Разрешите полноэкранные сигналы: будильник сможет включать экран и показывать кнопки поверх блокировки.";
  return null;
 }
 static void open(Activity a){
  NotificationManager n=a.getSystemService(NotificationManager.class);NotificationChannel ch=n.getNotificationChannel(RingService.CHANNEL);Intent intent;
  if(!n.areNotificationsEnabled())intent=new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,a.getPackageName());
  else if(ch!=null&&ch.getImportance()<NotificationManager.IMPORTANCE_HIGH)intent=new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,a.getPackageName()).putExtra(Settings.EXTRA_CHANNEL_ID,RingService.CHANNEL);
  else if(Build.VERSION.SDK_INT>=34&&!n.canUseFullScreenIntent())intent=new Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT",android.net.Uri.parse("package:"+a.getPackageName()));
  else intent=new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,a.getPackageName());
  try{a.startActivity(intent);}catch(ActivityNotFoundException e){a.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+a.getPackageName())));}
 }
}
