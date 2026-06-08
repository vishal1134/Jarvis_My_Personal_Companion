# Memory Design

Jarvis memory is separated by purpose so private data, command learning, and
daily behavior stay clean.

## Memory Types

```text
identity_memory
  Owner name, owner title, assistant names, old names, trusted speakers.

daily_memory
  First greeting state, last interaction time, today-only events.

command_memory
  Commands, detected intents, success/failure, corrections.

frequent_commands
  High-use command variants mapped to fast actions.

personal_memory
  Contact aliases, app aliases, routines, preferences, device names.

secure_memory
  Wi-Fi passwords, tokens, and secrets. This must be encrypted.
```

## First Greeting

Jarvis greets the owner once per day on the first interaction.

```json
{
  "date": "2026-06-08",
  "first_greeting_done": true,
  "first_interaction_time": "08:12"
}
```

Greeting language follows the detected command language unless the owner has set
an explicit response language.

## Frequent Command Cache

Frequently used commands are cached to reduce response time.

```json
{
  "phrase_variants": [
    "call appa",
    "appa ku call pannu",
    "appa call"
  ],
  "intent": "call_contact",
  "target": "appa",
  "usage_count": 48,
  "last_used_at": "2026-06-08T08:12:00+05:30"
}
```

## Security Rule

The owner can change identity, memory, assistant names, and sensitive settings.
Trusted users may get limited control. Unknown speakers should not access private
memory or risky actions.

