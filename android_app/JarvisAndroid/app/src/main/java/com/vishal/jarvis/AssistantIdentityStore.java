package com.vishal.jarvis;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AssistantIdentityStore {
    private static final String PREFS_NAME = "jarvis_identities";
    private static final String KEY_NAMES = "assistant_names";
    private static final String KEY_ACTIVE_NAME = "active_assistant_name";
    private static final String VOICE_PREFIX = "voice_";
    private static final String DEFAULT_NAME = "jarvis";

    private final SharedPreferences preferences;

    public AssistantIdentityStore(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (getNames().isEmpty()) {
            preferences.edit()
                    .putString(KEY_NAMES, DEFAULT_NAME)
                    .putString(KEY_ACTIVE_NAME, DEFAULT_NAME)
                    .apply();
        }
    }

    public List<String> getNames() {
        String stored = preferences.getString(KEY_NAMES, DEFAULT_NAME);
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (String name : stored.split("\\|")) {
            String normalized = normalizeName(name);
            if (!normalized.isEmpty()) {
                names.add(normalized);
            }
        }
        return new ArrayList<>(names);
    }

    public void addName(String name, String voiceName) {
        String normalized = normalizeName(name);
        if (normalized.isEmpty()) {
            return;
        }

        Set<String> names = new LinkedHashSet<>(getNames());
        names.add(normalized);
        SharedPreferences.Editor editor = preferences.edit()
                .putString(KEY_NAMES, join(names))
                .putString(KEY_ACTIVE_NAME, normalized);
        if (voiceName != null && !voiceName.isEmpty()) {
            editor.putString(voiceKey(normalized), voiceName);
        }
        editor.apply();
    }

    public String getActiveName() {
        String active = preferences.getString(KEY_ACTIVE_NAME, DEFAULT_NAME);
        return normalizeName(active).isEmpty() ? DEFAULT_NAME : normalizeName(active);
    }

    public void setActiveName(String name) {
        String normalized = normalizeName(name);
        if (!normalized.isEmpty()) {
            preferences.edit().putString(KEY_ACTIVE_NAME, normalized).apply();
        }
    }

    public String getVoiceForName(String name) {
        return preferences.getString(voiceKey(normalizeName(name)), null);
    }

    public void setVoiceForName(String name, String voiceName) {
        String normalized = normalizeName(name);
        if (!normalized.isEmpty() && voiceName != null && !voiceName.isEmpty()) {
            preferences.edit().putString(voiceKey(normalized), voiceName).apply();
        }
    }

    public boolean isKnownName(String name) {
        return getNames().contains(normalizeName(name));
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.US).replaceAll("\\s+", " ");
    }

    private String join(Set<String> names) {
        StringBuilder builder = new StringBuilder();
        for (String name : names) {
            if (builder.length() > 0) {
                builder.append("|");
            }
            builder.append(name);
        }
        return builder.toString();
    }

    private String voiceKey(String name) {
        return VOICE_PREFIX + name;
    }
}
