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
 final Activity a;final boolean dark;final int bg,card,ink,secondary,line,soft;
 static final int ORANGE=Color.rgb(255,159,10),GREEN=Color.rgb(48,209,88),RED=Color.rgb(255,69,58);
 static final Typeface LIGHT=Typeface.create("sans-serif-light",0),REGULAR=Typeface.create("sans-serif",0),MEDIUM=Typeface.create("sans-serif-medium",0);
 Ui(Activity a,boolean dark){this.a=a;this.dark=dark;bg=c(dark?"#000000":"#F2F2F7");card=c(dark?"#1C1C1E":"#FFFFFF");ink=c(dark?"#FFFFFF":"#000000");secondary=c(dark?"#98989F":"#6C6C70");line=c(dark?"#303033":"#DCDCE0");soft=c(dark?"#2C2C2E":"#E5E5EA");}
 static int c(String x){return Color.parseColor(x);} int dp(float n){return (int)(n*a.getResources().getDisplayMetrics().density+.5f);}
 GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 void surface(View v,int color,int radius){v.setBackground(shape(color,radius));}
 LinearLayout col(){LinearLayout l=new LinearLayout(a);l.setOrientation(1);return l;}
 LinearLayout row(){LinearLayout l=new LinearLayout(a);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 TextView text(String s,int size,int color){TextView t=new TextView(a);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("tnum");t.setIncludeFontPadding(false);return t;}
 TextView title(String s,int size){TextView t=text(s,size,ink);t.setTypeface(MEDIUM);return t;}
 TextView number(String s,int size){TextView t=text(s,size,ink);t.setTypeface(LIGHT);t.setSingleLine();t.setAutoSizeTextTypeUniformWithConfiguration(22,size,1,android.util.TypedValue.COMPLEX_UNIT_SP);return t;}
 int tint(int color){return Color.argb(dark?40:30,Color.red(color),Color.green(color),Color.blue(color));}
 int actionColor(int color){if(dark)return color;if(color==ORANGE)return c("#B85C00");if(color==GREEN)return c("#008A2E");if(color==RED)return c("#CE2923");return color;}
 TextView button(String label,boolean accent,Runnable action){TextView b=text(label,16,accent?actionColor(ORANGE):ink);b.setGravity(Gravity.CENTER);b.setMinHeight(dp(48));b.setPadding(dp(16),dp(12),dp(16),dp(12));
  b.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(dark?0x22ffffff:0x16000000),shape(accent?tint(ORANGE):soft,24),null));b.setFocusable(true);b.setContentDescription(label);b.setOnClickListener(v->action.run());return b;}
 TextView link(String label,Runnable action){TextView b=button(label,true,action);surface(b,Color.TRANSPARENT,24);return b;}
 TextView round(String label,int color,int diameter,Runnable action){TextView b=button(label,false,action);b.setTextColor(actionColor(color));b.setTextSize(diameter<60?22:16);b.setPadding(0,0,0,0);b.setMinHeight(0);b.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x22ffffff),shape(tint(color),diameter/2),null));b.setLayoutParams(new LinearLayout.LayoutParams(dp(diameter),dp(diameter)));return b;}
 SeekBar slider(int value){SeekBar s=new SeekBar(a,null,0,R.style.ClockSlider);s.setMax(100);s.setProgress(value);s.setMinimumHeight(dp(48));GradientDrawable track=shape(soft,3),fill=shape(ORANGE,3);track.setSize(dp(200),dp(4));fill.setSize(dp(200),dp(4));ClipDrawable progress=new ClipDrawable(fill,Gravity.LEFT,ClipDrawable.HORIZONTAL);LayerDrawable layers=new LayerDrawable(new Drawable[]{track,progress});layers.setId(0,android.R.id.background);layers.setId(1,android.R.id.progress);s.setProgressDrawable(layers);s.setProgressTintList(android.content.res.ColorStateList.valueOf(ORANGE));s.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(soft));s.setThumbTintList(null);GradientDrawable thumb=shape(Color.WHITE,14);thumb.setStroke(dp(1),line);thumb.setSize(dp(28),dp(28));s.setThumb(thumb);s.setThumbOffset(dp(14));s.setSplitTrack(false);s.setPadding(dp(15),dp(18),dp(15),dp(18));s.setContentDescription("Громкость приложения");return s;}
 void glass(View v){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{c(dark?"#F22D2D30":"#F7FFFFFF"),c(dark?"#F21B1B1D":"#F0F6F6F8")});g.setCornerRadius(dp(34));g.setStroke(dp(1),c(dark?"#35FFFFFF":"#BFFFFFFF"));v.setBackground(g);v.setElevation(dp(5));}
 void divider(LinearLayout parent){View v=new View(a);v.setBackgroundColor(line);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(1));p.setMargins(0,dp(12),0,dp(12));parent.addView(v,p);}
 void gap(LinearLayout l,int h){View g=new View(a);l.addView(g,new LinearLayout.LayoutParams(1,dp(h)));}
 LinearLayout panel(LinearLayout parent){LinearLayout p=col();p.setPadding(dp(20),dp(20),dp(20),dp(20));surface(p,card,24);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(14);parent.addView(p,lp);return p;}
 void equal(LinearLayout row,View v){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.setMargins(dp(3),0,dp(3),0);row.addView(v,p);}
 EditText input(String value,String hint,boolean number){EditText e=new EditText(a);e.setSingleLine(true);e.setText(value);e.setHint(hint);e.setContentDescription(hint);e.setTextSize(16);e.setTextColor(ink);e.setHintTextColor(secondary);e.setPadding(dp(14),dp(12),dp(14),dp(12));surface(e,soft,12);if(number)e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);return e;}
 ScrollView scroll(View content){ScrollView sc=new ScrollView(a);sc.setFillViewport(false);sc.setVerticalScrollBarEnabled(false);sc.addView(content);return sc;}
 Dialog dialog(String title,LinearLayout content){Dialog d=new Dialog(a);LinearLayout root=col();root.setPadding(dp(20),dp(12),dp(20),dp(20));surface(root,card,30);View handle=new View(a);surface(handle,secondary,3);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(dp(36),dp(4));hp.gravity=Gravity.CENTER;hp.bottomMargin=dp(20);root.addView(handle,hp);TextView heading=title(title,22);heading.setGravity(Gravity.CENTER);root.addView(heading);gap(root,18);root.addView(scroll(content));d.setContentView(root);android.view.Window w=d.getWindow();w.setBackgroundDrawableResource(android.R.color.transparent);w.setGravity(Gravity.BOTTOM);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);w.setDimAmount(.45f);w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);return d;}
 void systemBars(View root){a.getWindow().setStatusBarColor(bg);a.getWindow().setNavigationBarColor(bg);root.setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});root.requestApplyInsets();}
 class Icon extends View {
  final int kind,color;final Paint p=new Paint(3);
  Icon(int kind,int color){super(a);this.kind=kind;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  void line(Canvas c,float x,float y,float x2,float y2){c.drawLine(x,y,x2,y2,p);}
  @Override protected void onDraw(Canvas c){c.save();c.scale(getWidth()/24f,getHeight()/24f);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
   if(kind==0){c.drawCircle(12,13,8,p);line(c,12,8,12,13);line(c,12,13,16,13);line(c,9,2,15,2);}
   if(kind==1){c.drawCircle(12,13,7,p);line(c,12,9,12,13);line(c,12,13,15,15);line(c,4,5,7,3);line(c,17,3,20,5);line(c,7,19,5,22);line(c,17,19,19,22);}
   if(kind==2){c.drawCircle(12,14,8,p);line(c,9,2,15,2);line(c,12,2,12,5);line(c,18,6,20,4);line(c,12,9,12,14);line(c,12,14,15,16);}
   if(kind==3){c.drawCircle(12,12,9,p);c.drawOval(8,3,16,21,p);line(c,3,12,21,12);c.drawArc(4,5,20,11,0,180,false,p);c.drawArc(4,13,20,19,180,180,false,p);}
   if(kind==4){c.drawCircle(12,12,9,p);c.drawCircle(12,12,5,p);c.drawCircle(12,12,1,p);}
   if(kind==5){c.drawCircle(12,12,6,p);c.drawCircle(12,12,2,p);for(int j=0;j<8;j++){double v=j*Math.PI/4;line(c,12+(float)Math.cos(v)*7,12+(float)Math.sin(v)*7,12+(float)Math.cos(v)*9,12+(float)Math.sin(v)*9);}}
   if(kind==6){p.setStyle(Paint.Style.FILL);for(int j=0;j<3;j++)c.drawCircle(5+j*7,12,1.7f,p);}
   c.restore();}
 }
 class Dial extends View {
  final JSONObject timer;final Paint p=new Paint(3);final RectF rect=new RectF();
  long textSecond=-1,anchorEnd=Long.MIN_VALUE,anchorRemaining,anchorElapsed;String label="";
  Dial(JSONObject timer){super(a);this.timer=timer;setContentDescription("Осталось "+ClockLogic.duration(Store.remaining(timer)));setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);}
  @Override protected void onMeasure(int w,int h){int s=Math.min(MeasureSpec.getSize(w),dp(240));setMeasuredDimension(s,s);}
  @Override protected void onDraw(Canvas canvas){float size=getWidth(),cx=size/2,cy=size/2,r=size/2-dp(12);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(5));p.setStrokeCap(Paint.Cap.ROUND);p.setColor(line);canvas.drawCircle(cx,cy,r,p);
   long remaining=Store.remaining(timer);
   if(timer.optBoolean("running")){long end=timer.optLong("end");if(end!=anchorEnd||Math.abs(remaining-(anchorRemaining-(SystemClock.elapsedRealtime()-anchorElapsed)))>1500){anchorEnd=end;anchorRemaining=remaining;anchorElapsed=SystemClock.elapsedRealtime();}remaining=Math.max(0,anchorRemaining-(SystemClock.elapsedRealtime()-anchorElapsed));}else anchorEnd=Long.MIN_VALUE;
   float progress=Math.min(1,remaining/(float)Math.max(1,timer.optLong("duration")));rect.set(cx-r,cy-r,cx+r,cy+r);p.setColor(ORANGE);canvas.drawArc(rect,-90,360*progress,false,p);
   long second=(remaining+999)/1000;if(second!=textSecond){textSecond=second;label=ClockLogic.duration(remaining);setContentDescription("Осталось "+label);}
   p.setStyle(Paint.Style.FILL);p.setTypeface(LIGHT);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(dp(39));p.setColor(ink);canvas.drawText(label,cx,cy+dp(5),p);
   p.setTypeface(REGULAR);p.setTextSize(dp(13));p.setColor(secondary);canvas.drawText(timer.optBoolean("ringing")?"Время вышло":timer.optBoolean("running")?"До завершения":"Таймер",cx,cy+dp(34),p);
   if(timer.optBoolean("running")&&getWindowVisibility()==VISIBLE)postInvalidateOnAnimation();
  }
 }
}
