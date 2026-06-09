# Assistant Identity

Jarvis now has active names and old names in memory.

## Active Names

Jarvis can keep up to 3 active names.

Default:

```text
jarvis
```

## Commands

Change primary name:

```text
Jarvis change your name to Friday
Jarvis rename yourself to Friday
Jarvis your name is now Friday
```

Add another active name:

```text
Jarvis add Friday as your name
```

Remove an active name:

```text
Jarvis remove Friday
```

## Old Name Reminder

If the name is changed from `jarvis` to `friday`, then calling the old name:

```text
Jarvis
```

returns:

```text
You have changed my name to friday, sir.
```

## Profile Direction

Future profile mapping:

```text
Jarvis = male voice
Friday = female voice
```

The current MVP stores names on the server. Android voice-profile mapping comes
later.

