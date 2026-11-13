package com.konvoiku.touring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.konvoiku.touring.model.AvatarPreset
import com.konvoiku.touring.model.ChatMessageType
import com.konvoiku.touring.model.ConnectionStatus
import com.konvoiku.touring.model.ConvoyMember
import com.konvoiku.touring.model.ConvoyUiState
import com.konvoiku.touring.model.MapPosition
import com.konvoiku.touring.model.MemberConditionStatus
import com.konvoiku.touring.model.OffRouteAlertState
import com.konvoiku.touring.model.RestCheckpoint
import com.konvoiku.touring.model.RoomMessage
import com.konvoiku.touring.model.RoutePoint
import com.konvoiku.touring.model.SosEmergencyState
import com.konvoiku.touring.model.StragglerAlertState
import com.konvoiku.touring.model.TourRoom
import com.konvoiku.touring.model.TripHistoryRecord
import com.konvoiku.touring.model.TripParticipantSummary
import com.konvoiku.touring.model.UserRole
import com.konvoiku.touring.model.VehicleType
import com.konvoiku.touring.model.VisitedCheckpointLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

class ConvoyViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ConvoyUiState())
    val uiState: StateFlow<ConvoyUiState> = _uiState.asStateFlow()

    private var voiceTimerJob: Job? = null

    private data class SimTemplate(
        val name: String,
        val vehicleType: VehicleType,
        val vehicleDetail: String,
        val plate: String,
        val phone: String,
        val status: ConnectionStatus,
        val condition: MemberConditionStatus,
        val locationText: String,
        val baseProgress: Float,
        val baseSpeed: Int,
        val avatarPreset: AvatarPreset
    )

    data class DestinationPreset(
        val name: String,
        val endPoint: RoutePoint,
        val totalMeters: Int
    )

    val destinationPresets = listOf(
        DestinationPreset("Puncak Pass Resort", RoutePoint(0.86f, 0.14f, -6.6450, 106.9900), 12000),
        DestinationPreset("Lembang Highland", RoutePoint(0.78f, 0.22f, -6.8120, 107.6180), 15000),
        DestinationPreset("Ciwidey Kawah Putih", RoutePoint(0.24f, 0.18f, -7.1660, 107.4020), 18000),
        DestinationPreset("Rest Area KM 57", RoutePoint(0.88f, 0.44f, -6.3690, 107.3590), 9500)
    )

    private val defaultConvoyTemplates = listOf(
        SimTemplate(
            name = "Budi",
            vehicleType = VehicleType.MOBIL,
            vehicleDetail = "Avanza Hitam",
            plate = "B 1402 KNV",
            phone = "0812-3301-7712",
            status = ConnectionStatus.CONNECTED,
            condition = MemberConditionStatus.AMAN,
            locationText = "Simpang Gadog KM 44",
            baseProgress = 0.68f,
            baseSpeed = 58,
            avatarPreset = AvatarPreset.RIDER_CAPTAIN
        ),
        SimTemplate(
            name = "Siti",
            vehicleType = VehicleType.MOTOR,
            vehicleDetail = "Nmax Putih",
            plate = "D 4821 XZT",
            phone = "0813-9012-4411",
            status = ConnectionStatus.CONNECTED,
            condition = MemberConditionStatus.AMAN,
            locationText = "Jalur Puncak KM 46",
            baseProgress = 0.60f,
            baseSpeed = 54,
            avatarPreset = AvatarPreset.HELMET_TOURING
        ),
        SimTemplate(
            name = "Raka",
            vehicleType = VehicleType.MOTOR,
            vehicleDetail = "Vario 160 Merah",
            plate = "B 6190 TRG",
            phone = "0857-2201-8834",
            status = ConnectionStatus.WEAK_SIGNAL,
            condition = MemberConditionStatus.ISTIRAHAT,
            locationText = "Rest Area Ciawi KM 41",
            baseProgress = 0.54f,
            baseSpeed = 48,
            avatarPreset = AvatarPreset.HELMET_SPORT
        ),
        SimTemplate(
            name = "Nadia",
            vehicleType = VehicleType.MOBIL,
            vehicleDetail = "HR-V Abu-abu",
            plate = "B 2048 KNV",
            phone = "0811-3349-7720",
            status = ConnectionStatus.CONNECTED,
            condition = MemberConditionStatus.AMAN,
            locationText = "Tol Jagorawi KM 39",
            baseProgress = 0.46f,
            baseSpeed = 62,
            avatarPreset = AvatarPreset.PILOT_CLASSIC
        )
    )

    private var extraMemberCounter = 0

    init {
        _uiState.update { state ->
            val seeded = defaultTripHistory()
            state.copy(
                tripHistory = seeded,
                selectedTripId = seeded.firstOrNull()?.id
            )
        }
        startLiveMapTicker()
    }

    private fun startLiveMapTicker() {
        viewModelScope.launch {
            while (isActive) {
                delay(2000L)
                val state = _uiState.value
                val room = state.activeRoom
                if (state.isSimulationRunning && room != null) {
                    val orderedByProgress = room.members.sortedByDescending { it.mapPosition.routeProgress }
                    val lastMemberId = orderedByProgress.lastOrNull()?.id

                    val movedMembers = room.members.mapIndexed { idx, member ->
                        val isStopped = member.conditionStatus == MemberConditionStatus.MOGOK ||
                            member.conditionStatus == MemberConditionStatus.SOS
                        val isTail = member.id == lastMemberId && state.isStragglerSimActive
                        val step = when {
                            isStopped -> 0f
                            isTail -> 0.002f
                            else -> 0.010f + (idx % 2) * 0.002f
                        }
                        val nextProgress = (member.mapPosition.routeProgress + step).let {
                            if (it > 0.90f) 0.16f else it
                        }
                        val offRouteShift = if (member.isCurrentUser && state.isOffRouteSimActive) 0.12f else 0f
                        val nextPos = interpolateAlongPolyline(
                            progress = nextProgress,
                            waypoints = state.routeConfig.waypoints,
                            lateralOffset = offRouteShift,
                            speedKmh = when {
                                isStopped -> 0
                                isTail -> 28
                                else -> (48..65).random()
                            }
                        )
                        member.copy(
                            mapPosition = nextPos,
                            lastLocationText = landmarkForProgress(nextProgress),
                            lastUpdatedLabel = if (member.connectionStatus == ConnectionStatus.WEAK_SIGNAL) {
                                "5 dtk lalu"
                            } else {
                                "Baru saja"
                            }
                        )
                    }
                    recomputeAndCommitRoom(movedMembers)
                }
            }
        }
    }

    fun selectRole(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role) }
    }

    fun updateVehicleProfile(
        fullName: String? = null,
        vehicleType: VehicleType? = null,
        vehicleDetail: String? = null,
        licensePlate: String? = null,
        emergencyContact: String? = null,
        avatarPreset: AvatarPreset? = null,
        avatarPhotoUri: String? = null,
        clearPhoto: Boolean = false
    ) {
        _uiState.update { state ->
            val current = state.vehicleProfile
            state.copy(
                vehicleProfile = current.copy(
                    fullName = fullName ?: current.fullName,
                    vehicleType = vehicleType ?: current.vehicleType,
                    vehicleDetail = vehicleDetail ?: current.vehicleDetail,
                    licensePlate = (licensePlate ?: current.licensePlate).uppercase(),
                    emergencyContact = emergencyContact ?: current.emergencyContact,
                    avatarPreset = avatarPreset ?: current.avatarPreset,
                    avatarPhotoUri = if (clearPhoto) null else (avatarPhotoUri ?: current.avatarPhotoUri)
                )
            )
        }
    }

    fun createNewTourRoom(customTitle: String = "Konvoi Touring Akhir Pekan") {
        val profile = _uiState.value.vehicleProfile
        val routeConfig = _uiState.value.routeConfig
        val roomCode = generateUniqueRoomId()
        val mockLink = "https://konvoiku.id/join/$roomCode"

        val leaderMember = ConvoyMember(
            id = "self-${UUID.randomUUID()}",
            fullName = profile.fullName.trim().ifBlank { "Anda (Leader)" },
            vehicleType = profile.vehicleType,
            vehicleDetail = profile.displayVehicleLabel,
            licensePlate = profile.licensePlate.ifBlank { "B 1001 LDR" },
            emergencyContact = profile.emergencyContact.ifBlank { "0812-0000-0001" },
            role = UserRole.TOUR_LEADER,
            joinedAtLabel = "Baru saja",
            isCurrentUser = true,
            isSweeper = false,
            avatarPreset = profile.avatarPreset,
            avatarPhotoUri = profile.avatarPhotoUri,
            conditionStatus = MemberConditionStatus.AMAN,
            connectionStatus = ConnectionStatus.CONNECTED,
            lastLocationText = "Puncak Pass KM 49",
            lastUpdatedLabel = "Baru saja",
            mapPosition = interpolateAlongPolyline(0.74f, routeConfig.waypoints, 0f, 60)
        )

        val simulatedMembers = defaultConvoyTemplates.take(3).mapIndexed { index, tpl ->
            ConvoyMember(
                id = "sim-$index-${UUID.randomUUID()}",
                fullName = tpl.name,
                vehicleType = tpl.vehicleType,
                vehicleDetail = tpl.vehicleDetail,
                licensePlate = tpl.plate,
                emergencyContact = tpl.phone,
                role = UserRole.MEMBER,
                joinedAtLabel = "Aktif di room",
                isCurrentUser = false,
                isSweeper = index == 1,
                avatarPreset = tpl.avatarPreset,
                conditionStatus = tpl.condition,
                connectionStatus = tpl.status,
                lastLocationText = tpl.locationText,
                lastUpdatedLabel = if (tpl.status == ConnectionStatus.WEAK_SIGNAL) "4 dtk lalu" else "Baru saja",
                mapPosition = interpolateAlongPolyline(tpl.baseProgress, routeConfig.waypoints, 0f, tpl.baseSpeed)
            )
        }

        val allMembers = listOf(leaderMember) + simulatedMembers
        extraMemberCounter = 0
        _uiState.update {
            it.copy(
                activeRoom = TourRoom(
                    roomId = roomCode,
                    roomTitle = customTitle,
                    inviteLink = mockLink,
                    createdAtLabel = "Aktif sekarang",
                    members = allMembers
                ),
                selectedMemberId = leaderMember.id,
                sosState = SosEmergencyState(),
                messages = initialRoomMessages(roomCode),
                isOffRouteSimActive = false,
                isStragglerSimActive = false
            )
        }
        recomputeAndCommitRoom(allMembers)
    }

    fun simulateMemberJoin() {
        val room = _uiState.value.activeRoom ?: return
        val waypoints = _uiState.value.routeConfig.waypoints
        val tpl = defaultConvoyTemplates[extraMemberCounter % defaultConvoyTemplates.size]
        extraMemberCounter++

        val newMember = ConvoyMember(
            id = UUID.randomUUID().toString(),
            fullName = "${tpl.name} #${extraMemberCounter + 1}",
            vehicleType = tpl.vehicleType,
            vehicleDetail = tpl.vehicleDetail,
            licensePlate = tpl.plate,
            emergencyContact = tpl.phone,
            role = UserRole.MEMBER,
            joinedAtLabel = "Baru bergabung",
            isCurrentUser = false,
            avatarPreset = tpl.avatarPreset,
            conditionStatus = MemberConditionStatus.AMAN,
            connectionStatus = ConnectionStatus.CONNECTED,
            lastLocationText = tpl.locationText,
            lastUpdatedLabel = "Baru saja",
            mapPosition = interpolateAlongPolyline(0.50f, waypoints, 0f, tpl.baseSpeed)
        )

        recomputeAndCommitRoom(room.members + newMember)
    }

    fun removeMember(memberId: String) {
        val room = _uiState.value.activeRoom ?: return
        recomputeAndCommitRoom(room.members.filterNot { it.id == memberId && !it.isCurrentUser })
    }

    fun updateJoinInput(value: String) {
        _uiState.update { it.copy(joinInput = value, joinError = null) }
    }

    fun extractRoomId(rawInput: String): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return ""
        return trimmed.substringAfterLast("/", missingDelimiterValue = trimmed)
            .substringBefore("?")
            .trim()
            .uppercase()
    }

    fun joinConvoyRoom(): Boolean {
        val extractedId = extractRoomId(_uiState.value.joinInput)
        if (extractedId.length < 4) {
            _uiState.update {
                it.copy(joinError = "Masukkan Room ID yang valid (contoh: KNV-8492) atau tempel link undangan.")
            }
            return false
        }

        val profile = _uiState.value.vehicleProfile
        val waypoints = _uiState.value.routeConfig.waypoints
        val leaderTpl = defaultConvoyTemplates[0]
        val peerTpl1 = defaultConvoyTemplates[1]
        val peerTpl2 = defaultConvoyTemplates[2]

        val leaderMember = ConvoyMember(
            id = "leader-host",
            fullName = leaderTpl.name,
            vehicleType = leaderTpl.vehicleType,
            vehicleDetail = leaderTpl.vehicleDetail,
            licensePlate = leaderTpl.plate,
            emergencyContact = leaderTpl.phone,
            role = UserRole.TOUR_LEADER,
            joinedAtLabel = "Host Room",
            isCurrentUser = false,
            avatarPreset = leaderTpl.avatarPreset,
            conditionStatus = MemberConditionStatus.AMAN,
            connectionStatus = ConnectionStatus.CONNECTED,
            lastLocationText = "Puncak Pass KM 49",
            lastUpdatedLabel = "Baru saja",
            mapPosition = interpolateAlongPolyline(0.74f, waypoints, 0f, 58)
        )

        val selfMember = ConvoyMember(
            id = "self-${UUID.randomUUID()}",
            fullName = profile.fullName.trim().ifBlank { "Anda" },
            vehicleType = profile.vehicleType,
            vehicleDetail = profile.displayVehicleLabel,
            licensePlate = profile.licensePlate.ifBlank { "B 2026 AGT" },
            emergencyContact = profile.emergencyContact.ifBlank { "0812-9999-8888" },
            role = UserRole.MEMBER,
            joinedAtLabel = "Baru bergabung",
            isCurrentUser = true,
            avatarPreset = profile.avatarPreset,
            avatarPhotoUri = profile.avatarPhotoUri,
            conditionStatus = MemberConditionStatus.AMAN,
            connectionStatus = ConnectionStatus.CONNECTED,
            lastLocationText = "Jalur Puncak KM 45",
            lastUpdatedLabel = "Baru saja",
            mapPosition = interpolateAlongPolyline(0.58f, waypoints, 0f, 54)
        )

        val peer1 = ConvoyMember(
            id = "peer-1",
            fullName = peerTpl1.name,
            vehicleType = peerTpl1.vehicleType,
            vehicleDetail = peerTpl1.vehicleDetail,
            licensePlate = peerTpl1.plate,
            emergencyContact = peerTpl1.phone,
            role = UserRole.MEMBER,
            joinedAtLabel = "Aktif",
            isCurrentUser = false,
            isSweeper = true,
            avatarPreset = peerTpl1.avatarPreset,
            conditionStatus = peerTpl1.condition,
            connectionStatus = peerTpl1.status,
            lastLocationText = peerTpl1.locationText,
            lastUpdatedLabel = "1 dtk lalu",
            mapPosition = interpolateAlongPolyline(0.66f, waypoints, 0f, 52)
        )

        val peer2 = ConvoyMember(
            id = "peer-2",
            fullName = peerTpl2.name,
            vehicleType = peerTpl2.vehicleType,
            vehicleDetail = peerTpl2.vehicleDetail,
            licensePlate = peerTpl2.plate,
            emergencyContact = peerTpl2.phone,
            role = UserRole.MEMBER,
            joinedAtLabel = "Aktif",
            isCurrentUser = false,
            avatarPreset = peerTpl2.avatarPreset,
            conditionStatus = peerTpl2.condition,
            connectionStatus = peerTpl2.status,
            lastLocationText = peerTpl2.locationText,
            lastUpdatedLabel = "5 dtk lalu",
            mapPosition = interpolateAlongPolyline(0.51f, waypoints, 0f, 46)
        )

        val members = listOf(leaderMember, selfMember, peer1, peer2)
        val joinedRoom = TourRoom(
            roomId = extractedId,
            roomTitle = "Konvoi Bersama $extractedId",
            inviteLink = "https://konvoiku.id/join/$extractedId",
            createdAtLabel = "Terhubung",
            members = members
        )

        _uiState.update {
            it.copy(
                activeRoom = joinedRoom,
                joinError = null,
                selectedMemberId = selfMember.id,
                sosState = SosEmergencyState(),
                messages = initialRoomMessages(extractedId),
                isOffRouteSimActive = false,
                isStragglerSimActive = false
            )
        }
        recomputeAndCommitRoom(members)
        return true
    }

    fun selectMapMember(memberId: String) {
        _uiState.update { it.copy(selectedMemberId = memberId) }
    }

    fun toggleSimulation() {
        _uiState.update { it.copy(isSimulationRunning = !it.isSimulationRunning) }
    }

    // --- Checkpoints / Titik Istirahat (Broadcast to all room members) ---

    fun startPickingCheckpointOnMap(checkpointName: String) {
        _uiState.update {
            it.copy(
                isPickingCheckpointOnMap = true,
                isPickingDestinationOnMap = false,
                pendingCheckpointName = checkpointName.trim()
            )
        }
    }

    fun cancelPickingCheckpoint() {
        _uiState.update {
            it.copy(
                isPickingCheckpointOnMap = false,
                pendingCheckpointName = ""
            )
        }
    }

    fun addRestCheckpointAtPoint(name: String, xFraction: Float, yFraction: Float) {
        val cleanName = name.trim().ifBlank { "Titik Istirahat" }
        val clampedX = xFraction.coerceIn(0.10f, 0.90f)
        val clampedY = yFraction.coerceIn(0.10f, 0.88f)
        val lat = -6.7100 + (1f - clampedY) * 0.08
        val lng = 106.8500 + clampedX * 0.16

        val checkpoint = RestCheckpoint(
            id = "cp-${UUID.randomUUID()}",
            name = cleanName,
            xFraction = clampedX,
            yFraction = clampedY,
            latitude = lat,
            longitude = lng
        )

        val broadcastMsg = RoomMessage(
            id = "cp-msg-${UUID.randomUUID()}",
            senderId = "system",
            senderName = "Tour Leader",
            senderVehicle = "",
            type = ChatMessageType.SYSTEM,
            textContent = "Titik istirahat baru dibagikan ke seluruh rombongan: $cleanName",
            timestampLabel = currentClockLabel()
        )

        _uiState.update { state ->
            state.copy(
                routeConfig = state.routeConfig.copy(
                    checkpoints = state.routeConfig.checkpoints + checkpoint
                ),
                isPickingCheckpointOnMap = false,
                pendingCheckpointName = "",
                messages = state.messages + broadcastMsg
            )
        }
    }

    fun removeRestCheckpoint(checkpointId: String) {
        _uiState.update { state ->
            state.copy(
                routeConfig = state.routeConfig.copy(
                    checkpoints = state.routeConfig.checkpoints.filterNot { it.id == checkpointId }
                )
            )
        }
    }

    // --- Stage 4: SOS Emergency, Walkie-Talkie Voice, Text Chat, Condition & Sweeper ---

    fun triggerSosEmergency() {
        val room = _uiState.value.activeRoom ?: return
        val self = room.members.find { it.isCurrentUser } ?: room.members.firstOrNull() ?: return
        val now = currentClockLabel()

        val updatedMembers = room.members.map { member ->
            if (member.id == self.id) {
                member.copy(conditionStatus = MemberConditionStatus.SOS)
            } else {
                member
            }
        }

        val sos = SosEmergencyState(
            isActive = true,
            senderId = self.id,
            senderName = self.fullName,
            senderVehicle = self.vehicleDetail,
            latitude = self.mapPosition.latitude,
            longitude = self.mapPosition.longitude,
            locationText = self.lastLocationText,
            triggeredAtLabel = now
        )

        val sosMessage = RoomMessage(
            id = "msg-${UUID.randomUUID()}",
            senderId = self.id,
            senderName = self.fullName,
            senderVehicle = self.vehicleDetail,
            type = ChatMessageType.SOS_ALERT,
            textContent = "SINYAL DARURAT SOS di ${self.lastLocationText} (${String.format(Locale.US, "%.4f, %.4f", self.mapPosition.latitude, self.mapPosition.longitude)}). Butuh bantuan segera!",
            timestampLabel = now,
            isFromCurrentUser = self.isCurrentUser
        )

        _uiState.update {
            it.copy(
                sosState = sos,
                selectedMemberId = self.id,
                messages = it.messages + sosMessage
            )
        }
        recomputeAndCommitRoom(updatedMembers)
    }

    fun resolveSosEmergency() {
        val room = _uiState.value.activeRoom ?: return
        val sosSenderId = _uiState.value.sosState.senderId
        val updatedMembers = room.members.map { member ->
            if (member.id == sosSenderId && member.conditionStatus == MemberConditionStatus.SOS) {
                member.copy(conditionStatus = MemberConditionStatus.AMAN)
            } else {
                member
            }
        }
        val sysMsg = RoomMessage(
            id = "msg-${UUID.randomUUID()}",
            senderId = "system",
            senderName = "Sistem Konvoi",
            senderVehicle = "",
            type = ChatMessageType.SYSTEM,
            textContent = "Sinyal darurat SOS telah diselesaikan.",
            timestampLabel = currentClockLabel()
        )
        _uiState.update {
            it.copy(
                sosState = SosEmergencyState(),
                messages = it.messages + sysMsg
            )
        }
        recomputeAndCommitRoom(updatedMembers)
    }

    fun sendTextMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val room = _uiState.value.activeRoom ?: return
        val self = room.members.find { it.isCurrentUser }
        val profile = _uiState.value.vehicleProfile
        val message = RoomMessage(
            id = "msg-${UUID.randomUUID()}",
            senderId = self?.id ?: "self",
            senderName = self?.fullName ?: "Anda",
            senderVehicle = self?.vehicleDetail ?: "Kendaraan",
            type = ChatMessageType.TEXT,
            textContent = trimmed,
            timestampLabel = currentClockLabel(),
            isFromCurrentUser = true,
            senderAvatarPreset = self?.avatarPreset ?: profile.avatarPreset,
            senderAvatarPhotoUri = self?.avatarPhotoUri ?: profile.avatarPhotoUri
        )
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    fun startVoiceRecording() {
        if (_uiState.value.isRecordingVoice) return
        _uiState.update { it.copy(isRecordingVoice = true, recordingSeconds = 0) }
        voiceTimerJob?.cancel()
        voiceTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _uiState.update { it.copy(recordingSeconds = it.recordingSeconds + 1) }
            }
        }
    }

    fun stopAndSendVoiceRecording(filePath: String? = null) {
        if (!_uiState.value.isRecordingVoice) return
        voiceTimerJob?.cancel()
        val duration = _uiState.value.recordingSeconds.coerceAtLeast(1)
        val room = _uiState.value.activeRoom
        val self = room?.members?.find { it.isCurrentUser }
        val profile = _uiState.value.vehicleProfile

        val voiceMsg = RoomMessage(
            id = "voice-${UUID.randomUUID()}",
            senderId = self?.id ?: "self",
            senderName = self?.fullName ?: "Anda",
            senderVehicle = self?.vehicleDetail ?: "Kendaraan",
            type = ChatMessageType.VOICE,
            textContent = "Pesan Suara Walkie-Talkie",
            timestampLabel = currentClockLabel(),
            voiceDurationSec = duration,
            voiceFilePath = filePath,
            isFromCurrentUser = true,
            senderAvatarPreset = self?.avatarPreset ?: profile.avatarPreset,
            senderAvatarPhotoUri = self?.avatarPhotoUri ?: profile.avatarPhotoUri
        )

        _uiState.update {
            it.copy(
                isRecordingVoice = false,
                recordingSeconds = 0,
                messages = it.messages + voiceMsg
            )
        }
    }

    fun toggleVoicePlayback(messageId: String) {
        _uiState.update { state ->
            state.copy(
                playingMessageId = if (state.playingMessageId == messageId) null else messageId
            )
        }
    }

    fun updateOwnConditionStatus(newStatus: MemberConditionStatus) {
        val room = _uiState.value.activeRoom ?: return
        val self = room.members.find { it.isCurrentUser } ?: return
        val updatedMembers = room.members.map { member ->
            if (member.isCurrentUser) member.copy(conditionStatus = newStatus) else member
        }
        val statusMsg = RoomMessage(
            id = "status-${UUID.randomUUID()}",
            senderId = self.id,
            senderName = self.fullName,
            senderVehicle = self.vehicleDetail,
            type = ChatMessageType.SYSTEM,
            textContent = "${self.fullName} memperbarui status: ${newStatus.label}",
            timestampLabel = currentClockLabel()
        )
        _uiState.update { it.copy(messages = it.messages + statusMsg) }
        recomputeAndCommitRoom(updatedMembers)
    }

    fun assignSweeper(memberId: String) {
        val room = _uiState.value.activeRoom ?: return
        val target = room.members.find { it.id == memberId } ?: return
        val updatedMembers = room.members.map { member ->
            member.copy(isSweeper = member.id == memberId)
        }
        val sysMsg = RoomMessage(
            id = "sweeper-${UUID.randomUUID()}",
            senderId = "system",
            senderName = "Tour Leader",
            senderVehicle = "",
            type = ChatMessageType.SYSTEM,
            textContent = "${target.fullName} (${target.vehicleDetail}) ditetapkan sebagai Sweeper penutup barisan.",
            timestampLabel = currentClockLabel()
        )
        _uiState.update { it.copy(messages = it.messages + sysMsg) }
        recomputeAndCommitRoom(updatedMembers)
    }

    // --- Stage 3: Leader Route, Destination, Off-Route & Straggler Config ---

    fun selectDestinationPreset(preset: DestinationPreset) {
        val waypoints = buildPolylineToDestination(preset.endPoint)
        val coordText = String.format(
            Locale.US,
            "%.4f, %.4f",
            preset.endPoint.latitude,
            preset.endPoint.longitude
        )
        _uiState.update { state ->
            state.copy(
                routeConfig = state.routeConfig.copy(
                    destinationName = preset.name,
                    destinationAddress = "${preset.name} · $coordText",
                    waypoints = waypoints,
                    totalRouteMeters = preset.totalMeters
                ),
                isPickingDestinationOnMap = false
            )
        }
        syncMembersToUpdatedRoute()
    }

    fun setCustomDestinationPoint(
        xFraction: Float,
        yFraction: Float,
        placeName: String? = null,
        addressLabel: String? = null
    ) {
        val clampedX = xFraction.coerceIn(0.08f, 0.92f)
        val clampedY = yFraction.coerceIn(0.08f, 0.90f)
        val lat = -6.7100 + (1f - clampedY) * 0.08
        val lng = 106.8500 + clampedX * 0.16
        val endPoint = RoutePoint(
            xFraction = clampedX,
            yFraction = clampedY,
            latitude = lat,
            longitude = lng
        )
        val waypoints = buildPolylineToDestination(endPoint)
        val start = waypoints.first()
        val normDist = hypot(clampedX - start.xFraction, clampedY - start.yFraction)
        val estimatedMeters = (normDist * 16500f).roundToInt().coerceIn(3000, 32000)
        val resolvedName = placeName?.trim()?.takeIf { it.isNotEmpty() }
            ?: resolvePlaceNameForCoords(clampedX, clampedY)
        val coordStr = String.format(Locale.US, "%.4f, %.4f", lat, lng)
        val resolvedAddress = addressLabel?.trim()?.takeIf { it.isNotEmpty() }
            ?: "$resolvedName · $coordStr"

        _uiState.update { state ->
            state.copy(
                routeConfig = state.routeConfig.copy(
                    destinationName = resolvedName,
                    destinationAddress = resolvedAddress,
                    waypoints = waypoints,
                    totalRouteMeters = estimatedMeters
                ),
                isPickingDestinationOnMap = false
            )
        }
        syncMembersToUpdatedRoute()
    }

    private fun resolvePlaceNameForCoords(xFraction: Float, yFraction: Float): String {
        return when {
            xFraction > 0.72f && yFraction < 0.28f -> "Kawasan Puncak Pass"
            xFraction in 0.45f..0.72f && yFraction < 0.38f -> "Rest Area Taman Safari"
            xFraction < 0.40f && yFraction < 0.35f -> "Lembang Highland Point"
            xFraction > 0.65f && yFraction > 0.45f -> "Sentul Selatan Exit"
            xFraction < 0.45f && yFraction > 0.55f -> "Simpang Gadog Ciawi"
            else -> "Titik Tujuan Peta"
        }
    }

    fun setPickingDestinationMode(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isPickingDestinationOnMap = enabled,
                isPickingCheckpointOnMap = false
            )
        }
    }

    fun updateCustomWaypoints(waypoints: List<RoutePoint>, destinationName: String? = null) {
        if (waypoints.size < 2) return
        _uiState.update { state ->
            state.copy(
                routeConfig = state.routeConfig.copy(
                    destinationName = destinationName?.takeIf { it.isNotBlank() }
                        ?: state.routeConfig.destinationName,
                    waypoints = waypoints
                )
            )
        }
        syncMembersToUpdatedRoute()
    }

    fun resetDefaultWaypoints() {
        val defaultWaypoints = com.konvoiku.touring.model.ConvoyRouteConfig.defaultWaypoints()
        _uiState.update { state ->
            state.copy(
                routeConfig = state.routeConfig.copy(
                    waypoints = defaultWaypoints
                )
            )
        }
        syncMembersToUpdatedRoute()
    }

    fun updateMaxStragglerDistance(meters: Int) {
        _uiState.update { state ->
            state.copy(
                routeConfig = state.routeConfig.copy(maxStragglerDistanceMeters = meters)
            )
        }
        _uiState.value.activeRoom?.members?.let { recomputeAndCommitRoom(it) }
    }

    fun updateOffRouteTolerance(meters: Int) {
        _uiState.update { state ->
            state.copy(
                routeConfig = state.routeConfig.copy(offRouteToleranceMeters = meters)
            )
        }
        _uiState.value.activeRoom?.members?.let { recomputeAndCommitRoom(it) }
    }

    fun toggleSoundAlert() {
        _uiState.update { state ->
            state.copy(
                routeConfig = state.routeConfig.copy(
                    soundAlertEnabled = !state.routeConfig.soundAlertEnabled
                )
            )
        }
    }

    fun toggleOffRouteSimulation() {
        val nextActive = !_uiState.value.isOffRouteSimActive
        _uiState.update { it.copy(isOffRouteSimActive = nextActive) }
        val room = _uiState.value.activeRoom ?: return
        val waypoints = _uiState.value.routeConfig.waypoints
        val updated = room.members.map { member ->
            if (member.isCurrentUser) {
                val nextPos = interpolateAlongPolyline(
                    progress = member.mapPosition.routeProgress,
                    waypoints = waypoints,
                    lateralOffset = if (nextActive) 0.12f else 0f,
                    speedKmh = member.mapPosition.speedKmh
                )
                member.copy(mapPosition = nextPos)
            } else {
                member
            }
        }
        recomputeAndCommitRoom(updated)
    }

    fun toggleStragglerSimulation() {
        val nextActive = !_uiState.value.isStragglerSimActive
        _uiState.update { it.copy(isStragglerSimActive = nextActive) }
        val room = _uiState.value.activeRoom ?: return
        val waypoints = _uiState.value.routeConfig.waypoints
        val ordered = room.members.sortedByDescending { it.mapPosition.routeProgress }
        val tailId = ordered.lastOrNull()?.id ?: return

        val updated = room.members.map { member ->
            if (member.id == tailId) {
                val targetProgress = if (nextActive) 0.18f else 0.56f
                val nextPos = interpolateAlongPolyline(
                    progress = targetProgress,
                    waypoints = waypoints,
                    lateralOffset = 0f,
                    speedKmh = if (nextActive) 28 else 52
                )
                member.copy(
                    mapPosition = nextPos,
                    lastLocationText = landmarkForProgress(targetProgress)
                )
            } else {
                member
            }
        }
        recomputeAndCommitRoom(updated)
    }

    private fun syncMembersToUpdatedRoute() {
        val room = _uiState.value.activeRoom ?: return
        val state = _uiState.value
        val synced = room.members.map { member ->
            val offset = if (member.isCurrentUser && state.isOffRouteSimActive) 0.12f else 0f
            member.copy(
                mapPosition = interpolateAlongPolyline(
                    progress = member.mapPosition.routeProgress,
                    waypoints = state.routeConfig.waypoints,
                    lateralOffset = offset,
                    speedKmh = member.mapPosition.speedKmh
                )
            )
        }
        recomputeAndCommitRoom(synced)
    }

    private fun recomputeAndCommitRoom(rawMembers: List<ConvoyMember>) {
        val state = _uiState.value
        val route = state.routeConfig
        val room = state.activeRoom ?: return

        val enriched = rawMembers.map { member ->
            val routeMeters = (member.mapPosition.routeProgress * route.totalRouteMeters).roundToInt()
            val deviationMeters = computeOffRouteMeters(
                x = member.mapPosition.xFraction,
                y = member.mapPosition.yFraction,
                waypoints = route.waypoints,
                totalRouteMeters = route.totalRouteMeters
            )
            member.copy(
                routeDistanceMeters = routeMeters,
                offRouteMeters = deviationMeters
            )
        }

        val sortedByRoute = enriched.sortedByDescending { it.routeDistanceMeters }
        val rankMap = sortedByRoute.mapIndexed { idx, m -> m.id to (idx + 1) }.toMap()
        val rankedMembers = enriched.map { it.copy(convoyOrderRank = rankMap[it.id] ?: 1) }

        val self = rankedMembers.find { it.isCurrentUser }
        val selfDeviation = self?.offRouteMeters ?: 0
        val offRouteAlert = OffRouteAlertState(
            isOffRoute = selfDeviation > route.offRouteToleranceMeters,
            deviationMeters = selfDeviation,
            toleranceMeters = route.offRouteToleranceMeters
        )

        val stragglerAlert = if (sortedByRoute.size >= 2) {
            val designatedSweeper = sortedByRoute.find { it.isSweeper && it.id != sortedByRoute.last().id }
            val aheadVehicle = designatedSweeper ?: sortedByRoute[sortedByRoute.size - 2]
            val trailingVehicle = sortedByRoute[sortedByRoute.size - 1]
            val gapMeters = (aheadVehicle.routeDistanceMeters - trailingVehicle.routeDistanceMeters)
                .coerceAtLeast(0)
            StragglerAlertState(
                isTriggered = gapMeters > route.maxStragglerDistanceMeters,
                trailingMember = trailingVehicle,
                aheadMember = aheadVehicle,
                gapMeters = gapMeters,
                maxAllowedMeters = route.maxStragglerDistanceMeters
            )
        } else {
            StragglerAlertState(maxAllowedMeters = route.maxStragglerDistanceMeters)
        }

        _uiState.update {
            it.copy(
                activeRoom = room.copy(members = rankedMembers),
                offRouteAlert = offRouteAlert,
                stragglerAlert = stragglerAlert
            )
        }
    }

    private fun initialRoomMessages(roomCode: String): List<RoomMessage> = listOf(
        RoomMessage(
            id = "init-sys",
            senderId = "system",
            senderName = "Sistem Konvoi",
            senderVehicle = "",
            type = ChatMessageType.SYSTEM,
            textContent = "Room $roomCode aktif. Jaga jarak aman dan pantau titik istirahat di peta.",
            timestampLabel = "09:35"
        ),
        RoomMessage(
            id = "init-1",
            senderId = "leader",
            senderName = "Budi",
            senderVehicle = "Avanza Hitam",
            type = ChatMessageType.TEXT,
            textContent = "Rombongan siap, titik istirahat pertama di Rest Area KM 57.",
            timestampLabel = "09:37",
            senderAvatarPreset = AvatarPreset.RIDER_CAPTAIN
        ),
        RoomMessage(
            id = "init-voice",
            senderId = "sweeper",
            senderName = "Siti",
            senderVehicle = "Nmax Putih",
            type = ChatMessageType.VOICE,
            textContent = "Pesan Suara Walkie-Talkie",
            timestampLabel = "09:39",
            voiceDurationSec = 4,
            senderAvatarPreset = AvatarPreset.HELMET_TOURING
        )
    )

    private fun currentClockLabel(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun buildPolylineToDestination(end: RoutePoint): List<RoutePoint> {
        val start = RoutePoint(0.14f, 0.86f, -6.7100, 106.8500)
        val mid1 = RoutePoint(
            xFraction = start.xFraction + (end.xFraction - start.xFraction) * 0.30f + 0.04f,
            yFraction = start.yFraction + (end.yFraction - start.yFraction) * 0.28f,
            latitude = start.latitude + (end.latitude - start.latitude) * 0.30,
            longitude = start.longitude + (end.longitude - start.longitude) * 0.30
        )
        val mid2 = RoutePoint(
            xFraction = start.xFraction + (end.xFraction - start.xFraction) * 0.65f - 0.03f,
            yFraction = start.yFraction + (end.yFraction - start.yFraction) * 0.62f,
            latitude = start.latitude + (end.latitude - start.latitude) * 0.65,
            longitude = start.longitude + (end.longitude - start.longitude) * 0.65
        )
        return listOf(start, mid1, mid2, end)
    }

    private fun interpolateAlongPolyline(
        progress: Float,
        waypoints: List<RoutePoint>,
        lateralOffset: Float,
        speedKmh: Int
    ): MapPosition {
        val p = progress.coerceIn(0.05f, 0.95f)
        if (waypoints.size < 2) {
            return MapPosition(p, 0.5f, 0.5f, -6.67, 106.92, speedKmh)
        }
        val segmentCount = waypoints.size - 1
        val scaled = p * segmentCount
        val idx = scaled.toInt().coerceIn(0, segmentCount - 1)
        val localT = scaled - idx
        val a = waypoints[idx]
        val b = waypoints[idx + 1]

        val dx = b.xFraction - a.xFraction
        val dy = b.yFraction - a.yFraction
        val len = hypot(dx, dy).coerceAtLeast(0.001f)
        val perpX = -dy / len
        val perpY = dx / len

        val x = (a.xFraction + dx * localT + perpX * lateralOffset).coerceIn(0.06f, 0.94f)
        val y = (a.yFraction + dy * localT + perpY * lateralOffset).coerceIn(0.06f, 0.94f)
        val lat = a.latitude + (b.latitude - a.latitude) * localT
        val lng = a.longitude + (b.longitude - a.longitude) * localT

        return MapPosition(p, x, y, lat, lng, speedKmh)
    }

    private fun computeOffRouteMeters(
        x: Float,
        y: Float,
        waypoints: List<RoutePoint>,
        totalRouteMeters: Int
    ): Int {
        if (waypoints.size < 2) return 0
        var minNormalizedDist = Float.MAX_VALUE
        for (i in 0 until waypoints.size - 1) {
            val a = waypoints[i]
            val b = waypoints[i + 1]
            val abx = b.xFraction - a.xFraction
            val aby = b.yFraction - a.yFraction
            val abLenSq = abx * abx + aby * aby
            val t = if (abLenSq > 0f) {
                (((x - a.xFraction) * abx + (y - a.yFraction) * aby) / abLenSq).coerceIn(0f, 1f)
            } else 0f
            val projX = a.xFraction + t * abx
            val projY = a.yFraction + t * aby
            val dist = hypot(x - projX, y - projY)
            if (dist < minNormalizedDist) {
                minNormalizedDist = dist
            }
        }
        return (minNormalizedDist * (totalRouteMeters * 0.066f)).roundToInt()
    }

    private fun landmarkForProgress(progress: Float): String {
        return when {
            progress < 0.30f -> "Tol Jagorawi KM 39"
            progress < 0.50f -> "Rest Area Ciawi KM 42"
            progress < 0.70f -> "Simpang Gadog KM 45"
            else -> "Jalur Puncak KM 49"
        }
    }

    fun selectTripHistory(tripId: String) {
        _uiState.update { it.copy(selectedTripId = tripId) }
    }

    fun finishAndSaveCurrentTrip(): TripHistoryRecord? {
        val state = _uiState.value
        val room = state.activeRoom ?: return null
        val route = state.routeConfig
        val role = state.selectedRole ?: UserRole.MEMBER

        val activeSpeeds = room.members.map { it.mapPosition.speedKmh }.filter { it > 0 }
        val avgSpeed = if (activeSpeeds.isNotEmpty()) activeSpeeds.average().roundToInt() else 52
        val maxSpeed = (activeSpeeds.maxOrNull() ?: 64) + 12

        val distanceKm = (route.totalRouteMeters / 1000.0).coerceAtLeast(1.0)
        val estMinutes = ((distanceKm / avgSpeed.coerceAtLeast(25)) * 60.0)
            .roundToInt()
            .coerceAtLeast(15)
        val hours = estMinutes / 60
        val mins = estMinutes % 60
        val durationLabel = if (hours > 0) "${hours}j ${mins}m" else "$mins mnt"

        val nowLabel = currentClockLabel()
        val dateLabel = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(Date())

        val checkpointLogs = route.checkpoints.mapIndexed { idx, cp ->
            VisitedCheckpointLog(
                id = cp.id,
                name = cp.name,
                arrivedAtLabel = "Check #${idx + 1}",
                xFraction = cp.xFraction,
                yFraction = cp.yFraction,
                latitude = cp.latitude,
                longitude = cp.longitude
            )
        }

        val participants = room.members.map { m ->
            TripParticipantSummary(
                id = m.id,
                fullName = m.fullName,
                vehicleType = m.vehicleType,
                vehicleDetail = m.vehicleDetail,
                licensePlate = m.licensePlate,
                role = m.role,
                isSweeper = m.isSweeper,
                avatarPreset = m.avatarPreset,
                avatarPhotoUri = m.avatarPhotoUri
            )
        }

        val record = TripHistoryRecord(
            id = "trip-${UUID.randomUUID()}",
            roomId = room.roomId,
            tourTitle = room.roomTitle,
            dateLabel = dateLabel,
            timeRangeLabel = "Selesai $nowLabel",
            durationLabel = durationLabel,
            durationMinutes = estMinutes,
            distanceKm = distanceKm,
            avgSpeedKmh = avgSpeed,
            maxSpeedKmh = maxSpeed,
            userRole = role,
            destinationName = route.destinationName,
            destinationAddress = route.destinationAddress,
            waypoints = route.waypoints,
            visitedCheckpoints = checkpointLogs,
            participants = participants
        )

        _uiState.update {
            it.copy(
                tripHistory = listOf(record) + it.tripHistory,
                selectedTripId = record.id,
                activeRoom = null,
                sosState = SosEmergencyState()
            )
        }
        return record
    }

    private fun defaultTripHistory(): List<TripHistoryRecord> = listOf(
        TripHistoryRecord(
            id = "trip-seed-1",
            roomId = "KNV-7391",
            tourTitle = "Touring Puncak Sunrise",
            dateLabel = "08 Nov 2026",
            timeRangeLabel = "05:30 - 08:15",
            durationLabel = "2j 45m",
            durationMinutes = 165,
            distanceKm = 68.4,
            avgSpeedKmh = 54,
            maxSpeedKmh = 82,
            userRole = UserRole.TOUR_LEADER,
            destinationName = "Puncak Pass Resort",
            destinationAddress = "Jl. Raya Puncak KM 90, Cisarua · -6.6450, 106.9900",
            waypoints = listOf(
                RoutePoint(0.14f, 0.86f, -6.7100, 106.8500),
                RoutePoint(0.32f, 0.65f, -6.6920, 106.8850),
                RoutePoint(0.55f, 0.48f, -6.6740, 106.9250),
                RoutePoint(0.72f, 0.30f, -6.6580, 106.9600),
                RoutePoint(0.86f, 0.14f, -6.6450, 106.9900)
            ),
            visitedCheckpoints = listOf(
                VisitedCheckpointLog("cp-s1", "Rest Area KM 57", "06:20", 0.36f, 0.62f, -6.6900, 106.8900),
                VisitedCheckpointLog("cp-s2", "Warung Kopi Cisarua", "07:15", 0.66f, 0.36f, -6.6640, 106.9480)
            ),
            participants = listOf(
                TripParticipantSummary("p1", "Anda (Leader)", VehicleType.MOBIL, "Avanza Hitam", "B 1001 LDR", UserRole.TOUR_LEADER, avatarPreset = AvatarPreset.RIDER_CAPTAIN),
                TripParticipantSummary("p2", "Siti", VehicleType.MOTOR, "Nmax Putih", "D 4821 XZT", UserRole.MEMBER, isSweeper = true, avatarPreset = AvatarPreset.HELMET_TOURING),
                TripParticipantSummary("p3", "Raka", VehicleType.MOTOR, "Vario 160 Merah", "B 6190 TRG", UserRole.MEMBER, avatarPreset = AvatarPreset.HELMET_SPORT),
                TripParticipantSummary("p4", "Nadia", VehicleType.MOBIL, "HR-V Abu-abu", "B 2048 KNV", UserRole.MEMBER, avatarPreset = AvatarPreset.PILOT_CLASSIC)
            )
        ),
        TripHistoryRecord(
            id = "trip-seed-2",
            roomId = "KNV-4120",
            tourTitle = "Lembang Highland Weekend",
            dateLabel = "01 Nov 2026",
            timeRangeLabel = "07:00 - 10:20",
            durationLabel = "3j 20m",
            durationMinutes = 200,
            distanceKm = 94.2,
            avgSpeedKmh = 49,
            maxSpeedKmh = 78,
            userRole = UserRole.MEMBER,
            destinationName = "Lembang Highland",
            destinationAddress = "Jl. Raya Lembang No. 12, Bandung · -6.8120, 107.6180",
            waypoints = listOf(
                RoutePoint(0.16f, 0.84f, -6.8900, 107.4800),
                RoutePoint(0.38f, 0.68f, -6.8650, 107.5200),
                RoutePoint(0.52f, 0.44f, -6.8420, 107.5650),
                RoutePoint(0.78f, 0.22f, -6.8120, 107.6180)
            ),
            visitedCheckpoints = listOf(
                VisitedCheckpointLog("cp-s3", "SPBU Padalarang KM 120", "08:05", 0.38f, 0.68f, -6.8650, 107.5200),
                VisitedCheckpointLog("cp-s4", "Rumah Makan Sate Pak Budi", "09:10", 0.52f, 0.44f, -6.8420, 107.5650)
            ),
            participants = listOf(
                TripParticipantSummary("p5", "Budi", VehicleType.MOBIL, "Pajero Sport", "B 1402 KNV", UserRole.TOUR_LEADER, avatarPreset = AvatarPreset.RALLY_PRO),
                TripParticipantSummary("p6", "Anda", VehicleType.MOTOR, "PCX 160 Hitam", "B 2026 AGT", UserRole.MEMBER, avatarPreset = AvatarPreset.HELMET_TOURING),
                TripParticipantSummary("p7", "Siti", VehicleType.MOTOR, "Nmax Putih", "D 4821 XZT", UserRole.MEMBER, isSweeper = true, avatarPreset = AvatarPreset.HELMET_SPORT)
            )
        ),
        TripHistoryRecord(
            id = "trip-seed-3",
            roomId = "KNV-5583",
            tourTitle = "Eksplorasi Kawah Putih Ciwidey",
            dateLabel = "18 Okt 2026",
            timeRangeLabel = "06:15 - 10:35",
            durationLabel = "4j 20m",
            durationMinutes = 260,
            distanceKm = 118.5,
            avgSpeedKmh = 46,
            maxSpeedKmh = 74,
            userRole = UserRole.MEMBER,
            destinationName = "Ciwidey Kawah Putih",
            destinationAddress = "Kawasan Wisata Kawah Putih, Ciwidey · -7.1660, 107.4020",
            waypoints = listOf(
                RoutePoint(0.84f, 0.85f, -6.9400, 107.6100),
                RoutePoint(0.64f, 0.66f, -7.0150, 107.5400),
                RoutePoint(0.45f, 0.42f, -7.0950, 107.4700),
                RoutePoint(0.24f, 0.18f, -7.1660, 107.4020)
            ),
            visitedCheckpoints = listOf(
                VisitedCheckpointLog("cp-s5", "Rest Area Soreang KM 14", "07:40", 0.64f, 0.66f, -7.0150, 107.5400)
            ),
            participants = listOf(
                TripParticipantSummary("p8", "Dimas", VehicleType.MOBIL, "Innova Zenix", "D 1180 KNV", UserRole.TOUR_LEADER, avatarPreset = AvatarPreset.RIDER_CAPTAIN),
                TripParticipantSummary("p9", "Anda", VehicleType.MOBIL, "Brio RS Kuning", "B 1945 KNV", UserRole.MEMBER, avatarPreset = AvatarPreset.STANDARD_USER),
                TripParticipantSummary("p10", "Nadia", VehicleType.MOBIL, "HR-V Abu-abu", "B 2048 KNV", UserRole.MEMBER, isSweeper = true, avatarPreset = AvatarPreset.PILOT_CLASSIC)
            )
        )
    )

    private fun generateUniqueRoomId(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val suffix = (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        return "KNV-$suffix"
    }
}
