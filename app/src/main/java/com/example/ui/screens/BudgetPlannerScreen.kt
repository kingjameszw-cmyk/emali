package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.BudgetCategoryEntity
import com.example.data.local.entities.TransactionEntity
import com.example.ui.components.ProgressBarWithTarget
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.ZiGAmber
import com.example.util.CurrencyFormatter
import com.example.util.DashboardFinancialSummary

@Composable
fun BudgetPlannerScreen(
    summary: DashboardFinancialSummary,
    budgetCategories: List<BudgetCategoryEntity>,
    transactions: List<TransactionEntity>,
    onSaveCategory: (BudgetCategoryEntity) -> Unit,
    onDeleteCategory: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<BudgetCategoryEntity?>(null) }

    // Calculate actual expenses per category for the current month
    val expensesByCategory = transactions
        .filter { it.type == "EXPENSE" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val totalPlannedBudget = budgetCategories.sumOf { it.monthlyLimit }
    val totalActualSpend = budgetCategories.sumOf {
        expensesByCategory[it.category] ?: 0.0
    }
    val remainingOverallBudget = (totalPlannedBudget - totalActualSpend).coerceAtLeast(0.0)
    val overallProgress = if (totalPlannedBudget > 0) (totalActualSpend / totalPlannedBudget).toFloat() else 0f

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_budget_category")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget Category")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("budget_planner_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Hero Card: Monthly Budget Overview
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Monthly Budget Planner",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    "USD Budget",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Planned Limit", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    CurrencyFormatter.formatUsd(totalPlannedBudget),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Column {
                                Text("Actual Spent", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    CurrencyFormatter.formatUsd(totalActualSpend),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (overallProgress >= 1f) FinanceExpenseRed else FinanceIncomeGreen
                                )
                            }
                            Column {
                                Text("Remaining", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    CurrencyFormatter.formatUsd(remainingOverallBudget),
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        ProgressBarWithTarget(
                            progress = overallProgress,
                            targetLabel = if (overallProgress >= 1f) "Budget Exceeded!" else "${String.format("%.1f", (1f - overallProgress) * 100)}% Available",
                            currentLabel = "${String.format("%.1f", overallProgress * 100)}% Used",
                            color = if (overallProgress >= 1f) FinanceExpenseRed else if (overallProgress >= 0.8f) ZiGAmber else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Zero-Based Budgeting Insight Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "50/30/20 Allocation Rule",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Target 50% for essentials (groceries, electricity, transport), 30% discretionary, and 20% for savings & C-Trade investments.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Budget Limits (${budgetCategories.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+ Add Category",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.clickable { showAddDialog = true }
                    )
                }
            }

            // Category Limits List
            items(budgetCategories, key = { it.id }) { cat ->
                val spent = expensesByCategory[cat.category] ?: 0.0
                val ratio = if (cat.monthlyLimit > 0) (spent / cat.monthlyLimit).toFloat() else 0f
                val remaining = (cat.monthlyLimit - spent).coerceAtLeast(0.0)

                val statusColor = when {
                    ratio >= 1.0f -> FinanceExpenseRed
                    ratio >= 0.8f -> ZiGAmber
                    else -> MaterialTheme.colorScheme.primary
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    cat.category,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Limit: ${CurrencyFormatter.formatUsd(cat.monthlyLimit)}/month",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        CurrencyFormatter.formatUsd(spent),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = statusColor
                                    )
                                    Text(
                                        "Left: ${CurrencyFormatter.formatUsd(remaining)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { editingCategory = cat },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit Limit",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ProgressBarWithTarget(
                            progress = ratio,
                            targetLabel = when {
                                ratio >= 1.0f -> "LIMIT EXCEEDED"
                                ratio >= 0.8f -> "80% ALERT"
                                else -> "On Track"
                            },
                            currentLabel = "${String.format("%.1f", ratio * 100)}% Spent",
                            color = statusColor
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Category Dialog
    if (showAddDialog || editingCategory != null) {
        var catName by remember { mutableStateOf(editingCategory?.category ?: "") }
        var limitText by remember { mutableStateOf(editingCategory?.monthlyLimit?.toString() ?: "100") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingCategory = null
            },
            title = {
                Text(if (editingCategory != null) "Edit Category Budget" else "New Category Budget")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = { Text("Category Name (e.g. Groceries, Fuel, Electricity)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        label = { Text("Monthly Budget Limit (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = limitText.toDoubleOrNull() ?: 100.0
                        if (catName.isNotBlank() && limit > 0) {
                            onSaveCategory(
                                BudgetCategoryEntity(
                                    id = editingCategory?.id ?: 0L,
                                    category = catName.trim(),
                                    monthlyLimit = limit
                                )
                            )
                            showAddDialog = false
                            editingCategory = null
                        }
                    }
                ) {
                    Text("Save Budget")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAddDialog = false
                        editingCategory = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
