package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val role: String = "SALES", // "ADMIN" or "SALES"
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Product(
    @DocumentId val id: String = "",
    val name: String = "",
    val code: String = "",
    val brandOrOrigin: String = "Pabrik Utama", // Pabrik A, Pabrik B, Merk ABC, etc.
    val sellPrice: Double = 0.0,
    val hppPrice: Double = 0.0,
    val savingsPerUnit: Double = 500.0,
    val mandatoryDepositDefault: Double = 0.0,
    val category: String = "Umum",
    val active: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    // Calculated standard mandatory deposit (HPP + savings)
    val calculatedStandardDeposit: Double
        get() = hppPrice + savingsPerUnit

    // Potential profit margin per unit for sales
    val estimatedMargin: Double
        get() = sellPrice - calculatedStandardDeposit
}

@IgnoreExtraProperties
data class UserStock(
    @DocumentId val id: String = "", // formatted as userId_productId
    val userId: String = "",
    val productId: String = "",
    val productName: String = "",
    val brandOrOrigin: String = "",
    val totalAvailableStock: Long = 0L,
    val rolloverFromLastWeek: Long = 0L,
    val currentWeekAllocation: Long = 0L,
    val transfersReceived: Long = 0L,
    val transfersSent: Long = 0L,
    val debtBorrowed: Long = 0L,
    val soldCount: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WeeklyPool(
    @DocumentId val id: String = "",
    val weekIdentifier: String = "", // e.g. "Minggu 45 (Nov 2026)"
    val productId: String = "",
    val productName: String = "",
    val brandOrOrigin: String = "",
    val totalPoolQty: Long = 0L,
    val allocatedQty: Long = 0L,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val remainingPoolQty: Long
        get() = (totalPoolQty - allocatedQty).coerceAtLeast(0L)
}

@IgnoreExtraProperties
data class TransferRecord(
    @DocumentId val id: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val toUserName: String = "",
    val productId: String = "",
    val productName: String = "",
    val brandOrOrigin: String = "",
    val quantity: Long = 0L,
    val note: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)

@IgnoreExtraProperties
data class DebtRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val type: String = "SETORAN_TERTUNDA", // "SETORAN_TERTUNDA" or "PINJAM_BARANG"
    val productId: String = "",
    val productName: String = "",
    val brandOrOrigin: String = "",
    val quantity: Long = 0L,
    val totalNominal: Double = 0.0,
    val note: String = "",
    val isSettled: Boolean = false,
    val settledAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SaleTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val productId: String = "",
    val productName: String = "",
    val brandOrOrigin: String = "",
    val quantity: Long = 0L,
    val sellPricePerUnit: Double = 0.0,
    val hppPerUnit: Double = 0.0,
    val savingsPerUnit: Double = 0.0,
    val isSavingsIncluded: Boolean = true,
    val totalRevenue: Double = 0.0,
    val totalHpp: Double = 0.0,
    val totalSavings: Double = 0.0,
    val totalMandatoryDeposit: Double = 0.0,
    val netMarginSales: Double = 0.0,
    val isDepositSettled: Boolean = false,
    val note: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)

@IgnoreExtraProperties
data class SavingsRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val type: String = "SETOR_MANDIRI", // "SETOR_MANDIRI" or "TARIK"
    val nominal: Double = 0.0,
    val note: String = "",
    @ServerTimestamp val timestamp: Timestamp? = null
)
