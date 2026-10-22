package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "financial_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val name: String,
    val description: String = "",
    val category: String = "GENERAL",
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val startDateMillis: Long = System.currentTimeMillis(),
    val targetDateMillis: Long = System.currentTimeMillis() + 365L * 24 * 3600 * 1000,
    val priority: String = "HIGH", // HIGH, MEDIUM, LOW
    val monthlyContribution: Double = 0.0,
    val status: String = "ACTIVE", // ACTIVE, PAUSED, COMPLETED
    val linkedSavingsAccountId: Long? = null,
    val linkedProjectId: Long? = null,
    val notes: String = ""
) {
    val remainingAmount: Double get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
    val progressPercentage: Double
        get() = if (targetAmount > 0) ((currentAmount / targetAmount) * 100.0).coerceIn(0.0, 100.0) else 0.0
}
