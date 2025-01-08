package com.example.ecommerce.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.ecommerce.R

private val DarkColorScheme = darkColorScheme(
    primary = Ebony,
    onPrimary = Cream,
    secondary = Charcoal,
    onSecondary = CreamLight,
    tertiary = Sage,
    background = EbonyDark,
    surface = CharcoalDark
)
val Montserrat = FontFamily(
    // Normal (400)
    Font(
        resId = R.font.montserrat_variablefont_wght,
        weight = FontWeight.Normal,
        style = FontStyle.Normal
    ),

    Font(
        resId = R.font.montserrat_italic_variablefont_wght,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    ),
)

private val LightColorScheme = lightColorScheme(
    primary = Sage,
    onPrimary = Ebony,
    secondary = Charcoal,
    onSecondary = Cream,
    tertiary = EbonyLight,
    background = Cream,
    surface = CreamLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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