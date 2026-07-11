from __future__ import annotations


COMMAND_CATALOG: list[dict[str, object]] = [
    {
        "intent": "call_contact",
        "purpose": "Call a saved phone contact.",
        "trigger_words": ["call", "call pannu", "koopidu", "ku call pannu"],
        "examples": ["Jarvis call appa", "appa ku call pannu", "appa koopidu"],
    },
    {
        "intent": "open_app",
        "purpose": "Open an installed Android app.",
        "trigger_words": ["open", "open pannu", "thirakku"],
        "examples": ["Jarvis open whatsapp", "whatsapp open pannu", "calculator thirakku"],
    },
    {
        "intent": "search_youtube",
        "purpose": "Search YouTube for a spoken query.",
        "trigger_words": ["youtube search", "search in youtube", "in youtube", "youtube la", "search pannu"],
        "examples": [
            "Jarvis search vijay songs in youtube",
            "Jarvis youtube search tamil songs",
            "Jarvis youtube la tamil songs search pannu",
        ],
    },
    {
        "intent": "read_notifications",
        "purpose": "Summarize active Android notifications.",
        "trigger_words": ["read notifications", "notification read", "notifications read", "notification padi"],
        "examples": ["Jarvis read notifications", "notifications read pannu", "notifications padi"],
    },
    {
        "intent": "set_response_language",
        "purpose": "Change Jarvis response language.",
        "trigger_words": ["reply in tamil", "tamil la reply", "reply in english", "english la reply"],
        "examples": ["reply in tamil", "english la reply pannu"],
    },
    {
        "intent": "change_assistant_name",
        "purpose": "Change the primary assistant name.",
        "trigger_words": ["change your name to", "rename yourself to", "your name is now"],
        "examples": ["Jarvis change your name to Friday", "Jarvis rename yourself to Tony"],
    },
    {
        "intent": "add_assistant_name",
        "purpose": "Add another active assistant name.",
        "trigger_words": ["add", "as your name"],
        "examples": ["Jarvis add Friday as your name"],
    },
    {
        "intent": "remove_assistant_name",
        "purpose": "Remove one active assistant name.",
        "trigger_words": ["remove"],
        "examples": ["Jarvis remove Friday"],
    },
]


def command_catalog() -> dict[str, object]:
    return {
        "wake_names": "Dynamic. Default is jarvis. Up to 3 active names are stored in memory.",
        "language_hints": ["pannu", "pannunga", "ku", "la", "pesu", "koopidu", "thirakku"],
        "commands": COMMAND_CATALOG,
    }

