package com.vishal.jarvis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.Locale;

public class ContactCaller {
    public enum Result {
        STARTED,
        MISSING_PERMISSION,
        CONTACT_NOT_FOUND,
        PHONE_NUMBER_NOT_FOUND
    }

    private final Context context;

    public ContactCaller(Context context) {
        this.context = context;
    }

    public Result callContact(String target) {
        if (!hasPermission(Manifest.permission.READ_CONTACTS) || !hasPermission(Manifest.permission.CALL_PHONE)) {
            return Result.MISSING_PERMISSION;
        }

        String phoneNumber = findPhoneNumber(target);
        if (phoneNumber == null) {
            return Result.CONTACT_NOT_FOUND;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + Uri.encode(phoneNumber)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return Result.STARTED;
    }

    private boolean hasPermission(String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private String findPhoneNumber(String target) {
        if (target == null || target.trim().isEmpty()) {
            return null;
        }

        String normalizedTarget = normalize(target);
        String partialMatchNumber = null;

        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor == null) {
            return null;
        }

        try {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);
                if (name == null || number == null) {
                    continue;
                }

                String normalizedName = normalize(name);
                if (normalizedName.equals(normalizedTarget)) {
                    return number;
                }

                if (partialMatchNumber == null && normalizedName.contains(normalizedTarget)) {
                    partialMatchNumber = number;
                }
            }
        } finally {
            cursor.close();
        }

        return partialMatchNumber;
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.US).trim().replaceAll("\\s+", " ");
    }
}

