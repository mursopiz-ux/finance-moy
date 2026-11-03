package com.example.ui.components

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object CurrencyFormatter {
    private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatRupiah(amount: Long): String {
        return rupiahFormat.format(amount).replace("Rp", "Rp ")
    }

    fun formatShortDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun getTransactionTypeLabel(type: String): String {
        return when (type) {
            "MEMBER_DEPOSIT" -> "Setoran Anggota"
            "MEMBER_WITHDRAWAL" -> "Penarikan Anggota"
            "FAMILY_INCOME" -> "Pemasukan Kas Bersama"
            "FAMILY_EXPENSE" -> "Pengeluaran Kas Bersama"
            "ACCOUNT_TRANSFER" -> "Transfer Rekening"
            "CORRECTION" -> "Koreksi Pembukuan"
            else -> type
        }
    }

    fun getTransactionColor(type: String): Color {
        return when (type) {
            "MEMBER_DEPOSIT", "FAMILY_INCOME" -> SuccessGreen
            "MEMBER_WITHDRAWAL", "FAMILY_EXPENSE" -> DangerRed
            "ACCOUNT_TRANSFER" -> InfoBlue
            "CORRECTION" -> WarningAmber
            else -> EmeraldPrimary
        }
    }

    fun getTransactionSign(type: String): String {
        return when (type) {
            "MEMBER_DEPOSIT", "FAMILY_INCOME" -> "+"
            "MEMBER_WITHDRAWAL", "FAMILY_EXPENSE" -> "-"
            "ACCOUNT_TRANSFER" -> "⇄"
            else -> ""
        }
    }
}
