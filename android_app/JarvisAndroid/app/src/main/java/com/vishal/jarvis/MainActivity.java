package com.vishal.jarvis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String PREFS_NAME = "jarvis_settings";
    private static final String KEY_VOICE_NAME = "jarvis_voice_name";

    private EditText serverUrlInput;
    private EditText commandInput;
    private TextView statusText;
    private Spinner voiceSpinner;
    private TextToSpeech textToSpeech;
    private final List<Voice> availableVoices = new ArrayList<>();
    private final JarvisServerClient serverClient = new JarvisServerClient();
    private AppLauncher appLauncher;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverUrlInput = findViewById(R.id.serverUrlInput);
        commandInput = findViewById(R.id.commandInput);
        statusText = findViewById(R.id.statusText);
        voiceSpinner = findViewById(R.id.voiceSpinner);
        appLauncher = new AppLauncher(this);
        Button speakButton = findViewById(R.id.speakButton);
        Button sampleButton = findViewById(R.id.sampleButton);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
                textToSpeech.setSpeechRate(0.92f);
                textToSpeech.setPitch(0.9f);
                populateVoiceSpinner();
                statusText.setText(R.string.tts_ready);
            } else {
                statusText.setText(R.string.tts_failed);
            }
        });

        sampleButton.setOnClickListener(view -> commandInput.setText(R.string.sample_command));
        speakButton.setOnClickListener(this::sendCommand);
    }

    private void populateVoiceSpinner() {
        Set<Voice> voices = textToSpeech.getVoices();
        if (voices == null || voices.isEmpty()) {
            statusText.setText(R.string.no_tts_voices);
            return;
        }

        availableVoices.clear();
        for (Voice voice : voices) {
            Locale locale = voice.getLocale();
            if (locale != null && "en".equals(locale.getLanguage()) && !voice.isNetworkConnectionRequired()) {
                availableVoices.add(voice);
            }
        }

        availableVoices.sort(Comparator.comparing(Voice::getName));

        List<String> voiceNames = new ArrayList<>();
        for (Voice voice : availableVoices) {
            voiceNames.add(voice.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                voiceNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        voiceSpinner.setAdapter(adapter);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedVoiceName = preferences.getString(KEY_VOICE_NAME, null);
        int selectedIndex = findVoiceIndex(savedVoiceName);
        if (selectedIndex < 0) {
            selectedIndex = findLikelyJarvisVoiceIndex();
        }
        if (selectedIndex >= 0) {
            voiceSpinner.setSelection(selectedIndex);
            textToSpeech.setVoice(availableVoices.get(selectedIndex));
        }

        voiceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= availableVoices.size()) {
                    return;
                }

                Voice selectedVoice = availableVoices.get(position);
                textToSpeech.setVoice(selectedVoice);
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit()
                        .putString(KEY_VOICE_NAME, selectedVoice.getName())
                        .apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private int findVoiceIndex(String voiceName) {
        if (voiceName == null) {
            return -1;
        }

        for (int index = 0; index < availableVoices.size(); index++) {
            if (voiceName.equals(availableVoices.get(index).getName())) {
                return index;
            }
        }
        return -1;
    }

    private int findLikelyJarvisVoiceIndex() {
        for (int index = 0; index < availableVoices.size(); index++) {
            String name = availableVoices.get(index).getName().toLowerCase(Locale.US);
            if (name.contains("male") || name.contains("man")) {
                return index;
            }
        }
        return availableVoices.isEmpty() ? -1 : 0;
    }

    private void sendCommand(View view) {
        String serverUrl = serverUrlInput.getText().toString().trim();
        String command = commandInput.getText().toString().trim();

        if (serverUrl.isEmpty()) {
            statusText.setText(R.string.enter_server_url);
            return;
        }

        if (command.isEmpty()) {
            statusText.setText(R.string.enter_command);
            return;
        }

        statusText.setText(R.string.contacting_jarvis);
        executor.execute(() -> {
            try {
                JarvisResult result = serverClient.handleCommand(serverUrl, command);
                runOnUiThread(() -> handleJarvisResult(result));
            } catch (Exception exception) {
                runOnUiThread(() -> statusText.setText(
                        getString(R.string.server_error, exception.getMessage())
                ));
            }
        });
    }

    private void handleJarvisResult(JarvisResult result) {
        speakResponse(result.getSpokenResponse());

        if ("open_app".equals(result.getIntent())) {
            boolean opened = appLauncher.openApp(result.getTarget());
            if (!opened) {
                statusText.setText(getString(R.string.app_not_found, result.getTarget()));
            }
        }
    }

    private void speakResponse(String spokenResponse) {
        statusText.setText(spokenResponse);
        String speechText = spokenResponse
                .replace(", sir", " sir")
                .replace(", mam", " mam")
                .replace(", madam", " madam");
        textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "jarvis_response");
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
