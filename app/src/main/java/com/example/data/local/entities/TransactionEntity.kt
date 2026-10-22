package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["reference"], unique = false),
        Index(value = ["dateMillis"]),
        Index(value = ["accountId"]),
        Index(value = ["projectId"]),
        Index(value = ["savingsAccountId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val accountId: Long = 1,
    val type: String, // EXPENSE, INCOME, TRANSFER, SAVINGS_DEPOSIT, SAVINGS_WITHDRAWAL, INVESTMENT_BUY, INVESTMENT_SELL, DIVIDEND
    val amount: Double,
    val fee: Double = 0.0,
    val totalDeducted: Double = amount + fee,
    val currency: String = "USD",
    val category: String,
    val recipientOrSender: String = "",
    val reference: String = "",
    val description: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val status: String = "COMPLETED",
    val projectId: Long? = null,
    val savingsAccountId: Long? = null,
    val rawStatementLine: String? = null
)
