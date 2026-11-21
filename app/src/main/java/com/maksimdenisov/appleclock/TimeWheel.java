package com.maksimdenisov.appleclock;

import android.graphics.*;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;

/** Lightweight wheel editor, drawn directly without dozens of child TextViews. */
final class TimeWheel extends View {
 final Ui u;final Paint p=new Paint(3);final RectF selection=new RectF();
 static final String[] UNITS={"ч","мин","с"},DIGITS=new String[100];
 static{for(int i=0;i<DIGITS.length;i++)DIGITS[i]=ClockLogic.two(i);}
 final int columns;final int[] values=new int[3];int column;float lastY,downY,offset;boolean dragging;ValueAnimator settling;Runnable changed;
 TimeWheel(Ui u,long ms,boolean alarm){super(u.a);this.u=u;columns=alarm?2:3;values[0]=(int)(ms/3600000);values[1]=(int)(ms/60000%60);values[2]=(int)(ms/1000%60);setFocusable(true);describe();}
 long millis(){return (values[0]*3600L+values[1]*60L+values[2])*1000;}
 void describe(){setContentDescription("Время "+ClockLogic.duration(millis())+". Проведите вверх или вниз по часам, минутам или секундам");}
 @Override protected void onMeasure(int w,int h){setMeasuredDimension(MeasureSpec.getSize(w),u.dp(204));}
 @Override protected void onDraw(Canvas c){
  float width=getWidth()/(float)columns,center=getHeight()/2f,step=u.dp(42);
  p.setColor(u.soft);selection.set(u.dp(1),center-step/2,getWidth()-u.dp(1),center+step/2);c.drawRoundRect(selection,u.dp(12),u.dp(12),p);p.setTypeface(Ui.LIGHT);p.setTextAlign(Paint.Align.CENTER);
  for(int j=0;j<columns;j++)for(int k=-2;k<=2;k++){
   int max=j==0?(columns==2?24:100):60,value=(values[j]+k+max)%max;
   float shift=j==column?offset:0,distance=Math.abs(k+shift/step);p.setTextSize(u.dp(distance<.5f?29:24));p.setColor(distance<.5f?u.ink:u.secondary);p.setAlpha((int)Math.max(35,255-distance*95));
   c.drawText(DIGITS[value],width*j+width*.4f,center+k*step+shift+u.dp(10),p);
  }
  p.setAlpha(255);p.setTextSize(u.dp(12));p.setTypeface(Ui.REGULAR);p.setColor(u.secondary);
  for(int j=0;j<columns;j++)c.drawText(UNITS[j],width*j+width*.78f,center+u.dp(5),p);
 }
 void adjust(int delta){int max=column==0?(columns==2?24:100):60;values[column]=(values[column]+delta+max)%max;describe();invalidate();performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);if(changed!=null)changed.run();}
 @Override public boolean onTouchEvent(MotionEvent e){
  if(e.getAction()==MotionEvent.ACTION_DOWN){if(settling!=null)settling.cancel();offset=0;column=Math.max(0,Math.min(columns-1,(int)(e.getX()/getWidth()*columns)));lastY=downY=e.getY();dragging=false;getParent().requestDisallowInterceptTouchEvent(true);return true;}
  if(e.getAction()==MotionEvent.ACTION_MOVE){if(Math.abs(e.getY()-downY)>ViewConfiguration.get(getContext()).getScaledTouchSlop())dragging=true;offset+=e.getY()-lastY;lastY=e.getY();float step=u.dp(42);while(offset>=step){offset-=step;adjust(-1);}while(offset<=-step){offset+=step;adjust(1);}invalidate();return true;}
  if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
   getParent().requestDisallowInterceptTouchEvent(false);float step=u.dp(42);
   if(e.getAction()==MotionEvent.ACTION_UP&&!dragging){int delta=Math.round((e.getY()-getHeight()/2f)/step);if(delta!=0)adjust(delta);offset=0;performClick();}
   else if(Math.abs(offset)>step/2){int sign=offset>0?1:-1;adjust(-sign);offset-=sign*step;}
   settling=ValueAnimator.ofFloat(offset,0);settling.setDuration(160);settling.setInterpolator(new android.view.animation.DecelerateInterpolator());settling.addUpdateListener(v->{offset=(float)v.getAnimatedValue();invalidate();});settling.start();return true;
  }
  return true;
 }
 @Override public boolean performClick(){super.performClick();return true;}
 @Override protected void onDetachedFromWindow(){if(settling!=null)settling.cancel();super.onDetachedFromWindow();}
 @Override public boolean onKeyDown(int code,KeyEvent e){
  if(code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT){column=(column+(code==KeyEvent.KEYCODE_DPAD_RIGHT?1:columns-1))%columns;return true;}
  if(code==KeyEvent.KEYCODE_DPAD_UP||code==KeyEvent.KEYCODE_DPAD_DOWN){adjust(code==KeyEvent.KEYCODE_DPAD_UP?1:-1);return true;}return super.onKeyDown(code,e);
 }
 @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo n){super.onInitializeAccessibilityNodeInfo(n);n.setScrollable(true);n.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);n.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);n.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK);}
 @Override public boolean performAccessibilityAction(int action,Bundle b){
  if(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD||action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD){adjust(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?1:-1);return true;}
  if(action==AccessibilityNodeInfo.ACTION_CLICK){column=(column+1)%columns;announceForAccessibility(new String[]{"Часы","Минуты","Секунды"}[column]);return true;}
  return super.performAccessibilityAction(action,b);
 }
}
