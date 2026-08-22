package com.mark.simplecountdown.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mark.simplecountdown.model.AppThemeColor

private val CoralLight = lightScheme(
    primary = 0xFF9B3E32,
    primaryContainer = 0xFFFFDAD4,
    onPrimaryContainer = 0xFF3B0905,
    secondary = 0xFF775651,
    background = 0xFFFFF8F6,
)
private val CoralDark = darkScheme(
    primary = 0xFFFFB4A9,
    onPrimary = 0xFF5F160F,
    primaryContainer = 0xFF7D2A23,
    onPrimaryContainer = 0xFFFFDAD4,
    secondary = 0xFFE7BDB6,
    background = 0xFF1A1110,
)
private val OceanLight = lightScheme(0xFF006A6A, 0xFF9CF1EF, 0xFF002020, 0xFF4A6363, 0xFFF4FBFA)
private val OceanDark = darkScheme(0xFF80D5D3, 0xFF003737, 0xFF00504F, 0xFF9CF1EF, 0xFFB1CCCC, 0xFF0E1515)
private val ForestLight = lightScheme(0xFF3F6541, 0xFFC1EDBC, 0xFF002107, 0xFF52634F, 0xFFF7FBF2)
private val ForestDark = darkScheme(0xFFA6D1A2, 0xFF103916, 0xFF28502B, 0xFFC1EDBC, 0xFFBACCB6, 0xFF101510)
private val VioletLight = lightScheme(0xFF6D4EA2, 0xFFEBDDFF, 0xFF260058, 0xFF625B70, 0xFFFFF7FF)
private val VioletDark = darkScheme(0xFFD4BBFF, 0xFF3D1D70, 0xFF543687, 0xFFEBDDFF, 0xFFCCC2DC, 0xFF16121B)
private val AmberLight = lightScheme(0xFF805600, 0xFFFFDDAE, 0xFF291800, 0xFF6E5C3F, 0xFFFFF8F2)
private val AmberDark = darkScheme(0xFFFFBA43, 0xFF442B00, 0xFF614000, 0xFFFFDDAE, 0xFFDDC2A1, 0xFF19130B)
private val SkyLight = lightScheme(0xFF315DA8, 0xFFD8E2FF, 0xFF001A41, 0xFF565E71, 0xFFF9F9FF)
private val SkyDark = darkScheme(0xFFAEC6FF, 0xFF002E69, 0xFF16458F, 0xFFD8E2FF, 0xFFBEC6DC, 0xFF101318)

@Composable
fun SimpleCountdownTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: AppThemeColor = AppThemeColor.CORAL,
    content: @Composable () -> Unit,
) {
    val colors = when (themeColor) {
        AppThemeColor.CORAL -> if (darkTheme) CoralDark else CoralLight
        AppThemeColor.OCEAN -> if (darkTheme) OceanDark else OceanLight
        AppThemeColor.FOREST -> if (darkTheme) ForestDark else ForestLight
        AppThemeColor.VIOLET -> if (darkTheme) VioletDark else VioletLight
        AppThemeColor.AMBER -> if (darkTheme) AmberDark else AmberLight
        AppThemeColor.SKY -> if (darkTheme) SkyDark else SkyLight
    }
    MaterialTheme(colorScheme = colors, content = content)
}

private fun lightScheme(
    primary: Long,
    primaryContainer: Long,
    onPrimaryContainer: Long,
    secondary: Long,
    background: Long,
): ColorScheme = lightColorScheme(
    primary = Color(primary),
    onPrimary = Color.White,
    primaryContainer = Color(primaryContainer),
    onPrimaryContainer = Color(onPrimaryContainer),
    secondary = Color(secondary),
    background = Color(background),
    surface = Color(background),
)

private fun darkScheme(
    primary: Long,
    onPrimary: Long,
    primaryContainer: Long,
    onPrimaryContainer: Long,
    secondary: Long,
    background: Long,
): ColorScheme = darkColorScheme(
    primary = Color(primary),
    onPrimary = Color(onPrimary),
    primaryContainer = Color(primaryContainer),
    onPrimaryContainer = Color(onPrimaryContainer),
    secondary = Color(secondary),
    background = Color(background),
    surface = Color(background),
)
