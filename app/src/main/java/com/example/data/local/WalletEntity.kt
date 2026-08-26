package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AmrTransaction
import com.example.data.model.AmrTxType
import com.example.data.model.AmrWallet

@Entity(tableName = "wallet_info")
data class WalletEntity(
    @PrimaryKey val address: String,
    val balance: Double,
    val currency: String,
    val network: String,
    val userEmail: String,
    val userName: String,
    val isGodOwnerLicensed: Boolean,
    val isTidalHiFiUnlocked: Boolean
) {
    fun toWallet(): AmrWallet {
        return AmrWallet(
            address = address,
            balance = balance,
            currency = currency,
            network = network,
            userEmail = userEmail,
            userName = userName,
            isGodOwnerLicensed = isGodOwnerLicensed,
            isTidalHiFiUnlocked = isTidalHiFiUnlocked
        )
    }

    companion object {
        fun fromWallet(wallet: AmrWallet): WalletEntity {
            return WalletEntity(
                address = wallet.address,
                balance = wallet.balance,
                currency = wallet.currency,
                network = wallet.network,
                userEmail = wallet.userEmail,
                userName = wallet.userName,
                isGodOwnerLicensed = wallet.isGodOwnerLicensed,
                isTidalHiFiUnlocked = wallet.isTidalHiFiUnlocked
            )
        }
    }
}

@Entity(tableName = "wallet_transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val amount: Double,
    val concept: String,
    val timestamp: Long,
    val txHash: String,
    val status: String
) {
    fun toTransaction(): AmrTransaction {
        return AmrTransaction(
            id = id,
            type = try { AmrTxType.valueOf(type) } catch (e: Exception) { AmrTxType.PAYMENT },
            amount = amount,
            concept = concept,
            timestamp = timestamp,
            txHash = txHash,
            status = status
        )
    }

    companion object {
        fun fromTransaction(tx: AmrTransaction): TransactionEntity {
            return TransactionEntity(
                id = tx.id,
                type = tx.type.name,
                amount = tx.amount,
                concept = tx.concept,
                timestamp = tx.timestamp,
                txHash = tx.txHash,
                status = tx.status
            )
        }
    }
}
