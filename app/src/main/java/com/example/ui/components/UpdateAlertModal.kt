package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.update.AppUpdateInfo
import com.example.data.update.AppUpdateManager
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun UpdateAlertModal(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Instalador listo para descargar") }

    Dialog(onDismissRequest = { if (!isDownloading) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚀 ¡Nueva Versión Disponible!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen,
                        fontSize = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Actual: ${updateInfo.currentVersion}",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = " ➔ ",
                        color = SpotifyGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        color = SpotifyGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Nueva: ${updateInfo.latestVersion}",
                            color = SpotifyGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Release Notes Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Novedades y mejoras:",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = updateInfo.releaseNotes.ifBlank { "Mejoras generales de rendimiento, estabilidad y nuevas funciones." },
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isDownloading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            color = SpotifyGreen,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${(downloadProgress * 100).toInt()}% - $statusText",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Más tarde", color = TextMuted)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                isDownloading = true
                                statusText = "Descargando APK de actualización..."
                                coroutineScope.launch {
                                    val success = AppUpdateManager.downloadAndInstallApk(
                                        context = context,
                                        downloadUrl = updateInfo.downloadUrl,
                                        onProgress = { p -> downloadProgress = p }
                                    )
                                    if (success) {
                                        statusText = "Abriendo instalador oficial..."
                                    } else {
                                        isDownloading = false
                                        statusText = "Error en descarga"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.4f)
                        ) {
                            Text(
                                text = "Actualizar Ahora",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
