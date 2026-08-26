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

        // If it's a token pack purchase, convert USD to AMR (e.g. $1 USD = 10 AMR)
        val amrToAdd = if (!isMembership) amountUsd * 10.0 else 0.0
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
            amount = if (isMembership) amountUsd else amrToAdd,
            concept = "$concept (PayPal v6 SDK • $orderId)",
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
                title = "Suscripción Licencia ARKAIOS God Owner",
                description = "Acceso ilimitado a todos los nodos del ecosistema, descargas sin límites en Puter Cloud y cuota de inferencia en Gemini 1.5 Pro / GPT-4o.",
                priceAmr = 49.99,
                features = listOf(
                    "Descargas FLAC 24-bit ilimitadas",
                    "Reproducción Offline sin restricciones",
                    "Nodo de Audio Dedicado en Puter Cloud",
                    "Inferencia IA de recomendación musical",
                    "Insignia Dorada de Verificación God Card"
                ),
                badge = "⚡ LICENCIA OFICIAL GOD OWNER",
                isGodOwnerTier = true
            ),
            ArkaiosPremiumTier(
                id = "tidal_master_hifi",
                title = "Tidal Master HiFi & MQA Pass",
                description = "Transmisión de audio sin pérdidas hasta 9216 kbps (192 kHz / 24-bit FLAC) conectada directamente al SDK de Tidal.",
                priceAmr = 19.99,
                features = listOf(
                    "Flujo de datos Master MQA directo",
                    "Ecualizador Paramétrico Arkaios AI",
                    "Visualizador 3D de Ondas en Tiempo Real",
                    "Cero Publicidad de por vida"
                ),
                badge = "TIDAL MASTER HIFI"
            ),
            ArkaiosPremiumTier(
                id = "pro_creator_node",
                title = "Arkaios Sound Creator Node",
                description = "Sube tus pistas .mp3 y .m4a a la nube descentralizada y monetiza con AMR cada reproducción de tus oyentes.",
                priceAmr = 29.99,
                features = listOf(
                    "Hosting de 50GB en Puter.fs",
                    "Monetización de pistas con AMR Tokens",
                    "Estadísticas de streaming en vivo",
                    "API Webhook de Arkaios Pay para ventas"
                ),
                badge = "CREATOR NODE"
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
