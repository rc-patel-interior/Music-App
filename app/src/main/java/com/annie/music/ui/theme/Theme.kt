package com.annie.music.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary                = Accent,
    onPrimary              = Color.White,
    primaryContainer       = AccentSoft,
    onPrimaryContainer     = Accent,
    secondary              = AccentAlt,
    onSecondary            = Color.White,
    secondaryContainer     = Surface3,
    onSecondaryContainer   = TextPrimary,
    tertiary               = AccentAlt,
    background             = BgDark,
    surface                = Surface1,
    surfaceVariant         = Surface2,
    onBackground           = TextPrimary,
    onSurface              = TextPrimary,
    onSurfaceVariant       = TextSecondary,
    outline                = Surface4,
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

val AppTypography = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 22.sp, lineHeight = 28.sp),
    headlineSmall  = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 18.sp, lineHeight = 24.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 16.sp, lineHeight = 22.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 15.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 13.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 11.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 10.sp),
)

@Composable
fun AnnieMusicTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = DarkColorScheme,
        shapes      = AppShapes,
        typography  = AppTypography,
        content     = content
    )
}
