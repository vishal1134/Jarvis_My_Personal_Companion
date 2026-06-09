import unittest

from server.brain.rule_parser import detect_language, parse_command


class RuleParserTest(unittest.TestCase):
    def test_call_contact_english(self):
        command = parse_command("Jarvis call appa")

        self.assertEqual(command.intent, "call_contact")
        self.assertEqual(command.target, "appa")
        self.assertEqual(command.detected_language, "en")

    def test_call_contact_mixed_tamil_english(self):
        command = parse_command("appa ku call pannu")

        self.assertEqual(command.intent, "call_contact")
        self.assertEqual(command.target, "appa")
        self.assertEqual(command.detected_language, "ta-en")

    def test_open_app_mixed_tamil_english(self):
        command = parse_command("whatsapp open pannu")

        self.assertEqual(command.intent, "open_app")
        self.assertEqual(command.target, "whatsapp")

    def test_open_calculator(self):
        command = parse_command("Jarvis open calculator")

        self.assertEqual(command.intent, "open_app")
        self.assertEqual(command.target, "calculator")

    def test_youtube_search_english(self):
        command = parse_command("Jarvis search vijay songs in youtube")

        self.assertEqual(command.intent, "search_youtube")
        self.assertEqual(command.target, "vijay songs")

    def test_youtube_search_mixed_tamil_english(self):
        command = parse_command("Jarvis youtube la tamil songs search pannu")

        self.assertEqual(command.intent, "search_youtube")
        self.assertEqual(command.target, "tamil songs")

    def test_set_response_language(self):
        command = parse_command("reply in tamil")

        self.assertEqual(command.intent, "set_response_language")
        self.assertEqual(command.response_language, "ta")

    def test_read_notifications(self):
        command = parse_command("Jarvis read notifications")

        self.assertEqual(command.intent, "read_notifications")

    def test_change_assistant_name(self):
        command = parse_command("Jarvis change your name to Friday")

        self.assertEqual(command.intent, "change_assistant_name")
        self.assertEqual(command.target, "friday")

    def test_add_assistant_name(self):
        command = parse_command("Jarvis add Friday as your name")

        self.assertEqual(command.intent, "add_assistant_name")
        self.assertEqual(command.target, "friday")

    def test_language_detection(self):
        self.assertEqual(detect_language("hello"), "en")
        self.assertEqual(detect_language("appa ku call pannu"), "ta-en")


if __name__ == "__main__":
    unittest.main()
