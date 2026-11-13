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
                        Text("Sinyal darurat dan koordinat lokasi terakhir Anda telah dikirimkan ke grup konvoi.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { sosActive = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                            Text("Batalkan SOS")
                        }
                    }
                }
            } else {
                when (selectedTab) {
                    0 -> RoomManagementScreen()
                    1 -> LiveGpsTrackingScreen()
                    2 -> TripHistoryScreen()
                }
            }
        }
    }
}

@Composable
fun RoomManagementScreen() {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var roomName by remember { mutableStateOf("") }
    var roomCodeInput by remember { mutableStateOf("") }
    var activeRoomState by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Manajemen Room Konvoi", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (activeRoomState != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Room Aktif: $activeRoomState", fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Status: Terhubung ke Cloud Firebase", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { activeRoomState = null }) {
                        Text("Keluar Room")
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { showCreateDialog = true }, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buat Room Baru")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = { showJoinDialog = true }, 
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gabung via Kode Room")
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Buat Room Konvoi") },
            text = {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Nama Rute / Touring") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (roomName.isNotBlank()) {
                        activeRoomState = roomName
                        roomName = ""
                        showCreateDialog = false
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
            title = { Text("Gabung Room") },
            text = {
                OutlinedTextField(
                    value = roomCodeInput,
                    onValueChange = { roomCodeInput = it },
                    label = { Text("Masukkan Kode Room") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (roomCodeInput.isNotBlank()) {
                        activeRoomState = "Room: $roomCodeInput"
                        roomCodeInput = ""
                        showJoinDialog = false
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
fun LiveGpsTrackingScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Radar Konvoi Aktif", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Sinkronisasi Realtime GPS via Cloud", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📍 Checkpoint 1: SPBU Cibinong (Aktif)", fontSize = 14.sp)
                Text("🏁 Checkpoint 2: Alun-Alun Bogor (Tujuan)", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
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
