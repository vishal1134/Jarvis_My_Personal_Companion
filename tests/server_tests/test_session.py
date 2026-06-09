import tempfile
import unittest
from datetime import datetime, timezone
from pathlib import Path

from server.brain.session import handle_text
from server.memory.store import MemoryStore


class SessionTest(unittest.TestCase):
    def setUp(self):
        self.temp_dir = tempfile.TemporaryDirectory()
        self.store = MemoryStore(Path(self.temp_dir.name) / "memory.json")

    def tearDown(self):
        self.temp_dir.cleanup()

    def test_first_interaction_gets_daily_greeting(self):
        result = handle_text(
            "Jarvis call appa",
            store=self.store,
            now=datetime(2026, 6, 8, 8, 0, tzinfo=timezone.utc),
        )

        self.assertEqual(result["greeting"], "Good morning, sir.")
        self.assertEqual(result["spoken_response"], "Good morning, sir. Calling appa, sir.")

    def test_second_interaction_same_day_skips_greeting(self):
        now = datetime(2026, 6, 8, 8, 0, tzinfo=timezone.utc)

        handle_text("Jarvis call appa", store=self.store, now=now)
        result = handle_text("Jarvis open whatsapp", store=self.store, now=now)

        self.assertIsNone(result["greeting"])
        self.assertEqual(result["spoken_response"], "Opening whatsapp, sir.")

    def test_response_language_can_be_changed(self):
        now = datetime(2026, 6, 8, 8, 0, tzinfo=timezone.utc)

        handle_text("reply in tamil", store=self.store, now=now)
        result = handle_text("Jarvis call appa", store=self.store, now=now)

        self.assertEqual(result["command"]["response_language"], "ta")
        self.assertEqual(result["spoken_response"], "appa ku call pannuren, sir.")

    def test_frequent_command_cache_updates(self):
        now = datetime(2026, 6, 8, 8, 0, tzinfo=timezone.utc)

        handle_text("Jarvis call appa", store=self.store, now=now)
        handle_text("call appa", store=self.store, now=now)

        memory = self.store.load()
        cached = memory["frequent_commands"]["call_contact:appa"]
        self.assertEqual(cached["usage_count"], 2)
        self.assertIn("Jarvis call appa", cached["phrase_variants"])
        self.assertIn("call appa", cached["phrase_variants"])

    def test_change_assistant_name_tracks_old_name(self):
        now = datetime(2026, 6, 8, 8, 0, tzinfo=timezone.utc)

        result = handle_text("Jarvis change your name to Friday", store=self.store, now=now)

        memory = self.store.load()
        self.assertEqual(memory["identity"]["assistant"]["active_names"], ["friday"])
        self.assertIn("jarvis", memory["identity"]["assistant"]["inactive_old_names"])
        self.assertIn("you can call me friday now", result["spoken_response"].lower())

    def test_old_assistant_name_gets_reminder(self):
        now = datetime(2026, 6, 8, 8, 0, tzinfo=timezone.utc)

        handle_text("Jarvis change your name to Friday", store=self.store, now=now)
        result = handle_text("Jarvis", store=self.store, now=now)

        self.assertEqual(result["command"]["intent"], "old_assistant_name_used")
        self.assertEqual(result["spoken_response"], "You have changed my name to friday, sir.")

    def test_add_assistant_name_limit(self):
        now = datetime(2026, 6, 8, 8, 0, tzinfo=timezone.utc)

        handle_text("Jarvis add Friday as your name", store=self.store, now=now)
        handle_text("Jarvis add Tony as your name", store=self.store, now=now)
        result = handle_text("Jarvis add Buddy as your name", store=self.store, now=now)

        memory = self.store.load()
        self.assertEqual(memory["identity"]["assistant"]["active_names"], ["jarvis", "friday", "tony"])
        self.assertEqual(result["spoken_response"], "I can keep only three active names, sir.")

    def test_cached_command_is_used_for_repeated_phrase(self):
        now = datetime(2026, 6, 8, 8, 0, tzinfo=timezone.utc)

        handle_text("Jarvis open whatsapp", store=self.store, now=now)
        result = handle_text("Jarvis open whatsapp", store=self.store, now=now)

        self.assertEqual(result["command"]["intent"], "open_app")
        self.assertEqual(result["command"]["slots"]["cache_hit"], "true")


if __name__ == "__main__":
    unittest.main()
