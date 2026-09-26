package com.endra.clean;

import android.Manifest;
import android.app.*;
import android.app.usage.StorageStatsManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.Typeface;
import android.os.*;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.*;
import android.widget.*;
import java.util.*;

/** EndraClean UI shell. Existing cache-only cleaning services remain unchanged. */
public final class MainActivity extends Activity {
    private final List<AppEntry> apps=new ArrayList<>();
    private LinearLayout root,rows; private TextView status; private Button clean; private boolean systemPage;

    @Override public void onCreate(Bundle s){super.onCreate(s); showHome();
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1);
    }
    private void base(){
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(getColor(R.color.navy));
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16),dp(10),dp(16),dp(28));
        scroll.addView(root); setContentView(scroll);
    }
    private void header(boolean home){
        LinearLayout bar=new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        if(!home){Button back=small("⌂",this::showHome);bar.addView(back,new LinearLayout.LayoutParams(dp(52),dp(48)));}
        TextView brand=text("ENDRACLEAN",home?18:16,R.color.gold);brand.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(48),1);brand.setGravity(Gravity.CENTER);bar.addView(brand,bp);
        Button help=small("?",this::showHelp);bar.addView(help,new LinearLayout.LayoutParams(dp(52),dp(48)));root.addView(bar);
    }
    private void showHome(){
        systemPage=false;base();header(true);
        HydraArtworkView art=new HydraArtworkView(this);root.addView(art,new LinearLayout.LayoutParams(-1,-2));
        TextView title=text("EndraClean",31,R.color.gold);title.setGravity(Gravity.CENTER);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(title);
        TextView tag=text("CLEANER  •  FASTER  •  SMOOTHER",12,R.color.muted);tag.setGravity(Gravity.CENTER);root.addView(tag);
        LinearLayout p=panel(); LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.setMargins(0,dp(18),0,0);root.addView(p,pp);
        p.addView(navButton("USER APPS","Apps you installed",()->showApps(false)));
        p.addView(navButton("SYSTEM APPS","Built-in Android apps",()->showApps(true)));
        TextView note=text("CACHE ONLY  •  APP DATA IS NEVER CLEARED",11,R.color.gold);note.setGravity(Gravity.CENTER);root.addView(note);
    }
    private void showApps(boolean system){
        systemPage=system;base();header(false);
        TextView title=text(system?"SYSTEM APPS":"USER APPS",24,R.color.gold);title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);root.addView(title);
        root.addView(text(system?"Built-in Android apps · view only":"Installed apps · cache cleaning",12,R.color.muted));
        LinearLayout controls=panel();root.addView(controls);
        controls.addView(text("CACHE CONTROL",12,R.color.gold));
        status=text(system?"System apps are protected by EndraClean.":"Select installed apps to clear cache.",14,R.color.silver);controls.addView(status);
        if(!system){
            controls.addView(button("Grant usage access",false,()->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))));
            controls.addView(button("Enable cleaner service",false,()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
            controls.addView(button("Select all",false,this::selectAll));
        }
        controls.addView(button("Rescan "+(system?"system":"user")+" apps",false,this::scanApps));
        if(!system){clean=button("CLEAN SELECTED CACHE",true,this::startClean);controls.addView(clean);}
        rows=panel();LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.topMargin=dp(16);root.addView(rows,rp);scanApps();
    }
    @Override protected void onResume(){super.onResume();if(rows!=null)scanApps();}
    private void scanApps(){
        if(rows==null)return; apps.clear();rows.removeAllViews();rows.addView(text(systemPage?"SYSTEM APPLICATIONS":"USER APPLICATIONS",12,R.color.gold));
        boolean usage=hasUsageAccess(); StorageStatsManager stats=(StorageStatsManager)getSystemService(STORAGE_STATS_SERVICE); PackageManager pm=getPackageManager();
        for(ApplicationInfo info:pm.getInstalledApplications(0)){
            boolean sys=(info.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))!=0;
            if(systemPage!=sys)continue;
            if(!systemPage&&!CleanerAccessibilityService.eligible(this,info.packageName))continue;
            long bytes=-1;if(usage&&stats!=null)try{bytes=stats.queryStatsForPackage(StorageManager.UUID_DEFAULT,info.packageName,Process.myUserHandle()).getCacheBytes();}catch(Exception ignored){}
            AppEntry e=new AppEntry(info.packageName,pm.getApplicationLabel(info).toString(),bytes);apps.add(e);
        }
        Collections.sort(apps,(a,b)->a.label.compareToIgnoreCase(b.label));
        for(AppEntry e:apps){
            if(systemPage){TextView v=text(e.label+(e.cacheBytes>=0?"  ·  "+Formatter.formatShortFileSize(this,e.cacheBytes):""),15,R.color.silver);v.setPadding(dp(8),dp(12),dp(8),dp(12));rows.addView(v);}
            else{CheckBox b=new CheckBox(this);b.setText(e.label+(e.cacheBytes>=0?"  ·  "+Formatter.formatShortFileSize(this,e.cacheBytes):""));b.setTextColor(getColor(R.color.silver));b.setButtonTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.gold)));b.setPadding(dp(6),dp(8),dp(6),dp(8));b.setOnCheckedChangeListener((x,on)->e.selected=on);rows.addView(b);}
        }
        if(status!=null)status.setText(systemPage?apps.size()+" protected system apps found. Viewing only.":apps.size()+" user apps found. "+(usage?"Cache sizes shown where available.":"Grant usage access to show cache sizes."));
        if(clean!=null)clean.setEnabled(CleanerAccessibilityService.connected());
    }
    private void selectAll(){for(int i=0;i<rows.getChildCount();i++)if(rows.getChildAt(i) instanceof CheckBox)((CheckBox)rows.getChildAt(i)).setChecked(true);}
    private boolean hasUsageAccess(){AppOpsManager o=(AppOpsManager)getSystemService(APP_OPS_SERVICE);return o!=null&&o.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,Process.myUid(),getPackageName())==AppOpsManager.MODE_ALLOWED;}
    private void startClean(){
        ArrayList<String>s=new ArrayList<>();for(AppEntry e:apps)if(e.selected&&CleanerAccessibilityService.eligible(this,e.packageName))s.add(e.packageName);
        if(s.isEmpty()){status.setText("Select at least one user app.");return;}if(!CleanerAccessibilityService.connected()){status.setText("Enable the cleaner service first.");return;}
        Intent i=new Intent(this,CleanSessionService.class).setAction(CleanSessionService.START);i.putStringArrayListExtra(CleanSessionService.PACKAGES,s);
        try{startForegroundService(i);status.setText("Cleaning "+s.size()+" selected user apps. Watch the notification for progress.");}catch(RuntimeException ex){status.setText("Could not start cleaning: "+ex.getMessage());}
    }
    private void showHelp(){new AlertDialog.Builder(this).setTitle("EndraClean Help").setMessage("USER APPS\nApps you installed. EndraClean can guide Android Settings to clear only their cache. App data and storage are never cleared.\n\nSYSTEM APPS\nApps built into Android or supplied by the device maker. EndraClean lists them separately so you can identify them, but the existing safety engine blocks cleaning system apps.\n\nUse Grant usage access to display cache sizes and Enable cleaner service to run a user-started cache cleaning pass.").setPositiveButton("Got it",null).show();}
    private LinearLayout navButton(String a,String b,Runnable r){LinearLayout p=panel();p.setClickable(true);p.setFocusable(true);p.setOnClickListener(v->r.run());TextView t=text(a,20,R.color.gold);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);p.addView(t);p.addView(text(b,13,R.color.muted));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,dp(7));p.setLayoutParams(lp);return p;}
    private LinearLayout panel(){LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(16),dp(14),dp(16),dp(14));p.setBackgroundResource(R.drawable.panel);return p;}
    private TextView text(String s,int z,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(getColor(c));v.setPadding(0,dp(5),0,dp(5));return v;}
    private Button small(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextColor(getColor(R.color.gold));b.setTextSize(20);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackgroundResource(R.drawable.tech_button);b.setBackgroundTintList(null);b.setOnClickListener(v->r.run());return b;}
    private Button button(String s,boolean primary,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(getColor(primary?R.color.navy:R.color.silver));b.setTextSize(16);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackgroundResource(primary?R.drawable.tech_primary:R.drawable.tech_button);b.setBackgroundTintList(null);b.setMinHeight(dp(58));b.setOnClickListener(v->r.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.topMargin=dp(9);b.setLayoutParams(lp);return b;}
    private int dp(int v){return(int)(v*getResources().getDisplayMetrics().density+.5f);}
}