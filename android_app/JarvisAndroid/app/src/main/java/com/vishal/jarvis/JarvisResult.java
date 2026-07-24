package com.vishal.jarvis;

public class JarvisResult {
    private final String intent;
    private final String target;
    private final String spokenResponse;
    private final String wifiPassword;

    public JarvisResult(String intent, String target, String spokenResponse, String wifiPassword) {
        this.intent = intent;
        this.target = target;
        this.spokenResponse = spokenResponse;
        this.wifiPassword = wifiPassword;
    }

    public String getIntent() {
        return intent;
    }

    public String getTarget() {
        return target;
    }

    public String getSpokenResponse() {
        return spokenResponse;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }
}
