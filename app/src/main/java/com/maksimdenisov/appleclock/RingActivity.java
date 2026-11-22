package com.maksimdenisov.appleclock;
import android.app.*;
import android.content.SharedPreferences;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.json.*;

public class RingActivity extends Activity {
 SharedPreferences preferences;
 @Override public boolean dispatchTouchEvent(MotionEvent event){SmoothWindow.touch(getWindow(),event);return super.dispatchTouchEvent(event);}
 final SharedPreferences.OnSharedPreferenceChangeListener changed=(prefs,key)->{if("state".equals(key))show(Store.load(RingActivity.this));};
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);if(Build.VERSION.SDK_INT>=27){setShowWhenLocked(true);setTurnScreenOn(true);}show(Store.load(this));}
 @Override protected void onNewIntent(android.content.Intent intent){super.onNewIntent(intent);setIntent(intent);show(Store.load(this));}
 @Override protected void onResume(){super.onResume();preferences=getSharedPreferences("clock",0);preferences.registerOnSharedPreferenceChangeListener(changed);show(Store.load(this));}
 @Override protected void onPause(){if(preferences!=null)preferences.unregisterOnSharedPreferenceChangeListener(changed);super.onPause();}
 void show(JSONObject s){if(!Store.ringing(s)){finish();return;}Ui u=new Ui(this,s.optBoolean("dark"));LinearLayout root=u.col();root.setBackgroundColor(u.bg);LinearLayout body=u.col();body.setPadding(u.dp(24),u.dp(64),u.dp(24),u.dp(28));TextView now=u.number(new java.text.SimpleDateFormat("HH:mm",java.util.Locale.ROOT).format(new java.util.Date()),76);now.setGravity(Gravity.CENTER);body.addView(now);u.gap(body,36);
  for(JSONObject o:Store.items(s))if(o.optBoolean("ringing")){LinearLayout p=u.panel(body);u.surface(p,u.bg,0);TextView title=u.title(o.optString("name"),28);title.setGravity(Gravity.CENTER);p.addView(title);u.gap(p,14);TextView hint=u.text(o.optBoolean("alarm")?"Будильник":"Время вышло",17,u.secondary);hint.setGravity(Gravity.CENTER);p.addView(hint);u.gap(p,42);p.addView(u.button("Остановить",true,()->Store.dismiss(this,s,o.optInt("id"),false)));if(o.optBoolean("alarm")){u.gap(p,14);p.addView(u.button("Повторить через 2 минуты",false,()->Store.dismiss(this,s,o.optInt("id"),true)));}}
  root.addView(u.scroll(body),new LinearLayout.LayoutParams(-1,-1));setContentView(root);u.systemBars(root);
 }
}
