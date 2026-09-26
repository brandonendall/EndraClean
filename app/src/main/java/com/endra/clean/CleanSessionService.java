package com.endra.clean;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import java.util.ArrayList;

/** Keeps a user-started cache-only session visible while Android Settings is in front. */
public final class CleanSessionService extends Service {
    static final String START = "com.endra.clean.START";
    static final String STOP = "com.endra.clean.STOP";
    static final String PROGRESS = "com.endra.clean.PROGRESS";
    static final String DONE = "com.endra.clean.DONE";
    static final String PACKAGES = "packages";
    static final String CURRENT = "current";
    static final String STATUS = "status";
    static final String POSITION = "position";
    static final String TOTAL = "total";
    private static final String CHANNEL = "clean_session";
    private boolean running;

    @Override public void onCreate() {
        super.onCreate();
        NotificationChannel channel = new NotificationChannel(CHANNEL, "Hydra Cache Engine", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Progress for cleaning sessions you start");
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (START.equals(action)) {
            ArrayList<String> packages = intent.getStringArrayListExtra(PACKAGES);
            if (packages == null || packages.isEmpty() || !CleanerAccessibilityService.connected()) { stopSelf(); return START_NOT_STICKY; }
            if (running) return START_NOT_STICKY;
            running = true;
            startForeground(1, notification("Starting user-app cache cleaning", 0, packages.size()));
            CleanerAccessibilityService.start(packages);
        } else if (STOP.equals(action) || DONE.equals(action)) {
            CleanerAccessibilityService.stopCurrent();
            running = false;
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelf();
        } else if (PROGRESS.equals(action) && running) {
            String label = intent.getStringExtra(CURRENT);
            String state = intent.getStringExtra(STATUS);
            int current = intent.getIntExtra(POSITION, 0);
            int total = intent.getIntExtra(TOTAL, 0);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(1,
                notification(state + ": " + label, current, total));
        }
        return START_NOT_STICKY;
    }

    private Notification notification(String message, int current, int total) {
        int immutable = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent open = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), immutable);
        PendingIntent stop = PendingIntent.getService(this, 1,
            new Intent(this, CleanSessionService.class).setAction(STOP), immutable);
        return new Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("EndraClean · Hydra Cache Engine")
            .setContentText(message)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(total, current, total == 0)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", stop)
            .build();
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
