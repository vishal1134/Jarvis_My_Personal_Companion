from __future__ import annotations

from datetime import datetime


def greeting_for(now: datetime, response_language: str, title: str) -> str:
    hour = now.hour

    if response_language == "ta":
        if 5 <= hour < 12:
            return f"Kaalai vanakkam, {title}."
        if 12 <= hour < 17:
            return f"Mathiya vanakkam, {title}."
        if 17 <= hour < 21:
            return f"Maalai vanakkam, {title}."
        return f"Iravu vanakkam, {title}."

    if response_language == "ta-en":
        if 5 <= hour < 12:
            return f"Good morning, {title}. Kaalai vanakkam."
        if 12 <= hour < 17:
            return f"Good afternoon, {title}. Mathiya vanakkam."
        if 17 <= hour < 21:
            return f"Good evening, {title}. Maalai vanakkam."
        return f"Good night, {title}. Iravu vanakkam."

    if 5 <= hour < 12:
        return f"Good morning, {title}."
    if 12 <= hour < 17:
        return f"Good afternoon, {title}."
    if 17 <= hour < 21:
        return f"Good evening, {title}."
    return f"Good night, {title}."


def response_for(command: dict[str, object], title: str) -> str:
    intent = command["intent"]
    target = command.get("target")
    language = command["response_language"]

    if intent == "call_contact":
        if language == "ta":
            return f"{target} ku call pannuren, {title}."
        if language == "ta-en":
            return f"{target} ku call pannuren, {title}."
        return f"Calling {target}, {title}."

    if intent == "open_app":
        if language == "ta":
            return f"{target} open pannuren, {title}."
        if language == "ta-en":
            return f"{target} open pannuren, {title}."
        return f"Opening {target}, {title}."

    if intent == "search_youtube":
        if language in {"ta", "ta-en"}:
            return f"YouTube la {target} search pannuren, {title}."
        return f"Searching YouTube for {target}, {title}."

    if intent == "set_response_language":
        if target == "ta":
            return f"Sari {title}, inimey Tamil la reply pannuren."
        if target == "en":
            return f"Of course, {title}. I will respond in English."

    if intent == "read_notifications":
        if language == "ta":
            return f"Notifications check pannuren, {title}."
        if language == "ta-en":
            return f"Notifications check pannuren, {title}."
        return f"Checking notifications, {title}."

    if intent == "query_notifications":
        if language in {"ta", "ta-en"}:
            return f"{target} notifications check pannuren, {title}."
        return f"Checking {target} notifications, {title}."

    if intent == "wake_assistant":
        if language in {"ta", "ta-en"}:
            return f"Naan inga irukken, {title}. Unga command ku ready."
        return f"At your command, {title}."

    if intent == "list_commands":
        if language in {"ta", "ta-en"}:
            return (
                f"Naan contacts call pannalam, apps open pannalam, YouTube search pannalam, "
                f"notifications summary sollalam, settings open pannalam, flashlight control pannalam, "
                f"time date sollalam, last response repeat pannalam, en names manage pannalam, {title}."
            )
        return (
            f"I can call contacts, open apps, search YouTube, read notification summaries, "
            f"open phone settings, control flashlight, tell time, repeat my last response, "
            f"and manage my names, {title}."
        )

    if intent == "repeat_last_response":
        repeat_text = command.get("slots", {}).get("repeat_text")
        if repeat_text:
            return repeat_text
        if language in {"ta", "ta-en"}:
            return f"Repeat panna edhuvum illa, {title}."
        return f"I do not have anything to repeat yet, {title}."

    if intent == "stop_speaking":
        if language in {"ta", "ta-en"}:
            return f"Niruthuren, {title}."
        return f"Stopping, {title}."

    if intent == "get_time":
        current_time = command.get("slots", {}).get("current_time", "")
        if language in {"ta", "ta-en"}:
            return f"Ippo time {current_time}, {title}."
        return f"It is {current_time}, {title}."

    if intent == "get_date":
        current_date = command.get("slots", {}).get("current_date", "")
        if language in {"ta", "ta-en"}:
            return f"Innaiku {current_date}, {title}."
        return f"Today is {current_date}, {title}."

    if intent == "open_system_settings":
        if target == "wifi":
            if language in {"ta", "ta-en"}:
                return f"Wi-Fi settings open pannuren, {title}."
            return f"Opening Wi-Fi settings, {title}."
        if target == "bluetooth":
            if language in {"ta", "ta-en"}:
                return f"Bluetooth settings open pannuren, {title}."
            return f"Opening Bluetooth settings, {title}."
        if target == "notifications":
            if language in {"ta", "ta-en"}:
                return f"Notification settings open pannuren, {title}."
            return f"Opening notification settings, {title}."
        if target == "accessibility":
            if language in {"ta", "ta-en"}:
                return f"Accessibility settings open pannuren, {title}."
            return f"Opening accessibility settings, {title}."
        if language in {"ta", "ta-en"}:
            return f"Settings open pannuren, {title}."
        return f"Opening settings, {title}."

    if intent == "set_flashlight":
        if target == "on":
            if language in {"ta", "ta-en"}:
                return f"Flashlight on pannuren, {title}."
            return f"Turning on flashlight, {title}."
        if language in {"ta", "ta-en"}:
            return f"Flashlight off pannuren, {title}."
        return f"Turning off flashlight, {title}."

    if intent == "set_wifi_state":
        if target == "on":
            if language in {"ta", "ta-en"}:
                return f"Wi-Fi on panna settings open pannuren, {title}."
            return f"Opening Wi-Fi settings to turn it on, {title}."
        if language in {"ta", "ta-en"}:
            return f"Wi-Fi off panna settings open pannuren, {title}."
        return f"Opening Wi-Fi settings to turn it off, {title}."

    if intent == "set_bluetooth_state":
        if target == "on":
            if language in {"ta", "ta-en"}:
                return f"Bluetooth on panna settings open pannuren, {title}."
            return f"Opening Bluetooth settings to turn it on, {title}."
        if language in {"ta", "ta-en"}:
            return f"Bluetooth off panna settings open pannuren, {title}."
        return f"Opening Bluetooth settings to turn it off, {title}."

    if intent == "connect_bluetooth":
        if language in {"ta", "ta-en"}:
            return f"{target} Bluetooth device connect panna settings open pannuren, {title}."
        return f"Opening Bluetooth settings to connect {target}, {title}."

    if intent == "connect_wifi":
        has_password = bool(command.get("slots", {}).get("password"))
        if has_password:
            if language in {"ta", "ta-en"}:
                return f"{target} Wi-Fi connect panna try pannuren, {title}."
            return f"Trying to connect to {target} Wi-Fi, {title}."
        if language in {"ta", "ta-en"}:
            return f"{target} Wi-Fi open pannuren. Password venum, {title}."
        return f"Opening {target} Wi-Fi. I need the password, {title}."

    if intent == "sleep_assistant":
        if language in {"ta", "ta-en"}:
            return f"Sari {title}. Neenga koopidum varai silent ah iruppen."
        return f"Going quiet, {title}. Call my name when you need me."

    if intent == "change_assistant_name":
        if language in {"ta", "ta-en"}:
            return f"Sari {title}, inimey ennai {target} nu koopidunga."
        return f"Okay {title}, you can call me {target} now."

    if intent == "add_assistant_name":
        success = command.get("slots", {}).get("name_update_success") != "false"
        if not success:
            return f"I can keep only three active names, {title}."
        if language in {"ta", "ta-en"}:
            return f"Sari {title}, {target} name um add panniten."
        return f"Okay {title}, I will also respond to {target}."

    if intent == "remove_assistant_name":
        success = command.get("slots", {}).get("name_update_success") != "false"
        if not success:
            return f"I could not remove that name, {title}."
        if language in {"ta", "ta-en"}:
            return f"Sari {title}, {target} name remove panniten."
        return f"Okay {title}, I removed {target} from my active names."

    if intent == "old_assistant_name_used":
        active_name = command.get("slots", {}).get("active_name", "jarvis")
        return f"You have changed my name to {active_name}, {title}."

    return f"I did not understand that yet, {title}."
