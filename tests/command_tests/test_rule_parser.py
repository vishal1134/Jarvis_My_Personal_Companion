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

    def test_set_response_language(self):
        command = parse_command("reply in tamil")

        self.assertEqual(command.intent, "set_response_language")
        self.assertEqual(command.response_language, "ta")

    def test_language_detection(self):
        self.assertEqual(detect_language("hello"), "en")
        self.assertEqual(detect_language("appa ku call pannu"), "ta-en")


if __name__ == "__main__":
    unittest.main()

