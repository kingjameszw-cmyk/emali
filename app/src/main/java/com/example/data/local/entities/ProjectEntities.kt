package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val name: String, // e.g. "Poultry Farm Project", "Solar Power 5kVA"
    val description: String = "",
    val category: String = "AGRICULTURE", // AGRICULTURE, ENERGY, REAL_ESTATE, BUSINESS, PERSONAL
    val totalBudget: Double = 0.0,
    val startDateMillis: Long = System.currentTimeMillis(),
    val targetDateMillis: Long = System.currentTimeMillis() + 180L * 24 * 3600 * 1000,
    val status: String = "IN_PROGRESS", // PLANNING, IN_PROGRESS, COMPLETED
    val priority: String = "HIGH",
    val coverIcon: String = "agriculture"
)

@Entity(
    tableName = "project_budget_items",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class ProjectBudgetItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val projectId: Long,
    val name: String, // e.g. "Construction & Fencing", "Livestock", "Feed"
    val allocatedAmount: Double,
    val spentAmount: Double = 0.0
)

@Entity(
    tableName = "project_milestones",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class ProjectMilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val projectId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDateMillis: Long = System.currentTimeMillis() + 30L * 24 * 3600 * 1000
)
