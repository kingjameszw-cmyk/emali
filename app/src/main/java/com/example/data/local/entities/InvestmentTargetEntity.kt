package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investment_targets")
data class InvestmentTargetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val title: String, // e.g. "Accumulate 10,000 Econet Shares"
    val targetType: String, // SHARE_COUNT, PORTFOLIO_VALUE, DIVIDEND_INCOME, MONTHLY_CONTRIBUTION
    val ticker: String? = null,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val targetDateMillis: Long = System.currentTimeMillis() + 365L * 24 * 3600 * 1000,
    val notes: String = ""
)
