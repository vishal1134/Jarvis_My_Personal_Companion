package com.vishal.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int CALL_PERMISSION_REQUEST = 1001;
    private static final int AUDIO_PERMISSION_REQUEST = 1002;
    private static final int CAMERA_PERMISSION_REQUEST = 1003;

    private EditText serverUrlInput;
    private EditText commandInput;
    private TextView statusText;
    private TextView listeningStateText;
    private TextView permissionStateText;
    private TextView activeVoiceText;
    private Spinner voiceSpinner;
    private Spinner assistantNameSpinner;
    private Switch listeningSwitch;
    private EditText assistantNameInput;
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
    private AssistantIdentityStore identityStore;
    private LocalCommandParser localCommandParser;
    private String lastSpokenResponse = "";
    private boolean assistantAwake = true;
    private boolean continuousListeningEnabled = false;
    private boolean listeningSessionActive = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverUrlInput = findViewById(R.id.serverUrlInput);
        commandInput = findViewById(R.id.commandInput);
        statusText = findViewById(R.id.statusText);
        listeningStateText = findViewById(R.id.listeningStateText);
        permissionStateText = findViewById(R.id.permissionStateText);
        activeVoiceText = findViewById(R.id.activeVoiceText);
        voiceSpinner = findViewById(R.id.voiceSpinner);
        assistantNameSpinner = findViewById(R.id.assistantNameSpinner);
        listeningSwitch = findViewById(R.id.listeningSwitch);
        assistantNameInput = findViewById(R.id.assistantNameInput);
        appLauncher = new AppLauncher(this);
        contactCaller = new ContactCaller(this);
        notificationReader = new NotificationReader(this);
        youTubeSearcher = new YouTubeSearcher(this);
        systemSettingsOpener = new SystemSettingsOpener(this);
        flashlightController = new FlashlightController(this);
        wifiController = new WifiController(this);
        bluetoothController = new BluetoothController(this);
        identityStore = new AssistantIdentityStore(this);
        localCommandParser = new LocalCommandParser();
        Button speakButton = findViewById(R.id.speakButton);
        Button sampleButton = findViewById(R.id.sampleButton);
        Button micButton = findViewById(R.id.micButton);
        Button accessibilityButton = findViewById(R.id.accessibilityButton);
        Button addAssistantNameButton = findViewById(R.id.addAssistantNameButton);
        Button saveAssistantVoiceButton = findViewById(R.id.saveAssistantVoiceButton);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.ENGLISH);
                textToSpeech.setSpeechRate(0.92f);
                textToSpeech.setPitch(0.9f);
                populateVoiceSpinner();
                populateAssistantNameSpinner();
                statusText.setText(R.string.tts_ready);
            } else {
                statusText.setText(R.string.tts_failed);
            }
        });

        sampleButton.setOnClickListener(view -> commandInput.setText(R.string.sample_command));
        speakButton.setOnClickListener(this::sendCommand);
        micButton.setOnClickListener(view -> startVoiceCommand());
        accessibilityButton.setOnClickListener(view -> systemSettingsOpener.open("accessibility"));
        addAssistantNameButton.setOnClickListener(view -> addAssistantName());
        saveAssistantVoiceButton.setOnClickListener(view -> saveVoiceForActiveName());
        listeningSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setContinuousListeningEnabled(isChecked));

        setupSpeechRecognizer();
        updateAssistantStatus();
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
                listeningSessionActive = true;
                statusText.setText(R.string.listening);
                updateAssistantStatus();
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
                listeningSessionActive = false;
                statusText.setText(getString(R.string.speech_error, error));
                scheduleListeningRestart();
            }

            @Override
            public void onResults(Bundle results) {
                listeningSessionActive = false;
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches == null || matches.isEmpty()) {
                    statusText.setText(R.string.no_speech_match);
                    scheduleListeningRestart();
                    return;
                }

                String recognizedText = matches.get(0);
                commandInput.setText(recognizedText);
                sendCommand(null);
                scheduleListeningRestart();
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
            if (listeningSwitch != null) {
                listeningSwitch.setChecked(false);
            }
            return;
        }

        listeningSessionActive = true;
        speechRecognizer.startListening(speechRecognizerIntent);
        updateAssistantStatus();
    }

    private void setContinuousListeningEnabled(boolean enabled) {
        continuousListeningEnabled = enabled;
        if (!enabled) {
            listeningSessionActive = false;
            if (speechRecognizer != null) {
                speechRecognizer.cancel();
            }
            statusText.setText(R.string.jarvis_hearing_off);
            updateAssistantStatus();
            return;
        }

        assistantAwake = true;
        statusText.setText(R.string.jarvis_waiting_for_name);
        updateAssistantStatus();
        startVoiceCommand();
    }

    private void scheduleListeningRestart() {
        if (!continuousListeningEnabled) {
            updateAssistantStatus();
            return;
        }

        mainHandler.removeCallbacksAndMessages(null);
        mainHandler.postDelayed(() -> {
            if (continuousListeningEnabled && !listeningSessionActive) {
                startVoiceCommand();
            }
        }, 1200);
    }

    private void updateAssistantStatus() {
        if (listeningStateText == null || permissionStateText == null || identityStore == null) {
            return;
        }

        if (!continuousListeningEnabled) {
            listeningStateText.setText(R.string.jarvis_hearing_off);
        } else if (listeningSessionActive) {
            listeningStateText.setText(R.string.jarvis_listening_for_wake);
        } else {
            listeningStateText.setText(R.string.jarvis_waiting_for_name);
        }

        boolean micReady = checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        boolean notificationReady = notificationReader != null && notificationReader.hasNotificationAccess();
        boolean accessibilityReady = JarvisAccessibilityService.isRunning();
        permissionStateText.setText(getString(
                R.string.permission_state,
                identityStore.getActiveName(),
                micReady ? "OK" : "Needed",
                notificationReady ? "OK" : "Needed",
                accessibilityReady ? "OK" : "Needed"
        ));
        updateActiveVoiceText();
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
        voiceSpinner.setOnItemSelectedListener(null);
        voiceSpinner.setAdapter(adapter);

        String savedVoiceName = identityStore.getVoiceForName(identityStore.getActiveName());
        int selectedIndex = findVoiceIndex(savedVoiceName);
        if (selectedIndex < 0) {
            selectedIndex = findLikelyJarvisVoiceIndex();
            if (selectedIndex >= 0) {
                identityStore.setVoiceForName(identityStore.getActiveName(), availableVoices.get(selectedIndex).getName());
            }
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
                updateActiveVoiceText();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        updateActiveVoiceText();
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

    private void populateAssistantNameSpinner() {
        List<String> names = identityStore.getNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                names
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assistantNameSpinner.setOnItemSelectedListener(null);
        assistantNameSpinner.setAdapter(adapter);

        int activeIndex = names.indexOf(identityStore.getActiveName());
        if (activeIndex >= 0) {
            assistantNameSpinner.setSelection(activeIndex);
        }

        assistantNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<String> currentNames = identityStore.getNames();
                if (position < 0 || position >= currentNames.size()) {
                    return;
                }

                identityStore.setActiveName(currentNames.get(position));
                applyVoiceForActiveName();
                updateAssistantStatus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void addAssistantName() {
        String newName = assistantNameInput.getText().toString().trim();
        if (newName.isEmpty()) {
            statusText.setText(R.string.enter_assistant_name);
            return;
        }

        identityStore.addName(newName, getSelectedVoiceName());
        assistantNameInput.setText("");
        populateAssistantNameSpinner();
        applyVoiceForActiveName();
        statusText.setText(getString(R.string.assistant_name_saved, newName));
        updateAssistantStatus();
    }

    private void saveVoiceForActiveName() {
        String selectedVoiceName = getSelectedVoiceName();
        if (selectedVoiceName == null || selectedVoiceName.isEmpty()) {
            statusText.setText(R.string.no_tts_voices);
            return;
        }

        identityStore.setVoiceForName(identityStore.getActiveName(), selectedVoiceName);
        statusText.setText(getString(R.string.assistant_voice_saved, identityStore.getActiveName()));
        updateAssistantStatus();
    }

    private void applyVoiceForActiveName() {
        if (textToSpeech == null || availableVoices.isEmpty()) {
            return;
        }

        String voiceName = identityStore.getVoiceForName(identityStore.getActiveName());
        int voiceIndex = findVoiceIndex(voiceName);
        if (voiceIndex < 0) {
            voiceIndex = findLikelyJarvisVoiceIndex();
            if (voiceIndex >= 0) {
                identityStore.setVoiceForName(identityStore.getActiveName(), availableVoices.get(voiceIndex).getName());
            }
        }
        if (voiceIndex >= 0) {
            voiceSpinner.setSelection(voiceIndex);
            textToSpeech.setVoice(availableVoices.get(voiceIndex));
        }
        updateActiveVoiceText();
    }

    private String getSelectedVoiceName() {
        int position = voiceSpinner.getSelectedItemPosition();
        if (position >= 0 && position < availableVoices.size()) {
            return availableVoices.get(position).getName();
        }
        return null;
    }

    private void updateActiveVoiceText() {
        if (activeVoiceText == null || identityStore == null) {
            return;
        }

        String savedVoice = identityStore.getVoiceForName(identityStore.getActiveName());
        String selectedVoice = getSelectedVoiceName();
        if (savedVoice == null || savedVoice.isEmpty()) {
            activeVoiceText.setText(getString(R.string.active_voice_missing, identityStore.getActiveName()));
        } else if (savedVoice.equals(selectedVoice)) {
            activeVoiceText.setText(getString(R.string.active_voice_locked, identityStore.getActiveName(), savedVoice));
        } else {
            activeVoiceText.setText(getString(R.string.active_voice_unsaved, identityStore.getActiveName(), selectedVoice));
        }
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

        if (command.isEmpty()) {
            statusText.setText(R.string.enter_command);
            return;
        }

        if (continuousListeningEnabled && !usesWakeName(command)) {
            statusText.setText(R.string.jarvis_ignored_without_name);
            return;
        }

        if (!assistantAwake && !usesWakeName(command)) {
            statusText.setText(R.string.jarvis_sleeping);
            return;
        }

        List<JarvisResult> localResults = localCommandParser.parse(
                command,
                lastSpokenResponse,
                identityStore.getNames(),
                identityStore.getActiveName()
        );
        if (shouldHandleLocally(command, localResults)) {
            handleJarvisResults(localResults);
            return;
        }

        if (serverUrl.isEmpty()) {
            handleJarvisResults(localResults);
            return;
        }

        statusText.setText(R.string.contacting_jarvis);
        executor.execute(() -> {
            try {
                List<JarvisResult> results = serverClient.handleCommand(serverUrl, command);
                runOnUiThread(() -> handleJarvisResults(results));
            } catch (Exception exception) {
                runOnUiThread(() -> handleJarvisResults(localResults));
            }
        });
    }

    private boolean shouldHandleLocally(String command, List<JarvisResult> results) {
        if (results.isEmpty()) {
            return false;
        }

        for (JarvisResult result : results) {
            if ("unknown".equals(result.getIntent())) {
                return false;
            }
        }
        return true;
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
        if ("wake_assistant".equals(result.getIntent())) {
            assistantAwake = true;
            return;
        }

        if ("sleep_assistant".equals(result.getIntent())) {
            assistantAwake = false;
            if (continuousListeningEnabled) {
                setContinuousListeningEnabled(false);
                if (listeningSwitch != null) {
                    listeningSwitch.setChecked(false);
                }
            }
            return;
        }

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

        if ("phone_action".equals(result.getIntent())) {
            handlePhoneAction(result.getTarget());
            return;
        }

        if ("screen_action".equals(result.getIntent())) {
            handleScreenAction(result.getTarget());
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

    private void handlePhoneAction(String action) {
        if (!JarvisAccessibilityService.isRunning()) {
            statusText.setText(R.string.accessibility_needed);
            systemSettingsOpener.open("accessibility");
            return;
        }

        boolean performed = JarvisAccessibilityService.performPhoneAction(action);
        if (!performed) {
            statusText.setText(R.string.phone_action_failed);
        }
    }

    private void handleScreenAction(String action) {
        if (!JarvisAccessibilityService.isRunning()) {
            statusText.setText(R.string.accessibility_needed);
            systemSettingsOpener.open("accessibility");
            return;
        }

        boolean performed = false;
        if (action.startsWith("tap:")) {
            performed = JarvisAccessibilityService.tapVisibleText(action.substring("tap:".length()));
        } else if (action.startsWith("type:")) {
            performed = JarvisAccessibilityService.typeIntoFocusedField(action.substring("type:".length()));
        }
        if (!performed) {
            statusText.setText(R.string.screen_action_failed);
        }
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
        lastSpokenResponse = spokenResponse;
        statusText.setText(spokenResponse);
        String speechText = spokenResponse
                .replace(", sir", " sir")
                .replace(", mam", " mam")
                .replace(", madam", " madam");
        textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "jarvis_response");
    }

    private boolean usesWakeName(String command) {
        String normalized = command == null ? "" : command.toLowerCase(Locale.US).trim();
        for (String name : identityStore.getNames()) {
            String normalizedName = name.toLowerCase(Locale.US).trim();
            if (normalized.equals(normalizedName)
                    || normalized.startsWith(normalizedName + " ")
                    || normalized.endsWith(" " + normalizedName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacksAndMessages(null);
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
