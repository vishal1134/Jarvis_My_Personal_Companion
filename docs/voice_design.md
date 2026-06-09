# Voice Design

Jarvis is voice-first. Text is mainly for debugging, logs, memory, and training.

## Voice Direction

The assistant voice should feel:

- Calm
- Clear
- Loyal
- Warm
- Professional
- Bilingual Tamil-English friendly

The project should not depend on imitating a copyrighted movie character or a
real actor's exact voice. Instead, Jarvis should have an original voice identity
that can improve over time.

## Voice Layers

```text
Response text
  -> voice style cleanup
  -> text-to-speech engine
  -> audio output
```

## Laptop Voice Preview

For early testing on Windows, Jarvis can use the built-in Windows speech engine.
This lets us hear the server's `spoken_response` before the Android app exists.

## Android Voice

The Android app should use Android Text-to-Speech first because it is simple and
offline-friendly. Later options:

- Better Android TTS voice packs
- Piper TTS
- Coqui/Piper-style local voices
- Custom original Jarvis voice, trained only from permitted voice data

## Bilingual Behavior

Jarvis should speak in the selected response language:

```text
English: "Calling appa, sir."
Tamil-English: "appa ku call pannuren, sir."
Tamil mode: Tamil-style wording with the owner's title.
```

## Future Voice Settings

Jarvis should eventually support:

- Speaking rate
- Pitch
- Voice selection
- Owner title
- Quiet mode
- Confirmation tone
- Wake acknowledgement tone

## Pause Control

Some text-to-speech engines pause too long after commas. The early Windows voice
preview removes commas before `sir`, `mam`, and `madam` only for speech output.
The original server response remains unchanged for logs and debugging.
