package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.TransactionEntity
import com.example.ui.MainViewModel
import com.example.ui.components.CategoryColors
import com.example.ui.components.ExpenseCategoryChart
import com.example.ui.theme.FinanceExpenseRed
import com.example.util.CurrencyFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedExpensesScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onAddNewExpense: () -> Unit
) {
    // Pull saved expenses dynamically from the Room database via StateFlow
    val savedExpenses by viewModel.savedExpenses.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val usdToZwlRate = userProfile?.usdToZigRate ?: 26.50

    SavedExpensesContent(
        savedExpenses = savedExpenses,
        accounts = accounts,
        usdToZwlRate = usdToZwlRate,
        onDeleteExpense = { id -> viewModel.deleteTransaction(id) },
        onNavigateBack = onNavigateBack,
        onAddNewExpense = onAddNewExpense
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedExpensesContent(
    savedExpenses: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    usdToZwlRate: Double = 26.50,
    onDeleteExpense: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onAddNewExpense: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedSortOrder by remember { mutableStateOf("NEWEST") } // NEWEST, HIGHEST, LOWEST
    var showVisualBreakdown by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var expenseDetailToView by remember { mutableStateOf<TransactionEntity?>(null) }

    // Filter and Sort the saved expenses pulled from Room
    val filteredExpenses = remember(savedExpenses, searchQuery, selectedCategoryFilter, selectedSortOrder) {
        savedExpenses
            .filter { tx ->
                val matchesSearch = searchQuery.isBlank() ||
                        tx.category.contains(searchQuery, ignoreCase = true) ||
                        tx.recipientOrSender.contains(searchQuery, ignoreCase = true) ||
                        tx.description.contains(searchQuery, ignoreCase = true) ||
                        tx.reference.contains(searchQuery, ignoreCase = true)

                val matchesCategory = selectedCategoryFilter == "ALL" ||
                        tx.category.equals(selectedCategoryFilter, ignoreCase = true)

                matchesSearch && matchesCategory
            }
            .let { list ->
                when (selectedSortOrder) {
                    "HIGHEST" -> list.sortedByDescending { it.amount }
                    "LOWEST" -> list.sortedBy { it.amount }
                    else -> list.sortedByDescending { it.dateMillis }
                }
            }
    }

    val totalSpentUsd = remember(savedExpenses) { savedExpenses.sumOf { it.amount + it.fee } }
    val totalSpentZwl = remember(totalSpentUsd, usdToZwlRate) { totalSpentUsd * usdToZwlRate }

    val distinctCategories = remember(savedExpenses) {
        listOf("ALL") + savedExpenses.map { it.category }.distinct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Saved Expenses",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Room Database Records (${savedExpenses.size} items)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("saved_expenses_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showVisualBreakdown = !showVisualBreakdown },
                        modifier = Modifier.testTag("toggle_chart_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Toggle Category Breakdown Chart",
                            tint = if (showVisualBreakdown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewExpense,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_new_expense")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Expense")
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("saved_expenses_lazy_column"),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Total Overview Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL SAVED EXPENSES",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = "${savedExpenses.size} Records in Room",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = CurrencyFormatter.formatUsd(totalSpentUsd),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "≈ ZWL ${String.format(Locale.US, "%,.2f", totalSpentZwl)} (@ 1 USD = $usdToZwlRate ZWL)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // 2. Expandable Visual Breakdown Chart
            item {
                AnimatedVisibility(
                    visible = showVisualBreakdown,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ExpenseCategoryChart(expenses = savedExpenses)
                    }
                }
            }

            // 3. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search merchant, category, notes, ref...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("saved_expenses_search_input")
                )
            }

            // 4. Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    distinctCategories.forEach { category ->
                        val isSelected = selectedCategoryFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryFilter = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("filter_chip_$category")
                        )
                    }
                }
            }

            // 5. Results Counter & Sort Toggle
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredExpenses.size} of ${savedExpenses.size} expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            "NEWEST" to "Newest",
                            "HIGHEST" to "Highest $"
                        ).forEach { (key, label) ->
                            val isSelected = selectedSortOrder == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedSortOrder = key }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 6. Empty State
            if (filteredExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedCategoryFilter != "ALL")
                                    "No expenses matching your filter."
                                else
                                    "No saved expenses in the Room database yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onAddNewExpense,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log First Expense")
                            }
                        }
                    }
                }
            } else {
                // 7. Saved Expenses List Items in LazyColumn
                items(filteredExpenses, key = { it.id }) { tx ->
                    val account = accounts.find { it.id == tx.accountId }
                    val catInfo = expenseCategories.find { it.name.equals(tx.category, ignoreCase = true) }
                    val icon = catInfo?.icon ?: Icons.Default.Category
                    val zwlAmount = tx.amount * usdToZwlRate

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable { expenseDetailToView = tx }
                            .testTag("saved_expense_card_${tx.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = tx.recipientOrSender.ifBlank { tx.category },
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = tx.category,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = CurrencyFormatter.formatShortDate(tx.dateMillis),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (tx.description.isNotBlank() && tx.description != tx.recipientOrSender) {
                                        Text(
                                            text = tx.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "-${CurrencyFormatter.formatUsd(tx.amount)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinanceExpenseRed
                                    )
                                    Text(
                                        text = "≈ ZWL ${String.format(Locale.US, "%,.1f", zwlAmount)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { expenseToDelete = tx },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete from Room",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Expense Detail Modal Dialog
    expenseDetailToView?.let { tx ->
        val zwlValue = tx.amount * usdToZwlRate
        AlertDialog(
            onDismissRequest = { expenseDetailToView = null },
            title = {
                Text(
                    text = "Expense Details",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Category:", fontWeight = FontWeight.SemiBold)
                        Text(tx.category)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Amount (USD):", fontWeight = FontWeight.SemiBold)
                        Text(CurrencyFormatter.formatUsd(tx.amount), color = FinanceExpenseRed, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ZWL Equivalent:", fontWeight = FontWeight.SemiBold)
                        Text("ZWL ${String.format(Locale.US, "%,.2f", zwlValue)}")
                    }
                    if (tx.fee > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Transaction Fee:", fontWeight = FontWeight.SemiBold)
                            Text("$${String.format(Locale.US, "%.2f", tx.fee)}")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Merchant / Payee:", fontWeight = FontWeight.SemiBold)
                        Text(tx.recipientOrSender.ifBlank { "N/A" })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Date & Time:", fontWeight = FontWeight.SemiBold)
                        Text(CurrencyFormatter.formatDate(tx.dateMillis))
                    }
                    if (tx.reference.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Reference / Ref ID:", fontWeight = FontWeight.SemiBold)
                            Text(tx.reference)
                        }
                    }
                    if (tx.description.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Note / Memo:", fontWeight = FontWeight.SemiBold)
                            Text(tx.description)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { expenseDetailToView = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    expenseToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete from Room Database?") },
            text = { Text("Are you sure you want to permanently delete this ${CurrencyFormatter.formatUsd(tx.amount)} expense for ${tx.category}?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(tx.id)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
