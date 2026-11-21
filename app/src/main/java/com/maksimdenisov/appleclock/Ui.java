package com.maksimdenisov.appleclock;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;

class Ui {
 final Activity a;final boolean dark;final int bg,card,ink,secondary,line,soft;static final int BLUE=Color.rgb(0,122,255),ORANGE=Color.rgb(255,149,0);
 Ui(Activity a,boolean dark){this.a=a;this.dark=dark;bg=c(dark?"#1C1C1E":"#F5F5F7");card=c(dark?"#2C2C2E":"#FFFFFF");ink=c(dark?"#F5F5F7":"#1D1D1F");secondary=c(dark?"#A1A1A6":"#86868B");line=c(dark?"#3A3A3C":"#E9E9ED");soft=c(dark?"#3A3A3C":"#E9E9ED");}
 static int c(String x){return Color.parseColor(x);} int dp(float n){return (int)(n*a.getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 void surface(View v,int color,int radius){v.setBackground(shape(color,radius));}
 LinearLayout col(){LinearLayout l=new LinearLayout(a);l.setOrientation(1);return l;}
 LinearLayout row(){LinearLayout l=new LinearLayout(a);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 TextView text(String s,int size,int color){TextView t=new TextView(a);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("tnum");t.setIncludeFontPadding(false);return t;}
 TextView title(String s,int size){TextView t=text(s,size,ink);t.setTypeface(android.graphics.Typeface.create("sans-serif-medium",0));return t;}
 TextView button(String label,boolean accent,Runnable action){TextView b=text(label,14,accent?Color.WHITE:ink);b.setGravity(Gravity.CENTER);b.setMinHeight(dp(46));b.setPadding(dp(14),dp(10),dp(14),dp(10));
  b.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x22000000),shape(accent?BLUE:soft,13),null));b.setFocusable(true);b.setContentDescription(label);b.setOnClickListener(v->action.run());return b;}
 void gap(LinearLayout l,int h){View g=new View(a);l.addView(g,new LinearLayout.LayoutParams(1,dp(h)));}
 LinearLayout panel(LinearLayout parent){LinearLayout p=col();p.setPadding(dp(20),dp(20),dp(20),dp(20));surface(p,card,24);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(14);parent.addView(p,lp);return p;}
 void equal(LinearLayout row,View v){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(3),0,dp(3),0);row.addView(v,p);}
 EditText input(String value,String hint,boolean number){EditText e=new EditText(a);e.setSingleLine(true);e.setText(value);e.setHint(hint);e.setContentDescription(hint);e.setTextSize(16);e.setTextColor(ink);e.setHintTextColor(secondary);e.setPadding(dp(14),dp(12),dp(14),dp(12));surface(e,soft,12);if(number)e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);return e;}
 ScrollView scroll(View content){ScrollView sc=new ScrollView(a);sc.setFillViewport(false);sc.setVerticalScrollBarEnabled(false);sc.addView(content);return sc;}
 Dialog dialog(String title,LinearLayout content){Dialog d=new Dialog(a);LinearLayout root=col();root.setPadding(dp(22),dp(22),dp(22),dp(22));surface(root,card,26);root.addView(title(title,23));gap(root,18);root.addView(scroll(content));d.setContentView(root);android.view.Window w=d.getWindow();w.setBackgroundDrawableResource(android.R.color.transparent);w.setLayout(Math.min(a.getResources().getDisplayMetrics().widthPixels-dp(32),dp(480)),-2);w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);return d;}
 void systemBars(View root){a.getWindow().setStatusBarColor(bg);a.getWindow().setNavigationBarColor(bg);root.setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});root.requestApplyInsets();}
 class Icon extends View {
  final int kind,color;final Paint p=new Paint(3);
  Icon(int kind,int color){super(a);this.kind=kind;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  void line(Canvas c,float x,float y,float x2,float y2){c.drawLine(x,y,x2,y2,p);}
  @Override protected void onDraw(Canvas c){c.save();c.scale(getWidth()/24f,getHeight()/24f);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
   if(kind==0){line(c,5,3,19,3);line(c,5,21,19,21);Path path=new Path();path.moveTo(6,3);path.cubicTo(6,9,9,10,12,12);path.cubicTo(15,14,18,15,18,21);path.moveTo(18,3);path.cubicTo(18,9,15,10,12,12);path.cubicTo(9,14,6,15,6,21);c.drawPath(path,p);}
   if(kind==1){c.drawCircle(12,13,7,p);line(c,12,9,12,13);line(c,12,13,15,15);line(c,4,5,7,3);line(c,17,3,20,5);line(c,7,19,5,22);line(c,17,19,19,22);}
   if(kind==2){c.drawCircle(12,14,8,p);line(c,9,2,15,2);line(c,12,2,12,5);line(c,18,6,20,4);line(c,12,9,12,14);line(c,12,14,15,16);}
   if(kind==3){c.drawCircle(12,12,9,p);c.drawOval(8,3,16,21,p);line(c,3,12,21,12);c.drawArc(4,5,20,11,0,180,false,p);c.drawArc(4,13,20,19,180,180,false,p);}
   if(kind==4){c.drawCircle(12,12,9,p);c.drawCircle(12,12,5,p);c.drawCircle(12,12,1,p);}
   if(kind==5){c.drawCircle(12,12,6,p);c.drawCircle(12,12,2,p);for(int j=0;j<8;j++){double v=j*Math.PI/4;line(c,12+(float)Math.cos(v)*7,12+(float)Math.sin(v)*7,12+(float)Math.cos(v)*9,12+(float)Math.sin(v)*9);}}
   c.restore();}
 }
 class Dial extends View {
  final JSONObject timer;final Paint p=new Paint(3);final RectF rect=new RectF();
  Dial(JSONObject timer){super(a);this.timer=timer;setContentDescription("Осталось "+ClockLogic.duration(Store.remaining(timer)));setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);setLayerType(View.LAYER_TYPE_HARDWARE,null);}
  @Override protected void onMeasure(int w,int h){int s=Math.min(MeasureSpec.getSize(w),dp(260));setMeasuredDimension(s,s);}
  @Override protected void onDraw(Canvas canvas){float size=getWidth(),cx=size/2,cy=size/2,r=size/2-dp(12);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(5));p.setStrokeCap(Paint.Cap.ROUND);p.setColor(line);canvas.drawCircle(cx,cy,r,p);
   float progress=Math.min(1,Store.remaining(timer)/(float)Math.max(1,timer.optLong("duration")));rect.set(cx-r,cy-r,cx+r,cy+r);p.setColor(ORANGE);canvas.drawArc(rect,-90,360*progress,false,p);
   p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif-light",0));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(dp(35));p.setColor(ink);canvas.drawText(ClockLogic.duration(Store.remaining(timer)),cx,cy+dp(5),p);
   p.setTypeface(Typeface.create("sans-serif",0));p.setTextSize(dp(12));p.setColor(secondary);canvas.drawText(timer.optBoolean("ringing")?"Время вышло":timer.optBoolean("running")?"В своём ритме":"Готов к запуску",cx,cy+dp(31),p);
   if(timer.optBoolean("running")&&getWindowVisibility()==VISIBLE)postInvalidateOnAnimation();
  }
 }
}
