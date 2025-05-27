package algonquin.cst8410.finalapplication.data // Package declaration for data layer classes

import android.content.Context // Required for accessing application context to build the database
import androidx.room.Database // Annotation to mark this class as a Room Database
import androidx.room.Room // Provides the main access point for Room database creation (Room.databaseBuilder)
import androidx.room.RoomDatabase // Base class for Room databases

/**
 * The main database class for the application using Room persistence library.
 * It defines the database configuration and serves as the main access point
 * to the persisted data.
 *
 * @Database annotation identifies this as a Room database.
 * - entities: Lists all the data entity classes (tables) included in the database. Here, only the Note entity.
 * - version: Specifies the database version. Used for migrations when the schema changes. Must be >= 1.
 * - exportSchema: If true, Room exports the schema into a JSON file (for version control). Set to false to disable.
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() { // Inherits from RoomDatabase

    /**
     * Abstract function to get the Data Access Object (DAO) for the Note entity.
     * Room will generate the implementation for this method.
     * DAOs are used to define database interactions (queries, inserts, updates, deletes).
     * @return An instance of NoteDao.
     */
    abstract fun noteDao(): NoteDao

    /**
     * Companion object allows accessing the database instance creator method (`getDatabase`)
     * without having an instance of AppDatabase, typically via `AppDatabase.getDatabase(context)`.
     * It implements the Singleton pattern to ensure only one instance of the database exists application-wide.
     */
    companion object {
        /**
         * @Volatile annotation ensures that writes to the INSTANCE variable are immediately
         * visible to other threads, preventing potential issues in multi-threaded environments.
         * This holds the single instance of the AppDatabase. Initialized to null.
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton database instance. If the instance doesn't exist, it creates it
         * in a thread-safe way using a synchronized block.
         *
         * @param context The application context, needed to locate the database file.
         * @return The singleton AppDatabase instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Return the existing INSTANCE if it's already created (non-null).
            // The ?: elvis operator executes the block on the right only if the left side (INSTANCE) is null.
            return INSTANCE ?: synchronized(this) {
                // synchronized block ensures that only one thread can create the database instance at a time
                // if multiple threads call getDatabase concurrently when INSTANCE is null.

                // Build the database instance using Room's databaseBuilder.
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to avoid memory leaks associated with Activity/Fragment contexts.
                    AppDatabase::class.java,    // The database class Room should implement.
                    "notes_database"            // The name of the database file on the device.
                )
                    // Strategy to handle database version upgrades when no explicit migration path is provided.
                    // .fallbackToDestructiveMigration() will discard the existing database and all its data,
                    // then recreate the schema from scratch. USE WITH CAUTION IN PRODUCTION.
                    // Proper migrations should be implemented for production apps to preserve user data.
                    .fallbackToDestructiveMigration()
                    .build() // Creates the database instance.

                // Assign the newly created instance to the static INSTANCE variable.
                INSTANCE = instance

                // Return the created instance.
                instance
            } // End synchronized block
        } // End getDatabase function
    } // End companion object
} // End AppDatabase class