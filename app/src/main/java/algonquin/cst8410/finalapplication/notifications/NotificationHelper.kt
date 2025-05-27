package algonquin.cst8410.finalapplication.notifications // Package for notification-related classes

// --- Android SDK Imports ---
import android.Manifest // Required for checking the POST_NOTIFICATIONS permission.
import android.app.AlarmManager // System service used to schedule alarms (for timed notifications).
import android.app.NotificationChannel // Required for creating notification channels on Android Oreo (API 26) and higher.
import android.app.NotificationManager // System service used to manage notification channels and post notifications.
import android.app.PendingIntent // Represents an intent that can be triggered later (e.g., by AlarmManager or notification tap).
import android.content.Context // Provides access to application environment and system services.
import android.content.Intent // Used to define actions and targets for PendingIntents and broadcasts.
import android.content.pm.PackageManager // Used to check if permissions are granted.
import android.net.Uri // Used for creating deep link URIs (though not directly used in this file).
import android.os.Build // Provides information about the device's Android version (e.g., Build.VERSION.SDK_INT).
import android.provider.Settings // Can be used to navigate users to specific system settings (like exact alarm permission).
import android.util.Log // Android's logging framework for debugging.

// --- AndroidX Core Imports ---
import androidx.core.app.ActivityCompat // Helper for checking permissions.
import androidx.core.app.NotificationCompat // Helper class for building notifications with backward compatibility.
import androidx.core.app.NotificationManagerCompat // Helper for posting notifications, handles compatibility.
import androidx.core.app.TaskStackBuilder // Helper for creating PendingIntents with a proper back stack for deep links.

// --- Project Specific Imports ---
import algonquin.cst8410.finalapplication.MainActivity // The main activity to launch when immediate notification is tapped.
import algonquin.cst8410.finalapplication.R // Access to project resources (like drawable icons).

// --- Java Util Imports ---
import java.util.Date // Used for formatting timestamps in log messages.

/**
 * Singleton object providing helper functions for creating, scheduling,
 * and cancelling notifications within the application.
 */
object NotificationHelper {

    // Tag used for filtering logs in Logcat specific to this helper.
    private const val TAG = "NotificationHelper"
    // Unique ID for the immediate notification channel. Must be consistent.
    const val CHANNEL_ID_IMMEDIATE = "notes_immediate_channel"
    // Unique ID for the scheduled notification channel. Must be consistent.
    const val CHANNEL_ID_SCHEDULED = "notes_scheduled_channel"
    // Base notification ID used for immediate notifications. Offset by noteId to make unique.
    private const val IMMEDIATE_NOTIFICATION_BASE_ID = 1001
    // Base request code for PendingIntents used with scheduled alarms. Offset by noteId.
    // Also used as base for scheduled notification IDs. Needs to be different from immediate base ID.
    const val RC_SCHEDULED_NOTE_BASE = 2000

    /**
     * Creates the necessary notification channels for the application.
     * This MUST be called once (e.g., during app startup in MainActivity or Application class)
     * before any notifications can be shown on Android Oreo (API 26) or higher.
     * Does nothing on older versions.
     * @param context The application context.
     */
    fun createNotificationChannel(context: Context) {
        // Check if the Android version is Oreo or higher, as channels are only needed then.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel for immediate notifications (standard importance).
            val immediateChannel = NotificationChannel(
                CHANNEL_ID_IMMEDIATE, // Unique ID string for the channel
                "Immediate Note Notifications", // User-visible name in system settings
                NotificationManager.IMPORTANCE_DEFAULT // Default notification importance
            ).apply { description = "Notifications shown immediately after adding a note" } // User-visible description

            // Create channel for scheduled reminders (high importance to alert user).
            val scheduledChannel = NotificationChannel(
                CHANNEL_ID_SCHEDULED, // Unique ID string
                "Scheduled Note Reminders", // User-visible name
                NotificationManager.IMPORTANCE_HIGH // High importance (makes sound, pops up)
            ).apply { description = "Timed reminders for your notes" } // User-visible description

            // Get the NotificationManager system service.
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Register the channels with the system.
            notificationManager.createNotificationChannel(immediateChannel)
            notificationManager.createNotificationChannel(scheduledChannel)
            Log.i(TAG, "Notification channels created.") // Log confirmation.
        }
    }

    /**
     * Creates and displays an immediate notification, typically used after a new note is added.
     * The notification, when tapped, opens the MainActivity (Home Screen).
     * Requires the POST_NOTIFICATIONS permission on Android 13+.
     * @param context The application context.
     * @param noteTitle The title of the note to display in the notification text.
     * @param noteId The ID of the note, used to create a unique notification/PendingIntent ID.
     */
    fun sendImmediateNotification(context: Context, noteTitle: String, noteId: Int) {
        // Create an Intent to launch MainActivity when the notification is tapped.
        val intent = Intent(context, MainActivity::class.java).apply {
            // Flags to clear the task stack and start fresh or bring existing task forward.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // Wrap the Intent in a PendingIntent for the notification system to use.
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            IMMEDIATE_NOTIFICATION_BASE_ID + noteId, // Unique request code to prevent collisions.
            intent,
            // Update if exists, make immutable (recommended for security).
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification using NotificationCompat for backward compatibility.
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_IMMEDIATE) // Specify the correct channel ID.
            // --- !!! IMPORTANT: Replace with your actual icon resource !!! ---
            .setSmallIcon(R.drawable.ic_notification_icon) // Icon displayed in the status bar.
            // --- --------------------------------------------------------- ---
            .setContentTitle("Note Added") // Title text of the notification.
            .setContentText("New note '$noteTitle' was added successfully!") // Body text.
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Standard priority.
            .setContentIntent(pendingIntent) // Set the action to perform when tapped.
            .setAutoCancel(true) // Automatically dismiss the notification when tapped.

        // Check if the app has permission to post notifications BEFORE attempting to notify.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot post immediate notification - POST_NOTIFICATIONS permission missing.")
            return // Exit if permission is denied.
        }

        // Get the NotificationManagerCompat service and display the notification.
        with(NotificationManagerCompat.from(context)) {
            // Use a unique ID for the notification itself (can be same as PendingIntent request code).
            notify(IMMEDIATE_NOTIFICATION_BASE_ID + noteId, builder.build())
            Log.i(TAG, "Sent immediate notification for note ID: $noteId")
        }
    }

    /**
     * Schedules a timed notification using AlarmManager to trigger NotificationReceiver at a specific time.
     * Requires SCHEDULE_EXACT_ALARM/USE_EXACT_ALARM permission for reliable timing on Android 12+.
     * @param context The application context.
     * @param noteId The ID of the note for the reminder.
     * @param noteTitle The title of the note for the reminder text.
     * @param triggerTimeMillis The exact time (in milliseconds since epoch) when the notification should trigger.
     */
    fun scheduleNotification(context: Context, noteId: Int, noteTitle: String, triggerTimeMillis: Long) {
        // Get the AlarmManager system service safely.
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available. Cannot schedule notification for Note ID: $noteId")
            return // Cannot proceed without AlarmManager.
        }

        // Create an Intent targeting the NotificationReceiver broadcast receiver.
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            // Define a custom action to identify this specific alarm type in the receiver.
            action = NotificationReceiver.ACTION_SCHEDULED_NOTIFICATION
            // Pass necessary data (note ID and title) as extras.
            putExtra(NotificationReceiver.EXTRA_NOTE_ID, noteId)
            putExtra(NotificationReceiver.EXTRA_NOTE_TITLE, noteTitle)
            // Set the package explicitly for security (prevents other apps intercepting).
            `package` = context.packageName
        }
        // Create a PendingIntent to be broadcast when the alarm triggers.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            RC_SCHEDULED_NOTE_BASE + noteId, // Unique request code per note alarm.
            intent,
            // Update if exists, make immutable.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Log the scheduling attempt with formatted date/time.
        Log.i(TAG, "Attempting to schedule notification for Note ID: $noteId, Title: '$noteTitle', Time: ${Date(triggerTimeMillis)}")

        try {
            // Use appropriate AlarmManager method based on Android version and permissions.
            when {
                // Android 12 (S) and higher: Requires checking SCHEDULE_EXACT_ALARM/USE_EXACT_ALARM permission.
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (alarmManager.canScheduleExactAlarms()) {
                        // Permission granted: Schedule a precise alarm that works even in Doze mode.
                        Log.d(TAG, "Exact alarm permission IS granted. Calling setExactAndAllowWhileIdle.")
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                        Log.i(TAG, "Scheduled exact alarm (>= S) for note $noteId.")
                    } else {
                        // Permission denied: Fallback to an inexact alarm (using setWindow as an example).
                        // The notification will likely be delayed by the system.
                        Log.w(TAG, "Exact alarm permission IS NOT granted. Using setWindow (inexact). Timing will not be precise. Check App Settings -> Permissions -> Alarms & Reminders.")
                        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerTimeMillis - 60000, 120000, pendingIntent) // Example: 1 min before to 1 min after target.
                        Log.w(TAG, "Scheduled inexact alarm (>= S) for note $noteId.")
                        // Consider informing the user that reminders might be delayed.
                    }
                }
                // Android 6 (M) to 11 (R): setExactAndAllowWhileIdle should work without special permission check.
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    Log.d(TAG, "Scheduling alarm using setExactAndAllowWhileIdle (M-R).")
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                    Log.i(TAG, "Scheduled exact alarm (M-R) for note $noteId.")
                }
                // Below Android M: Use the older setExact method.
                else -> {
                    Log.d(TAG, "Scheduling alarm using setExact (< M).")
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                    Log.i(TAG, "Scheduled exact alarm (< M) for note $noteId.")
                }
            }
        } catch (se: SecurityException) {
            // Catch potential security exceptions (e.g., if WAKE_LOCK permission is missing or revoked).
            Log.e(TAG, "SecurityException while scheduling alarm for note $noteId. Check required permissions (WAKE_LOCK, etc.)", se)
        } catch (e: Exception) {
            // Catch any other unexpected exceptions during scheduling.
            Log.e(TAG, "Unexpected Exception while scheduling alarm for note $noteId.", e)
        }
    }

    /**
     * Cancels a previously scheduled notification alarm for a specific note ID.
     * It also attempts to dismiss the corresponding notification from the notification tray if it's visible.
     * @param context The application context.
     * @param noteId The ID of the note whose scheduled notification should be cancelled.
     */
    fun cancelScheduledNotification(context: Context, noteId: Int) {
        // Get AlarmManager safely.
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available for cancellation for Note ID: $noteId.")
            return
        }
        // Recreate the *exact same Intent* that was used for scheduling.
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_SCHEDULED_NOTIFICATION
            `package` = context.packageName // Match package too.
            // Extras are not strictly needed for cancellation, only action and request code matter.
        }
        // Recreate the *exact same PendingIntent* using the SAME request code and appropriate flags.
        // FLAG_NO_CREATE checks if an intent with this signature already exists without creating a new one.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            RC_SCHEDULED_NOTE_BASE + noteId, // MUST match the request code used for scheduling.
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE // Check existence, immutable flag must match original.
        )

        // If the PendingIntent exists (was previously scheduled), cancel it.
        if (pendingIntent != null) {
            try {
                alarmManager.cancel(pendingIntent) // Cancel the alarm associated with the PendingIntent.
                pendingIntent.cancel() // Cancel the PendingIntent itself.
                Log.i(TAG, "Cancelled scheduled notification via AlarmManager for note ID: $noteId")
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling alarm for note ID: $noteId", e)
            }
        } else {
            // Log if no matching PendingIntent was found (it might have already fired or was never set).
            Log.w(TAG, "No scheduled PendingIntent found to cancel for note ID: $noteId (might have already fired or wasn't set).")
        }

        // Also try to cancel any potentially visible notification in the status bar using its ID.
        // This is useful if the alarm fired but the user hasn't dismissed the notification yet.
        val notificationIdToCancel = RC_SCHEDULED_NOTE_BASE + noteId
        NotificationManagerCompat.from(context).cancel(notificationIdToCancel)
        Log.d(TAG, "Also called NotificationManagerCompat.cancel for notification ID: $notificationIdToCancel")
    }
}