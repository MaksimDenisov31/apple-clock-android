package com.maksimdenisov.appleclock;

import java.util.*;

public final class ClockLogic {
 public static long nextAlarm(int hour,int minute,int days,long now,TimeZone zone) {
  Calendar c=Calendar.getInstance(zone); c.setTimeInMillis(now);
  for(int i=0;i<9;i++) {
   Calendar n=(Calendar)c.clone(); n.add(Calendar.DATE,i);
   n.set(Calendar.HOUR_OF_DAY,hour); n.set(Calendar.MINUTE,minute); n.set(Calendar.SECOND,0); n.set(Calendar.MILLISECOND,0);
   int bit=(n.get(Calendar.DAY_OF_WEEK)+5)%7;
   if(n.getTimeInMillis()>now && (days==0 || (days & (1<<bit))!=0)) return n.getTimeInMillis();
  }
  throw new IllegalArgumentException("Invalid weekday mask");
 }
 public static long parseDuration(String text) {
  if(!text.matches("[0-9]{2}:[0-9]{2}:[0-9]{2}")) return -1;
  String[] p=text.split(":"); int h=Integer.parseInt(p[0]),m=Integer.parseInt(p[1]),s=Integer.parseInt(p[2]);
  return m>59||s>59||h*3600+m*60+s==0 ? -1 : (h*3600L+m*60+s)*1000;
 }
 public static String duration(long ms) {
  long s=Math.max(0,(ms+999)/1000); return String.format(Locale.ROOT,"%02d:%02d:%02d",s/3600,s/60%60,s%60);
 }
 public static long remaining(boolean running,long end,long remaining,long now) {return Math.max(0,running?end-now:remaining);}
}
