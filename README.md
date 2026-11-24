# Semester Sathi

Semester Sathi is an offline-first semester planner for Android and modern desktop browsers. It helps students keep exam dates, syllabus progress, and study tasks together. Calendar, task, subject, and progress data are stored locally on the device; there is no Semester Sathi server.

## Download and try it

- Android APK: [downloads/Semester-Sathi-0.1.0-debug.apk](downloads/Semester-Sathi-0.1.0-debug.apk)
- Browser demo: open [demo/index.html](demo/index.html), or download [downloads/semester-sathi-demo.zip](downloads/semester-sathi-demo.zip)

The APK is a debug build for personal testing. On your phone, copy the APK to the device, open it, and allow installation from that file manager if Android asks. Allow notifications when prompted if you want task reminders.

## Features

- First-run student setup for class, course, academic year, and semester.
- Semester calendar with exam dates and daily study agenda.
- Subject cards with exam information and manually managed chapters or lessons.
- Topic statuses: Not started, In progress, and Done.
- Subject and semester progress bars calculated automatically from topics marked Done.
- Study task list with filters and completion state.
- Offline study-plan generator.
- Optional Gemini AI Chat and AI study-plan generation.
- Light and dark themes.
- Android local task reminders, scheduled on-device and restored after a phone restart.
- Local JSON backup and restore.

## Gemini setup

1. Create a personal Gemini API key in [Google AI Studio](https://aistudio.google.com/apikey).
2. In Semester Sathi, open Data & settings, paste the key, and choose Save for this session.
3. Choose AI Chat from the main navigation to start chatting. Use Build study plan to generate an AI plan.

Gemini requests go directly from the app to Google's API; the app does not use a backend server. The key is stored in the current app/browser session, is not included in backups, and is not embedded in this repository. Chat sends the message and relevant study context (subjects, exam dates, topic statuses, and upcoming open tasks). Profile name and college fields are excluded. AI study-plan generation has a separate data notice.

Gemini Chat can propose new study tasks, chapters / lessons, and topic-status changes. Each proposal is shown in the chat and must be approved before it changes local app data. Approved tasks request a local phone reminder. AI does not currently remove subjects, chapters, or tasks; chapters can be removed manually in the Subjects screen.

Gemini requires internet. Model availability, free-tier eligibility, limits, and pricing depend on the user's Google AI Studio project and can change. Review Google's current [Gemini API pricing and terms](https://ai.google.dev/gemini-api/docs/pricing/) before use. Free-tier content may be used by Google to improve its products.

## Offline data and privacy

- Subject, exam, task, theme, chat-history, and progress data are saved in local app/browser storage.
- The app has no account system, sync service, or application server.
- Gemini is the only feature that sends data to Google, and it only runs when the user chooses an AI feature.
- The API key belongs to each user. Do not commit a personal API key to this repository.
- Android backup is disabled for the app.

## Notifications

The Android app schedules task reminders locally, so reminders do not require internet. Android 13 and later requests notification permission. The app uses battery-friendly inexact alarms, which Android may delay while the device is idle or saving battery. The browser demo does not schedule notifications.

## Build the Android app

### Requirements

- JDK 17 or newer
- Android SDK Platform 36
- Android Gradle Plugin 8.13.2 and Gradle 8.13 (provided by the Gradle wrapper)

### Build a debug APK

From the android directory, run:

    .\gradlew.bat assembleDebug

On macOS or Linux, run:

    ./gradlew assembleDebug

The APK is created at android/app/build/outputs/apk/debug/app-debug.apk. The Android Gradle Plugin uses the standard local debug signing key; signing keys are intentionally excluded from the repository. For a public release, create and protect a separate release signing key.

## Repository layout

    android/      Native Android WebView wrapper and app source
    demo/         Standalone browser app and its notes
    downloads/    Test APK and browser demo archive
