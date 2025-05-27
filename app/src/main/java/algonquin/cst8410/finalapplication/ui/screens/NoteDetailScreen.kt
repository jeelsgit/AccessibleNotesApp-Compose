package algonquin.cst8410.finalapplication.ui.screens // Defines the package for UI screen composables

// --- Standard Android Imports ---
import android.annotation.SuppressLint // Used to suppress specific lint warnings.
import android.app.Application // Application context needed for ViewModel factory.
// import android.content.Context // Not directly used here, but LocalContext.current provides it.
import android.util.Log // Android's logging framework.
import android.widget.Toast // Used to display short feedback messages.

// --- Jetpack Compose Imports ---
import androidx.compose.foundation.layout.* // Core layout composables (Column, Row, Spacer, Box, padding, etc.).
import androidx.compose.foundation.rememberScrollState // Remembers scroll position for scrollable containers.
import androidx.compose.foundation.verticalScroll // Modifier to make a Column scrollable.
import androidx.compose.material.icons.Icons // Access to Material Design icons.
import androidx.compose.material.icons.filled.ArrowBack // Specific back arrow icon.
import androidx.compose.material.icons.filled.Delete // Specific delete icon.
import androidx.compose.material.icons.filled.Edit // Specific edit icon.
import androidx.compose.material3.* // Material Design 3 components (Scaffold, TopAppBar, AlertDialog, Text, Button, etc.).
import androidx.compose.runtime.* // Core Compose runtime functions (Composable, remember, mutableStateOf, LaunchedEffect, etc.).
import androidx.compose.ui.Alignment // Used for aligning elements within layouts.
import androidx.compose.ui.Modifier // Base class for UI element modifiers.
import androidx.compose.ui.platform.LocalContext // Provides the current Context within a Composable.
import androidx.compose.ui.semantics.contentDescription // Modifier property for accessibility.
import androidx.compose.ui.semantics.heading // Semantic property to mark an element as a heading for accessibility.
import androidx.compose.ui.semantics.semantics // Modifier to add semantic properties.
import androidx.compose.ui.text.style.TextOverflow // Defines how visual overflow of text is handled.
import androidx.compose.ui.tooling.preview.Preview // Annotation for Android Studio previews.
import androidx.compose.ui.unit.dp // Extension for density-independent pixels.

// --- Lifecycle & Navigation Imports ---
import androidx.lifecycle.viewmodel.compose.viewModel // Composable function to obtain a ViewModel.
import androidx.navigation.NavController // Controller for navigating between screens.
import androidx.navigation.compose.rememberNavController // Remembers NavController for previews.

// --- Project Specific Imports ---
import algonquin.cst8410.finalapplication.Screen // Sealed class defining navigation routes.
import algonquin.cst8410.finalapplication.data.Note // Data class representing a note.
import algonquin.cst8410.finalapplication.notifications.NotificationHelper // Helper for notification tasks.
import algonquin.cst8410.finalapplication.ui.theme.FinalApplicationTheme // App's Compose theme.
import algonquin.cst8410.finalapplication.viewmodel.NotesViewModel // ViewModel for notes data.
import algonquin.cst8410.finalapplication.viewmodel.NotesViewModelFactory // Factory for creating NotesViewModel.

// --- Coroutines Import ---
import kotlinx.coroutines.launch // Builder for launching coroutines (for delete operation).

/**
 * Composable screen that displays the full details of a single note.
 * It shows the title, content, and reminder time (if set).
 * Provides options to navigate back, edit the note, or delete the note.
 *
 * @param navController The NavController used for navigation actions (back, edit).
 * @param viewModel The NotesViewModel instance providing data access methods.
 * @param noteId The unique ID of the note to display, passed as a navigation argument.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Suppress warning about unused padding lambda parameter in Scaffold.
@OptIn(ExperimentalMaterial3Api::class) // Opt-in required for Scaffold, TopAppBar, AlertDialog etc.
@Composable
fun NoteDetailScreen(
    navController: NavController,
    viewModel: NotesViewModel = viewModel( // Obtain ViewModel instance via factory.
        factory = NotesViewModelFactory(LocalContext.current.applicationContext as Application)
    ),
    noteId: Int // The ID of the note passed via navigation arguments.
) {
    // --- Hooks and Setup ---
    val TAG = "NoteDetailScreen" // Tag for Logcat filtering.
    Log.d(TAG, "Composable entered with Note ID: $noteId") // Log entry point.

    val context = LocalContext.current // Get current Android context for Toasts.
    val scope = rememberCoroutineScope() // Get coroutine scope for launching delete operation.

    // --- State Variables ---
    // State to hold the fetched Note object from the database. Null initially or if not found.
    var noteState by remember { mutableStateOf<Note?>(null) }
    // State to track whether the note data is currently being loaded.
    var isLoading by remember { mutableStateOf(true) }
    // State to control the visibility of the delete confirmation dialog.
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- LaunchedEffect for Data Fetching ---
    // Fetches the note details from the ViewModel when the screen is first composed
    // or if the `noteId` passed as an argument changes.
    LaunchedEffect(noteId) {
        Log.d(TAG, "LaunchedEffect started to fetch details for Note ID: $noteId")
        isLoading = true // Show loading indicator.
        noteState = null // Reset previous state before fetching new data.
        try {
            // Call the suspending function in the ViewModel to get the note by ID.
            val fetchedNote = viewModel.getNoteById(noteId)
            noteState = fetchedNote // Update the state with the result (Note object or null).
            Log.d(TAG, "ViewModel returned note data: ID=${fetchedNote?.id}, Title='${fetchedNote?.title}'")
        } catch (e: Exception) {
            // Log any errors during the database fetch.
            Log.e(TAG, "Error fetching note from ViewModel for ID: $noteId", e)
            noteState = null // Ensure state remains null on error.
        } finally {
            // Ensure the loading indicator is hidden regardless of success or failure.
            isLoading = false
            Log.d(TAG, "LaunchedEffect finished fetching. isLoading=$isLoading, noteState is null: ${noteState == null}")
        }
        // Handle the case where the note wasn't found AFTER loading attempt is complete.
        if (noteState == null && !isLoading) {
            Log.w(TAG, "Note could not be loaded (ID: $noteId). Navigating back.")
            Toast.makeText(context, "Note not found or deleted", Toast.LENGTH_SHORT).show()
            // Navigate back programmatically. Needs careful handling in LaunchedEffect.
            // Using simple popBackStack for this case.
            navController.popBackStack()
        }
    } // End LaunchedEffect

    // --- UI Structure using Scaffold ---
    Scaffold(
        // --- Top App Bar ---
        topBar = {
            TopAppBar(
                // Display note title, or "Loading..." if data isn't ready yet. Ellipsize if too long.
                title = { Text(noteState?.title ?: "Loading...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                // Back navigation icon.
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Log.d(TAG, "Back button clicked. Popping back stack.")
                            navController.popBackStack() // Use simple popBackStack for standard back navigation.
                        },
                        modifier = Modifier.semantics { contentDescription = "Back to notes list" } // Accessibility.
                    ) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back Button") }
                },
                // Action icons on the right.
                actions = {
                    // Edit Button: Navigates to the AddEditNote screen with the current note's ID.
                    IconButton(
                        onClick = {
                            Log.d(TAG, "Edit button clicked for Note ID: $noteId")
                            // Navigate using the route defined in Screen sealed class.
                            navController.navigate(Screen.AddEditNote.createRoute(noteId)) {
                                launchSingleTop = true // Prevent multiple edit screens.
                            }
                        },
                        modifier = Modifier.semantics { contentDescription = "Edit note" } // Accessibility.
                    ) { Icon(Icons.Filled.Edit, contentDescription = "Edit Button") }
                    // Delete Button: Shows the confirmation dialog.
                    IconButton(
                        onClick = {
                            Log.d(TAG, "Delete button clicked for Note ID: $noteId. Showing dialog.")
                            showDeleteDialog = true // Set state to true to display the AlertDialog.
                        },
                        modifier = Modifier.semantics { contentDescription = "Delete note" } // Accessibility.
                    ) { Icon(Icons.Filled.Delete, contentDescription = "Delete Button") }
                }
            ) // End TopAppBar
        } // End topBar lambda
    ) { paddingValues -> // Content lambda receives padding from Scaffold.
        // --- Main Content Area Logic ---
        // Uses a `when` expression to conditionally display UI based on loading/data state.
        when {
            // State 1: Data is currently being loaded.
            isLoading -> {
                Log.d(TAG, "UI Rendering State: Showing Loading Indicator.")
                // Display a centered loading indicator.
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues), // Apply padding.
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // State 2: Loading is finished, and valid note data is available (`noteState` is not null).
            noteState != null -> {
                // Smart cast `noteState` to non-null `Note` within this block.
                val note = noteState!!
                Log.d(TAG, "UI Rendering State: Showing Note Details for ID: ${note.id}")
                // Column to layout the note details vertically. Made scrollable.
                Column(
                    modifier = Modifier
                        .fillMaxSize() // Take full space.
                        .padding(paddingValues) // Apply Scaffold padding.
                        .padding(16.dp) // Add screen padding.
                        .verticalScroll(rememberScrollState()) // Allow scrolling for long content.
                ) {
                    // Display Note Title - marked as a heading for accessibility.
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.headlineSmall, // Use appropriate style.
                        modifier = Modifier.semantics { heading() } // Accessibility: Mark as heading.
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Vertical spacing.

                    // Display Reminder Time, only if it exists (`notificationTime` is not null).
                    note.notificationTime?.let { time ->
                        Text(
                            text = "Reminder set for: ${formatDateTime(time, context)}", // Format the timestamp.
                            style = MaterialTheme.typography.labelMedium, // Use appropriate style.
                            color = MaterialTheme.colorScheme.primary, // Use primary color for emphasis.
                            // Accessibility description for the reminder time text.
                            modifier = Modifier.semantics { contentDescription = "Reminder time is ${formatDateTime(time, context)}" }
                        )
                        Spacer(modifier = Modifier.height(16.dp)) // More spacing after reminder.
                    }

                    // Display Note Content.
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyLarge // Use appropriate style.
                    )
                } // End Column for note details

                // --- Delete Confirmation Dialog ---
                // This AlertDialog is conditionally displayed based on the `showDeleteDialog` state.
                if (showDeleteDialog) {
                    Log.d(TAG, "UI Rendering State: Showing Delete Confirmation Dialog.")
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false }, // Close dialog if user clicks outside.
                        // Icon(Icons.Filled.Warning, contentDescription = "Warning"), // Optional icon
                        title = { Text("Delete Note") },
                        // Provide context in the confirmation message. Use safe call for title.
                        text = { Text("Are you sure you want to permanently delete the note titled '${noteState?.title ?: ""}'?") },
                        // Confirm (Delete) Button Action.
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false // Close the dialog.
                                    Log.i(TAG, "Delete action confirmed for Note ID: $noteId")
                                    // Launch a coroutine to perform background operations (cancel notification, delete from DB).
                                    scope.launch {
                                        // Capture reminder time before noteState potentially becomes null after deletion.
                                        val timeToCancel = noteState?.notificationTime
                                        // Cancel notification if it exists.
                                        if (timeToCancel != null) {
                                            Log.d(TAG, "Cancelling potentially scheduled notification before delete.")
                                            NotificationHelper.cancelScheduledNotification(context, noteId)
                                        }
                                        // Call ViewModel to delete the note from the database.
                                        Log.d(TAG, "Calling viewModel.deleteNoteById($noteId).")
                                        viewModel.deleteNoteById(noteId)
                                        // Provide user feedback.
                                        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                                        // Navigate back to the home screen after deletion.
                                        Log.d(TAG, "Navigating back to Home screen after delete.")
                                        // Pop back stack up to (but not including) the Home route.
                                        navController.popBackStack(Screen.Home.route, inclusive = false)
                                    } // End coroutine launch
                                },
                                // Style the delete button to indicate a destructive action.
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) { Text("Delete") }
                        },
                        // Dismiss (Cancel) Button Action.
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                        }
                    ) // End AlertDialog
                } // End if (showDeleteDialog)
            } // End when (noteState != null)

            // State 3: Loading finished, but noteState is still null (error occurred during fetch).
            else -> {
                Log.w(TAG, "UI Rendering State: Showing 'Note not available' message (noteState is null after load).")
                // Display a fallback message if note data couldn't be loaded.
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues), // Apply padding.
                    contentAlignment = Alignment.Center // Center the text.
                ) {
                    Text("Note not available.")
                }
            }
        } // End when expression for content display
    } // End Scaffold
} // End NoteDetailScreen Composable


// --- Preview Function ---
/**
 * Provides a design-time preview of the NoteDetailScreen in Android Studio.
 * Uses sample data.
 */
@OptIn(ExperimentalMaterial3Api::class) // Opt-in needed for preview too if it uses experimental components.
@Preview(showBackground = true)
@Composable
fun NoteDetailScreenPreview() {
    FinalApplicationTheme { // Wrap preview in theme.
        // Create sample data for the preview.
        val fakeNote = Note(
            id = 1,
            title = "Sample Preview Title - Long Enough To Test Ellipsis Maybe",
            content = "This is the detailed content of the note.\nIt can span multiple lines and might be quite long, requiring the screen to scroll.",
            notificationTime = System.currentTimeMillis() + 3600000 // Example reminder time.
        )
        // Simulate the Scaffold structure for preview layout.
        Scaffold(
            topBar = { TopAppBar(
                title = { Text(fakeNote.title, maxLines=1, overflow=TextOverflow.Ellipsis) },
                // Provide dummy icons and content descriptions for preview.
                navigationIcon = { Icon(Icons.Default.ArrowBack, "Back")},
                actions = { Icon(Icons.Default.Edit,"Edit"); Icon(Icons.Default.Delete,"Delete")}
            ) }
        ) { padding -> // Receive padding from preview Scaffold.
            // Simulate the Column layout from the main composable.
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text(text = fakeNote.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                fakeNote.notificationTime?.let { time ->
                    Text(
                        text = "Reminder set for: ${formatDateTime(time, LocalContext.current)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(text = fakeNote.content, style = MaterialTheme.typography.bodyLarge)
            } // End preview Column
        } // End preview Scaffold
    } // End Theme wrapper
} // End Preview