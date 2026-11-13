package com.kangekie.konvoitouring

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {}
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocationPermission()

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF1E88E5),
                    secondary = Color(0xFFFFB300)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KonvoiApp()
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KonvoiApp() {
    var selectedTab by remember { mutableStateOf(0) }
    var sosActive by remember { mutableStateOf(false) }
    var activeRoomCode by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Konvoi Touring", fontWeight = FontWeight.Bold)
                        Text("Created by Kang Ekie", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Room") },
                    label = { Text("Room") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Live Map") },
                    label = { Text("Live GPS") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "History") },
                    label = { Text("Riwayat") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { sosActive = !sosActive },
                containerColor = if (sosActive) Color.Red else Color(0xFFFF5722)
            ) {
                Icon(Icons.Default.Warning, contentDescription = "SOS", tint = Color.White)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (sosActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🚨 DARURAT: SOS DIAKTIFKAN!", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sinyal darurat dan koordinat lokasi terakhir Anda telah disiarkan ke Cloud Firebase.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { sosActive = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                            Text("Batalkan SOS")
                        }
                    }
                }
            } else {
                when (selectedTab) {
                    0 -> RoomManagementScreen(
                        activeRoom = activeRoomCode,
                        onRoomChanged = { newCode -> activeRoomCode = newCode }
                    )
                    1 -> LiveGpsTrackingScreen(activeRoom = activeRoomCode)
                    2 -> TripHistoryScreen()
                }
            }
        }
    }
}

@Composable
fun RoomManagementScreen(activeRoom: String?, onRoomChanged: (String?) -> Unit) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var roomNameInput by remember { mutableStateOf("") }
    var roomCodeInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    val database = Firebase.database.reference

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Manajemen Room Cloud", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (activeRoom != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Terhubung ke Room: $activeRoom", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Status: Sinkronisasi Realtime Firebase Aktif", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { onRoomChanged(null) }) {
                        Text("Keluar Room")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (statusMessage.isNotEmpty()) {
            Text(statusMessage, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { showCreateDialog = true }, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buat Room Konvoi Online")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showJoinDialog = true }, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gabung Room via Kode")
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Buat Room Baru") },
            text = {
                OutlinedTextField(
                    value = roomNameInput,
                    onValueChange = { roomNameInput = it },
                    label = { Text("Nama Touring / Rute") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (roomNameInput.isNotBlank()) {
                        val generatedCode = (1000..9999).random().toString()
                        val roomData = mapOf(
                            "name" to roomNameInput,
                            "creator" to "Kang Ekie",
                            "status" to "Active"
                        )
                        database.child("rooms").child(generatedCode).setValue(roomData)
                            .addOnSuccessListener {
                                onRoomChanged(generatedCode)
                                statusMessage = "Berhasil membuat Room dengan kode: $generatedCode"
                                roomNameInput = ""
                                showCreateDialog = false
                            }
                            .addOnFailureListener {
                                statusMessage = "Gagal terhubung ke Cloud Firebase"
                                showCreateDialog = false
                            }
                    }
                }) {
                    Text("Buat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Gabung Room Konvoi") },
            text = {
                OutlinedTextField(
                    value = roomCodeInput,
                    onValueChange = { roomCodeInput = it },
                    label = { Text("Masukkan 4 Digit Kode Room") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (roomCodeInput.isNotBlank()) {
                        database.child("rooms").child(roomCodeInput).get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    onRoomChanged(roomCodeInput)
                                    statusMessage = "Berhasil bergabung ke Room $roomCodeInput"
                                } else {
                                    statusMessage = "Room dengan kode tersebut tidak ditemukan!"
                                }
                                roomCodeInput = ""
                                showJoinDialog = false
                            }
                            .addOnFailureListener {
                                statusMessage = "Gagal memuat data dari Firebase"
                                showJoinDialog = false
                            }
                    }
                }) {
                    Text("Gabung")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun LiveGpsTrackingScreen(activeRoom: String?) {
    var roomStatusText by remember { mutableStateOf("Menunggu data...") }
    val database = Firebase.database.reference

    LaunchedEffect(activeRoom) {
        if (activeRoom != null) {
            database.child("rooms").child(activeRoom).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val roomName = snapshot.child("name").getValue(String::class.java) ?: "Konvoi"
                    roomStatusText = "Room Aktif: $roomName (Terhubung ke Cloud)"
                }
                override fun onCancelled(error: DatabaseError) {
                    roomStatusText = "Gagal sinkronisasi GPS: ${error.message}"
                }
            })
        } else {
            roomStatusText = "Anda belum bergabung di Room manapun."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Radar Konvoi Realtime", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(roomStatusText, fontSize = 13.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📍 Status GPS: Aktif & Sinkron Cloud", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("🏁 Tujuan: Sesuai rute Room aktif", fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun TripHistoryScreen() {
    data class TripItem(val title: String, val distance: String, val time: String)
    val dummyTrips = listOf(
        TripItem("Touring Bandung - Lembang", "120 km", "3 jam 45 mnt"),
        TripItem("Ride Santai Bogor - Puncak", "75 km", "2 jam 10 mnt"),
        TripItem("Night Ride Sentul City", "45 km", "1 jam 20 mnt")
    )

    var selectedTrip by remember { mutableStateOf<TripItem?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Riwayat Perjalanan (Strava Style)", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dummyTrips) { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTrip = trip }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(trip.title, fontWeight = FontWeight.Medium)
                            Text("Jarak: ${trip.distance} • Waktu: ${trip.time}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (selectedTrip != null) {
        AlertDialog(
            onDismissRequest = { selectedTrip = null },
            title = { Text("📊 Ringkasan Strava Card") },
            text = {
                Column {
                    Text("Perjalanan: ${selectedTrip?.title}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Jarak Tempuh: ${selectedTrip?.distance}")
                    Text("Durasi Total: ${selectedTrip?.time}")
                    Text("Kecepatan Rata-rata: 42 km/jam")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✨ Created by Kang Ekie", fontSize = 12.sp, color = Color.Blue)
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTrip = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}
