package algonquin.cst8410.finalapplication.data // Package declaration, part of the data layer

import androidx.room.Entity // Annotation from Room library to mark this class as a database entity (table).
import androidx.room.PrimaryKey // Annotation from Room library to designate a field as the primary key.

/**
 * Represents a single note entity in the Room database.
 * Each instance of this class corresponds to a row in the "notes" table.
 *
 * @Entity annotation marks this data class as a Room entity.
 * - tableName: Specifies the name of the table in the SQLite database. If omitted, the class name is used.
 */
@Entity(tableName = "notes")
data class Note( // Using a data class automatically provides equals(), hashCode(), toString(), copy().

    /**
     * The unique identifier for the note.
     * @PrimaryKey annotation designates this field as the primary key for the table.
     * - autoGenerate = true: Instructs Room to automatically generate a unique integer value
     *   for this field when a new Note object (without an explicitly set ID or with ID=0) is inserted.
     *   The generated value will typically be an auto-incrementing integer.
     * val id: Int = 0: Defines the field name (`id`), its type (`Int`), and a default value (`0`).
     * Providing a default value (especially 0) is often needed when using autoGenerate=true,
     * allowing you to create Note instances without specifying an ID before insertion.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * The title of the note.
     * This field will be stored as a TEXT column in the database.
     * It's declared as non-nullable (`String`), meaning every note must have a title.
     */
    val title: String,

    /**
     * The main content/body of the note.
     * This field will be stored as a TEXT column in the database.
     * It's declared as non-nullable (`String`), meaning content is required, although it could be an empty string "".
     */
    val content: String,

    /**
     * The optional timestamp (in milliseconds since the epoch) for the scheduled notification reminder.
     * This field will be stored as an INTEGER (or BIGINT) column in the database.
     * It's declared as nullable (`Long?`), meaning a note might not have a reminder set,
     * in which case the value stored in the database will be NULL.
     */
    val notificationTime: Long? = null
)