package com.maksimdenisov.appleclock;

import android.graphics.*;
import android.view.View;
import java.util.function.LongSupplier;
import java.util.function.BooleanSupplier;

/** Frame-synchronised digits without repeated TextView layout or autosizing. */
final class StopwatchDisplay extends View {
 private final Ui u;private final LongSupplier elapsed;private final BooleanSupplier running;
 private final Paint paint=new Paint(3);private long hundredth=-1,spokenSecond=-1;
 private String digits="00:00:00.00";private int fittedLength;private float size,baseline;
 StopwatchDisplay(Ui u,LongSupplier elapsed,BooleanSupplier running){
  super(u.a);this.u=u;this.elapsed=elapsed;this.running=running;
  paint.setTypeface(Ui.LIGHT);paint.setFontFeatureSettings("tnum");paint.setTextAlign(Paint.Align.CENTER);paint.setColor(u.ink);
  setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);setContentDescription("Секундомер");
 }
 @Override protected void onMeasure(int w,int h){setMeasuredDimension(MeasureSpec.getSize(w),resolveSize(u.dp(230),h));}
 @Override protected void onSizeChanged(int w,int h,int ow,int oh){fit(w,h);}
 private void fit(int w,int h){
  size=u.dp(54);paint.setTextSize(size);size*=Math.min(1,(w-u.dp(8))/Math.max(1,paint.measureText(digits)));
  paint.setTextSize(size);Paint.FontMetrics metrics=paint.getFontMetrics();baseline=h/2f-(metrics.ascent+metrics.descent)/2;
  fittedLength=digits.length();
 }
 @Override protected void onDraw(Canvas c){
  long ms=elapsed.getAsLong(),value=ms/10;
  if(value!=hundredth){hundredth=value;digits=ClockLogic.stopwatch(ms);if(digits.length()!=fittedLength)fit(getWidth(),getHeight());}
  if(ms/1000!=spokenSecond){spokenSecond=ms/1000;setContentDescription("Секундомер "+ClockLogic.duration(ms/1000*1000));}
  c.drawText(digits,getWidth()/2f,baseline,paint);
  if(running.getAsBoolean()&&getWindowVisibility()==VISIBLE)postInvalidateOnAnimation();
 }
 @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(visibility==VISIBLE)invalidate();}
}
