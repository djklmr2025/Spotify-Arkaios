package com.example.data.model

data class AmrWallet(
    val address: String = "amr_arkaios2026",
    val balance: Double = 1000.00,
    val currency: String = "AMR",
    val network: String = "ARKAIOS-MAINNET-v1",
    val userEmail: String = "arkaios2026@gmail.com",
    val userName: String = "Arkaios Master",
    val isGodOwnerLicensed: Boolean = false,
    val isTidalHiFiUnlocked: Boolean = false
)

data class AmrTransaction(
    val id: String,
    val type: AmrTxType,
    val amount: Double,
    val concept: String,
    val timestamp: Long = System.currentTimeMillis(),
    val txHash: String,
    val status: String = "CONFIRMED"
)

enum class AmrTxType {
    PAYMENT,
    REWARD_MINT,
    TRANSFER_IN,
    TRANSFER_OUT
}

data class ArkaiosPremiumTier(
    val id: String,
    val title: String,
    val description: String,
    val priceAmr: Double,
    val features: List<String>,
    val badge: String,
    val isGodOwnerTier: Boolean = false
)
