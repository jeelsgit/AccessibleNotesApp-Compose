package algonquin.cst8410.finalapplication.notifications // Package for notification-related classes

// --- Android SDK Imports ---
import android.Manifest // Required for permission constants and potentially for @RequiresPermission.
import android.app.PendingIntent // Represents an intent to be fired later.
import android.content.BroadcastReceiver // Base class for components that receive broadcast Intents.
import android.content.Context // Provides access to application environment and services.
import android.content.Intent // Used to define the broadcast action and carry data.
import android.content.pm.PackageManager // Used to check permissions at runtime.
import android.net.Uri // Represents a URI, used here for deep linking.
import android.util.Log // Android's logging framework.
import android.widget.Toast // Used for simple feedback (e.g., on boot complete).

// --- AndroidX Core Imports ---
import androidx.annotation.RequiresPermission // Annotation to indicate a permission requirement (mostly for Lint).
import androidx.core.app.ActivityCompat // Helper for checking permissions compatibly.
import androidx.core.app.NotificationCompat // Helper for building notifications compatibly.
import androidx.core.app.NotificationManagerCompat // Helper for posting notifications compatibly.
import androidx.core.app.TaskStackBuilder // Helper for building a synthetic back stack for PendingIntents.
// import androidx.core.net.toUri // Alternative way to create Uri (not used here).

// --- Project Specific Imports ---
import algonquin.cst8410.finalapplication.MainActivity // The Activity to launch via deep link.
import algonquin.cst8410.finalapplication.R // Access to project resources (icons).

/**
 * A BroadcastReceiver responsible for handling scheduled alarms (triggered by AlarmManager)
 * and potentially other system broadcasts like BOOT_COMPLETED.
 */
class NotificationReceiver : BroadcastReceiver() {

    /**
     * Companion object to hold constants related to the receiver,
     * making them accessible without an instance (e.g., NotificationReceiver.EXTRA_NOTE_ID).
     */
    companion object {
        private const val TAG = "NotificationReceiver" // Tag for Logcat filtering.
        // Keys for Intent extras used to pass data to the receiver.
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_NOTE_TITLE = "extra_note_title"
        // Custom action string to identify intents meant for scheduled notifications. Must match NotificationHelper.
        const val ACTION_SCHEDULED_NOTIFICATION = "algonquin.cst8410.finalapplication.action.SCHEDULED_NOTIFICATION"
        // Base URI pattern for constructing note detail deep links. Must match Manifest and Navigation.kt.
        const val DEEP_LINK_URI_PATTERN = "notesapp://note/"
    }

    /**
     * This method is called when the BroadcastReceiver receives an Intent broadcast.
     * It determines the action and processes the intent accordingly.
     *
     * @param context The Context in which the receiver is running. Can be null in rare cases.
     * @param intent The Intent being received. Can be null.
     *
     * @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS) annotation is a hint for Lint/static analysis.
     * NOTE: Runtime permission checks are still MANDATORY within the method before posting notifications,
     * as this annotation doesn't enforce runtime checks itself, especially in a BroadcastReceiver context.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS) // Static analysis hint
    override fun onReceive(context: Context?, intent: Intent?) {
        // Log that the receiver was triggered and the action received.
        Log.d(TAG, "*** onReceive TRIGGERED! Action: ${intent?.action} ***")

        // Basic null checks for safety.
        if (context == null || intent == null) {
            Log.e(TAG, "Context or Intent was null in onReceive. Aborting.")
            return
        }

        // Determine how to handle the broadcast based on the Intent's action string.
        when (intent.action) {
            // Handle the custom action for our scheduled note reminders.
            ACTION_SCHEDULED_NOTIFICATION -> {
                // Extract data passed from NotificationHelper via Intent extras.
                val noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1) // Get note ID, default to -1 if not found.
                val noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE) ?: "Note Reminder" // Get title, provide default.
                Log.i(TAG, "Action matched SCHED_NOTIFICATION. Received Note ID: $noteId, Title: '$noteTitle'")
                // Proceed only if a valid note ID was received.
                if (noteId != -1) {
                    // Call the function to build and display the notification.
                    showScheduledNotification(context, noteId, noteTitle)
                } else {
                    Log.w(TAG, "Invalid Note ID (-1) received in scheduled notification intent.")
                }
            }
            // Handle the system broadcast action indicating the device has finished booting.
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.i(TAG, "Received BOOT_COMPLETED action.")
                // TODO: Implement robust rescheduling logic here.
                // This typically involves:
                // 1. Starting a background service or WorkManager job.
                // 2. Querying the database for all notes with notificationTime > current time.
                // 3. Calling NotificationHelper.scheduleNotification for each found note.
                // Showing a Toast here is just a placeholder for testing.
                Toast.makeText(context, "Notes App: Boot complete (Rescheduling placeholder)", Toast.LENGTH_SHORT).show()
            }
            // Handle any other unexpected actions.
            else -> {
                Log.w(TAG, "Received unknown broadcast action: ${intent.action}")
            }
        }
    }

    /**
     * Builds and displays the scheduled notification for a specific note.
     * This method requires the POST_NOTIFICATIONS permission at runtime.
     *
     * @param context The application context.
     * @param noteId The ID of the note to link to.
     * @param noteTitle The title of the note to display.
     *
     * @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS) annotation is a hint for Lint/static analysis.
     * A runtime check using ActivityCompat.checkSelfPermission is still performed before notifying.
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS) // Static analysis hint
    private fun showScheduledNotification(context: Context, noteId: Int, noteTitle: String) {
        Log.d(TAG, "showScheduledNotification called for Note ID: $noteId")

        // 1. Create the deep link Intent: Opens MainActivity which handles the notesapp://note/ID URI.
        val detailIntent = Intent(
            Intent.ACTION_VIEW, // Standard action for viewing data.
            Uri.parse("$DEEP_LINK_URI_PATTERN$noteId"), // Construct the specific deep link URI.
            context, // Context needed for creating the intent.
            MainActivity::class.java // Explicitly target MainActivity to handle this intent.
        )
        Log.d(TAG, "Created detail Intent with URI: ${detailIntent.dataString}")

        // 2. Create the PendingIntent using TaskStackBuilder for proper back stack navigation.
        // When the user taps the notification, this PendingIntent will launch the detailIntent.
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            // Add the intent that opens the detail screen. TaskStackBuilder reads parent info from Manifest.
            addNextIntentWithParentStack(detailIntent)
            // Get the PendingIntent. Request code must be unique per notification. Flags control behavior.
            getPendingIntent(
                NotificationHelper.RC_SCHEDULED_NOTE_BASE + noteId, // Unique request code.
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // Update if exists, immutable flag.
            )
        }

        // Ensure PendingIntent creation was successful.
        if (pendingIntent == null) {
            Log.e(TAG, "Failed to create PendingIntent using TaskStackBuilder! Cannot show notification for Note ID: $noteId")
            return
        }
        Log.d(TAG, "PendingIntent with TaskStackBuilder created successfully.")

        // Note: The following 'simplePendingIntent' block seems redundant if the TaskStackBuilder version works.
        // It creates a PendingIntent without the artificial back stack. Keeping it commented out unless needed for debugging.
        /*
        val simplePendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            NotificationHelper.RC_SCHEDULED_NOTE_BASE + noteId, // Same request code
            detailIntent, // Use the same deep link intent
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        */

        // 3. Build the notification content and appearance.
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID_SCHEDULED) // Use the correct channel ID.
            // --- !!! IMPORTANT: Replace with your actual reminder icon resource !!! ---
            .setSmallIcon(R.drawable.ic_notification_reminder) // Status bar icon.
            // --- ------------------------------------------------------------- ---
            .setContentTitle("Note Reminder") // Notification title.
            .setContentText("Time to check your note: $noteTitle") // Notification body.
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Ensure it alerts the user promptly.
            .setContentIntent(pendingIntent) // Set the PendingIntent to fire on tap (using TaskStackBuilder version).
            .setAutoCancel(true) // Dismiss the notification when the user taps it.
        // Optional: .setCategory(NotificationCompat.CATEGORY_REMINDER) // Helps system categorize notification.

        // 4. Get the NotificationManager service.
        val notificationManager = NotificationManagerCompat.from(context)

        // 5. MANDATORY Runtime Permission Check: Verify POST_NOTIFICATIONS permission before showing.
        Log.d(TAG, "Checking POST_NOTIFICATIONS permission before showing notification.")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // Log a warning and exit if permission is denied at runtime. Cannot proceed.
            Log.w(TAG, "POST_NOTIFICATIONS permission check FAILED inside receiver. Cannot show notification.")
            return
        }
        Log.d(TAG, "POST_NOTIFICATIONS permission check PASSED.") // Log success if permission granted.

        // 6. Display the notification.
        val notificationId = NotificationHelper.RC_SCHEDULED_NOTE_BASE + noteId // Use a unique ID for this notification.
        Log.i(TAG, "Attempting to display notification (ID: $notificationId)")
        try {
            // Post the notification to the NotificationManager.
            notificationManager.notify(notificationId, builder.build())
            Log.i(TAG, "Notification displayed successfully for note ID $noteId")
        } catch (e: Exception) {
            // Catch potential exceptions during the notify call (though rare if permission check passed).
            Log.e(TAG, "Error displaying notification for note ID $noteId", e)
        }
    } // End showScheduledNotification
} // End NotificationReceiver class