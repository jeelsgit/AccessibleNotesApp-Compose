package algonquin.cst8410.finalapplication.viewmodel // Package declaration for ViewModel classes

// --- Android Framework Imports ---
import android.app.Application // Required by AndroidViewModel to access the application context.
import android.util.Log // Android's logging framework.

// --- AndroidX Lifecycle Imports ---
import androidx.lifecycle.AndroidViewModel // Base class for ViewModels that need an Application context.
import androidx.lifecycle.viewModelScope // The CoroutineScope tied to this ViewModel's lifecycle. Automatically cancels coroutines when the ViewModel is cleared.

// --- Project Specific Data Layer Imports ---
import algonquin.cst8410.finalapplication.data.AppDatabase // The Room database class.
import algonquin.cst8410.finalapplication.data.Note // The data entity class representing a note.
import algonquin.cst8410.finalapplication.data.NoteDao // The Data Access Object interface for note operations.

// --- Kotlin Coroutines Imports ---
import kotlinx.coroutines.flow.SharingStarted // Defines strategies for starting and stopping the sharing of a Flow in a StateFlow.
import kotlinx.coroutines.flow.StateFlow // A Flow that represents a read-only state with a single updatable data value that emits updates to collectors. Ideal for exposing UI state.
import kotlinx.coroutines.flow.stateIn // Operator to convert a cold Flow into a hot StateFlow.
import kotlinx.coroutines.launch // Coroutine builder used to launch background tasks without blocking the current thread.

/**
 * ViewModel for managing the Notes data and providing it to the UI (Compose screens).
 * It interacts with the NoteDao to perform database operations (CRUD - Create, Read, Update, Delete).
 * Inherits from AndroidViewModel to get access to the Application context, which is needed
 * to instantiate the Room database singleton.
 *
 * @param application The Application context passed automatically by the ViewModelProvider/Factory
 *                    when the ViewModel is instantiated.
 */
class NotesViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Private instance of the NoteDao (Data Access Object).
     * This provides the interface for all database operations related to Notes.
     * It's initialized immediately by:
     * 1. Getting the singleton instance of the AppDatabase using the application context.
     * 2. Calling the abstract `noteDao()` method on the database instance (Room generates the implementation).
     */
    private val noteDao: NoteDao = AppDatabase.getDatabase(application).noteDao()

    /**
     * A public StateFlow that holds the current list of all notes retrieved from the database.
     * This Flow is designed to be observed by the UI (Compose screens). When the underlying
     * data in the 'notes' table changes, this StateFlow automatically emits the new list.
     *
     * - `noteDao.getAllNotes()`: Calls the DAO function which returns a `Flow<List<Note>>`. This Flow
     *   observes the database table for changes.
     * - `.stateIn(...)`: Converts the "cold" Flow from the DAO into a "hot" StateFlow suitable for UI state.
     *   - `scope = viewModelScope`: The StateFlow is managed within the lifecycle scope of this ViewModel.
     *     Collection stops automatically when the ViewModel is cleared, preventing memory leaks.
     *   - `started = SharingStarted.WhileSubscribed(5000L)`: Configures the StateFlow to start collecting
     *     from the database Flow only when there's at least one active UI collector (observer). It keeps
     *     collecting for 5 seconds after the last collector disappears (useful for surviving configuration changes).
     *   - `initialValue = emptyList()`: Sets the initial value of the StateFlow to an empty list. This prevents
     *     the UI from having a null state while the first database query is executing.
     */
    val allNotes: StateFlow<List<Note>> = noteDao.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // Keep Flow active for 5s after last UI observer leaves.
            initialValue = emptyList() // Start with an empty list until DB provides data.
        )

    /**
     * Retrieves a single note from the database based on its unique ID.
     * This is declared as a `suspend` function because database operations can be time-consuming
     * and should not block the main UI thread. It must be called from within a coroutine scope
     * (like `viewModelScope.launch` or a `LaunchedEffect` in Compose).
     *
     * @param noteId The integer ID of the note to fetch.
     * @return The `Note` object if found, or `null` if no note with the specified ID exists.
     */
    suspend fun getNoteById(noteId: Int): Note? {
        // Directly calls the corresponding suspend function in the NoteDao.
        return noteDao.getNoteById(noteId)
    }

    /**
     * Inserts a new note into the database or updates an existing one if the Note object's ID matches an existing entry.
     * This function launches a new coroutine in the `viewModelScope` to perform the database
     * operation off the main thread. This is a "fire-and-forget" operation; it doesn't return the result.
     * (See `insertAndGetId` for inserting and getting the ID back).
     *
     * @param note The `Note` object containing the data to be inserted or updated.
     *             If `note.id` is 0, Room treats it as an insert and generates a new ID.
     *             If `note.id` matches an existing ID, Room replaces the existing entry (due to `OnConflictStrategy.REPLACE`).
     */
    fun insertOrUpdateNote(note: Note) = viewModelScope.launch {
        // Calls the suspend function in the NoteDao within a background coroutine.
        noteDao.insertOrUpdateNote(note) // This specific DAO call doesn't return the ID.
    }

    /**
     * Inserts a new note (or updates if ID exists) and returns the row ID of the inserted/updated note.
     * This is a `suspend` function, intended to be called from a coroutine when the newly generated ID is needed immediately
     * (e.g., for scheduling notifications right after saving a *new* note).
     *
     * @param note The `Note` object to insert or update.
     * @return The `Long` row ID assigned by the database upon successful insertion/replacement. May return -1 or throw exception on error depending on Room implementation/conflict strategy.
     */
    suspend fun insertAndGetId(note: Note): Long {
        Log.d("NotesViewModel", "Calling DAO's insertOrUpdateNote to insert/update and get ID.")
        // Call the DAO method that is declared to return a Long (the row ID).
        val returnedId = noteDao.insertOrUpdateNote(note) // Assumes NoteDao.insertOrUpdateNote now returns Long.
        Log.d("NotesViewModel", "DAO returned row ID: $returnedId")
        // Return the ID obtained from the DAO.
        return returnedId
    }

    /**
     * Deletes a note from the database based on its unique ID.
     * Launches a new coroutine within the `viewModelScope` for asynchronous execution,
     * ensuring the main thread is not blocked.
     *
     * @param noteId The integer ID of the note to be deleted.
     */
    fun deleteNoteById(noteId: Int) = viewModelScope.launch {
        // Calls the suspend function in the NoteDao to perform the deletion.
        noteDao.deleteNoteById(noteId)
    }
}