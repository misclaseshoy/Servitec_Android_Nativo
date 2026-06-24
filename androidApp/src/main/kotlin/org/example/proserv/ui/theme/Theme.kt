package org.example.proserv.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AzulPetroleo,
    secondary = VerdeAdmin,
    tertiary = AzulServicios,
    background = FondoOscuro,
    surface = FondoOscuro,
    onPrimary = Color.White,
    onSecondary = AzulPetroleo,
    onTertiary = AzulPetroleo,
    onBackground = Color.White,
    onSurface = Color.White,
    error = ErrorRojo
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPetroleo,
    secondary = VerdeAdmin,
    tertiary = AzulServicios,
    background = FondoCrema,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = AzulPetroleo,
    onTertiary = AzulPetroleo,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro,
    error = ErrorRojo
)

@Composable
fun ProServTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Desactivado para mantener consistencia de marca
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
