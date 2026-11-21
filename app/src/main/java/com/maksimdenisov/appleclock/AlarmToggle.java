package com.maksimdenisov.appleclock;
import android.graphics.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;

final class AlarmToggle extends View {
 final Ui u;final boolean checked;final Paint p=new Paint(3);final RectF rect=new RectF();
 AlarmToggle(Ui u,boolean checked,Runnable action){super(u.a);this.u=u;this.checked=checked;setFocusable(true);setContentDescription(checked?"Выключить будильник":"Включить будильник");setOnClickListener(v->action.run());}
 @Override protected void onMeasure(int w,int h){setMeasuredDimension(u.dp(58),u.dp(46));}
 @Override protected void onDraw(Canvas c){float x=u.dp(3),y=u.dp(8);rect.set(x,y,x+u.dp(51),y+u.dp(31));p.setColor(checked?Ui.GREEN:u.soft);c.drawRoundRect(rect,u.dp(16),u.dp(16),p);p.setColor(Color.WHITE);c.drawCircle(x+u.dp(checked?35:16),y+u.dp(15.5f),u.dp(13.5f),p);}
 @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo n){super.onInitializeAccessibilityNodeInfo(n);n.setClassName("android.widget.Switch");n.setCheckable(true);n.setChecked(checked);}
}
