package com.vishal.jarvis;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private EditText serverUrlInput;
    private EditText commandInput;
    private TextView statusText;
    private TextToSpeech textToSpeech;
    private final JarvisServerClient serverClient = new JarvisServerClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverUrlInput = findViewById(R.id.serverUrlInput);
        commandInput = findViewById(R.id.commandInput);
        statusText = findViewById(R.id.statusText);
        Button speakButton = findViewById(R.id.speakButton);
        Button sampleButton = findViewById(R.id.sampleButton);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
                textToSpeech.setSpeechRate(0.92f);
                textToSpeech.setPitch(0.9f);
                statusText.setText(R.string.tts_ready);
            } else {
                statusText.setText(R.string.tts_failed);
            }
        });

        sampleButton.setOnClickListener(view -> commandInput.setText(R.string.sample_command));
        speakButton.setOnClickListener(this::sendCommand);
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
                String spokenResponse = serverClient.handleCommand(serverUrl, command);
                runOnUiThread(() -> speakResponse(spokenResponse));
            } catch (Exception exception) {
                runOnUiThread(() -> statusText.setText(
                        getString(R.string.server_error, exception.getMessage())
                ));
            }
        });
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

