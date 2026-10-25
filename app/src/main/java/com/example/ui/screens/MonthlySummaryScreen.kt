package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BudgetCategoryEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserProfileEntity
import com.example.ui.components.RemainingFundsProgressBar
import com.example.ui.theme.EcoCashBlue
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.ZiGAmber
import com.example.util.CurrencyFormatter
import java.text.DateFormatSymbols
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySummaryScreen(
    transactions: List<TransactionEntity>,
    budgetCategories: List<BudgetCategoryEntity>,
    userProfile: UserProfileEntity?,
    onNavigateBack: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToTransactions: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    // Selected Month & Year
    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) } // 0-based

    val monthName = DateFormatSymbols().months[selectedMonth]

    // Compute month boundaries
    val (startMillis, endMillis) = remember(selectedYear, selectedMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.MONTH, selectedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis
        Pair(start, end)
    }

    // Filter transactions for this month
    val monthTransactions = remember(transactions, startMillis, endMillis) {
        transactions.filter { it.dateMillis in startMillis until endMillis }
    }

    // Income calculations
    val incomeTransactions = remember(monthTransactions) {
        monthTransactions.filter { it.type.equals("INCOME", ignoreCase = true) }
    }
    val totalIncome = incomeTransactions.sumOf { it.amount }

    // Expense calculations
    val expenseTransactions = remember(monthTransactions) {
        monthTransactions.filter { it.type.equals("EXPENSE", ignoreCase = true) }
    }
    val totalExpense = expenseTransactions.sumOf { it.amount }
    val totalFees = expenseTransactions.sumOf { it.fee }
    val totalOutflow = totalExpense + totalFees

    // Net Cash Flow & Savings Rate
    val netSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100.0).coerceIn(-100.0, 100.0) else 0.0

    // Expenses grouped by category
    val categoryTotals = remember(expenseTransactions) {
        expenseTransactions
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Income grouped by category
    val incomeCategoryTotals = remember(incomeTransactions) {
        incomeTransactions
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Top spending category
    val topSpendingCategory = categoryTotals.firstOrNull()
    val topCategoryName = topSpendingCategory?.first ?: "None"
    val topCategoryAmount = topSpendingCategory?.second ?: 0.0
    val topCategoryPercent = if (totalExpense > 0) (topCategoryAmount / totalExpense * 100.0) else 0.0
    val topCategoryBudgetLimit = budgetCategories.find { it.category.equals(topCategoryName, ignoreCase = true) }?.monthlyLimit ?: 0.0

    // Textual summary content for clipboard export
    val fullTextSummary = remember(
        monthName, selectedYear, totalIncome, totalExpense, netSavings, savingsRate,
        topCategoryName, topCategoryAmount, topCategoryPercent, categoryTotals
    ) {
        buildString {
            appendLine("=== Monthly Financial Summary: $monthName $selectedYear ===")
            appendLine("Author: ${userProfile?.userName ?: "Account Holder"}")
            appendLine()
            appendLine("1. TOTAL INCOME VS. EXPENSES BREAKDOWN:")
            appendLine("• Total Earned: ${CurrencyFormatter.formatUsd(totalIncome)} across ${incomeTransactions.size} deposits.")
            appendLine("• Total Spent: ${CurrencyFormatter.formatUsd(totalExpense)} across ${expenseTransactions.size} expenses (Fees: ${CurrencyFormatter.formatUsd(totalFees)}).")
            if (netSavings >= 0) {
                appendLine("• Net Position: +${CurrencyFormatter.formatUsd(netSavings)} surplus (Savings Rate: ${String.format("%.1f", savingsRate)}%). Income exceeded expenditures.")
            } else {
                appendLine("• Net Position: -${CurrencyFormatter.formatUsd(-netSavings)} deficit. Outflows exceeded monthly earnings.")
            }
            appendLine()
            appendLine("2. TOP SPENDING CATEGORY:")
            if (topSpendingCategory != null) {
                appendLine("• HIGHEST OUTFLOW: $topCategoryName with ${CurrencyFormatter.formatUsd(topCategoryAmount)}.")
                appendLine("• It accounted for ${String.format("%.1f", topCategoryPercent)}% of all living expenses this month.")
                if (topCategoryBudgetLimit > 0) {
                    val diff = topCategoryAmount - topCategoryBudgetLimit
                    if (diff > 0) {
                        appendLine("• Budget Comparison: Exceeded planned limit of ${CurrencyFormatter.formatUsd(topCategoryBudgetLimit)} by ${CurrencyFormatter.formatUsd(diff)}.")
                    } else {
                        appendLine("• Budget Comparison: Within planned limit of ${CurrencyFormatter.formatUsd(topCategoryBudgetLimit)} (remaining: ${CurrencyFormatter.formatUsd(-diff)}).")
                    }
                }
            } else {
                appendLine("• No expenses recorded for $monthName $selectedYear.")
            }
            appendLine()
            appendLine("3. CATEGORY RANKINGS:")
            categoryTotals.forEachIndexed { idx, (cat, amt) ->
                val p = if (totalExpense > 0) (amt / totalExpense * 100.0) else 0.0
                appendLine("${idx + 1}. $cat: ${CurrencyFormatter.formatUsd(amt)} (${String.format("%.1f", p)}%)")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Monthly Financial Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$monthName $selectedYear Breakdown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("monthly_summary_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Monthly Financial Summary", fullTextSummary))
                            Toast.makeText(context, "Monthly Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("copy_monthly_summary_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Summary")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("monthly_summary_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Month-Year Navigation Bar
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (selectedMonth == 0) {
                                    selectedMonth = 11
                                    selectedYear -= 1
                                } else {
                                    selectedMonth -= 1
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$monthName $selectedYear",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (selectedMonth == 11) {
                                    selectedMonth = 0
                                    selectedYear += 1
                                } else {
                                    selectedMonth += 1
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                        }
                    }
                }
            }

            // 2. HIGHLIGHT HERO: Top Spending Category of the Month
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (topSpendingCategory != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ZiGAmber.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = ZiGAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Top Spending Category",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$monthName $selectedYear Peak Outflow",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            if (topSpendingCategory != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "${String.format("%.1f", topCategoryPercent)}% of Expenses",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (topSpendingCategory != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            getCategoryIcon(topCategoryName),
                                            contentDescription = topCategoryName,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = topCategoryName,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${expenseTransactions.count { it.category == topCategoryName }} transactions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = CurrencyFormatter.formatUsd(topCategoryAmount),
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = FinanceExpenseRed
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Highlight Text Narrative
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                    .padding(12.dp)
                            ) {
                                val budgetNarrative = if (topCategoryBudgetLimit > 0) {
                                    val overBy = topCategoryAmount - topCategoryBudgetLimit
                                    if (overBy > 0) {
                                        "Exceeded your monthly planned limit of ${CurrencyFormatter.formatUsd(topCategoryBudgetLimit)} by ${CurrencyFormatter.formatUsd(overBy)} (${String.format("%.1f", (overBy / topCategoryBudgetLimit) * 100)}% over budget)."
                                    } else {
                                        "Stayed within your planned limit of ${CurrencyFormatter.formatUsd(topCategoryBudgetLimit)}, leaving ${CurrencyFormatter.formatUsd(-overBy)} unspent."
                                    }
                                } else {
                                    "No monthly ceiling was configured for $topCategoryName."
                                }

                                Text(
                                    text = "$topCategoryName was your highest expenditure in $monthName $selectedYear, accounting for ${CurrencyFormatter.formatUsd(topCategoryAmount)} (${String.format("%.1f", topCategoryPercent)}% of all spending). $budgetNarrative",
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        } else {
                            Text(
                                text = "No expenses recorded yet for $monthName $selectedYear.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. TEXTUAL BREAKDOWN: Total Income vs. Expenses Narrative
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Textual Breakdown: Income vs. Expenses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FinanceIncomeGreen.copy(alpha = 0.12f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = FinanceIncomeGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Total Income", style = MaterialTheme.typography.labelSmall, color = FinanceIncomeGreen, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyFormatter.formatUsd(totalIncome),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinanceIncomeGreen
                                    )
                                    Text(
                                        text = "${incomeTransactions.size} inflow transactions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FinanceExpenseRed.copy(alpha = 0.12f))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = FinanceExpenseRed, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Total Expenses", style = MaterialTheme.typography.labelSmall, color = FinanceExpenseRed, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyFormatter.formatUsd(totalExpense),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinanceExpenseRed
                                    )
                                    Text(
                                        text = "${expenseTransactions.size} outflow transactions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Paragraph 1: Income Analysis
                        val incomeNarrative = if (totalIncome > 0) {
                            val topIncomeSources = incomeCategoryTotals.take(2).joinToString(" and ") { "${it.first} (${CurrencyFormatter.formatUsd(it.second)})" }
                            "In $monthName $selectedYear, your accounts received a total inflow of ${CurrencyFormatter.formatUsd(totalIncome)} across ${incomeTransactions.size} records, primarily powered by $topIncomeSources."
                        } else {
                            "No income inflows were deposited during $monthName $selectedYear."
                        }

                        Text(
                            text = incomeNarrative,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Paragraph 2: Expense Analysis
                        val expenseNarrative = if (totalExpense > 0) {
                            val topThreeCats = categoryTotals.take(3).joinToString(", ") { "${it.first} (${CurrencyFormatter.formatUsd(it.second)})" }
                            "Your living expenses and bill payments totaled ${CurrencyFormatter.formatUsd(totalExpense)} across ${expenseTransactions.size} transactions. Your leading expenditure items were $topThreeCats."
                        } else {
                            "Zero expenditures have been logged for this period."
                        }

                        Text(
                            text = expenseNarrative,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Paragraph 3: Net Cashflow Result & Savings
                        val netNarrative = if (netSavings >= 0) {
                            "Net Cashflow Verdict: You recorded a healthy financial surplus of +${CurrencyFormatter.formatUsd(netSavings)}, achieving an overall monthly savings rate of ${String.format("%.1f", savingsRate)}%. You kept ${String.format("%.0f", savingsRate)} cents of every dollar earned, providing strong momentum for your savings and project reserves."
                        } else {
                            "Net Cashflow Verdict: You experienced a monthly cash deficit of -${CurrencyFormatter.formatUsd(-netSavings)}. Spending outpaced deposits by ${CurrencyFormatter.formatUsd(-netSavings)}, drawing down on existing wallet and bank balances."
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (netSavings >= 0) FinanceIncomeGreen.copy(alpha = 0.08f)
                                    else FinanceExpenseRed.copy(alpha = 0.08f)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = netNarrative,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                lineHeight = 21.sp,
                                color = if (netSavings >= 0) FinanceIncomeGreen else FinanceExpenseRed
                            )
                        }

                        if (totalFees > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Transaction Fees & Levies: You paid ${CurrencyFormatter.formatUsd(totalFees)} in mobile money (EcoCash) and IMTT transaction charges during $monthName, representing ${String.format("%.1f", (totalFees / (totalExpense + totalFees)) * 100)}% of your gross cash outflows.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 4. Detailed Category Breakdown with Textual Commentary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category Breakdown & Rankings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToBudget) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Budget Limits")
                    }
                }
            }

            if (categoryTotals.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No category expenses logged for $monthName $selectedYear.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(categoryTotals) { index, (category, amount) ->
                    val pct = if (totalExpense > 0) (amount / totalExpense * 100.0) else 0.0
                    val limit = budgetCategories.find { it.category.equals(category, ignoreCase = true) }?.monthlyLimit ?: 0.0
                    val isTopCategory = index == 0

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTopCategory) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                            else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isTopCategory) ZiGAmber else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isTopCategory) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        getCategoryIcon(category),
                                        contentDescription = null,
                                        tint = if (isTopCategory) ZiGAmber else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = CurrencyFormatter.formatUsd(amount),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinanceExpenseRed
                                    )
                                    Text(
                                        text = "${String.format("%.1f", pct)}% of total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Textual sentence per category
                            val commentary = if (limit > 0) {
                                val rem = limit - amount
                                if (rem >= 0) {
                                    "Within budget limit of ${CurrencyFormatter.formatUsd(limit)} (${CurrencyFormatter.formatUsd(rem)} remaining)."
                                } else {
                                    "Exceeded budget limit of ${CurrencyFormatter.formatUsd(limit)} by ${CurrencyFormatter.formatUsd(-rem)}."
                                }
                            } else {
                                "Accounted for ${String.format("%.1f", pct)}% of monthly expenditure."
                            }

                            Text(
                                text = "• $commentary",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (limit > 0 && amount > limit) FinanceExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 5. Action Buttons at Bottom
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ledger History")
                    }

                    Button(
                        onClick = onNavigateToBudget,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Budget Planner")
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "groceries", "supermarket", "food", "provisions" -> Icons.Default.ShoppingCart
        "transport & fuel", "fuel", "diesel", "petrol", "transport", "commute", "car" -> Icons.Default.DirectionsCar
        "food & dining", "dining", "restaurant", "lunch", "dinner" -> Icons.Default.Restaurant
        "electricity", "zesa", "tokens", "power" -> Icons.Default.Bolt
        "airtime & data", "airtime", "data", "internet", "econet" -> Icons.Default.PhoneAndroid
        "rent & housing", "rent", "housing", "rates", "accommodation" -> Icons.Default.Home
        "school fees", "tuition", "education", "books" -> Icons.Default.School
        "healthcare", "pharmacy", "medical", "doctor" -> Icons.Default.LocalHospital
        else -> Icons.Default.Payments
    }
}
