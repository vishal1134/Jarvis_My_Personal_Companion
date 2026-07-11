# Server API

The local laptop server is the optional stronger brain for Jarvis. The Android
app can call it when the laptop is reachable.

## Health

```text
GET /health
```

Response:

```json
{"status":"ok"}
```

## Parse Command

```text
POST /commands/parse
```

Request:

```json
{
  "text": "Jarvis call appa",
  "response_language": "auto"
}
```

Response:

```json
{
  "intent": "call_contact",
  "source_text": "Jarvis call appa",
  "target": "appa",
  "detected_language": "en",
  "response_language": "en",
  "speaker_role": "unknown",
  "requires_confirmation": false,
  "slots": {}
}
```

## Handle Command

```text
POST /commands/handle
```

This endpoint parses the command, applies owner memory, adds first daily greeting
when needed, updates the frequent-command cache, and returns the phrase Jarvis
should speak.

Request:

```json
{
  "text": "Jarvis call appa"
}
```

Response:

```json
{
  "command": {
    "intent": "call_contact",
    "source_text": "Jarvis call appa",
    "target": "appa",
    "detected_language": "en",
    "response_language": "en",
    "speaker_role": "unknown",
    "requires_confirmation": false,
    "slots": {}
  },
  "spoken_response": "Good morning, sir. Calling appa, sir.",
  "should_speak": true,
  "greeting": "Good morning, sir."
}
```

## Command Catalog

```text
GET /commands/catalog
```

Returns the current command intents, trigger words, and examples Jarvis can
recognize.

## Read Memory

```text
GET /memory
```

Returns current local memory. This is for development only until security is
added.

## Update Identity

```text
POST /memory/identity
```

Request:

```json
{
  "owner_name": "Vishal",
  "owner_title": "sir",
  "preferred_response_language": "auto"
}
```
