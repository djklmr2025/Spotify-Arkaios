package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.data.model.UserListeningStatus
import com.example.data.model.VotedTrack
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityVotingModal(
    isOpen: Boolean,
    onClose: () -> Unit,
    votedTracks: List<VotedTrack>,
    userListeningStatuses: List<UserListeningStatus>,
    onVoteTrack: (String) -> Unit,
    onPlayTrack: (Track) -> Unit,
    onProposeNewSong: (String, String) -> Unit
) {
    if (!isOpen) return

    var selectedTab by remember { mutableStateOf(0) }
    var showProposeDialog by remember { mutableStateOf(false) }
    var proposeTitle by remember { mutableStateOf("") }
    var proposeArtist by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = SurfaceDark,
        scrimColor = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.HowToVote,
                        contentDescription = "Votación",
                        tint = ArkaiosGold,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = "Comunidad Arkaios: Ranking & Votos",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Vota por tus canciones o melodías favoritas (100GB Nube)",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs (Top Votadas / Escuchando Ahora)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) CyanPrimary else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔥 Ranking Votadas (${votedTracks.size})",
                        color = if (selectedTab == 0) BackgroundDark else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) CyanPrimary else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎧 Escuchando Ahora (${userListeningStatuses.size})",
                        color = if (selectedTab == 1) BackgroundDark else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                // Button Propose Song
                Button(
                    onClick = { showProposeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ArkaiosGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = BackgroundDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Proponer Canción o Melodía Propia para Votar", color = BackgroundDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of Voted Tracks
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(votedTracks.sortedByDescending { it.votesCount }) { index, item ->
                        val rank = index + 1
                        val rankBadgeColor = when (rank) {
                            1 -> ArkaiosGold
                            2 -> CyanLight
                            3 -> Color(0xFFCD7F32)
                            else -> BorderSubtle
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, if (item.userHasVoted) ArkaiosGold else BorderSubtle, RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rank Badge
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(rankBadgeColor.copy(alpha = 0.2f))
                                        .border(1.dp, rankBadgeColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#$rank",
                                        color = if (rank <= 3) rankBadgeColor else TextSecondary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Cover Image
                                AsyncImage(
                                    model = item.coverUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = item.title,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (item.isOriginalMelody) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(CyanLight.copy(alpha = 0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("Melodía Propia", color = CyanLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${item.artist} • ${item.uploadedBy}",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Play Button
                                IconButton(
                                    onClick = {
                                        onPlayTrack(
                                            Track(
                                                id = item.id,
                                                title = item.title,
                                                artist = item.artist,
                                                album = "Votación Comunidad Arkaios",
                                                durationMs = 180000L,
                                                audioUrl = item.audioUrl,
                                                coverUrl = item.coverUrl,
                                                genre = "Comunidad 100GB"
                                            )
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", tint = CyanLight)
                                }

                                // Vote Button
                                Button(
                                    onClick = { onVoteTrack(item.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (item.userHasVoted) ArkaiosGold else SurfaceDark
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .border(1.dp, if (item.userHasVoted) ArkaiosGold else BorderSubtle, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ThumbUp,
                                        contentDescription = "Votar",
                                        tint = if (item.userHasVoted) BackgroundDark else ArkaiosGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${item.votesCount}",
                                        color = if (item.userHasVoted) BackgroundDark else TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // List of User Listening Statuses
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(userListeningStatuses) { _, status ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // User Avatar
                                AsyncImage(
                                    model = status.avatarUrl,
                                    contentDescription = status.username,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, CyanLight, CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = status.username,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EmeraldLight.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(status.timestampText, color = EmeraldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "🎵 ${status.songTitle} - ${status.artistName}",
                                        color = CyanLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = CyanLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Propose Song Dialog
    if (showProposeDialog) {
        AlertDialog(
            onDismissRequest = { showProposeDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Proponer Canción o Melodía Propia", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Agrega el título de tu canción o composición musical para que la comunidad empiece a votar por ella.", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = proposeTitle,
                        onValueChange = { proposeTitle = it },
                        label = { Text("Título de la Canción o Melodía") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            unfocusedBorderColor = BorderSubtle,
                            focusedLabelColor = CyanLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = proposeArtist,
                        onValueChange = { proposeArtist = it },
                        label = { Text("Artista o Tu Nombre de Creador") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            unfocusedBorderColor = BorderSubtle,
                            focusedLabelColor = CyanLight
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (proposeTitle.isNotBlank()) {
                            onProposeNewSong(proposeTitle, proposeArtist.ifEmpty { "Creador Arkaios" })
                            proposeTitle = ""
                            proposeArtist = ""
                            showProposeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArkaiosGold)
                ) {
                    Text("Enviar a Votación", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProposeDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}
