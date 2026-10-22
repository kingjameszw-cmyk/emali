package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userName: String = "Tendai Moyo",
    val pinCode: String? = null, // 4-digit PIN for security
    val isBiometricEnabled: Boolean = false,
    val isPinRequired: Boolean = false,
    val defaultCurrency: String = "USD",
    val usdToZigRate: Double = 26.50, // 1 USD = 26.50 ZiG
    val isSetupCompleted: Boolean = true,
    val isZeroProfile: Boolean = false,
    val isCtradeLoggedIn: Boolean = false,
    val ctradeAccountNumber: String = "",
    val ctradeUsername: String = "",
    val ctradeBroker: String = "C-Trade Direct",
    val lastPricePullTimestamp: Long = 0L
)
