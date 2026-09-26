package com.endra.clean;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;

/** User-triggered Settings automation. The only destructive control ever clicked is exact Clear cache. */
public final class CleanerAccessibilityService extends AccessibilityService {
    private static CleanerAccessibilityService instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<String> queue = new ArrayList<>();
    private int index;
    private long stepStarted;
    private int stage; // 0 idle, 1 app info, 2 storage, 3 cache click pending
    private boolean clicked;
    private Runnable timeout;

    static boolean connected() { return instance != null; }

    /** Defense in depth: system, updated system, and the cleaner itself are never eligible. */
    static boolean eligible(Context context, String packageName) {
        if (packageName == null || packageName.equals(context.getPackageName()) || packageName.equals("com.android.settings")) return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return (info.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0;
        } catch (PackageManager.NameNotFoundException ex) { return false; }
    }

    @Override protected void onServiceConnected() { super.onServiceConnected(); instance = this; }
    @Override public void onDestroy() { stop(); if (instance == this) instance = null; super.onDestroy(); }
    @Override public void onInterrupt() {
        stop();
        getApplicationContext().startService(new Intent(this, CleanSessionService.class).setAction(CleanSessionService.DONE));
    }

    static void start(List<String> packages) {
        if (instance != null) instance.begin(packages);
    }
    static void stopCurrent() { if (instance != null) instance.stop(); }

    private void begin(List<String> packages) {
        stop();
        for (String name : packages) if (eligible(this, name) && !queue.contains(name)) queue.add(name);
        index = 0;
        next();
    }

    private void stop() {
        queue.clear(); stage = 0; clicked = false;
        if (timeout != null) handler.removeCallbacks(timeout);
        timeout = null;
    }

    private void next() {
        if (timeout != null) handler.removeCallbacks(timeout);
        if (index >= queue.size()) {
            stop();
            getApplicationContext().startService(new Intent(this, CleanSessionService.class).setAction(CleanSessionService.DONE));
            return;
        }
        String current = queue.get(index);
        if (!eligible(this, current)) { report("Skipped system or missing app", current); advance(); return; }
        stage = 1; clicked = false; stepStarted = System.currentTimeMillis();
        report("Opening app settings", current);
        Intent page = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + current));
        page.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try { startActivity(page); }
        catch (RuntimeException ex) { report("Skipped: settings unavailable", current); advance(); return; }
        final String expected = current;
        timeout = () -> {
            if (stage != 0 && index < queue.size() && queue.get(index).equals(expected)) {
                report("Skipped: settings timed out", expected); advance();
            }
        };
        handler.postDelayed(timeout, 12000);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (stage == 0 || index >= queue.size() || event.getPackageName() == null) return;
        // Only inspect Android Settings. Never act on UI belonging to another app.
        String source = event.getPackageName().toString();
        if (!source.equals("com.android.settings") && !source.equals("com.samsung.android.settings")) return;
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        try {
            if (stage == 1) {
                // Wait until the app-info page actually shows the selected app's name.
                String label;
                try { label = getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(queue.get(index), 0)).toString(); }
                catch (PackageManager.NameNotFoundException ex) { advance(); return; }
                if (exact(root, label) == null) return;
                AccessibilityNodeInfo storage = exact(root, "Storage", "Storage & cache");
                if (storage != null && clickAncestor(storage)) { stage = 2; report("Opening storage", queue.get(index)); }
            } else if (stage == 2) {
                AccessibilityNodeInfo clear = exact(root, "Clear cache");
                if (clear != null && !clicked && clear.isEnabled()) {
                    // No substring matching: "Clear data" and "Clear storage" can never pass this test.
                    clicked = true; stage = 3;
                    if (clickAncestor(clear)) {
                        report("Clear cache tapped", queue.get(index));
                        handler.postDelayed(this::advance, 1300);
                    } else { report("Skipped: button unavailable", queue.get(index)); advance(); }
                }
            }
        } finally { root.recycle(); }
    }

    private AccessibilityNodeInfo exact(AccessibilityNodeInfo node, String... labels) {
        CharSequence title = node.getText();
        CharSequence description = node.getContentDescription();
        for (String label : labels) {
            if (title != null && label.equalsIgnoreCase(title.toString().trim())) return node;
            if (description != null && label.equalsIgnoreCase(description.toString().trim())) return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            AccessibilityNodeInfo match = exact(child, labels);
            if (match != null) return match;
        }
        return null;
    }

    private boolean clickAncestor(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo current = node;
        for (int i = 0; current != null && i < 5; i++, current = current.getParent()) {
            if (current.isClickable() && current.isEnabled()) return current.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        return false;
    }

    private void advance() { if (stage == 0) return; index++; next(); }
    private void report(String message, String name) {
        Intent update = new Intent(this, CleanSessionService.class).setAction(CleanSessionService.PROGRESS);
        update.putExtra(CleanSessionService.STATUS, message);
        update.putExtra(CleanSessionService.CURRENT, name);
        update.putExtra(CleanSessionService.POSITION, index + 1);
        update.putExtra(CleanSessionService.TOTAL, queue.size());
        getApplicationContext().startService(update);
    }
}
