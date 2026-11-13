package com.konvoiku.touring.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Subtle touring atmosphere canvas backdrop:
 * - Light mode: Clean slate off-white with low-opacity topographic contours & highway dashed curves.
 * - Dark mode: Deep asphalt charcoal -> midnight adventure blue -> subtle golden hour horizon glow.
 * All foreground Material 3 cards keep solid surface colors for optimal contrast.
 */
@Composable
fun TouringDashboardCanvasBackdrop(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = isSystemInDarkTheme()
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (isDarkTheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0A0F16),
                                Color(0xFF0E1624),
                                Color(0xFF0A0F16)
                            )
                        )
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF3F6FA),
                                Color(0xFFF8FAFC),
                                Color(0xFFFDF8F4)
                            )
                        )
                    )
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Topographic contour arcs at very low opacity
            val contourColor = if (isDarkTheme) {
                Color(0xFF93C5FD).copy(alpha = 0.055f)
            } else {
                Color(0xFF1E3A8A).copy(alpha = 0.045f)
            }

            for (i in 1..5) {
                val offset = i * 44.dp.toPx()
                val contourPath = Path().apply {
                    moveTo(-w * 0.1f, h * 0.18f + offset)
                    cubicTo(
                        w * 0.28f, h * 0.06f + offset * 0.8f,
                        w * 0.65f, h * 0.30f + offset * 0.9f,
                        w * 1.1f, h * 0.15f + offset
                    )
                }
                drawPath(
                    path = contourPath,
                    color = contourColor,
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // Subtle winding road centerline near bottom whitespace
            val roadColor = if (isDarkTheme) {
                Color(0xFFFB923C).copy(alpha = 0.07f)
            } else {
                Color(0xFFEA580C).copy(alpha = 0.055f)
            }
            val roadPath = Path().apply {
                moveTo(-w * 0.05f, h * 0.88f)
                cubicTo(
                    w * 0.32f, h * 0.72f,
                    w * 0.68f, h * 0.96f,
                    w * 1.08f, h * 0.78f
                )
            }
            drawPath(
                path = roadPath,
                color = roadColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 16f))
                )
            )
        }
    }
}

/**
 * Atmospheric Hero Header for the Dashboard:
 * Combines Asphalt Charcoal, Midnight Blue, and warm Golden Hour Horizon accents
 * with subtle topographic contour lines behind high-contrast white typography.
 */
@Composable
fun TouringHeroAtmosphereHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.65f)),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0B1117), // Asphalt charcoal
                            Color(0xFF172554), // Midnight adventure blue
                            Color(0xFF7C2D12)  // Golden hour horizon warmth
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(900f, 480f)
                    )
                )
        ) {
            // Subtle topographic contour + horizon glow in background layer
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Golden hour horizon soft glow at bottom-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF59E0B).copy(alpha = 0.24f),
                            Color(0xFFEA580C).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.88f, h * 0.82f),
                        radius = w * 0.55f
                    ),
                    radius = w * 0.55f,
                    center = Offset(w * 0.88f, h * 0.82f)
                )

                // Subtle topographic contour lines
                for (i in 1..4) {
                    val shift = i * 16.dp.toPx()
                    val path = Path().apply {
                        moveTo(w * 0.35f, -10f + shift)
                        cubicTo(
                            w * 0.58f, h * 0.32f + shift,
                            w * 0.78f, h * 0.12f + shift,
                            w * 1.05f, h * 0.52f + shift
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.08f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Subtle winding touring highway trace
                val roadTrace = Path().apply {
                    moveTo(w * 0.52f, h * 1.05f)
                    cubicTo(
                        w * 0.68f, h * 0.65f,
                        w * 0.84f, h * 0.72f,
                        w * 1.02f, h * 0.28f
                    )
                }
                drawPath(
                    path = roadTrace,
                    color = Color(0xFFFDBA74).copy(alpha = 0.28f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                    )
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 19.sp
                )
            }
        }
    }
}
