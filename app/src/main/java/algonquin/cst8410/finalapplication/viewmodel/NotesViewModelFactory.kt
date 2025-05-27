package algonquin.cst8410.finalapplication.viewmodel // Package declaration for ViewModel related classes

// --- Android Framework Imports ---
import android.app.Application // Required to pass the application context to the ViewModel.

// --- AndroidX Lifecycle Imports ---
import androidx.lifecycle.ViewModel // Base class for ViewModels.
import androidx.lifecycle.ViewModelProvider // Interface used to instantiate ViewModel instances.

/**
 * A Factory class responsible for creating instances of `NotesViewModel`.
 * This factory is necessary because `NotesViewModel` requires an `Application` context
 * in its constructor, which cannot be provided by the default ViewModel factory.
 *
 * How it's used:
 * When requesting a `NotesViewModel` instance (e.g., using `viewModel()` composable
 * or `ViewModelProvider(owner).get(NotesViewModel::class.java)`), you provide an
 * instance of this factory. The system then calls the `create` method of this factory
 * to get the ViewModel instance.
 *
 * @param application The Application context instance, typically obtained from `LocalContext.current.applicationContext`
 *                    in a Composable or `activity.application` / `fragment.requireActivity().application` elsewhere.
 */
class NotesViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    // Inherits from ViewModelProvider.Factory, which requires implementing the 'create' method.

    /**
     * Creates a new instance of the specified `ViewModel` class.
     * This method is called by the ViewModel framework when a ViewModel instance is requested
     * for the first time for a given scope (like an Activity, Fragment, or NavBackStackEntry).
     *
     * @param T The type of the ViewModel to create (must extend androidx.lifecycle.ViewModel).
     * @param modelClass The `Class` object of the ViewModel requested.
     * @return A newly created ViewModel instance of type T.
     * @throws IllegalArgumentException if the requested `modelClass` is not `NotesViewModel`.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel class (`modelClass`) is assignable from (i.e., is or is a subclass of)
        // our `NotesViewModel` class.
        if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
            // If it is the correct class, create a new instance of NotesViewModel,
            // passing the stored `application` context to its constructor.
            // We need to cast the result to `T` (the requested type).
            @Suppress("UNCHECKED_CAST") // Suppress the compiler warning about the unchecked cast,
            // as we have already verified the class type with isAssignableFrom.
            return NotesViewModel(application) as T
        }
        // If the requested `modelClass` is not `NotesViewModel` or a subclass,
        // this factory doesn't know how to create it. Throw an exception.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}