package com.example.aihealthassistant;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.*;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private EditText input;
    private Button button;
    private TextView result;
    private ProgressBar loadingBar;
    private View resultCard;
    private TextView diseaseHeader;

    // 🔑 PUT YOUR REAL GEMINI API KEY HERE
    private String API_KEY = "AIzaSyA95rhgLbbwcAFYbynmVpYBawaN0l3CfqQ";

    // Load native library
    static {
        System.loadLibrary("native-lib");
    }

    // Native function declaration
    public native int[] processSymptoms(int[] input);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        input = findViewById(R.id.symptomsInput);
        button = findViewById(R.id.predictBtn);
        result = findViewById(R.id.resultText);
        loadingBar = findViewById(R.id.loadingBar);
        resultCard = findViewById(R.id.resultCard);
        diseaseHeader = findViewById(R.id.diseaseHeader);

        button.setOnClickListener(v -> {
            String userInput = input.getText().toString().trim();

            if (userInput.isEmpty()) {
                Toast.makeText(this, "⚠ Please enter symptoms", Toast.LENGTH_SHORT).show();
                return;
            }

            button.setEnabled(false);
            loadingBar.setVisibility(View.VISIBLE);
            resultCard.setVisibility(View.GONE);

            new Thread(() -> {
                try {
                    // 🔹 STEP 0: NATIVE PROCESSING
                    String[] parts = userInput.split(",");
                    int[] symptomArray = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        try {
                            symptomArray[i] = Integer.parseInt(parts[i].trim());
                        } catch (NumberFormatException e) {
                            symptomArray[i] = 0;
                        }
                    }

                    // Call Native C++ function to validate/clean data
                    int[] processedArray = processSymptoms(symptomArray);
                    
                    // Convert back to string for backend
                    String processedInput = Arrays.stream(processedArray)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(","));

                    // 🔹 STEP 1: CALL YOUR ACTUAL LOCAL BACKEND
                    String jsonRequest = "{\"symptoms\":[" + processedInput + "]}";

                    URL url = new URL("http://10.0.2.2:8080/health-backend/resources/health/predict");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(5000);

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonRequest.getBytes());
                        os.flush();
                    }

                    String disease = "Unknown";
                    if (conn.getResponseCode() == 200) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        String backendResponse = br.readLine();
                        
                        if (backendResponse != null && backendResponse.contains(":")) {
                            disease = backendResponse
                                    .replace("{", "")
                                    .replace("}", "")
                                    .replace("\"", "")
                                    .split(":")[1].trim();
                        }
                    }

                    final String finalDisease = disease;

                    if (disease.equals("Unknown")) {
                        runOnUiThread(() -> {
                            loadingBar.setVisibility(View.GONE);
                            button.setEnabled(true);
                            Toast.makeText(this, "Backend returned no prediction", Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    // 🔹 STEP 2: GET GEMINI EXPLANATION
                    String explanation = getGeminiResponse(finalDisease);

                    // 🔹 FALLBACK: Use Wikipedia if Gemini fails
                    if (explanation.startsWith("⚠")) {
                        String wikiSummary = getWikipediaSummary(finalDisease);
                        if (!wikiSummary.startsWith("⚠")) {
                            explanation = "<b>Source: Wikipedia</b><br><br>" + wikiSummary;
                        }
                    }

                    final String finalExplanation = explanation;
                    runOnUiThread(() -> {
                        loadingBar.setVisibility(View.GONE);
                        button.setEnabled(true);
                        resultCard.setVisibility(View.VISIBLE);
                        diseaseHeader.setText("Condition: " + finalDisease);
                        result.setText(Html.fromHtml(finalExplanation.replace("\n", "<br>"), Html.FROM_HTML_MODE_COMPACT));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        loadingBar.setVisibility(View.GONE);
                        button.setEnabled(true);
                        Toast.makeText(this, "❌ Backend error (check connection)", Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
    }

    private String getGeminiResponse(String disease) {
        try {
            URL url = new URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY
            );

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String requestBody = "{ \"contents\": [ { \"parts\": [ { \"text\": \"Provide a comprehensive medical guide for "
                    + disease +
                    ". Include clear sections for: OVERVIEW, CAUSES, SYMPTOMS, TREATMENT, and PREVENTION. Use bullet points for details and keep explanations simple but professional. Add a medical disclaimer at the end.\" } ] } ] }";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes());
                os.flush();
            }

            BufferedReader br;
            if (conn.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            JSONObject json = new JSONObject(response.toString());
            String rawText = json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            return formatMedicalText(rawText);

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ AI explanation unavailable";
        }
    }

    private String getWikipediaSummary(String diseaseName) {
        try {
            String encoded = URLEncoder.encode(diseaseName, "UTF-8");
            URL url = new URL("https://en.wikipedia.org/w/api.php?action=query&prop=extracts&exintro=1&titles=" + encoded + "&format=json&origin=*");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "AIHealthAssistant/1.0");
            conn.setConnectTimeout(5000);

            if (conn.getResponseCode() != 200) {
                return "⚠ No summary found on Wikipedia.";
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            JSONObject json = new JSONObject(sb.toString());
            JSONObject pages = json.getJSONObject("query").getJSONObject("pages");
            String key = pages.keys().next();
            String extract = pages.getJSONObject(key).optString("extract", "No detailed information found.");
            
            return formatMedicalText(extract);

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ Wikipedia fallback failed.";
        }
    }

    private String formatMedicalText(String text) {
        if (text == null) return "";
        
        String[] sections = {"OVERVIEW", "CAUSES", "SYMPTOMS", "TREATMENT", "PREVENTION", "DISCLAIMER", "DESCRIPTION"};
        String formatted = text;
        
        formatted = formatted.replace("**", "");
        
        for (String section : sections) {
            formatted = formatted.replaceAll("(?i)(?m)^(" + section + ")(:?)", 
                "<br><font color='#8AB4F8'><b>$1</b></font><br>");
        }

        return formatted.replace("\n", "<br>")
                        .replace("* ", "• ")
                        .replace("- ", "• ");
    }
}
