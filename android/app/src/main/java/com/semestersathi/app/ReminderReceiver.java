package com.semestersathi.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    static final String CHANNEL_ID = "study_reminders";
    private static final String PREFS = "study_reminders";
    private static final String TIME_PREFIX = "at:";
    private static final String TITLE_PREFIX = "title:";
    private static final String EXTRA_ID = "reminder_id";
    private static final String EXTRA_TITLE = "reminder_title";

    @Override
    public void onReceive(Context context, Intent intent) {
        ensureChannel(context);
        String id = intent.getStringExtra(EXTRA_ID);
        String title = intent.getStringExtra(EXTRA_TITLE);
        if (id == null || title == null) return;

        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId(id), openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);
        Notification notification = builder
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Time to study")
                .setContentText(title)
                .setStyle(new Notification.BigTextStyle().bigText(title))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_REMINDER)
                .build();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            try { manager.notify(notificationId(id), notification); }
            catch (SecurityException ignored) { }
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.remove(TIME_PREFIX + id).remove(TITLE_PREFIX + id).apply();
    }

    static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Study reminders", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Reminders for scheduled study tasks and exam preparation.");
            manager.createNotificationChannel(channel);
        }
    }

    static void schedule(Context context, String id, String title, long triggerAt) {
        ensureChannel(context);
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.putLong(TIME_PREFIX + id, triggerAt).putString(TITLE_PREFIX + id, title).apply();
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        PendingIntent operation = alarmIntent(context, id, title);
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation);
    }

    static void cancel(Context context, String id) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            PendingIntent operation = alarmIntent(context, id, "");
            manager.cancel(operation);
            operation.cancel();
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        editor.remove(TIME_PREFIX + id).remove(TITLE_PREFIX + id).apply();
    }

    static void restore(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) return;
        for (String key : prefs.getAll().keySet()) {
            if (!key.startsWith(TIME_PREFIX)) continue;
            String id = key.substring(TIME_PREFIX.length());
            long triggerAt = prefs.getLong(key, 0L);
            String title = prefs.getString(TITLE_PREFIX + id, null);
            if (title == null || triggerAt <= System.currentTimeMillis()) continue;
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, alarmIntent(context, id, title));
        }
    }

    private static PendingIntent alarmIntent(Context context, String id, String title) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(context.getPackageName() + ".STUDY_REMINDER." + id);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_TITLE, title);
        return PendingIntent.getBroadcast(context, notificationId(id), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static int notificationId(String id) {
        return id.hashCode() & 0x7fffffff;
    }
}
