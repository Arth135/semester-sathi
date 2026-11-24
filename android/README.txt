SEMESTER SATHI — ANDROID PROJECT

This project wraps the offline Semester Sathi planner in a small Android WebView app. It has no app server. Semester data, chat history, theme, and tasks are stored locally on the device. Gemini is optional and requires internet.

Features
- First-time student setup, calendar, exam dates, study to-dos, subjects, and topic-based progress.
- Add and remove chapters or lessons manually.
- Light and dark themes.
- Optional Gemini chat and study-plan generation using the API key supplied by the student. AI changes are shown as proposals and require approval.
- Test connection in Data & settings reports key, quota, or request errors. Temporary network/server failures are retried once.
- Local task reminders, restored after a phone restart. Android 13+ asks for notification permission.
- HTTPS-style local WebView origin for reliable cross-origin Gemini requests. No app screen is hosted remotely.
- On first launch after upgrading from 0.1.0, existing semester data and the active session API key are migrated to the new app origin.

Build
1. Install JDK 17 or newer.
2. Install Android SDK Platform 36 and accept its license.
3. From this folder, run: gradlew.bat assembleDebug
4. The APK is created at: app\build\outputs\apk\debug\app-debug.apk

Install
Copy the debug APK to an Android phone and open it. Allow installation from the file manager if Android asks. Android creates a local debug signing key on the first build. This debug APK is for personal testing, not public release.

Gemini and privacy
The API key belongs to each user and is not embedded in the APK or exported in backups. Gemini sends the prompt and relevant study data to Google only when an AI feature is used. The app itself has no account or sync server. Google quota and free-tier policies are controlled by Google.

Reminders use Android's inexact alarms, so the operating system may delay a notification while the device is idle or saving battery.
