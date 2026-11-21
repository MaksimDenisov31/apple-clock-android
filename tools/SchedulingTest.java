package com.maksimdenisov.appleclock;

public final class SchedulingTest {
 static int checks;
 static void check(boolean condition){if(!condition)throw new AssertionError("Scheduling check "+(checks+1));checks++;}
 public static void main(String[] args){
  ScheduleCache c=new ScheduleCache();
  check(c.needs(1,0));c.recorded(1,0);check(!c.needs(1,0));
  check(c.needs(1,10000));c.recorded(1,10000);check(!c.needs(1,10000));
  // Navigating, renaming and changing theme must leave a registered deadline alone.
  check(!c.needs(1,10000));
  check(c.needs(1,0));c.recorded(1,0);check(!c.needs(1,0)); // pause / permission revoked
  check(c.needs(1,15000));c.recorded(1,15000);check(!c.needs(1,15000)); // resume / permission restored
  check(c.needs(2,15000));c.recorded(2,15000);check(!c.needs(2,15000));
  c.forget(1);check(c.needs(1,15000));check(!c.needs(2,15000));
  c.clear();check(c.needs(2,15000)); // consumed one-shot, reboot or system schedule reset
  check(c.needs(3,999));check(c.needs(3,999)); // failure before recorded() remains retryable
  check(ClockLogic.stopwatch(0).equals("00:00:00.00"));
  check(ClockLogic.stopwatch(3599999).equals("00:59:59.99"));
  check(ClockLogic.stopwatch(3600010).equals("01:00:00.01"));
  check(ClockLogic.duration(359999000).equals("99:59:59"));
  System.out.println("PASS "+checks+" scheduling and display checks");
 }
}
