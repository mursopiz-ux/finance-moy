package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_entries",
    indices = [
        Index(value = ["transactionId"]),
        Index(value = ["accountOrEntity"]),
        Index(value = ["timestamp"])
    ]
)
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val transactionId: String,
    val entryType: String, // "DEBIT" or "CREDIT"
    val accountOrEntity: String, // "MEMBER:<id>", "ACCOUNT:<id>", "FAMILY_POOL"
    val entityName: String,
    val amount: Long,
    val memo: String,
    val timestamp: Long = System.currentTimeMillis()
)
