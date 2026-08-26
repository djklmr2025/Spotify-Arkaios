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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AmrTransaction
import com.example.data.model.AmrTxType
import com.example.data.model.AmrWallet
import com.example.data.model.ArkaiosPremiumTier
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.BorderSubtleCyan
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ErrorColor
import com.example.ui.theme.SuccessColor
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TidalCyan

@Composable
fun AmrStoreScreen(
    wallet: AmrWallet,
    premiumTiers: List<ArkaiosPremiumTier>,
    transactions: List<AmrTransaction>,
    onOpenCheckout: (ArkaiosPremiumTier) -> Unit,
    onClaimListeningReward: () -> Unit,
    onTransferTokens: (String, Double) -> Unit
) {
    var transferToAddress by remember { mutableStateOf("") }
    var transferAmountStr by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("amr_store_screen"),
        contentPadding = PaddingValues(bottom = 120.dp, top = 12.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "ARKAIOS AMR & Tienda",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Moneda del Ecosistema • Licencias y Pasarela Arkaios Pay",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Widget Cartera Real AMR (Matching `amr-wallet.js` structure)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x2606B6D4))
                                    .border(1.dp, BorderSubtleCyan, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🪙", fontSize = 18.sp)
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Cartera AMR Token",
                                        color = CyanLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x2606B6D4))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Red Activa", color = CyanLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = wallet.address,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${"%.2f".format(wallet.balance)} AMR",
                                color = CyanLight,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = wallet.network,
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Reward Claim Button (+25 AMR)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x1F10B981))
                            .clickable { onClaimListeningReward() }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Recompensa por Aporte de Red",
                                    color = EmeraldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Reclama bonificación de tokens por streaming",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldAccent)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("+25.00 AMR", color = Color(0xFF042F2E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Arkaios Pay Checkout Licencias & Membresías
        item {
            Text(
                text = "Membresías & Licencias (Pasarela ARKAIOS Pay)",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Premium Tiers List
        items(premiumTiers) { tier ->
            val isPurchased = (tier.isGodOwnerTier && wallet.isGodOwnerLicensed) ||
                    (tier.id == "tidal_master_hifi" && wallet.isTidalHiFiUnlocked)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (tier.isGodOwnerTier) 14.dp else 4.dp, shape = RoundedCornerShape(18.dp), spotColor = CyanPrimary)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (tier.isGodOwnerTier) SurfaceDark else SurfaceCard)
                    .border(
                        1.dp,
                        if (tier.isGodOwnerTier) BorderSubtleCyan else BorderSubtle,
                        RoundedCornerShape(18.dp)
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Badge & Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (tier.isGodOwnerTier) Color(0x3306B6D4) else Color(0x333B82F6))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tier.badge,
                                color = if (tier.isGodOwnerTier) CyanLight else BlueAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Text(
                            text = "${"%.2f".format(tier.priceAmr)} AMR",
                            color = CyanLight,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Text(
                        text = tier.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = tier.description,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // Features Bullets
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        tier.features.forEach { feat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = feat,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Arkaios Pay 1-Click Button (matches `data-arkaios-pay`)
                    if (isPurchased) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x3310B981))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                                Text("✔ Licencia Activa en tu Cartera", color = EmeraldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = { onOpenCheckout(tier) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("arkaios_pay_button_${tier.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tier.isGodOwnerTier) CyanPrimary else BlueAccent,
                                contentColor = Color(0xFF08080C)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡ Pagar con ARKAIOS Pay (1-Clic)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Section: Transferir AMR Tokens
        item {
            Text(
                text = "Transferir Tokens AMR",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = transferToAddress,
                        onValueChange = { transferToAddress = it },
                        label = { Text("Dirección destino (ej. amr_peer_01)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            focusedLabelColor = CyanLight
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = transferAmountStr,
                        onValueChange = { transferAmountStr = it },
                        label = { Text("Monto a enviar en AMR") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanLight,
                            focusedLabelColor = CyanLight
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val amt = transferAmountStr.toDoubleOrNull() ?: 0.0
                            if (transferToAddress.isNotBlank() && amt > 0) {
                                onTransferTokens(transferToAddress, amt)
                                transferToAddress = ""
                                transferAmountStr = ""
                            }
                        },
                        enabled = transferToAddress.isNotBlank() && (transferAmountStr.toDoubleOrNull() ?: 0.0) > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color(0xFF08080C)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ejecutar Transferencia AMR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Section: Libro Mayor de Transacciones
        item {
            Text(
                text = "Libro Mayor de Transacciones (Ledger)",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(transactions) { tx ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.concept,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "TX: ${tx.txHash.take(20)}...",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        val isPositive = tx.type == AmrTxType.REWARD_MINT || tx.type == AmrTxType.TRANSFER_IN
                        Text(
                            text = "${if (isPositive) "+" else "-"}${"%.2f".format(tx.amount)} AMR",
                            color = if (isPositive) SuccessColor else CyanLight,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x2210B981))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(tx.status, color = SuccessColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

