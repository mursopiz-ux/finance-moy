package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, accountType: String, accountNumber: String, balance: Long, isFamilyPool: Boolean, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("BANK") }
    var accountNumber by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("0") }
    var isFamilyPool by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val types = listOf("BANK" to "Rekening Bank", "CASH" to "Kas Tunai", "E_WALLET" to "E-Wallet")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Rekening / Kas", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nama Rekening / Kas") },
                    placeholder = { Text("Contoh: BRI Tabungan Utama") },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth().testTag("account_name_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Tipe Akun:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    types.forEach { (key, label) ->
                        FilterChip(
                            selected = accountType == key,
                            onClick = { accountType = key },
                            label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("Nomor Rekening / Keterangan") },
                    placeholder = { Text("Misal: 5420182740") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) balanceText = it },
                    label = { Text("Saldo Awal (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("account_initial_balance_input")
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFamilyPool,
                        onCheckedChange = { isFamilyPool = it }
                    )
                    Text("Alokasikan sebagai Kas Bersama Keluarga", style = MaterialTheme.typography.bodyMedium)
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Nama rekening/kas tidak boleh kosong"
                    } else {
                        val bal = balanceText.toLongOrNull() ?: 0L
                        onConfirm(name.trim(), accountType, accountNumber.trim(), bal, isFamilyPool, notes.trim())
                    }
                },
                modifier = Modifier.testTag("submit_account_button")
            ) {
                Text("Simpan Akun")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
