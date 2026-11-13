package com.konvoiku.touring.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konvoiku.touring.model.TripCardTheme
import com.konvoiku.touring.model.TripHistoryRecord
import com.konvoiku.touring.model.UserRole
import com.konvoiku.touring.model.VehicleType
import com.konvoiku.touring.model.buildTripGpxXml
import com.konvoiku.touring.model.buildTripJsonLog
import com.konvoiku.touring.model.buildTripShareSummaryText
import com.konvoiku.touring.ui.components.ConvoyAvatar
import com.konvoiku.touring.ui.components.StravaStyleTripShareCard
import com.konvoiku.touring.ui.components.renderTripCardBitmapToFile
import com.konvoiku.touring.ui.components.shareTripCardImage
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    trip: TripHistoryRecord?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showGpxPreview by remember { mutableStateOf(false) }
    var cardTheme by remember { mutableStateOf(TripCardTheme.DARK_CARBON) }
    var customPhotoUri by remember { mutableStateOf<String?>(null) }
    var isRenderingCard by remember { mutableStateOf(false) }

    val gpxXml = remember(trip) { trip?.let { buildTripGpxXml(it) }.orEmpty() }
    val jsonLog = remember(trip) { trip?.let { buildTripJsonLog(it) }.orEmpty() }
    val shareSummary = remember(trip) { trip?.let { buildTripShareSummaryText(it) }.orEmpty() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            customPhotoUri = uri.toString()
            cardTheme = TripCardTheme.CUSTOM_PHOTO
        }
    }

    val pngSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        val activeTrip = trip ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            coroutineScope.launch {
                isRenderingCard = true
                runCatching {
                    val file = renderTripCardBitmapToFile(context, activeTrip, cardTheme, customPhotoUri)
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        file.inputStream().use { input -> input.copyTo(out) }
                    }
                }.onSuccess {
                    snackbarHostState.showSnackbar("Kartu PNG berhasil disimpan ke perangkat.")
                }
                isRenderingCard = false
            }
        }
    }

    val gpxExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/gpx+xml")
    ) { uri ->
        if (uri != null && gpxXml.isNotEmpty()) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(gpxXml.toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("File GPX berhasil disimpan & siap dibuka di Google Earth / Garmin.")
                }
            }
        }
    }

    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && jsonLog.isNotEmpty()) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(jsonLog.toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Log perjalanan JSON berhasil diekspor.")
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = trip?.tourTitle ?: "Rekapitulasi Tour",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (trip != null) "${trip.roomId} · ${trip.dateLabel}" else "Detail Perjalanan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (trip == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Data perjalanan tidak ditemukan.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 0. Strava-Style Trip Summary Card Generator & Social Share
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Kartu Rute Perjalanan",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Kustomisasi gaya kartu lalu bagikan gambar PNG ke Instagram Story, WhatsApp, atau TikTok.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StravaStyleTripShareCard(
                            trip = trip,
                            theme = cardTheme,
                            customPhotoUri = customPhotoUri
                        )

                        // Theme Customization Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TripCardTheme.entries.forEach { themeOption ->
                                val selected = cardTheme == themeOption
                                FilterChip(
                                    selected = selected,
                                    onClick = { cardTheme = themeOption },
                                    label = { Text(themeOption.label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        // Custom Photo Picker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (customPhotoUri != null) "Ganti Foto Galeri" else "Pilih Foto Galeri",
                                    fontSize = 12.sp
                                )
                            }

                            if (customPhotoUri != null) {
                                OutlinedButton(
                                    onClick = {
                                        customPhotoUri = null
                                        if (cardTheme == TripCardTheme.CUSTOM_PHOTO) {
                                            cardTheme = TripCardTheme.DARK_CARBON
                                        }
                                    },
                                    modifier = Modifier.height(44.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = "Hapus Foto",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Primary CTA: Render PNG & Share Sheet
                        Button(
                            enabled = !isRenderingCard,
                            onClick = {
                                coroutineScope.launch {
                                    isRenderingCard = true
                                    runCatching {
                                        val pngFile = renderTripCardBitmapToFile(
                                            context = context,
                                            trip = trip,
                                            theme = cardTheme,
                                            customPhotoUri = customPhotoUri
                                        )
                                        shareTripCardImage(context, pngFile, trip)
                                    }.onFailure {
                                        snackbarHostState.showSnackbar("Gagal merender kartu gambar.")
                                    }
                                    isRenderingCard = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRenderingCard) "Merender Kartu PNG..." else "Bagikan Kartu Tour (Share Image)",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            enabled = !isRenderingCard,
                            onClick = {
                                pngSaveLauncher.launch("konvoiku-card-${trip.roomId.lowercase()}.png")
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Kartu ke Galeri (.PNG)", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 1. Mini Polyline Track Map Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Peta Rekam Jejak Rute", style = MaterialTheme.typography.titleMedium)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${trip.waypoints.size} Titik GPS",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                                .background(Color(0xFFEEF2F6), RoundedCornerShape(16.dp))
                        ) {
                            val widthPx = constraints.maxWidth.toFloat()
                            val heightPx = constraints.maxHeight.toFloat()
                            val density = LocalDensity.current

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val step = 36.dp.toPx()
                                var gx = 0f
                                while (gx < size.width) {
                                    drawLine(Color(0xFFDCE3EC), Offset(gx, 0f), Offset(gx, size.height), 1.dp.toPx())
                                    gx += step
                                }
                                var gy = 0f
                                while (gy < size.height) {
                                    drawLine(Color(0xFFDCE3EC), Offset(0f, gy), Offset(size.width, gy), 1.dp.toPx())
                                    gy += step
                                }

                                val pts = trip.waypoints
                                if (pts.size >= 2) {
                                    val path = Path().apply {
                                        moveTo(pts.first().xFraction * size.width, pts.first().yFraction * size.height)
                                        for (i in 1 until pts.size) {
                                            lineTo(pts[i].xFraction * size.width, pts[i].yFraction * size.height)
                                        }
                                    }
                                    drawPath(path, Color.White, style = Stroke(12.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                    drawPath(path, Color(0xFF1D4ED8), style = Stroke(5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                                }

                                pts.firstOrNull()?.let { start ->
                                    val c = Offset(start.xFraction * size.width, start.yFraction * size.height)
                                    drawCircle(Color.White, radius = 9.dp.toPx(), center = c)
                                    drawCircle(Color(0xFFEA580C), radius = 6.dp.toPx(), center = c)
                                }

                                pts.lastOrNull()?.let { end ->
                                    val c = Offset(end.xFraction * size.width, end.yFraction * size.height)
                                    drawCircle(Color.White, radius = 9.dp.toPx(), center = c)
                                    drawCircle(Color(0xFF16A34A), radius = 6.dp.toPx(), center = c)
                                }
                            }

                            trip.visitedCheckpoints.forEach { cp ->
                                val px = (cp.xFraction * widthPx).roundToInt()
                                val py = (cp.yFraction * heightPx).roundToInt()
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0F766E),
                                    border = BorderStroke(1.5.dp, Color.White),
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                x = px - with(density) { 11.dp.roundToPx() },
                                                y = py - with(density) { 11.dp.roundToPx() }
                                            )
                                        }
                                        .size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.LocalCafe,
                                            contentDescription = cp.name,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Tujuan: ${trip.destinationName} · ${trip.destinationAddress}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Speed, Distance & Duration Stats Grid
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Statistik Konvoi", style = MaterialTheme.typography.titleMedium)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SummaryStatBox(
                                icon = Icons.Rounded.Route,
                                label = "Jarak Tempuh",
                                value = String.format(Locale.US, "%.1f km", trip.distanceKm),
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatBox(
                                icon = Icons.Rounded.Schedule,
                                label = "Durasi Total",
                                value = trip.durationLabel,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SummaryStatBox(
                                icon = Icons.Rounded.Speed,
                                label = "Kecepatan Rata-rata",
                                value = "${trip.avgSpeedKmh} km/j",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryStatBox(
                                icon = Icons.Rounded.Flag,
                                label = "Kecepatan Puncak",
                                value = "${trip.maxSpeedKmh} km/j",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Export GPX / JSON & Share Tour Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Ekspor Data & Bagikan", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Unduh jejak rute GPX/JSON untuk Google Earth, Garmin, atau bagikan rekap konvoi ke grup WhatsApp.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                gpxExportLauncher.launch("konvoiku-${trip.roomId.lowercase()}.gpx")
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ekspor Rute (.GPX)", fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    jsonExportLauncher.launch("konvoiku-${trip.roomId.lowercase()}.json")
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Rounded.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ekspor JSON", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(shareSummary))
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Rekap Tour ${trip.tourTitle}")
                                        putExtra(Intent.EXTRA_TEXT, shareSummary)
                                    }
                                    runCatching {
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Bagikan Ringkasan Tour")
                                        )
                                    }.onFailure {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Ringkasan tour disalin ke clipboard.")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bagikan Tour", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        TextButton(
                            onClick = { showGpxPreview = !showGpxPreview },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (showGpxPreview) "Sembunyikan Pratinjau GPX" else "Lihat Pratinjau GPX 1.1")
                        }

                        AnimatedVisibility(visible = showGpxPreview) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.background,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = gpxXml,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Visited Checkpoints
            item {
                Text(
                    text = "Titik Istirahat Dikunjungi (${trip.visitedCheckpoints.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (trip.visitedCheckpoints.isEmpty()) {
                item {
                    Text(
                        text = "Tidak ada titik istirahat tercatat pada perjalanan ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(trip.visitedCheckpoints, key = { it.id }) { cp ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFCCFBF1),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalCafe,
                                        contentDescription = null,
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cp.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    text = String.format(Locale.US, "%.4f, %.4f", cp.latitude, cp.longitude),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = cp.arrivedAtLabel,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F766E)
                            )
                        }
                    }
                }
            }

            // 5. Convoy Participants
            item {
                Text(
                    text = "Peserta Konvoi (${trip.participants.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(trip.participants, key = { it.id }) { participant ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ConvoyAvatar(
                            preset = participant.avatarPreset,
                            photoUri = participant.avatarPhotoUri,
                            displayName = participant.fullName,
                            size = 38.dp,
                            borderColor = if (participant.role == UserRole.TOUR_LEADER) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(participant.fullName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(
                                text = "${if (participant.isSweeper) "Sweeper" else participant.role.label} · ${participant.vehicleDetail} · ${participant.licensePlate}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (participant.vehicleType == VehicleType.MOBIL) Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
                            contentDescription = participant.vehicleType.label,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SummaryStatBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
