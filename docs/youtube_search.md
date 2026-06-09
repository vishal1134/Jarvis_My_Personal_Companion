# YouTube Search

Jarvis can search YouTube from the Android app.

## Commands

```text
Jarvis search vijay songs in youtube
Jarvis youtube search tamil songs
Jarvis youtube la tamil songs search pannu
```

## Flow

```text
Command
  -> server returns search_youtube intent
  -> Android speaks response
  -> Android opens YouTube search results
```

## Notes

The Android app prefers the YouTube app. If the YouTube app is unavailable, it
falls back to opening the search URL in a browser.

