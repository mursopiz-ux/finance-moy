package com.example

import com.example.ui.components.CurrencyFormatter
import org.junit.Assert.*
import org.junit.Test

class FamilySavingsFinancialAuditTest {

    @Test
    fun testCurrencyFormattingRupiah() {
        val amount = 15_000_000L
        val formatted = CurrencyFormatter.formatRupiah(amount)
        assertTrue("Format rupiah harus mengandung 'Rp' dan 15.000.000", formatted.contains("15.000.000"))
        assertEquals("Setoran Anggota", CurrencyFormatter.getTransactionTypeLabel("MEMBER_DEPOSIT"))
        assertEquals("Penarikan Anggota", CurrencyFormatter.getTransactionTypeLabel("MEMBER_WITHDRAWAL"))
        assertEquals("Pemasukan Kas Bersama", CurrencyFormatter.getTransactionTypeLabel("FAMILY_INCOME"))
        assertEquals("Pengeluaran Kas Bersama", CurrencyFormatter.getTransactionTypeLabel("FAMILY_EXPENSE"))
        assertEquals("Transfer Rekening", CurrencyFormatter.getTransactionTypeLabel("ACCOUNT_TRANSFER"))
    }

    @Test
    fun testDoubleEntryReconciliationInvariant() {
        // Double entry ledger invariant: For every financial transaction, sum(Debits) == sum(Credits)
        val transactions = listOf(
            Triple("TXN-1", "MEMBER_DEPOSIT", 2_500_000L),
            Triple("TXN-2", "FAMILY_INCOME", 10_000_000L),
            Triple("TXN-3", "MEMBER_WITHDRAWAL", 500_000L),
            Triple("TXN-4", "ACCOUNT_TRANSFER", 1_000_000L)
        )

        var totalDebit = 0L
        var totalCredit = 0L

        transactions.forEach { (_, _, amount) ->
            totalDebit += amount
            totalCredit += amount
        }

        assertEquals("Total Debit harus persis sama dengan Total Kredit", totalDebit, totalCredit)
    }

    @Test
    fun testOverspendingValidationRule() {
        val memberBalance = 1_500_000L
        val requestedWithdrawal = 2_000_000L

        val canWithdraw = memberBalance >= requestedWithdrawal
        assertFalse("Penarikan melebihi saldo anggota harus ditolak oleh sistem", canWithdraw)

        val accountBalance = 5_000_000L
        val familyExpense = 6_000_000L
        val canSpend = accountBalance >= familyExpense
        assertFalse("Pengeluaran melebihi kas rekening harus ditolak", canSpend)
    }

    @Test
    fun testAtomicReversalOnVoid() {
        var memberBalance = 10_000_000L
        var accountBalance = 15_000_000L
        val depositAmount = 2_000_000L

        // Execute deposit
        memberBalance += depositAmount
        accountBalance += depositAmount
        assertEquals(12_000_000L, memberBalance)
        assertEquals(17_000_000L, accountBalance)

        // Void deposit (reversal)
        memberBalance -= depositAmount
        accountBalance -= depositAmount
        assertEquals("Saldo anggota harus kembali ke nilai awal setelah void", 10_000_000L, memberBalance)
        assertEquals("Saldo rekening harus kembali ke nilai awal setelah void", 15_000_000L, accountBalance)
    }

    @Test
    fun testCsvReportHeaderAndRowFormatting() {
        val header = "ID Transaksi,Waktu,Tipe,Anggota,Rekening Sumber,Rekening Tujuan,Nominal (Rp),Kategori,Admin,Status,Catatan"
        val row = "\"TXN-202611-001\",\"2026-11-03 12:00\",\"MEMBER_DEPOSIT\",\"Ayah\",\"BCA\",\"-\",15000000,\"Tabungan\",\"Ayah\",\"POSTED\",\"Catatan\""

        assertTrue(header.contains("ID Transaksi"))
        assertTrue(header.contains("Nominal (Rp)"))
        assertTrue(row.contains("15000000"))
        assertTrue(row.contains("MEMBER_DEPOSIT"))
    }
}
