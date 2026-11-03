package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TransactionRecord
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.VoidTransactionDialog
import com.example.ui.theme.*

@Composable
fun TransactionsScreen(
    transactions: List<TransactionRecord>,
    onVoidTransaction: (transactionId: String, reason: String) -> Unit,
    onOpenAddTransaction: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var transactionToVoid by remember { mutableStateOf<TransactionRecord?>(null) }

    val filterOptions = listOf(
        "ALL" to "Semua",
        "MEMBER_DEPOSIT" to "Setoran",
        "MEMBER_WITHDRAWAL" to "Penarikan",
        "FAMILY_INCOME" to "Pemasukan",
        "FAMILY_EXPENSE" to "Pengeluaran",
        "ACCOUNT_TRANSFER" to "Transfer",
        "PENDING" to "Antrean Offline",
        "VOIDED" to "Dibatalkan"
    )

    val filteredTransactions = transactions.filter { txn ->
        val matchesSearch = searchQuery.isBlank() ||
                txn.notes.contains(searchQuery, ignoreCase = true) ||
                txn.category.contains(searchQuery, ignoreCase = true) ||
                (txn.memberName?.contains(searchQuery, ignoreCase = true) ?: false) ||
                (txn.sourceAccountName?.contains(searchQuery, ignoreCase = true) ?: false) ||
                txn.adminName.contains(searchQuery, ignoreCase = true) ||
                txn.id.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "ALL" -> true
            "PENDING" -> txn.status == "PENDING"
            "VOIDED" -> txn.status == "VOIDED"
            else -> txn.transactionType == selectedFilter
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAddTransaction,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Transaksi Baru", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_add_transaction")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_transactions_input"),
                placeholder = { Text("Cari transaksi, anggota, atau catatan...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = if (searchQuery.isNotBlank()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedFilter == key,
                        onClick = { selectedFilter = key },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Transactions
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada transaksi yang sesuai",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { txn ->
                        TransactionCardItem(
                            transaction = txn,
                            onVoidRequest = { transactionToVoid = txn }
                        )
                    }
                }
            }
        }
    }

    transactionToVoid?.let { txn ->
        VoidTransactionDialog(
            transaction = txn,
            onDismiss = { transactionToVoid = null },
            onConfirmVoid = { id, reason ->
                onVoidTransaction(id, reason)
                transactionToVoid = null
            }
        )
    }
}

@Composable
private fun TransactionCardItem(
    transaction: TransactionRecord,
    onVoidRequest: () -> Unit
) {
    val isDepositOrIncome = transaction.transactionType == "MEMBER_DEPOSIT" || transaction.transactionType == "FAMILY_INCOME"
    val isVoided = transaction.status == "VOIDED"
    val isPending = transaction.status == "PENDING"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isVoided) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isVoided) DangerRed.copy(alpha = 0.15f)
                            else if (isDepositOrIncome) SuccessGreen.copy(alpha = 0.15f)
                            else DangerRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isVoided) Icons.Default.Cancel
                        else if (isDepositOrIncome) Icons.Default.ArrowDownward
                        else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isVoided) DangerRed else if (isDepositOrIncome) SuccessGreen else DangerRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = CurrencyFormatter.getTransactionTypeLabel(transaction.transactionType),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${CurrencyFormatter.getTransactionSign(transaction.transactionType)} ${CurrencyFormatter.formatRupiah(transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isVoided) MaterialTheme.colorScheme.outline
                        else CurrencyFormatter.getTransactionColor(transaction.transactionType)
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (transaction.status) {
                            "PENDING" -> PendingOrange.copy(alpha = 0.2f)
                            "VOIDED" -> DangerRed.copy(alpha = 0.2f)
                            else -> SuccessGreen.copy(alpha = 0.15f)
                        },
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = when (transaction.status) {
                                "PENDING" -> "Antrean Offline"
                                "VOIDED" -> "Dibatalkan"
                                else -> "Sah (Posted)"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (transaction.status) {
                                "PENDING" -> PendingOrange
                                "VOIDED" -> DangerRed
                                else -> SuccessGreen
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Transaction Details Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (transaction.memberName != null) {
                    Text(
                        text = "Anggota: ${transaction.memberName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (transaction.sourceAccountName != null) {
                    Text(
                        text = "Akun: ${transaction.sourceAccountName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (transaction.destinationAccountName != null) {
                Text(
                    text = "Tujuan Transfer: ${transaction.destinationAccountName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (transaction.notes.isNotBlank()) {
                Text(
                    text = "Catatan: \"${transaction.notes}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isVoided && !transaction.voidReason.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DangerRed.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Text(
                        text = "Alasan Batal: ${transaction.voidReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DangerRed,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer with Admin and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Admin: ${transaction.adminName} • ${CurrencyFormatter.formatShortDate(transaction.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (!isVoided && !isPending) {
                    TextButton(
                        onClick = onVoidRequest,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Batalkan (Void)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
