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

    def test_youtube_search_tanglish_song_name(self):
        command = parse_command("jarvis youtube la munbe vaa search pannu")

        self.assertEqual(command.intent, "search_youtube")
        self.assertEqual(command.target, "munbe vaa")

    def test_youtube_search_tanglish_without_pannu(self):
        command = parse_command("jarvis youtube la cooking video search")

        self.assertEqual(command.intent, "search_youtube")
        self.assertEqual(command.target, "cooking video")

    def test_set_response_language(self):
        command = parse_command("reply in tamil")

        self.assertEqual(command.intent, "set_response_language")
        self.assertEqual(command.response_language, "ta")

    def test_read_notifications(self):
        command = parse_command("Jarvis read notifications")

        self.assertEqual(command.intent, "read_notifications")

    def test_wake_assistant(self):
        command = parse_command("Jarvis")

        self.assertEqual(command.intent, "wake_assistant")

    def test_tamil_wake_assistant(self):
        command = parse_command("Jarvis irukiya")

        self.assertEqual(command.intent, "wake_assistant")
        self.assertEqual(command.detected_language, "ta-en")

    def test_help_command(self):
        command = parse_command("Jarvis what can you do")

        self.assertEqual(command.intent, "list_commands")

    def test_tamil_help_command(self):
        command = parse_command("Jarvis enna panna mudiyum")

        self.assertEqual(command.intent, "list_commands")

    def test_repeat_command(self):
        command = parse_command("Jarvis repeat")

        self.assertEqual(command.intent, "repeat_last_response")

    def test_tamil_repeat_command(self):
        command = parse_command("Jarvis marubadi sollu")

        self.assertEqual(command.intent, "repeat_last_response")

    def test_time_command(self):
        command = parse_command("Jarvis what time is it")

        self.assertEqual(command.intent, "get_time")

    def test_tamil_time_command(self):
        command = parse_command("Jarvis neram enna")

        self.assertEqual(command.intent, "get_time")

    def test_wake_name_at_end_for_time(self):
        command = parse_command("time ena jarvis")

        self.assertEqual(command.intent, "get_time")

    def test_wifi_settings_command(self):
        command = parse_command("Jarvis wifi settings")

        self.assertEqual(command.intent, "open_system_settings")
        self.assertEqual(command.target, "wifi")

    def test_flashlight_command(self):
        command = parse_command("Jarvis turn on flashlight")

        self.assertEqual(command.intent, "set_flashlight")
        self.assertEqual(command.target, "on")

    def test_tamil_flashlight_command(self):
        command = parse_command("Jarvis torch podu")

        self.assertEqual(command.intent, "set_flashlight")
        self.assertEqual(command.target, "on")

    def test_tanglish_flashlight_on_command(self):
        command = parse_command("jarvis torch on pannu")

        self.assertEqual(command.intent, "set_flashlight")
        self.assertEqual(command.target, "on")

    def test_tanglish_flashlight_off_command_with_wake_name_at_end(self):
        command = parse_command("torch off pannu jarvis")

        self.assertEqual(command.intent, "set_flashlight")
        self.assertEqual(command.target, "off")

    def test_tanglish_notification_command_with_wake_name_at_end(self):
        command = parse_command("notification ena jarvis")

        self.assertEqual(command.intent, "read_notifications")

    def test_whatsapp_notification_query_tanglish(self):
        command = parse_command("whatsapp la yaaru enaku message pannirukaa jarvis")

        self.assertEqual(command.intent, "query_notifications")
        self.assertEqual(command.target, "whatsapp")

    def test_whatsapp_notification_query_english(self):
        command = parse_command("jarvis whom have messaged me in whatsapp")

        self.assertEqual(command.intent, "query_notifications")
        self.assertEqual(command.target, "whatsapp")

    def test_tanglish_wifi_command_with_flexible_order(self):
        command = parse_command("wifi open pannu jarvis")

        self.assertEqual(command.intent, "open_system_settings")
        self.assertEqual(command.target, "wifi")

    def test_wifi_on_command_with_wake_name_at_end(self):
        command = parse_command("turn on wifi jarvis")

        self.assertEqual(command.intent, "set_wifi_state")
        self.assertEqual(command.target, "on")

    def test_tanglish_wifi_on_command(self):
        command = parse_command("jarvis wifi on pannu")

        self.assertEqual(command.intent, "set_wifi_state")
        self.assertEqual(command.target, "on")

    def test_wifi_connect_with_password(self):
        command = parse_command("jarvis kowsalya connect pannu wifi password vandhu kowsalya at 05 k capital letter")

        self.assertEqual(command.intent, "connect_wifi")
        self.assertEqual(command.target, "kowsalya")
        self.assertEqual(command.slots["password"], "Kowsalya@05")

    def test_wifi_connect_with_hash_and_dollar_password(self):
        command = parse_command("jarvis home connect pannu wifi password vandhu home hash 12 dollar")

        self.assertEqual(command.intent, "connect_wifi")
        self.assertEqual(command.target, "home")
        self.assertEqual(command.slots["password"], "home#12$")

    def test_sleep_assistant_command(self):
        command = parse_command("jarvis turn off")

        self.assertEqual(command.intent, "sleep_assistant")

    def test_bluetooth_on_command(self):
        command = parse_command("jarvis bluetooth on pannu")

        self.assertEqual(command.intent, "set_bluetooth_state")
        self.assertEqual(command.target, "on")

    def test_bluetooth_connect_command(self):
        command = parse_command("jarvis sony headphones bluetooth connect pannu")

        self.assertEqual(command.intent, "connect_bluetooth")
        self.assertEqual(command.target, "sony headphones")

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
