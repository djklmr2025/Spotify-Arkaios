package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.TransactionEntity
import com.example.data.local.WalletEntity
import com.example.data.model.AmrTransaction
import com.example.data.model.AmrTxType
import com.example.data.model.AmrWallet
import com.example.data.model.ArkaiosPremiumTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AmrWalletRepository(private val db: AppDatabase) {

    private val walletDao = db.walletDao()

    private val _walletState = MutableStateFlow(
        AmrWallet(
            address = "amr_arkaios2026",
            balance = 0.00,
            currency = "AMR",
            network = "ARKAIOS-MAINNET-v1",
            userEmail = "arkaios2026@gmail.com",
            userName = "Arkaios Master",
            isGodOwnerLicensed = false,
            isTidalHiFiUnlocked = false
        )
    )
    val walletState: StateFlow<AmrWallet> = _walletState.asStateFlow()

    val transactionsFlow: Flow<List<AmrTransaction>> = walletDao.getAllTransactions().map { entities ->
        entities.map { it.toTransaction() }
    }

    suspend fun initializeWallet() = withContext(Dispatchers.IO) {
        val existing = walletDao.getWallet()
        if (existing != null) {
            // If existing wallet had the old demo balance (1000 or 1050), reset it cleanly to 0.00 AMR
            val adjusted = if (existing.balance == 1000.0 || existing.balance == 1050.0) {
                existing.copy(balance = 0.0)
            } else {
                existing
            }
            walletDao.saveWallet(adjusted)
            _walletState.value = adjusted.toWallet()
        } else {
            val initial = _walletState.value
            walletDao.saveWallet(WalletEntity.fromWallet(initial))
        }
    }

    suspend fun processPayPalPayment(
        amountUsd: Double,
        concept: String,
        isMembership: Boolean = false
    ): Result<AmrTransaction> = withContext(Dispatchers.IO) {
        val current = _walletState.value
        val orderId = "PAYPAL-ORD-" + System.currentTimeMillis().toString().takeLast(8)

        val isGod = if (concept.contains("God Owner", ignoreCase = true)) true else current.isGodOwnerLicensed
        val isTidal = if (concept.contains("Tidal", ignoreCase = true)) true else current.isTidalHiFiUnlocked

        // Tasa de Cambio Ecosistema AMR-IO: 1 AMR = 1 MXN ($1 Peso Mexicano)
        // Convert USD paid to MXN/AMR (aprox $1 USD = 20 MXN / 20 AMR, o monto directo asignado)
        val amrToAdd = if (!isMembership) {
            when {
                amountUsd >= 20.0 -> 500.0 // Pack 500 AMR ($500 MXN)
                amountUsd >= 4.0 -> 100.0   // Pack 100 AMR ($100 MXN)
                else -> 50.0                // Pack 50 AMR ($50 MXN)
            }
        } else 0.0

        val updatedWallet = current.copy(
            balance = current.balance + amrToAdd,
            isGodOwnerLicensed = isGod,
            isTidalHiFiUnlocked = isTidal
        )
        walletDao.saveWallet(WalletEntity.fromWallet(updatedWallet))
        _walletState.value = updatedWallet

        val txHash = generateTxHash("paypal_v6_gateway", current.address, amountUsd)
        val transaction = AmrTransaction(
            id = "tx_pp_" + System.currentTimeMillis(),
            type = if (isMembership) AmrTxType.PAYMENT else AmrTxType.REWARD_MINT,
            amount = if (isMembership) amountUsd * 20.0 else amrToAdd,
            concept = "$concept (PayPal v6 SDK • 1 AMR = 1 MXN • $orderId)",
            txHash = txHash,
            status = "CONFIRMED"
        )
        walletDao.insertTransaction(TransactionEntity.fromTransaction(transaction))
        Result.success(transaction)
    }

    fun getPremiumTiers(): List<ArkaiosPremiumTier> {
        return listOf(
            ArkaiosPremiumTier(
                id = "god_owner_licence",
                title = "Membresía Anual God Owner & Nube 5TB",
                description = "1 Año de Suscripción Anual. Acceso ilimitado a la Nube de 5TB en Google Drive, descargas FLAC 24-bit sin límites e inferencia IA musical.",
                priceAmr = 500.00, // 500 AMR = $500 MXN / año
                features = listOf(
                    "Suscripción Válida por 1 Año Completo",
                    "Nube ilimitada de 5TB en Google Drive",
                    "Descargas FLAC 24-bit ilimitadas",
                    "Inferencia IA Gemini 1.5 Pro musical",
                    "Insignia Dorada God Card de Verificación"
                ),
                badge = "⚡ MEMBRESÍA ANUAL 5TB GOD OWNER",
                isGodOwnerTier = true
            ),
            ArkaiosPremiumTier(
                id = "pro_creator_node",
                title = "Membresía Anual Creator Studio 50GB",
                description = "1 Año de Suscripción Anual. Sube tus pistas .mp3/.flac a Google Drive 50GB, comparte con la comunidad y gana regalías AMR-IO.",
                priceAmr = 300.00, // 300 AMR = $300 MXN / año
                features = listOf(
                    "Suscripción Válida por 1 Año Completo",
                    "50GB de espacio en Google Drive para creadores",
                    "Compartición directa de audio con usuarios",
                    "Regalías en AMR-IO por cada reproducción",
                    "Estadísticas de streaming en tiempo real"
                ),
                badge = "CREATOR STUDIO ANUAL 50GB"
            ),
            ArkaiosPremiumTier(
                id = "tidal_master_hifi",
                title = "Membresía Anual Tidal Master HiFi",
                description = "1 Año de Suscripción Anual. Transmisión FLAC Master MQA hasta 9216 kbps integrada con ecualizador paramétrico IA.",
                priceAmr = 200.00, // 200 AMR = $200 MXN / año
                features = listOf(
                    "Suscripción Válida por 1 Año Completo",
                    "Flujo de datos Master MQA directo",
                    "Ecualizador Paramétrico Arkaios AI",
                    "Visualizador 3D de Ondas en Tiempo Real"
                ),
                badge = "TIDAL MASTER HIFI ANUAL"
            )
        )
    }

    suspend fun processPayment(amount: Double, concept: String): Result<AmrTransaction> = withContext(Dispatchers.IO) {
        val current = _walletState.value
        if (current.balance < amount) {
            return@withContext Result.failure(
                Exception("Saldo insuficiente en Cartera AMR. Tienes ${"%.2f".format(current.balance)} AMR y se requieren ${"%.2f".format(amount)} AMR.")
            )
        }

        val nextBalance = current.balance - amount
        val isGod = if (concept.contains("God Owner", ignoreCase = true)) true else current.isGodOwnerLicensed
        val isTidal = if (concept.contains("Tidal", ignoreCase = true)) true else current.isTidalHiFiUnlocked

        val updatedWallet = current.copy(
            balance = nextBalance,
            isGodOwnerLicensed = isGod,
            isTidalHiFiUnlocked = isTidal
        )
        walletDao.saveWallet(WalletEntity.fromWallet(updatedWallet))
        _walletState.value = updatedWallet

        val txHash = generateTxHash(current.address, "ark_merchant_music", amount)
        val transaction = AmrTransaction(
            id = "tx_" + System.currentTimeMillis(),
            type = AmrTxType.PAYMENT,
            amount = amount,
            concept = concept,
            txHash = txHash,
            status = "CONFIRMED"
        )
        walletDao.insertTransaction(TransactionEntity.fromTransaction(transaction))

        Result.success(transaction)
    }

    suspend fun rewardListeningContribution(amount: Double = 25.00, reason: String = "Recompensa por Nodo Activo de Audio"): AmrTransaction = withContext(Dispatchers.IO) {
        val current = _walletState.value
        val nextBalance = current.balance + amount

        val updatedWallet = current.copy(balance = nextBalance)
        walletDao.saveWallet(WalletEntity.fromWallet(updatedWallet))
        _walletState.value = updatedWallet

        val txHash = generateTxHash("network_mint", current.address, amount)
        val transaction = AmrTransaction(
            id = "tx_" + System.currentTimeMillis(),
            type = AmrTxType.REWARD_MINT,
            amount = amount,
            concept = reason,
            txHash = txHash,
            status = "CONFIRMED"
        )
        walletDao.insertTransaction(TransactionEntity.fromTransaction(transaction))
        transaction
    }

    suspend fun transferTokens(toAddress: String, amount: Double): Result<AmrTransaction> = withContext(Dispatchers.IO) {
        val current = _walletState.value
        if (current.balance < amount) {
            return@withContext Result.failure(Exception("Saldo insuficiente"))
        }

        val nextBalance = current.balance - amount
        val updatedWallet = current.copy(balance = nextBalance)
        walletDao.saveWallet(WalletEntity.fromWallet(updatedWallet))
        _walletState.value = updatedWallet

        val txHash = generateTxHash(current.address, toAddress, amount)
        val transaction = AmrTransaction(
            id = "tx_" + System.currentTimeMillis(),
            type = AmrTxType.TRANSFER_OUT,
            amount = amount,
            concept = "Transferencia a $toAddress",
            txHash = txHash,
            status = "CONFIRMED"
        )
        walletDao.insertTransaction(TransactionEntity.fromTransaction(transaction))
        Result.success(transaction)
    }

    private fun generateTxHash(from: String, to: String, amount: Double): String {
        val raw = "$from-$to-$amount-${System.currentTimeMillis()}"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(raw.toByteArray())
        return "0x" + hash.joinToString("") { "%02x".format(it) }
    }
}
