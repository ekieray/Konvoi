package com.konvoiku.touring.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.konvoiku.touring.model.VehicleProfile
import com.konvoiku.touring.ui.components.ConvoyAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTourScreen(
    joinInput: String,
    joinError: String?,
    extractedRoomId: String,
    vehicleProfile: VehicleProfile,
    onInputChange: (String) -> Unit,
    onJoinSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gabung Room Konvoi",
                        style = MaterialTheme.typography.titleMedium
                    )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Masukkan Room ID atau tempel link undangan yang dibagikan oleh Tour Leader.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Input kolom Room ID / Link
            OutlinedTextField(
                value = joinInput,
                onValueChange = onInputChange,
                label = { Text("Room ID atau Link Undangan") },
                placeholder = { Text("KNV-8492 atau https://konvoiku.id/join/KNV-8492") },
                leadingIcon = {
                    Icon(Icons.Rounded.Link, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) {
                                onInputChange(clip)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentPaste,
                            contentDescription = "Tempel dari Clipboard"
                        )
                    }
                },
                isError = joinError != null,
                supportingText = if (joinError != null) {
                    { Text(joinError) }
                } else if (extractedRoomId.length >= 4) {
                    { Text("Terdeteksi Room ID: $extractedRoomId") }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onJoinSubmit() }),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Quick demo sample helper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Contoh cepat:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { onInputChange("https://konvoiku.id/join/KNV-8492") }
                ) {
                    Text(
                        text = "KNV-8492",
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Ringkasan Profil Pengendara
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ConvoyAvatar(
                        preset = vehicleProfile.avatarPreset,
                        photoUri = vehicleProfile.avatarPhotoUri,
                        displayName = vehicleProfile.fullName.ifBlank { "Anggota" },
                        size = 46.dp,
                        borderColor = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Profil Terdaftar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = vehicleProfile.fullName.ifBlank { "Anggota Touring" },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${vehicleProfile.vehicleType.label} · ${vehicleProfile.licensePlate.ifBlank { "-" }} · Darurat: ${vehicleProfile.emergencyContact.ifBlank { "-" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onJoinSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Login,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Masuk Konvoi",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
