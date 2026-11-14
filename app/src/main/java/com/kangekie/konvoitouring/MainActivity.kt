package com.kangekie.konvoitouring

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
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
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    private lateinit var sharedPreferences: SharedPreferences

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
        
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        sharedPreferences = getSharedPreferences("KonvoiPrefs", Context.MODE_PRIVATE)
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
                    KonvoiApp(sharedPreferences)
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

data class TripItem(val title: String, val distance: String, val time: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KonvoiApp(prefs: SharedPreferences) {
    var selectedTab by remember { mutableStateOf(0) }
    var sosActive by remember { mutableStateOf(false) }
    
    var activeRoomCode by remember { 
        mutableStateOf(prefs.getString("ACTIVE_ROOM", null)) 
    }

    // Inisialisasi Database Region Amerika (.firebaseio.com)
    val database = remember {
        try {
            Firebase.database("https://konvoitouring-default-rtdb.firebaseio.com").reference
        } catch (e: Exception) {
            null
        }
    }

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
                onClick = { 
                    sosActive = !sosActive
                    if (activeRoomCode != null && database != null) {
                        try {
                            database.child("rooms").child(activeRoomCode!!).child("sos").setValue(sosActive)
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                },
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
                        Text("Sinyal darurat disiarkan secara real-time ke Cloud Firebase.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { 
                                sosActive = false
                                if (activeRoomCode != null && database != null) {
                                    try {
                                        database.child("rooms").child(activeRoomCode!!).child("sos").setValue(false)
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            }, 
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Batalkan SOS")
                        }
                    }
                }
            } else {
                when (selectedTab) {
                    0 -> CloudRoomScreen(
                        activeRoom = activeRoomCode, 
                        onRoomChange = { newRoom ->
                            activeRoomCode = newRoom
                            prefs.edit().putString("ACTIVE_ROOM", newRoom).apply()
                        }
                    )
                    1 -> CloudLiveGpsScreen(activeRoom = activeRoomCode)
                    2 -> TripHistoryScreen()
                }
            }
        }
    }
}

@Composable
fun CloudRoomScreen(activeRoom: String?, onRoomChange: (String?) -> Unit) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var roomNameInput by remember { mutableStateOf("") }
    var roomCodeInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    val database = remember {
        try {
            Firebase.database("https://konvoitouring-default-rtdb.firebaseio.com").reference
        } catch (e: Exception) {
            null
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text
