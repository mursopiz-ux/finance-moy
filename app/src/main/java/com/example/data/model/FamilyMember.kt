package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey val id: String,
    val name: String,
    val relationship: String, // "Kepala Keluarga / Ayah", "Ibu", "Anak", "Keluarga"
    val targetSavings: Long = 0L,
    val currentBalance: Long = 0L,
    val avatarColorHex: String = "#0F6E47",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
