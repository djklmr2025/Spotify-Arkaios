package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_info WHERE address = :address LIMIT 1")
    fun getWalletFlow(address: String): Flow<WalletEntity?>

    @Query("SELECT * FROM wallet_info LIMIT 1")
    suspend fun getWallet(): WalletEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWallet(wallet: WalletEntity)

    @Query("UPDATE wallet_info SET balance = :newBalance WHERE address = :address")
    suspend fun updateBalance(address: String, newBalance: Double)

    @Query("UPDATE wallet_info SET isGodOwnerLicensed = :licensed WHERE address = :address")
    suspend fun updateGodOwnerLicense(address: String, licensed: Boolean)

    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
}
