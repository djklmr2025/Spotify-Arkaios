package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CreatorCloudStats
import com.example.data.model.CreatorTrack
import com.example.data.model.Track
import com.example.ui.theme.ArkaiosGold
import com.example.ui.theme.ArkaiosGoldLight
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderSubtleCyan
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CreatorStudioModal(
    stats: CreatorCloudStats,
    creatorTracks: List<CreatorTrack>,
    onUploadNewTrack: (title: String, artist: String, album: String, genre: String, fileMb: Double) -> Unit,
    onPlayTrack: (Track) -> Unit,
    onShareTrack: (CreatorTrack) -> Unit,
    onClaimEarnings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isUploadFormOpen by remember { mutableStateOf(false) }

    // Form inputs
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("Arkaios Sound Creator") }
    var album by remember { mutableStateOf("Single Independiente 2026") }
    var genre by remember { mutableStateOf("Electronic / Synthwave") }
    var fileSizeMb by remember { mutableStateOf("24.5") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6050508))
            .clickable { onDismiss() }
            .testTag("creator_studio_modal"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxSize(0.92f)
                .clickable(enabled = false) {}
                .shadow(24.dp, shape = RoundedCornerShape(24.dp), spotColor = CyanPrimary),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleCyan)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x26F59E0B))
                                .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Creator Studio",
                                tint = ArkaiosGoldLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Arkaios Creator Studio",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x3310B981))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("50 GB Nube Activa", color = EmeraldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "Alojado en Google Drive 5TB Master • Regalías AMR P2P",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Google Drive 5TB Storage Allocation Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0C192E), Color(0xFF0A101D))
                                    )
                                )
                                .border(1.dp, Color(0x4038BDF8), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Storage, contentDescription = null, tint = CyanLight, modifier = Modifier.size(18.dp))
                                        Text("Tu Cuota en Google Drive 5TB", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Button to open Master Drive in browser
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x2638BDF8))
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(stats.gdriveMasterFolderUrl))
                                                context.startActivity(intent)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = CyanLight, modifier = Modifier.size(13.dp))
                                        Text("Ver Nube 5TB", color = CyanLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Storage Bar
                                val progress = (stats.usedStorageGb / stats.allocatedStorageGb).toFloat().coerceIn(0f, 1f)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${"%.2f".format(stats.usedStorageGb)} GB usados",
                                            color = CyanLight,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "${"%.0f".format(stats.allocatedStorageGb)} GB asignados (5TB Pool)",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = CyanLight,
                                        trackColor = Color(0x3338BDF8)
                                    )
                                }

                                Text(
                                    text = "Carpeta Raíz Master: /Arkaios_Cloud_5TB/Creators/amr_arkaios2026/ (ID: ...${stats.gdriveMasterFolderId.takeLast(8)})",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Monetization & AMR Earnings Banner
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Streams Card
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = CyanLight, modifier = Modifier.size(15.dp))
                                        Text("Streams Globales", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Text(
                                        text = "%,d".format(stats.totalGlobalStreams),
                                        color = TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "+${stats.royaltyRatePerStreamAmr} AMR / play",
                                        color = EmeraldLight,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Earnings Card with Claim
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x1F10B981))
                                    .border(1.dp, Color(0x4010B981), RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(15.dp))
                                        Text("Ganancias AMR", color = EmeraldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "${"%.2f".format(stats.totalAmrEarned)} AMR",
                                        color = EmeraldLight,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(EmeraldAccent)
                                            .clickable { onClaimEarnings() }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("Reclamar a Cartera", color = Color(0xFF042F2E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Action: Upload or Add Track Form Toggle
                    item {
                        if (!isUploadFormOpen) {
                            Button(
                                onClick = { isUploadFormOpen = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Text("Subir Nueva Pista a Google Drive 5TB", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        } else {
                            // Upload Form Container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Subida Directa a Nube 5TB", color = CyanLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { isUploadFormOpen = false }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextSecondary)
                                        }
                                    }

                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = { title = it },
                                        label = { Text("Título de la Canción") },
                                        placeholder = { Text("Ej. Cyber Nebula 2026") },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyanLight,
                                            focusedLabelColor = CyanLight
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = artist,
                                            onValueChange = { artist = it },
                                            label = { Text("Artista / Creador") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanLight,
                                                focusedLabelColor = CyanLight
                                            ),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = genre,
                                            onValueChange = { genre = it },
                                            label = { Text("Género") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanLight,
                                                focusedLabelColor = CyanLight
                                            ),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = album,
                                            onValueChange = { album = it },
                                            label = { Text("Álbum / EP") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanLight,
                                                focusedLabelColor = CyanLight
                                            ),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = fileSizeMb,
                                            onValueChange = { fileSizeMb = it },
                                            label = { Text("Tamaño (MB)") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyanLight,
                                                focusedLabelColor = CyanLight
                                            ),
                                            modifier = Modifier.weight(0.6f),
                                            singleLine = true
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (title.isNotBlank()) {
                                                val mb = fileSizeMb.toDoubleOrNull() ?: 22.0
                                                onUploadNewTrack(title, artist, album, genre, mb)
                                                title = ""
                                                isUploadFormOpen = false
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Publicar y Transmitir a Google Drive 5TB", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Section Title: Uploaded Tracks
                    item {
                        Text(
                            text = "Tus Pistas Publicadas (${creatorTracks.size})",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Tracks List
                    if (creatorTracks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Aún no has subido canciones a tu nodo.", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    } else {
                        items(creatorTracks) { ct ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        AsyncImage(
                                            model = ct.coverUrl,
                                            contentDescription = ct.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceDark)
                                        )

                                        Column {
                                            Text(
                                                text = ct.title,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${ct.genre} • ${ct.fileSizeMb} MB • ${ct.audioFormat}",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "%,d plays".format(ct.streamsCount),
                                                    color = CyanLight,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Text(
                                                    text = "• +${"%.2f".format(ct.amrEarned)} AMR",
                                                    color = EmeraldLight,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { onPlayTrack(ct.toCatalogTrack()) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = CyanLight)
                                        }

                                        IconButton(
                                            onClick = { onShareTrack(ct) },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "Compartir", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
