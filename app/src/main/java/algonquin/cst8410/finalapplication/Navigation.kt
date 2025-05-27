package algonquin.cst8410.finalapplication // Root package declaration

// --- Android Framework Imports ---
import android.app.Application // Needed for ViewModelFactory to get application context.
import android.util.Log // Android logging utility.

// --- Jetpack Compose Runtime Imports ---
import androidx.compose.runtime.Composable // Annotation for Composable functions.
import androidx.compose.runtime.LaunchedEffect // Coroutine scope tied to the Composable lifecycle, used for side effects like initial navigation.

// --- Jetpack Compose UI Imports ---
import androidx.compose.ui.platform.LocalContext // Provides the current Android Context within a Composable.

// --- AndroidX Lifecycle & ViewModel Imports ---
import androidx.lifecycle.viewmodel.compose.viewModel // Composable helper to obtain a ViewModel instance.

// --- AndroidX Navigation Compose Imports ---
import androidx.navigation.NavHostController // Controller class for Compose Navigation.
import androidx.navigation.NavType // Defines types for navigation arguments (e.g., IntType, StringType).
import androidx.navigation.compose.NavHost // Composable that defines the navigation graph container.
import androidx.navigation.compose.composable // Builder function within NavHost to define a specific destination screen.
import androidx.navigation.compose.rememberNavController // Remembers a NavController across recompositions (mainly for previews).
import androidx.navigation.navArgument // Builder function to define arguments for a navigation route.
import androidx.navigation.navDeepLink // Builder function to associate deep link URIs with a destination.

// --- Project Specific Imports ---
// Import screen composables defined in the ui.screens package.
import algonquin.cst8410.finalapplication.ui.screens.AddEditNoteScreen
import algonquin.cst8410.finalapplication.ui.screens.HomeScreen
import algonquin.cst8410.finalapplication.ui.screens.NoteDetailScreen
// Import ViewModel and its Factory.
import algonquin.cst8410.finalapplication.viewmodel.NotesViewModel
import algonquin.cst8410.finalapplication.viewmodel.NotesViewModelFactory

// --- Sealed class defining screen routes and helpers ---
/**
 * Defines the distinct navigation destinations (screens) in the application.
 * Using a sealed class provides type safety and allows defining helper functions
 * associated with specific routes (like creating routes with arguments).
 *
 * @param route The string representation of the navigation route used by the NavController.
 *              Arguments are defined using curly braces (e.g., "{noteId}").
 *              Optional arguments use query parameter syntax (e.g., "?noteId={noteId}").
 */
sealed class Screen(val route: String) {
    // Represents the Home screen destination.
    object Home : Screen("home")

    // Represents the Add/Edit Note screen destination.
    // Includes an optional argument `noteId` for editing existing notes.
    // If `noteId` is not provided (or is -1), it implies adding a new note.
    object AddEditNote : Screen("addEditNote?noteId={noteId}") {
        /**
         * Helper function to construct the full route string for navigating to AddEditNote,
         * including the noteId query parameter if provided.
         * @param noteId The ID of the note to edit, or null/(-1) to add a new note.
         * @return The complete route string (e.g., "addEditNote?noteId=5" or "addEditNote?noteId=-1").
         */
        fun createRoute(noteId: Int?): String {
            return "addEditNote?noteId=${noteId ?: -1}" // Use -1 if noteId is null.
        }
    }

    // Represents the Note Detail screen destination.
    // Includes a mandatory argument `noteId`.
    object NoteDetail : Screen("noteDetail/{noteId}") {
        /**
         * Helper function to construct the full route string for navigating to NoteDetail.
         * @param noteId The ID of the note to display.
         * @return The complete route string (e.g., "noteDetail/5").
         */
        fun createRoute(noteId: Int) = "noteDetail/$noteId"

        /**
         * The deep link URI pattern associated with this destination.
         * This MUST exactly match the `<data>` tag in AndroidManifest.xml and the URI
         * used when creating the PendingIntent in NotificationReceiver.kt.
         */
        val uriPattern = "notesapp://note/{noteId}"
    }
}


// --- Navigation Host Composable ---
/**
 * Sets up the main navigation graph for the application using Jetpack Compose Navigation.
 * Defines all possible destinations and the transitions between them.
 * Handles initial navigation based on deep links or launch intents.
 *
 * @param navController The NavHostController instance that manages navigation state. Defaults to a remembered instance.
 * @param notesViewModel The NotesViewModel instance, shared across screens within this graph that need it.
 * @param startDestination The route string defining the starting point of the NavHost's *structure* (e.g., "home").
 * @param initialTargetRoute An optional route string representing the *actual* screen the app should display
 *                           immediately upon launch (e.g., "noteDetail/3" if launched from a deep link). If null,
 *                           the app stays on the `startDestination`.
 */
@Composable
fun NotesAppNavigation(
    navController: NavHostController = rememberNavController(), // Manages navigation stack.
    notesViewModel: NotesViewModel = viewModel( // Provides data access via ViewModel.
        factory = NotesViewModelFactory(LocalContext.current.applicationContext as Application) // Factory needed for Application context.
    ),
    startDestination: String, // Structural start point (e.g., Screen.Home.route).
    initialTargetRoute: String? // Target route for initial navigation (e.g., deep link target or null).
) {
    val TAG = "NotesAppNavigation" // Logging Tag.
    Log.d(TAG, "NavHost composable rendered. Structural Start: $startDestination, Initial Target: $initialTargetRoute")

    // --- LaunchedEffect for Handling Initial Deep Link Navigation ---
    // This effect runs once when NotesAppNavigation is first composed, or if the keys (target route/navController) change.
    // It handles navigating away from the structural startDestination if an initialTargetRoute is specified.
    LaunchedEffect(initialTargetRoute, navController) {
        // Check if a specific target route was provided from MainActivity and it's not the same as the structural start.
        if (initialTargetRoute != null && initialTargetRoute != startDestination) {
            Log.i(TAG, "LaunchedEffect: Triggering initial navigation to target route: $initialTargetRoute")
            try {
                // Perform the programmatic navigation.
                navController.navigate(initialTargetRoute) {
                    // launchSingleTop = true: Avoids creating multiple instances of the target screen if triggered rapidly.
                    launchSingleTop = true
                    // Optional: Modify the back stack. Example: Remove the 'home' screen from back stack if deep linking.
                    // popUpTo(startDestination) { inclusive = true }
                }
                Log.i(TAG, "LaunchedEffect: Navigation command issued for $initialTargetRoute.")
            } catch (e: IllegalArgumentException) {
                // Catch errors if the provided initialTargetRoute doesn't match any defined composable route.
                Log.e(TAG, "LaunchedEffect: Navigation failed for route '$initialTargetRoute'. Route likely not defined in NavHost graph.", e)
                // TODO: Consider navigating to a safe default (like home) or showing an error if the deep link is invalid.
            } catch (e: Exception) {
                // Catch any other unexpected errors during the initial navigation attempt.
                Log.e(TAG, "LaunchedEffect: Unexpected error during initial navigation to $initialTargetRoute", e)
            }
        } else {
            // Log if no specific initial target was provided or if it was just the default home screen.
            Log.d(TAG, "LaunchedEffect: No initial target route provided, or target matches start destination. Staying on $startDestination.")
        }
    } // --- End LaunchedEffect ---


    // --- Define the Navigation Graph using NavHost ---
    // The NavHost composable acts as the container for all navigation destinations.
    NavHost(
        navController = navController, // The controller managing this graph.
        startDestination = startDestination // The route to display *structurally* first (e.g., "home").
    ) {

        // Define the composable associated with the "Home" route.
        composable(Screen.Home.route) { // Takes the route string from the sealed class.
            Log.d(TAG, "Composing NavHost destination: ${Screen.Home.route}") // Log route composition.
            // Call the HomeScreen composable, passing the necessary NavController and ViewModel.
            HomeScreen(navController = navController, viewModel = notesViewModel)
        }

        // Define the composable associated with the "Add/Edit Note" route.
        composable(
            route = Screen.AddEditNote.route, // Route string with optional argument placeholder.
            // Define the navigation arguments expected by this route.
            arguments = listOf(navArgument("noteId") { // Argument named "noteId".
                type = NavType.IntType // Expected data type.
                defaultValue = -1 // Default value if not provided (used for "Add" mode).
            })
        ) { backStackEntry -> // Lambda provides access to the NavBackStackEntry, which contains arguments.
            // Retrieve the 'noteId' argument from the back stack entry.
            val noteIdArg = backStackEntry.arguments?.getInt("noteId")
            Log.d(TAG, "Composing NavHost destination: ${Screen.AddEditNote.route}, noteIdArg: $noteIdArg")
            // Call the AddEditNoteScreen composable.
            AddEditNoteScreen(
                navController = navController,
                viewModel = notesViewModel,
                // Pass null if the argument was the default value (-1), indicating "Add" mode.
                noteId = if (noteIdArg == -1) null else noteIdArg
            )
        }

        // Define the composable associated with the "Note Detail" route.
        composable(
            route = Screen.NoteDetail.route, // Route string with mandatory argument placeholder.
            // Define the mandatory 'noteId' argument.
            arguments = listOf(navArgument("noteId") {
                type = NavType.IntType
                // No defaultValue means this argument MUST be provided in the route.
            }),
            // Associate the deep link URI pattern with this destination.
            deepLinks = listOf(navDeepLink { uriPattern = Screen.NoteDetail.uriPattern })
        ) { backStackEntry -> // Lambda provides access to arguments.
            // Retrieve the mandatory 'noteId' argument.
            val noteIdArg = backStackEntry.arguments?.getInt("noteId")
            Log.d(TAG, "Composing NavHost destination: ${Screen.NoteDetail.route}, noteIdArg: $noteIdArg")
            // Validate the retrieved argument before calling the screen composable.
            if (noteIdArg != null && noteIdArg != -1) { // Check for null and potential default value misuse.
                // Call the NoteDetailScreen composable with the valid ID.
                NoteDetailScreen(
                    navController = navController,
                    viewModel = notesViewModel,
                    noteId = noteIdArg
                )
            } else {
                // Handle the error case where an invalid ID was somehow passed.
                Log.e(TAG, "Invalid Note ID ($noteIdArg) found when composing NoteDetail destination! Navigating home as fallback.")
                // Use LaunchedEffect to navigate safely outside the main composition flow.
                LaunchedEffect(Unit) {
                    navController.popBackStack(Screen.Home.route, inclusive = false) // Go back to home screen.
                }
            }
        } // End NoteDetail composable definition

    } // End NavHost
} // End NotesAppNavigation composable