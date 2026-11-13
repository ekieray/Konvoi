package com.konvoiku.touring.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContactPhone
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konvoiku.touring.model.AvatarPreset
import com.konvoiku.touring.model.UserRole
import com.konvoiku.touring.model.VehicleProfile
import com.konvoiku.touring.model.VehicleType
import com.konvoiku.touring.ui.components.ConvoyAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleProfileScreen(
    selectedRole: UserRole,
    profile: VehicleProfile,
    onUpdateProfile: (
        fullName: String?,
        vehicleType: VehicleType?,
        vehicleDetail: String?,
        licensePlate: String?,
        emergencyContact: String?,
        avatarPreset: AvatarPreset?,
        avatarPhotoUri: String?,
        clearPhoto: Boolean
    ) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onOpenAboutApp: () -> Unit
) {
    var showValidationErrors by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdateProfile(null, null, null, null, null, null, uri.toString(), false)
        }
    }

    val nameError = showValidationErrors && profile.fullName.trim().length < 2
    val plateError = showValidationErrors && profile.licensePlate.trim().length < 3
    val contactError = showValidationErrors && profile.emergencyContact.trim().length < 8

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Profil Kendaraan",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Peran: ${selectedRole.label}",
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
                    IconButton(onClick = onOpenAboutApp) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Tentang Aplikasi",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Button(
                    onClick = {
                        if (profile.isValid) {
                            onContinue()
                        } else {
                            showValidationErrors = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) {
                    Text(
                        text = if (selectedRole == UserRole.TOUR_LEADER) {
                            "Lanjut Setup Konvoi"
                        } else {
                            "Lanjut Gabung Konvoi"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Lengkapi foto profil/avatar dan identitas kendaraan agar Anda mudah dikenali di peta, daftar peserta, dan chat konvoi.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 0. Foto Profil & Pilihan Avatar Bawaan
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ConvoyAvatar(
                            preset = profile.avatarPreset,
                            photoUri = profile.avatarPhotoUri,
                            displayName = profile.fullName.ifBlank { "Rider" },
                            size = 64.dp,
                            borderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 2.5.dp
                        )
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Foto Profil / Avatar Konvoi",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pilih Galeri", fontSize = 12.sp)
                                }
                                if (!profile.avatarPhotoUri.isNullOrBlank()) {
                                    TextButton(
                                        onClick = {
                                            onUpdateProfile(null, null, null, null, null, null, null, true)
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Hapus Foto", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Hapus", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Atau pilih avatar karakter rider bawaan:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(AvatarPreset.entries) { preset ->
                            val isSelected = profile.avatarPhotoUri.isNullOrBlank() && profile.avatarPreset == preset
                            Surface(
                                onClick = {
                                    onUpdateProfile(null, null, null, null, null, preset, null, true)
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(82.dp)
                                        .padding(vertical = 10.dp, horizontal = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    ConvoyAvatar(
                                        preset = preset,
                                        photoUri = null,
                                        displayName = preset.label,
                                        size = 40.dp
                                    )
                                    Text(
                                        text = preset.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 1. Nama Lengkap
            OutlinedTextField(
                value = profile.fullName,
                onValueChange = { onUpdateProfile(it, null, null, null, null, null, null, false) },
                label = { Text("Nama Lengkap") },
                placeholder = { Text("Contoh: Budi Santoso") },
                leadingIcon = {
                    Icon(Icons.Rounded.Person, contentDescription = null)
                },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Nama lengkap minimal 2 karakter") }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // 2. Jenis Kendaraan (Mobil / Motor)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Jenis Kendaraan",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VehicleType.entries.forEachIndexed { index, type ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = VehicleType.entries.size
                            ),
                            selected = profile.vehicleType == type,
                            onClick = { onUpdateProfile(null, type, null, null, null, null, null, false) },
                            icon = {
                                Icon(
                                    imageVector = if (type == VehicleType.MOBIL) {
                                        Icons.Rounded.DirectionsCar
                                    } else {
                                        Icons.Rounded.TwoWheeler
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        ) {
                            Text(
                                text = type.label,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3. Model & Warna Kendaraan (untuk Label Marker Peta)
            OutlinedTextField(
                value = profile.vehicleDetail,
                onValueChange = { onUpdateProfile(null, null, it, null, null, null, null, false) },
                label = { Text("Model & Warna Kendaraan (Opsional)") },
                placeholder = {
                    Text(
                        if (profile.vehicleType == VehicleType.MOBIL) {
                            "Contoh: Avanza Hitam"
                        } else {
                            "Contoh: Nmax Putih"
                        }
                    )
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Palette, contentDescription = null)
                },
                supportingText = {
                    Text("Ditampilkan pada label marker peta, misal: Budi - Avanza Hitam.")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // 4. Nomor Plat Kendaraan
            OutlinedTextField(
                value = profile.licensePlate,
                onValueChange = { onUpdateProfile(null, null, null, it, null, null, null, false) },
                label = { Text("Nomor Plat Kendaraan") },
                placeholder = { Text("Contoh: B 1945 KNV") },
                leadingIcon = {
                    Icon(Icons.Rounded.Badge, contentDescription = null)
                },
                isError = plateError,
                supportingText = if (plateError) {
                    { Text("Masukkan nomor plat kendaraan yang valid") }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // 5. Kontak Darurat
            OutlinedTextField(
                value = profile.emergencyContact,
                onValueChange = { onUpdateProfile(null, null, null, null, it, null, null, false) },
                label = { Text("Kontak Darurat (No. Telepon / WA)") },
                placeholder = { Text("Contoh: 0812-3456-7890") },
                leadingIcon = {
                    Icon(Icons.Rounded.ContactPhone, contentDescription = null)
                },
                isError = contactError,
                supportingText = if (contactError) {
                    { Text("Masukkan nomor kontak darurat minimal 8 digit") }
                } else {
                    { Text("Digunakan jika terjadi kendala darurat saat konvoi.") }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // 6. Tentang Aplikasi & Kredit Pembuat
            Card(
                onClick = onOpenAboutApp,
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
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tentang Aplikasi KonvoiKu",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "App Created & Designed by Kang Ekie",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Buka Tentang Aplikasi",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
