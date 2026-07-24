package com.vishal.jarvis;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;

public class WifiController {
    private final Context context;

    public WifiController(Context context) {
        this.context = context;
    }

    public void openWifiSettings() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public boolean openAddNetworkFlow(String ssid, String password) {
        if (ssid == null || ssid.trim().isEmpty() || password == null || password.isEmpty()) {
            openWifiSettings();
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            openWifiSettings();
            return false;
        }

        WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build();

        ArrayList<WifiNetworkSuggestion> suggestions = new ArrayList<>();
        suggestions.add(suggestion);

        try {
            Intent intent = new Intent(Settings.ACTION_WIFI_ADD_NETWORKS);
            intent.putParcelableArrayListExtra(Settings.EXTRA_WIFI_NETWORK_LIST, suggestions);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } catch (RuntimeException exception) {
            openWifiSettings();
            return false;
        }
    }
}
