package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["idempotencyKey"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["memberId"]),
        Index(value = ["sourceAccountId"])
    ]
)
data class TransactionRecord(
    @PrimaryKey val id: String, // e.g. "TXN-202611-1001"
    val idempotencyKey: String, // Prevents duplicate submissions
    val transactionType: String, // MEMBER_DEPOSIT, MEMBER_WITHDRAWAL, FAMILY_INCOME, FAMILY_EXPENSE, ACCOUNT_TRANSFER, CORRECTION
    val memberId: String? = null,
    val memberName: String? = null,
    val sourceAccountId: String? = null,
    val sourceAccountName: String? = null,
    val destinationAccountId: String? = null,
    val destinationAccountName: String? = null,
    val amount: Long, // Nominal in Rupiah
    val category: String, // e.g., "Tabungan Rutin", "Kas Darurat", "Pendidikan", "Uang Saku", dll
    val notes: String = "",
    val adminId: String, // Admin who recorded
    val adminName: String,
    val status: String = "POSTED", // POSTED, PENDING, VOIDED
    val voidReason: String? = null,
    val voidTimestamp: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)
