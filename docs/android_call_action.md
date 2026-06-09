# Android Call Action

Current command examples:

```text
Jarvis call appa
appa ku call pannu
```

Android permissions required:

```text
READ_CONTACTS
CALL_PHONE
```

First run behavior:

```text
1. Jarvis speaks the response.
2. Android asks for Contacts and Phone permissions.
3. Allow both permissions.
4. Send the command again.
5. Jarvis finds the contact and starts the call.
```

Current lookup behavior:

```text
1. Exact contact name match.
2. First partial name match.
```

Future improvements:

```text
Contact aliases:
  appa -> exact saved contact
  amma -> exact saved contact

Multiple match handling:
  "I found two contacts named Appa. Which one should I call, sir?"
```

