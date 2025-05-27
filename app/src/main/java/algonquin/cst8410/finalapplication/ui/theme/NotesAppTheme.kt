package algonquin.cst8410.finalapplication.ui.theme // Defines the package for UI theme related classes

// --- Standard Android Imports ---
import android.app.Activity // Needed to get the current Activity's window.
import android.os.Build // Provides information about the device's Android version (e.g., Build.VERSION.SDK_INT for dynamic color check).

// --- Jetpack Compose Imports ---
import androidx.compose.foundation.isSystemInDarkTheme // Composable function to detect if the system is currently in dark mode.
import androidx.compose.material3.MaterialTheme // The main Composable function that applies Material Design theming (colors, typography, shapes).
import androidx.compose.material3.darkColorScheme // Function to create a Material 3 dark color scheme.
import androidx.compose.material3.dynamicDarkColorScheme // Function to generate a dark color scheme based on the user's wallpaper (Android 12+).
import androidx.compose.material3.dynamicLightColorScheme // Function to generate a light color scheme based on the user's wallpaper (Android 12+).
import androidx.compose.material3.lightColorScheme // Function to create a Material 3 light color scheme.
import androidx.compose.runtime.Composable // Annotation indicating a function is a Jetpack Compose UI builder.
import androidx.compose.runtime.SideEffect // Composable function to run non-Compose code (like changing status bar color) that needs to execute after composition.
import androidx.compose.ui.graphics.toArgb // Extension function to convert a Compose Color to an Android ARGB integer color value.
import androidx.compose.ui.platform.LocalContext // Composable function providing the current Android Context.
import androidx.compose.ui.platform.LocalView // Composable function providing the current Android View associated with the composition.
import androidx.core.view.WindowCompat // Compatibility helper for accessing window features like inset controllers (for status bar appearance).

// --- Potentially Incorrect Class Structure ---
// It seems this code was intended to be a top-level function `FinalApplicationTheme`
// similar to the standard template, but is currently wrapped in a class `NotesAppTheme`.
// This is UNUSUAL and likely incorrect. The comments assume it should be a top-level function.
class NotesAppTheme { // <-- This class wrapper is likely unnecessary and incorrect for a standard theme setup.

    // Define the default light color scheme using colors defined in Color.kt (e.g., Purple40).
    // This scheme is used when the system is not in dark mode and dynamic color is not enabled/available.
    private val LightColorScheme = lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40
        // Other colors (background, surface, onPrimary, etc.) will use Material 3 defaults
        // unless explicitly overridden here.
        /* Example Overrides:
        background = Color(0xFFFFFBFE),
        surface = Color(0xFFFFFBFE),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onTertiary = Color.White,
        onBackground = Color(0xFF1C1B1F),
        onSurface = Color(0xFF1C1B1F),
        */
    )

    // Define the default dark color scheme using colors defined in Color.kt (e.g., Purple80).
    // This scheme is used when the system is in dark mode and dynamic color is not enabled/available.
    private val DarkColorScheme = darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80
        // Other colors will use Material 3 dark defaults unless overridden.
        /* Example Overrides:
         background = Color(0xFF1C1B1F),
         surface = Color(0xFF1C1B1F),
         onPrimary = Color(0xFF3700B3),
         onSecondary = Color(0xFF03DAC6),
         onTertiary = Color(0xFF03DAC6),
         onBackground = Color(0xFFFFFBFE),
         onSurface = Color(0xFFFFFBFE),
        */
    )

    /**
     * The main theme composable function for the application.
     * This applies the appropriate color scheme (dynamic, light, or dark),
     * typography, and potentially shapes to the content within it.
     * It also handles setting the system status bar appearance.
     *
     * NOTE: This function is currently nested within the `NotesAppTheme` class.
     *       It should likely be a top-level function named `FinalApplicationTheme`.
     *
     * @param darkTheme Whether to force dark theme. Defaults to the system setting.
     * @param dynamicColor Whether to use dynamic colors (Material You) on Android 12+. Defaults to true.
     * @param content The Composable lambda representing the UI content that should receive the theme.
     */
    @Composable
    fun FinalApplication( // <-- Function name should likely be FinalApplicationTheme and be top-level.
        darkTheme: Boolean = isSystemInDarkTheme(), // Check system setting for dark mode.
        dynamicColor: Boolean = true, // Enable dynamic color by default.
        content: @Composable () -> Unit // Lambda parameter for the UI content.
    ) {
        // Determine the correct ColorScheme to use based on parameters and device capabilities.
        val colorScheme = when {
            // Use dynamic color if enabled and running on Android 12 (API 31) or higher.
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current // Get context for dynamic color generation.
                // Select dynamic dark or light scheme based on the darkTheme parameter.
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            // If dynamic color is not used/available, use the predefined dark scheme if darkTheme is true.
            darkTheme -> DarkColorScheme
            // Otherwise, use the predefined light scheme.
            else -> LightColorScheme
        }

        // --- Side Effect for System UI ---
        // Get the current View associated with this part of the composition.
        val view = LocalView.current
        // `SideEffect` runs after composition. Used here to interact with the Android View system (Window).
        // `!view.isInEditMode` prevents this code from running in Android Studio previews.
        if (!view.isInEditMode) {
            SideEffect {
                // Get the window of the current Activity.
                val window = (view.context as Activity).window
                // Set the status bar color. Using primary color here, could use surface or background.
                window.statusBarColor = colorScheme.primary.toArgb()
                // Set the appearance of status bar icons (light icons on dark background, dark icons on light background).
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                // --- Optional: Customize Navigation Bar ---
                // Set the navigation bar color (e.g., to match surface).
                window.navigationBarColor = colorScheme.surface.toArgb() // Example
                // Set the appearance of navigation bar icons.
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme // Example
                // --- End Optional ---
            }
        }

        // Apply the determined color scheme and typography (defined in Type.kt)
        // to the content lambda passed to this function.
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography, // Assumes Typography object is defined in Type.kt
            // shapes = Shapes, // Uncomment if you have a Shapes.kt defining custom shapes.
            content = content // Renders the UI defined within the theme block in MainActivity.
        )
    } // End Composable function FinalApplication (should be FinalApplicationTheme)
} // End class NotesAppTheme (likely should be removed)