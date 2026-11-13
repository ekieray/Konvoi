package com.konvoiku.touring.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

enum class UserRole(val label: String, val subtitle: String) {
    TOUR_LEADER(
        label = "Tour Leader",
        subtitle = "Buat room baru, bagikan link akses, dan kelola daftar anggota konvoi."
    ),
    MEMBER(
        label = "Anggota Tour",
        subtitle = "Gabung ke room konvoi menggunakan Room ID atau link undangan."
    )
}

enum class VehicleType(val label: String) {
    MOBIL("Mobil"),
    MOTOR("Motor")
}

enum class ConnectionStatus(val label: String) {
    CONNECTED("Terhubung"),
    WEAK_SIGNAL("Sinyal Lemah"),
    OFFLINE("Terputus")
}

enum class MemberConditionStatus(val label: String) {
    AMAN("Aman"),
    ISTIRAHAT("Istirahat / Rest Area"),
    MOGOK("Kendaraan Mogok"),
    SOS("Darurat SOS")
}

enum class ChatMessageType {
    TEXT,
    VOICE,
    SOS_ALERT,
    SYSTEM
}

enum class AvatarPreset(val label: String) {
    HELMET_TOURING("Helm Touring"),
    HELMET_SPORT("Helm Sport"),
    RIDER_CAPTAIN("Kapten Rider"),
    PILOT_CLASSIC("Pilot Klasik"),
    RALLY_PRO("Rally Pro"),
    STANDARD_USER("Foto Standar")
}

data class RoomMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderVehicle: String,
    val type: ChatMessageType,
    val textContent: String,
    val timestampLabel: String,
    val voiceDurationSec: Int = 0,
    val voiceFilePath: String? = null,
    val isFromCurrentUser: Boolean = false,
    val senderAvatarPreset: AvatarPreset = AvatarPreset.HELMET_TOURING,
    val senderAvatarPhotoUri: String? = null
)

data class SosEmergencyState(
    val isActive: Boolean = false,
    val senderId: String = "",
    val senderName: String = "",
    val senderVehicle: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationText: String = "",
    val triggeredAtLabel: String = ""
)

data class RoutePoint(
    val xFraction: Float,
    val yFraction: Float,
    val latitude: Double,
    val longitude: Double
)

data class RestCheckpoint(
    val id: String,
    val name: String,
    val xFraction: Float,
    val yFraction: Float,
    val latitude: Double,
    val longitude: Double
)

data class ConvoyRouteConfig(
    val destinationName: String = "Puncak Pass Resort",
    val destinationAddress: String = "Jl. Raya Puncak KM 90, Cisarua · -6.6450, 106.9900",
    val waypoints: List<RoutePoint> = defaultWaypoints(),
    val checkpoints: List<RestCheckpoint> = defaultCheckpoints(),
    val totalRouteMeters: Int = 12000,
    val offRouteToleranceMeters: Int = 50,
    val maxStragglerDistanceMeters: Int = 1000,
    val soundAlertEnabled: Boolean = true
) {
    companion object {
        fun defaultWaypoints(): List<RoutePoint> = listOf(
            RoutePoint(0.14f, 0.86f, -6.7100, 106.8500),
            RoutePoint(0.32f, 0.65f, -6.6920, 106.8850),
            RoutePoint(0.55f, 0.48f, -6.6740, 106.9250),
            RoutePoint(0.72f, 0.30f, -6.6580, 106.9600),
            RoutePoint(0.86f, 0.14f, -6.6450, 106.9900)
        )

        fun defaultCheckpoints(): List<RestCheckpoint> = listOf(
            RestCheckpoint(
                id = "cp-default-1",
                name = "Rest Area KM 57",
                xFraction = 0.44f,
                yFraction = 0.56f,
                latitude = -6.6830,
                longitude = 106.9050
            )
        )
    }
}

data class StragglerAlertState(
    val isTriggered: Boolean = false,
    val trailingMember: ConvoyMember? = null,
    val aheadMember: ConvoyMember? = null,
    val gapMeters: Int = 0,
    val maxAllowedMeters: Int = 1000
)

data class OffRouteAlertState(
    val isOffRoute: Boolean = false,
    val deviationMeters: Int = 0,
    val toleranceMeters: Int = 50
)

data class MapPosition(
    val routeProgress: Float,
    val xFraction: Float,
    val yFraction: Float,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Int
)

data class VehicleProfile(
    val fullName: String = "",
    val vehicleType: VehicleType = VehicleType.MOBIL,
    val vehicleDetail: String = "",
    val licensePlate: String = "",
    val emergencyContact: String = "",
    val avatarPreset: AvatarPreset = AvatarPreset.HELMET_TOURING,
    val avatarPhotoUri: String? = null
) {
    val isValid: Boolean
        get() = fullName.trim().length >= 2 &&
            licensePlate.trim().length >= 3 &&
            emergencyContact.trim().length >= 8

    val displayVehicleLabel: String
        get() = vehicleDetail.trim().ifBlank { vehicleType.label }
}

data class ConvoyMember(
    val id: String,
    val fullName: String,
    val vehicleType: VehicleType,
    val vehicleDetail: String,
    val licensePlate: String,
    val emergencyContact: String,
    val role: UserRole,
    val joinedAtLabel: String,
    val isCurrentUser: Boolean = false,
    val isSweeper: Boolean = false,
    val avatarPreset: AvatarPreset = AvatarPreset.HELMET_TOURING,
    val avatarPhotoUri: String? = null,
    val conditionStatus: MemberConditionStatus = MemberConditionStatus.AMAN,
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED,
    val lastLocationText: String = "Tol Jagorawi KM 39",
    val lastUpdatedLabel: String = "Baru saja",
    val routeDistanceMeters: Int = 5000,
    val offRouteMeters: Int = 0,
    val convoyOrderRank: Int = 1,
    val mapPosition: MapPosition = MapPosition(
        routeProgress = 0.5f,
        xFraction = 0.5f,
        yFraction = 0.5f,
        latitude = -6.6740,
        longitude = 106.9250,
        speedKmh = 54
    )
) {
    val markerTitle: String
        get() = "$fullName - ${vehicleDetail.ifBlank { vehicleType.label }}"
}

data class DestinationEtaInfo(
    val remainingMeters: Int,
    val speedKmh: Int,
    val etaMinutes: Int?,
    val isStopped: Boolean,
    val isArrived: Boolean,
    val durationLabel: String,
    val arrivalClockLabel: String,
    val distanceLabel: String
)

fun calculateDestinationEta(
    member: ConvoyMember?,
    routeConfig: ConvoyRouteConfig,
    nowMillis: Long = System.currentTimeMillis()
): DestinationEtaInfo {
    val progress = (member?.mapPosition?.routeProgress ?: 0f).coerceIn(0f, 1f)
    val traveledMeters = (progress * routeConfig.totalRouteMeters).roundToInt()
    val remainingMeters = (routeConfig.totalRouteMeters - traveledMeters).coerceAtLeast(0)
    val speedKmh = (member?.mapPosition?.speedKmh ?: 0).coerceAtLeast(0)
    val distanceLabel = if (remainingMeters >= 1000) {
        String.format(Locale.US, "%.1f km", remainingMeters / 1000.0)
    } else {
        "$remainingMeters m"
    }
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    if (remainingMeters <= 50) {
        return DestinationEtaInfo(
            remainingMeters = 0,
            speedKmh = speedKmh,
            etaMinutes = 0,
            isStopped = false,
            isArrived = true,
            durationLabel = "Tiba",
            arrivalClockLabel = formatter.format(Date(nowMillis)),
            distanceLabel = "0 m"
        )
    }

    if (speedKmh <= 0) {
        return DestinationEtaInfo(
            remainingMeters = remainingMeters,
            speedKmh = 0,
            etaMinutes = null,
            isStopped = true,
            isArrived = false,
            durationLabel = "Berhenti",
            arrivalClockLabel = "--:--",
            distanceLabel = distanceLabel
        )
    }

    val etaMinutes = ((remainingMeters / 1000.0 / speedKmh.toDouble()) * 60.0)
        .roundToInt()
        .coerceAtLeast(1)

    val durationLabel = if (etaMinutes >= 60) {
        val hours = etaMinutes / 60
        val mins = etaMinutes % 60
        if (mins > 0) "${hours}j ${mins}m" else "$hours jam"
    } else {
        "$etaMinutes mnt"
    }

    val arrivalClockLabel = formatter.format(Date(nowMillis + etaMinutes * 60_000L))

    return DestinationEtaInfo(
        remainingMeters = remainingMeters,
        speedKmh = speedKmh,
        etaMinutes = etaMinutes,
        isStopped = false,
        isArrived = false,
        durationLabel = durationLabel,
        arrivalClockLabel = arrivalClockLabel,
        distanceLabel = distanceLabel
    )
}

data class TripParticipantSummary(
    val id: String,
    val fullName: String,
    val vehicleType: VehicleType,
    val vehicleDetail: String,
    val licensePlate: String,
    val role: UserRole,
    val isSweeper: Boolean = false,
    val avatarPreset: AvatarPreset = AvatarPreset.HELMET_TOURING,
    val avatarPhotoUri: String? = null
)

data class VisitedCheckpointLog(
    val id: String,
    val name: String,
    val arrivedAtLabel: String,
    val xFraction: Float,
    val yFraction: Float,
    val latitude: Double,
    val longitude: Double
)

data class TripHistoryRecord(
    val id: String,
    val roomId: String,
    val tourTitle: String,
    val dateLabel: String,
    val timeRangeLabel: String,
    val durationLabel: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val avgSpeedKmh: Int,
    val maxSpeedKmh: Int,
    val userRole: UserRole,
    val destinationName: String,
    val destinationAddress: String,
    val waypoints: List<RoutePoint>,
    val visitedCheckpoints: List<VisitedCheckpointLog>,
    val participants: List<TripParticipantSummary>
)

enum class TripCardTheme(val label: String, val subtitle: String) {
    DARK_CARBON("Mode Gelap", "Karbon kontras tinggi"),
    SATELLITE("Peta Satelit", "Topografi & citra satelit"),
    MINIMAL_LIGHT("Minimalis", "Putih bersih editorial"),
    CUSTOM_PHOTO("Foto Galeri", "Latar foto perjalanan")
}

fun buildTripGpxXml(trip: TripHistoryRecord): String {
    val sb = StringBuilder()
    sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
    sb.appendLine("""<gpx version="1.1" creator="KonvoiKu Touring App" xmlns="http://www.topografix.com/GPX/1/1">""")
    sb.appendLine("  <metadata>")
    sb.appendLine("    <name>${escapeXml(trip.tourTitle)} (${trip.roomId})</name>")
    sb.appendLine("    <desc>Konvoi Touring menuju ${escapeXml(trip.destinationName)} · ${trip.distanceKm} km · Rata-rata ${trip.avgSpeedKmh} km/j</desc>")
    sb.appendLine("  </metadata>")

    trip.waypoints.firstOrNull()?.let { start ->
        sb.appendLine("""  <wpt lat="${String.format(Locale.US, "%.6f", start.latitude)}" lon="${String.format(Locale.US, "%.6f", start.longitude)}">""")
        sb.appendLine("    <name>Titik Mulai Konvoi</name>")
        sb.appendLine("    <type>Start</type>")
        sb.appendLine("  </wpt>")
    }

    trip.visitedCheckpoints.forEach { cp ->
        sb.appendLine("""  <wpt lat="${String.format(Locale.US, "%.6f", cp.latitude)}" lon="${String.format(Locale.US, "%.6f", cp.longitude)}">""")
        sb.appendLine("    <name>${escapeXml(cp.name)}</name>")
        sb.appendLine("    <desc>Titik Istirahat · Tiba ${cp.arrivedAtLabel}</desc>")
        sb.appendLine("    <type>Rest Area</type>")
        sb.appendLine("  </wpt>")
    }

    trip.waypoints.lastOrNull()?.let { end ->
        sb.appendLine("""  <wpt lat="${String.format(Locale.US, "%.6f", end.latitude)}" lon="${String.format(Locale.US, "%.6f", end.longitude)}">""")
        sb.appendLine("    <name>Tujuan: ${escapeXml(trip.destinationName)}</name>")
        sb.appendLine("    <desc>${escapeXml(trip.destinationAddress)}</desc>")
        sb.appendLine("    <type>Destination</type>")
        sb.appendLine("  </wpt>")
    }

    sb.appendLine("  <trk>")
    sb.appendLine("    <name>${escapeXml(trip.tourTitle)}</name>")
    sb.appendLine("    <trkseg>")
    trip.waypoints.forEach { pt ->
        sb.appendLine("""      <trkpt lat="${String.format(Locale.US, "%.6f", pt.latitude)}" lon="${String.format(Locale.US, "%.6f", pt.longitude)}"/>""")
    }
    sb.appendLine("    </trkseg>")
    sb.appendLine("  </trk>")
    sb.appendLine("</gpx>")
    return sb.toString()
}

fun buildTripJsonLog(trip: TripHistoryRecord): String {
    val participantsJson = trip.participants.joinToString(",\n    ") { p ->
        """{"name":"${escapeJson(p.fullName)}","role":"${p.role.label}","vehicle":"${escapeJson(p.vehicleDetail)}","plate":"${escapeJson(p.licensePlate)}","sweeper":${p.isSweeper}}"""
    }
    val checkpointsJson = trip.visitedCheckpoints.joinToString(",\n    ") { cp ->
        """{"name":"${escapeJson(cp.name)}","arrivedAt":"${escapeJson(cp.arrivedAtLabel)}","lat":${String.format(Locale.US, "%.6f", cp.latitude)},"lng":${String.format(Locale.US, "%.6f", cp.longitude)}}"""
    }
    val trackJson = trip.waypoints.joinToString(",\n    ") { pt ->
        """{"lat":${String.format(Locale.US, "%.6f", pt.latitude)},"lng":${String.format(Locale.US, "%.6f", pt.longitude)}}"""
    }
    return """
{
  "app": "KonvoiKu",
  "roomId": "${escapeJson(trip.roomId)}",
  "tourTitle": "${escapeJson(trip.tourTitle)}",
  "date": "${escapeJson(trip.dateLabel)}",
  "timeRange": "${escapeJson(trip.timeRangeLabel)}",
  "duration": "${escapeJson(trip.durationLabel)}",
  "distanceKm": ${String.format(Locale.US, "%.1f", trip.distanceKm)},
  "avgSpeedKmh": ${trip.avgSpeedKmh},
  "maxSpeedKmh": ${trip.maxSpeedKmh},
  "destination": "${escapeJson(trip.destinationName)}",
  "userRole": "${trip.userRole.label}",
  "participants": [
    $participantsJson
  ],
  "checkpoints": [
    $checkpointsJson
  ],
  "trackPoints": [
    $trackJson
  ]
}
""".trimIndent()
}

fun buildTripShareSummaryText(trip: TripHistoryRecord): String {
    val cpList = if (trip.visitedCheckpoints.isEmpty()) {
        "Tidak ada perhentian khusus"
    } else {
        trip.visitedCheckpoints.joinToString(", ") { "${it.name} (${it.arrivedAtLabel})" }
    }
    val memberList = trip.participants.joinToString(", ") { it.fullName }
    return buildString {
        appendLine("🏁 REKAP TOUR KONVOIKU")
        appendLine("Tour: ${trip.tourTitle} (${trip.roomId})")
        appendLine("Tanggal: ${trip.dateLabel} (${trip.timeRangeLabel})")
        appendLine("Tujuan: ${trip.destinationName}")
        appendLine("Jarak Tempuh: ${String.format(Locale.US, "%.1f", trip.distanceKm)} km · Durasi: ${trip.durationLabel}")
        appendLine("Kecepatan Rata-rata: ${trip.avgSpeedKmh} km/j (Maks ${trip.maxSpeedKmh} km/j)")
        appendLine("Titik Istirahat: $cpList")
        appendLine("Peserta (${trip.participants.size}): $memberList")
    }.trim()
}

private fun escapeXml(input: String): String =
    input.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

private fun escapeJson(input: String): String =
    input.replace("\\", "\\\\").replace("\"", "\\\"")

data class TourRoom(
    val roomId: String,
    val roomTitle: String,
    val inviteLink: String,
    val createdAtLabel: String,
    val members: List<ConvoyMember>
)

data class ConvoyUiState(
    val selectedRole: UserRole? = null,
    val vehicleProfile: VehicleProfile = VehicleProfile(),
    val activeRoom: TourRoom? = null,
    val joinInput: String = "",
    val joinError: String? = null,
    val selectedMemberId: String? = null,
    val isSimulationRunning: Boolean = true,
    val routeConfig: ConvoyRouteConfig = ConvoyRouteConfig(),
    val stragglerAlert: StragglerAlertState = StragglerAlertState(),
    val offRouteAlert: OffRouteAlertState = OffRouteAlertState(),
    val sosState: SosEmergencyState = SosEmergencyState(),
    val messages: List<RoomMessage> = emptyList(),
    val isRecordingVoice: Boolean = false,
    val recordingSeconds: Int = 0,
    val playingMessageId: String? = null,
    val isPickingDestinationOnMap: Boolean = false,
    val isPickingCheckpointOnMap: Boolean = false,
    val pendingCheckpointName: String = "",
    val isOffRouteSimActive: Boolean = false,
    val isStragglerSimActive: Boolean = false,
    val tripHistory: List<TripHistoryRecord> = emptyList(),
    val selectedTripId: String? = null
)
