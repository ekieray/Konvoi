package com.konvoiku.touring.ui.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddLocationAlt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.EditLocationAlt
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sos
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konvoiku.touring.model.ChatMessageType
import com.konvoiku.touring.model.ConvoyMember
import com.konvoiku.touring.model.ConvoyRouteConfig
import com.konvoiku.touring.model.DestinationEtaInfo
import com.konvoiku.touring.model.MemberConditionStatus
import com.konvoiku.touring.model.OffRouteAlertState
import com.konvoiku.touring.model.RestCheckpoint
import com.konvoiku.touring.model.RoomMessage
import com.konvoiku.touring.model.SosEmergencyState
import com.konvoiku.touring.model.StragglerAlertState
import com.konvoiku.touring.model.TourRoom
import com.konvoiku.touring.model.UserRole
import com.konvoiku.touring.model.VehicleType
import com.konvoiku.touring.model.calculateDestinationEta
import com.konvoiku.touring.ui.components.ConvoyAvatar
import com.konvoiku.touring.viewmodel.ConvoyViewModel
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackingMapScreen(
    activeRoom: TourRoom?,
    role: UserRole,
    selectedMemberId: String?,
    isSimulationRunning: Boolean,
    routeConfig: ConvoyRouteConfig,
    stragglerAlert: StragglerAlertState,
    offRouteAlert: OffRouteAlertState,
    sosState: SosEmergencyState,
    messages: List<RoomMessage>,
    isRecordingVoice: Boolean,
    recordingSeconds: Int,
    playingMessageId: String?,
    isPickingDestinationOnMap: Boolean,
    isPickingCheckpointOnMap: Boolean,
    pendingCheckpointName: String,
    isOffRouteSimActive: Boolean,
    isStragglerSimActive: Boolean,
    destinationPresets: List<ConvoyViewModel.DestinationPreset>,
    onSelectMember: (String) -> Unit,
    onToggleSimulation: () -> Unit,
    onSelectPreset: (ConvoyViewModel.DestinationPreset) -> Unit,
    onPickCustomDestination: (Float, Float) -> Unit,
    onTogglePickMode: (Boolean) -> Unit,
    onStartPickCheckpoint: (String) -> Unit,
    onCancelPickCheckpoint: () -> Unit,
    onConfirmAddCheckpoint: (String, Float, Float) -> Unit,
    onRemoveCheckpoint: (String) -> Unit,
    onUpdateMaxStragglerDistance: (Int) -> Unit,
    onUpdateOffRouteTolerance: (Int) -> Unit,
    onToggleSoundAlert: () -> Unit,
    onToggleOffRouteSim: () -> Unit,
    onToggleStragglerSim: () -> Unit,
    onTriggerSos: () -> Unit,
    onResolveSos: () -> Unit,
    onSendTextMessage: (String) -> Unit,
    onStartVoiceRecord: () -> Unit,
    onStopVoiceRecord: () -> Unit,
    onToggleVoicePlay: (String) -> Unit,
    onUpdateOwnCondition: (MemberConditionStatus) -> Unit,
    onAssignSweeper: (String) -> Unit,
    onFinishAndSaveTrip: () -> Unit,
    onExitRoom: () -> Unit
) {
    var mapScale by remember { mutableFloatStateOf(1f) }
    var mapOffset by remember { mutableStateOf(Offset.Zero) }
    var isParticipantsExpanded by remember { mutableStateOf(false) }
    var showChatSheet by remember { mutableStateOf(false) }
    var showLeaderSettingsSheet by remember { mutableStateOf(false) }
    var showSosConfirmDialog by remember { mutableStateOf(false) }
    var showFinishTripDialog by remember { mutableStateOf(false) }
    var chatInputText by remember { mutableStateOf("") }

    var checkpointDraftName by remember { mutableStateOf("") }
    var tappedCheckpointCoords by remember { mutableStateOf<Pair<Float, Float>?>(null) }

    val members = activeRoom?.members.orEmpty().sortedBy { it.convoyOrderRank }
    val focusedMember = members.find { it.id == selectedMemberId } ?: members.firstOrNull()
    val currentUser = members.find { it.isCurrentUser }
    val trackedMember = focusedMember ?: currentUser
    val etaInfo = remember(
        trackedMember?.mapPosition?.routeProgress,
        trackedMember?.mapPosition?.speedKmh,
        routeConfig.totalRouteMeters,
        routeConfig.destinationName
    ) {
        calculateDestinationEta(trackedMember, routeConfig)
    }

    LaunchedEffect(stragglerAlert.isTriggered, offRouteAlert.isOffRoute, sosState.isActive, routeConfig.soundAlertEnabled) {
        if (routeConfig.soundAlertEnabled && (stragglerAlert.isTriggered || offRouteAlert.isOffRoute || sosState.isActive)) {
            runCatching {
                val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Peta Konvoi · ${activeRoom?.roomId ?: "KNV-0000"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Tujuan: ${routeConfig.destinationName} · ${routeConfig.checkpoints.size} Rest Area",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onExitRoom) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Keluar Room"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showFinishTripDialog = true }) {
                        Text("Selesai", fontWeight = FontWeight.SemiBold)
                    }
                    if (role == UserRole.TOUR_LEADER) {
                        IconButton(onClick = { showLeaderSettingsSheet = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Pengaturan Rute Leader",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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
            // Top Collapsible Participant Strip + Expanded Panel
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isParticipantsExpanded = !isParticipantsExpanded }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Peserta Konvoi (${members.size}) · Ketuk untuk detail",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isParticipantsExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                            contentDescription = "Buka atau Tutup Daftar Peserta",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!isParticipantsExpanded) {
                        LazyRow(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(members, key = { it.id }) { member ->
                                TopParticipantChip(
                                    member = member,
                                    isSelected = member.id == focusedMember?.id,
                                    onClick = {
                                        onSelectMember(member.id)
                                        mapOffset = Offset.Zero
                                    }
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isParticipantsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    MemberConditionStatus.AMAN to "Aman",
                                    MemberConditionStatus.ISTIRAHAT to "Istirahat",
                                    MemberConditionStatus.MOGOK to "Mogok"
                                ).forEach { (status, shortLabel) ->
                                    FilterChip(
                                        selected = currentUser?.conditionStatus == status,
                                        onClick = { onUpdateOwnCondition(status) },
                                        label = { Text(shortLabel, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.heightIn(max = 180.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(members, key = { it.id }) { member ->
                                    val memberEta = calculateDestinationEta(member, routeConfig)
                                    ParticipantExpandedRow(
                                        member = member,
                                        etaInfo = memberEta,
                                        viewerIsLeader = role == UserRole.TOUR_LEADER,
                                        isSelected = member.id == focusedMember?.id,
                                        onAssignSweeper = { onAssignSweeper(member.id) },
                                        onClick = {
                                            onSelectMember(member.id)
                                            mapOffset = Offset.Zero
                                            isParticipantsExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }

            // Safety Alert Banners
            if (sosState.isActive) {
                Surface(
                    color = Color(0xFF991B1B),
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.Sos, contentDescription = "Darurat SOS", modifier = Modifier.size(22.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DARURAT SOS · ${sosState.senderName} (${sosState.senderVehicle})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${sosState.locationText} (${String.format(Locale.US, "%.4f, %.4f", sosState.latitude, sosState.longitude)}) · ${sosState.triggeredAtLabel}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp)
                            )
                        }
                        TextButton(onClick = onResolveSos) {
                            Text("Selesai", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (offRouteAlert.isOffRoute) {
                Surface(
                    color = Color(0xFFDC2626),
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.Warning, contentDescription = "Keluar Jalur", modifier = Modifier.size(18.dp))
                        Text(
                            text = "KELUAR JALUR · Menyimpang ${offRouteAlert.deviationMeters} m dari rute resmi (batas ${offRouteAlert.toleranceMeters} m)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }

            if (stragglerAlert.isTriggered && stragglerAlert.trailingMember != null && stragglerAlert.aheadMember != null) {
                Surface(
                    color = Color(0xFFB45309),
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = "TERTINGGAL ${formatDistanceLabel(stragglerAlert.gapMeters)} · ${stragglerAlert.trailingMember.fullName} di belakang ${stragglerAlert.aheadMember.fullName}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        if (routeConfig.soundAlertEnabled) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Full Live Map Viewport
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFEEF2F6))
                    .pointerInput(isPickingDestinationOnMap, isPickingCheckpointOnMap, pendingCheckpointName) {
                        if (isPickingDestinationOnMap || isPickingCheckpointOnMap) {
                            detectTapGestures { tapOffset ->
                                val xFrac = (tapOffset.x - mapOffset.x) / size.width.toFloat()
                                val yFrac = (tapOffset.y - mapOffset.y) / size.height.toFloat()
                                if (isPickingDestinationOnMap) {
                                    onPickCustomDestination(xFrac, yFrac)
                                } else if (isPickingCheckpointOnMap) {
                                    if (pendingCheckpointName.isNotBlank()) {
                                        onConfirmAddCheckpoint(pendingCheckpointName, xFrac, yFrac)
                                        checkpointDraftName = ""
                                    } else {
                                        tappedCheckpointCoords = xFrac to yFrac
                                    }
                                }
                            }
                        } else {
                            detectTransformGestures { _, pan, zoom, _ ->
                                mapScale = (mapScale * zoom).coerceIn(0.85f, 2.2f)
                                mapOffset = Offset(
                                    x = (mapOffset.x + pan.x).coerceIn(-220f, 220f),
                                    y = (mapOffset.y + pan.y).coerceIn(-220f, 220f)
                                )
                            }
                        }
                    }
            ) {
                val widthPx = constraints.maxWidth.toFloat()
                val heightPx = constraints.maxHeight.toFloat()
                val waypoints = routeConfig.waypoints

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 52.dp.toPx() * mapScale
                    val offsetX = mapOffset.x % step
                    val offsetY = mapOffset.y % step

                    for (i in -2..((size.width / step).toInt() + 2)) {
                        val x = i * step + offsetX
                        drawLine(Color(0xFFDCE3EC), Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                    }
                    for (j in -2..((size.height / step).toInt() + 2)) {
                        val y = j * step + offsetY
                        drawLine(Color(0xFFDCE3EC), Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                    }

                    if (waypoints.size >= 2) {
                        val routePath = Path().apply {
                            moveTo(waypoints.first().xFraction * size.width + mapOffset.x, waypoints.first().yFraction * size.height + mapOffset.y)
                            for (i in 1 until waypoints.size) {
                                lineTo(waypoints[i].xFraction * size.width + mapOffset.x, waypoints[i].yFraction * size.height + mapOffset.y)
                            }
                        }
                        drawPath(routePath, Color.White, style = Stroke(14.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(
                            routePath,
                            Color(0xFF1D4ED8),
                            style = Stroke(5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f))
                        )
                    }
                }

                // Rest Area / Checkpoint Markers
                routeConfig.checkpoints.forEach { checkpoint ->
                    val cpX = (checkpoint.xFraction * widthPx + mapOffset.x).roundToInt()
                    val cpY = (checkpoint.yFraction * heightPx + mapOffset.y).roundToInt()
                    RestCheckpointMapMarker(
                        checkpoint = checkpoint,
                        modifier = Modifier.offset { IntOffset(cpX - 84, cpY - 54) }
                    )
                }

                // Destination Flag Marker with Live ETA Badge
                val destPoint = waypoints.lastOrNull()
                if (destPoint != null) {
                    val destX = (destPoint.xFraction * widthPx + mapOffset.x).roundToInt()
                    val destY = (destPoint.yFraction * heightPx + mapOffset.y).roundToInt()
                    Column(
                        modifier = Modifier
                            .offset { IntOffset(destX - 92, destY - 70) }
                            .width(184.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF0F172A), shadowElevation = 4.dp) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Tujuan: ${routeConfig.destinationName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (etaInfo.isStopped) {
                                        "ETA --:-- · ${etaInfo.distanceLabel}"
                                    } else {
                                        "ETA ${etaInfo.arrivalClockLabel} · ${etaInfo.durationLabel} · ${etaInfo.distanceLabel}"
                                    },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color(0xFF86EFAC),
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF16A34A),
                            border = BorderStroke(2.dp, Color.White),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Convoy Member Markers
                members.forEach { member ->
                    val markerX = (member.mapPosition.xFraction * widthPx + mapOffset.x).roundToInt().coerceIn(48, (widthPx - 48).roundToInt().coerceAtLeast(48))
                    val markerY = (member.mapPosition.yFraction * heightPx + mapOffset.y).roundToInt().coerceIn(56, (heightPx - 36).roundToInt().coerceAtLeast(56))

                    ConvoyMapMarker(
                        member = member,
                        isSelected = member.id == focusedMember?.id,
                        isOffRoute = member.offRouteMeters > routeConfig.offRouteToleranceMeters,
                        isStraggler = stragglerAlert.isTriggered && stragglerAlert.trailingMember?.id == member.id,
                        isSosBeacon = member.conditionStatus == MemberConditionStatus.SOS || (sosState.isActive && sosState.senderId == member.id),
                        onClick = { onSelectMember(member.id) },
                        modifier = Modifier.offset { IntOffset(markerX - 96, markerY - 84) }
                    )
                }

                // Map Tap Picking Hint
                if (isPickingDestinationOnMap || isPickingCheckpointOnMap) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp, bottom = 92.dp, end = 12.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isPickingCheckpointOnMap) Color(0xFF0F766E) else Color(0xFF0F172A)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isPickingCheckpointOnMap) {
                                    if (pendingCheckpointName.isNotBlank()) "Ketuk peta untuk menaruh: $pendingCheckpointName"
                                    else "Ketuk di peta untuk lokasi Titik Istirahat"
                                } else {
                                    "Ketuk di peta untuk menetapkan Titik Tujuan baru"
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    if (isPickingCheckpointOnMap) onCancelPickCheckpoint()
                                    else onTogglePickMode(false)
                                }
                            ) {
                                Text("Batal", color = Color(0xFFFFEDD5), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Top-Left Quick Simulation Triggers
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = isOffRouteSimActive,
                        onClick = onToggleOffRouteSim,
                        label = { Text("Uji Keluar Jalur", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = isStragglerSimActive,
                        onClick = onToggleStragglerSim,
                        label = { Text("Uji Tertinggal", fontSize = 11.sp) }
                    )
                }

                // Top-Right Zoom & Center Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = onToggleSimulation,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(if (isSimulationRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null)
                    }
                    SmallFloatingActionButton(
                        onClick = { mapScale = (mapScale + 0.2f).coerceAtMost(2.2f) },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Perbesar")
                    }
                    SmallFloatingActionButton(
                        onClick = { mapScale = (mapScale - 0.2f).coerceAtLeast(0.85f) },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(Icons.Rounded.Remove, contentDescription = "Perkecil")
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            mapScale = 1f
                            mapOffset = Offset.Zero
                        },
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = "Pusatkan")
                    }
                }

                // Bottom-Right Floating Action Buttons: Chat (Blue), Voice PTT (Orange/Amber), SOS (Vivid Red)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = { showChatSheet = true },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = "Buka Chat")
                            Text("Chat (${messages.size})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            if (isRecordingVoice) {
                                onStopVoiceRecord()
                                showChatSheet = true
                            } else {
                                onStartVoiceRecord()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = if (isRecordingVoice) Color(0xFFDC2626) else MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isRecordingVoice) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = "Rekam Suara"
                        )
                    }

                    // Prominent Rounded Elevated Red SOS Button
                    Surface(
                        onClick = { showSosConfirmDialog = true },
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFDC2626),
                        contentColor = Color.White,
                        shadowElevation = 10.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Rounded.Sos, contentDescription = "Tombol Darurat SOS", modifier = Modifier.size(22.dp))
                            Text("SOS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Docked Destination ETA Telemetry Bar
            DestinationEtaHudBar(
                destinationName = routeConfig.destinationName,
                trackedMember = trackedMember,
                currentUserId = currentUser?.id,
                etaInfo = etaInfo,
                onFocusSelf = if (currentUser != null && trackedMember?.id != currentUser.id) {
                    { onSelectMember(currentUser.id) }
                } else null
            )
        }
    }

    // Modern Messenger Chat & Large Waveform PTT Bottom Sheet
    if (showChatSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showChatSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 420.dp, max = 560.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Komunikasi Konvoi", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${activeRoom?.roomId ?: "KNV-8492"} · ${members.size} Anggota Terhubung",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showChatSheet = false }) {
                        Text("Tutup")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val matchedMember = members.find { it.id == msg.senderId }
                        MessengerChatBubble(
                            message = msg,
                            avatarPreset = matchedMember?.avatarPreset ?: msg.senderAvatarPreset,
                            avatarPhotoUri = matchedMember?.avatarPhotoUri ?: msg.senderAvatarPhotoUri,
                            isPlaying = playingMessageId == msg.id,
                            onTogglePlay = { onToggleVoicePlay(msg.id) }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Large Interactive Audio Waveform Push-to-Talk Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InteractiveWaveformPttButton(
                        isRecording = isRecordingVoice,
                        recordingSeconds = recordingSeconds,
                        onToggleRecord = {
                            if (isRecordingVoice) onStopVoiceRecord() else onStartVoiceRecord()
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Tulis pesan ke rombongan...", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        )

                        FloatingActionButton(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    onSendTextMessage(chatInputText)
                                    chatInputText = ""
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Kirim Pesan")
                        }
                    }
                }
            }
        }
    }

    // Dialog naming a tapped Checkpoint if Leader tapped map without pre-entering name
    if (tappedCheckpointCoords != null) {
        val (xFrac, yFrac) = tappedCheckpointCoords!!
        var dialogName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {
                tappedCheckpointCoords = null
                onCancelPickCheckpoint()
            },
            icon = { Icon(Icons.Rounded.LocalCafe, contentDescription = null, tint = Color(0xFF0F766E)) },
            title = { Text("Nama Titik Istirahat", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dialogName,
                        onValueChange = { dialogName = it },
                        label = { Text("Nama Checkpoint / Rest Area") },
                        placeholder = { Text("Misal: Rest Area KM 57") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Rest Area KM 57", "SPBU Cisarua").forEach { suggestion ->
                            FilterChip(
                                selected = dialogName == suggestion,
                                onClick = { dialogName = suggestion },
                                label = { Text(suggestion, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onConfirmAddCheckpoint(dialogName.ifBlank { "Rest Area Konvoi" }, xFrac, yFrac)
                        tappedCheckpointCoords = null
                    }
                ) {
                    Text("Simpan & Broadcast")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tappedCheckpointCoords = null
                        onCancelPickCheckpoint()
                    }
                ) {
                    Text("Batal")
                }
            }
        )
    }

    // Finish & Save Trip to History Dialog
    if (showFinishTripDialog) {
        AlertDialog(
            onDismissRequest = { showFinishTripDialog = false },
            icon = { Icon(Icons.Rounded.Flag, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(30.dp)) },
            title = { Text("Akhiri & Simpan Perjalanan?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Rekam jejak rute menuju ${routeConfig.destinationName}, titik istirahat, dan daftar peserta akan disimpan ke Riwayat Tour untuk ekspor GPX.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishTripDialog = false
                        onFinishAndSaveTrip()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Simpan ke Riwayat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishTripDialog = false }) {
                    Text("Lanjut Tour")
                }
            }
        )
    }

    // SOS Confirmation Dialog
    if (showSosConfirmDialog) {
        val latStr = String.format(Locale.US, "%.4f", currentUser?.mapPosition?.latitude ?: -6.6740)
        val lngStr = String.format(Locale.US, "%.4f", currentUser?.mapPosition?.longitude ?: 106.9250)
        AlertDialog(
            onDismissRequest = { showSosConfirmDialog = false },
            icon = { Icon(Icons.Rounded.Sos, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(32.dp)) },
            title = { Text("Kirim Sinyal Darurat SOS?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Sinyal bahaya dan koordinat lokasi Anda ($latStr, $lngStr) akan dikirimkan ke seluruh anggota room konvoi.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSosConfirmDialog = false
                        onTriggerSos()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Kirim SOS Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Leader Route, Checkpoints & Straggler Settings Bottom Sheet
    if (showLeaderSettingsSheet && role == UserRole.TOUR_LEADER) {
        ModalBottomSheet(
            onDismissRequest = { showLeaderSettingsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Pengaturan Rute & Peringatan Leader", style = MaterialTheme.typography.titleLarge)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Titik Tujuan Akhir & Garis Rute Resmi", style = MaterialTheme.typography.labelLarge)
                    destinationPresets.chunked(2).forEach { rowPresets ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowPresets.forEach { preset ->
                                FilterChip(
                                    selected = routeConfig.destinationName == preset.name,
                                    onClick = { onSelectPreset(preset) },
                                    label = { Text(preset.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            onTogglePickMode(true)
                            showLeaderSettingsSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.EditLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ketuk Peta untuk Tujuan Kustom")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Titik Istirahat (Checkpoint / Rest Area)", style = MaterialTheme.typography.labelLarge)
                    routeConfig.checkpoints.forEach { cp ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.background,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.LocalCafe, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(18.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cp.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(
                                        text = String.format(Locale.US, "%.4f, %.4f", cp.latitude, cp.longitude),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onRemoveCheckpoint(cp.id) }) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus Checkpoint")
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = checkpointDraftName,
                        onValueChange = { checkpointDraftName = it },
                        label = { Text("Nama Titik Istirahat Baru") },
                        placeholder = { Text("Contoh: Rumah Makan Sate Pak Budi") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            onStartPickCheckpoint(checkpointDraftName)
                            showLeaderSettingsSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                    ) {
                        Icon(Icons.Rounded.AddLocationAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Titik Istirahat (Pilih di Peta)")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                // Interactive Straggler Distance Slider
                val kmValue = (routeConfig.maxStragglerDistanceMeters / 1000f).coerceIn(0.5f, 5.0f)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Batas Jarak Maksimal Tertinggal: ${String.format(Locale.US, "%.1f km", kmValue)}",
                        style = MaterialTheme.typography.labelLarge
                    )
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
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Peringatan Suara (Audio Alert)", style = MaterialTheme.typography.titleMedium)
                        Text("Bunyikan nada peringatan saat SOS, keluar jalur, atau tertinggal.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = routeConfig.soundAlertEnabled, onCheckedChange = { onToggleSoundAlert() })
                }

                Button(
                    onClick = { showLeaderSettingsSheet = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Simpan & Terapkan ke Room")
                }
            }
        }
    }
}

@Composable
private fun TopParticipantChip(
    member: ConvoyMember,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConvoyAvatar(
                preset = member.avatarPreset,
                photoUri = member.avatarPhotoUri,
                displayName = member.fullName,
                size = 30.dp,
                borderColor = if (member.isCurrentUser) MaterialTheme.colorScheme.secondary else Color.White
            )
            Column {
                Text(
                    text = member.fullName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = if (member.vehicleType == VehicleType.MOBIL) Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
                        contentDescription = member.vehicleType.label,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = member.vehicleType.label,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DestinationEtaHudBar(
    destinationName: String,
    trackedMember: ConvoyMember?,
    currentUserId: String?,
    etaInfo: DestinationEtaInfo,
    onFocusSelf: (() -> Unit)?
) {
    val isTrackingSelf = trackedMember == null || trackedMember.id == currentUserId
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Destination & Tracked Vehicle
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (etaInfo.isArrived) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = "Estimasi Tiba",
                                tint = if (etaInfo.isArrived) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = destinationName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isTrackingSelf) "Posisi Anda" else trackedMember?.fullName ?: "Konvoi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (onFocusSelf != null) {
                                Text(
                                    text = "• Kembali ke Anda",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable(onClick = onFocusSelf)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // ETA, Distance, and Current Speed Metrics
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ETA ${etaInfo.arrivalClockLabel}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                etaInfo.isArrived -> Color(0xFF16A34A)
                                etaInfo.isStopped -> Color(0xFFD97706)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Text(
                            text = etaInfo.durationLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (etaInfo.isStopped) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = etaInfo.distanceLabel,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "${etaInfo.speedKmh} km/j",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantExpandedRow(
    member: ConvoyMember,
    etaInfo: DestinationEtaInfo,
    viewerIsLeader: Boolean,
    isSelected: Boolean,
    onAssignSweeper: () -> Unit,
    onClick: () -> Unit
) {
    val conditionColor = when (member.conditionStatus) {
        MemberConditionStatus.AMAN -> Color(0xFF16A34A)
        MemberConditionStatus.ISTIRAHAT -> Color(0xFFD97706)
        MemberConditionStatus.MOGOK, MemberConditionStatus.SOS -> Color(0xFFDC2626)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ConvoyAvatar(
                preset = member.avatarPreset,
                photoUri = member.avatarPhotoUri,
                displayName = member.fullName,
                size = 38.dp,
                borderColor = if (member.isCurrentUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (member.isCurrentUser) "${member.markerTitle} (Anda)" else member.markerTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${if (member.isSweeper) "Sweeper" else member.role.label} · ${member.vehicleType.label} · ${member.lastLocationText}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (viewerIsLeader && member.role != UserRole.TOUR_LEADER) {
                    TextButton(
                        onClick = onAssignSweeper,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (member.isSweeper) "Sweeper Aktif" else "Jadikan Sweeper", fontSize = 11.sp)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        imageVector = when (member.conditionStatus) {
                            MemberConditionStatus.AMAN -> Icons.Rounded.CheckCircle
                            MemberConditionStatus.ISTIRAHAT -> Icons.Rounded.Coffee
                            MemberConditionStatus.MOGOK -> Icons.Rounded.Build
                            MemberConditionStatus.SOS -> Icons.Rounded.Sos
                        },
                        contentDescription = member.conditionStatus.label,
                        tint = conditionColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(member.conditionStatus.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = conditionColor)
                }
                Text(
                    text = "${member.mapPosition.speedKmh} km/j · ETA ${etaInfo.durationLabel}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sisa ${etaInfo.distanceLabel}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InteractiveWaveformPttButton(
    isRecording: Boolean,
    recordingSeconds: Int,
    onToggleRecord: () -> Unit
) {
    val heights = listOf(10, 20, 14, 28, 18, 32, 22, 16, 26, 14, 20, 12)
    Surface(
        onClick = onToggleRecord,
        shape = RoundedCornerShape(18.dp),
        color = if (isRecording) Color(0xFFDC2626) else MaterialTheme.colorScheme.secondary,
        contentColor = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                    contentDescription = "Rekam Suara",
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (isRecording) "Sedang Merekam · Ketuk Kirim" else "Tekan untuk Bicara (Walkie-Talkie)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isRecording) "0:${recordingSeconds.toString().padStart(2, '0')} · Audio konvoi" else "Pesan suara instan ke semua anggota",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                heights.forEachIndexed { idx, h ->
                    val barHeight = if (isRecording) ((h + (recordingSeconds * 5 + idx * 3) % 10).coerceAtMost(34)) else h / 2 + 4
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(barHeight.dp)
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun MessengerChatBubble(
    message: RoomMessage,
    avatarPreset: com.konvoiku.touring.model.AvatarPreset,
    avatarPhotoUri: String?,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit
) {
    when (message.type) {
        ChatMessageType.SYSTEM -> {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "${message.timestampLabel} · ${message.textContent}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
        ChatMessageType.SOS_ALERT -> {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, Color(0xFFDC2626)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ConvoyAvatar(
                        preset = avatarPreset,
                        photoUri = avatarPhotoUri,
                        displayName = message.senderName,
                        size = 34.dp,
                        borderColor = Color(0xFFDC2626)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DARURAT SOS · ${message.senderName} (${message.timestampLabel})",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = message.textContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }
        }
        else -> {
            val isMine = message.isFromCurrentUser
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                if (!isMine) {
                    ConvoyAvatar(
                        preset = avatarPreset,
                        photoUri = avatarPhotoUri,
                        displayName = message.senderName,
                        size = 30.dp,
                        borderColor = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Surface(
                    shape = if (isMine) {
                        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
                    } else {
                        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
                    },
                    color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = if (isMine) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.widthIn(max = 250.dp)
                ) {
                    if (message.type == ChatMessageType.VOICE) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = onTogglePlay,
                                shape = CircleShape,
                                color = if (isMine) Color.White.copy(alpha = 0.22f) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                        contentDescription = "Putar Suara",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "${message.senderName} · ${message.senderVehicle}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isMine) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Voice 0:${message.voiceDurationSec.toString().padStart(2, '0')} · ${message.timestampLabel}",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            if (!isMine) {
                                Text(
                                    text = "${message.senderName} · ${message.senderVehicle}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text(
                                text = message.textContent,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = message.timestampLabel,
                                fontSize = 10.sp,
                                color = if (isMine) Color.White.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isMine) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ConvoyAvatar(
                        preset = avatarPreset,
                        photoUri = avatarPhotoUri,
                        displayName = message.senderName,
                        size = 30.dp,
                        borderColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RestCheckpointMapMarker(
    checkpoint: RestCheckpoint,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(168.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F766E),
            shadowElevation = 4.dp
        ) {
            Text(
                text = checkpoint.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Surface(
            shape = CircleShape,
            color = Color(0xFF0F766E),
            border = BorderStroke(2.dp, Color.White),
            shadowElevation = 6.dp,
            modifier = Modifier.size(30.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.LocalCafe, contentDescription = checkpoint.name, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ConvoyMapMarker(
    member: ConvoyMember,
    isSelected: Boolean,
    isOffRoute: Boolean,
    isStraggler: Boolean,
    isSosBeacon: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSosBeacon) 1.65f else 1f,
        animationSpec = infiniteRepeatable(animation = tween(650), repeatMode = RepeatMode.Reverse),
        label = "pulseScale"
    )

    val labelBg = when {
        isSosBeacon -> Color(0xFF991B1B)
        isOffRoute -> Color(0xFFDC2626)
        isStraggler -> Color(0xFFB45309)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val labelText = if (isSosBeacon || isOffRoute || isStraggler || isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val pinBackground = when {
        isSosBeacon || isOffRoute -> Color(0xFFDC2626)
        member.isCurrentUser -> MaterialTheme.colorScheme.secondary
        member.role == UserRole.TOUR_LEADER -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val pinIconTint = if (isSosBeacon || isOffRoute || member.isCurrentUser || member.role == UserRole.TOUR_LEADER) Color.White else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .width(192.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = labelBg,
            border = BorderStroke(1.dp, if (isSosBeacon || isSelected || isOffRoute || isStraggler) labelBg else MaterialTheme.colorScheme.outline),
            shadowElevation = 4.dp
        ) {
            val prefix = if (isSosBeacon) "SOS · " else if (member.isSweeper) "Sweeper · " else ""
            Text(
                text = prefix + if (member.isCurrentUser) "${member.markerTitle} (Anda)" else member.markerTitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = labelText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Circular Avatar perched right above the Vehicle Marker Icon
        ConvoyAvatar(
            preset = member.avatarPreset,
            photoUri = member.avatarPhotoUri,
            displayName = member.fullName,
            size = 28.dp,
            borderColor = if (isSosBeacon || isOffRoute) Color(0xFFDC2626) else Color.White,
            borderWidth = 2.dp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(contentAlignment = Alignment.Center) {
            if (isSosBeacon) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .scale(pulseScale)
                        .background(Color(0xFFDC2626).copy(alpha = 0.35f), CircleShape)
                )
            }
            Surface(
                shape = CircleShape,
                color = pinBackground,
                border = BorderStroke(2.dp, Color.White),
                shadowElevation = 6.dp,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSosBeacon) Icons.Rounded.Sos else if (member.vehicleType == VehicleType.MOBIL) Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
                        contentDescription = member.markerTitle,
                        tint = pinIconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

private fun formatDistanceLabel(meters: Int): String {
    return if (meters >= 1000) {
        val km = meters / 1000f
        if (km % 1f == 0f) "${km.toInt()} km" else String.format(Locale.US, "%.1f km", km)
    } else {
        "$meters m"
    }
}
