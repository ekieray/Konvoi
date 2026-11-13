package com.konvoiku.touring.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.konvoiku.touring.model.ConvoyMember
import com.konvoiku.touring.model.ConvoyRouteConfig
import com.konvoiku.touring.model.RoutePoint
import com.konvoiku.touring.model.TourRoom
import com.konvoiku.touring.model.UserRole
import com.konvoiku.touring.model.VehicleType
import com.konvoiku.touring.ui.components.ConvoyAvatar
import com.konvoiku.touring.viewmodel.ConvoyViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourLeaderScreen(
    activeRoom: TourRoom?,
    routeConfig: ConvoyRouteConfig,
    destinationPresets: List<ConvoyViewModel.DestinationPreset>,
    onSelectPreset: (ConvoyViewModel.DestinationPreset) -> Unit,
    onPickDestinationFromMap: (xFraction: Float, yFraction: Float, placeName: String, addressLabel: String) -> Unit,
    onUpdateWaypoints: (List<RoutePoint>, String?) -> Unit,
    onResetDefaultWaypoints: () -> Unit,
    onAddCheckpointAtPoint: (String, Float, Float) -> Unit,
    onRemoveCheckpoint: (String) -> Unit,
    onUpdateMaxStragglerDistance: (Int) -> Unit,
    onToggleSoundAlert: () -> Unit,
    onCreateRoom: (String) -> Unit,
    onSimulateJoin: () -> Unit,
    onRemoveMember: (String) -> Unit,
    onEnterMapPlaceholder: () -> Unit,
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(0) }
    var showSetupStepper by remember(activeRoom) { mutableStateOf(activeRoom == null) }
    var showDestinationMapPicker by remember { mutableStateOf(false) }
    var tourNameDraft by remember { mutableStateOf(activeRoom?.roomTitle ?: "Konvoi Puncak Pass Weekend") }
    var checkpointDraft by remember { mutableStateOf("") }

    val stepTitles = listOf("Tujuan", "Jalur", "Istirahat", "Jarak")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (showSetupStepper) "Buat Konvoi Baru" else "Ruang Tour Leader",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (showSetupStepper) "Langkah ${currentStep + 1} dari 4 · ${stepTitles[currentStep]}" else "Room ID & Peserta Konvoi",
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
                actions = {
                    if (activeRoom != null) {
                        TextButton(onClick = { showSetupStepper = !showSetupStepper }) {
                            Text(if (showSetupStepper) "Lihat Room" else "Edit Rute")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (!showSetupStepper && activeRoom != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp
                ) {
                    Button(
                        onClick = onEnterMapPlaceholder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buka Peta Konvoi Live",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showSetupStepper) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    // Stepper Header Bar
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            stepTitles.forEachIndexed { idx, label ->
                                val isActive = idx == currentStep
                                val isDone = idx < currentStep
                                Surface(
                                    onClick = { currentStep = idx },
                                    shape = RoundedCornerShape(12.dp),
                                    color = when {
                                        isActive -> MaterialTheme.colorScheme.primary
                                        isDone -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (isDone) "✓" else "${idx + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isActive -> Color.White
                                                isDone -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = when {
                                                isActive -> Color.White
                                                isDone -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            when (currentStep) {
                                // Step 1: Menentukan Tujuan Akhir
                                0 -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Rounded.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text("1. Menentukan Tujuan Akhir", style = MaterialTheme.typography.titleLarge)
                                    }

                                    OutlinedTextField(
                                        value = tourNameDraft,
                                        onValueChange = { tourNameDraft = it },
                                        label = { Text("Nama Perjalanan / Tour") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Text(
                                        text = "Pilih Destinasi Utama",
                                        style = MaterialTheme.typography.labelLarge
                                    )

                                    destinationPresets.chunked(2).forEach { pair ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            pair.forEach { preset ->
                                                FilterChip(
                                                    selected = routeConfig.destinationName == preset.name,
                                                    onClick = { onSelectPreset(preset) },
                                                    label = {
                                                        Text(
                                                            text = preset.name,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { showDestinationMapPicker = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AddLocationAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Pilih dari Peta (Pick on Map)",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    val destPoint = routeConfig.waypoints.lastOrNull()
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.LocationOn,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = routeConfig.destinationName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                                Text(
                                                    text = routeConfig.destinationAddress,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (destPoint != null) {
                                                    Text(
                                                        text = String.format(
                                                            Locale.US,
                                                            "Koordinat: %.4f, %.4f · Estimasi %d km",
                                                            destPoint.latitude,
                                                            destPoint.longitude,
                                                            routeConfig.totalRouteMeters / 1000
                                                        ),
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Step 2: Menggambar / Pilih Jalur
                                1 -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(Icons.Rounded.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                            Text("2. Gambar / Pilih Jalur", style = MaterialTheme.typography.titleLarge)
                                        }
                                        TextButton(onClick = onResetDefaultWaypoints) {
                                            Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reset")
                                        }
                                    }

                                    Text(
                                        text = "Ketuk kanvas peta di bawah untuk menambah titik belok jalur baru menuju ${routeConfig.destinationName}.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .background(Color(0xFFEEF2F6), RoundedCornerShape(16.dp))
                                            .pointerInput(routeConfig.waypoints) {
                                                detectTapGestures { offset ->
                                                    val nx = (offset.x / size.width.toFloat()).coerceIn(0.10f, 0.90f)
                                                    val ny = (offset.y / size.height.toFloat()).coerceIn(0.10f, 0.90f)
                                                    val newPt = RoutePoint(
                                                        xFraction = nx,
                                                        yFraction = ny,
                                                        latitude = -6.7100 + (1f - ny) * 0.08,
                                                        longitude = 106.8500 + nx * 0.16
                                                    )
                                                    val current = routeConfig.waypoints
                                                    val updated = if (current.size >= 2) {
                                                        current.dropLast(1) + newPt + current.last()
                                                    } else {
                                                        current + newPt
                                                    }
                                                    onUpdateWaypoints(updated, null)
                                                }
                                            }
                                    ) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            val pts = routeConfig.waypoints
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
                                            pts.forEachIndexed { index, pt ->
                                                val center = Offset(pt.xFraction * size.width, pt.yFraction * size.height)
                                                val dotColor = when (index) {
                                                    0 -> Color(0xFFEA580C)
                                                    pts.lastIndex -> Color(0xFF16A34A)
                                                    else -> Color(0xFF1D4ED8)
                                                }
                                                drawCircle(Color.White, radius = 8.dp.toPx(), center = center)
                                                drawCircle(dotColor, radius = 5.5.dp.toPx(), center = center)
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${routeConfig.waypoints.size} titik jalur aktif · Titik Oranye = Start, Hijau = Tujuan",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Step 3: Menambahkan Titik Istirahat (Checkpoint)
                                2 -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Rounded.LocalCafe, contentDescription = null, tint = Color(0xFF0F766E))
                                        Text("3. Titik Istirahat (Checkpoint)", style = MaterialTheme.typography.titleLarge)
                                    }

                                    OutlinedTextField(
                                        value = checkpointDraft,
                                        onValueChange = { checkpointDraft = it },
                                        label = { Text("Nama Checkpoint / Rest Area") },
                                        placeholder = { Text("Misal: Rest Area KM 57") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("SPBU Cisarua", "RM Sate Pak Budi").forEach { suggestion ->
                                            FilterChip(
                                                selected = checkpointDraft == suggestion,
                                                onClick = { checkpointDraft = suggestion },
                                                label = { Text(suggestion, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            val name = checkpointDraft.ifBlank { "Rest Area KM 57" }
                                            val factor = (0.35f + routeConfig.checkpoints.size * 0.14f).coerceIn(0.25f, 0.78f)
                                            onAddCheckpointAtPoint(name, factor, 1f - factor)
                                            checkpointDraft = ""
                                        },
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Tambah Titik Istirahat ke Rute")
                                    }

                                    routeConfig.checkpoints.forEach { cp ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.background,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Icon(Icons.Rounded.LocalCafe, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(18.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(cp.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                                    Text(
                                                        text = String.format(Locale.US, "%.4f, %.4f", cp.latitude, cp.longitude),
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                IconButton(onClick = { onRemoveCheckpoint(cp.id) }) {
                                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus Checkpoint")
                                                }
                                            }
                                        }
                                    }
                                }

                                // Step 4: Mengatur Batas Jarak Tertinggal (Interactive Slider)
                                3 -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Rounded.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text("4. Batas Jarak Tertinggal", style = MaterialTheme.typography.titleLarge)
                                    }

                                    val kmValue = (routeConfig.maxStragglerDistanceMeters / 1000f).coerceIn(0.5f, 5.0f)

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Jarak Maksimal dari Sweeper",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = String.format(Locale.US, "%.1f km", kmValue),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "Peringatan aktif otomatis jika jarak anggota paling belakang melebihi batas ini.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Slider(
                                        value = kmValue,
                                        onValueChange = { nextKm ->
                                            val snappedMeters = ((nextKm * 2f).roundToInt() * 500).coerceIn(500, 5000)
                                            onUpdateMaxStragglerDistance(snappedMeters)
                                        },
                                        valueRange = 0.5f..5.0f,
                                        steps = 8,
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.secondary,
                                            activeTrackColor = MaterialTheme.colorScheme.primary
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("0.5 km", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("2.5 km", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("5.0 km", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Peringatan Suara Otomatis", fontWeight = FontWeight.SemiBold)
                                            Text("Aktifkan nada peringatan bila anggota tertinggal atau keluar jalur.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(checked = routeConfig.soundAlertEnabled, onCheckedChange = { onToggleSoundAlert() })
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.background,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Flag,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Tujuan Akhir: ${routeConfig.destinationName}",
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = routeConfig.destinationAddress,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (currentStep > 0) {
                                    OutlinedButton(
                                        onClick = { currentStep-- },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Sebelumnya")
                                    }
                                }
                                if (currentStep < 3) {
                                    Button(
                                        onClick = { currentStep++ },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text("Langkah Berikutnya")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            onCreateRoom(tourNameDraft.ifBlank { "Konvoi Touring Akhir Pekan" })
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Buat Room & Mulai", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (activeRoom != null) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = activeRoom.roomTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = activeRoom.roomId,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(activeRoom.roomId))
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Room ID ${activeRoom.roomId} disalin")
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Salin ID")
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activeRoom.inviteLink,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(activeRoom.inviteLink))
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Link undangan disalin")
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Salin Link", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onCreateRoom(activeRoom.roomTitle) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Buat ID Baru")
                                }
                                Button(
                                    onClick = onSimulateJoin,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simulasi Anggota")
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Daftar Sementara Anggota (${activeRoom.members.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(activeRoom.members, key = { it.id }) { member ->
                    MemberListCard(
                        member = member,
                        onRemove = if (member.role != UserRole.TOUR_LEADER) {
                            { onRemoveMember(member.id) }
                        } else null
                    )
                }

                item { Spacer(modifier = Modifier.height(84.dp)) }
            }
        }
    }

    if (showDestinationMapPicker) {
        val currentEnd = routeConfig.waypoints.lastOrNull()
            ?: RoutePoint(0.86f, 0.14f, -6.6450, 106.9900)
        DestinationMapPickerDialog(
            initialPoint = currentEnd,
            initialName = routeConfig.destinationName,
            onDismiss = { showDestinationMapPicker = false },
            onConfirm = { x, y, placeName, addressLabel ->
                onPickDestinationFromMap(x, y, placeName, addressLabel)
                showDestinationMapPicker = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Lokasi tujuan disimpan: $placeName")
                }
            }
        )
    }
}

@Composable
private fun MemberListCard(
    member: ConvoyMember,
    onRemove: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(44.dp)) {
                ConvoyAvatar(
                    preset = member.avatarPreset,
                    photoUri = member.avatarPhotoUri,
                    displayName = member.fullName,
                    size = 42.dp,
                    borderColor = if (member.role == UserRole.TOUR_LEADER) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
                Surface(
                    color = if (member.role == UserRole.TOUR_LEADER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (member.vehicleType == VehicleType.MOBIL) Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
                            contentDescription = member.vehicleType.label,
                            tint = if (member.role == UserRole.TOUR_LEADER) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(member.fullName, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${member.role.label} · ${member.vehicleType.label} · ${member.licensePlate}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Keluarkan Anggota")
                }
            }
        }
    }
}

private data class MapLandmark(
    val name: String,
    val address: String,
    val xFraction: Float,
    val yFraction: Float
)

private val REGIONAL_LANDMARKS = listOf(
    MapLandmark("Puncak Pass Resort", "Jl. Raya Puncak KM 90, Cisarua", 0.86f, 0.14f),
    MapLandmark("Taman Safari Cisarua", "Jl. Kapten Harun Kabir No. 724", 0.58f, 0.30f),
    MapLandmark("Rest Area KM 57", "Tol Jakarta-Cikampek KM 57", 0.82f, 0.46f),
    MapLandmark("Lembang Highland", "Jl. Raya Lembang No. 12, Bandung", 0.26f, 0.20f),
    MapLandmark("Simpang Gadog", "Jl. Raya Ciawi - Gadog KM 45", 0.34f, 0.66f),
    MapLandmark("Sentul Selatan Exit", "Jl. MH Thamrin, Babakan Madang", 0.72f, 0.68f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DestinationMapPickerDialog(
    initialPoint: RoutePoint,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (xFraction: Float, yFraction: Float, placeName: String, addressLabel: String) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1.25f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var pinX by remember { mutableFloatStateOf(initialPoint.xFraction.coerceIn(0.08f, 0.92f)) }
    var pinY by remember { mutableFloatStateOf(initialPoint.yFraction.coerceIn(0.08f, 0.90f)) }
    var placeName by remember { mutableStateOf(initialName) }
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }

    val lat = -6.7100 + (1f - pinY) * 0.08
    val lng = 106.8500 + pinX * 0.16
    val nearestLandmark = remember(pinX, pinY) {
        REGIONAL_LANDMARKS.minByOrNull { lm ->
            val dx = lm.xFraction - pinX
            val dy = lm.yFraction - pinY
            dx * dx + dy * dy
        }
    }
    val resolvedAddress = remember(pinX, pinY, nearestLandmark) {
        val base = nearestLandmark?.address ?: "Jalur Touring Jawa Barat"
        String.format(Locale.US, "%s · %.4f, %.4f", base, lat, lng)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("Pilih Tujuan Akhir di Peta", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "Geser, perbesar, lalu ketuk peta untuk menaruh pin",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Rounded.Close, contentDescription = "Tutup Peta")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFE9EFF5))
                            .clipToBounds()
                            .onSizeChanged { canvasSize = it }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(1f, 3f)
                                    val maxOffsetX = (size.width * (newScale - 1f)) / 2f
                                    val maxOffsetY = (size.height * (newScale - 1f)) / 2f
                                    scale = newScale
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                }
                            }
                            .pointerInput(scale, offset) {
                                detectTapGestures { tapOffset ->
                                    val w = size.width.toFloat().coerceAtLeast(1f)
                                    val h = size.height.toFloat().coerceAtLeast(1f)
                                    val cx = w / 2f
                                    val cy = h / 2f
                                    val mapX = ((tapOffset.x - cx - offset.x) / scale + cx) / w
                                    val mapY = ((tapOffset.y - cy - offset.y) / scale + cy) / h
                                    pinX = mapX.coerceIn(0.08f, 0.92f)
                                    pinY = mapY.coerceIn(0.08f, 0.90f)
                                    val closest = REGIONAL_LANDMARKS.minByOrNull { lm ->
                                        val dx = lm.xFraction - pinX
                                        val dy = lm.yFraction - pinY
                                        dx * dx + dy * dy
                                    }
                                    if (closest != null) {
                                        val distSq = (closest.xFraction - pinX) * (closest.xFraction - pinX) +
                                            (closest.yFraction - pinY) * (closest.yFraction - pinY)
                                        placeName = if (distSq < 0.025f) {
                                            closest.name
                                        } else {
                                            "Area ${closest.name}"
                                        }
                                    }
                                }
                            }
                    ) {
                        // Transformed Interactive Map Layer
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = offset.x
                                    translationY = offset.y
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val step = 42.dp.toPx()
                                var gx = 0f
                                while (gx < size.width) {
                                    drawLine(Color(0xFFD5DEE8), Offset(gx, 0f), Offset(gx, size.height), 1.dp.toPx())
                                    gx += step
                                }
                                var gy = 0f
                                while (gy < size.height) {
                                    drawLine(Color(0xFFD5DEE8), Offset(0f, gy), Offset(size.width, gy), 1.dp.toPx())
                                    gy += step
                                }
                                // Simulated highway corridors
                                val highwayPath = Path().apply {
                                    moveTo(size.width * 0.12f, size.height * 0.88f)
                                    lineTo(size.width * 0.34f, size.height * 0.66f)
                                    lineTo(size.width * 0.58f, size.height * 0.30f)
                                    lineTo(size.width * 0.86f, size.height * 0.14f)
                                }
                                drawPath(highwayPath, Color.White, style = Stroke(14.dp.toPx(), cap = StrokeCap.Round))
                                drawPath(highwayPath, Color(0xFF93C5FD), style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))

                                val branchPath = Path().apply {
                                    moveTo(size.width * 0.34f, size.height * 0.66f)
                                    lineTo(size.width * 0.72f, size.height * 0.68f)
                                    lineTo(size.width * 0.82f, size.height * 0.46f)
                                }
                                drawPath(branchPath, Color(0xFFCBD5E1), style = Stroke(5.dp.toPx(), cap = StrokeCap.Round))
                            }

                            // Landmark clickable nodes
                            val density = LocalDensity.current
                            REGIONAL_LANDMARKS.forEach { lm ->
                                val px = (lm.xFraction * canvasSize.width).roundToInt()
                                val py = (lm.yFraction * canvasSize.height).roundToInt()
                                Surface(
                                    onClick = {
                                        pinX = lm.xFraction
                                        pinY = lm.yFraction
                                        placeName = lm.name
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.92f),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.offset {
                                        IntOffset(
                                            x = px - with(density) { 46.dp.roundToPx() },
                                            y = py + with(density) { 8.dp.roundToPx() }
                                        )
                                    }
                                ) {
                                    Text(
                                        text = lm.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Dropped Destination Pin Marker
                            val pinPx = (pinX * canvasSize.width).roundToInt()
                            val pinPy = (pinY * canvasSize.height).roundToInt()
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.offset {
                                    IntOffset(
                                        x = pinPx - with(density) { 18.dp.roundToPx() },
                                        y = pinPy - with(density) { 36.dp.roundToPx() }
                                    )
                                }
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFDC2626),
                                    border = BorderStroke(2.5.dp, Color.White),
                                    shadowElevation = 6.dp,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Flag,
                                            contentDescription = "Pin Tujuan",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Zoom Controls
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SmallFloatingActionButton(
                                onClick = { scale = (scale + 0.35f).coerceAtMost(3f) },
                                containerColor = MaterialTheme.colorScheme.surface,
                                elevation = FloatingActionButtonDefaults.elevation(4.dp)
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Perbesar Peta")
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    val next = (scale - 0.35f).coerceAtLeast(1f)
                                    scale = next
                                    if (next == 1f) offset = Offset.Zero
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                elevation = FloatingActionButtonDefaults.elevation(4.dp)
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Perkecil Peta")
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    scale = 1f
                                    offset = Offset.Zero
                                },
                                containerColor = MaterialTheme.colorScheme.surface,
                                elevation = FloatingActionButtonDefaults.elevation(4.dp)
                            ) {
                                Icon(Icons.Rounded.MyLocation, contentDescription = "Reset Tampilan Peta")
                            }
                        }

                        // Zoom Badge
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xCC0F172A),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "Zoom %.1fx · Ketuk untuk taruh pin", scale),
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Bottom Confirmation Sheet
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 10.dp,
                        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = placeName,
                                onValueChange = { placeName = it },
                                label = { Text("Nama Lokasi Tujuan") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = resolvedAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Batal")
                                }
                                Button(
                                    onClick = {
                                        onConfirm(
                                            pinX,
                                            pinY,
                                            placeName.trim().ifBlank { "Titik Tujuan Peta" },
                                            resolvedAddress
                                        )
                                    },
                                    modifier = Modifier.weight(1.6f).height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Gunakan Lokasi Ini", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

