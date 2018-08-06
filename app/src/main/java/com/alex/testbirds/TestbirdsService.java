package com.alex.testbirds;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class TestbirdsService extends IntentService {
    private static final int ID = 100;
    private static final int TIMEOUT = 1;
    private static final String USAGESTATS = "usagestats";
    public static volatile boolean shouldContinue = true;

    public TestbirdsService() {
        super("testbirds");
        shouldContinue = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        startForeground(ID, getNotification());
        while (shouldContinue) {
            try {
                TimeUnit.SECONDS.sleep(TIMEOUT);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
            Set<String> selectedApps = sharedPreferences.getStringSet(Constants.APPS_LIST, new HashSet<>());
            String topPackageName = getTopApplication();
            if (selectedApps.contains(topPackageName)) {
                Intent activityIntent = new Intent(this, MainActivity.class);
                activityIntent.putExtra(Constants.BLOCKED, true);
                activityIntent.putExtra(Constants.APP_NAME, topPackageName);
                startActivity(activityIntent);
            }
        }
        stopForeground(true);
        stopSelf();
    }

    @SuppressLint("WrongConstant")
    @Nullable
    private String getTopApplication() {
        String topPackageName = null;
        UsageStatsManager usage = (UsageStatsManager) getSystemService(USAGESTATS);
        if (usage == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();
        List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
        if (stats != null) {
            SortedMap<Long, UsageStats> runningTask = new TreeMap<>();
            for (UsageStats usageStats : stats) {
                runningTask.put(usageStats.getLastTimeUsed(), usageStats);
            }
            topPackageName =  runningTask.get(runningTask.lastKey()).getPackageName();
        }
        return topPackageName;
    }

    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, getString(R.string.service_is_stopped), Toast.LENGTH_LONG).show();
    }

    private Notification getNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        NotificationCompat.Builder foregroundNotification = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID);
        foregroundNotification.setOngoing(true);
        foregroundNotification.setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_message))
                .setSmallIcon(android.R.drawable.ic_notification_overlay)
                .setContentIntent(pendingIntent);

        return foregroundNotification.build();
    }
}
