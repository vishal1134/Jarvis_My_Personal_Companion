package com.vishal.jarvis;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class YouTubeSearcher {
    private final Context context;

    public YouTubeSearcher(Context context) {
        this.context = context;
    }

    public boolean search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String encodedQuery = encode(query.trim());
        Uri uri = Uri.parse("https://www.youtube.com/results?search_query=" + encodedQuery);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.youtube");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (intent.resolveActivity(context.getPackageManager()) == null) {
            intent.setPackage(null);
        }

        context.startActivity(intent);
        return true;
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException exception) {
            return value.replace(" ", "+");
        }
    }
}

