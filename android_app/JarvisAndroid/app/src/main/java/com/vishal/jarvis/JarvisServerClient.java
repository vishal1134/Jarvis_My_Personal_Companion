package com.vishal.jarvis;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JarvisServerClient {
    public String handleCommand(String serverUrl, String commandText) throws Exception {
        String cleanServerUrl = trimTrailingSlash(serverUrl);
        URL url = new URL(cleanServerUrl + "/commands/handle");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setDoOutput(true);

        JSONObject payload = new JSONObject();
        payload.put("text", commandText);

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write(payload.toString());
        }

        int statusCode = connection.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                statusCode >= 200 && statusCode < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream(),
                StandardCharsets.UTF_8
        ));

        StringBuilder responseBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            responseBody.append(line);
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new IllegalStateException("Server returned " + statusCode + ": " + responseBody);
        }

        JSONObject response = new JSONObject(responseBody.toString());
        return response.getString("spoken_response");
    }

    private String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}

