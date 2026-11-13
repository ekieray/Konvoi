package com.konvoiku.touring.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Professional Blue (Navigation & Main Structure)
val TouringBlue = Color(0xFF1D4ED8)
val TouringBlueBright = Color(0xFF2563EB)
val TouringBlueLight = Color(0xFFEFF6FF)
val TouringBlueDark = Color(0xFF1E3A8A)

// Touring Orange / Amber (Primary Touring CTAs)
val RoadAmber = Color(0xFFEA580C)
val RoadAmberBright = Color(0xFFF59E0B)
val RoadAmberLight = Color(0xFFFFF7ED)
val RoadAmberDark = Color(0xFF9A3412)

// SOS Emergency Red
val SosRed = Color(0xFFDC2626)
val SosRedDark = Color(0xFF991B1B)

// Asphalt Charcoal & Clean Off-White Neutrals
val AsphaltCharcoal = Color(0xFF0F172A)
val SlateInk = Color(0xFF475569)
val MutedSteel = Color(0xFF64748B)
val CanvasLight = Color(0xFFF4F6FA)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFEEF2F7)
val OutlineSubtle = Color(0xFFE2E8F0)

// Deep Asphalt & Midnight Adventure Neutrals
val CanvasDark = Color(0xFF0A0F16)
val SurfaceDark = Color(0xFF131B26)
val SurfaceVariantDark = Color(0xFF1C2636)
val OutlineDark = Color(0xFF2D3A4F)

private val LightColorScheme = lightColorScheme(
    primary = TouringBlue,
    onPrimary = Color.White,
    primaryContainer = TouringBlueLight,
    onPrimaryContainer = TouringBlueDark,
    secondary = RoadAmber,
    onSecondary = Color.White,
    secondaryContainer = RoadAmberLight,
    onSecondaryContainer = RoadAmberDark,
    tertiary = Color(0xFF0F766E),
    onTertiary = Color.White,
    background = CanvasLight,
    onBackground = AsphaltCharcoal,
    surface = SurfaceWhite,
    onSurface = AsphaltCharcoal,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = SlateInk,
    outline = OutlineSubtle,
    error = SosRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFFFB923C),
    onSecondary = Color(0xFF431407),
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Color(0xFFFFEDD5),
    tertiary = Color(0xFF2DD4BF),
    onTertiary = Color(0xFF042F2E),
    background = CanvasDark,
    onBackground = Color(0xFFF1F5F9),
    surface = SurfaceDark,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = OutlineDark,
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

val KonvoiTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.4).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

val KonvoiShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun KonvoiKuTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = KonvoiTypography,
        shapes = KonvoiShapes,
        content = content
    )
}
