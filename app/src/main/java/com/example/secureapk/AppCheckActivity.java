package com.example.secureapk;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AppCheckActivity extends AppCompatActivity {

    private EditText appNameEditText;
    private Button checkAppButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_check);

        appNameEditText = findViewById(R.id.appNameEditText);
        checkAppButton = findViewById(R.id.checkAppButton);

        checkAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appName = appNameEditText.getText().toString().trim();
                if (!appName.isEmpty()) {
                    // Check if the app is malicious (dummy logic)
                    boolean isMalicious = checkIfAppIsMalicious(appName);

                    // Check internet connectivity
                    boolean isInternetAvailable = isInternetConnected();

                    // Show the result in an AlertDialog
                    showResult(isMalicious, isInternetAvailable);
                } else {
                    showAlert("Error", "Please enter an app name.");
                }
            }
        });
    }

    // Dummy logic to check if the app is malicious
    private boolean checkIfAppIsMalicious(String appName) {
        // Replace with your own logic
        return appName.toLowerCase().contains("malicious");
    }

    // Check internet connectivity
    private boolean isInternetConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Show the result in an AlertDialog
    private void showResult(boolean isMalicious, boolean isInternetAvailable) {
        String title = "App Check Result";
        String message;

        if (!isInternetAvailable) {
            message = "No internet connection. Please check your network settings.";
        } else {
            message = isMalicious ? "The given app is malicious! Please do not install!" : "The app is not malicious, you can install it.";
        }

        showAlert(title, message);
    }

    // Utility method to show an AlertDialog
    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}