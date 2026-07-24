package com.vishal.jarvis;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JarvisServerClient {
    public List<JarvisResult> handleCommand(String serverUrl, String commandText) throws Exception {
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
        if (response.has("commands")) {
            return parseMultipleResults(response);
        }

        ArrayList<JarvisResult> results = new ArrayList<>();
        results.add(parseResult(response.getJSONObject("command"), response.getString("spoken_response")));
        return results;
    }

    private String trimTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private List<JarvisResult> parseMultipleResults(JSONObject response) throws Exception {
        JSONArray commands = response.getJSONArray("commands");
        JSONArray spokenResponses = response.optJSONArray("spoken_responses");
        ArrayList<JarvisResult> results = new ArrayList<>();

        for (int index = 0; index < commands.length(); index++) {
            String spokenResponse = spokenResponses != null && index < spokenResponses.length()
                    ? spokenResponses.getString(index)
                    : "";
            results.add(parseResult(commands.getJSONObject(index), spokenResponse));
        }

        return results;
    }

    private JarvisResult parseResult(JSONObject command, String spokenResponse) {
        JSONObject slots = command.optJSONObject("slots");
        return new JarvisResult(
                command.optString("intent", "unknown"),
                command.optString("target", null),
                spokenResponse,
                slots == null ? null : slots.optString("password", null)
        );
    }
}
