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


if __name__ == "__main__":
    unittest.main()

