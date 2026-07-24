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

## YouTube Search

```text
Jarvis search vijay songs in youtube
Jarvis youtube search tamil songs
Jarvis youtube la tamil songs search pannu
Jarvis youtube la munbe vaa search pannu
Jarvis youtube la cooking video search
```

Current Android behavior:

```text
Jarvis speaks the response, then opens YouTube search results.
```

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
whatsapp la yaaru enaku message pannirukaa Jarvis
Jarvis whom have messaged me in whatsapp
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

## Essential Local Commands

```text
Jarvis wake up
Jarvis are you there
Jarvis what can you do
Jarvis repeat
Jarvis stop
Jarvis what time is it
Jarvis today date
time ena Jarvis
Jarvis open settings
Jarvis wifi settings
wifi open pannu Jarvis
Jarvis wifi on pannu
turn on wifi Jarvis
Jarvis wifi off pannu
Jarvis connect to homewifi
Jarvis homewifi connect pannu wifi password vandhu kowsalya at 05 k capital letter
Jarvis bluetooth settings
Jarvis bluetooth on pannu
Jarvis bluetooth off pannu
Jarvis sony headphones bluetooth connect pannu
Jarvis connect to car bluetooth
Jarvis turn on flashlight
Jarvis torch on pannu
torch off pannu Jarvis
Jarvis torch off
```

Multiple commands:

```text
Jarvis time enna and torch on pannu
Jarvis open calculator appuram bluetooth on pannu
```

Wi-Fi note:

```text
Android may ask for confirmation before adding or connecting to a Wi-Fi network.
Jarvis stores provided Wi-Fi passwords in local server memory.
```

Android behavior:

```text
Stop speaking stops Android TTS.
Settings commands open Android settings screens.
Flashlight commands need camera permission once.
```

## Assistant Names

```text
Jarvis change your name to Friday
Jarvis rename yourself to Friday
Jarvis your name is now Friday
Jarvis add Friday as your name
Jarvis remove Friday
```

Jarvis supports up to 3 active names. Removed or changed names are kept as old
names so Jarvis can remind you of the current name.
