import unittest

from server.voice.style import prepare_for_speech


class VoiceStyleTest(unittest.TestCase):
    def test_prepare_for_speech_collapses_whitespace(self):
        self.assertEqual(
            prepare_for_speech("  Good afternoon,   sir.  Calling appa, sir. "),
            "Good afternoon, sir. Calling appa, sir.",
        )


if __name__ == "__main__":
    unittest.main()

