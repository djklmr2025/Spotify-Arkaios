package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.UserProfile
import com.example.data.model.UserTier
import com.example.ui.theme.ArkaiosGold
import com.example.ui.theme.ArkaiosGoldDark
import com.example.ui.theme.ArkaiosGoldLight
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderSubtleCyan
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ArkaiosAuthModal(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onLoginGoogle: () -> Unit,
    onLoginEmail: (String) -> Unit,
    onToggleOfflineCache: (Boolean) -> Unit,
    onUpgradeTier: (UserTier) -> Unit
) {
    var emailInput by remember { mutableStateOf(userProfile.email) }
    var isSigningIn by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = ArkaiosGoldLight, modifier = Modifier.size(24.dp))
                    Text("Cuenta Arkaios & Google", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // User Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1E1E2E), Color(0xFF13131F))
                            )
                        )
                        .border(1.dp, BorderSubtleCyan, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = userProfile.avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .border(2.dp, ArkaiosGoldLight, CircleShape)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.displayName,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = userProfile.email,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x33F59E0B))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (userProfile.tier == UserTier.FREE) "FREE TIER" else "ARKAIOS PREMIUM HIFI",
                                        color = ArkaiosGoldLight,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = "🪙 ${userProfile.treasureBalanceAmr.toInt()} AMR",
                                    color = CyanLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Google & Arkaios Sign-in Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            isSigningIn = true
                            onLoginGoogle()
                            isSigningIn = false
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("G", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF4285F4))
                            Text("Conectar con Google Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            placeholder = { Text("correo@arkaios.com", fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanLight,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (emailInput.isNotBlank()) {
                                    onLoginEmail(emailInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Login", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Arkaios Vault Offline DRM Cache Feature Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, if (userProfile.offlineEncryptedCacheEnabled) BorderSubtleCyan else BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(
                                        text = "Caché Offline Cifrado (.arkcache)",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Descargas protegidas solo reproducibles por la app",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Switch(
                                checked = userProfile.offlineEncryptedCacheEnabled,
                                onCheckedChange = { onToggleOfflineCache(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmeraldLight,
                                    checkedTrackColor = Color(0x3310B981)
                                )
                            )
                        }

                        Text(
                            text = "✅ Las pistas y álbumes descargados se almacenan bajo el formato contenedor `.arkcache` con cifrado AES-128 propietario. Ningún otro reproductor del teléfono podrá reproducir el archivo crudo.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                // Tier Upgrade / Active Membership Status
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Membresías Arkaios Premium:",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Premium Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (userProfile.tier == UserTier.ARKAIOS_PREMIUM_HIFI) Color(0x26F59E0B) else SurfaceElevated)
                            .border(1.dp, if (userProfile.tier == UserTier.ARKAIOS_PREMIUM_HIFI) ArkaiosGoldLight else BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { onUpgradeTier(UserTier.ARKAIOS_PREMIUM_HIFI) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Arkaios Hi-Fi Master", color = ArkaiosGoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Descarga de álbumes completos + FLAC 24-Bit + DRM Cifrado", color = TextSecondary, fontSize = 10.sp)
                        }
                        if (userProfile.tier == UserTier.ARKAIOS_PREMIUM_HIFI) {
                            Icon(Icons.Default.Check, contentDescription = "Activo", tint = EmeraldLight, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Lifetime VIP Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (userProfile.tier == UserTier.TREASURE_VIP_LIFETIME) Color(0x2606B6D4) else SurfaceElevated)
                            .border(1.dp, if (userProfile.tier == UserTier.TREASURE_VIP_LIFETIME) CyanLight else BorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { onUpgradeTier(UserTier.TREASURE_VIP_LIFETIME) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Treasure VIP Lifetime Pass", color = CyanLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Acceso ilimitado de por vida con balance AMR sincronizado", color = TextSecondary, fontSize = 10.sp)
                        }
                        if (userProfile.tier == UserTier.TREASURE_VIP_LIFETIME) {
                            Icon(Icons.Default.Check, contentDescription = "Activo", tint = EmeraldLight, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C))
            ) {
                Text("Aceptar", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceDark
    )
}
