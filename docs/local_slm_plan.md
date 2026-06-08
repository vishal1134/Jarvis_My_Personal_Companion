# Local SLM Plan

## Decision

Jarvis will not use OpenAI or Gemini APIs for the SLM.

The practical path is to start from a free open-source small language model and
fine-tune it locally for Jarvis commands. Training a model from true zero is
possible in theory, but it is not the best first step for this project because
it needs a lot of data, GPU time, and experimentation before it becomes useful.

## Recommended Model Path

Start with one of these free local model families:

- Qwen2.5 0.5B / 1.5B
- Gemma 2 2B, only if the license and laptop performance fit
- TinyLlama 1.1B
- Phi-style small models, only if license and local tooling fit

For the first useful Jarvis brain, prefer a small instruct model that can run on
CPU or modest GPU. Then fine-tune it with LoRA/QLoRA for command understanding.

## Training Strategy

Phase 1: No model training.

- Use rules and examples for the first commands.
- Store command variants and successful mappings.
- Build the Android action system first.

Phase 2: Local intent model.

- Fine-tune a small model or train a smaller classifier.
- Input: spoken text.
- Output: structured command JSON.

Phase 3: Bilingual command model.

- Add Tamil, English, and Tamil-English examples.
- Train on your own command style.
- Evaluate against a fixed command test set before accepting a model.

Phase 4: Companion response model.

- Generate short bilingual voice responses.
- Keep action decisions separate from friendly wording.

## Dataset Format

Use JSONL for training examples.

```jsonl
{"input":"jarvis call appa","output":{"intent":"call_contact","target":"appa","response_language":"en"}}
{"input":"appa ku call pannu","output":{"intent":"call_contact","target":"appa","response_language":"ta-en"}}
{"input":"tamil la reply pannu","output":{"intent":"set_response_language","language":"ta"}}
```

## Training Pipeline

```text
Upload dataset
  -> validate JSONL
  -> normalize Tamil/English/mixed phrases
  -> split train/test
  -> fine-tune adapter
  -> run command evaluation
  -> save model version
  -> make available to server
  -> optionally export lightweight command cache to phone
```

## Laptop Requirements

CPU-only training is possible for very small experiments, but slow. A laptop with
an NVIDIA GPU is much better for fine-tuning. If there is no GPU, we can still:

- Run smaller quantized models.
- Train lightweight intent classifiers.
- Fine-tune very small adapters slowly.
- Keep most daily command learning as memory/cache instead of full model training.

