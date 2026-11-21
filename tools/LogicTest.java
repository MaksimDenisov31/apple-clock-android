import com.maksimdenisov.appleclock.ClockLogic;
import java.util.*;
public class LogicTest {
 static int checks;static void check(boolean value){checks++;if(!value)throw new AssertionError("Check "+checks);}
 static long date(String s,TimeZone z)throws Exception{java.text.SimpleDateFormat f=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");f.setTimeZone(z);return f.parse(s).getTime();}
 public static void main(String[] a)throws Exception{
  check(ClockLogic.parseDuration("00:00:01")==1000);check(ClockLogic.parseDuration("99:59:59")==359999000);check(ClockLogic.parseDuration("00:00:00")==-1);check(ClockLogic.parseDuration("01:60:00")==-1);check(ClockLogic.parseDuration("1:00:00")==-1);check(ClockLogic.parseDuration("00:00:60")==-1);check(ClockLogic.duration(1).equals("00:00:01"));check(ClockLogic.duration(-10).equals("00:00:00"));
  check(ClockLogic.remaining(true,10000,7000,9000)==1000);check(ClockLogic.remaining(true,10000,7000,11000)==0);check(ClockLogic.remaining(false,10000,7000,11000)==7000);
  TimeZone m=TimeZone.getTimeZone("Europe/Moscow");long now=date("2026-11-23 07:00:00",m);
  check(ClockLogic.nextAlarm(7,30,0,now,m)==date("2026-11-23 07:30:00",m));
  check(ClockLogic.nextAlarm(7,30,0,date("2026-11-23 07:30:00",m),m)==date("2026-11-24 07:30:00",m));
  check(ClockLogic.nextAlarm(7,30,64,now,m)==date("2026-11-29 07:30:00",m));
  check(ClockLogic.nextAlarm(7,30,31,date("2026-11-27 08:00:00",m),m)==date("2026-11-30 07:30:00",m));
  TimeZone n=TimeZone.getTimeZone("America/New_York");long before=date("2026-03-07 08:00:00",n);long next=ClockLogic.nextAlarm(7,30,127,before,n);check(next==date("2026-03-08 07:30:00",n));check(next>before);
  System.out.println("PASS "+checks+" logic checks");
 }
}
