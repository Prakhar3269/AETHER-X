# AETHER X — Beyond The Interface

AETHER X is a native Android digital universe platform built with Kotlin, Jetpack Compose, Material 3, Firebase Authentication (Google Sign-In via Jetpack Credential Manager), Cloud Firestore, and Firebase AI Logic (Gemini).

## Core Modules

- **3D Living Universe & Infinity Engine (`ui/universe/`)**: Interactive 3D perspective-projected celestial canvas with pinch-to-zoom, pan, orbit rotation, starfield instancing, glowing cyan neural links between Worlds, selectable planetary nodes, Reduced Motion support, adaptive quality presets, and a 2D Constellation fallback.
- **AETHER Command Center (`ui/screens/CommandCenterScreen.kt`)**: Personalized cosmic greeting, universe telemetry, active Worlds, recent Projects, Tasks, Notifications, AI Civilization status, and a Universal Command Palette searching across Worlds, Projects, Tasks, Conversations, and Team Members.
- **AI Civilization (`data/ai/AetherAiService.kt`, `ui/screens/AgentsScreen.kt`)**: Five specialized Gemini agents (`AETHER ARCHITECT`, `AETHER RESEARCHER`, `AETHER DEVELOPER`, `AETHER STRATEGIST`, `AETHER CREATOR`) with streaming responses plus a Multi-Agent Council orchestrator using structured JSON routing (`Schema.obj`) and consolidated executive synthesis.
- **AETHER VOICE (`ui/voice/AetherVoiceSheet.kt`)**: Optional microphone speech-to-text (`SpeechRecognizer` with runtime `RECORD_AUDIO` permission) and spoken `TextToSpeech` responses alongside a reliable text input fallback.
- **Live Worlds Collaboration (`ui/screens/NetworkScreen.kt`)**: Team creation, invite codes, role-based access (`Owner`, `Administrator`, `Editor`, `Viewer`), and live collaborative activity feeds.
- **Nexus Market (`ui/screens/NexusMarketScreen.kt`)**: Searchable and filterable digital creation catalog with favorites, creator profiles, creation publishing, and 1-tap World launch.

## Firebase & Security

- **Authentication**: Google Sign-In exclusively via `CredentialManager` (`GetSignInWithGoogleOption`).
- **Database**: Named Firestore Enterprise instance configured via `R.string.firestore_database_id` (`app/src/main/res/values/firebase_applet_config.xml`).
- **Security Rules**: Zero-trust schema-validated rules in `firestore.rules` corresponding to `firebase-blueprint.json`.
- **Gemini AI**: Routed through Firebase AI Logic (`Firebase.ai`) and protected by Firebase App Check (`configureAppCheck` in `MainActivity.onCreate()`). No client-side `GEMINI_API_KEY` is required.

## Verification

- JS Security Rules Suite: `FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js`
- Kotlin Repository Rule Tests: `gradle :app:testDebugUnitTest --tests '*Rule*'` (with local Firestore/Auth emulators running on ports 8085/9099)
- Full Debug Unit Tests & Compilation: `gradle :app:testDebugUnitTest` and `compile_applet`
