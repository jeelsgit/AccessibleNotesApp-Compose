package algonquin.cst8410.finalapplication // Root package declaration for the application

// --- Android SDK & Framework Imports ---
import android.Manifest // Required for accessing permission constants like POST_NOTIFICATIONS.
import android.app.AlarmManager // System service for scheduling alarms (timed notifications).
import android.content.Context // Provides access to application environment and system services.
import android.content.Intent // Represents an action to be performed, used for launching activities, broadcasts, deep links.
import android.content.pm.PackageManager // Used for checking runtime permissions status.
import android.net.Uri // Represents a URI, used for parsing deep link data and creating intents for Settings.
import android.os.Build // Provides information about the device's Android version (API level).
import android.os.Bundle // Used for saving and restoring activity state (passed to onCreate).
import android.provider.Settings // Provides access to system settings screens (like exact alarm permission).
import android.util.Log // Android's logging utility for debugging.

// --- AndroidX Activity & Compose Imports ---
import androidx.activity.ComponentActivity // Base class for activities using Jetpack components (like Compose).
import androidx.activity.compose.setContent // Extension function to set the Compose UI content for an Activity.
import androidx.activity.result.contract.ActivityResultContracts // Provides standard contracts for Activity Result APIs (used for permission requests).
import androidx.compose.foundation.layout.fillMaxSize // Compose Modifier to make a composable occupy its maximum available space.
import androidx.compose.material3.MaterialTheme // Composable that provides Material Design theming (colors, typography).
import androidx.compose.material3.Surface // A basic Material Design surface composable, often used as a root container.
import androidx.compose.ui.Modifier // Base class for applying modifiers (like size, padding) to composables.

// --- AndroidX Core Imports ---
import androidx.core.content.ContextCompat // Helper class for accessing features (like permission checks) compatibly across Android versions.

// --- Project Specific Imports ---
// Import necessary classes/composables from YOUR project structure
import algonquin.cst8410.finalapplication.notifications.NotificationHelper // Your helper for notification tasks.
import algonquin.cst8410.finalapplication.ui.theme.FinalApplicationTheme // Your defined Compose theme composable.
// Import the NavHost composable and Screen definitions from Navigation.kt
import algonquin.cst8410.finalapplication.NotesAppNavigation // Your main navigation composable.
import algonquin.cst8410.finalapplication.Screen // Your sealed class defining navigation routes.
import android.widget.Toast

/**
 * The main entry point Activity for the Notes application.
 * Responsible for setting up the Compose UI using Jetpack Navigation,
 * handling application startup logic like initializing notification channels,
 * checking/requesting permissions, and processing incoming Intents (including deep links).
 */
class MainActivity : ComponentActivity() { // Inherits from ComponentActivity for Jetpack compatibility.

    // Logging tag specific to this Activity for easy filtering in Logcat.
    private val TAG = "MainActivity"

    // Activity Result Launcher for requesting the POST_NOTIFICATIONS permission.
    // This replaces the deprecated onActivityResult/onRequestPermissionsResult methods.
    // It registers a callback to handle the user's permission grant/deny response.
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission() // Standard contract for requesting a single permission.
    ) { isGranted: Boolean -> // Lambda executed when the result is received.
        if (isGranted) {
            // Log if permission was granted by the user via the system dialog.
            Log.i(TAG, "POST_NOTIFICATIONS permission granted via launcher.")
        } else {
            // Log if permission was denied.
            Log.w(TAG, "POST_NOTIFICATIONS permission denied via launcher.")
            // TODO: Implement logic here to show a rationale (using Snackbar or Dialog)
            // explaining *why* the permission is needed for reminders the *next* time
            // the user tries to set one, guiding them to settings if they permanently denied.
        }
    }

    /**
     * Called when the activity is first created.
     * This is where most initialization should go: calling super.onCreate,
     * setting up the UI (setContent), initializing helpers, checking permissions.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Always call the superclass implementation first.
        Log.d(TAG, "onCreate called.")

        // --- Initialization ---
        // Create Notification Channels required for Android Oreo (API 26) and higher. Needs to be done early.
        NotificationHelper.createNotificationChannel(this)
        // Check/request runtime permissions needed by the app.
        askNotificationPermission() // Check/request Notification permission (Android 13+).
        checkExactAlarmPermission() // Check Exact Alarm permission status (Android 12+).

        // --- Determine Initial Navigation Target ---
        // Analyze the Intent that launched this activity to see if it's a deep link or standard launch.
        val targetDestinationRoute = determineTargetDestination(intent) // Returns the specific route string or null.
        Log.i(TAG, "Intent target destination route: ${targetDestinationRoute ?: "Home (null)"}")

        // --- Set Compose UI Content ---
        // Set the Activity's content to be managed by Jetpack Compose.
        setContent {
            // Apply the application's Material 3 theme.
            FinalApplicationTheme {
                // Surface acts as a root container with background color from the theme.
                Surface(
                    modifier = Modifier.fillMaxSize(), // Occupy the entire screen.
                    color = MaterialTheme.colorScheme.background // Set background color from theme.
                ) {
                    // Set up the Jetpack Compose Navigation graph.
                    val structuralStartRoute = Screen.Home.route // Define the base starting point for the NavHost structure.
                    Log.d(TAG,"Setting up NotesAppNavigation. Structural Start: $structuralStartRoute, Initial Target: $targetDestinationRoute")
                    // Call the main navigation composable defined in Navigation.kt.
                    NotesAppNavigation(
                        startDestination = structuralStartRoute,   // Tell NavHost where the graph definition starts.
                        initialTargetRoute = targetDestinationRoute // Pass the actual target determined from the Intent.
                        // NotesAppNavigation will handle navigating to this target if needed.
                    )
                } // End Surface
            } // End Theme
        } // End setContent
    } // End onCreate

    /**
     * Called when the activity is re-launched while at the top of the activity stack,
     * or if a new intent is delivered to an existing instance (e.g., via singleTop launch mode
     * or tapping a notification that deep links to this activity when it's already open).
     *
     * @param intent The new intent that was started for the activity. Must be nullable per override signature.
     */
    override fun onNewIntent(intent: Intent) { // Signature requires nullable Intent?
        super.onNewIntent(intent) // Call superclass implementation.
        Log.d(TAG, "onNewIntent received: Action=${intent?.action}, Data=${intent?.dataString}")
        // Replace the activity's current intent with the new one. This is important if other parts
        // of the activity (like determineTargetDestination if called again) rely on getIntent().
        setIntent(intent)

        // --- Handling navigation updates from onNewIntent in Compose ---
        // Determining the t`arget is easy, but triggering navigation within the already running
        // Compose UI requires a mechanism to pass this event/request to the NavController instance
        // managed within the NotesAppNavigation composable.
        // Common patterns: Shared ViewModel StateFlow, Event Bus.
        val target = determineTargetDestination(intent) // Re-check the target based on the new intent.
        if (target != null) {
            Log.i(TAG, "onNewIntent: Target destination determined: $target. Manual navigation trigger required within Compose UI state.")
            // TODO: Implement a mechanism (e.g., update a StateFlow in a shared ViewModel)
            //       to signal the NotesAppNavigation composable to navigate to this `target` route.
            // Example: viewModel.requestNavigation(target)
        }
        // --- For this assignment's scope, primarily handling the initial launch intent is sufficient. ---
    } // End onNewIntent

    /**
     * Analyzes the launch Intent to determine the intended navigation target route.
     * Checks for specific deep link URI patterns or known Intent extras.
     *
     * @param intent The Intent to analyze (usually from `getIntent()` in onCreate or `onNewIntent`).
     * @return The specific navigation route string (e.g., "noteDetail/3") if a deep link or specific extra is found,
     *         otherwise returns `null` to indicate a standard launch to the default start destination (Home).
     */
    private fun determineTargetDestination(intent: Intent?): String? {
        var targetRoute: String? = null // Default to null, meaning navigate to NavHost's startDestination.

        // 1. Check if the Intent action is VIEW and if it contains data (potential deep link).
        if (intent?.action == Intent.ACTION_VIEW && intent.data != null) {
            val dataUri = intent.data // Get the URI from the Intent.
            Log.d(TAG, "Intent has ACTION_VIEW and data URI: $dataUri")
            // Check if the scheme and host match the pattern defined for note details.
            if (dataUri?.scheme == "notesapp" && dataUri.host == "note") {
                val noteIdString = dataUri.lastPathSegment // Get the last part of the path (should be the ID).
                val noteId = noteIdString?.toIntOrNull() // Safely try to parse the ID string to an Int.
                // Check if parsing was successful and ID is valid.
                if (noteId != null && noteId != -1) {
                    Log.i(TAG, "Deep link target found for Note ID: $noteId in Intent data.")
                    // Construct the navigation route string for the NoteDetail screen.
                    targetRoute = Screen.NoteDetail.createRoute(noteId)
                } else {
                    Log.w(TAG, "Deep link received but Note ID part ('$noteIdString') is invalid or missing.")
                }
            } else {
                // Log if the URI doesn't match the expected scheme/host for note deep links.
                Log.w(TAG, "ACTION_VIEW received, but data URI scheme/host (${dataUri?.scheme}://${dataUri?.host}) doesn't match expected pattern (notesapp://note).")
            }
        }
        // 2. If not a recognized deep link, check for a simple "NOTE_ID" integer extra as a fallback.
        else {
            val noteIdFromExtra = intent?.getIntExtra("NOTE_ID", -1) ?: -1 // Get extra, default to -1.
            if (noteIdFromExtra != -1) {
                Log.i(TAG, "NOTE_ID extra target found in Intent: $noteIdFromExtra")
                // Construct the navigation route string.
                targetRoute = Screen.NoteDetail.createRoute(noteIdFromExtra)
            }
        }

        // Log the final determined target route (or null for default home).
        Log.d(TAG, "Determined target route: ${targetRoute ?: "(null - defaults to Home)"}")
        return targetRoute // Return the route string or null.
    } // End determineTargetDestination


    // --- Permission Handling Functions ---

    /**
     * Checks if POST_NOTIFICATIONS permission is granted on Android 13+ and requests it if not.
     * Uses the Activity Result API (`requestNotificationPermissionLauncher`).
     */
    private fun askNotificationPermission() {
        // Permission is only required on Android 13 (TIRAMISU, API 33) and higher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS // The permission string.
            // Check if the permission is already granted.
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "$permission permission already granted.")
            }
            // Check if we should show an explanation (rationale) to the user.
            // This is true if the user previously denied the permission but didn't check "Don't ask again".
            else if (shouldShowRequestPermissionRationale(permission)) {
                Log.i(TAG, "Rationale should be shown for $permission permission.")
                // TODO: Display a dialog or Snackbar explaining clearly why notifications are needed
                //       (e.g., "This app needs notification permission to show reminders you set.").
                //       After the user interacts with the rationale, then launch the request.
                // For now, request directly:
                requestNotificationPermissionLauncher.launch(permission)
            }
            // If permission is not granted and rationale isn't needed (first time asking or "Don't ask again" was checked),
            // request the permission directly.
            else {
                Log.i(TAG, "Requesting $permission permission (no rationale needed).")
                requestNotificationPermissionLauncher.launch(permission)
            }
        } else {
            // On versions below Android 13, the permission doesn't exist and is implicitly granted.
            Log.d(TAG, "POST_NOTIFICATIONS permission not required on this Android version (${Build.VERSION.SDK_INT}).")
        }
    } // End askNotificationPermission

    /**
     * Checks if the SCHEDULE_EXACT_ALARM permission is granted on Android 12+.
     * Logs the status but does NOT automatically request the permission, as it's a sensitive one
     * that requires explicit user opt-in via system settings.
     */
    private fun checkExactAlarmPermission() {
        // Permission check is only relevant on Android 12 (S, API 31) and higher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Get the AlarmManager system service safely.
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager service not available, cannot check exact alarm permission.")
                return // Cannot proceed.
            }
            // Check if the app can schedule exact alarms.
            if (alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "SCHEDULE_EXACT_ALARM permission is currently granted.")
            } else {
                // Log a warning if permission is not granted. Reminders might be unreliable.
                Log.w(TAG, "SCHEDULE_EXACT_ALARM permission NOT granted. Timed reminders might be inexact or delayed significantly by the system. User must grant manually in App Settings -> Permissions -> Alarms & reminders.")
                // IMPORTANT: Do NOT automatically redirect the user to settings on app launch.
                // This permission should ideally be requested only when the user explicitly tries
                // to set a timed reminder, accompanied by an explanation of why it's needed.
                // Consider calling `requestExactAlarmPermission()` from a dialog shown in AddEditNoteScreen.
            }
        } else {
            // On versions below Android 12, exact alarms generally work without this specific permission.
            Log.d(TAG, "SCHEDULE_EXACT_ALARM permission check not applicable on this Android version (${Build.VERSION.SDK_INT}).")
        }
    } // End checkExactAlarmPermission

    /**
     * Example function demonstrating how to navigate the user to the system settings screen
     * where they can grant the "Alarms & reminders" (Exact Alarm) permission.
     * This should ONLY be called after explaining the need to the user and getting their consent
     * (e.g., by tapping a "Go to Settings" button in a dialog).
     */
    @Suppress("unused") // Mark as potentially unused, as it's ideally called from UI interaction.
    private fun requestExactAlarmPermission() {
        // Only relevant on Android 12 (S) and higher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.i(TAG, "Navigating user to system settings for SCHEDULE_EXACT_ALARM permission.")
            try {
                // Create an Intent to open the specific settings screen for this permission.
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    // Optional: Hinting the settings app which package is requesting.
                    data = Uri.parse("package:$packageName")
                })
                // Show feedback instructing the user what to do in the settings screen.
                Toast.makeText(this, "Please enable 'Alarms & reminders' permission for reliable reminders.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Handle cases where the settings screen might not be available on a specific device/ROM.
                Log.e(TAG, "Could not open exact alarm settings screen.", e)
                Toast.makeText(this, "Could not open permission settings.", Toast.LENGTH_SHORT).show()
            }
        }
    } // End requestExactAlarmPermission

    // --- Ensure NO duplicate @Composable fun NotesAppNavigation(...) definition exists here! ---

} // End MainActivity class