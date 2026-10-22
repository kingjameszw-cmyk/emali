package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val name: String,
    val type: String, // ECOCASH, CASH, BANK, INVESTMENT
    val currency: String = "USD", // USD or ZWG
    val balance: Double = 0.0,
    val accountNumber: String = "",
    val isDefault: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
