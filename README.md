# Semester Sathi

Semester Sathi is an offline-first semester planner for Android and modern desktop browsers. Calendar, subject, task, progress, and chat data stay on the device; there is no Semester Sathi server.

## Download and try it

- Android APK (version 0.1.1): [downloads/Semester-Sathi-0.1.2-debug.apk](downloads/Semester-Sathi-0.1.2-debug.apk)
- Android source project: [downloads/semester-sathi-android-source.zip](downloads/semester-sathi-android-source.zip)
- Browser demo: open [demo/index.html](demo/index.html), or download [downloads/semester-sathi-demo.zip](downloads/semester-sathi-demo.zip)

The APK is a debug build for personal testing. Copy it to your phone, open it, and allow installation from that file manager if Android asks. Allow notifications if you want task reminders. If updating from version 0.1.0 or 0.1.1, the first launch migrates existing semester data and the current session API key.

## Features

- First-run student setup for class, course, academic year, and semester.
- Semester calendar with exam dates and a daily study agenda.
- Subjects with exam details and manually managed chapters or lessons.
- Topic statuses: Not started, In progress, and Done; progress bars calculate from topics marked Done.
- Study tasks, offline study-plan generator, and local JSON backup and restore.
- Optional Gemini AI Chat and AI study-plan generation.
- Gemini connection check with Google key, quota, and request error details; temporary network/server errors are retried once.
- Light and dark themes.
- Android local reminders scheduled on-device and restored after a phone restart.

## Gemini setup

1. Create your own Gemini API key in [Google AI Studio](https://aistudio.google.com/apikey).
2. In Semester Sathi, open Data & settings, paste the key, and save it on this device.
3. Choose AI Chat to chat, or Build study plan to request a plan. Use Test connection in settings if requests fail.

Gemini requests go directly from the app to Google; there is no backend server. The key is not embedded in this repository or APK and is not included in backups. Android encrypts it with Android Keystore for reuse between app launches; the browser demo saves it in that browser. Use Clear in Settings to remove it. Chat sends the prompt and relevant study context (subjects, exam dates, topic statuses, and upcoming open tasks); profile name and college fields are excluded. AI can propose study tasks, chapters or lessons, and topic-status changes. Review each proposal and choose Apply changes before the app changes local data.

Gemini requires internet. Model availability, free-tier eligibility, limits, and pricing are set by Google and may change. See Google's current [Gemini API pricing and terms](https://ai.google.dev/gemini-api/docs/pricing/). A 429 response usually means a project quota or rate limit was reached; wait for the quota window or review the Google AI Studio project limits.

The Android APK loads its bundled page under an HTTPS-style local WebView origin to support web API requests. The app interface itself remains on the device. The browser demo stores data in the browser's local storage.

## Build the Android app

Requirements: JDK 17 or newer and Android SDK Platform 36. From the `android` directory, run:

    .\gradlew.bat assembleDebug

On macOS or Linux, run `./gradlew assembleDebug`. The APK is created at `android/app/build/outputs/apk/debug/app-debug.apk`. Gradle creates a local debug signing key for personal testing; signing keys are not included in the source archive. Use a separately protected release key for a public release.

## Notifications and offline use

Calendar, subjects, topics, tasks, progress, and the offline planner work without internet. Gemini needs internet. Android reminders are local and use inexact alarms, which Android may delay while the phone is idle. Browser demos do not schedule phone notifications.
