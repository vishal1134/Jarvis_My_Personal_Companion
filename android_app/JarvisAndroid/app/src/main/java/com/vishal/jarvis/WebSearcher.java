package com.vishal.jarvis;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class WebSearcher {
    private final Context context;

    public WebSearcher(Context context) {
        this.context = context;
    }

    public boolean search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        Intent searchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        searchIntent.putExtra(SearchManager.QUERY, query.trim());
        searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (searchIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(searchIntent);
            return true;
        }

        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=" + encode(query.trim()))
        );
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(browserIntent);
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
