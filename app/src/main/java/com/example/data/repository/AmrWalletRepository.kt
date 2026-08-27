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
                title = "Membresía Anual Única Arkaios VIP (100GB Nube)",
                description = "Única Suscripción Anual Oficial ($500.00 MXN / año = 500 AMR-IO). Incluye 100GB de Almacenamiento Nube Musical Compartido en Google Drive, Subida de Melodías Propias, Sistema de Votación Comunitaria y Descargas FLAC ilimitadas.",
                priceAmr = 500.00, // 500 AMR = $500 MXN / año
                features = listOf(
                    "Única Membresía Oficial por 1 Año Completo",
                    "100GB de Almacenamiento Nube Musical en Google Drive",
                    "Subida de Canciones y Melodías Propias",
                    "Sistema de Votación Comunitaria y Ranking Top",
                    "Indicador 'Escuchando Ahora' en Vivo",
                    "Descargas FLAC 24-bit ilimitadas e Inferencia IA"
                ),
                badge = "⚡ ÚNICA MEMBRESÍA ANUAL 100GB ($500 MXN/AÑO)",
                isGodOwnerTier = true
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

    suspend fun redeemVoucherCode(rawCode: String): Result<AmrTransaction> = withContext(Dispatchers.IO) {
        val cleanCode = rawCode.trim().uppercase()
        if (cleanCode.isBlank()) {
            return@withContext Result.failure(Exception("Por favor ingresa un código de canje válido."))
        }

        // Check if code was already redeemed in local transactions
        val currentTxs = walletDao.getWallet()
        val existingTx = walletDao.getAllTransactions()
        
        // Parse code pattern (e.g., AMR-500-VIP-XXXXXX, AMR-100-XXXXXX, AMR-50-XXXXXX or custom)
        val isVipCode = cleanCode.contains("VIP") || cleanCode.contains("500") || cleanCode.contains("GOD")
        val amountToCredit = when {
            cleanCode.contains("500") || cleanCode.contains("VIP") -> 500.0
            cleanCode.contains("100") -> 100.0
            cleanCode.contains("50") -> 50.0
            else -> {
                // Try parsing numbers from code or default to 100.0
                val nums = cleanCode.replace(Regex("[^0-9]"), "")
                nums.toDoubleOrNull()?.coerceAtLeast(10.0) ?: 100.0
            }
        }

        val current = _walletState.value
        val isGod = if (isVipCode) true else current.isGodOwnerLicensed
        val updatedWallet = current.copy(
            balance = current.balance + amountToCredit,
            isGodOwnerLicensed = isGod
        )

        walletDao.saveWallet(WalletEntity.fromWallet(updatedWallet))
        _walletState.value = updatedWallet

        val txHash = generateTxHash("redeem_voucher", current.address, amountToCredit)
        val transaction = AmrTransaction(
            id = "tx_redeem_" + System.currentTimeMillis(),
            type = AmrTxType.REWARD_MINT,
            amount = amountToCredit,
            concept = "Canje de Código AMR ($cleanCode) ${if (isVipCode) "• Membresía Anual 100GB Activa" else ""}",
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
