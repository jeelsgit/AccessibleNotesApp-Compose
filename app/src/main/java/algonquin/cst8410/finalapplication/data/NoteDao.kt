package algonquin.cst8410.finalapplication.data // Package declaration, indicating this file is part of the data persistence layer.

// --- AndroidX Room Imports ---
// These annotations define how Room interacts with this interface and its methods.
import androidx.room.Dao // Marks the interface as a Data Access Object. Room uses this to generate database access code.
import androidx.room.Delete // Annotation for methods that delete one or more rows based on the provided entity instances.
import androidx.room.Insert // Annotation for methods that insert data into a table.
import androidx.room.OnConflictStrategy // Defines strategies for resolving conflicts when inserting data (e.g., duplicate primary keys).
import androidx.room.Query // Annotation used to define custom SQL queries (SELECT, UPDATE, DELETE) executed by a method.

// --- Kotlin Coroutines Import ---
import kotlinx.coroutines.flow.Flow // Represents an asynchronous stream of data. Used here to observe database changes reactively.

/**
 * Data Access Object (DAO) for the Note entity.
 * This interface defines the contract for interacting with the 'notes' table in the Room database.
 * It abstracts the underlying SQL operations. Room generates the necessary implementation at compile time.
 * The @Dao annotation identifies this interface to Room's processor.
 */
@Dao
interface NoteDao {

    /**
     * Retrieves all notes from the 'notes' table, ordered by their ID in descending order.
     * This typically results in the newest notes appearing first in the list.
     *
     * @Query annotation specifies the SQL query to be executed when this method is called.
     * SQL: "SELECT * FROM notes ORDER BY id DESC" selects all columns from the 'notes' table
     *      and sorts the result set based on the 'id' column in descending order.
     *
     * @return A Flow emitting a List of Note objects (`Flow<List<Note>>`).
     *         Using Flow enables reactive updates. The UI layer can collect (observe) this Flow,
     *         and whenever data in the 'notes' table changes (insert, update, delete),
     *         Room will automatically re-query the database and emit the new list to the Flow collectors.
     */
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>> // Use Flow for reactive updates

    /**
     * Retrieves a single note from the 'notes' table based on its unique ID.
     *
     * @Query annotation specifies the SQL query.
     * SQL: "SELECT * FROM notes WHERE id = :noteId" selects all columns from the 'notes' table
     *      for the row where the 'id' column matches the value passed in the `noteId` parameter.
     *      The colon syntax (`:noteId`) binds the method parameter to the placeholder in the query.
     *
     * suspend fun: This function performs a database operation, which can be time-consuming.
     *              Marking it as `suspend` ensures it can only be called from a coroutine or
     *              another suspend function, preventing it from blocking the main UI thread.
     *
     * @param noteId The integer ID of the note to retrieve.
     * @return A nullable Note (`Note?`). It returns the Note object if a match is found,
     *         or `null` if no note exists with the given `noteId`.
     */
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note? // Suspend for background execution

    /**
     * Inserts a new note into the 'notes' table or replaces an existing note if one with the
     * same primary key (ID) already exists.
     *
     * @Insert annotation indicates this method performs an insertion operation.
     * onConflict = OnConflictStrategy.REPLACE: Specifies the conflict resolution strategy.
     *                                         If a note with the same `id` is inserted, the
     *                                         existing row will be removed and replaced by the new one.
     *
     * suspend fun: Ensures the database write operation runs asynchronously off the main thread.
     *
     * @param note The Note object to be inserted or that will replace an existing one.
     *             If `note.id` is 0 (the default), Room treats this as an insertion and
     *             auto-generates a unique ID because the `id` field is marked with
     *             `@PrimaryKey(autoGenerate = true)` in the Note entity.
     * @return Long: Returns the rowId of the inserted item. If the item was updated (due to REPLACE),
     *              this might still return the rowId, but its primary key value (`note.id`) remains unchanged.
     *              If insertion fails, it might return -1.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNote(note: Note): Long // Note: Return type changed to Long to get the inserted row ID.

    /**
     * Deletes a specific note from the database. Room identifies the row(s) to delete
     * based on the primary key(s) of the entity instance(s) passed to the method.
     *
     * @Delete annotation marks this as a deletion method based on entity instance(s).
     * suspend fun: Ensures the database delete operation runs asynchronously.
     *
     * @param note The Note object representing the row to delete. Room uses its `id` field.
     */
    @Delete // This specific method might be less used if deleting by ID is preferred.
    suspend fun deleteNote(note: Note)

    /**
     * Deletes a note from the 'notes' table specifically by its unique ID.
     * This avoids needing to fetch the entire Note object just to delete it.
     *
     * @Query annotation allows defining a custom SQL DELETE statement.
     * SQL: "DELETE FROM notes WHERE id = :noteId" deletes rows where the 'id' column matches
     *      the provided `noteId` parameter.
     *
     * suspend fun: Ensures the database delete operation runs asynchronously.
     *
     * @param noteId The integer ID of the note to be deleted.
     */
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int) // This is likely the primary delete method used by the ViewModel.
}