# Voice Scripts

These scripts are for early laptop voice testing on Windows.

## Speak Plain Text

```powershell
.\scripts\voice\speak_text.ps1 -Text "Good afternoon, sir. Jarvis is online."
```

## Speak A Jarvis Server Response

First start the server:

```powershell
python -m uvicorn server.api.main:app --reload --host 127.0.0.1 --port 8000
```

Then in another PowerShell window:

```powershell
.\scripts\voice\speak_jarvis_response.ps1 -Text "Jarvis call appa"
```

This sends the command to the server, receives `spoken_response`, speaks it
aloud, and prints the spoken response.

By default, the preview removes small commas before `sir`, `mam`, and `madam`
only while speaking, so the voice does not pause too long. To keep the original
punctuation while speaking:

```powershell
.\scripts\voice\speak_jarvis_response.ps1 -Text "Jarvis call appa" -KeepCommas
```
