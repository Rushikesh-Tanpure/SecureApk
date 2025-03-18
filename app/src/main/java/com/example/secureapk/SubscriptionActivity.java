package com.example.secureapk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubscriptionActivity extends AppCompatActivity {

    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    int subscriptiondays=0;
    private String publishableKey = "pk_test_51QfekhK1VCEQa4bkR1PtXBfG9B0tjbjLwuYNajHIqtZ5ZlwRlQt0uIix3xmZudrHvQyCQ2RsFw9RE5XtbPGdFndm009ynozYeK"; // Replace with your Stripe Publishable Key
    private String backendUrl = "https://stripe-server-lgvf.onrender.com/create-payment-intent"; // Replace with your backend URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        PaymentConfiguration.init(this, publishableKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        Button monthlyBtn = findViewById(R.id.monthlySubscription);
        Button sixMonthBtn = findViewById(R.id.sixMonthSubscription);
        Button yearlyBtn = findViewById(R.id.yearlySubscription);

        monthlyBtn.setOnClickListener(v -> startPayment(20000, "Monthly Subscription",30));
        sixMonthBtn.setOnClickListener(v -> startPayment(100000, "6-Month Subscription",180));
        yearlyBtn.setOnClickListener(v -> startPayment(150000, "Yearly Subscription",365));
    }

    private void startPayment(int amount, String description,int days) {
        // Send request to backend server to get PaymentIntent
        subscriptiondays=days;
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("amount", amount);
            jsonObject.put("currency", "INR");
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(backendUrl)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(SubscriptionActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject responseObject = new JSONObject(response.body().string());
                        paymentIntentClientSecret = responseObject.getString("clientSecret");

                        runOnUiThread(() -> {
                            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration("Secure APK");
                            paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Save subscription status in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("is_subscribed", true).apply();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String currentDate = sdf.format(new Date());  // Corrected: Format the current date
            prefs.edit().putString("subscription_start_date", currentDate).apply();  // Fixed this line
            prefs.edit().putInt("subscription_days", subscriptiondays).apply();



            // Show success alert and navigate to MainActivity
            new AlertDialog.Builder(this)
                    .setTitle("Subscription Activated")
                    .setMessage("Thank you! Your subscription has been activated successfully.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        Intent intent = new Intent(SubscriptionActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Close the SubscriptionActivity
                    })
                    .setCancelable(false) // Prevent dismissing without confirmation
                    .show();

        } else {
            // Show failure alert
            new AlertDialog.Builder(this)
                    .setTitle("Payment Failed")
                    .setMessage("Sorry, the payment was unsuccessful. Please try again.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

}
