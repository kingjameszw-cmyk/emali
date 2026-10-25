package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BudgetCategoryEntity
import com.example.data.local.entities.TransactionEntity
import com.example.ui.components.RemainingFundsProgressBar
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.ZiGAmber
import com.example.util.CurrencyFormatter
import com.example.util.DashboardFinancialSummary
import java.util.Calendar

val presetCategories = listOf(
    "Groceries",
    "Transport & Fuel",
    "Electricity",
    "Food & Dining",
    "Rent & Housing",
    "Airtime & Data",
    "School Fees",
    "Healthcare",
    "Shopping",
    "Entertainment",
    "Business Expense",
    "Vehicle Maintenance",
    "Water & Council Rates",
    "Other"
)

fun getCategoryIcon(category: String): ImageVector {
    val lower = category.lowercase()
    return when {
        lower.contains("groc") -> Icons.Default.ShoppingCart
        lower.contains("fuel") || lower.contains("trans") -> Icons.Default.DirectionsCar
        lower.contains("elec") || lower.contains("zesa") || lower.contains("power") -> Icons.Default.Bolt
        lower.contains("food") || lower.contains("din") -> Icons.Default.Restaurant
        lower.contains("rent") || lower.contains("house") -> Icons.Default.Home
        lower.contains("air") || lower.contains("data") || lower.contains("phone") -> Icons.Default.PhoneAndroid
        lower.contains("school") || lower.contains("fee") || lower.contains("edu") -> Icons.Default.School
        lower.contains("health") || lower.contains("pharm") || lower.contains("med") -> Icons.Default.LocalHospital
        lower.contains("shop") || lower.contains("cloth") -> Icons.Default.ShoppingBag
        lower.contains("bus") || lower.contains("work") -> Icons.Default.BusinessCenter
        else -> Icons.Default.Payments
    }
}

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
    var categoryToDelete by remember { mutableStateOf<BudgetCategoryEntity?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

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
    val isOverBudget = totalActualSpend > totalPlannedBudget && totalPlannedBudget > 0
    val overBudgetAmount = if (isOverBudget) totalActualSpend - totalPlannedBudget else 0.0
    val remainingPercentage = if (totalPlannedBudget > 0) ((totalPlannedBudget - totalActualSpend) / totalPlannedBudget * 100.0).coerceIn(0.0, 100.0) else 0.0

    // Remaining daily allowance for current month
    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val remainingDays = (daysInMonth - currentDay + 1).coerceAtLeast(1)
    val dailyBudgetRemaining = if (remainingOverallBudget > 0) remainingOverallBudget / remainingDays else 0.0

    // Filter categories
    val filteredCategories = budgetCategories.filter { cat ->
        val spent = expensesByCategory[cat.category] ?: 0.0
        val remaining = cat.monthlyLimit - spent
        val ratio = if (cat.monthlyLimit > 0) spent / cat.monthlyLimit else 0.0

        val matchesSearch = searchQuery.isBlank() || cat.category.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "CAUTION" -> ratio >= 0.8 && ratio < 1.0
            "EXCEEDED" -> ratio >= 1.0
            "HEALTHY" -> ratio < 0.8
            else -> true
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_budget_category")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Set Spending Limit")
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
            // Header Hero Card: Monthly Budget Overview & Remaining Funds Progress Bar
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("monthly_budget_overview_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Monthly Budget Tracker",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                                Text(
                                    "Track spending limits & remaining funds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isOverBudget) FinanceExpenseRed.copy(alpha = 0.15f)
                                else if (remainingPercentage <= 20.0) ZiGAmber.copy(alpha = 0.15f)
                                else FinanceIncomeGreen.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isOverBudget) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isOverBudget) FinanceExpenseRed else if (remainingPercentage <= 20.0) ZiGAmber else FinanceIncomeGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOverBudget) "Over Budget" else if (remainingPercentage <= 20.0) "Caution" else "Healthy",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isOverBudget) FinanceExpenseRed else if (remainingPercentage <= 20.0) ZiGAmber else FinanceIncomeGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Remaining Funds Display
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "TOTAL REMAINING FUNDS",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isOverBudget) "-$${String.format("%.2f", overBudgetAmount)}"
                                    else CurrencyFormatter.formatUsd(remainingOverallBudget),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 32.sp
                                    ),
                                    color = if (isOverBudget) FinanceExpenseRed else FinanceIncomeGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isOverBudget) "Spending exceeded monthly limits by ${CurrencyFormatter.formatUsd(overBudgetAmount)}"
                                    else "${String.format("%.1f", remainingPercentage)}% of your ${CurrencyFormatter.formatUsd(totalPlannedBudget)} limit remains",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Detailed Remaining Funds Progress Bar
                        RemainingFundsProgressBar(
                            remainingAmount = remainingOverallBudget,
                            totalBudget = totalPlannedBudget,
                            spentAmount = totalActualSpend,
                            barHeight = 12.dp,
                            modifier = Modifier.fillMaxWidth().testTag("overview_remaining_funds_bar")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Daily Allowance stat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$remainingDays days remaining in month",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(
                                text = "Safe to spend: $${String.format("%.2f", dailyBudgetRemaining)}/day",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Quick Category Limit Management Banner
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Limits (${budgetCategories.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("set_category_limit_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Set Limit", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Search & Filter Row
            item {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search category limits...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ALL" to "All (${budgetCategories.size})",
                            "HEALTHY" to "Healthy",
                            "CAUTION" to "Caution (<20%)",
                            "EXCEEDED" to "Exceeded"
                        ).forEach { (key, label) ->
                            val isSel = selectedFilter == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedFilter = key }
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Category Limits List
            if (filteredCategories.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (budgetCategories.isEmpty()) "No Category Budgets Set" else "No Matching Categories",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Set monthly spending limits for Groceries, Fuel, Electricity, or Dining to take control of where your money goes.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showAddDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Set First Budget Limit")
                            }
                        }
                    }
                }
            } else {
                items(filteredCategories, key = { it.id }) { cat ->
                    val spent = expensesByCategory[cat.category] ?: 0.0
                    val remaining = (cat.monthlyLimit - spent).coerceAtLeast(0.0)
                    val icon = getCategoryIcon(cat.category)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_category_item_${cat.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = cat.category,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Limit: ${CurrencyFormatter.formatUsd(cat.monthlyLimit)} / month",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { editingCategory = cat },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Limit",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { categoryToDelete = cat },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Budget",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Individual Category Remaining Funds Progress Bar
                            RemainingFundsProgressBar(
                                remainingAmount = remaining,
                                totalBudget = cat.monthlyLimit,
                                spentAmount = spent,
                                barHeight = 8.dp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Steppers to Adjust Spending Limit Instantly
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Adjust limit:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                listOf(-50, -25, +25, +50, +100).forEach { delta ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (delta > 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                val next = (cat.monthlyLimit + delta).coerceAtLeast(10.0)
                                                onSaveCategory(cat.copy(monthlyLimit = next))
                                            }
                                    ) {
                                        Text(
                                            text = if (delta > 0) "+$$delta" else "-$${-delta}",
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (delta > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Financial Rule & Allocation Insight Card
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
                                "50/30/20 Budgeting Rule",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Target 50% for essentials (groceries, electricity, transport), 30% discretionary, and 20% for savings & investments.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Set / Edit Category Budget Dialog
    if (showAddDialog || editingCategory != null) {
        var catName by remember { mutableStateOf(editingCategory?.category ?: "") }
        var limitText by remember { mutableStateOf(editingCategory?.monthlyLimit?.toString() ?: "100.00") }
        var categoryDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingCategory = null
            },
            title = {
                Text(
                    text = if (editingCategory != null) "Edit Category Spending Limit" else "Set Category Spending Limit",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Set a monthly spending limit for this category. The dashboard progress bar will track your remaining funds in realtime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selector
                    Box {
                        OutlinedTextField(
                            value = catName,
                            onValueChange = { catName = it },
                            label = { Text("Category") },
                            placeholder = { Text("e.g. Groceries, Fuel, Electricity") },
                            trailingIcon = {
                                IconButton(onClick = { categoryDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("budget_category_input")
                        )
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            presetCategories.forEach { pCat ->
                                DropdownMenuItem(
                                    text = { Text(pCat) },
                                    leadingIcon = {
                                        Icon(getCategoryIcon(pCat), contentDescription = null, modifier = Modifier.size(18.dp))
                                    },
                                    onClick = {
                                        catName = pCat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = limitText,
                        onValueChange = { limitText = it },
                        label = { Text("Monthly Spending Limit (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("budget_limit_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Quick Limit Presets:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(50, 100, 200, 350, 500).forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { limitText = preset.toString() }
                            ) {
                                Text(
                                    text = "$$preset",
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
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
                    },
                    modifier = Modifier.testTag("save_budget_category_btn")
                ) {
                    Text("Save Limit")
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

    // Delete Confirmation Dialog
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Budget Limit?") },
            text = { Text("Are you sure you want to remove the monthly spending limit for '${cat.category}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory(cat.id)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
