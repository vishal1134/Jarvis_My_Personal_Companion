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
            "Jarvis youtube la munbe vaa search pannu",
        ],
    },
    {
        "intent": "read_notifications",
        "purpose": "Summarize active Android notifications.",
        "trigger_words": ["read notifications", "notification read", "notifications read", "notification padi"],
        "examples": ["Jarvis read notifications", "notifications read pannu", "notifications padi"],
    },
    {
        "intent": "query_notifications",
        "purpose": "Summarize active notifications from a specific app.",
        "trigger_words": ["whatsapp la yaaru", "whom messaged me in whatsapp", "message pannirukaa"],
        "examples": [
            "whatsapp la yaaru enaku message pannirukaa jarvis",
            "jarvis whom have messaged me in whatsapp",
        ],
    },
    {
        "intent": "wake_assistant",
        "purpose": "Check that Jarvis is awake and listening.",
        "trigger_words": ["jarvis", "are you there", "hello", "status", "irukiya", "kelu", "ready ah"],
        "examples": ["Jarvis", "Jarvis irukiya", "Jarvis status"],
    },
    {
        "intent": "list_commands",
        "purpose": "Hear what Jarvis can currently do.",
        "trigger_words": ["help", "what can you do", "udhavi", "enna panna mudiyum", "commands sollu"],
        "examples": ["Jarvis help", "Jarvis enna panna mudiyum"],
    },
    {
        "intent": "repeat_last_response",
        "purpose": "Repeat the last spoken response.",
        "trigger_words": ["repeat", "repeat that", "marubadi sollu", "thirumba sollu"],
        "examples": ["Jarvis repeat", "Jarvis marubadi sollu"],
    },
    {
        "intent": "stop_speaking",
        "purpose": "Stop Jarvis voice output.",
        "trigger_words": ["stop", "cancel", "be quiet", "niruthu", "pesatha"],
        "examples": ["Jarvis stop", "Jarvis niruthu"],
    },
    {
        "intent": "get_time",
        "purpose": "Tell the current server time.",
        "trigger_words": ["time", "what time is it", "neram enna", "time sollu", "mani enna"],
        "examples": ["Jarvis what time is it", "Jarvis neram enna"],
    },
    {
        "intent": "get_date",
        "purpose": "Tell today's server date.",
        "trigger_words": ["date", "today date", "date sollu", "thethi enna", "innaiku date enna"],
        "examples": ["Jarvis today date", "Jarvis thethi enna"],
    },
    {
        "intent": "open_system_settings",
        "purpose": "Open common Android settings screens.",
        "trigger_words": ["settings", "settings thira", "wifi thira", "bluetooth thira", "notification settings"],
        "examples": ["Jarvis settings thira", "Jarvis wifi thira", "Jarvis bluetooth settings"],
    },
    {
        "intent": "set_wifi_state",
        "purpose": "Open Android Wi-Fi controls for turning Wi-Fi on or off.",
        "trigger_words": ["wifi on", "wifi on pannu", "turn on wifi", "wifi off", "wifi off pannu"],
        "examples": ["Jarvis wifi on pannu", "turn on wifi Jarvis", "Jarvis wifi off pannu"],
    },
    {
        "intent": "connect_wifi",
        "purpose": "Open Android Wi-Fi connection flow and cache provided password.",
        "trigger_words": ["connect to", "connect pannu", "wifi password vandhu", "password is"],
        "examples": [
            "Jarvis connect to homewifi",
            "Jarvis homewifi connect pannu wifi password vandhu kowsalya at 05 k capital letter",
        ],
    },
    {
        "intent": "set_bluetooth_state",
        "purpose": "Open Android Bluetooth settings for turning Bluetooth on or off.",
        "trigger_words": ["bluetooth on", "bluetooth on pannu", "turn on bluetooth", "bluetooth off"],
        "examples": ["Jarvis bluetooth on pannu", "Jarvis bluetooth off pannu"],
    },
    {
        "intent": "connect_bluetooth",
        "purpose": "Open Bluetooth settings to connect a matching device.",
        "trigger_words": ["connect bluetooth", "bluetooth connect pannu", "connect to"],
        "examples": ["Jarvis sony headphones bluetooth connect pannu", "Jarvis connect to car bluetooth"],
    },
    {
        "intent": "set_flashlight",
        "purpose": "Turn Android flashlight on or off.",
        "trigger_words": ["turn on flashlight", "flashlight off", "torch on", "torch podu", "torch niruthu"],
        "examples": ["Jarvis turn on flashlight", "Jarvis torch podu", "Jarvis torch niruthu"],
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
    {
        "intent": "sleep_assistant",
        "purpose": "Make Jarvis ignore commands until the wake name is called again.",
        "trigger_words": ["turn off", "go offline", "sleep", "thoongu"],
        "examples": ["Jarvis turn off", "Jarvis sleep"],
    },
]


def command_catalog() -> dict[str, object]:
    return {
        "wake_names": "Dynamic. Default is jarvis. Wake name can be at the start or end of a command.",
        "language_hints": ["pannu", "pannunga", "ku", "la", "pesu", "koopidu", "thirakku", "enna", "ena"],
        "commands": COMMAND_CATALOG,
    }
