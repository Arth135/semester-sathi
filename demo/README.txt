SEMESTER SATHI — LAPTOP DEMO

Open the demo
1. Open index.html in a modern browser.
2. On first open, fill in your study level and class/year. Name, institution, course, and semester are optional.
3. Choose the sample semester to explore the app, or start with an empty workspace.
4. Use Calendar, Subjects, To-dos, AI Chat, and Progress. Your semester information is saved in this browser.

Files and Gemini
- Open Files from the app sections menu to save exam timetables, e-books, PDFs, Office/OpenDocument files, plain text, and common images on this device.
- In AI Chat, choose Attach files or Browse saved files. Attached copies are kept in Files; the selected document is sent to Google only when you send the chat message.
- Gemini can read PDFs, Office/OpenDocument documents, text files, and common images. EPUB e-books are kept locally but need to be converted to PDF before Gemini can read them.
- Up to 50 MB of files can be attached to one message. Google temporarily stores large Files API uploads and deletes them after 48 hours.
- Reattach a saved document when asking follow-up questions in a new chat message. File contents are not included in the app's JSON backup.

Profile and navigation
- Tap the profile icon to open Student profile or Data & settings.
- On a phone, tap the Sections button in the header to open the app tabs. Use the profile icon for your student profile and Data & settings.
- AI Chat has suggested prompts when your Gemini key is saved. API setup details appear there only when no key is saved.

Subjects and progress
- Add or remove chapters / lessons from any subject card.
- Mark each topic as not started, in progress, or done. Subject and semester progress bars are calculated from the topics marked done; there is no manual percentage slider.
- Edit or remove a subject from its card. Removing a subject also removes its linked tasks and cancels their reminders after confirmation.
- Remove a chapter with the × button; the app asks before removing it. Existing calendar tasks remain.
- Remove an individual task with its × button; the app asks before deleting it and cancels its reminder.

Appearance
- Open Data & settings and switch between light and dark mode. The selection is saved with your local workspace.

Gemini AI
- Create a personal key at https://aistudio.google.com/apikey and save it on this device in Data & settings.
- After saving the key, choose AI Chat in the main navigation to ask questions about your subjects or request schedule changes.
- AI Chat can propose adding or removing subjects and tasks, adding chapters / lessons, or changing topic status. A subject needs only its name; exam details and chapters / lessons are optional. Review each proposal and select Apply changes; the app does not apply AI changes automatically. Approved task proposals also schedule a phone reminder in the Android APK.
- “Build study plan” also offers an offline planner and optional Gemini generation. Gemini requests use Google's Interactions API with store=false.
- Messages sent from AI Chat include your message, subject names, exam dates, topic statuses, and task details. Your profile name and college are excluded. The app keeps chat history locally on the device.
- Gemini needs internet. API availability, free-tier eligibility, limits, and pricing depend on the Google AI Studio project and may change. Free-tier prompts may be used to improve Google products. Review current Google AI Studio usage and terms.
- Use “Test connection” in Data & settings to check whether Google can accept the saved key. The app shows Google's returned quota, key, or request error details. Temporary network/server errors are retried once.
- The Android app encrypts the saved key with Android Keystore for reuse between launches. The browser demo stores it in this browser. The key is not in your JSON backup or synced anywhere. Use Clear in Settings to remove it.

Data and limitations
Semester/profile data and chat history stay in this browser's local storage and are not synced to a server. Use Data & settings to export or import a JSON backup. If you clear browser data, you will need the backup to restore it.
The direct browser API-key setup is for private testing. Each user should supply their own key; do not put a developer-owned key in a public APK.
