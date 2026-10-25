package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.BudgetCategoryEntity
import com.example.data.local.entities.TransactionEntity
import com.example.ui.components.ExpenseCategoryChart
import com.example.ui.theme.EcoCashBlue
import com.example.ui.theme.FinanceExpenseRed
import com.example.util.CurrencyFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ExpenseCategoryInfo(
    val name: String,
    val icon: ImageVector
)

val expenseCategories = listOf(
    ExpenseCategoryInfo("Groceries", Icons.Default.ShoppingCart),
    ExpenseCategoryInfo("Transport & Fuel", Icons.Default.LocalGasStation),
    ExpenseCategoryInfo("Electricity & ZETDC", Icons.Default.ElectricBolt),
    ExpenseCategoryInfo("Food & Dining", Icons.Default.Restaurant),
    ExpenseCategoryInfo("Airtime & Data", Icons.Default.PhoneAndroid),
    ExpenseCategoryInfo("Rent & Housing", Icons.Default.Home),
    ExpenseCategoryInfo("Shopping", Icons.Default.ShoppingBag),
    ExpenseCategoryInfo("Healthcare", Icons.Default.MedicalServices),
    ExpenseCategoryInfo("School Fees", Icons.Default.School),
    ExpenseCategoryInfo("Entertainment", Icons.Default.Movie),
    ExpenseCategoryInfo("Other", Icons.Default.Category)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    accounts: List<AccountEntity>,
    expenses: List<TransactionEntity>,
    budgetCategories: List<BudgetCategoryEntity> = emptyList(),
    initialExchangeRate: Double = 26.50,
    onSaveExpense: (TransactionEntity) -> Unit,
    onDeleteExpense: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSavedExpenses: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Input Form, 1 = History & Breakdown
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Currency Converter States: USD vs ZWL
    var selectedCurrency by remember { mutableStateOf("USD") } // "USD" or "ZWL"
    var exchangeRate by remember { mutableDoubleStateOf(initialExchangeRate) }
    var showRateDialog by remember { mutableStateOf(false) }

    // Form States
    var amountInput by remember { mutableStateOf("") }
    var feeInput by remember { mutableStateOf("0.00") }
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var customCategoryInput by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedAccountId by remember { mutableLongStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var payeeInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var expenseToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    // Real-time conversion calculations
    val numericAmount = amountInput.toDoubleOrNull() ?: 0.0
    val convertedAmount = remember(numericAmount, selectedCurrency, exchangeRate) {
        if (selectedCurrency == "USD") {
            numericAmount * exchangeRate // to ZWL
        } else {
            if (exchangeRate > 0) numericAmount / exchangeRate else 0.0 // to USD
        }
    }

    // Quick calculations
    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayExpenses = expenses.filter { it.type == "EXPENSE" && it.dateMillis >= todayStartMillis }
    val todayTotalSpent = todayExpenses.sumOf { it.amount + it.fee }
    val allExpensesOnly = expenses.filter { it.type == "EXPENSE" }
    val monthlyTotalSpent = allExpensesOnly.sumOf { it.amount + it.fee }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Expense Tracker",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Track expenses • USD & ZWL converter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("expense_tracker_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard"
                        )
                    }
                },
                actions = {
                    if (onNavigateToSavedExpenses != null) {
                        TextButton(
                            onClick = onNavigateToSavedExpenses,
                            modifier = Modifier.testTag("top_bar_all_saved_expenses_btn")
                        ) {
                            Text("Room DB", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Summary Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Spent Today",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatUsd(todayTotalSpent),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "≈ ZWL ${String.format(Locale.US, "%,.1f", todayTotalSpent * exchangeRate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Monthly Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = CurrencyFormatter.formatUsd(monthlyTotalSpent),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = FinanceExpenseRed
                        )
                        Text(
                            text = "${allExpensesOnly.size} total expenses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Tabs: Form vs History & Visual Breakdown
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Input Expense Form", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_input_expense")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Visual Breakdown", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_expense_history")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTab == 0) {
                // Input Form with Currency Converter
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. AMOUNT & CURRENCY CONVERTER SECTION
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1. Amount & Currency Converter",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Currency Toggle: USD vs ZWL
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(modifier = Modifier.padding(3.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (selectedCurrency == "USD") MaterialTheme.colorScheme.primary else Color.Transparent,
                                            modifier = Modifier
                                                .clickable { selectedCurrency = "USD" }
                                                .testTag("currency_toggle_usd")
                                        ) {
                                            Text(
                                                text = "USD ($)",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (selectedCurrency == "USD") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (selectedCurrency == "ZWL") MaterialTheme.colorScheme.primary else Color.Transparent,
                                            modifier = Modifier
                                                .clickable { selectedCurrency = "ZWL" }
                                                .testTag("currency_toggle_zwl")
                                        ) {
                                            Text(
                                                text = "ZWL / ZiG",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (selectedCurrency == "ZWL") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Exchange Rate Pill / Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Exchange Rate:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(25.0, 26.50, 28.0, 30.0).forEach { r ->
                                        val isCurrent = exchangeRate == r
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isCurrent) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { exchangeRate = r }
                                        ) {
                                            Text(
                                                text = "$r",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isCurrent) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { showRateDialog = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CurrencyExchange, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Custom", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Main Amount Input
                            OutlinedTextField(
                                value = amountInput,
                                onValueChange = {
                                    amountInput = it
                                    amountError = null
                                },
                                label = { Text("Amount ($selectedCurrency)") },
                                placeholder = { Text("0.00") },
                                prefix = {
                                    Text(
                                        text = if (selectedCurrency == "USD") "$ " else "ZWL ",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = amountError != null,
                                supportingText = {
                                    if (amountError != null) {
                                        Text(text = amountError!!, color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Enter value in $selectedCurrency")
                                    }
                                },
                                trailingIcon = {
                                    if (amountInput.isNotEmpty()) {
                                        IconButton(onClick = { amountInput = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear amount")
                                        }
                                    }
                                },
                                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("expense_amount_input")
                            )

                            // Live Conversion Preview Banner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CurrencyExchange,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (selectedCurrency == "USD")
                                                "Equivalent: ZWL ${String.format(Locale.US, "%,.2f", convertedAmount)}"
                                            else
                                                "Equivalent: $ ${String.format(Locale.US, "%.2f", convertedAmount)} USD",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Text(
                                        text = "@ $exchangeRate",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick preset chips (currency adaptive)
                            Text(
                                text = "Quick Presets ($selectedCurrency):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val presets = if (selectedCurrency == "USD") {
                                    listOf(5.0, 10.0, 20.0, 50.0, 100.0)
                                } else {
                                    listOf(100.0, 250.0, 500.0, 1000.0, 2500.0)
                                }

                                presets.forEach { preset ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.clickable {
                                            val current = amountInput.toDoubleOrNull() ?: 0.0
                                            val updated = current + preset
                                            amountInput = if (selectedCurrency == "USD") {
                                                String.format(Locale.US, "%.2f", updated)
                                            } else {
                                                String.format(Locale.US, "%.0f", updated)
                                            }
                                            amountError = null
                                        }
                                    ) {
                                        Text(
                                            text = if (selectedCurrency == "USD") "+$${preset.toInt()}" else "+${preset.toInt()} ZWL",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { amountInput = "" }
                                ) {
                                    Text(
                                        text = "Reset",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. CATEGORY SELECTION SECTION
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2. Expense Category",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = selectedCategory,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Scrollable Category FilterChips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                expenseCategories.forEach { cat ->
                                    val isSelected = selectedCategory == cat.name
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedCategory = cat.name },
                                        label = { Text(cat.name) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = cat.icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.testTag("category_chip_${cat.name.replace(" ", "_")}")
                                    )
                                }
                            }

                            if (selectedCategory == "Other") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customCategoryInput,
                                    onValueChange = { customCategoryInput = it },
                                    label = { Text("Specify Custom Category") },
                                    placeholder = { Text("e.g. Garden supplies, Pet care...") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // 3. DATE SELECTION SECTION
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "3. Transaction Date",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Date Shortcuts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val isToday = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                                    .get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

                                val yesterdayMillis = remember {
                                    Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, -1)
                                    }.timeInMillis
                                }
                                val isYesterday = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                                    .get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().apply { timeInMillis = yesterdayMillis }.get(Calendar.DAY_OF_YEAR)

                                FilterChip(
                                    selected = isToday,
                                    onClick = { selectedDateMillis = System.currentTimeMillis() },
                                    label = { Text("Today") },
                                    leadingIcon = if (isToday) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null,
                                    modifier = Modifier.weight(1f)
                                )

                                FilterChip(
                                    selected = isYesterday,
                                    onClick = { selectedDateMillis = yesterdayMillis },
                                    label = { Text("Yesterday") },
                                    leadingIcon = if (isYesterday) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedButton(
                                    onClick = { showDatePickerDialog = true },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("pick_custom_date_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Pick Date",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pick Date", maxLines = 1)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Date Display Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePickerDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        val displayFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
                                        Text(
                                            text = displayFormat.format(Date(selectedDateMillis)),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                        )
                                    }
                                    Text(
                                        text = "Change",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // 4. ADDITIONAL DETAILS (Account, Payee, Fee)
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "4. Payment Source & Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Account selector
                            Text(
                                text = "Payment Account:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.forEach { acc ->
                                    val isSelected = selectedAccountId == acc.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedAccountId = acc.id },
                                        label = { Text("${acc.name} (${acc.currency})") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = if (acc.type == "ECOCASH") EcoCashBlue else MaterialTheme.colorScheme.secondary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = payeeInput,
                                onValueChange = { payeeInput = it },
                                label = { Text("Merchant / Payee (Optional)") },
                                placeholder = { Text("e.g. OK Supermarket, ZETDC, Total Avondale") },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("expense_payee_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = feeInput,
                                    onValueChange = { feeInput = it },
                                    label = { Text("Fee ($selectedCurrency)") },
                                    placeholder = { Text("0.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = notesInput,
                                    onValueChange = { notesInput = it },
                                    label = { Text("Note / Memo") },
                                    placeholder = { Text("e.g. Weekly supplies") },
                                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                                    modifier = Modifier.weight(1.5f)
                                )
                            }
                        }
                    }

                    // SAVE EXPENSE BUTTON
                    Button(
                        onClick = {
                            val rawAmount = amountInput.toDoubleOrNull()
                            if (rawAmount == null || rawAmount <= 0.0) {
                                amountError = "Please enter a valid amount greater than 0"
                                return@Button
                            }

                            // Calculate final amount in USD for canonical storage
                            val amountInUsd = if (selectedCurrency == "USD") {
                                rawAmount
                            } else {
                                rawAmount / exchangeRate
                            }

                            val rawFee = feeInput.toDoubleOrNull() ?: 0.0
                            val feeInUsd = if (selectedCurrency == "USD") rawFee else rawFee / exchangeRate

                            val finalCategory = if (selectedCategory == "Other" && customCategoryInput.isNotBlank()) {
                                customCategoryInput.trim()
                            } else {
                                selectedCategory
                            }

                            val convertedDesc = if (selectedCurrency == "ZWL") {
                                "ZWL ${String.format(Locale.US, "%,.2f", rawAmount)} (@ $exchangeRate). ${notesInput.ifBlank { "" }}".trim()
                            } else {
                                notesInput.ifBlank { "Logged expense: $finalCategory" }
                            }

                            val tx = TransactionEntity(
                                type = "EXPENSE",
                                amount = amountInUsd,
                                fee = feeInUsd,
                                totalDeducted = amountInUsd + feeInUsd,
                                currency = "USD",
                                category = finalCategory,
                                recipientOrSender = payeeInput.ifBlank { finalCategory },
                                reference = "EXP-" + System.currentTimeMillis().toString().takeLast(6),
                                description = convertedDesc,
                                dateMillis = selectedDateMillis,
                                accountId = selectedAccountId
                            )

                            onSaveExpense(tx)

                            // Clear form
                            amountInput = ""
                            feeInput = "0.00"
                            payeeInput = ""
                            notesInput = ""
                            customCategoryInput = ""
                            amountError = null

                            scope.launch {
                                val toastMsg = if (selectedCurrency == "USD") {
                                    "Saved $${String.format(Locale.US, "%.2f", rawAmount)} for $finalCategory"
                                } else {
                                    "Saved ZWL ${String.format(Locale.US, "%,.0f", rawAmount)} (≈ $${String.format(Locale.US, "%.2f", amountInUsd)}) for $finalCategory"
                                }
                                snackbarHostState.showSnackbar(toastMsg)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_expense_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Expense",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                // Visual Breakdown & Saved Expenses Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    // Visual Breakdown Chart
                    item {
                        ExpenseCategoryChart(expenses = allExpensesOnly)
                    }

                    if (onNavigateToSavedExpenses != null) {
                        item {
                            Button(
                                onClick = onNavigateToSavedExpenses,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("view_room_saved_expenses_btn"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Full Saved Expenses (Room DB)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Recent Saved Expenses (${allExpensesOnly.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(allExpensesOnly, key = { it.id }) { tx ->
                        val catInfo = expenseCategories.find { it.name.equals(tx.category, ignoreCase = true) }
                        val icon = catInfo?.icon ?: Icons.Default.Category
                        val zwlVal = tx.amount * exchangeRate

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_item_${tx.id}")
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
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = tx.recipientOrSender.ifBlank { tx.category },
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${tx.category} • ${CurrencyFormatter.formatShortDate(tx.dateMillis)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (tx.description.isNotBlank() && tx.description != tx.recipientOrSender) {
                                            Text(
                                                text = tx.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            text = "≈ ZWL ${String.format(Locale.US, "%,.1f", zwlVal)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { expenseToDelete = tx },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete expense",
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
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Custom Exchange Rate Dialog
    if (showRateDialog) {
        var rateInputText by remember { mutableStateOf(exchangeRate.toString()) }
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Set Exchange Rate (ZWL per 1 USD)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter current market rate for 1 USD to ZWL:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rateInputText,
                        onValueChange = { rateInputText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        label = { Text("Rate (ZWL)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = rateInputText.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            exchangeRate = parsed
                        }
                        showRateDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    expenseToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to remove this ${CurrencyFormatter.formatUsd(tx.amount)} expense for ${tx.category}?") },
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
