<div align="center">
  <img src="https://raw.githubusercontent.com/MM2-0/Kvaesitso/main/assets/icons/ic_launcher.png" width="128" alt="AI Launcher Icon">

  # AI Launcher

  **A search-focused, AI-powered, free and open source Android launcher**

  [![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
  [![API 26+](https://img.shields.io/badge/API-26%2B-brightgreen.svg)](https://android-arsenal.com/api?level=26)
  [![Kotlin](https://img.shields.io/badge/Kotlin-2.3-purple.svg)](https://kotlinlang.org)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.11-4285F4.svg)](https://developer.android.com/jetpack/compose)
  [![Build](https://img.shields.io/badge/build-Gradle%20KTS-02303A.svg)](https://gradle.org)

  [Documentation](https://kvaesitso.mm20.de) · [Report Bug](https://github.com/MM2-0/Kvaesitso/issues) · [Request Feature](https://github.com/MM2-0/Kvaesitso/issues)

</div>

---

## 📖 Overview

AI Launcher is an enhanced fork of [Kvaesitso](https://kvaesitso.mm20.de) — a search-focused Android launcher — augmented with **multi-provider AI capabilities**. It integrates LLM-powered features directly into your home screen, including an **AI chat assistant**, **AI-powered search results**, and **proactive app suggestions**, all while maintaining the privacy-first, open-source philosophy of the original project.

### ✨ What Makes This Different

| Feature | Description |
|---------|-------------|
| 🤖 **AI Chat Assistant** | Full conversational assistant embedded in the launcher with streaming responses, conversation history, and device action execution |
| 🔍 **AI-Powered Search** | Ask natural language questions directly in the search bar and get inline AI answers alongside local results |
| 💡 **Proactive Suggestions** | AI analyzes your app usage patterns and context (time, calendar) to suggest apps you're likely to open next |
| 🔌 **Multi-Provider Support** | Bring your own API key — supports **Google Gemini**, **OpenAI (GPT-4o)**, **Anthropic (Claude)**, and a stub for future on-device models |
| 🛡️ **Privacy-First** | API keys stored in Android EncryptedSharedPreferences; all data stays on-device unless you opt in to AI features |

---

## 🏗️ Architecture

The project follows a **multi-module Gradle architecture** organized into four primary layers:

```
AI-Launcher/
├── app/                    # Application layer
│   ├── app/                # Main Android application module (entry point)
│   └── ui/                 # Jetpack Compose UI layer
├── core/                   # Shared infrastructure
│   ├── base/               # Base classes, icons, search abstractions
│   ├── compat/             # Android version compatibility
│   ├── crashreporter/      # Crash reporting
│   ├── devicepose/         # Device orientation/pose
│   ├── i18n/               # Internationalization (35+ languages)
│   ├── ktx/                # Kotlin extensions
│   ├── permissions/        # Permission management
│   ├── preferences/        # DataStore-backed settings
│   ├── profiles/           # User profiles
│   └── shared/             # Plugin SDK shared types
├── data/                   # Data sources & repositories
│   ├── ai-search/          # 🆕 AI-powered search results
│   ├── applications/       # Installed app indexing
│   ├── appshortcuts/       # App shortcuts
│   ├── calculator/         # Calculator engine
│   ├── calendar/           # Calendar events
│   ├── contacts/           # Contact search
│   ├── currencies/         # Currency conversion
│   ├── customattrs/        # Custom attributes
│   ├── database/           # Room database (conversations, searchables)
│   ├── files/              # File search
│   ├── locations/          # Location search
│   ├── notifications/      # Notification listener
│   ├── plugins/            # Plugin data layer
│   ├── search-actions/     # Custom search actions
│   ├── searchable/         # Unified search abstractions
│   ├── themes/             # Theme engine
│   ├── unitconverter/      # Unit converter
│   ├── weather/            # Weather providers
│   ├── websites/           # Web search
│   ├── widgets/            # Widget management
│   └── wikipedia/          # Wikipedia integration
├── services/               # Business logic & service layer
│   ├── ai/                 # 🆕 AI provider abstraction & implementations
│   ├── ai-assistant/       # 🆕 Chat assistant with tool calling
│   ├── ai-suggestions/     # 🆕 Proactive app suggestions via AI
│   ├── accounts/           # Account management (Nextcloud, etc.)
│   ├── backup/             # Backup/restore
│   ├── badges/             # Notification badges
│   ├── favorites/          # Favorites management
│   ├── feed/               # Feed system
│   ├── global-actions/     # System actions
│   ├── icons/              # Icon pack management
│   ├── music/              # Media playback integration
│   ├── plugins/            # Plugin service layer
│   ├── search/             # Unified search orchestration
│   ├── tags/               # Tagging system
│   └── widgets/            # Widget service layer
├── libs/                   # Internal libraries
│   ├── address-formatter/  # Address formatting
│   ├── material-color-utilities/ # Material You color extraction
│   ├── nextcloud/          # Nextcloud client
│   ├── owncloud/           # OwnCloud client
│   └── webdav/             # WebDAV protocol
├── plugins/
│   └── sdk/                # Plugin SDK (Apache 2.0 licensed)
└── docs/                   # VitePress documentation site
```

---

## 🤖 AI System Deep Dive

### Provider Architecture

The AI system is built around a **pluggable provider interface**:

```kotlin
interface AiProvider {
    suspend fun chat(messages: List<AiMessage>): Flow<String>  // Streaming chat
    suspend fun classify(text: String, labels: List<String>): String?  // Intent classification
    val isConfigured: Boolean
}
```

**Supported Providers:**

| Provider | Model | API Style |
|----------|-------|-----------|
| Google Gemini | `gemini-1.5-flash` | REST + SSE streaming |
| OpenAI | `gpt-4o-mini` | Chat Completions + SSE |
| Anthropic | `claude-3.5-sonnet` | Messages API + SSE |
| Local (stub) | — | Placeholder for future on-device models (e.g., Gemini Nano) |

The `AiProviderManager` selects the active provider based on user preferences and securely stores API keys using Android's `EncryptedSharedPreferences`.

### AI Chat Assistant

The assistant (`services/ai-assistant`) provides a full conversational experience:

- **Streaming responses** — Tokens are emitted in real-time via Kotlin `Flow<String>`
- **Conversation persistence** — Messages are stored in Room database with full history
- **Context-aware** — Automatically injects current date/time, foreground app, and upcoming calendar events
- **Tool calling** — The AI can execute device actions by responding with JSON tool calls:

| Tool | Description |
|------|-------------|
| `launch_app` | Launch an app by package name |
| `search` | Trigger a web search |
| `create_alarm` | Set an alarm with time and label |
| `open_settings` | Open Android system settings |
| `web_search` | Open a Google search in the browser |

### AI-Powered Search

The `data/ai-search` module integrates AI answers directly into the launcher search bar:

1. User types a query ≥ 5 characters
2. Local results are shown immediately
3. In parallel, the query is sent to the configured AI provider
4. Streaming AI answer appears as an `AiSearchResult` card alongside other results
5. Non-question inputs are filtered out (the AI responds with `NOT_A_QUESTION`)

### Proactive Suggestions

The `services/ai-suggestions` module uses `WorkManager` to periodically:

1. Analyze the user's top-20 most-used apps (by launch frequency)
2. Send the ranked app list + current time context to the AI provider
3. Parse the AI's JSON response for up to 3 suggested apps with reasons
4. Surface suggestions on the home screen

---

## 🔧 Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.3 |
| **UI Framework** | Jetpack Compose (Material 3 + Material 3 Adaptive) |
| **Architecture** | Multi-module Gradle, MVVM, Repository pattern |
| **DI** | Koin 4.2 |
| **Networking** | Ktor 3.4 (OkHttp engine) |
| **Database** | Room 2.8 |
| **Background Work** | WorkManager |
| **Async** | Kotlin Coroutines + Flow |
| **Serialization** | Kotlinx Serialization |
| **Image Loading** | Coil 2.7 |
| **Effects** | Haze (glassmorphism/blur) |
| **Documentation** | VitePress + Dokka |
| **CI/CD** | GitHub Actions (nightly builds, docs deployment) |
| **Distribution** | F-Droid, GitHub Releases |
| **Min SDK** | API 26 (Android 8.0) |
| **Target SDK** | API 36 |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Meerkat (2025.1) or later
- **JDK 17** (required by Gradle)
- **Android SDK** with API 36 installed

### Build & Run

```bash
# Clone the repository
git clone https://github.com/nihalpj/AI-Launcher.git
cd AI-Launcher

# Build the debug variant
./gradlew assembleDefaultDebug

# Install on a connected device
./gradlew installDefaultDebug
```

### Build Types

| Variant | App ID Suffix | Description |
|---------|---------------|-------------|
| `debug` | `.debug` | Development with debuggable flag |
| `release` | `.release` | Minified + R8 shrinking |
| `nightly` | `.nightly` | Auto-dated nightly with CI signing |

### Product Flavors

| Flavor | Description |
|--------|-------------|
| `default` | Full-featured build with all integrations |
| `fdroid` | F-Droid build (non-free APIs removed) |

---

## ⚙️ Configuration

### Setting Up AI Providers

1. Open **AI Launcher** → **Settings** → **AI Settings**
2. Select your preferred AI provider (Gemini, OpenAI, or Anthropic)
3. Enter your API key (securely stored on-device)
4. Toggle features:
   - **AI Search** — Enable AI answers in the search bar
   - **AI Assistant** — Enable the chat assistant
   - **Proactive Suggestions** — Enable AI-powered app suggestions

### API Key Sources

| Provider | Get API Key |
|----------|-------------|
| Google Gemini | [Google AI Studio](https://aistudio.google.com/apikey) |
| OpenAI | [OpenAI Platform](https://platform.openai.com/api-keys) |
| Anthropic | [Anthropic Console](https://console.anthropic.com/) |

---

## 🌍 Localization

AI Launcher supports **35+ languages** via Crowdin, including gender-variant translations for applicable languages.

<details>
<summary>Supported Languages</summary>

Arabic, Azerbaijani, Basque, Belarusian, Bengali, Catalan, Chinese (Simplified & Traditional), Czech, Danish, Dutch, Esperanto, Finnish, French, German, Greek, Hebrew, Hindi, Hungarian, Indonesian, Italian, Japanese, Korean, Lithuanian, Malay, Norwegian Bokmål, Persian, Polish, Portuguese (Brazil & Portugal), Romanian, Russian, Spanish, Swedish, Thai, Turkish, Ukrainian, Vietnamese

</details>

Want to contribute translations? See the [translation guide](https://kvaesitso.mm20.de/docs/contributor-guide/i18n).

<a href="https://i18n.mm20.de/engage/kvaesitso/">
<img src="https://i18n.mm20.de/widgets/kvaesitso/-/287x66-grey.png" alt="Translation Status">
</a>

---

## 🔌 Plugin System

AI Launcher features a **plugin SDK** (Apache 2.0 licensed) that allows third-party developers to extend search functionality with custom data sources. The SDK provides shared types and abstractions for building external search providers.

- **Plugin SDK**: `plugins/sdk` — Apache 2.0 License
- **Shared types**: `core/shared` — Apache 2.0 License
- **API documentation** auto-generated via Dokka

---

## 🧪 CI/CD

The project uses GitHub Actions for automated workflows:

| Workflow | Trigger | Description |
|----------|---------|-------------|
| **Build Nightly** | Daily at 04:00 UTC + manual | Builds a signed nightly APK for testing |
| **Deploy Docs** | Push to `main` (docs/SDK changes) | Builds VitePress docs + Dokka API reference and deploys to GitHub Pages |
| **F-Droid Repo** | Manual trigger | Triggers F-Droid repository rebuild |

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Bug fixes & small features** — Open a PR directly
2. **Larger features** — Create an issue first to discuss the approach
3. **Translations** — Help via [Crowdin](https://i18n.mm20.de/engage/kvaesitso/)

### Development Tips

- The project uses **Gradle Configuration Cache** for faster builds
- Enable parallel builds with `org.gradle.parallel=true` (already configured)
- Use `./gradlew assembleDefaultDebug` for the fastest development iteration
- AI modules are under `services/ai*` and `data/ai-search`

---

## 📜 License

This project is licensed under the **GNU General Public License v3.0**.

```
Copyright (C) 2021–2026 MM2-0 and the Kvaesitso contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
```

> **Note:** The Plugin SDK modules (`plugins/sdk` and `core/shared`) are licensed under the **Apache License 2.0**.

---

## 🙏 Acknowledgments

- [Kvaesitso](https://github.com/MM2-0/Kvaesitso) — The original open-source launcher this project is built upon
- [@EliotAku](https://github.com/EliotAku) — App icon design
- All [translators and contributors](https://github.com/MM2-0/Kvaesitso/graphs/contributors)

---

<div align="center">

**⭐ Star this repo if you find it useful! ⭐**

Made with ❤️ for the Android open-source community

</div>
