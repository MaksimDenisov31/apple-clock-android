package com.maksimdenisov.appleclock;
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;

public class RingActivity extends Activity {
 Handler h=new Handler();String snapshot="";
 final Runnable tick=new Runnable(){public void run(){JSONObject s=Store.load(RingActivity.this);String now=s.toString();if(!now.equals(snapshot)){snapshot=now;show(s);}h.postDelayed(this,500);}};
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);if(Build.VERSION.SDK_INT>=27){setShowWhenLocked(true);setTurnScreenOn(true);}show(Store.load(this));}
 @Override protected void onResume(){super.onResume();h.post(tick);}
 @Override protected void onPause(){h.removeCallbacks(tick);super.onPause();}
 void show(JSONObject s){if(!Store.ringing(s)){finish();return;}Ui u=new Ui(this,s.optBoolean("dark"));LinearLayout root=u.col();root.setBackgroundColor(u.bg);LinearLayout body=u.col();body.setPadding(u.dp(24),u.dp(60),u.dp(24),u.dp(24));body.addView(u.text("APPLE CLOCK",13,u.secondary));u.gap(body,18);body.addView(u.title("Время для важного",29));u.gap(body,28);
  for(JSONObject o:Store.items(s))if(o.optBoolean("ringing")){LinearLayout p=u.panel(body);p.addView(u.title(o.optString("name"),25));u.gap(p,14);p.addView(u.text(o.optBoolean("alarm")?"Пора просыпаться":"Время вышло",16,u.secondary));u.gap(p,24);p.addView(u.button("Остановить",true,()->{Store.dismiss(this,s,o.optInt("id"),false);show(Store.load(this));}));if(o.optBoolean("alarm")){u.gap(p,12);p.addView(u.button("Ещё 5 минут",false,()->{Store.dismiss(this,s,o.optInt("id"),true);show(Store.load(this));}));}}
  root.addView(u.scroll(body),new LinearLayout.LayoutParams(-1,-1));setContentView(root);u.systemBars(root);
 }
}
