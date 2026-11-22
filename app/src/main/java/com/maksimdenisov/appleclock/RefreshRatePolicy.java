package com.maksimdenisov.appleclock;

/** Prefer native-resolution modes; a refresh-rate request must never change resolution. */
final class RefreshRatePolicy {
 static float include(float best,int width,int height,int candidateWidth,int candidateHeight,float rate){
  return width==candidateWidth&&height==candidateHeight&&Float.isFinite(rate)&&rate>best?rate:best;
 }
 static float request(float maximum,boolean focused,boolean moving,boolean powerSaver){
  return focused&&moving&&!powerSaver?maximum:0;
 }
}
