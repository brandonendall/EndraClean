package com.endra.clean;

import android.Manifest;
import android.app.*;
import android.app.usage.StorageStatsManager;
import android.content.*;
import android.content.pm.*;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.*;
import android.widget.*;
import java.util.*;

/** EndraClean UI shell. Existing cache-only cleaning services remain unchanged. */
public final class MainActivity extends Activity {
    private static final int CONTENT_MAX_DP=560;
    private final List<AppEntry> apps=new ArrayList<>();
    private LinearLayout root,rows; private TextView status; private Button clean; private boolean systemPage;

    @Override public void onCreate(Bundle s){super.onCreate(s); showHome();
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1);
    }
    private void base(){
        FrameLayout shell=new FrameLayout(this); shell.setBackgroundColor(getColor(R.color.navy));
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.setClipToPadding(false);
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(12),dp(8),dp(12),dp(28));
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));
        FrameLayout.LayoutParams slp=new FrameLayout.LayoutParams(-1,-1,Gravity.CENTER);
        slp.width=Math.min(getResources().getDisplayMetrics().widthPixels,dp(CONTENT_MAX_DP));
        shell.addView(scroll,slp); setContentView(shell);
    }
    private void header(boolean home){
        LinearLayout bar=new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        if(!home){Button back=small("⌂  Home",this::showHome);bar.addView(back,new LinearLayout.LayoutParams(dp(104),dp(52)));}
        TextView brand=text("EndraClean",home?22:20,R.color.gold);brand.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,dp(48),1);brand.setGravity(Gravity.CENTER);bar.addView(brand,bp);
        Button help=small("?",this::showHelp);bar.addView(help,new LinearLayout.LayoutParams(dp(58),dp(52)));root.addView(bar);
    }
    private void showHome(){
        systemPage=false; rows=null; clean=null; status=null;
        FrameLayout shell=new FrameLayout(this); shell.setBackgroundColor(android.graphics.Color.BLACK);

        ImageView art=new ImageView(this);
        art.setImageResource(R.drawable.endra_clean_home);
        art.setScaleType(ImageView.ScaleType.FIT_CENTER);
        art.setAdjustViewBounds(false);
        shell.addView(art,new FrameLayout.LayoutParams(-1,-1));

        View help=hotspot(this::showHelp);
        FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(dp(76),dp(76),Gravity.TOP|Gravity.RIGHT);
        hp.setMargins(0,dp(6),dp(4),0); shell.addView(help,hp);

        View user=hotspot(()->showApps(false));
        FrameLayout.LayoutParams up=new FrameLayout.LayoutParams(-1,dp(128),Gravity.CENTER_HORIZONTAL);
        up.leftMargin=dp(20);up.rightMargin=dp(20);up.topMargin=(int)(getResources().getDisplayMetrics().heightPixels*.56f);
        shell.addView(user,up);

        View system=hotspot(()->showApps(true));
        FrameLayout.LayoutParams sp=new FrameLayout.LayoutParams(-1,dp(128),Gravity.CENTER_HORIZONTAL);
        sp.leftMargin=dp(20);sp.rightMargin=dp(20);sp.topMargin=(int)(getResources().getDisplayMetrics().heightPixels*.70f);
        shell.addView(system,sp);

        setContentView(shell);
    }
    private View hotspot(Runnable action){
        View v=new View(this); v.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        v.setClickable(true); v.setFocusable(true); v.setOnClickListener(x->action.run()); return v;
    }
    private void showApps(boolean system){
        systemPage=system;base();header(false);
        if(system){ showSystemApps(); return; }

        LinearLayout hero=goldPanel();
        TextView title=text("👥  USER APPS",25,R.color.gold); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD); hero.addView(title);
        hero.addView(text("Scan and clear cache for apps you installed.",13,R.color.silver));
        LinearLayout.LayoutParams heroLp=new LinearLayout.LayoutParams(-1,-2); heroLp.setMargins(0,dp(12),0,dp(14)); root.addView(hero,heroLp);

        LinearLayout summary=goldPanel();
        TextView cacheTitle=text("CACHE SCAN",12,R.color.gold); cacheTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD); summary.addView(cacheTitle);
        status=text("Ready to scan installed apps.",14,R.color.silver); summary.addView(status);
        LinearLayout actions=new LinearLayout(this); actions.setGravity(Gravity.CENTER); actions.setOrientation(LinearLayout.HORIZONTAL);
        Button scan=compactButton("Scan Cache",true,()->{if(!hasUsageAccess())startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));else scanApps();});
        Button rescan=compactButton("Rescan",false,this::scanApps);
        Button all=compactButton("Select All",false,this::selectAll);
        actions.addView(scan,new LinearLayout.LayoutParams(0,dp(54),1));
        actions.addView(rescan,new LinearLayout.LayoutParams(0,dp(54),1));
        actions.addView(all,new LinearLayout.LayoutParams(0,dp(54),1));
        summary.addView(actions);
        root.addView(summary);

        rows=new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2); rp.setMargins(0,dp(14),0,dp(14)); root.addView(rows,rp);

        clean=button("🧹  Clean Selected Cache",true,this::startClean);
        root.addView(clean);
        scanApps();
    }
    private void showSystemApps(){
        LinearLayout hero=goldPanel();
        TextView title=text("⚙  SYSTEM APPS",25,R.color.gold); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD); hero.addView(title);
        hero.addView(text("Scan cache for Android system apps.",13,R.color.silver));
        LinearLayout.LayoutParams heroLp=new LinearLayout.LayoutParams(-1,-2); heroLp.setMargins(0,dp(12),0,dp(14)); root.addView(hero,heroLp);

        LinearLayout summary=goldPanel();
        TextView cacheTitle=text("CACHE SCAN",12,R.color.gold); cacheTitle.setTypeface(Typeface.DEFAULT,Typeface.BOLD); summary.addView(cacheTitle);
        status=text("System apps are protected by EndraClean.",14,R.color.silver); summary.addView(status);
        LinearLayout actions=new LinearLayout(this); actions.setGravity(Gravity.CENTER); actions.setOrientation(LinearLayout.HORIZONTAL);
        Button scan=compactButton("Scan Cache",true,()->{if(!hasUsageAccess())startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));else scanApps();});
        Button rescan=compactButton("Rescan",false,this::scanApps);
        Button all=compactButton("Select All",false,()->{if(status!=null)status.setText("System apps remain protected. Selection is disabled.");});
        actions.addView(scan,new LinearLayout.LayoutParams(0,dp(54),1));
        actions.addView(rescan,new LinearLayout.LayoutParams(0,dp(54),1));
        actions.addView(all,new LinearLayout.LayoutParams(0,dp(54),1));
        summary.addView(actions); root.addView(summary);

        rows=new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2); rp.setMargins(0,dp(14),0,dp(14)); root.addView(rows,rp);
        Button protectedButton=button("🛡  System Cache Protected",true,()->{if(status!=null)status.setText("EndraClean does not clear protected system-app cache.");});
        protectedButton.setEnabled(false); root.addView(protectedButton);
        scanApps();
    }
    private LinearLayout goldPanel(){
        LinearLayout p=new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL); p.setPadding(dp(16),dp(14),dp(16),dp(14));
        GradientDrawable g=new GradientDrawable(); g.setColor(getColor(R.color.surface)); g.setStroke(dp(2),getColor(R.color.outline)); g.setCornerRadius(dp(8)); p.setBackground(g); return p;
    }
    private Button compactButton(String label,boolean primary,Runnable action){
        Button b=button(label,primary,action); b.setTextSize(13); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(54),1); lp.setMargins(dp(3),dp(8),dp(3),0); b.setLayoutParams(lp); return b;
    }
    @Override protected void onResume(){super.onResume();if(rows!=null)scanApps();}
    private void scanApps(){
        if(rows==null)return; apps.clear();rows.removeAllViews(); if(systemPage)rows.addView(text("SYSTEM APPLICATIONS",12,R.color.gold));
        boolean usage=hasUsageAccess(); StorageStatsManager stats=(StorageStatsManager)getSystemService(STORAGE_STATS_SERVICE); PackageManager pm=getPackageManager();
        for(ApplicationInfo info:pm.getInstalledApplications(0)){
            boolean sys=(info.flags&(ApplicationInfo.FLAG_SYSTEM|ApplicationInfo.FLAG_UPDATED_SYSTEM_APP))!=0;
            if(systemPage!=sys)continue;
            if(!systemPage&&!CleanerAccessibilityService.eligible(this,info.packageName))continue;
            long bytes=-1;if(usage&&stats!=null)try{bytes=stats.queryStatsForPackage(StorageManager.UUID_DEFAULT,info.packageName,android.os.Process.myUserHandle()).getCacheBytes();}catch(Exception ignored){}
            AppEntry e=new AppEntry(info.packageName,pm.getApplicationLabel(info).toString(),bytes);apps.add(e);
        }
        Collections.sort(apps,(a,b)->a.label.compareToIgnoreCase(b.label));
        for(AppEntry e:apps){
            if(systemPage){
                LinearLayout row=goldPanel(); row.setGravity(Gravity.CENTER_VERTICAL); row.setOrientation(LinearLayout.HORIZONTAL);
                try{ImageView icon=new ImageView(this);icon.setImageDrawable(pm.getApplicationIcon(e.packageName));row.addView(icon,new LinearLayout.LayoutParams(dp(44),dp(44)));}catch(Exception ignored){}
                TextView label=text(e.label+(e.cacheBytes>=0?"\n"+Formatter.formatShortFileSize(this,e.cacheBytes):"\nCache size unavailable"),15,R.color.silver);
                LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(0,-2,1);llp.setMargins(dp(12),0,dp(8),0);row.addView(label,llp);
                TextView lock=text("🔒",18,R.color.gold); lock.setGravity(Gravity.CENTER); row.addView(lock,new LinearLayout.LayoutParams(dp(44),dp(44)));
                LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(-1,-2);rlp.setMargins(0,0,0,dp(6));rows.addView(row,rlp);
            }
            else{
                LinearLayout row=goldPanel(); row.setGravity(Gravity.CENTER_VERTICAL); row.setOrientation(LinearLayout.HORIZONTAL);
                try{ImageView icon=new ImageView(this);icon.setImageDrawable(pm.getApplicationIcon(e.packageName));row.addView(icon,new LinearLayout.LayoutParams(dp(44),dp(44)));}catch(Exception ignored){}
                TextView label=text(e.label+(e.cacheBytes>=0?"\n"+Formatter.formatShortFileSize(this,e.cacheBytes):"\nCache size unavailable"),15,R.color.silver);
                LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(0,-2,1);llp.setMargins(dp(12),0,dp(8),0);row.addView(label,llp);
                CheckBox cb=new CheckBox(this);cb.setButtonTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.gold)));cb.setOnCheckedChangeListener((x,on)->e.selected=on);row.addView(cb);
                LinearLayout.LayoutParams rlp=new LinearLayout.LayoutParams(-1,-2);rlp.setMargins(0,0,0,dp(8));rows.addView(row,rlp);
            }
        }
        if(status!=null){
            long total=0; for(AppEntry e:apps)if(e.cacheBytes>0)total+=e.cacheBytes;
            status.setText(systemPage?apps.size()+" protected system apps found. Viewing only.":"Last Scan: just now\nTotal Cache Found: "+(usage?Formatter.formatShortFileSize(this,total):"Usage access required"));
        }
        if(clean!=null)clean.setEnabled(CleanerAccessibilityService.connected());
    }
    private void selectAll(){
        for(AppEntry e:apps)e.selected=true;
        for(int i=0;i<rows.getChildCount();i++)checkAll(rows.getChildAt(i));
    }
    private void checkAll(View v){
        if(v instanceof CheckBox){((CheckBox)v).setChecked(true);return;}
        if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)checkAll(g.getChildAt(i));}
    }
    private boolean hasUsageAccess(){AppOpsManager o=(AppOpsManager)getSystemService(APP_OPS_SERVICE);return o!=null&&o.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),getPackageName())==AppOpsManager.MODE_ALLOWED;}
    private void startClean(){
        ArrayList<String>s=new ArrayList<>();for(AppEntry e:apps)if(e.selected&&CleanerAccessibilityService.eligible(this,e.packageName))s.add(e.packageName);
        if(s.isEmpty()){status.setText("Select at least one user app.");return;}if(!CleanerAccessibilityService.connected()){status.setText("Enable the cleaner service first.");return;}
        Intent i=new Intent(this,CleanSessionService.class).setAction(CleanSessionService.START);i.putStringArrayListExtra(CleanSessionService.PACKAGES,s);
        try{startForegroundService(i);status.setText("Cleaning "+s.size()+" selected user apps. Watch the notification for progress.");}catch(RuntimeException ex){status.setText("Could not start cleaning: "+ex.getMessage());}
    }
    private void showHelp(){new AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert).setTitle("✦  EndraClean Help").setMessage("USER APPS\nApps you installed. EndraClean can guide Android Settings to clear only their cache. App data and storage are never cleared.\n\nSYSTEM APPS\nApps built into Android or supplied by the device maker. EndraClean lists them separately so you can identify them, but the existing safety engine blocks cleaning system apps.\n\nUse Grant usage access to display cache sizes and Enable cleaner service to run a user-started cache cleaning pass.").setPositiveButton("Got it",null).show();}
    private LinearLayout navButton(String a,String b,Runnable r){LinearLayout p=panel();p.setMinimumHeight(dp(112));p.setGravity(Gravity.CENTER_VERTICAL);p.setClickable(true);p.setFocusable(true);p.setOnClickListener(v->r.run());TextView t=text(a,23,R.color.gold);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);p.addView(t);p.addView(text(b,13,R.color.muted));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(7),0,dp(7));p.setLayoutParams(lp);return p;}
    private LinearLayout panel(){LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(dp(16),dp(14),dp(16),dp(14));p.setBackgroundResource(R.drawable.panel);return p;}
    private TextView text(String s,int z,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(getColor(c));v.setPadding(0,dp(5),0,dp(5));return v;}
    private Button small(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setTextColor(getColor(R.color.gold));b.setTextSize(20);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackgroundResource(R.drawable.tech_button);b.setBackgroundTintList(null);b.setOnClickListener(v->r.run());return b;}
    private Button button(String s,boolean primary,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(getColor(primary?R.color.navy:R.color.silver));b.setTextSize(16);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackgroundResource(primary?R.drawable.tech_primary:R.drawable.tech_button);b.setBackgroundTintList(null);b.setMinHeight(dp(58));b.setOnClickListener(v->r.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.topMargin=dp(9);b.setLayoutParams(lp);return b;}
    private int dp(int v){return(int)(v*getResources().getDisplayMetrics().density+.5f);}
}