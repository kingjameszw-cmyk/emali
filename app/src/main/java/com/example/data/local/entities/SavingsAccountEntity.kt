package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_accounts")
data class SavingsAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val name: String, // e.g. "Emergency Fund", "House Deposit", "Holiday Fund"
    val category: String = "EMERGENCY",
    val currentBalance: Double = 0.0,
    val targetBalance: Double = 0.0,
    val targetDateMillis: Long = System.currentTimeMillis() + 180L * 24 * 3600 * 1000,
    val colorHex: String = "#10B981",
    val savingsRule: String = "$150 / month",
    val createdAtMillis: Long = System.currentTimeMillis()
)
