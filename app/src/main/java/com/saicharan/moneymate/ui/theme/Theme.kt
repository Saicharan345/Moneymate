package com.saicharan.moneymate.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Navy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6F7),
    onPrimaryContainer = Navy,
    secondary = IncomeGreen,
    onSecondary = Color.White,
    secondaryContainer = IncomeGreenBg,
    onSecondaryContainer = IncomeGreenDark,
    tertiary = WarningAmber,
    onTertiary = Color.White,
    tertiaryContainer = WarningAmberBg,
    onTertiaryContainer = WarningAmber,
    error = ExpenseCoral,
    onError = Color.White,
    errorContainer = ExpenseCoralBg,
    onErrorContainer = Color(0xFF8C1D22),
    background = AppBackground,
    onBackground = Navy,
    surface = Color.White,
    onSurface = Navy,
    surfaceVariant = SurfaceTint,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF8A97AB),
    outlineVariant = OutlineSoft
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp)
)

/** Light-only fintech theme (deep navy text, green for income, coral for expenses). */
@Composable
fun MoneyMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
