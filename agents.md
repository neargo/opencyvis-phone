# agents.md

This file consolidates the shared “how to build / run / configure / report issues” snippets that are repeated across the repo.

## Quick Start (Emulator)

- Deploy (AOSP emulator): `./scripts/deploy-emu.sh`
- Open Control Panel: `adb -s emulator-5554 shell "am start -n ai.opencyvis/.ui.ControlPanelActivity"`
- Send a task (demo broadcast):
  - `adb -s emulator-5554 shell "am broadcast -a ai.opencyvis.TEST -p ai.opencyvis --es demo '<YOUR_TASK>'"`

Notes:
- For “AOSP device / ROM integration” and platform signing, use `docs/aosp-deployment.md`.

## Configure LLM Provider

You can configure in-app, or via a deeplink:

- Local Ollama (no API key):
  - `adb shell am start -a android.intent.action.VIEW -d "opencyvis://config?provider=ollama&base_url=http://localhost:11434&model=<MODEL>"`
- Cloud OpenAI-compatible API:
  - `adb shell am start -a android.intent.action.VIEW -d "opencyvis://config?provider=openai&base_url=<BASE_URL>&api_key=<KEY>&model=<MODEL>"`

Common parameters:
- `provider`: `openai` | `anthropic` | `ollama`
- `base_url`: API base URL (e.g. `https://api.openai.com/v1` or your proxy)
- `api_key`: API key (not needed for `ollama`)
- `model`: model name

## Build

- Android (dev): `cd android && ./gradlew assembleDebug`
- Android (release): `cd android && ./gradlew assembleRelease`

## Tests

- Android unit tests: `cd android && ./gradlew testDebugUnitTest`
- Python tests: `python -m pytest tests/ -v`

## Reporting Issues

When filing a GitHub Issue, include:
- Device model + Android version
- Steps to reproduce
- Expected vs actual behavior
- Logs (logcat) and/or screenshots if available

## Security

If you find a security vulnerability, prefer private disclosure (do not post full exploit details in a public issue).
