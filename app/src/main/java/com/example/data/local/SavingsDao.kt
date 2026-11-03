package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {

    // --- Admin Users ---
    @Query("SELECT * FROM admin_users ORDER BY role ASC, name ASC")
    fun getAllAdmins(): Flow<List<AdminUser>>

    @Query("SELECT * FROM admin_users WHERE isActiveCurrent = 1 LIMIT 1")
    fun getCurrentActiveAdmin(): Flow<AdminUser?>

    @Query("SELECT * FROM admin_users WHERE isActiveCurrent = 1 LIMIT 1")
    suspend fun getCurrentActiveAdminSync(): AdminUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmins(admins: List<AdminUser>)

    @Query("UPDATE admin_users SET isActiveCurrent = CASE WHEN id = :adminId THEN 1 ELSE 0 END")
    suspend fun setActiveAdmin(adminId: String)

    // --- Family Members ---
    @Query("SELECT * FROM family_members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :memberId")
    suspend fun getMemberById(memberId: String): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<FamilyMember>)

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Query("DELETE FROM family_members WHERE id = :memberId")
    suspend fun deleteMember(memberId: String)

    @Query("UPDATE family_members SET currentBalance = currentBalance + :amountDiff WHERE id = :memberId")
    suspend fun adjustMemberBalance(memberId: String, amountDiff: Long)

    // --- Financial Accounts ---
    @Query("SELECT * FROM financial_accounts ORDER BY isFamilyPool DESC, name ASC")
    fun getAllAccounts(): Flow<List<FinancialAccount>>

    @Query("SELECT * FROM financial_accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: String): FinancialAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FinancialAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<FinancialAccount>)

    @Update
    suspend fun updateAccount(account: FinancialAccount)

    @Query("DELETE FROM financial_accounts WHERE id = :accountId")
    suspend fun deleteAccount(accountId: String)

    @Query("UPDATE financial_accounts SET balance = balance + :amountDiff WHERE id = :accountId")
    suspend fun adjustAccountBalance(accountId: String, amountDiff: Long)

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY timestamp ASC")
    fun getPendingTransactions(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): TransactionRecord?

    @Query("SELECT * FROM transactions WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getTransactionByIdempotencyKey(key: String): TransactionRecord?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: TransactionRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionRecord>)

    @Update
    suspend fun updateTransaction(transaction: TransactionRecord)

    @Query("UPDATE transactions SET status = 'POSTED' WHERE id = :transactionId")
    suspend fun markTransactionPosted(transactionId: String)

    @Query("UPDATE transactions SET status = 'VOIDED', voidReason = :reason, voidTimestamp = :voidTime WHERE id = :transactionId")
    suspend fun voidTransaction(transactionId: String, reason: String, voidTime: Long)

    // --- Ledger Entries ---
    @Query("SELECT * FROM ledger_entries ORDER BY timestamp DESC")
    fun getAllLedgerEntries(): Flow<List<LedgerEntry>>

    @Query("SELECT * FROM ledger_entries WHERE transactionId = :transactionId")
    suspend fun getLedgerEntriesForTransaction(transactionId: String): List<LedgerEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<LedgerEntry>)

    @Query("DELETE FROM ledger_entries WHERE transactionId = :transactionId")
    suspend fun deleteLedgerEntriesForTransaction(transactionId: String)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLog>)

    // --- Direct Bulk Queries for Backup/Export ---
    @Query("SELECT * FROM admin_users")
    suspend fun getAdminsSync(): List<AdminUser>

    @Query("SELECT * FROM family_members")
    suspend fun getMembersSync(): List<FamilyMember>

    @Query("SELECT * FROM financial_accounts")
    suspend fun getAccountsSync(): List<FinancialAccount>

    @Query("SELECT * FROM transactions ORDER BY timestamp ASC")
    suspend fun getTransactionsSync(): List<TransactionRecord>

    @Query("SELECT * FROM ledger_entries")
    suspend fun getLedgerEntriesSync(): List<LedgerEntry>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    suspend fun getAuditLogsSync(): List<AuditLog>

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM ledger_entries")
    suspend fun clearLedgerEntries()

    @Query("DELETE FROM family_members")
    suspend fun clearMembers()

    @Query("DELETE FROM financial_accounts")
    suspend fun clearAccounts()

    @Query("DELETE FROM audit_logs")
    suspend fun clearAuditLogs()
}
