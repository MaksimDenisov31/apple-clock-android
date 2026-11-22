package com.maksimdenisov.appleclock;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

/** Follow actual display VSYNC; don't run a 16 ms or 8 ms software frame timer. */
abstract class AnimatedClockView extends View {
 private final Rect visibleRect=new Rect();
 AnimatedClockView(Context context){super(context);}
 final void nextFrame(boolean running){
  if(running&&isAttachedToWindow()&&hasWindowFocus()&&getWindowVisibility()==VISIBLE&&isShown()&&getLocalVisibleRect(visibleRect)){
   SmoothWindow.motion(this);postInvalidateOnAnimation();
  }
 }
 @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)invalidate();}
 @Override protected void onWindowVisibilityChanged(int visibility){super.onWindowVisibilityChanged(visibility);if(visibility==VISIBLE)invalidate();}
 @Override protected void onVisibilityChanged(View view,int visibility){super.onVisibilityChanged(view,visibility);if(visibility==VISIBLE)invalidate();}
}
