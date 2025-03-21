package com.example.secureapk;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class SecureApkApp extends Application {

    public static final String CHANNEL_ID = "malicious_url_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Malicious URL Detection",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for detected malicious URLs in SMS");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}