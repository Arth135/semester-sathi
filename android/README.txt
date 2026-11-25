SEMESTER SATHI — ANDROID PROJECT

This project wraps the offline Semester Sathi planner in a small Android WebView app. It has no app server. Semester data, chat history, theme, and tasks are stored locally on the device. Gemini is optional and requires internet.

Features
- First-time student setup, calendar, exam dates, study to-dos, subjects, and topic-based progress.
- Add and remove chapters or lessons manually.
- Light and dark themes.
- Optional Gemini chat and study-plan generation using the key supplied by the student. AI chat can propose adding or removing subjects and tasks, adding chapters / lessons, and changing topic statuses. Review and approve proposals before app data changes. A subject needs only a name; exam details and topics can be added or edited later.
- Local Files library for PDFs, Office/OpenDocument files, text, images, and EPUB e-books. Attach supported study documents in AI Chat to ask Gemini about exam dates or build a study plan.
- Phone navigation opens from the header Sections button; profile icon contains Student profile and Data & settings.
- AI Chat uses a focused conversation layout with study prompts and compact document controls. API setup guidance appears in chat only when no key is saved.
- Test connection in Data & settings reports key, quota, or request errors. Temporary network/server failures are retried once.
- Local task reminders, restored after a phone restart. Android 13+ asks for notification permission.
- HTTPS-style local WebView origin for reliable cross-origin Gemini requests. No app screen is hosted remotely.
- On first launch after upgrading from 0.1.0 through 0.1.3, existing semester data and any still-available session API key are migrated. The Android app encrypts the key for reuse between launches.

Build
1. Install JDK 17 or newer.
2. Install Android SDK Platform 36 and accept its license.
3. From this folder, run: gradlew.bat assembleDebug
4. The APK is created at: app\build\outputs\apk\debug\app-debug.apk

Install
Copy the debug APK to an Android phone and open it. Allow installation from the file manager if Android asks. Android creates a local debug signing key on the first build. This debug APK is for personal testing, not public release.

Gemini and privacy
The API key belongs to each user and is not embedded in the APK or exported in backups. It is encrypted with Android Keystore on the phone and persists until cleared in Data & settings. Gemini sends the prompt and relevant study data to Google only when an AI feature is used. Attached documents are copied to on-device storage and sent directly to Google only when the student sends the chat. Large Google Files API uploads are temporarily retained by Google for up to 48 hours. The app itself has no account or sync server. Google quota and free-tier policies are controlled by Google.

Reminders use Android's inexact alarms, so the operating system may delay a notification while the device is idle or saving battery.
