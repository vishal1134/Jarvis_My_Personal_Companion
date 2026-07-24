package com.vishal.jarvis;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class BluetoothController {
    private final Context context;

    public BluetoothController(Context context) {
        this.context = context;
    }

    public void openBluetoothSettings() {
        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
