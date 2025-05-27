package algonquin.cst8410.finalapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define your light color scheme using the colors from Color.kt
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = OnLightPrimary,
    onSecondary = OnLightSecondary,
    onTertiary = OnLightTertiary,
    onBackground = OnLightBackground,
    onSurface = OnLightSurface,
    onSurfaceVariant = OnLightSurfaceVariant
)

// Define your dark color scheme using the colors from Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = OnDarkPrimary,
    onSecondary = OnDarkSecondary,
    onTertiary = OnDarkTertiary,
    onBackground = OnDarkBackground,
    onSurface = OnDarkSurface,
    onSurfaceVariant = OnDarkSurfaceVariant
)

@Composable
fun FinalApplicationTheme( // This is the theme function to call
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit // Content lambda parameter
) {
    val colorScheme = when {
        // Use dynamic colors if available and enabled
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Fallback to predefined dark/light schemes
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Set status bar appearance
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color (adjust as needed, e.g., colorScheme.surface.toArgb())
            window.statusBarColor = colorScheme.primary.toArgb()
            // Set status bar icons (light or dark) based on the theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            // Optional: Set navigation bar color and icons appearance as well
            // window.navigationBarColor = colorScheme.surface.toArgb()
            // WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    // Apply the selected color scheme, typography, and shapes (if defined)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // From Type.kt
        content = content // Render the content passed via the trailing lambda
    )
}