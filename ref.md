<picture>
  <source media="(prefers-color-scheme: dark)" srcset="github-banner-dark.png">
  <source media="(prefers-color-scheme: light)" srcset="github-banner-light.png">
  <img src="github-banner-light.png" alt="OpenCyvis" width="100%">
</picture>

<p align="center">
  <strong>The open-source AI phone.</strong><br>
  Commercial AI phones are black boxes. This one isn't.
</p>

<p align="center">
  <a href="README_CN.md">中文</a> •
  <a href="#getting-started">Getting Started</a> •
  <a href="#roadmap">Roadmap</a> •
  <a href="CONTRIBUTING.md">Contributing</a>
</p>

<p align="center">
  <a href="https://github.com/opencyvis/opencyvis-phone/actions/workflows/build.yml"><img src="https://github.com/opencyvis/opencyvis-phone/actions/workflows/build.yml/badge.svg" alt="Build"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache%202.0-blue.svg" alt="License"></a>
  <a href="https://github.com/opencyvis/opencyvis-phone/stargazers"><img src="https://img.shields.io/github/stars/opencyvis/opencyvis-phone?style=social" alt="Stars"></a>
</p>

---

<!-- TODO: Demo GIF / screenshot showcase -->
<!-- <p align="center">
  <img src="demo.gif" alt="OpenCyvis Demo" width="600">
</p> -->

---

## Why

When a company ships an "AI phone," they get full access to your screen, your apps, your messages, your banking — and you can't see what model is running, can't verify what data leaves your device, can't choose an alternative.

Doubao AI Phone? Locked to ByteDance's model. Samsung Galaxy AI? Locked to Samsung + Google. You get whatever they decide to give you.

**You should at least have the choice.**

OpenCyvis is the open-source alternative: you see every line of code, you pick the AI model, you decide where your data goes. With a local model, nothing ever leaves your device.

---

## What It Does

OpenCyvis turns any compatible Android device into an AI phone. Give it a task in natural language — it sees your screen, understands the UI, and operates apps just like you would.

**"Find the best-rated coffee shop nearby and get directions"** — opens Maps, searches, sorts by rating, taps the top result, starts navigation.

**"Look up flights to Tokyo next Friday — find the cheapest direct one"** — opens the travel site, enters dates, filters direct flights, sorts by price.

**"Set a 7am alarm, turn on Do Not Disturb, and switch to dark mode"** — chains Clock, Settings, and Display in one go.

### It works in the background

Most AI tools lock your screen while they work. OpenCyvis operates on a **virtual display** — an isolated background screen. The AI books your flight while you scroll Twitter.

```
┌─────────────────────┐    ┌─────────────────────┐
│   Your screen        │    │   Virtual display    │
│                      │    │   (AI works here)    │
│   Browse, chat,      │    │                      │
│   watch videos —     │    │   Booking flights,   │
│   phone is yours     │    │   sending messages,  │
│                      │    │   placing orders     │
└─────────────────────┘    └─────────────────────┘
      You use this              AI uses this
```

Watch the AI work anytime. Take over if something looks wrong. Hand it back when you're done. It picks up right where you left off.

---

## How It Compares

| | Commercial AI Phones | Cloud Phones | ADB / Scripting | **OpenCyvis** |
|---|---|---|---|---|
| **Open source** | No | No | Varies | **Yes — 100%** |
| **Choose your AI model** | No — vendor-locked | No | N/A | **Yes — any model** |
| **Data stays on device** | Trust them | No — remote servers | Yes | **Yes — verifiable** |
| **Phone usable while AI works** | Varies | Yes (different phone) | No | **Yes — virtual display** |
| **Works with any app** | Varies | Yes | Needs selectors | **Yes — sees like a human** |
| **No computer needed** | Yes | Yes | No | **Yes** |

---

## Features

- **Background operation** — AI works on a virtual display; your phone stays free
- **Any AI model** — Qwen, Claude, GPT, Llama, Gemma, or run locally with Ollama
- **Natural language** — describe what you want in plain text or voice
- **Visual + structural understanding** — reads both screenshots and the UI element tree
- **Watch & takeover** — observe the AI in real-time, take control anytime, hand back seamlessly
- **Asks when unsure** — pauses on ambiguity ("Which Zhang Wei? I see three") instead of guessing
- **Safety guards** — repeated action detection, confirmation for sensitive operations
- **Offline voice** — on-device speech recognition, no internet needed
- **100% open source** — audit every line

---

## Supported Models

| Provider | Examples | Notes |
|----------|---------|-------|
| OpenAI-compatible | Qwen, GPT | Default — works with any OpenAI-compatible API |
| Anthropic | Claude | Native Anthropic API support |
| Ollama (local) | Llama, Gemma, Qwen | Runs on-device — nothing leaves your phone |

Bring your own API key. Or run a local model and never make a network call.

---

## Privacy & Security

An AI agent with full phone access is one of the most privileged pieces of software you can run. This is not a place for "trust us."

- **Screenshots stay in memory** — never written to disk, never stored
- **You choose the endpoint** — self-hosted, private cloud, or fully local
- **No telemetry, no analytics, no phone-home** — zero tracking code
- **Open source** — security researchers, journalists, anyone can audit
- **Local model option** — use Ollama and nothing leaves the device. Period.

```
Your screen ──→ Screenshot (RAM only) ──→ Your chosen AI ──→ Action
                                           ↑
                              You control this endpoint
```

---

## Getting Started

### Requirements

- AOSP 16 (API 36+) system image
- Platform key signing (system app privileges)

OpenCyvis is currently a privileged system application. It requires system-level access for screen capture and input injection — capabilities that make the AI phone experience possible without root hacks or accessibility service workarounds.

### Build from source

```bash
git clone https://github.com/opencyvis/opencyvis-phone.git
cd opencyvis-phone/android
./gradlew assembleRelease
```

### Deploy to emulator

```bash
./scripts/deploy-emu.sh
```

### Configure

Set your LLM provider in-app, or via deeplink:

```bash
# Local Ollama (fully private, no API key)
adb shell am start -a android.intent.action.VIEW \
  -d "opencyvis://config?provider=ollama&base_url=http://localhost:11434&model=qwen3"

# Cloud API
adb shell am start -a android.intent.action.VIEW \
  -d "opencyvis://config?provider=openai&base_url=https://api.openai.com/v1&api_key=YOUR_KEY&model=qwen-vl-max"
```

> **For detailed AOSP integration** (symlink into tree, device makefile, platform key signing): see [android/README-AOSP.md](android/README-AOSP.md)

---

## Architecture (Brief)

```
┌──────────────────────────────────────────────────┐
│                 AgentService                       │
│                                                    │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐      │
│  │ OBSERVE  │ → │  THINK   │ → │   ACT    │ ─┐   │
│  │ Screen   │   │  LLM     │   │  Input   │  │   │
│  │ Capture  │   │  Call    │   │  Inject  │  │   │
│  └──────────┘   └──────────┘   └──────────┘  │   │
│       ↑                                       │   │
│       └───────────────────────────────────────┘   │
│              (repeat until done)                   │
│                                                    │
│  VirtualDisplay │ Coordinator │ State Machine      │
└──────────────────────────────────────────────────┘
```

The agent sees what you see (screenshot + UI tree), reasons about what to do next (LLM), and acts through the same touch/keyboard inputs you'd use. No app modifications needed. If you can do it on your phone, OpenCyvis can too.

> **Full architecture docs:** [docs/architecture.md](docs/architecture.md)

---

## Roadmap

### Now
- Core agent loop (observe → think → act)
- Virtual display background operation
- Chat interface with step-by-step results
- Real-time view mode with live mirroring
- User takeover with seamless handoff
- Multi-provider LLM support (Qwen, Claude, Ollama)
- Offline voice input (Sherpa-ONNX)
- 100+ unit tests, E2E test framework

### Next
- Lighter installation (no ROM flash required)
- Scheduled and recurring tasks
- Multi-app workflow orchestration
- On-device local model inference
- Voice output and conversational interaction

### Vision
- Cross-device coordination (phone + desktop)
- Community task templates
- Plugin system for domain-specific skills
- Enterprise fleet management

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). We welcome code, bug reports, security audits, translations, and documentation.

## License

[Apache 2.0](LICENSE)

## Acknowledgments

- [Sherpa-ONNX](https://github.com/k2-fsa/sherpa-onnx) — on-device speech recognition (Apache 2.0)
