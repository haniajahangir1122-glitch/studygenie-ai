# StudyGenie AI 🧞📚

StudyGenie AI is a high-performance, visually polished, and securely hardened educational dashboard built with a modern **Japanese Bento-style compartmentalized grid**. It serves as an offline-first study companion, combining local database persistence, secure AI proxying, interactive learning trackers, and advanced user privacy controls.

Developed using **Kotlin**, **Jetpack Compose (Material 3)**, and **Room Database**, StudyGenie AI integrates seamlessly with the **Gemini 3.5 Flash** model via a secure, rate-limited Node.js backend proxy.

---

## 🎨 Design Philosophy & Features

StudyGenie AI utilizes modern UI styling principles featuring high-contrast typography, ample negative space, and a sleek layout structured around functional compartments:

-   **Intelligent Bento Grid Dashboard**: Tracks study courses, active schedules, levels, and progress.
-   **Local Flashcard & Quiz Modules**: Study flashcards, take quizzes, and track your metrics.
-   **Daily Goals & Streak Tracker**: Motivates daily learning with custom quiz and study time targets.
-   **Secure AI Tutor Chat & Smart Tips**: Ask the AI tutor for contextual advice, explanations, or study timelines.
-   **Document Vault**: Keeps track of textbook PDFs and processing history.

---

## 🔒 Security & Privacy Posture (Production-Ready)

This repository is prepared for public GitHub release and has been hardened to prevent security leaks or privacy breaches:

1.  **Zero Exposed Keys**: All API credentials and secret keys are routed through environment variables. No client-side builds contain hardcoded keys.
2.  **Lightweight Secure Proxy**: The Node.js server (`server.js`) acts as a secure forwarding gateway to the Google AI Studio APIs, hiding the `GEMINI_API_KEY` behind backend requests.
3.  **Cross-Site Scripting (XSS) Mitigation**: The proxy server sanitizes and escapes all user inputs before processing or mirroring them to prevent script injection.
4.  **IP Rate Limiting**: Simple, in-memory IP-based rate limiters protect proxy routes against brute-force queries or abuse.
5.  **Robust Security Headers**: The backend proxy enforces standard secure headers:
    -   `X-Frame-Options: DENY` (Anti-Clickjacking)
    -   `X-Content-Type-Options: nosniff` (Anti-MIME sniffing)
    -   `X-XSS-Protection: 1; mode=block` (Anti-XSS)
    -   `Referrer-Policy: no-referrer`
    -   `Content-Security-Policy` (CSP restricted to self-served assets)
6.  **Granular User Privacy Controls**: Users can configure their privacy directly from the Profile settings:
    -   *Anonymize Usage Analytics*: Anonymizes telemetry logs to protect device identity.
    -   *Personal Data Sharing*: Toggles anonymous study progress sharing.
    -   *Public Profile Sharing*: Controls whether study streaks are visible to others.
7.  **GDPR & CCPA Legal Compliance**: Built-in, easily accessible scrollable modals for:
    -   *Privacy Policy*
    -   *Terms & Conditions*
    -   *My Data & Rights Center* (explaining database access, backups, and encryption)
8.  **Complete Account Purging**: Users can trigger permanent account deletion, wiping all cloud records and purging local Room databases completely.
9.  **Firebase Security Blueprints**: Pre-configured `firestore.rules` and `storage.rules` enforce UID-matched access and isolate user notes, files, and chat histories.

---

## 📂 Project Architecture

```
├── app/                        # Android Client Module (Kotlin/Compose)
│   ├── src/main/java/com/example/
│   │   ├── data/               # SQLite Room Entities, DAOs, and Database
│   │   ├── ui/                 # Jetpack Compose Screens, Themes, and ViewModels
│   │   └── MainActivity.kt     # App Entry Point
│   ├── build.gradle.kts        # Android build configuration
│   └── .env.example            # Android-specific local secrets placeholder
├── firestore.rules             # Secure rules protecting Firestore collections
├── storage.rules               # Private storage folder access configuration
├── server.js                   # Secure Node.js proxy and static asset server
├── package.json                # Server-side scripts and package definition
├── .env.example                # Global environment variables blueprint
└── .gitignore                  # Prevents environment keys, build logs, or caches from committing
```

---

## 🛠️ Getting Started

### 1. Prerequisites
-   **Android Development**: JDK 17 (recommended) and Android SDK configured.
-   **Backend Proxy**: Node.js v16+ and npm installed.

### 2. Environment Setup
1.  In the project root, duplicate `.env.example` as `.env`:
    ```bash
    cp .env.example .env
    ```
2.  Open the newly created `.env` file and insert your official Google AI Studio Gemini API key:
    ```env
    GEMINI_API_KEY=your_actual_api_key_from_google_ai_studio
    PORT=3000
    ```
3.  Similarly, you may copy `/app/.env.example` to `/app/.env` for Android compilation secrets.

### 3. Running the Secure Proxy Server
Install server-side dependencies and start the lightweight proxy:
```bash
npm install
npm start
```
The server will boot on `http://localhost:3000`. You can test the configuration status by visiting `http://localhost:3000/api/config`.

### 4. Running the Android Application
1.  Open the `app` module folder in Android Studio.
2.  Ensure Kotlin Gradle Plugins and dependency sync processes are complete.
3.  Build and deploy the application to your target physical device or streaming emulator.

---

## 🧪 Verification & Linting

To guarantee that code style, gradle dependencies, and compilation units are perfectly clean:

-   To compile the applet and check build stability:
    ```bash
    gradle assembleDebug
    ```
-   To run standard unit and mock UI tests:
    ```bash
    gradle :app:testDebugUnitTest
    ```

---

## 📄 License & Attribution

StudyGenie AI is licensed under the MIT License. Portions of the grid style are inspired by traditional Japanese Bento configurations. All educational models are backed by official Gemini API engines.
