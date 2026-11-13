package com.konvoiku.touring.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SportsMotorsports
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.konvoiku.touring.model.AvatarPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ConvoyAvatar(
    preset: AvatarPreset,
    photoUri: String?,
    displayName: String,
    size: Dp = 40.dp,
    borderColor: Color = Color.White,
    borderWidth: Dp = 2.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loadedBitmap = produceState<ImageBitmap?>(initialValue = null, key1 = photoUri) {
        value = if (!photoUri.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(photoUri))?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        } else {
            null
        }
    }.value

    val gradientColors = when (preset) {
        AvatarPreset.HELMET_TOURING -> listOf(Color(0xFFEA580C), Color(0xFFF59E0B))
        AvatarPreset.HELMET_SPORT -> listOf(Color(0xFF1D4ED8), Color(0xFF38BDF8))
        AvatarPreset.RIDER_CAPTAIN -> listOf(Color(0xFF1E3A8A), Color(0xFF2563EB))
        AvatarPreset.PILOT_CLASSIC -> listOf(Color(0xFF0F766E), Color(0xFF14B8A6))
        AvatarPreset.RALLY_PRO -> listOf(Color(0xFFB91C1C), Color(0xFFF97316))
        AvatarPreset.STANDARD_USER -> listOf(Color(0xFF475569), Color(0xFF64748B))
    }

    Surface(
        shape = CircleShape,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = 2.dp,
        modifier = modifier.size(size)
    ) {
        if (loadedBitmap != null) {
            Image(
                bitmap = loadedBitmap,
                contentDescription = "Avatar $displayName",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                when (preset) {
                    AvatarPreset.HELMET_TOURING,
                    AvatarPreset.HELMET_SPORT,
                    AvatarPreset.RALLY_PRO -> {
                        // Crisp Stylized Helmet Badge + Visor Accent
                        Canvas(modifier = Modifier.fillMaxSize(0.76f)) {
                            val w = this.size.width
                            val h = this.size.height
                            // Helmet shell
                            drawRoundRect(
                                color = Color.White,
                                topLeft = Offset(w * 0.14f, h * 0.16f),
                                size = Size(w * 0.72f, h * 0.64f),
                                cornerRadius = CornerRadius(w * 0.36f, w * 0.36f)
                            )
                            // Tinted visor
                            drawRoundRect(
                                color = when (preset) {
                                    AvatarPreset.HELMET_SPORT -> Color(0xFF0F172A)
                                    AvatarPreset.RALLY_PRO -> Color(0xFFFDE047)
                                    else -> Color(0xFF1E293B)
                                },
                                topLeft = Offset(w * 0.30f, h * 0.34f),
                                size = Size(w * 0.52f, h * 0.24f),
                                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
                            )
                            // Chin guard stripe
                            drawRoundRect(
                                color = gradientColors.first(),
                                topLeft = Offset(w * 0.26f, h * 0.64f),
                                size = Size(w * 0.48f, h * 0.10f),
                                cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
                            )
                        }
                    }
                    AvatarPreset.RIDER_CAPTAIN -> {
                        Icon(
                            imageVector = Icons.Rounded.MilitaryTech,
                            contentDescription = "Avatar $displayName",
                            tint = Color.White,
                            modifier = Modifier.size(size * 0.56f)
                        )
                    }
                    AvatarPreset.PILOT_CLASSIC -> {
                        Icon(
                            imageVector = Icons.Rounded.SportsMotorsports,
                            contentDescription = "Avatar $displayName",
                            tint = Color.White,
                            modifier = Modifier.size(size * 0.56f)
                        )
                    }
                    AvatarPreset.STANDARD_USER -> {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Avatar $displayName",
                            tint = Color.White,
                            modifier = Modifier.size(size * 0.58f)
                        )
                    }
                }
            }
        }
    }
}
