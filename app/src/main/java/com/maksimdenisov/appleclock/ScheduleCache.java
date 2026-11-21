package com.maksimdenisov.appleclock;
import java.util.HashMap;
import java.util.Map;

/** Successful platform registrations only; permission restoration and boot can invalidate them. */
final class ScheduleCache {
 private final Map<Integer,Long> ends=new HashMap<>();
 boolean needs(int id,long end){Long previous=ends.get(id);return previous==null||previous.longValue()!=end;}
 void recorded(int id,long end){ends.put(id,end);}
 void forget(int id){ends.remove(id);}
 void clear(){ends.clear();}
}
