package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GenreCategory
import com.example.data.model.Track
import com.example.data.repository.DownloadProgress
import com.example.player.PlaybackState
import com.example.ui.components.TrackItemRow
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderSubtleCyan
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    searchQuery: String,
    selectedGenre: String?,
    searchResults: List<Track>,
    genres: List<GenreCategory>,
    playbackState: PlaybackState,
    downloadStatus: Map<String, DownloadProgress>,
    isSearching: Boolean = false,
    onQueryChange: (String) -> Unit,
    onGenreSelect: (String?) -> Unit,
    onTrackClick: (Track) -> Unit,
    onFavoriteToggle: (Track) -> Unit,
    onDownloadClick: (Track) -> Unit,
    onRemoveDownloadClick: (Track) -> Unit,
    onOpenDownloaderSheet: () -> Unit = {},
    onOpenTidalModal: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val quickSuggestions = listOf("Tiësto", "David Guetta", "Martin Garrix", "Bad Bunny", "Synthwave", "Hi-Fi Master")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
            .testTag("search_screen")
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("search_input_field"),
            placeholder = { Text("Buscar en TIDAL, Deezer, Apple & Local...", color = TextMuted, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = CyanLight
                )
            },
            trailingIcon = {
                if (isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = CyanLight,
                        strokeWidth = 2.dp
                    )
                } else if (searchQuery.isNotEmpty() || selectedGenre != null) {
                    IconButton(onClick = {
                        onQueryChange("")
                        onGenreSelect(null)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = TextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = CyanLight,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
        )

        // Quick Artist Suggestions Pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickSuggestions) { item ->
                val isSelected = searchQuery.equals(item, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) CyanGlow else SurfaceDark)
                        .border(1.dp, if (isSelected) CyanLight else BorderSubtle, RoundedCornerShape(16.dp))
                        .clickable {
                            onQueryChange(item)
                            focusManager.clearFocus()
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item,
                        color = if (isSelected) CyanLight else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Quick Integration Banners: TIDAL Hi-Fi + Background Downloader
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // TIDAL Direct Stream Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(14.dp))
                    .clickable { onOpenTidalModal() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "TIDAL Hi-Fi",
                        tint = CyanLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "TIDAL Hi-Fi API",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Token: kgsOOmYk3zShYrNP",
                            color = CyanLight,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Background Downloader Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .clickable { onOpenDownloaderSheet() }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descargas en 2do plano",
                        tint = CyanLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Descargas 2do Plano",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "yt-dlp • SpotiDown",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Active Genre Filter Chip
        if (selectedGenre != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x2606B6D4))
                        .border(1.dp, BorderSubtleCyan, RoundedCornerShape(20.dp))
                        .clickable { onGenreSelect(null) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Género: $selectedGenre",
                            color = CyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Filter",
                            tint = CyanLight,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // If searching or filtering: Show Results. Otherwise: Show Explore Cards.
        if (searchQuery.isNotBlank() || selectedGenre != null) {
            Text(
                text = "Resultados (${searchResults.size})",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp, start = 8.dp, end = 8.dp)
            ) {
                if (searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔍", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No se encontraron canciones",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Intenta buscar por artista, álbum o título diferente.",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                } else {
                    items(searchResults) { track ->
                        val isCurrent = playbackState.currentTrack?.id == track.id
                        val isPlaying = isCurrent && playbackState.isPlaying
                        val progress = downloadStatus[track.id]

                        TrackItemRow(
                            track = track,
                            isCurrentTrack = isCurrent,
                            isPlaying = isPlaying,
                            downloadProgress = progress,
                            onTrackClick = { onTrackClick(track) },
                            onFavoriteToggle = { onFavoriteToggle(track) },
                            onDownloadClick = { onDownloadClick(track) },
                            onRemoveDownloadClick = { onRemoveDownloadClick(track) }
                        )
                    }
                }
            }
        } else {
            // Explore all categories grid
            Text(
                text = "Explorar todo el catálogo",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(genres) { genre ->
                    Box(
                        modifier = Modifier
                            .height(96.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(genre.gradientStartHex), Color(genre.gradientEndHex))
                                )
                            )
                            .clickable { onGenreSelect(genre.name) }
                            .padding(14.dp)
                    ) {
                        Text(
                            text = genre.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.TopStart)
                        )
                    }
                }
            }
        }
    }
}
