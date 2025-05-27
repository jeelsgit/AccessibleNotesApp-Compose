# Accessible Notes App (Jetpack Compose) - Final Assignment

A functional notes application built for Android using Jetpack Compose. This app allows users to create, view, edit, and deleteOkay, here is the complete `README.md` file code again, based on all our discussions notes, with added features for scheduling timed reminder notifications and a focus on accessibility. Data is stored locally using the Room persistence library.

## App Description

This application provides a straightforward interface for managing personal notes. Users can perform standard CRUD ( and the project requirements.

Make sure to:

1.  **Create a file named `README.md`**Create, Read, Update, Delete) operations on notes, each consisting of a title and content.

Key functionalities include:
* (exactly like that, case-sensitive) in the **root directory** of your Android Studio project.
2.  **Copy   **Home Screen:** Displays a scrollable list of all saved notes. Each item shows the note's title, a preview and paste** the entire content below into that `README.md` file.
3.  **Review and customize of the content, and the scheduled reminder time (if one has been set). A Floating Action Button (FAB) allows users to initiate adding a new note.
*   **Add/Edit Screen:** Allows creating a new note or modifying an existing** any specific details if needed (though it should be quite comprehensive for your project).

```markdown
# Accessible Notes App (Jet one. Users input a title (required) and content. Optionally, they can use Date and Time pickers to set apack Compose) - Final Assignment

A functional notes application built for Android using Jetpack Compose. This app allows users to create, view, edit, and delete notes, with added features for scheduling timed reminder notifications and a focus on accessibility. Data specific time for a future reminder notification.
*   **Detail Screen:** Displays the full title and content of a selected note, is stored locally using the Room persistence library.

## App Description

This application provides a straightforward interface for managing personal notes. Users can along with its reminder time. Buttons are provided to navigate back, edit the current note, or delete it (with confirmation).
* perform standard CRUD (Create, Read, Update, Delete) operations on notes, each consisting of a title and content.

Key functionalities   **Data Persistence:** All notes are saved locally on the device using the Room Persistence Library (`androidx.room`), ensuring data survives include:
*   **Home Screen:** Displays a scrollable list of all saved notes. Each item shows the note's app restarts.
*   **Notifications:** Provides immediate feedback when a new note is added and allows users to schedule timed reminders title, a preview of the content, and the scheduled reminder time (if one has been set). A Floating Action Button for specific notes.

## Accessibility Features (Approx. 180 words)

Accessibility was a core focus to (FAB) allows users to initiate adding a new note.
*   **Add/Edit Screen:** Allows creating a new note ensure the app is usable by a wider range of users, including those relying on assistive technologies. Key features implemented include: or modifying an existing one. Users input a title (required) and content. Optionally, they can use Date and Time pickers

*   **Content Descriptions:** All interactive UI elements, including `IconButton`s (Back, Save, Edit, to set a specific time for a future reminder notification.
*   **Detail Screen:** Displays the full title and content of a Delete, Set Date, Set Time, Clear Reminder), the `FloatingActionButton` (Add Note), and decorative icons within buttons, selected note, along with its reminder time (if set). Buttons are provided to navigate back, edit the current note, or delete have meaningful `contentDescription` attributes. This allows screen readers like TalkBack to clearly announce the purpose and action of each control. Note it (with confirmation).
*   **Data Persistence:** All notes are saved locally on the device using the Room Persistence Library (` list items on the home screen also provide a semantic description summarizing the note and its action (e.g., "Note titled 'androidx.room`), ensuring data survives app restarts.
*   **Notifications:** Provides immediate feedback when a new note isShopping List'. Tap to view details.").
*   **TalkBack Compatibility:** Standard Jetpack Compose Material 3 components were used added and allows users to schedule timed reminders for specific notes.

## Accessibility Features (Approx. 180 words, which generally offer good built-in support for TalkBack navigation. Logical focus order is maintained through standard layout structures ()

Accessibility was a core focus to ensure the app is usable by a wider range of users, including those relying onColumns, Rows). The note title on the Detail Screen is marked as a `heading` using the `semantics` modifier, assistive technologies. Key features implemented include:

*   **Content Descriptions:** All interactive UI elements, including `IconButton` improving structural navigation for screen reader users. Manual testing confirmed usability with TalkBack.
*   **Color Contrast:** The applications (Back, Save, Edit, Delete, Set Date, Set Time, Clear Reminder), the `FloatingActionButton` (Add Note utilizes `MaterialTheme` and its standard light/dark color schemes. These generally adhere to WCAG AA contrast ratio guidelines (4.), and decorative icons within buttons, have meaningful `contentDescription` attributes. This allows screen readers like TalkBack to clearly announce the purpose and action of each control. Note list items on the home screen also provide a semantic description summarizing the note and5:1 for normal text, 3:1 for large text), ensuring text readability against backgrounds. Hardcoded colors that might violate contrast standards were avoided.
*   **Adjustable Text Size:** The UI respects the user's system- its action (e.g., "Note titled 'Shopping List'. Tap to view details.").
*   **TalkBack Compatibility:**wide font size preferences. By using scalable text units (`sp`) and typography styles from `MaterialTheme.typography` (e.g Standard Jetpack Compose Material 3 components were used, which generally offer good built-in support for TalkBack navigation. Logical focus order is maintained through standard layout structures (Columns, Rows). The note title on the Detail Screen is explicitly marked as a., `bodyLarge`, `titleMedium`), text elements resize appropriately. Flexible layouts help prevent text from being cut off when `heading` using the `semantics` modifier, improving structural navigation for screen reader users. Manual testing confirmed usability with Talk larger font sizes are selected in device settings.

## Notification Features

The app implements two types of user notifications:

1Back.
*   **Color Contrast:** The application utilizes `MaterialTheme` and its standard light/dark color schemes..  **Immediate Notification:** When a user successfully saves a **new** note via the Add/Edit screen, a notification is immediately These generally adhere to WCAG AA contrast ratio guidelines (4.5:1 for normal text, 3:1 displayed using `NotificationManagerCompat`.
    *   **Purpose:** Provides instant confirmation that the note was saved.
    *    for large text), ensuring text readability against backgrounds. Hardcoded colors that might violate contrast standards were avoided.
*   ****Content:** Shows a title like "Note Added" and text like "New note '[Note Title]' was added successfully!".
Adjustable Text Size:** The UI respects the user's system-wide font size preferences. By using scalable text units (`    *   **Action:** Tapping this notification opens the app and navigates the user to the **Home Screen**.sp`) and typography styles from `MaterialTheme.typography` (e.g., `bodyLarge`, `titleMedium
    *   **Implementation:** Uses the `CHANNEL_ID_IMMEDIATE` notification channel with default importance.

2`), text elements resize appropriately. Flexible layouts help prevent text from being cut off when larger font sizes are selected in device settings.  **Scheduled Notification (Reminder):** If a user sets a specific date and time on the Add/Edit screen for a note.

## Notification Features

The app implements two types of user notifications:

1.  **Immediate Notification:** When:
    *   **Purpose:** Reminds the user about the note at the specified future time.
    *   **Scheduling:** Uses Android's `AlarmManager` (specifically `setExactAndAllowWhileIdle` or appropriate fallback based a user successfully saves a **new** note via the Add/Edit screen, a notification is immediately displayed using `NotificationManagerCompat`.
    *   **Purpose:** Provides instant confirmation that the note was saved.
    *   **Content:** on permissions and API level) to trigger a `BroadcastReceiver` (`NotificationReceiver`) at the exact time. The `SCHEDULE_ Shows a title like "Note Added" and text like "New note '[Note Title]' was added successfully!".
    *   **EXACT_ALARM` / `USE_EXACT_ALARM` permission is required for reliable timing on Android 1Action:** Tapping this notification opens the app and navigates the user to the **Home Screen**.
    *   **Implementation:**2+.
    *   **Triggering:** The `NotificationReceiver` receives the broadcast when the alarm fires.
    *    Uses the `CHANNEL_ID_IMMEDIATE` notification channel with default importance.

2.  **Scheduled Notification (Reminder):**Content:** The receiver builds and displays a notification (using `NotificationManagerCompat` on the `CHANNEL_ID_SCHEDULE** If a user sets a specific date and time on the Add/Edit screen for a note:
    *   **Purpose:**D` with high importance) showing a title like "Note Reminder" and text like "Time to check your note: Reminds the user about the note at the specified future time.
    *   **Scheduling:** Uses Android's `Alarm [Note Title]".
    *   **Action:** This notification contains a **deep link** (`notesapp://note/{noteId}`). Tapping it opens the app directly to the **Note Detail Screen** for that specific note, usingManager` (specifically `setExactAndAllowWhileIdle` or appropriate fallback based on permissions and API level) to trigger `TaskStackBuilder` to ensure a correct back stack.
    *   **Cancellation:** Scheduled alarms/notifications are automatically cancelled a `BroadcastReceiver` (`NotificationReceiver`) at the exact time. The `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` permission is required for reliable timing on Android 12+.
    *   ** using `AlarmManager.cancel` and `NotificationManagerCompat.cancel` if the user edits the reminder time, clears the reminderTriggering:** The `NotificationReceiver` receives the broadcast when the alarm fires.
    *   **Content:** The receiver time, or deletes the note.

## How to Use

1.  **View Notes:** Launch the app. The Home builds and displays a notification (using `NotificationManagerCompat` on the `CHANNEL_ID_SCHEDULED` with high Screen displays your existing notes. If empty, it shows a prompt.
2.  **Add a New Note:** Tap the importance) showing a title like "Note Reminder" and text like "Time to check your note: [Note Title]".
    * circular '+' button (Floating Action Button) at the bottom right of the Home Screen.
3.  **Enter Note Details:**   **Action:** This notification contains a **deep link** (`notesapp://note/{noteId}`). Tapping it opens the On the "Add Note" screen, type a **Title** (required) and the main **Content** for your app directly to the **Note Detail Screen** for that specific note, using `TaskStackBuilder` to ensure a correct back stack. note in the provided text fields.
4.  **Set a Reminder (Optional):**
    *   Tap the **Calendar icon** (Set Date button) to open a date picker. Select a date (today or future)
    *   **Cancellation:** Scheduled alarms/notifications are automatically cancelled using `AlarmManager.cancel` and `NotificationManager and tap "OK".
    *   Tap the **Clock icon** (Set Time button) to open a timeCompat.cancel` if the user edits the reminder time, clears the reminder time, or deletes the note.

## How to picker. Select a time and tap "OK". The system ensures the selected time is in the future.
    *   The selected Use

1.  **View Notes:** Launch the app. The Home Screen displays your existing notes. If empty, it date and time will be displayed.
    *   To remove the reminder, tap the **'X' icon** shows a prompt.
2.  **Add a New Note:** Tap the circular '+' button (Floating Action Button) (Clear Reminder button) that appears next to the time buttons.
5.  **Save the Note:** Tap the **Check at the bottom right of the Home Screen.
3.  **Enter Note Details:** On the "Add Note" screen, type a **Title** (required) and the main **Content** for your note in the provided text fieldsmark icon** (✓) in the top app bar.
    *   If it was a new note, a "Note Added" notification will appear briefly.
    *   You will be returned to the Home Screen, where the.
4.  **Set a Reminder (Optional):**
    *   Tap the **Calendar icon** (Set Date new/updated note will appear at the top.
6.  **View Note Details:** Tap on any note item button) to open a date picker. Select a date (today or future) and tap "OK".
    * in the list on the Home Screen. This will navigate you to the Note Detail Screen.
7.  **Edit   Tap the **Clock icon** (Set Time button) to open a time picker. Select a time and tap " an Existing Note:** From the Note Detail Screen, tap the **Pencil icon** (✎) in the top app bar. ThisOK". The system ensures the selected time is in the future.
    *   The selected date and time will be displayed.
    *   To remove the reminder, tap the **'X' icon** (Clear Reminder button) that opens the "Edit Note" screen with the current note's details loaded. Make your changes and tap Save (✓). appears next to the time buttons (if a reminder is set).
5.  **Save the Note:** Tap the **
8.  **Delete a Note:** From the Note Detail Screen, tap the **Trash Can icon** (🗑️) in the top app bar. A confirmation dialog will appear. Tap "Delete" to permanently remove the note,Checkmark icon** (✓) in the top app bar.
    *   If it was a new note, a or "Cancel" to keep it. Deleting also cancels any scheduled reminder.
9.  **Interact with Notifications:**
 "Note Added" notification will appear briefly.
    *   You will be returned to the Home Screen, where the new/    *   Tap the "Note Added" notification to go to the Home Screen.
    *   Tap a scheduledupdated note will appear at the top.
6.  **View Note Details:** Tap on any note item in the list on the "Note Reminder" notification to go directly to that specific note's Detail Screen.

## Code Comments

The source code includes Home Screen. This will navigate you to the Note Detail Screen.
7.  **Edit an Existing Note:** From comments explaining key logic points, particularly around:
*   Room Database setup (Entity, DAO, Database).
*   ViewModel the Note Detail Screen, tap the **Pencil icon** (✎) in the top app bar. This opens the "Edit Note" screen with the current note's details loaded. Make your changes and tap Save (✓).
8.  ** state management (`StateFlow`, `viewModelScope`).
*   Jetpack Compose Navigation setup (`NavHost`, routes,Delete a Note:** From the Note Detail Screen, tap the **Trash Can icon** (🗑️) in the top arguments, deep links, `LaunchedEffect` for initial navigation).
*   Notification channel creation, building notifications (`Notification app bar. A confirmation dialog will appear. Tap "Delete" to permanently remove the note, or "Cancel" toCompat.Builder`), scheduling alarms (`AlarmManager`), handling broadcasts (`BroadcastReceiver`), and cancellation logic (`NotificationHelper`).
 keep it. Deleting also cancels any scheduled reminder.
9.  **Interact with Notifications:**
    *   *   Specific accessibility implementations (`semantics`, `contentDescription`).
