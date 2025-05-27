package algonquin.cst8410.finalapplication.ui.screens // Defines the package for UI screen composables

// --- Standard Android Imports ---
import android.annotation.SuppressLint // Used to suppress specific lint warnings (like UnusedMaterial3ScaffoldPaddingParameter).
import android.app.Application // Represents the application context, needed for ViewModel factory.
import android.app.DatePickerDialog // Standard Android dialog for picking a date.
import android.app.TimePickerDialog // Standard Android dialog for picking a time.
import android.content.Context // Provides access to application environment and services (like Toast).
import android.util.Log // Android's logging framework for debugging.
import android.widget.Toast // Used to display short feedback messages to the user.

// --- Jetpack Compose Imports ---
import androidx.compose.foundation.layout.* // Core layout composables (Column, Row, Spacer, Box, padding, fillMaxSize, etc.).
import androidx.compose.foundation.rememberScrollState // Remembers scroll position for scrollable containers.
import androidx.compose.foundation.verticalScroll // Modifier to make a Column scrollable vertically.
import androidx.compose.material.icons.Icons // Provides access to built-in Material icons.
import androidx.compose.material.icons.filled.* // Imports all standard filled Material icons (e.g., Add, Check, ArrowBack).
import androidx.compose.material3.* // Core Material Design 3 components (Scaffold, TopAppBar, OutlinedTextField, Button, etc.).
import androidx.compose.runtime.* // Core Compose runtime functions (Composable, remember, mutableStateOf, LaunchedEffect, collectAsState, SideEffect, etc.).
import androidx.compose.ui.Alignment // Used for aligning elements within layouts (e.g., Alignment.Center).
import androidx.compose.ui.Modifier // Base class for UI element modifiers (padding, size, semantics, etc.).
import androidx.compose.ui.platform.LocalContext // Provides access to the current Context within a Composable.
import androidx.compose.ui.semantics.contentDescription // Modifier property for accessibility (screen readers).
import androidx.compose.ui.semantics.onClick // Used to assign accessibility click actions.
import androidx.compose.ui.semantics.semantics // Modifier to add semantic properties for accessibility.
import androidx.compose.ui.text.input.TextFieldValue // Represents the state (text, selection, composition) for TextFields.
import androidx.compose.ui.tooling.preview.Preview // Annotation to show a preview of the Composable in Android Studio.
import androidx.compose.ui.unit.dp // Extension for specifying dimensions in density-independent pixels.

// --- Lifecycle & Navigation Imports ---
import androidx.lifecycle.viewmodel.compose.viewModel // Composable function to get a ViewModel instance scoped to the navigation graph or Activity.
import androidx.navigation.NavController // Controller used for navigating between composable screens.
import androidx.navigation.compose.rememberNavController // Remembers a NavController across recompositions (mainly for previews).

// --- Your Project Specific Imports ---
import algonquin.cst8410.finalapplication.data.Note // Data class representing a note entity.
import algonquin.cst8410.finalapplication.notifications.NotificationHelper // Helper object for notification tasks.
import algonquin.cst8410.finalapplication.ui.theme.FinalApplicationTheme // Your app's custom Compose theme.
import algonquin.cst8410.finalapplication.viewmodel.NotesViewModel // ViewModel managing note data and operations.
import algonquin.cst8410.finalapplication.viewmodel.NotesViewModelFactory // Factory to create NotesViewModel instances with dependencies.

// --- Coroutines Import ---
import kotlinx.coroutines.launch // Builder for launching coroutines (used for background tasks like DB operations).

// --- Java Util Imports ---
import java.util.* // Needed for Date (logging) and Calendar (date/time manipulation).

// --- Helper Extension Function (Top Level) ---
/**
 * Extension function to clear the time components (hour, minute, second, millisecond)
 * of a Calendar instance, setting them to zero. Useful for date-only comparisons.
 */
fun Calendar.clearTime() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
// --- End Helper ---


/**
 * Composable screen for adding a new note or editing an existing one.
 * Uses Material 3 components and handles user input, date/time picking,
 * saving to the database via ViewModel, and scheduling/cancelling notifications.
 *
 * @param navController The NavController used for navigating back after saving.
 * @param viewModel The NotesViewModel instance providing access to data operations.
 * @param noteId The ID of the note to edit. If null or -1, the screen operates in "Add Note" mode.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Suppress warning about unused padding lambda parameter in Scaffold.
@OptIn(ExperimentalMaterial3Api::class) // Opt-in required for using experimental Material 3 components like Scaffold, TopAppBar, OutlinedTextField.
@Composable
fun AddEditNoteScreen(
    navController: NavController,
    // Obtain ViewModel instance, using factory for Application context.
    viewModel: NotesViewModel = viewModel(
        factory = NotesViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    noteId: Int? // The ID of the note being edited, or null/(-1) for a new note.
) {
    // --- Hooks and Setup ---
    val context = LocalContext.current // Get current Android context for Toasts, Dialogs.
    val scope = rememberCoroutineScope() // Get a coroutine scope bound to this Composable's lifecycle for launching background tasks.
    val TAG = "AddEditNoteScreen" // Tag for Logcat filtering.

    // --- State Variables ---
    // State for the title TextField. Using TextFieldValue allows tracking cursor position etc.
    var noteTitle by remember { mutableStateOf(TextFieldValue("")) }
    // State for the content TextField.
    var noteContent by remember { mutableStateOf(TextFieldValue("")) }
    // State holding the selected Date and Time for the reminder, or null if none set.
    var selectedDateTime by remember { mutableStateOf<Calendar?>(null) }
    // State to store the original note data when editing, used to compare notification time changes.
    var originalNote by remember { mutableStateOf<Note?>(null) }
    // State to control whether a loading indicator is shown (true when fetching an existing note).
    var isLoading by remember { mutableStateOf(noteId != null) }
    // State to reliably hold the note's ID once it's known (either from nav arg or after fetching). Used for notification logic.
    var currentNoteIdForNotifications by remember { mutableStateOf(noteId ?: 0) }

    // --- LaunchedEffect for Data Loading ---
    // This effect runs when the composable first launches or when `noteId` changes.
    // It fetches the note data from the ViewModel if editing an existing note.
    LaunchedEffect(noteId) {
        Log.d(TAG, "LaunchedEffect triggered to load note state. noteId: $noteId")
        // Check if noteId indicates an existing note needs to be loaded.
        if (noteId != null && noteId != -1) {
            isLoading = true // Show loading indicator.
            Log.d(TAG, "Fetching existing note with ID: $noteId from ViewModel.")
            // Fetch the note data using the ViewModel's suspend function.
            val existingNote = viewModel.getNoteById(noteId)
            // Check if the note was found in the database.
            if (existingNote != null) {
                Log.d(TAG, "Existing note found: ID=${existingNote.id}, Title='${existingNote.title}'")
                // Store the original note data for comparison later (e.g., if reminder time changed).
                originalNote = existingNote
                // Update UI state fields with the fetched data.
                noteTitle = TextFieldValue(existingNote.title)
                noteContent = TextFieldValue(existingNote.content)
                currentNoteIdForNotifications = existingNote.id // Store the definite ID for notification logic.
                // Check if the fetched note has a reminder time set.
                existingNote.notificationTime?.let { time ->
                    // If a time exists, convert the Long timestamp back into a Calendar object for the state.
                    selectedDateTime = Calendar.getInstance().apply { timeInMillis = time }
                    Log.d(TAG, "Loaded existing reminder time: ${Date(time)}")
                } ?: run {
                    // If no time exists in the database, ensure the state is null.
                    selectedDateTime = null
                    Log.d(TAG, "No existing reminder time found for this note.")
                }
            } else {
                // Handle case where an ID was passed via navigation, but no corresponding note exists in the database.
                Log.w(TAG, "Note with ID $noteId provided but not found in database. Navigating back.")
                Toast.makeText(context, "Note not found", Toast.LENGTH_SHORT).show()
                // Automatically navigate back to the previous screen.
                navController.popBackStack()
            }
            isLoading = false // Hide loading indicator after fetching is complete.
        } else {
            // This block executes if noteId is null or -1, indicating "Add Note" mode.
            Log.d(TAG, "Entering screen in 'Add Note' mode.")
            // Reset state variables to their default empty/null values.
            selectedDateTime = null
            originalNote = null
            currentNoteIdForNotifications = 0 // No ID assigned yet.
            isLoading = false // No data fetching needed.
        }
        Log.d(TAG, "LaunchedEffect finished loading state.")
    } // End LaunchedEffect


    // --- Date and Time Picker Dialog Setup ---
    val currentCalendar = Calendar.getInstance() // Get current calendar instance for default values in pickers.

    // Listener implementation for the DatePickerDialog. Called when the user confirms a date selection.
    val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        // Create a temporary Calendar instance based on current selection or a new instance.
        val tempCalendar = (selectedDateTime ?: Calendar.getInstance()).apply {
            // Set the year, month (0-indexed), and day selected by the user.
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        // Get today's date with the time cleared to prevent selecting a past date.
        val today = Calendar.getInstance().apply { clearTime() } // Use helper extension function.
        // Check if the selected date is before today.
        if (tempCalendar.timeInMillis < today.timeInMillis) {
            Log.w(TAG, "User selected a past date ($year-${month+1}-$dayOfMonth), ignoring selection.")
            Toast.makeText(context, "Cannot select a past date", Toast.LENGTH_SHORT).show()
        } else {
            // If the date is valid (today or future), update the state variable.
            selectedDateTime = tempCalendar
            Log.d(TAG, "Date selected and updated in state: ${Date(selectedDateTime!!.timeInMillis)}")
            // Optional: Automatically show the TimePickerDialog after a valid date is set.
            // timePickerDialog.show()
        }
    }

    // Listener implementation for the TimePickerDialog. Called when the user confirms a time selection.
    val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        // Ensure the date part of the Calendar is initialized (defaults to today if selectedDateTime was null).
        val baseCalendar = selectedDateTime ?: Calendar.getInstance().apply { clearTime() }
        // Update the state with the selected hour and minute, resetting seconds/milliseconds.
        selectedDateTime = baseCalendar.apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // After setting the time, check if the combined date/time is in the past relative to the current moment.
        if (selectedDateTime!!.timeInMillis <= System.currentTimeMillis()) {
            // If the selected time has already passed today, advance the date by one day.
            selectedDateTime!!.add(Calendar.DAY_OF_YEAR, 1)
            Log.w(TAG, "Selected time was in the past for today, automatically adjusted to the next day.")
            Toast.makeText(context, "Reminder set for the next occurrence", Toast.LENGTH_SHORT).show()
        }
        Log.d(TAG, "Time selected. Final reminder DateTime set in state: ${Date(selectedDateTime!!.timeInMillis)}")
    }

    // Create the DatePickerDialog instance, configuring it with the listener and initial values.
    val datePickerDialog = DatePickerDialog(
        context, // The current context.
        dateSetListener, // The listener to handle date selection.
        // Set initial date shown in the dialog: Use selected date if available, else current date.
        selectedDateTime?.get(Calendar.YEAR) ?: currentCalendar.get(Calendar.YEAR),
        selectedDateTime?.get(Calendar.MONTH) ?: currentCalendar.get(Calendar.MONTH),
        selectedDateTime?.get(Calendar.DAY_OF_MONTH) ?: currentCalendar.get(Calendar.DAY_OF_MONTH)
    ).apply { // Further configure the dialog after creation.
        // Set the minimum selectable date in the picker to today's date.
        val todayMillis = Calendar.getInstance().apply { clearTime() }.timeInMillis
        datePicker.minDate = todayMillis
    }

    // Create the TimePickerDialog instance, configuring it with the listener and initial values.
    val timePickerDialog = TimePickerDialog(
        context, // The current context.
        timeSetListener, // The listener to handle time selection.
        // Set initial time shown in the dialog: Use selected time if available, else current time.
        selectedDateTime?.get(Calendar.HOUR_OF_DAY) ?: currentCalendar.get(Calendar.HOUR_OF_DAY),
        selectedDateTime?.get(Calendar.MINUTE) ?: currentCalendar.get(Calendar.MINUTE),
        // Use the device's preferred 12-hour or 24-hour format setting.
        android.text.format.DateFormat.is24HourFormat(context)
    )
    // --- End Picker Setup ---


    // --- UI Structure using Scaffold ---
    // Scaffold provides the basic Material Design structure (AppBar, content area, FAB).
    Scaffold(
        // --- Top App Bar Configuration ---
        topBar = {
            TopAppBar(
                // Title changes based on whether adding or editing.
                title = { Text(if (noteId == null) "Add Note" else "Edit Note") },
                // Navigation icon (usually back button).
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }, // Action to navigate back.
                        // Accessibility description for the button.
                        modifier = Modifier.semantics { contentDescription = "Back to previous screen" }) {
                        // The actual icon visual with its own description for screen readers.
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back Button")
                    }
                },
                // Action icons displayed on the right side of the AppBar.
                actions = {
                    // Save Button Configuration.
                    IconButton(
                        // onClick lambda defines what happens when the Save button is tapped.
                        onClick = {
                            // 1. Get current text from state, trim whitespace.
                            val title = noteTitle.text.trim()
                            val content = noteContent.text.trim()
                            // 2. Basic validation: Ensure title is not empty.
                            if (title.isNotEmpty()) {
                                // 3. Get the final timestamp (Long or null) from the selectedDateTime state.
                                val finalNotificationTime = selectedDateTime?.timeInMillis
                                Log.d(TAG, "Save button clicked. Title is valid. Final Reminder Time: ${finalNotificationTime?.let { Date(it) } ?: "None"}")

                                // 4. Create a Note object with the current data.
                                val noteToSave = Note(
                                    // Use the known ID if editing (from currentNoteIdForNotifications).
                                    // If adding (ID is 0), Room will generate a new ID upon insertion.
                                    id = currentNoteIdForNotifications.takeIf { it != 0 } ?: 0,
                                    title = title,
                                    content = content,
                                    notificationTime = finalNotificationTime
                                )

                                // 5. Initiate Save/Update and Notification Handling within a Coroutine.
                                // This ensures database and notification operations don't block the main UI thread.
                                scope.launch {
                                    // Variable to hold the ID used for subsequent notification logic.
                                    var finalNoteId: Int

                                    // Determine the ID: If editing, use known ID. If adding, call the ViewModel function that returns the new ID.
                                    if (noteId == null) { // Branch for ADDING a NEW note.
                                        Log.d(TAG, "Adding new note. Calling viewModel.insertAndGetId to get new ID.")
                                        try {
                                            // Call the suspending function in ViewModel to insert and get the generated ID.
                                            val returnedId = viewModel.insertAndGetId(noteToSave)
                                            finalNoteId = returnedId.toInt() // Convert the returned Long ID to Int.
                                            Log.i(TAG, "Successfully inserted new note. Received Actual ID: $finalNoteId")
                                        } catch (e: Exception) {
                                            // Handle potential errors during insertion.
                                            Log.e(TAG, "Error inserting note and getting ID from ViewModel", e)
                                            finalNoteId = 0 // Set ID to 0 to indicate failure.
                                        }
                                    } else { // Branch for UPDATING an EXISTING note.
                                        Log.d(TAG, "Updating existing note. Calling viewModel.insertOrUpdateNote.")
                                        // Call the standard update function (doesn't need to return ID).
                                        viewModel.insertOrUpdateNote(noteToSave)
                                        // Use the ID we already stored when the note was loaded.
                                        finalNoteId = currentNoteIdForNotifications
                                        Log.i(TAG, "Update requested for existing note ID: $finalNoteId")
                                    }

                                    // Retrieve the original reminder time for comparison.
                                    val originalTime = originalNote?.notificationTime

                                    // --- Perform Notification Scheduling/Cancellation Logic ---
                                    // Proceed only if we have a valid (non-zero) note ID.
                                    if (finalNoteId != 0) {
                                        // Check if the reminder time was changed or removed.
                                        if (originalTime != null && originalTime != finalNotificationTime) {
                                            Log.i(TAG, "Reminder time changed/removed. Cancelling previous notification for Note ID: $finalNoteId")
                                            // Cancel the previously scheduled alarm/notification.
                                            NotificationHelper.cancelScheduledNotification(context, finalNoteId)
                                        }
                                        // Check if a new time is set AND it's different from the original (or if it's a new note).
                                        if (finalNotificationTime != null && finalNotificationTime != originalTime) {
                                            Log.i(TAG, "Scheduling new/updated notification for Note ID: $finalNoteId at ${Date(finalNotificationTime)}")
                                            // Schedule the new alarm/notification.
                                            NotificationHelper.scheduleNotification(context, finalNoteId, title, finalNotificationTime)
                                        }
                                        // Send an immediate "Note Added" notification only if this was a NEW note (original noteId was null).
                                        if (noteId == null) {
                                            Log.i(TAG, "Sending immediate 'Note Added' notification for new Note ID: $finalNoteId")
                                            NotificationHelper.sendImmediateNotification(context, title, finalNoteId)
                                        }
                                    } else {
                                        // Log if we failed to get a valid ID (should only happen if insert failed).
                                        Log.w(TAG, "Could not get valid note ID after save/insert attempt. Skipping notification handling.")
                                    }
                                    // --- End Notification Logic ---

                                    // 6. Navigate back to the previous screen AFTER the coroutine operations are done/initiated.
                                    Log.d(TAG, "Save/Notification operations complete. Navigating back.")
                                    navController.popBackStack()
                                } // End scope.launch for background tasks

                            } else {
                                // Handle validation failure (empty title).
                                Log.w(TAG, "Save attempt failed: Title is empty.")
                                Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                            }
                        }, // End onClick lambda for Save button
                        modifier = Modifier.semantics { contentDescription = "Save note" } // Accessibility description.
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save Button") // Checkmark icon with description.
                    }
                } // End actions block
            ) // End TopAppBar
        }, // End topBar lambda definition

        // --- Main Content Area of the Scaffold ---
        content = { paddingValues -> // Content lambda receives padding values applied by Scaffold (e.g., for AppBar).
            // Conditionally display loading indicator or the input form.
            if (isLoading) {
                // Display a centered CircularProgressIndicator while data is loading.
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues), // Apply Scaffold padding.
                    contentAlignment = Alignment.Center // Center the indicator.
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Display the form content (Title, Content, Reminder) in a Column.
                Column(
                    modifier = Modifier
                        .fillMaxSize() // Occupy all available space.
                        .padding(paddingValues) // Apply padding from Scaffold.
                        .padding(16.dp) // Add extra padding around the form elements.
                        .verticalScroll(rememberScrollState()) // Make the entire form scrollable if needed.
                ) {
                    // Title Input Field.
                    OutlinedTextField(
                        value = noteTitle, // Bind state variable.
                        onValueChange = { noteTitle = it }, // Update state on input change.
                        label = { Text("Title") }, // Hint label for the field.
                        // Modifier for layout and accessibility.
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Note title input field" },
                        singleLine = true, // Input constrained to a single line.
                        textStyle = MaterialTheme.typography.titleLarge, // Apply theme typography.
                        colors = OutlinedTextFieldDefaults.colors() // Use default Material 3 text field colors.
                    )
                    Spacer(modifier = Modifier.height(16.dp)) // Vertical spacing.

                    // Content Input Field.
                    OutlinedTextField(
                        value = noteContent, // Bind state variable.
                        onValueChange = { noteContent = it }, // Update state on input change.
                        label = { Text("Content") }, // Hint label.
                        modifier = Modifier
                            .fillMaxWidth() // Take full width.
                            .heightIn(min = 150.dp) // Ensure a reasonable minimum height for typing content.
                            .semantics { contentDescription = "Note content input field" }, // Accessibility.
                        textStyle = MaterialTheme.typography.bodyLarge, // Apply theme typography.
                        colors = OutlinedTextFieldDefaults.colors(), // Use default Material 3 colors.
                        maxLines = 10 // Allow multiple lines (up to 10 visible before scrolling internally).
                    )
                    Spacer(modifier = Modifier.height(24.dp)) // Larger spacing before reminder section.

                    // --- Reminder Section UI ---
                    Text("Reminder", style = MaterialTheme.typography.titleMedium) // Section heading.
                    Spacer(modifier = Modifier.height(8.dp)) // Spacing below heading.
                    // Row to layout reminder text and action buttons horizontally.
                    Row(
                        modifier = Modifier.fillMaxWidth(), // Take full width.
                        verticalAlignment = Alignment.CenterVertically, // Align items vertically in the center of the row.
                        horizontalArrangement = Arrangement.SpaceBetween // Push text left and buttons right.
                    ) {
                        // Display the currently selected reminder time, or placeholder text.
                        Text(
                            // Format the Calendar state into a readable string if not null.
                            text = selectedDateTime?.let { formatDateTime(it.timeInMillis, context) } ?: "No reminder set",
                            style = MaterialTheme.typography.bodyLarge, // Apply text style.
                            // Use weight to make text take available space. Add padding to avoid touching buttons.
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        // Row containing the action buttons for the reminder.
                        Row {
                            // Clear Button: Displayed only if a reminder (`selectedDateTime`) is currently set.
                            if (selectedDateTime != null) {
                                IconButton(
                                    onClick = {
                                        Log.d(TAG, "Clear reminder button clicked. Cancelling notification for ID: $currentNoteIdForNotifications")
                                        // Cancel any pending alarm associated with this note before clearing the state.
                                        if (currentNoteIdForNotifications != 0) {
                                            NotificationHelper.cancelScheduledNotification(context, currentNoteIdForNotifications)
                                        }
                                        selectedDateTime = null // Update state to remove the reminder.
                                    },
                                    // Accessibility description for the clear button.
                                    modifier = Modifier.semantics { contentDescription = "Clear reminder time" }
                                ) { Icon(Icons.Filled.Clear, contentDescription = "Clear Reminder Button") } // Clear icon.
                            }
                            // Set Date Button: Always visible, triggers the DatePickerDialog.
                            IconButton(
                                onClick = { datePickerDialog.show() }, // Show the date picker dialog.
                                modifier = Modifier.semantics { contentDescription = "Set reminder date" } // Accessibility.
                            ) { Icon(Icons.Filled.Event, contentDescription = "Set Date Button") } // Calendar icon.
                            // Set Time Button: Always enabled, triggers the TimePickerDialog.
                            IconButton(
                                enabled = true, // Keep enabled; time picker logic handles selecting future time.
                                onClick = { timePickerDialog.show() }, // Show the time picker dialog.
                                modifier = Modifier.semantics { contentDescription = "Set reminder time" } // Accessibility.
                            ) { Icon(Icons.Filled.Schedule, contentDescription = "Set Time Button") } // Clock icon.
                        } // End Button Row
                    } // End Reminder Row
                } // End Main Column
            } // End Else (isLoading condition)
        } // End Scaffold Content lambda
    ) // End Scaffold composable
} // End AddEditNoteScreen Composable


// --- Preview Function ---
/**
 * Provides a design-time preview of the AddEditNoteScreen in Android Studio.
 * Shows the screen in the state for adding a new note (noteId = null).
 */
@Preview(showBackground = true) // Show background in preview pane.
@Composable
fun AddEditNoteScreenPreview() {
    FinalApplicationTheme { // Apply the app's theme to the preview for consistency.
        // Render the screen with a dummy NavController and null noteId for "Add Note" mode.
        AddEditNoteScreen(navController = rememberNavController(), noteId = null)
    }
}