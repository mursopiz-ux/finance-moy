package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.FamilyMember
import com.example.data.model.FinancialAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    members: List<FamilyMember>,
    accounts: List<FinancialAccount>,
    onDismiss: () -> Unit,
    onSubmit: (
        type: String,
        amount: Long,
        memberId: String?,
        sourceAccountId: String?,
        destAccountId: String?,
        category: String,
        notes: String,
        isOfflinePending: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val txnTypes = listOf(
        "MEMBER_DEPOSIT" to "Setor Tabungan",
        "MEMBER_WITHDRAWAL" to "Tarik Tabungan",
        "FAMILY_INCOME" to "Pemasukan Kas",
        "FAMILY_EXPENSE" to "Pengeluaran Kas",
        "ACCOUNT_TRANSFER" to "Transfer Akun"
    )

    var selectedType by remember { mutableStateOf("MEMBER_DEPOSIT") }
    var amountText by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
    var selectedSourceAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var selectedDestAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: "") }
    var selectedCategory by remember { mutableStateOf("Tabungan Rutin") }
    var notesText by remember { mutableStateOf("") }
    var isOfflinePending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = when (selectedType) {
        "MEMBER_DEPOSIT" -> listOf("Tabungan Rutin", "Tabungan Pokok", "Hadiah / THR", "Bonus / Uang Saku")
        "MEMBER_WITHDRAWAL" -> listOf("Keperluan Pribadi", "Biaya Sekolah", "Kesehatan", "Belanja Mandiri")
        "FAMILY_INCOME" -> listOf("Gaji Bulanan", "Hasil Usaha", "Bagi Hasil", "Sumbangan Keluarga", "Lainnya")
        "FAMILY_EXPENSE" -> listOf("Kebutuhan Dapur", "Listrik & Air", "Internet & Pulsa", "Pendidikan Anak", "Darurat", "Transportasi")
        "ACCOUNT_TRANSFER" -> listOf("Pindah Buku", "Isi E-Wallet", "Tarik Tunai Kas", "Setor Tunai Bank")
        else -> listOf("Umum")
    }

    LaunchedEffect(selectedType) {
        selectedCategory = categories.first()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Catat Transaksi Baru",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Selector (Chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                txnTypes.forEach { (typeKey, label) ->
                    FilterChip(
                        selected = selectedType == typeKey,
                        onClick = {
                            selectedType = typeKey
                            errorMessage = null
                        },
                        label = { Text(label) },
                        leadingIcon = if (selectedType == typeKey) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        amountText = input
                        errorMessage = null
                    }
                },
                label = { Text("Nominal Transaksi (Rp)") },
                placeholder = { Text("Contoh: 500000") },
                prefix = { Text("Rp ", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_amount_input")
            )

            // Quick Amount Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50_000L to "+50rb", 100_000L to "+100rb", 250_000L to "+250rb", 500_000L to "+500rb", 1_000_000L to "+1jt").forEach { (quickVal, label) ->
                    SuggestionChip(
                        onClick = {
                            val current = amountText.toLongOrNull() ?: 0L
                            amountText = (current + quickVal).toString()
                            errorMessage = null
                        },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Member Selector (if applicable)
            if (selectedType == "MEMBER_DEPOSIT" || selectedType == "MEMBER_WITHDRAWAL") {
                Text(
                    text = "Anggota Keluarga:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    members.forEach { m ->
                        InputChip(
                            selected = selectedMemberId == m.id,
                            onClick = { selectedMemberId = m.id },
                            label = { Text("${m.name} (${CurrencyFormatter.formatRupiah(m.currentBalance)})") },
                            avatar = {
                                Icon(Icons.Default.Person, contentDescription = null, Modifier.size(16.dp))
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Source Account Selector
            Text(
                text = if (selectedType == "ACCOUNT_TRANSFER") "Dari Rekening / Kas Asal:" else "Rekening / Kas Terkait:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accounts.forEach { acc ->
                    InputChip(
                        selected = selectedSourceAccountId == acc.id,
                        onClick = { selectedSourceAccountId = acc.id },
                        label = { Text("${acc.name} (${CurrencyFormatter.formatRupiah(acc.balance)})") },
                        avatar = {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, Modifier.size(16.dp))
                        }
                    )
                }
            }

            // Destination Account Selector (Transfer only)
            if (selectedType == "ACCOUNT_TRANSFER") {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ke Rekening / Kas Tujuan:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.filter { it.id != selectedSourceAccountId }.forEach { acc ->
                        InputChip(
                            selected = selectedDestAccountId == acc.id,
                            onClick = { selectedDestAccountId = acc.id },
                            label = { Text("${acc.name} (${CurrencyFormatter.formatRupiah(acc.balance)})") },
                            avatar = {
                                Icon(Icons.Default.Savings, contentDescription = null, Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            Text(
                text = "Kategori:",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Catatan / Keterangan (Opsional)") },
                placeholder = { Text("Misal: Tabungan gaji bulan ini") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Offline Pending Toggle
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Simulasi Offline (Antrean Pending)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Simpan transaksi sebagai draft offline sebelum disahkan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isOfflinePending,
                        onCheckedChange = { isOfflinePending = it },
                        modifier = Modifier.testTag("offline_toggle_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    if (amount <= 0L) {
                        errorMessage = "Nominal harus lebih dari 0"
                        return@Button
                    }

                    // Pre-validation checks
                    if (selectedType == "MEMBER_WITHDRAWAL") {
                        val m = members.find { it.id == selectedMemberId }
                        if (m != null && m.currentBalance < amount) {
                            errorMessage = "Saldo tabungan ${m.name} tidak cukup (${CurrencyFormatter.formatRupiah(m.currentBalance)})"
                            return@Button
                        }
                    }
                    if (selectedType == "FAMILY_EXPENSE" || selectedType == "MEMBER_WITHDRAWAL" || selectedType == "ACCOUNT_TRANSFER") {
                        val acc = accounts.find { it.id == selectedSourceAccountId }
                        if (acc != null && acc.balance < amount) {
                            errorMessage = "Saldo ${acc.name} tidak cukup (${CurrencyFormatter.formatRupiah(acc.balance)})"
                            return@Button
                        }
                    }

                    onSubmit(
                        selectedType,
                        amount,
                        if (selectedType.startsWith("MEMBER")) selectedMemberId else null,
                        selectedSourceAccountId,
                        if (selectedType == "ACCOUNT_TRANSFER") selectedDestAccountId else null,
                        selectedCategory,
                        notesText,
                        isOfflinePending
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_transaction_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOfflinePending) "Simpan ke Antrean Offline" else "Sah & Bukukan Transaksi",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
