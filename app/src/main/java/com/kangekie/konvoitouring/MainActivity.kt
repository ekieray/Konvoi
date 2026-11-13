package com.kangekie.konvoitouring

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Lokasi akurat diizinkan
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Lokasi kasar diizinkan
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Meminta izin lokasi secara otomatis saat aplikasi dibuka
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
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Manajemen Room Konvoi", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { /* Buat Room */ }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buat Room Baru")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { /* Gabung Room */ }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gabung via Kode Room")
        }
    }
}

@Composable
fun LiveGpsTrackingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Memuat Peta Live GPS...", fontWeight = FontWeight.SemiBold)
            Text("Melacak posisi anggota konvoi & avatar aktif", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TripHistoryScreen() {
    val dummyTrips = listOf(
        "Touring Bandung - Lembang (120 km) • Strava Card",
        "Ride Santai Bogor - Puncak (75 km) • Strava Card",
        "Night Ride Sentul City (45 km) • Strava Card"
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Riwayat Perjalanan (Trip History)", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dummyTrips) { trip ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(trip, fontWeight = FontWeight.Medium)
                            Text("Ketuk untuk lihat Ringkasan Strava", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
