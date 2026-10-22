package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_tasks")
data class TodoTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val title: String,
    val description: String = "",
    val category: String = "GENERAL", // BILLS, ECOCASH, CTRADE, SAVINGS, PROJECT, GENERAL
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val dueDateMillis: Long = System.currentTimeMillis() + 86400000L,
    val isCompleted: Boolean = false,
    val linkedProjectId: Long? = null,
    val linkedGoalId: Long? = null,
    val estimatedAmount: Double = 0.0,
    val createdAtMillis: Long = System.currentTimeMillis()
)
