package com.example.secureapk;

import static android.app.ProgressDialog.show;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_SMS_PERMISSION = 123;
    private SmsReceiver smsReceiver;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_TRIAL_START = "trial_start_date";
    private static final String KEY_IS_SUBSCRIBED = "is_subscribed";
    Button showButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //startActivity(new Intent(MainActivity.this,SubscriptionActivity.class));

        View.OnClickListener bt=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button b=findViewById(view.getId());
                b.setVisibility(INVISIBLE);
            }
        };

        showButton = findViewById(R.id.showButton);
        initiateTrialIfNeeded();
        showButton.setOnClickListener(v -> checkSubscription());
        Button appCheckButton = findViewById(R.id.appCheckButton);
        Button websiteCheckButton = findViewById(R.id.websiteCheckButton);

        appCheckButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AppCheckActivity.class)));
        websiteCheckButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WebsiteCheckActivity.class)));

        requestPermissions();
    }
    private void checkSubscription() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isSubscribed = prefs.getBoolean(KEY_IS_SUBSCRIBED, false);
        String trialStartDate = prefs.getString(KEY_TRIAL_START, "");
        String subscriptionStartDate = prefs.getString("subscription_start_date", "");
        int days=prefs.getInt("subscription_days",0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date currentDate = new Date();

        if (isSubscribed && !subscriptionStartDate.isEmpty()) {
            try {
                Date subStartDate = sdf.parse(subscriptionStartDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(subStartDate);
                calendar.add(Calendar.DAY_OF_YEAR, days); // Assuming 30-day subscription
                Date subscriptionEndDate = calendar.getTime();

                if (currentDate.before(subscriptionEndDate)) {
                    long remainingDays = (subscriptionEndDate.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24);
                    showAlert("Subscription Active", "You are subscribed. " + remainingDays + " days remaining.");
                } else {
                    showAlert("Subscription Expired", "Your subscription has ended. Please renew.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        if (!trialStartDate.isEmpty()) {
            try {
                Date startDate = sdf.parse(trialStartDate);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);
                calendar.add(Calendar.DAY_OF_YEAR, 7); // Fix: Corrected to 7 days
                Date trialEndDate = calendar.getTime();

                if (currentDate.after(trialEndDate)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Trial Expired")
                            .setMessage("Your free trial has ended. Please subscribe to continue using the app.")
                            .setPositiveButton("Subscribe Now", (dialog, which) -> {
                                startActivity(new Intent(MainActivity.this, SubscriptionActivity.class));
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    long remainingDays = (trialEndDate.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24);
                    showAlert("Trial Active", "Your free trial is active. " + remainingDays + " days remaining.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void markAsSubscribed() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        prefs.edit().putBoolean(KEY_IS_SUBSCRIBED, true)
                .putString("subscription_start_date", currentDate)  // Tracks subscription start
                .apply();

        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("Subscription Activated")
                    .setMessage("Thank you! Your subscription has been activated successfully.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void initiateTrialIfNeeded() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.contains(KEY_TRIAL_START)) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            prefs.edit().putString(KEY_TRIAL_START, currentDate).apply();

            new AlertDialog.Builder(this)
                    .setTitle("Free Trial Started")
                    .setMessage("Your 7-day free trial has started!")
                    .setPositiveButton("OK", null)
                    .show();
        } else {
            checkSubscription();
        }
    }


    private void requestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_SMS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_SMS_PERMISSION);
        } else {
            registerSmsReceiver();  // Register receiver if permissions are already granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                registerSmsReceiver();
            } else {
                new AlertDialog.Builder(this)
                        .setMessage("SMS and notification permissions are required for full functionality.")
                        .setPositiveButton("OK", (dialog, which) -> requestPermissions())
                        .show();
            }
        }
    }

    private void registerSmsReceiver() {
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
        smsReceiver = new SmsReceiver();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
    }
//    private void initiateTrialIfNeeded() {
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        if (!prefs.contains(KEY_TRIAL_START)) {
//            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//            prefs.edit().putString(KEY_TRIAL_START, currentDate).apply();
//            Toast.makeText(this, "Your 7-day free trial has started!", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    private void checkSubscription() {
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        boolean isSubscribed = prefs.getBoolean(KEY_IS_SUBSCRIBED, false);
//        String trialStartDate = prefs.getString(KEY_TRIAL_START, "");
//
//        if (isSubscribed) {
//            Toast.makeText(this, "You are subscribed. Enjoy the app!", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        if (!trialStartDate.isEmpty()) {
//            try {
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
//                Date startDate = sdf.parse(trialStartDate);
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(startDate);
//                calendar.add(Calendar.DAY_OF_YEAR, 7);
//                Date trialEndDate = calendar.getTime();
//                Date currentDate = new Date();
//
//                if (currentDate.after(trialEndDate)) {
//                    Toast.makeText(this, "Your free trial has ended. Please subscribe.", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(this, "Your free trial is active. Enjoy!", Toast.LENGTH_LONG).show();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void markAsSubscribed() {
//        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//        prefs.edit().putBoolean(KEY_IS_SUBSCRIBED, true).apply();
//        Toast.makeText(this, "Subscription activated successfully!", Toast.LENGTH_LONG).show();
//    }
}