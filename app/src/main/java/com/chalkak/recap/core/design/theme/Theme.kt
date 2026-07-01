package com.chalkak.recap.core.design.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RecapBlue300,
    onPrimary = RecapGray900,
    primaryContainer = RecapBlue900,
    onPrimaryContainer = RecapBlue50,
    secondary = RecapBlue500,
    onSecondary = RecapGray100,
    secondaryContainer = RecapGray700,
    onSecondaryContainer = RecapGray100,
    tertiary = RecapBlue50,
    onTertiary = RecapGray900,
    background = RecapGray900,
    onBackground = RecapGray100,
    surface = RecapGray900,
    onSurface = RecapGray100,
    surfaceVariant = RecapGray700,
    onSurfaceVariant = RecapGray300,
    surfaceContainer = RecapGray700,
    surfaceContainerHighest = RecapGray500,
    outline = RecapGray500,
    outlineVariant = RecapGray700
)

private val LightColorScheme = lightColorScheme(
    primary = RecapBlue500,
    onPrimary = RecapGray100,
    primaryContainer = RecapBlue50,
    onPrimaryContainer = RecapBlue900,
    secondary = RecapBlue300,
    onSecondary = RecapGray900,
    secondaryContainer = RecapBlue50,
    onSecondaryContainer = RecapBlue900,
    tertiary = RecapBlue900,
    onTertiary = RecapGray100,
    background = androidx.compose.ui.graphics.Color.White,
    onBackground = RecapGray900,
    surface = androidx.compose.ui.graphics.Color.White,
    onSurface = RecapGray900,
    surfaceVariant = RecapGray100,
    onSurfaceVariant = RecapGray500,
    surfaceContainer = RecapBlue50,
    surfaceContainerHighest = RecapGray100,
    outline = RecapGray300,
    outlineVariant = RecapGray200
)

@Composable
fun RECAPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
