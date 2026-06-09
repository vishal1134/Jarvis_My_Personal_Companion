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

    return f"I did not understand that yet, {title}."
