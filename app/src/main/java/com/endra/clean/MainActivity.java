package com.endra.clean;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** User-app cache list and explicit controls for a user-started cleaning pass. */
public final class MainActivity extends Activity {
    private final List<AppEntry> apps = new ArrayList<>();
    private LinearLayout rows;
    private TextView status;
    private Button clean;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(getColor(R.color.navy));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(10), dp(16), dp(28));
        scroll.addView(root);
        setContentView(scroll);
        HydraArtworkView art = new HydraArtworkView(this);
        root.addView(art, new LinearLayout.LayoutParams(-1, -2));
        TextView title = text("ENDRA CLEAN", 27, R.color.silver);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);
        root.addView(text("HYDRA CACHE ENGINE  /  USER APPS", 12, R.color.gold));
        View line = new View(this); line.setBackgroundResource(R.drawable.accent_line);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(-1, dp(3));
        lineParams.setMargins(0, dp(12), 0, dp(20)); root.addView(line, lineParams);
        LinearLayout controls = panel(); root.addView(controls);
        controls.addView(text("CACHE CONTROL", 12, R.color.muted));
        status = text("Select user-installed apps to clear their cache.", 15, R.color.silver);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        controls.addView(status);
        controls.addView(button("Grant usage access", false, () -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))));
        controls.addView(button("Enable cleaner service", false, () -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
        controls.addView(button("Rescan user apps", false, this::scanApps));
        clean = button("CLEAN SELECTED CACHE", true, this::startClean);
        controls.addView(clean);
        rows = panel();
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
        rowParams.topMargin = dp(16); root.addView(rows, rowParams);
        rows.addView(text("USER APPLICATIONS", 12, R.color.gold));
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    @Override protected void onResume() { super.onResume(); scanApps(); }

    private void scanApps() {
        if (rows == null) return;
        apps.clear(); rows.removeAllViews();
        rows.addView(text("USER APPLICATIONS", 12, R.color.gold));
        boolean usage = hasUsageAccess();
        StorageStatsManager stats = (StorageStatsManager) getSystemService(STORAGE_STATS_SERVICE);
        PackageManager pm = getPackageManager();
        for (ApplicationInfo info : pm.getInstalledApplications(0)) {
            if (!CleanerAccessibilityService.eligible(this, info.packageName)) continue;
            long bytes = -1;
            if (usage && stats != null) {
                try { bytes = stats.queryStatsForPackage(StorageManager.UUID_DEFAULT, info.packageName, Process.myUserHandle()).getCacheBytes(); }
                catch (Exception ignored) { /* Some apps do not expose storage statistics. */ }
            }
            apps.add(new AppEntry(info.packageName, pm.getApplicationLabel(info).toString(), bytes));
        }
        Collections.sort(apps, (a, b) -> a.label.compareToIgnoreCase(b.label));
        for (AppEntry entry : apps) {
            CheckBox box = new CheckBox(this);
            box.setText(entry.label + (entry.cacheBytes >= 0 ? "  ·  " + Formatter.formatShortFileSize(this, entry.cacheBytes) : ""));
            box.setContentDescription(entry.label + ", " + entry.packageName + ", " + (entry.cacheBytes >= 0 ? Formatter.formatShortFileSize(this, entry.cacheBytes) : "cache size unavailable"));
            box.setTextColor(getColor(R.color.silver));
            box.setButtonTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.gold)));
            box.setPadding(dp(6), dp(8), dp(6), dp(8));
            box.setOnCheckedChangeListener((b, selected) -> entry.selected = selected);
            rows.addView(box);
        }
        status.setText(apps.size() + " user apps found. " + (usage ? "Cache sizes shown where available." : "Grant usage access to show cache sizes."));
        clean.setEnabled(CleanerAccessibilityService.connected());
    }

    private boolean hasUsageAccess() {
        AppOpsManager ops = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        return ops != null && ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName()) == AppOpsManager.MODE_ALLOWED;
    }

    private void startClean() {
        ArrayList<String> selected = new ArrayList<>();
        for (AppEntry entry : apps) if (entry.selected && CleanerAccessibilityService.eligible(this, entry.packageName)) selected.add(entry.packageName);
        if (selected.isEmpty()) { status.setText("Select at least one user app."); return; }
        if (!CleanerAccessibilityService.connected()) { status.setText("Enable the cleaner service first."); return; }
        Intent start = new Intent(this, CleanSessionService.class).setAction(CleanSessionService.START);
        start.putStringArrayListExtra(CleanSessionService.PACKAGES, selected);
        try { startForegroundService(start); status.setText("Cleaning " + selected.size() + " selected user apps. Watch the notification for progress."); }
        catch (RuntimeException ex) { status.setText("Could not start cleaning: " + ex.getMessage()); }
    }

    private LinearLayout panel() {
        LinearLayout p = new LinearLayout(this); p.setOrientation(LinearLayout.VERTICAL);
        p.setPadding(dp(16), dp(16), dp(16), dp(16)); p.setBackgroundResource(R.drawable.panel);
        return p;
    }
    private TextView text(String value, int sp, int color) {
        TextView v = new TextView(this); v.setText(value); v.setTextSize(sp); v.setTextColor(getColor(color));
        v.setPadding(0, dp(5), 0, dp(5)); return v;
    }
    private Button button(String label, boolean primary, Runnable click) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false);
        b.setTextColor(getColor(primary ? R.color.navy : R.color.silver));
        b.setTextSize(16); b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackgroundResource(primary ? R.drawable.tech_primary : R.drawable.tech_button);
        b.setBackgroundTintList(null);
        b.setMinHeight(dp(60)); b.setOnClickListener(v -> click.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(60)); lp.topMargin = dp(10); b.setLayoutParams(lp);
        return b;
    }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
}
