package com.maksimdenisov.appleclock;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import android.text.*;
import org.json.*;
import java.text.*;
import java.util.*;

public class MainActivity extends Activity {
 JSONObject state;Ui u;LinearLayout root,body;ScrollView scroll;int page,scrollPage;boolean visible,editing,scrollTop,draftDirty,volumeDirty;Handler handler=new Handler();ArrayList<Runnable> refresh=new ArrayList<>();
 static final String[] TITLES={"Таймеры","Будильники","Секундомер","Мировые часы","Фокусировка","Настройки"};
 static final String[] SUB={"Маленькие паузы. Большие планы.","Начните день в своём ритме.","Каждое мгновение имеет значение.","Один момент. Разные города.","Одно дело. Всё внимание.","Ваши часы — ваши привычки."};
 static final String[] CITIES={"Москва","Лондон","Нью-Йорк","Токио","Париж","Дубай","Лос-Анджелес","Сидней","Пекин","Берлин","Екатеринбург","Новосибирск","Владивосток","Калининград","Стамбул","Дели"};
 static final String[] ZONES={"Europe/Moscow","Europe/London","America/New_York","Asia/Tokyo","Europe/Paris","Asia/Dubai","America/Los_Angeles","Australia/Sydney","Asia/Shanghai","Europe/Berlin","Asia/Yekaterinburg","Asia/Novosibirsk","Asia/Vladivostok","Europe/Kaliningrad","Europe/Istanbul","Asia/Kolkata"};
 final Runnable saveDraft=()->flushDraft();
 void flushDraft(){if(!draftDirty)return;JSONObject latest=Store.load(this);Store.put(latest,"task",state.optString("task"));Store.saveUi(this,latest);draftDirty=false;}
 final Runnable ticker=new Runnable(){public void run(){if(!visible)return;if(Store.tick(MainActivity.this,state)){if(Store.ringing(state))startForegroundService(new Intent(MainActivity.this,RingService.class));build();}for(int i=0;i<refresh.size();i++)refresh.get(i).run();handler.postDelayed(this,1000-System.currentTimeMillis()%1000);}};
 @Override public void onCreate(Bundle b){super.onCreate(b);state=Store.load(this);page=b==null?state.optInt("page"):b.getInt("page");if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7);}
 @Override protected void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putInt("page",page);}
 @Override protected void onResume(){super.onResume();state=Store.load(this);visible=true;Store.schedule(this,state);build();handler.removeCallbacks(ticker);handler.post(ticker);}
 @Override protected void onPause(){visible=false;handler.removeCallbacks(ticker);handler.removeCallbacks(saveDraft);flushDraft();flushVolume();super.onPause();}
 void save(){Store.changed(this,state);}void change(){save();build();}
 void build(){
  int previousY=scroll!=null&&scrollPage==page&&!scrollTop?scroll.getScrollY():0;scrollTop=false;
  u=new Ui(this,state.optBoolean("dark"));refresh.clear();root=u.col();root.setBackgroundColor(u.bg);
  LinearLayout top=u.row();top.setPadding(u.dp(14),u.dp(6),u.dp(14),0);
  if(page==1||page==3)top.addView(u.link(editing?"Готово":"Править",()->{editing=!editing;build();}),new LinearLayout.LayoutParams(0,-2,1));
  else {TextView brand=u.text("Часы",17,u.secondary);brand.setPadding(u.dp(6),0,0,0);top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));}
  TextView theme=u.round("◐",u.ink,44,()->{Store.put(state,"dark",!state.optBoolean("dark"));Store.saveUi(this,state);build();});theme.setContentDescription("Переключить светлую и тёмную тему");top.addView(theme);
  if(page==0||page==1||page==3){TextView add=u.round("+",Ui.ORANGE,44,()->{if(page==0)editTimer(null);else if(page==1)editAlarm(null);else addCity();});add.setTextSize(32);add.setContentDescription("Добавить");top.addView(add);}root.addView(top);
  body=u.col();body.setPadding(u.dp(20),u.dp(10),u.dp(20),u.dp(20));TextView title=u.title(TITLES[page],34);body.addView(title);u.gap(body,24);
  if(page==0)timers();else if(page==1)alarms();else if(page==2)stopwatch();else if(page==3)world();else if(page==4)focus();else settings();
  scroll=u.scroll(body);scrollPage=page;root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout nav=u.row();nav.setPadding(u.dp(5),u.dp(6),u.dp(5),u.dp(6));u.glass(nav);
  int[] targets={3,1,2,0,5},icons={3,1,2,0,6};String[] labels={"Мир","Будильник","Секундомер","Таймеры","Ещё"};
  for(int j=0;j<targets.length;j++){
   final int target=targets[j];boolean selected=target==page||(target==5&&page==4);int color=selected?u.actionColor(Ui.ORANGE):u.secondary;
   LinearLayout tab=u.col();tab.setGravity(Gravity.CENTER);tab.setPadding(0,u.dp(7),0,u.dp(7));tab.setMinimumHeight(u.dp(58));tab.addView(u.new Icon(icons[j],color),new LinearLayout.LayoutParams(u.dp(24),u.dp(24)));u.gap(tab,5);
   TextView label=u.text(labels[j],10,color);label.setGravity(Gravity.CENTER);label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);label.setSingleLine(true);label.setEllipsize(TextUtils.TruncateAt.END);tab.addView(label,new LinearLayout.LayoutParams(-1,-2));
   tab.setContentDescription(target==5?"Ещё: фокусировка и настройки":TITLES[target]);tab.setFocusable(true);if(selected)u.surface(tab,u.soft,27);
   tab.setOnClickListener(v->{if(target==5)more();else navigate(target);});nav.addView(tab,new LinearLayout.LayoutParams(0,-2,1));
  }
  LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.setMargins(u.dp(12),u.dp(5),u.dp(12),u.dp(8));root.addView(nav,np);
  setContentView(root);u.systemBars(root);for(Runnable r:refresh)r.run();ScrollView current=scroll;current.post(()->current.scrollTo(0,previousY));
 }
 void navigate(int target){page=target;editing=false;Store.put(state,"page",page);Store.saveUi(this,state);build();}
 void more(){LinearLayout c=u.col();Dialog d=u.dialog("Ещё",c);c.addView(u.button("Фокусировка",false,()->{d.dismiss();navigate(4);}));u.gap(c,10);c.addView(u.button("Настройки",false,()->{d.dismiss();navigate(5);}));u.gap(c,10);c.addView(u.link("Отмена",d::dismiss));d.show();size(d);}

 void addButton(String label,Runnable r){body.addView(u.button(label,true,r));u.gap(body,16);}
 void timers(){
  body.addView(u.text("БЫСТРЫЙ СТАРТ",11,u.secondary));u.gap(body,12);
  LinearLayout quick=u.row();for(int m:new int[]{1,5,10,15,30}){TextView b=u.button(m+" мин",true,()->{JSONObject t=Store.timer(Store.id(state),m+" минут",m*60000L);Store.addTimer(state,t);scrollTop=true;startTimer(t);});b.setTextSize(13);b.setPadding(u.dp(4),u.dp(12),u.dp(4),u.dp(12));u.equal(quick,b);}body.addView(quick);u.gap(body,24);
  JSONArray a=state.optJSONArray("timers");for(int i=0;i<a.length();i++){JSONObject t=a.optJSONObject(i);if(i==0)timerCard(t,false);else timerRow(t);}
  if(a.length()==0){u.gap(body,35);TextView empty=u.text("Нет таймеров",24,u.secondary);empty.setGravity(Gravity.CENTER);body.addView(empty);u.gap(body,16);addButton("Добавить таймер",()->editTimer(null));}
 }
 void timerRow(JSONObject t){
  LinearLayout p=u.panel(body),row=u.row(),info=u.col();TextView name=u.text(t.optString("name"),15,u.secondary),time=u.number("",34);info.addView(name);u.gap(info,6);info.addView(time);row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
  refresh.add(()->setTextIfChanged(time,ClockLogic.duration(Store.remaining(t))));
  boolean ring=t.optBoolean("ringing"),running=t.optBoolean("running");TextView control=u.round(ring?"■":running?"Ⅱ":"▶",ring?Ui.ORANGE:running?Ui.ORANGE:Ui.GREEN,48,()->{if(ring){Store.dismiss(this,state,t.optInt("id"),false);build();}else if(running){Store.pause(t);change();}else startTimer(t);});control.setContentDescription(ring?"Остановить сигнал":running?"Пауза":"Старт");row.addView(control);
  if(!ring){TextView menu=u.link("•••",()->timerMenu(t));menu.setContentDescription("Действия таймера "+t.optString("name"));row.addView(menu);}p.addView(row);
 }
 static void setTextIfChanged(TextView view,String value){if(!value.contentEquals(view.getText()))view.setText(value);}

 void startTimer(JSONObject t){if(!Store.exact(this)){Store.saveUi(this,state);permissionExact();return;}Store.start(t);change();}
 void timerCard(JSONObject t,boolean focus){
  LinearLayout p=u.panel(body);u.surface(p,u.bg,0);p.setPadding(u.dp(4),0,u.dp(4),u.dp(14));LinearLayout head=u.row();TextView name=u.title(t.optString("name"),21);head.addView(name,new LinearLayout.LayoutParams(0,-2,1));
  if(!focus&&!t.optBoolean("ringing"))head.addView(u.link("•••",()->timerMenu(t)));p.addView(head);u.gap(p,12);
  Ui.Dial dial=u.new Dial(t);LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,-2);dp.gravity=Gravity.CENTER;p.addView(dial,dp);u.gap(p,18);
  if(t.optBoolean("ringing")){p.addView(u.button("Остановить сигнал",true,()->{Store.dismiss(this,state,t.optInt("id"),false);build();}));return;}
  LinearLayout actions=u.row();actions.setGravity(Gravity.CENTER);TextView reset=u.round("Сбросить",u.secondary,78,()->{Store.reset(t);change();});actions.addView(reset);
  View spacer=new View(this);actions.addView(spacer,new LinearLayout.LayoutParams(0,1,1));
  actions.addView(u.round(t.optBoolean("running")?"Пауза":"Старт",t.optBoolean("running")?Ui.ORANGE:Ui.GREEN,78,()->{if(t.optBoolean("running")){Store.pause(t);change();}else startTimer(t);}));p.addView(actions);
 }

 void timerMenu(JSONObject t){LinearLayout c=u.col();Dialog d=u.dialog(t.optString("name"),c);c.addView(u.button("Изменить",false,()->{d.dismiss();if(!t.optBoolean("ringing"))editTimer(t);}));u.gap(c,10);c.addView(u.button("Удалить",false,()->{d.dismiss();confirm("Удалить таймер?",()->{cancel(t);remove("timers",t);change();});}));u.gap(c,10);c.addView(u.button("Отмена",false,d::dismiss));d.show();size(d);}
 void cancel(JSONObject t){((AlarmManager)getSystemService(ALARM_SERVICE)).cancel(Store.pending(this,t.optInt("id")));if(t.optBoolean("ringing"))Store.dismiss(this,state,t.optInt("id"),false);Store.forgetSchedule(t.optInt("id"));}
 void remove(String key,JSONObject t){JSONArray a=state.optJSONArray(key);for(int i=0;i<a.length();i++)if(a.optJSONObject(i).optInt("id")==t.optInt("id")){a.remove(i);return;}}
 void size(Dialog d){d.getWindow().setLayout(Math.min(getResources().getDisplayMetrics().widthPixels,u.dp(540)),-2);d.getWindow().setGravity(Gravity.BOTTOM);}
 void confirm(String title,Runnable r){LinearLayout c=u.col();Dialog d=u.dialog(title,c);c.addView(u.button("Удалить",true,()->{d.dismiss();r.run();}));u.gap(c,10);c.addView(u.button("Отмена",false,d::dismiss));d.show();size(d);}
 void editTimer(JSONObject old){
  LinearLayout c=u.col();TimeWheel wheel=new TimeWheel(u,old==null?300000:old.optLong("duration"),false);c.addView(wheel);u.gap(c,12);
  EditText name=u.input(old==null?"":old.optString("name"),"Название таймера",false);c.addView(name);u.gap(c,12);
  EditText time=u.input(ClockLogic.duration(wheel.millis()),"Время ЧЧ:ММ:СС",false);time.setVisibility(View.GONE);c.addView(u.link("Ввести время вручную",()->{time.setVisibility(time.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);}));c.addView(time);
  wheel.changed=()->time.setText(ClockLogic.duration(wheel.millis()));u.gap(c,10);
  LinearLayout adjust=u.row();for(int sign:new int[]{-1,1})u.equal(adjust,u.button((sign>0?"+":"−")+state.optInt("step",10)+" сек",false,()->{time.setVisibility(View.VISIBLE);long value=ClockLogic.parseDuration(time.getText().toString());if(value<0)value=1000;time.setText(ClockLogic.duration(Math.max(1000,Math.min(359999000,value+sign*state.optInt("step",10)*1000L))));}));c.addView(adjust);u.gap(c,12);
  TextView error=u.text("",13,u.actionColor(Ui.ORANGE));c.addView(error);Dialog d=u.dialog(old==null?"Новый таймер":"Изменить таймер",c);
  c.addView(u.button("Сохранить",true,()->{long duration=time.getVisibility()==View.VISIBLE?ClockLogic.parseDuration(time.getText().toString()):wheel.millis();if(duration<1000){error.setText("Выберите время от 1 секунды до 99:59:59");return;}String n=name.getText().toString().trim();JSONObject t=old==null?Store.timer(Store.id(state),n.isEmpty()?"Таймер":n,duration):old;Store.put(t,"name",n.isEmpty()?"Таймер":n);Store.put(t,"duration",duration);Store.reset(t);if(old==null){Store.addTimer(state,t);scrollTop=true;}d.dismiss();change();}));u.gap(c,8);c.addView(u.link("Отмена",d::dismiss));d.show();size(d);
 }

 void alarms(){
  JSONArray a=state.optJSONArray("alarms");if(a.length()==0){u.gap(body,42);TextView empty=u.text("Нет будильников",24,u.secondary);empty.setGravity(Gravity.CENTER);body.addView(empty);u.gap(body,20);addButton("Добавить будильник",()->editAlarm(null));}
  for(int i=0;i<a.length();i++){
   JSONObject alarm=a.optJSONObject(i);LinearLayout p=u.col();p.setPadding(0,u.dp(10),0,u.dp(8));LinearLayout row=u.row(),info=u.col();
   TextView time=u.number(ClockLogic.two(alarm.optInt("hour"))+":"+ClockLogic.two(alarm.optInt("minute")),60);time.setTextColor(alarm.optBoolean("enabled")?u.ink:u.secondary);info.addView(time);u.gap(info,6);info.addView(u.text(alarm.optString("name")+", "+days(alarm.optInt("days")),15,u.secondary));info.setOnClickListener(v->{if(!alarm.optBoolean("ringing"))editAlarm(alarm);});row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
   AlarmToggle toggle=new AlarmToggle(u,alarm.optBoolean("enabled"),()->{if(!alarm.optBoolean("enabled")&&!Store.exact(this)){permissionExact();return;}Store.put(alarm,"enabled",!alarm.optBoolean("enabled"));if(alarm.optBoolean("enabled"))Store.next(alarm);change();});row.addView(toggle);p.addView(row);
   if(editing){u.gap(p,12);LinearLayout controls=u.row();u.equal(controls,u.link("Изменить",()->editAlarm(alarm)));u.equal(controls,u.link("Удалить",()->confirm("Удалить будильник?",()->{cancel(alarm);remove("alarms",alarm);change();})));p.addView(controls);}
   if(alarm.optBoolean("ringing")){u.gap(p,12);p.addView(u.button("Остановить сигнал",true,()->{Store.dismiss(this,state,alarm.optInt("id"),false);build();}));}body.addView(p);u.divider(body);
  }
 }

 static String days(int mask){if(mask==0)return "Один раз";if(mask==127)return "Каждый день";String[] d={"Пн","Вт","Ср","Чт","Пт","Сб","Вс"};StringBuilder s=new StringBuilder();for(int i=0;i<7;i++)if((mask&(1<<i))!=0)s.append(d[i]).append(" ");return s.toString();}
 void editAlarm(JSONObject old){
  LinearLayout c=u.col();long ms=old==null?(7*60+30)*60000L:(old.optInt("hour")*60+old.optInt("minute"))*60000L;TimeWheel wheel=new TimeWheel(u,ms,true);c.addView(wheel);u.gap(c,12);
  EditText name=u.input(old==null?"Будильник":old.optString("name"),"Название",false);c.addView(name);u.gap(c,10);
  EditText time=u.input(ClockLogic.duration(ms).substring(0,5),"Время ЧЧ:ММ",false);time.setVisibility(View.GONE);c.addView(u.link("Ввести время вручную",()->time.setVisibility(time.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE)));c.addView(time);wheel.changed=()->time.setText(ClockLogic.duration(wheel.millis()).substring(0,5));u.gap(c,12);
  c.addView(u.text("ПОВТОР · БЕЗ ВЫБОРА — ОДИН РАЗ",11,u.secondary));u.gap(c,12);int[] mask={old==null?0:old.optInt("days")};LinearLayout week=u.row();String[] names={"Пн","Вт","Ср","Чт","Пт","Сб","Вс"};
  for(int i=0;i<7;i++){final int n=i;TextView b=u.button(names[i],(mask[0]&(1<<i))!=0,()->{});b.setTextSize(13);b.setPadding(0,u.dp(10),0,u.dp(10));b.setOnClickListener(v->{mask[0]^=1<<n;u.surface(b,(mask[0]&(1<<n))!=0?u.tint(Ui.ORANGE):u.soft,24);b.setTextColor((mask[0]&(1<<n))!=0?u.actionColor(Ui.ORANGE):u.ink);});u.equal(week,b);}c.addView(week);u.gap(c,16);
  TextView error=u.text("",13,u.actionColor(Ui.ORANGE));c.addView(error);Dialog d=u.dialog("Будильник",c);
  c.addView(u.button("Сохранить",true,()->{String value=time.getVisibility()==View.VISIBLE?time.getText().toString():ClockLogic.duration(wheel.millis()).substring(0,5);if(!value.matches("([01][0-9]|2[0-3]):[0-5][0-9]")){error.setText("Введите время от 00:00 до 23:59");return;}if(!Store.exact(this)){permissionExact();return;}JSONObject o=old==null?Store.obj("id",Store.id(state),"alarm",true):old;Store.put(o,"hour",Integer.parseInt(value.substring(0,2)));Store.put(o,"minute",Integer.parseInt(value.substring(3)));Store.put(o,"name",name.getText().toString().trim().isEmpty()?"Будильник":name.getText().toString().trim());Store.put(o,"days",mask[0]);Store.put(o,"enabled",true);Store.next(o);if(old==null)state.optJSONArray("alarms").put(o);d.dismiss();change();}));u.gap(c,8);c.addView(u.link("Отмена",d::dismiss));d.show();size(d);
 }

 long stopwatchMs(){long v=state.optLong("swValue");if(state.optBoolean("swRunning"))v+=Math.max(0,SystemClock.elapsedRealtime()-state.optLong("swStart"));return v;}
 void stopwatch(){
  int boot=Settings.Global.getInt(getContentResolver(),"boot_count",0);if(state.optInt("swBoot",boot)!=boot){Store.put(state,"swRunning",false);Store.put(state,"swValue",0);Store.put(state,"swBoot",boot);Store.saveUi(this,state);}
  body.addView(new StopwatchDisplay(u,()->stopwatchMs(),()->state.optBoolean("swRunning")),new LinearLayout.LayoutParams(-1,u.dp(230)));
  LinearLayout actions=u.row();boolean running=state.optBoolean("swRunning");TextView left=u.round(running?"Круг":"Сбросить",u.secondary,82,()->{if(state.optBoolean("swRunning")){state.optJSONArray("laps").put(stopwatchMs());Store.saveUi(this,state);build();}else{Store.put(state,"swValue",0);Store.put(state,"laps",new JSONArray());Store.saveUi(this,state);build();}});actions.addView(left);actions.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
  actions.addView(u.round(running?"Стоп":"Старт",running?Ui.RED:Ui.GREEN,82,()->{boolean active=state.optBoolean("swRunning");Store.put(state,"swValue",stopwatchMs());Store.put(state,"swStart",SystemClock.elapsedRealtime());Store.put(state,"swBoot",boot);Store.put(state,"swRunning",!active);Store.saveUi(this,state);build();}));body.addView(actions);u.gap(body,28);
  JSONArray laps=state.optJSONArray("laps");for(int i=laps.length()-1;i>=0;i--){LinearLayout row=u.row();long total=laps.optLong(i),split=total-(i==0?0:laps.optLong(i-1));row.addView(u.text("Круг "+(i+1),17,u.ink),new LinearLayout.LayoutParams(0,-2,1));row.addView(u.text(ClockLogic.stopwatch(split),20,u.ink));row.setPadding(0,u.dp(7),0,u.dp(7));body.addView(row);u.divider(body);}
 }

 void world(){
  JSONArray cities=state.optJSONArray("cities");if(cities.length()==0)addButton("Добавить город",this::addCity);
  for(int i=0;i<cities.length();i++){
   final int index=i,city=cities.optInt(i);LinearLayout row=u.row(),info=u.col();TextView name=u.title(CITIES[city],25),date=u.text("",13,u.secondary);info.addView(date);u.gap(info,7);info.addView(name);row.addView(info,new LinearLayout.LayoutParams(0,-2,1));TextView time=u.number("",43);row.addView(time);row.setPadding(0,u.dp(12),0,u.dp(12));body.addView(row);
   SimpleDateFormat f=new SimpleDateFormat("HH:mm:ss",Locale.ROOT),d=new SimpleDateFormat("EEE, d MMM · z",new Locale("ru"));f.setTimeZone(TimeZone.getTimeZone(ZONES[city]));d.setTimeZone(f.getTimeZone());Date now=new Date();refresh.add(()->{now.setTime(System.currentTimeMillis());setTextIfChanged(time,f.format(now));setTextIfChanged(date,d.format(now));});
   if(editing)body.addView(u.link("Удалить "+CITIES[city],()->{cities.remove(index);Store.saveUi(this,state);build();}));u.divider(body);
  }
 }

 void addCity(){LinearLayout c=u.col();EditText query=u.input("","Поиск города",false);c.addView(query);u.gap(c,14);LinearLayout list=u.col();c.addView(list);Dialog d=u.dialog("Добавить город",c);Runnable filter=()->{list.removeAllViews();String q=query.getText().toString().toLowerCase(new Locale("ru"));for(int j=0;j<CITIES.length;j++){final int city=j;boolean exists=false;JSONArray arr=state.optJSONArray("cities");for(int i=0;i<arr.length();i++)if(arr.optInt(i)==city)exists=true;if(!exists&&CITIES[j].toLowerCase(new Locale("ru")).contains(q)){list.addView(u.button(CITIES[j],false,()->{arr.put(city);d.dismiss();change();}));u.gap(list,6);}}if(list.getChildCount()==0)list.addView(u.text("Нет новых городов по этому запросу",14,u.secondary));};query.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){filter.run();}public void afterTextChanged(Editable e){}});filter.run();u.gap(c,8);c.addView(u.button("Закрыть",false,d::dismiss));d.show();size(d);}
 void focus(){LinearLayout p=u.panel(body);EditText task=u.input(state.optString("task"),"На чём сосредоточимся?",false);p.addView(task);task.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){Store.put(state,"task",s.toString());draftDirty=true;handler.removeCallbacks(saveDraft);handler.postDelayed(saveDraft,500);}public void afterTextChanged(Editable e){}});u.gap(p,12);p.addView(u.text(state.optBoolean("onBreak")?"Сейчас перерыв":"Сейчас время для важного",14,u.secondary));JSONObject t=state.optJSONObject("focus");timerCard(t,true);LinearLayout config=u.panel(body);EditText work=u.input(""+state.optInt("work",25),"Работа, минут (1–180)",true),rest=u.input(""+state.optInt("rest",5),"Перерыв, минут (1–60)",true);config.addView(u.text("Работа, минут",13,u.secondary));u.gap(config,8);config.addView(work);u.gap(config,12);config.addView(u.text("Перерыв, минут",13,u.secondary));u.gap(config,8);config.addView(rest);u.gap(config,12);TextView error=u.text("",13,u.actionColor(Ui.ORANGE));config.addView(error);config.addView(u.button("Применить интервалы",false,()->{int w=number(work),r=number(rest);if(w<1||w>180||r<1||r>60){error.setText("Работа: 1–180 мин, перерыв: 1–60 мин");return;}if(t.optBoolean("ringing")){error.setText("Сначала остановите сигнал");return;}Store.put(state,"work",w);Store.put(state,"rest",r);Store.put(t,"duration",(state.optBoolean("onBreak")?r:w)*60000L);Store.reset(t);change();}));String day=new SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());u.gap(config,16);config.addView(u.text("Сессий сегодня: "+(day.equals(state.optString("focusDay"))?state.optInt("completed"):0),14,u.secondary));}
 int number(EditText e){try{return Integer.parseInt(e.getText().toString());}catch(NumberFormatException ex){return -1;}}
 void flushVolume(){if(!volumeDirty)return;JSONObject latest=Store.load(this);Store.put(latest,"volume",state.optInt("volume",100));Store.saveUi(this,latest);volumeDirty=false;if(Store.ringing(latest))startService(new Intent(this,RingService.class));}
 void toggleRow(LinearLayout p,String label,boolean checked,Runnable action){LinearLayout row=u.row();row.addView(u.text(label,17,u.ink),new LinearLayout.LayoutParams(0,-2,1));AlarmToggle toggle=new AlarmToggle(u,checked,action);toggle.setContentDescription(label);row.addView(toggle);p.addView(row);}
 void settings(){
  LinearLayout appearance=u.panel(body);toggleRow(appearance,"Тёмная тема",state.optBoolean("dark"),()->{Store.put(state,"dark",!state.optBoolean("dark"));Store.saveUi(this,state);build();});
  LinearLayout sound=u.panel(body);toggleRow(sound,"Звук сигналов",state.optBoolean("sound",true),()->{Store.put(state,"sound",!state.optBoolean("sound",true));Store.saveUi(this,state);if(Store.ringing(state))startService(new Intent(this,RingService.class));build();});u.divider(sound);
  TextView volumeLabel=u.text("Громкость · "+state.optInt("volume",100)+"%",15,u.secondary);sound.addView(volumeLabel);u.gap(sound,10);SeekBar volume=u.slider(state.optInt("volume",100));sound.addView(volume,new LinearLayout.LayoutParams(-1,u.dp(48)));
  volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar bar){}public void onProgressChanged(SeekBar bar,int value,boolean user){if(user){Store.put(state,"volume",value);volumeDirty=true;volumeLabel.setText("Громкость · "+value+"%");if(!bar.isPressed())flushVolume();}}public void onStopTrackingTouch(SeekBar bar){flushVolume();}});
  LinearLayout timer=u.panel(body);timer.addView(u.title("Шаг таймера",18));u.gap(timer,10);timer.addView(u.text("Изменение времени кнопками, в секундах",13,u.secondary));u.gap(timer,12);EditText step=u.input(""+state.optInt("step",10),"Шаг в секундах",true);timer.addView(step);u.gap(timer,12);LinearLayout arrows=u.row();for(int sign:new int[]{-1,1})u.equal(arrows,u.button(sign<0?"− 1 сек":"+ 1 сек",false,()->step.setText(""+Math.max(1,Math.min(3600,Math.max(1,number(step))+sign)))));timer.addView(arrows);u.gap(timer,12);TextView error=u.text("",13,u.actionColor(Ui.ORANGE));timer.addView(error);timer.addView(u.button("Сохранить шаг",true,()->{int st=number(step);if(st<1||st>3600){error.setText("Допустимо от 1 до 3600 секунд");return;}Store.put(state,"step",st);Store.saveUi(this,state);error.setText("Сохранено");}));
  LinearLayout access=u.panel(body);access.addView(u.title("Работа на Samsung",19));u.gap(access,12);NotificationManager nm=getSystemService(NotificationManager.class);access.addView(u.text("Уведомления: "+(nm.areNotificationsEnabled()?"разрешены":"отключены")+"\nТочные сигналы: "+(Store.exact(this)?"разрешены":"требуется доступ"),14,u.secondary));u.gap(access,12);access.addView(u.button("Настройки уведомлений",false,()->startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()))));if(!Store.exact(this)){u.gap(access,10);access.addView(u.button("Разрешить точные сигналы",false,this::permissionExact));}
  if(Build.VERSION.SDK_INT>=34&&!nm.canUseFullScreenIntent()){u.gap(access,10);access.addView(u.button("Сигнал на экране блокировки",false,()->startActivity(new Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT",android.net.Uri.parse("package:"+getPackageName())))));}
  u.gap(access,12);access.addView(u.text("Разрешите уведомления и полноэкранные сигналы. Если Samsung ограничивает часы, добавьте Apple Clock в список «Никогда не спящие приложения». Громкость будильника телефона и режим «Не беспокоить» влияют на слышимость.\n\nТаймеры работают после сворачивания и удаления из списка недавних. После принудительной остановки в настройках Android нужно снова открыть часы. Для обновления устанавливайте APK поверх прежней версии.",13,u.secondary));
  LinearLayout about=u.panel(body);about.addView(u.title("Apple Clock 1.1.0",18));u.gap(about,10);about.addView(u.text("Android 8.0 и новее · работает без интернета\nДизайн по мотивам Apple Clock для Windows.\nМелодия Koshi — Membeth, CC0.\nНезависимый проект, не приложение Apple.",13,u.secondary));
 }
 void permissionExact(){if(Build.VERSION.SDK_INT>=31)startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,android.net.Uri.parse("package:"+getPackageName())));}
}
