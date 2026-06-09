# Current Commands

These are the commands Jarvis understands in the current MVP.

## Calling

```text
Jarvis call appa
call appa
appa ku call pannu
appa koopidu
```

Current output:

```text
Calling appa, sir.
appa ku call pannuren, sir.
```

The current Android MVP now attempts to find the contact and start a real phone
call after Jarvis speaks.

## Open App

```text
Jarvis open whatsapp
open whatsapp
whatsapp open pannu
whatsapp thirakku
Jarvis open calculator
calculator open pannu
```

Current output:

```text
Opening whatsapp, sir.
whatsapp open pannuren, sir.
```

The Android app only speaks the response for now. It does not open the real app
yet on older builds. The current Android MVP now attempts to open the matching
installed app.

## Response Language

```text
reply in tamil
tamil la reply
reply in english
english la reply pannu
```

Current output:

```text
Sari sir, inimey Tamil la reply pannuren.
Of course, sir. I will respond in English.
```

## Unknown Commands

Anything outside these patterns returns:

```text
I did not understand that yet, sir.
```

## Notifications

```text
Jarvis read notifications
notifications read pannu
notifications padi
```

Current Android behavior:

```text
1. If notification access is missing, Jarvis opens the settings screen.
2. After access is enabled, Jarvis summarizes active notifications by app.
```

Current privacy rule:

```text
Jarvis reads counts and app names only, not private notification message text.
```
