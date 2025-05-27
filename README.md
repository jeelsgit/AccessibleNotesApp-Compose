# Accessible Notes App (Jetpack Compose)

A functional notes application built for Android using Jetpack Compose. This app allows users to create, view, edit, and delete notes, with added features for scheduling timed reminder notifications and a focus on accessibility. Data is stored locally using the Room persistence library.

## App Description

This application provides a straightforward interface for managing personal notes. Users can perform standard CRUD (Create, Read, Update, Delete) operations on notes, each consisting of a title and content.

Key functionalities include:
*   **Home Screen:** Displays a scrollable list of all saved notes. Each item shows the note's title, a preview of the content, and the scheduled reminder time (if one has been set). A Floating Action Button (FAB) allows users to initiate adding a new note.
*   **Add/Edit Screen:** Allows creating a new note or modifying an existing one. Users input a title (required) and content. Optionally, they can use Date and Time pickers to set a specific time for a future reminder notification.
*   **Detail Screen:** Displays the full title and content of a selected note, along with its reminder time. Buttons are provided to navigate back, edit the current note, or delete it (with confirmation).
*   **Data Persistence:** All notes are saved locally on the device using the Room Persistence Library, ensuring data survives app restarts.
*   **Notifications:** Provides immediate feedback when a new note is added and allows users to schedule timed reminders for specific notes.

## Accessibility Features

Accessibility was a core focus to ensure the app is usable by a wider range of users, including those relying on assistive technologies. Key features implemented include:

*   **Content Descriptions:** All interactive UI elements, including `IconButton`s (Back, Save, Edit, Delete, Set Date, Set Time, Clear Reminder), the `FloatingActionButton` (Add Note), and decorative icons within buttons, have meaningful `contentDescription` attributes. This allows screen readers like TalkBack to clearly announce the purpose and action of each control. List items on the home screen also provide a semantic description summarizing the note and its action (e.g., "Note titled 'Shopping List'. Tap to view details.").
*   **TalkBack Compatibility:** Standard Jetpack Compose Material 3 components were used, which generally offer good built-in support for TalkBack navigation. Logical focus order is maintained through standard layout structures (Columns, Rows). The note title on the Detail Screen is marked as a `heading` using the `semantics` modifier, improving structural navigation for screen reader users. Manual testing confirmed usability with TalkBack.
*   **Color Contrast:** The application utilizes `MaterialTheme` and its standard light/dark color schemes. These generally adhere to WCAG AA contrast ratio guidelines (4.5:1 for normal text, 3:1 for large text), ensuring text readability against backgrounds. Hardcoded colors that might violate contrast standards were avoided.
*   **Adjustable Text Size:** The UI respects the user's system-wide font size preferences. By using scalable text units (`sp`) and typography styles from `MaterialTheme.typography` (e.g., `bodyLarge`, `titleMedium`), text elements resize appropriately. Flexible layouts help prevent text from being cut off when larger font sizes are selected in device settings.

## Notification Features

The app implements two types of user notifications:

1.  **Immediate Notification:** When a user successfully saves a **new** note via the Add/Edit screen, a notification is immediately displayed using `NotificationManagerCompat`.
    *   **Purpose:** Provides instant confirmation that the note was saved.
    *   **Content:** Shows a title like "Note Added" and text like "New note '[Note Title]' was added successfully!".
    *   **Action:** Tapping this notification opens the app and navigates the user to the **Home Screen**.
    *   **Implementation:** Uses the `CHANNEL_ID_IMMEDIATE` notification channel with default importance.

2.  **Scheduled Notification (Reminder):** If a user sets a specific date and time on the Add/Edit screen for a note:
    *   **Purpose:** Reminds the user about the note at the specified future time.
    *   **Scheduling:** Uses Android's `AlarmManager` (specifically `setExactAndAllowWhileIdle` or appropriate fallback based on permissions and API level) to trigger a `BroadcastReceiver` (`NotificationReceiver`) at the exact time. The `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` permission is required for reliable timing on Android 12+.
    *   **Triggering:** The `NotificationReceiver` receives the broadcast when the alarm fires.
    *   **Content:** The receiver builds and displays a notification (using `NotificationManagerCompat` on the `CHANNEL_ID_SCHEDULED` with high importance) showing a title like "Note Reminder" and text like "Time to check your note: [Note Title]".
    *   **Action:** This notification contains a **deep link** (`notesapp://note/{noteId}`). Tapping it opens the app directly to the **Note Detail Screen** for that specific note, using `TaskStackBuilder` to ensure a correct back stack.
    *   **Cancellation:** Scheduled alarms/notifications are automatically cancelled using `AlarmManager.cancel` and `NotificationManagerCompat.cancel` if the user edits the reminder time, clears the reminder time, or deletes the note.

## How to Use

1.  **View Notes:** Launch the app. The Home Screen displays your existing notes. If empty, it shows a prompt.
2.  **Add a New Note:** Tap the circular '+' button (Floating Action Button) at the bottom right of the Home Screen.
3.  **Enter Note Details:** On the "Add Note" screen, type a **Title** (required) and the main **Content** for your note in the provided text fields.
4.  **Set a Reminder (Optional):**
    *   Tap the **Calendar icon** (Set Date button) to open a date picker. Select a date (today or future) and tap "OK".
    *   Tap the **Clock icon** (Set Time button) to open a time picker. Select a time and tap "OK". The system ensures the selected time is in the future.
    *   The selected date and time will be displayed.
    *   To remove the reminder, tap the **'X' icon** (Clear Reminder button) that appears next to the time buttons.
5.  **Save the Note:** Tap the **Checkmark icon** (✓) in the top app bar.
    *   If it was a new note, a "Note Added" notification will appear briefly.
    *   You will be returned to the Home Screen, where the new/updated note will appear at the top.
6.  **View Note Details:** Tap on any note item in the list on the Home Screen. This will navigate you to the Note Detail Screen.
7.  **Edit an Existing Note:** From the Note Detail Screen, tap the **Pencil icon** (✎) in the top app bar. This opens the "Edit Note" screen with the current note's details loaded. Make your changes and tap Save (✓).
8.  **Delete a Note:** From the Note Detail Screen, tap the **Trash Can icon** (🗑️) in the top app bar. A confirmation dialog will appear. Tap "Delete" to permanently remove the note, or "Cancel" to keep it. Deleting also cancels any scheduled reminder.
9.  **Interact with Notifications:**
    *   Tap the "Note Added" notification to go to the Home Screen.
    *   Tap a scheduled "Note Reminder" notification to go directly to that specific note's Detail Screen.

## Code Comments

The source code includes comments explaining key logic points, particularly around:
*   Room Database setup (Entity, DAO, Database).
*   ViewModel state management (`StateFlow`, `viewModelScope`).
*   Jetpack Compose Navigation setup (`NavHost`, routes, arguments, deep links, `LaunchedEffect` for initial navigation).
*   Notification channel creation, building notifications (`NotificationCompat.Builder`), scheduling alarms (`AlarmManager`), handling broadcasts (`BroadcastReceiver`), and cancellation logic (`NotificationHelper`).
*   Specific accessibility implementations (`semantics`, `contentDescription`).
