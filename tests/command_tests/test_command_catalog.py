import unittest

from server.brain.command_catalog import command_catalog


class CommandCatalogTest(unittest.TestCase):
    def test_catalog_lists_current_intents(self):
        catalog = command_catalog()
        intents = {command["intent"] for command in catalog["commands"]}

        self.assertIn("call_contact", intents)
        self.assertIn("open_app", intents)
        self.assertIn("search_youtube", intents)
        self.assertIn("read_notifications", intents)
        self.assertIn("change_assistant_name", intents)


if __name__ == "__main__":
    unittest.main()

