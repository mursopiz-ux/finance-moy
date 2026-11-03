package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val adminId: String,
    val adminName: String,
    val action: String, // CREATE_TRANSACTION, VOID_TRANSACTION, MEMBER_UPDATE, CORRECTION, BACKUP_EXPORT, BACKUP_RESTORE
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
