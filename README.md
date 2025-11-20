# RTTC - Real-Time Translation Client 🌍📱

**RTTC** is a native Android application developed in Java that provides a comprehensive translation experience. It integrates text translation, voice recognition (STT/TTS), and optical character recognition (OCR) within a robust MVVM architecture backed by Supabase.

## 📋 Table of Contents
- [About](#about)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Configuration & Installation](#configuration--installation)
- [Running the App](#running-the-app)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Authors](#authors)

## About
This repository contains the source code for the **DSSMV_ProjectDroid_1231562_1230985** project, developed for the **Mobile Software Systems Development** course at **ISEP** (Instituto Superior de Engenharia do Porto). 

The goal was to create a mobile solution capable of breaking language barriers in real-time, focusing on usability, secure data persistence, and integration with external AI APIs.

## Features
- **🎙️ Voice Translation (Bilingual & Standard):** - Automatic language detection.
  - Split-screen bilingual conversation mode.
  - Integration with *Speech-to-Text* and *Text-to-Speech* (TTS).
- **📷 Image Translation (OCR):** - Capture photos or import from the gallery.
  - Text extraction via OCR43 API.
  - Immediate translation of extracted text.
  - Visual history with thumbnails of analyzed images.
- **💬 Phrase Management:** - Access to useful pre-defined generic phrases (Greetings, Travel, etc.).
  - Personal phrase dictionary management (CRUD) with cloud sync.
- **👤 Account & History:** - Secure authentication (Login/Register) via Supabase.
  - **Guest Mode** available for quick access.
  - Full translation history synchronized to the cloud.
  - Profile and language preference management.

## Tech Stack
- **Language:** Java 8+
- **Platform:** Android (Min SDK 26, Target SDK 34)
- **Build Tool:** Gradle (Kotlin DSL)
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern
- **Backend:** [Supabase](https://supabase.com/) (PostgreSQL & Storage)
- **External APIs:**
    - [TranslateAI (RapidAPI)](https://rapidapi.com/) - Detection & Translation
    - [OCR43 (RapidAPI)](https://rapidapi.com/) - Optical Character Recognition
- **Key Libraries:** Retrofit/OkHttp, Picasso, Android Jetpack (LiveData, ViewModel, Navigation).

## Prerequisites
To compile and run this project, you need:
- **Android Studio** (Latest stable version recommended).
- **Java JDK 1.8** or higher.
- A **RapidAPI** account (for Translation and OCR keys).
- A **Supabase** project (for Database and Auth).

## Configuration & Installation

### 1. Clone the Repository
```bash
git clone [https://github.com/VascoMagolo/DSSMV_ProjectDroid_1231562_1230985.git](https://github.com/VascoMagolo/DSSMV_ProjectDroid_1231562_1230985.git)
cd DSSMV_ProjectDroid_1231562_1230985
```
### 2. Configure API Keys (⚠️ Crucial Step)
For security reasons, API keys are not included in the repository. You must create a local configuration file for the app to function correctly.

Navigate to the root directory of the project (where gradlew and settings.gradle.kts are located).

Create a file named keys.properties.

Add the following lines to the file, replacing the placeholders with your actual credentials:

Properties
```bash
TranslateAPI_KEY="YOUR_RAPIDAPI_KEY"
SUPABASE_URL="YOUR_SUPABASE_URL"
SUPABASE_KEY="YOUR_SUPABASE_ANON_KEY"
```
Note: The keys.properties file is already listed in .gitignore to ensure your secrets remain private.

### 3. Sync Project
Open the project in Android Studio and click "Sync Project with Gradle Files".

Running the App
Via Android Studio (Recommended)
Connect a physical Android device (with USB Debugging enabled) or start an Android Emulator.

Select the app configuration in the toolbar.

Click the Run button (▶️) or press Shift + F10.

Via Command Line
To build and install the debug version directly onto a connected device:

```Bash
./gradlew installDebug
```
Testing
You can execute the different test suites using Gradle wrapper commands:

Unit Tests (Local logic, e.g., SessionManagerTest, TranslationTest):

```Bash

./gradlew test
```
Instrumented Tests (UI/Android components):

```Bash

./gradlew connectedAndroidTest
```
Generate Documentation (Javadoc):

```Bash
./gradlew generateJavadoc
```
Documentation will be generated at app/build/docs/javadoc/index.html.

Project Structure
```Plaintext

app/src/main/java/rttc/dssmv_projectdroid_1231562_1230985/
├── exceptions/      # Custom exceptions (AuthException, NetworkException...)
├── model/           # Data models (User, Translation, GenericPhrase...)
├── repository/      # Data logic & API calls (Auth, Translation, Image...)
├── utils/           # Utilities (SessionManager, AuthUiHelper)
├── view/            # UI Layer
│   ├── activities/  # MainActivity, LoginActivity, etc.
│   ├── adapters/    # RecyclerView Adapters
│   └── fragments/   # Main screens (Bilingual, Image, Phrases...)
└── viewmodel/       # Presentation logic (LiveData & State Management)
```
Authors
Group 3 - 2DD

Francisco Silva (1230985)

Vasco Magolo (1231562)

Developed at Instituto Superior de Engenharia do Porto (ISEP).
