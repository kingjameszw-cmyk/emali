package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_categories")
data class BudgetCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val category: String,
    val monthlyLimit: Double,
    val currency: String = "USD",
    val alertThresholdPercent: Double = 100.0,
    val alertEnabled: Boolean = true
)
