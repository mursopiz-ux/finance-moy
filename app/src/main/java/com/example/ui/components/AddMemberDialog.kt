package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, relationship: String, targetSavings: Long, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("Anak") }
    var targetText by remember { mutableStateOf("10000000") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val relOptions = listOf("Kepala Keluarga", "Ibu Rumah Tangga", "Anak", "Kakek/Nenek", "Saudara")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row {
                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Anggota Keluarga", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nama Anggota") },
                    placeholder = { Text("Misal: Sarah (Anak Ke-3)") },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth().testTag("member_name_input")
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Hubungan Keluarga:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    relOptions.take(3).forEach { rel ->
                        FilterChip(
                            selected = relationship == rel,
                            onClick = { relationship = rel },
                            label = { Text(rel, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) targetText = it },
                    label = { Text("Target Tabungan (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("member_target_input")
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan (Opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Nama anggota wajib diisi"
                    } else {
                        val target = targetText.toLongOrNull() ?: 0L
                        onConfirm(name.trim(), relationship, target, notes.trim())
                    }
                },
                modifier = Modifier.testTag("submit_member_button")
            ) {
                Text("Simpan Anggota")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
