package com.vishal.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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
    private static final int CALL_PERMISSION_REQUEST = 1001;
    private static final int AUDIO_PERMISSION_REQUEST = 1002;
    private static final int CAMERA_PERMISSION_REQUEST = 1003;

    private EditText serverUrlInput;
    private EditText commandInput;
    private TextView statusText;
    private Spinner voiceSpinner;
    private TextToSpeech textToSpeech;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private final List<Voice> availableVoices = new ArrayList<>();
    private final JarvisServerClient serverClient = new JarvisServerClient();
    private AppLauncher appLauncher;
    private ContactCaller contactCaller;
    private NotificationReader notificationReader;
    private YouTubeSearcher youTubeSearcher;
    private SystemSettingsOpener systemSettingsOpener;
    private FlashlightController flashlightController;
    private WifiController wifiController;
    private BluetoothController bluetoothController;
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
        contactCaller = new ContactCaller(this);
        notificationReader = new NotificationReader(this);
        youTubeSearcher = new YouTubeSearcher(this);
        systemSettingsOpener = new SystemSettingsOpener(this);
        flashlightController = new FlashlightController(this);
        wifiController = new WifiController(this);
        bluetoothController = new BluetoothController(this);
        Button speakButton = findViewById(R.id.speakButton);
        Button sampleButton = findViewById(R.id.sampleButton);
        Button micButton = findViewById(R.id.micButton);

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
        micButton.setOnClickListener(view -> startVoiceCommand());

        setupSpeechRecognizer();
    }

    private void setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            statusText.setText(R.string.speech_recognition_unavailable);
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                statusText.setText(R.string.listening);
            }

            @Override
            public void onBeginningOfSpeech() {
                statusText.setText(R.string.hearing_you);
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                statusText.setText(R.string.processing_voice);
            }

            @Override
            public void onError(int error) {
                statusText.setText(getString(R.string.speech_error, error));
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches == null || matches.isEmpty()) {
                    statusText.setText(R.string.no_speech_match);
                    return;
                }

                String recognizedText = matches.get(0);
                commandInput.setText(recognizedText);
                sendCommand(null);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    private void startVoiceCommand() {
        if (speechRecognizer == null || speechRecognizerIntent == null) {
            statusText.setText(R.string.speech_recognition_unavailable);
            return;
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST);
            statusText.setText(R.string.audio_permission_needed);
            return;
        }

        speechRecognizer.startListening(speechRecognizerIntent);
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
                List<JarvisResult> results = serverClient.handleCommand(serverUrl, command);
                runOnUiThread(() -> handleJarvisResults(results));
            } catch (Exception exception) {
                runOnUiThread(() -> statusText.setText(
                        getString(R.string.server_error, exception.getMessage())
                ));
            }
        });
    }

    private void handleJarvisResults(List<JarvisResult> results) {
        StringBuilder spokenResponse = new StringBuilder();
        for (JarvisResult result : results) {
            if (result.getSpokenResponse() == null || result.getSpokenResponse().isEmpty()) {
                continue;
            }
            if (spokenResponse.length() > 0) {
                spokenResponse.append(" ");
            }
            spokenResponse.append(result.getSpokenResponse());
        }

        if (spokenResponse.length() > 0) {
            speakResponse(spokenResponse.toString());
        }

        for (JarvisResult result : results) {
            handleJarvisAction(result);
        }
    }

    private void handleJarvisAction(JarvisResult result) {
        if ("stop_speaking".equals(result.getIntent())) {
            stopSpeaking();
            return;
        }

        if ("read_notifications".equals(result.getIntent())) {
            handleReadNotifications();
            return;
        }

        if ("query_notifications".equals(result.getIntent())) {
            handleQueryNotifications(result.getTarget());
            return;
        }

        if ("open_app".equals(result.getIntent())) {
            boolean opened = appLauncher.openApp(result.getTarget());
            if (!opened) {
                statusText.setText(getString(R.string.app_not_found, result.getTarget()));
            }
            return;
        }

        if ("search_youtube".equals(result.getIntent())) {
            boolean opened = youTubeSearcher.search(result.getTarget());
            if (!opened) {
                statusText.setText(R.string.youtube_search_failed);
            }
            return;
        }

        if ("open_system_settings".equals(result.getIntent())) {
            systemSettingsOpener.open(result.getTarget());
            return;
        }

        if ("set_wifi_state".equals(result.getIntent())) {
            wifiController.openWifiSettings();
            return;
        }

        if ("connect_wifi".equals(result.getIntent())) {
            handleConnectWifi(result);
            return;
        }

        if ("set_bluetooth_state".equals(result.getIntent())
                || "connect_bluetooth".equals(result.getIntent())) {
            bluetoothController.openBluetoothSettings();
            return;
        }

        if ("set_flashlight".equals(result.getIntent())) {
            handleFlashlight(result.getTarget());
            return;
        }

        if ("call_contact".equals(result.getIntent())) {
            handleCallContact(result.getTarget());
        }
    }

    private void stopSpeaking() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        statusText.setText(R.string.stopped_speaking);
    }

    private void handleFlashlight(String target) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            statusText.setText(R.string.camera_permission_needed);
            return;
        }

        boolean enabled = "on".equals(target);
        FlashlightController.Result result = flashlightController.setEnabled(enabled);
        if (result == FlashlightController.Result.MISSING_PERMISSION) {
            statusText.setText(R.string.camera_permission_needed);
        } else if (result == FlashlightController.Result.UNAVAILABLE) {
            statusText.setText(R.string.flashlight_unavailable);
        }
    }

    private void handleReadNotifications() {
        if (!notificationReader.hasNotificationAccess()) {
            statusText.setText(R.string.notification_access_needed);
            notificationReader.openNotificationAccessSettings();
            return;
        }

        speakResponse(notificationReader.summarizeNotifications("sir"));
    }

    private void handleQueryNotifications(String appName) {
        if (!notificationReader.hasNotificationAccess()) {
            statusText.setText(R.string.notification_access_needed);
            notificationReader.openNotificationAccessSettings();
            return;
        }

        speakResponse(notificationReader.summarizeNotificationsForApp(appName, "sir"));
    }

    private void handleConnectWifi(JarvisResult result) {
        if (result.getWifiPassword() == null || result.getWifiPassword().isEmpty()) {
            wifiController.openWifiSettings();
            return;
        }

        boolean openedAddNetwork = wifiController.openAddNetworkFlow(result.getTarget(), result.getWifiPassword());
        if (!openedAddNetwork) {
            statusText.setText(R.string.wifi_manual_connection_needed);
        }
    }

    private void handleCallContact(String target) {
        if (!hasCallPermissions()) {
            requestPermissions(
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION_REQUEST
            );
            statusText.setText(R.string.call_permissions_needed);
            return;
        }

        ContactCaller.Result result = contactCaller.callContact(target);
        if (result == ContactCaller.Result.CONTACT_NOT_FOUND) {
            statusText.setText(getString(R.string.contact_not_found, target));
        } else if (result == ContactCaller.Result.PHONE_NUMBER_NOT_FOUND) {
            statusText.setText(getString(R.string.phone_number_not_found, target));
        } else if (result == ContactCaller.Result.MISSING_PERMISSION) {
            statusText.setText(R.string.call_permissions_needed);
        }
    }

    private boolean hasCallPermissions() {
        return checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
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
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}
