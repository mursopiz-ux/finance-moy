package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_users")
data class AdminUser(
    @PrimaryKey val id: String,
    val name: String,
    val role: String, // "SUPER_ADMIN", "ADMIN", "MEMBER_VIEWER"
    val pin: String = "1234",
    val email: String = "",
    val isActiveCurrent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
