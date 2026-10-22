package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "investment_transactions")
data class InvestmentTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val ticker: String,
    val companyName: String,
    val type: String, // BUY, SELL, DIVIDEND_CASH, DIVIDEND_REINVEST
    val shares: Double,
    val pricePerShare: Double,
    val fees: Double = 0.0,
    val totalAmount: Double,
    val realizedGain: Double = 0.0,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
