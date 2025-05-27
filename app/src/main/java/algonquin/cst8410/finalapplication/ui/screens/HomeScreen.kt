package algonquin.cst8410.finalapplication.ui.screens // Package for UI screen composables

// --- Standard Android Imports ---
import android.annotation.SuppressLint // Used to suppress specific lint warnings.
import android.app.Application // Application context for ViewModel factory.
import android.content.Context // Android context for resources, formatting.
import android.util.Log // Android logging framework.

// --- Jetpack Compose Imports ---
import androidx.compose.foundation.clickable // Modifier to make a composable clickable.
import androidx.compose.foundation.layout.* // Core layout composables (Column, Row, Spacer, Box, padding, etc.).
import androidx.compose.foundation.lazy.LazyColumn // Efficiently displays scrollable lists.
import androidx.compose.foundation.lazy.items // Builder function within LazyColumn to define list items.
import androidx.compose.material.icons.Icons // Access to Material Design icons.
import androidx.compose.material.icons.filled.Add // The '+' icon.
import androidx.compose.material3.* // Material Design 3 components (Scaffold, TopAppBar, Card, FloatingActionButton, etc.).
import androidx.compose.runtime.* // Core Compose runtime functions (Composable, remember, collectAsState, etc.).
import androidx.compose.ui.Alignment // Used for aligning elements within layouts.
import androidx.compose.ui.Modifier // Base class for UI element modifiers.
import androidx.compose.ui.platform.LocalContext // Provides the current Context within a Composable.
import androidx.compose.ui.semantics.contentDescription // Modifier property for accessibility.
import androidx.compose.ui.semantics.semantics // Modifier to add semantic properties.
import androidx.compose.ui.text.style.TextOverflow // Defines how visual overflow of text is handled (e.g., Ellipsis...).
import androidx.compose.ui.tooling.preview.Preview // Annotation for Android Studio previews.
import androidx.compose.ui.unit.dp // Extension for density-independent pixels.

// --- Lifecycle & Navigation Imports ---
import androidx.lifecycle.viewmodel.compose.viewModel // Composable function to obtain a ViewModel.
import androidx.navigation.NavController // Controller for navigating between screens.
import androidx.navigation.compose.rememberNavController // Remembers NavController for previews.

// --- Project Specific Imports ---
import algonquin.cst8410.finalapplication.Screen // Sealed class defining navigation routes.
import algonquin.cst8410.finalapplication.data.Note // Data class representing a note.
import algonquin.cst8410.finalapplication.ui.theme.FinalApplicationTheme // App's Compose theme.
import algonquin.cst8410.finalapplication.viewmodel.NotesViewModel // ViewModel for notes data.
import algonquin.cst8410.finalapplication.viewmodel.NotesViewModelFactory // Factory for creating NotesViewModel.

// --- Java Util Imports (Potentially needed by formatDateTime) ---
// import java.text.SimpleDateFormat // Alternative date formatting (not used here).
import java.util.* // Needed for Date object.

/**
 * Helper function to format a timestamp (Long in milliseconds) into a user-readable date and time string.
 * Uses the device's locale and 12/24 hour settings via `android.text.format.DateFormat`.
 *
 * @param timestamp The time in milliseconds since the epoch.
 * @param context The Android context, required to access system date/time formatting preferences.
 * @return A formatted date and time string (e.g., "Jan 1, 2024, 10:30 AM").
 */
fun formatDateTime(timestamp: Long, context: Context): String {
    val date = Date(timestamp) // Create a Date object from the timestamp.
    // Get system-preferred time format (e.g., "10:30 AM" or "10:30").
    val timeFormat = android.text.format.DateFormat.getTimeFormat(context)
    // Get system-preferred medium date format (e.g., "Jan 1, 2024").
    val dateFormat = android.text.format.DateFormat.getMediumDateFormat(context)
    // Combine date and time strings.
    return "${dateFormat.format(date)}, ${timeFormat.format(date)}"
}

/**
 * The main Composable screen displaying the list of notes.
 * Provides a TopAppBar, a FloatingActionButton to add notes, and handles
 * displaying an empty state or the list of notes.
 *
 * @param navController The NavController used for navigating to other screens (Add/Edit, Detail).
 * @param viewModel The NotesViewModel instance providing the list of notes.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Suppress warning about unused padding lambda parameter.
@OptIn(ExperimentalMaterial3Api::class) // Opt-in required for Scaffold, TopAppBar, FAB, etc.
@Composable
fun HomeScreen(
    navController: NavController,
    // Obtain ViewModel instance using viewModel() composable with the factory.
    viewModel: NotesViewModel = viewModel(
        factory = NotesViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    // Observe the list of notes from the ViewModel as state. Recomposes when the list changes.
    val notesState by viewModel.allNotes.collectAsState()
    // Get current context for the date formatter.
    val context = LocalContext.current
    // Tag for logging within this screen.
    val TAG = "HomeScreen"

    // Scaffold provides basic Material Design layout structure (AppBar, FAB, content area).
    Scaffold(
        // Top App Bar configuration.
        topBar = {
            TopAppBar(
                title = { Text("My Notes") }, // Title text.
                // Customize colors using MaterialTheme color scheme.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        // Floating Action Button configuration.
        floatingActionButton = {
            FloatingActionButton(
                // Action to perform on click: navigate to the Add/Edit screen for a new note.
                onClick = {
                    Log.d(TAG, "Add Note FAB clicked. Navigating to AddEditNote.")
                    // Use createRoute helper to build the navigation route (with null/(-1) ID).
                    // Pass lambda to configure NavOptions (launchSingleTop=true prevents multiple copies).
                    navController.navigate(Screen.AddEditNote.createRoute(null)) {
                        launchSingleTop = true
                    }
                },
                // Accessibility description for the FAB itself.
                modifier = Modifier.semantics { contentDescription = "Add a new note" }
            ) {
                // Icon inside the FAB, with its own accessibility description.
                Icon(Icons.Filled.Add, contentDescription = "Add Note Button")
            }
        }
    ) { paddingValues -> // Content lambda receives padding values from Scaffold (for AppBar, etc.).
        // Check if the list of notes is empty.
        if (notesState.isEmpty()) {
            // Display a centered message indicating no notes exist.
            Box(
                modifier = Modifier
                    .fillMaxSize() // Take up all available space.
                    .padding(paddingValues) // Apply padding from Scaffold.
                    .padding(16.dp), // Add extra padding.
                contentAlignment = Alignment.Center // Center content within the Box.
            ) {
                Text(
                    "No notes yet. Tap the + button to add one!",
                    style = MaterialTheme.typography.bodyLarge, // Apply text style.
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Use appropriate color.
                )
            }
        } else {
            // Display the list of notes using LazyColumn for efficiency.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize() // Take up all available space.
                    .padding(paddingValues), // Apply padding from Scaffold.
                // Padding within the LazyColumn, around the items.
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                // Spacing between individual list items.
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // items() builder function efficiently handles list rendering.
                // Provide the list data (notesState) and a stable key (note.id) for performance.
                items(notesState, key = { note -> note.id }) { note ->
                    // Render each note using the NoteItem composable.
                    NoteItem(note = note, context = context) {
                        // Lambda passed to NoteItem, executed when the item is clicked.
                        // Navigates to the detail screen for the specific note.
                        Log.d(TAG, "Note item clicked (ID: ${note.id}). Navigating to NoteDetail.")
                        navController.navigate(Screen.NoteDetail.createRoute(note.id)) {
                            launchSingleTop = true // Prevent multiple copies of detail screen.
                        }
                    }
                }
            }
        }
    } // End Scaffold
} // End HomeScreen Composable

/**
 * Composable function representing a single item in the notes list.
 * Displays note title, content preview, and reminder time within a Card.
 *
 * @param note The Note object to display.
 * @param context The Android context needed for date formatting.
 * @param onClick A lambda function to be executed when this item is clicked.
 */
@Composable
fun NoteItem(note: Note, context: Context, onClick: () -> Unit) {
    val TAG = "NoteItem" // Logging tag for this specific composable.
    // Card provides a Material Design surface with elevation.
    Card(
        modifier = Modifier
            .fillMaxWidth() // Card takes the full available width.
            .clickable { // Make the entire card clickable.
                Log.d(TAG, "NoteItem clicked! Executing onClick lambda for Note ID: ${note.id}")
                onClick() // Execute the lambda passed from the caller (HomeScreen).
            }
            // Semantics for accessibility: describe the item and its action.
            .semantics { contentDescription = "Note titled ${note.title}. Tap to view details." },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Set card shadow.
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Set card background color.
    ) {
        // Column layout for content inside the card.
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp) // Internal padding.
        ) {
            // Display Note Title.
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium, // Use defined text style.
                maxLines = 1, // Limit title to one line.
                overflow = TextOverflow.Ellipsis, // Add "..." if title is too long.
                color = MaterialTheme.colorScheme.onSurfaceVariant // Set text color.
            )
            Spacer(modifier = Modifier.height(4.dp)) // Small vertical space.
            // Display Note Content Preview.
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium, // Use defined text style.
                maxLines = 3, // Show up to 3 lines of content.
                overflow = TextOverflow.Ellipsis, // Add "..." if content is too long.
                color = MaterialTheme.colorScheme.onSurfaceVariant // Set text color.
            )
            // Display reminder time only if it's set (not null).
            note.notificationTime?.let { time ->
                Spacer(modifier = Modifier.height(8.dp)) // Space above reminder text.
                Text(
                    // Format the timestamp using the helper function.
                    text = "Reminder: ${formatDateTime(time, context)}",
                    style = MaterialTheme.typography.labelSmall, // Use smaller label style.
                    color = MaterialTheme.colorScheme.primary // Use primary color to highlight.
                )
            }
        } // End Column
    } // End Card
} // End NoteItem Composable

// --- Preview Functions ---

/**
 * Provides a design-time preview of the HomeScreen in Android Studio.
 * Shows the screen with an empty state.
 */
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FinalApplicationTheme { // Wrap preview in the app theme.
        // Use rememberNavController() for the preview's NavController instance.
        HomeScreen(navController = rememberNavController())
    }
}

/**
 * Provides a design-time preview of a single NoteItem in Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun NoteItemPreview() {
    FinalApplicationTheme { // Wrap preview in the app theme.
        // Create a sample Note object for the preview.
        NoteItem(
            note = Note(1, "Grocery List", "Milk, Bread, Eggs, Butter, Cheese...", System.currentTimeMillis() + 3600000), // Sample data
            context = LocalContext.current, // Use context from preview environment.
            onClick = {} // Provide an empty lambda for the preview click action.
        )
    }
}