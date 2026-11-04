"""Offline adapter checks: no SDK installation, API key, or network is required."""
import importlib
import json
import sys
import unittest
from types import SimpleNamespace
from unittest.mock import Mock, patch


class FakeAPITimeoutError(Exception):
    pass


class InferenceAdapterTest(unittest.TestCase):
    def setUp(self):
        self.factory = Mock()
        self.http = Mock()
        with patch.dict(sys.modules, {
            "openai": SimpleNamespace(OpenAI=self.factory, APITimeoutError=FakeAPITimeoutError),
            "httpx": SimpleNamespace(Client=self.http),
            "requests": Mock(),
        }):
            sys.modules.pop("openai_model.train_model", None)
            self.module = importlib.import_module("openai_model.train_model")
        self.client = self.factory.return_value
        self.response = SimpleNamespace(
            model="snapshot", usage=SimpleNamespace(model_dump=lambda **_: {"prompt_tokens": 12, "completion_tokens": 3, "total_tokens": 15}),
            choices=[SimpleNamespace(finish_reason="stop", message=SimpleNamespace(content='{"ok":true}', refusal=None))])
        self.client.chat.completions.create.return_value = self.response

    def call(self, **options):
        return json.loads(self.module.use_model(json.dumps({"openAiSecretKey": "test-private-key", "model": "test-model",
                    "messages": [{"role": "user", "content": "requirements"}], **options})))

    def test_optional_temperature_and_reasoning_are_not_silently_substituted(self):
        result = self.call(temperature=None, reasoningEffort="low", maxOutputTokens=24000)
        sent = self.client.chat.completions.create.call_args.kwargs
        self.assertNotIn("temperature", sent)
        self.assertNotIn("max_tokens", sent)
        self.assertEqual(24000, sent["max_completion_tokens"])
        self.assertEqual("low", sent["reasoning_effort"])
        self.assertEqual(0, self.factory.call_args.kwargs["max_retries"])
        self.assertNotIn("test-private-key", json.dumps(result))
        self.http.return_value.close.assert_called_once()

    def test_explicit_temperature_and_provider_finish_are_retained(self):
        self.response.choices[0].finish_reason = "length"
        result = self.call(temperature=0.4)
        self.assertEqual(0.4, result["request_parameters"]["temperature"])
        self.assertEqual("length", result["finish_reason"])
        self.assertEqual("length", result["diagnostics"]["finish_reason"])
        self.assertEqual(3, result["diagnostics"]["completion_tokens"])
        self.assertEqual(15, result["usage"]["total_tokens"])

    def test_refusal_is_retained_for_the_research_ledger(self):
        self.response.choices[0].message.refusal = "test refusal"
        self.response.choices[0].message.content = None
        self.assertEqual("test refusal", self.call()["refusal"])

    def test_provider_error_is_not_retried_or_allowed_to_echo_the_key(self):
        self.client.chat.completions.create.side_effect = RuntimeError("failed test-private-key")
        result = self.call()
        self.assertEqual(1, result["code"])
        self.assertNotIn("test-private-key", result["message"])
        self.client.chat.completions.create.assert_called_once()

    def test_timeout_keeps_diagnostics_without_retrying_or_leaking_input(self):
        error = FakeAPITimeoutError("Request timed out test-private-key")
        error.__cause__ = TimeoutError("underlying transport")
        self.client.chat.completions.create.side_effect = error
        result = self.call()
        self.assertEqual(1, result["code"])
        self.assertEqual("TimeoutError", result["diagnostics"]["cause_type"])
        self.assertEqual("test-model", result["diagnostics"]["requested_model"])
        self.assertEqual(600, result["diagnostics"]["timeout_seconds"])
        self.assertGreaterEqual(result["diagnostics"]["elapsed_ms"], 0)
        self.assertEqual(600.0, self.factory.call_args.kwargs["timeout"])
        self.assertEqual(600.0, self.http.call_args.kwargs["timeout"])
        self.assertIn("600 segundos", result["message"])
        self.assertNotIn("test-private-key", json.dumps(result))
        self.client.chat.completions.create.assert_called_once()
        self.http.return_value.close.assert_called_once()


if __name__ == "__main__":
    unittest.main()
