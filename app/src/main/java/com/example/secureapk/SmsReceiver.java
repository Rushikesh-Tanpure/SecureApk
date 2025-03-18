package com.example.secureapk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive triggered");

        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Log.d(TAG, "SMS Received Intent Detected");

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                String format = bundle.getString("format");

                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
                        } else {
                            smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        }

                        String messageBody = smsMessage.getMessageBody();
                        String sender = smsMessage.getOriginatingAddress();

                        Log.d(TAG, "Received SMS from: " + sender);
                        Log.d(TAG, "Message content: " + messageBody);

                        List<String> links = extractLinks(messageBody);
                        if (!links.isEmpty()) {
                            for (String link : links) {
                                Log.d(TAG, "Extracted Link: " + link);
                                String safetyStatus = checkLinkSafety(link);

                                if ("Malicious".equals(safetyStatus)) {
                                    Log.d(TAG, "Malicious link detected: " + link);
                                    showNotification(context, sender, link);
                                } else {
                                    Log.d(TAG, "Safe link detected: " + link);
                                }
                            }
                        } else {
                            Log.d(TAG, "No links detected in SMS.");
                        }
                    }
                }
            }
        }
    }

    public static List<String> extractLinks(String text) {
        Pattern urlPattern = Pattern.compile("(https?://[a-zA-Z0-9\\-.]+(:\\d+)?(/[\\w\\-.\\/?%&=]*)?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = urlPattern.matcher(text);

        List<String> links = new ArrayList<>();
        while (matcher.find()) {
            links.add(matcher.group(1));
        }
        return links;
    }

    public static String checkLinkSafety(String link) {
        String[] maliciousDomains = {"examplemaliciousurl.com", "malicious.com", "phishing.com", "dangerous.net", "malicious-site.com"};

        // Extract the domain from the link
        String domain = extractDomain(link);

        if (domain != null) {
            for (String maliciousDomain : maliciousDomains) {
                if (domain.equals(maliciousDomain) || domain.endsWith("." + maliciousDomain)) {
                    return "Malicious";
                }
            }
        }

        return "Safe";
    }

    public static String extractDomain(String link) {
        // Use a regex to extract the domain from the link
        Pattern domainPattern = Pattern.compile("https?://([a-zA-Z0-9\\-.]+)(?:/|$)");
        Matcher matcher = domainPattern.matcher(link);

        if (matcher.find()) {
            return matcher.group(1); // Return the domain
        }

        return null; // No domain found
    }

    public static void showNotification(Context context, String sender, String link) {
        String channelId = "security_alert_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Security Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifies when a malicious link is detected.");

            // Set the custom sound for the notification channel
            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert_sound);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        int notificationId = link.hashCode();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Security Alert: Malicious Link Detected!")
                .setContentText("Sender: " + sender + "\nMalicious link: " + link)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Sender: " + sender + "\nMalicious link: " + link))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Set the custom sound for the notification (for devices running Android 7.1 and below)
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.alert_sound);
        builder.setSound(soundUri);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}