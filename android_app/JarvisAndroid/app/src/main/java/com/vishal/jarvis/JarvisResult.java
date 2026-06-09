package com.vishal.jarvis;

public class JarvisResult {
    private final String intent;
    private final String target;
    private final String spokenResponse;

    public JarvisResult(String intent, String target, String spokenResponse) {
        this.intent = intent;
        this.target = target;
        this.spokenResponse = spokenResponse;
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
}

