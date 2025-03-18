package com.example.secureapk;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebsiteCheckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website_check);

        EditText etUrl = findViewById(R.id.websiteEditText);
        Button btnCheckUrl = findViewById(R.id.checkWebsiteButton);

        btnCheckUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = etUrl.getText().toString().trim();

                // Check if the URL starts with http or https
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    // If HTTP or HTTPS, check with VirusTotal API
                    new VirusTotalTask().execute(url);
                } else {
                    // If the URL doesn't start with http or https, assume it's invalid
                    showResultPopup("Invalid URL. Please enter a valid URL starting with http:// or https://.");
                }
            }
        });
    }

    private class VirusTotalTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String apiKey = "6b438dbc90208d58c0720f43a3ca235047e981697b938b2793ac9ad44a1b7d8f"; // Replace with your VirusTotal API key
            String apiUrl = "https://www.virustotal.com/vtapi/v2/url/report?apikey=" + apiKey + "&resource=" + url;

            try {
                URL apiEndpoint = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) apiEndpoint.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    int responseCode = jsonResponse.getInt("response_code");

                    if (responseCode == 1) {
                        int positives = jsonResponse.getInt("positives"); // Number of engines that detected the URL as malicious
                        if (positives > 0) {
                            showResultPopup("Warning: This URL is flagged as malicious by " + positives + " security engines. Do not visit!");
                        } else {
                            showResultPopup("This URL is safe. No security engines flagged it as malicious.");
                        }
                    } else {
                        showResultPopup("This URL is not found in the VirusTotal database. Proceed with caution.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showResultPopup("Error parsing the response from VirusTotal.");
                }
            } else {
                showResultPopup("Error: Unable to scan the URL. Please check your internet connection or try again later.");
            }
        }
    }

    private void showResultPopup(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Scan Result")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}