package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FamilySavingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = FamilySavingsRepository(db, db.savingsDao())

    val activeAdmin: StateFlow<AdminUser?> = repository.activeAdmin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val admins: StateFlow<List<AdminUser>> = repository.allAdmins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val members: StateFlow<List<FamilyMember>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<FinancialAccount>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionRecord>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<TransactionRecord>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ledgerEntries: StateFlow<List<LedgerEntry>> = repository.allLedgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
        }
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun switchAdmin(adminId: String, pin: String) {
        viewModelScope.launch {
            val result = repository.switchActiveAdmin(adminId, pin)
            result.onSuccess {
                _snackbarMessage.value = "Admin aktif: ${it.name} (${it.role})"
            }.onFailure {
                _snackbarMessage.value = "Gagal ganti admin: ${it.localizedMessage}"
            }
        }
    }

    fun addMember(name: String, relationship: String, targetSavings: Long, notes: String) {
        viewModelScope.launch {
            val result = repository.createMember(name, relationship, targetSavings, notes)
            result.onSuccess {
                _snackbarMessage.value = "Anggota ${it.name} berhasil ditambahkan!"
            }.onFailure {
                _snackbarMessage.value = "Gagal menambah anggota: ${it.localizedMessage}"
            }
        }
    }

    fun addAccount(name: String, accountType: String, accountNumber: String, balance: Long, isFamilyPool: Boolean, notes: String) {
        viewModelScope.launch {
            val result = repository.createAccount(name, accountType, accountNumber, balance, isFamilyPool, notes)
            result.onSuccess {
                _snackbarMessage.value = "Rekening/kas ${it.name} berhasil ditambahkan!"
            }.onFailure {
                _snackbarMessage.value = "Gagal menambah rekening: ${it.localizedMessage}"
            }
        }
    }

    fun recordTransaction(
        type: String,
        amount: Long,
        memberId: String?,
        sourceAccountId: String?,
        destAccountId: String?,
        category: String,
        notes: String,
        isOfflinePending: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.executeTransaction(
                transactionType = type,
                amount = amount,
                memberId = memberId,
                sourceAccountId = sourceAccountId,
                destinationAccountId = destAccountId,
                category = category,
                notes = notes,
                isOfflinePending = isOfflinePending
            )
            result.onSuccess { txn ->
                val statusText = if (txn.status == "PENDING") "Tersimpan di antrean offline" else "Berhasil dibukukan"
                _snackbarMessage.value = "Transaksi Rp ${txn.amount} $statusText [${txn.id}]"
            }.onFailure {
                _snackbarMessage.value = "Transaksi gagal: ${it.localizedMessage}"
            }
        }
    }

    fun voidTransaction(transactionId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.voidTransaction(transactionId, reason)
            result.onSuccess {
                _snackbarMessage.value = "Transaksi berhasil dibatalkan (saldo direverse)"
            }.onFailure {
                _snackbarMessage.value = "Gagal membatalkan: ${it.localizedMessage}"
            }
        }
    }

    fun syncOfflineQueue() {
        viewModelScope.launch {
            val result = repository.synchronizePendingQueue()
            result.onSuccess { count ->
                _snackbarMessage.value = "Sinkronisasi selesai: $count transaksi offline berhasil dibukukan"
            }.onFailure {
                _snackbarMessage.value = "Gagal sinkronisasi: ${it.localizedMessage}"
            }
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = repository.exportDatabaseToJson()
                onResult(json)
                _snackbarMessage.value = "Cadangan JSON berhasil dibuat!"
            } catch (e: Exception) {
                _snackbarMessage.value = "Gagal export: ${e.localizedMessage}"
            }
        }
    }

    fun importBackup(jsonString: String) {
        viewModelScope.launch {
            val result = repository.importDatabaseFromJson(jsonString)
            result.onSuccess {
                _snackbarMessage.value = "Data berhasil dipulihkan dari cadangan JSON!"
            }.onFailure {
                _snackbarMessage.value = "Gagal restore: ${it.localizedMessage}"
            }
        }
    }

    fun exportCsv(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val csv = repository.generateTransactionsCsv()
                onResult(csv)
                _snackbarMessage.value = "Laporan CSV transaksi berhasil diekspor!"
            } catch (e: Exception) {
                _snackbarMessage.value = "Gagal ekspor CSV: ${e.localizedMessage}"
            }
        }
    }
}
