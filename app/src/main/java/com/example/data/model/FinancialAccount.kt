package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_accounts")
data class FinancialAccount(
    @PrimaryKey val id: String,
    val name: String, // e.g. "Bank Mandiri Kas Utama", "BCA Tabungan", "Kas Tunai / Brankas", "GoPay"
    val accountType: String, // "BANK", "CASH", "E_WALLET"
    val accountNumber: String = "",
    val balance: Long = 0L,
    val isFamilyPool: Boolean = false, // Kas bersama keluarga
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
