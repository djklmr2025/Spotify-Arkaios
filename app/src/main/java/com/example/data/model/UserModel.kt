package com.example.data.model

enum class UserTier {
    FREE,
    ARKAIOS_PREMIUM_HIFI,
    TREASURE_VIP_LIFETIME
}

data class UserProfile(
    val userId: String = "ark_user_001",
    val email: String = "arkaios2026@gmail.com",
    val displayName: String = "Arkaios Master",
    val avatarUrl: String = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200",
    val tier: UserTier = UserTier.ARKAIOS_PREMIUM_HIFI,
    val isGoogleLinked: Boolean = true,
    val treasureBalanceAmr: Double = 1250.0,
    val offlineEncryptedCacheEnabled: Boolean = true,
    val flacMasterDownloadAccess: Boolean = true,
    val memberSince: String = "Agosto 2026"
)
