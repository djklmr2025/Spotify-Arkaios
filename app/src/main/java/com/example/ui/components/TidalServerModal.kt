package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.data.tidal.AudioServerProvider
import com.example.data.tidal.TidalAudioQuality
import com.example.data.tidal.TidalConfig
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderSubtleCyan
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TidalServerModal(
    config: TidalConfig,
    connectionLog: String,
    tidalSearchResults: List<Track>,
    isSearching: Boolean,
    onUpdateToken: (String) -> Unit,
    onUpdateQuality: (TidalAudioQuality) -> Unit,
    onUpdateProvider: (AudioServerProvider) -> Unit,
    onSearchTidal: (String) -> Unit,
    onPlayTrack: (Track) -> Unit,
    onDownloadTrack: (Track) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Servidor y Token, 1: Búsqueda TIDAL HiFi, 2: Documentación y Servidores
    var tokenInput by remember { mutableStateOf(config.clientToken) }
    var searchInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDA08080C))
            .padding(top = 26.dp, start = 12.dp, end = 12.dp, bottom = 12.dp)
            .testTag("tidal_server_modal"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .shadow(24.dp, shape = RoundedCornerShape(24.dp), spotColor = CyanPrimary)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, BorderSubtleCyan, RoundedCornerShape(24.dp)),
            color = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x2606B6D4))
                                .border(1.dp, BorderSubtleCyan, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "TIDAL",
                                tint = CyanLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "TIDAL Hi-Fi & Servidores Cloud",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x2610B981))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("En Vivo", color = EmeraldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "Token Activo: ${config.clientToken} • ${config.audioQuality.displayName}",
                                color = CyanLight,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tabs Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf("Servidor & Token", "Buscador TIDAL", "Docs & Servidores")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanPrimary else SurfaceCard)
                                .border(1.dp, if (isSelected) CyanLight else BorderSubtle, RoundedCornerShape(10.dp))
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFF08080C) else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    0 -> ServerAndTokenTab(
                        config = config,
                        tokenInput = tokenInput,
                        connectionLog = connectionLog,
                        onTokenInputChange = { tokenInput = it },
                        onApplyToken = { onUpdateToken(tokenInput) },
                        onSelectQuality = onUpdateQuality,
                        onSelectProvider = onUpdateProvider
                    )
                    1 -> TidalSearchTab(
                        searchInput = searchInput,
                        isSearching = isSearching,
                        results = tidalSearchResults,
                        onSearchInputChange = { searchInput = it },
                        onExecuteSearch = { onSearchTidal(searchInput) },
                        onPlay = onPlayTrack,
                        onDownload = onDownloadTrack
                    )
                    2 -> DocsAndAlternativeServersTab(
                        onOpenUrl = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ServerAndTokenTab(
    config: TidalConfig,
    tokenInput: String,
    connectionLog: String,
    onTokenInputChange: (String) -> Unit,
    onApplyToken: () -> Unit,
    onSelectQuality: (TidalAudioQuality) -> Unit,
    onSelectProvider: (AudioServerProvider) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Status & Log
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Conexión a Servidor de Streaming",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = connectionLog,
                            color = CyanLight,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Token Input Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                        Text(
                            text = "TIDAL Client Token (API Key / Auth Header)",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = onTokenInputChange,
                        placeholder = { Text("Ej: kgsOOmYk3zShYrNP", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tidal_token_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Token por defecto: kgsOOmYk3zShYrNP",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Button(
                            onClick = onApplyToken,
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guardar Token", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quality Selectors
        item {
            Text(
                text = "Calidad de Audio de Streaming:",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TidalAudioQuality.values().forEach { quality ->
                    val isSelected = config.audioQuality == quality
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x1F06B6D4) else SurfaceCard)
                            .border(1.dp, if (isSelected) CyanLight else BorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { onSelectQuality(quality) }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = quality.displayName,
                                    color = if (isSelected) CyanLight else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Bit depth: ${quality.bitDepth} • Code: ${quality.code}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Available Audio Server Engine Providers
        item {
            Text(
                text = "Proveedor de Servidor de Música:",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AudioServerProvider.values().forEach { provider ->
                    val isSelected = config.selectedProvider == provider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x1F06B6D4) else SurfaceCard)
                            .border(1.dp, if (isSelected) CyanLight else BorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { onSelectProvider(provider) }
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = provider.displayName,
                                    color = if (isSelected) CyanLight else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = provider.description,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TidalSearchTab(
    searchInput: String,
    isSearching: Boolean,
    results: List<Track>,
    onSearchInputChange: (String) -> Unit,
    onExecuteSearch: () -> Unit,
    onPlay: (Track) -> Unit,
    onDownload: (Track) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = onSearchInputChange,
                placeholder = { Text("Buscar canción en servidores TIDAL...", color = TextMuted, fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tidal_search_input"),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = CyanLight, modifier = Modifier.size(18.dp))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanLight,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard
                ),
                singleLine = true
            )

            Button(
                onClick = onExecuteSearch,
                enabled = searchInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = Color(0xFF08080C),
                    disabledContainerColor = Color(0x2606B6D4),
                    disabledContentColor = TextMuted
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Buscar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Results
        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                    Text("Catálogo TIDAL HiFi", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Escribe el nombre de un artista o canción para buscar y escuchar streams directos.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results, key = { it.id }) { track ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceDark)
                            ) {
                                AsyncImage(
                                    model = track.coverUrl,
                                    contentDescription = track.title,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${track.artist} • ${track.bitrate}",
                                    color = CyanLight,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { onPlay(track) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Reproducir", tint = CyanLight)
                                }

                                IconButton(
                                    onClick = { onDownload(track) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Descargar", tint = EmeraldLight)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocsAndAlternativeServersTab(onOpenUrl: (String) -> Unit) {
    val links = listOf(
        Pair("TIDAL Developer OAuth Docs", "https://developer.tidal.com/documentation/api-sdk/api-sdk-authorization"),
        Pair("Navidrome Music Server", "https://www.navidrome.org/"),
        Pair("Ampache Music Server", "https://ampache.org/"),
        Pair("Koel Audio Platform", "https://koel.dev/"),
        Pair("SwingMusic Cloud Server", "https://swingmx.com/"),
        Pair("Spotube Multiplatform Client", "https://spotube.cc/")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Arquitectura y Documentación Oficial:",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "TIDAL utiliza OAuth 2.1 con Proof Key for Code Exchange (PKCE). Para clientes de streaming sin sesión de usuario, el token cliente nativo 'kgsOOmYk3zShYrNP' permite acceder a metadatos, búsqueda de catálogo y resolución de streams de audio.",
                color = TextMuted,
                fontSize = 11.sp
            )
        }

        items(links) { (title, url) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .clickable { onOpenUrl(url) }
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = url, color = CyanLight, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
