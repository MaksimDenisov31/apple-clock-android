package com.maksimdenisov.appleclock;
public final class RefreshRateTest {
 static int checks;
 static void equal(float actual,float expected){if(Math.abs(actual-expected)>.01f)throw new AssertionError(actual+" != "+expected);checks++;}
 public static void main(String[] args){
  float best=60;
  for(float hz:new float[]{60,90,120,144}){best=RefreshRatePolicy.include(best,1080,2340,1080,2340,hz);equal(best,hz);}
  equal(RefreshRatePolicy.include(60,1080,2340,1440,3120,120),60);
  equal(RefreshRatePolicy.include(120,1080,2340,1080,2340,90),120);
  equal(RefreshRatePolicy.include(60,1080,2340,1080,2340,Float.NaN),60);
  equal(RefreshRatePolicy.include(60,1080,2340,1080,2340,Float.POSITIVE_INFINITY),60);
  equal(RefreshRatePolicy.request(120,true,true,false),120);
  equal(RefreshRatePolicy.request(90,true,true,false),90);
  equal(RefreshRatePolicy.request(60,true,true,false),60);
  equal(RefreshRatePolicy.request(120,false,true,false),0);
  equal(RefreshRatePolicy.request(120,true,false,false),0);
  equal(RefreshRatePolicy.request(120,true,true,true),0);
  System.out.println("PASS "+checks+" adaptive refresh policy checks");
 }
}
