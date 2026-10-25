package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.BudgetCategoryEntity
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.HoldingEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.SavingsAccountEntity
import com.example.data.local.entities.TodoTaskEntity
import com.example.data.local.entities.TransactionEntity
import com.example.ui.AppScreen
import com.example.ui.DashboardCardId
import com.example.ui.components.AddAccountDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.CurrencyBadge
import com.example.ui.components.EditAccountDialog
import com.example.ui.components.EditExchangeRateDialog
import com.example.ui.components.EditStockCardDialog
import com.example.ui.components.EditTransactionDialog
import com.example.ui.components.ProgressBarWithTarget
import com.example.ui.components.RemainingFundsProgressBar
import com.example.ui.components.QuickAddTodoDialog
import com.example.ui.components.QuickBudgetEditDialog
import com.example.ui.components.QuickDepositDialog
import com.example.ui.components.SegmentedTimeFilter
import com.example.ui.components.StatCard
import com.example.ui.theme.EcoCashBlue
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.ZiGAmber
import com.example.util.CurrencyFormatter
import com.example.util.DashboardFinancialSummary

@Composable
fun DashboardScreen(
    summary: DashboardFinancialSummary,
    accounts: List<AccountEntity>,
    recentTransactions: List<TransactionEntity>,
    holdings: List<HoldingEntity>,
    goals: List<GoalEntity>,
    projects: List<ProjectEntity>,
    budgetCategories: List<BudgetCategoryEntity>,
    todoTasks: List<TodoTaskEntity>,
    savingsAccounts: List<SavingsAccountEntity>,
    selectedPeriod: String,
    usdToZigRate: Double,
    cardOrder: List<DashboardCardId>,
    isCustomizingLayout: Boolean,
    onToggleCustomizeLayout: () -> Unit,
    onMoveCardUp: (DashboardCardId) -> Unit,
    onMoveCardDown: (DashboardCardId) -> Unit,
    onResetLayout: () -> Unit,
    onPeriodSelected: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onQuickAddTransaction: () -> Unit,
    onQuickLogStock: () -> Unit,
    onToggleTask: (TodoTaskEntity) -> Unit,
    onUpdateAccount: (AccountEntity) -> Unit,
    onAddAccount: (AccountEntity) -> Unit,
    onDepositSavings: (SavingsAccountEntity, Double) -> Unit,
    onContributeGoal: (GoalEntity, Double) -> Unit,
    onUpdateBudgetCategory: (BudgetCategoryEntity) -> Unit,
    onAddTodoTask: (title: String, amount: Double, category: String) -> Unit,
    onUpdateTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onUpdateHolding: (HoldingEntity) -> Unit,
    onDeleteHolding: (Long) -> Unit,
    onUpdateExchangeRate: (Double) -> Unit
) {
    var hideBalances by remember { mutableStateOf(false) }

    // Dialog control states for direct card editing
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showEditRateDialog by remember { mutableStateOf(false) }
    var showQuickDepositDialog by remember { mutableStateOf(false) }
    var showQuickBudgetDialog by remember { mutableStateOf(false) }
    var showAddTodoDialog by remember { mutableStateOf(false) }
    var editingHolding by remember { mutableStateOf<HoldingEntity?>(null) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var showQuickAddExpenseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Layout Customization Banner / Controls
        item(key = "layout_customizer_header") {
            if (isCustomizingLayout) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customizing_layout_banner")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.DashboardCustomize,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Customizing Card Layout",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Use ▲ / ▼ buttons on each card to change order",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onResetLayout,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset Default")
                            }

                            Button(
                                onClick = onToggleCustomizeLayout,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Done Customizing")
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Financial Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    OutlinedButton(
                        onClick = onToggleCustomizeLayout,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("toggle_customize_layout_btn")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Customize Layout", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Render Cards dynamically based on user-movable cardOrder
        cardOrder.forEachIndexed { index, cardId ->
            item(key = cardId.name) {
                MovableCardContainer(
                    cardId = cardId,
                    cardIndex = index,
                    totalCards = cardOrder.size,
                    isCustomizing = isCustomizingLayout,
                    onMoveUp = { onMoveCardUp(cardId) },
                    onMoveDown = { onMoveCardDown(cardId) }
                ) {
                    when (cardId) {
                        DashboardCardId.HERO_NET_WORTH -> {
                            HeroNetWorthCard(
                                summary = summary,
                                usdToZigRate = usdToZigRate,
                                hideBalances = hideBalances,
                                onToggleHide = { hideBalances = !hideBalances },
                                onEditRate = { showEditRateDialog = true }
                            )
                        }

                        DashboardCardId.ACCOUNTS_CAROUSEL -> {
                            AccountsCarouselCard(
                                accounts = accounts,
                                hideBalances = hideBalances,
                                onAccountClick = { editingAccount = it },
                                onAddAccount = { showAddAccountDialog = true },
                                onManage = { onNavigate(AppScreen.TRANSACTIONS) }
                            )
                        }

                        DashboardCardId.QUICK_ACTIONS -> {
                            QuickActionsCard(
                                todoCount = todoTasks.count { !it.isCompleted },
                                onQuickAddExpense = onQuickAddTransaction,
                                onQuickLogStock = onQuickLogStock,
                                onQuickDeposit = { showQuickDepositDialog = true },
                                onNavigateBudget = { onNavigate(AppScreen.BUDGET_PLANNER) },
                                onNavigateTodo = { onNavigate(AppScreen.TODO_LIST) }
                            )
                        }

                        DashboardCardId.TIME_FILTER -> {
                            SegmentedTimeFilter(
                                selectedPeriod = selectedPeriod,
                                onPeriodSelected = onPeriodSelected
                            )
                        }

                        DashboardCardId.MONTHLY_CASHFLOW -> {
                            MonthlyCashflowCard(
                                summary = summary,
                                onRecordIncome = { showQuickAddExpenseDialog = true },
                                onRecordExpense = onQuickAddTransaction
                            )
                        }

                        DashboardCardId.SAVINGS_RATE -> {
                            SavingsRateCard(
                                summary = summary,
                                onDeposit = { showQuickDepositDialog = true },
                                onManageSavings = { onNavigate(AppScreen.SAVINGS_GOALS) }
                            )
                        }

                        DashboardCardId.BUDGET_HEALTH -> {
                            BudgetHealthCard(
                                budgetCategories = budgetCategories,
                                consumptionExpense = summary.monthlyConsumptionExpense,
                                transactions = recentTransactions,
                                onEditLimits = { showQuickBudgetDialog = true },
                                onNavigatePlanner = { onNavigate(AppScreen.BUDGET_PLANNER) }
                            )
                        }

                        DashboardCardId.TODO_CHECKLIST -> {
                            TodoChecklistCard(
                                todoTasks = todoTasks,
                                onToggleTask = onToggleTask,
                                onAddTask = { showAddTodoDialog = true },
                                onViewAll = { onNavigate(AppScreen.TODO_LIST) }
                            )
                        }

                        DashboardCardId.INVESTMENTS -> {
                            InvestmentsCard(
                                summary = summary,
                                holdings = holdings,
                                onHoldingClick = { editingHolding = it },
                                onBuyStock = onQuickLogStock,
                                onViewAll = { onNavigate(AppScreen.INVESTMENTS) }
                            )
                        }

                        DashboardCardId.PROJECTS_GOALS -> {
                            ProjectsGoalsCard(
                                summary = summary,
                                goals = goals,
                                onNavigate = { onNavigate(AppScreen.PROJECTS) },
                                onQuickContribute = { goal ->
                                    onContributeGoal(goal, 50.0)
                                }
                            )
                        }

                        DashboardCardId.RECENT_TRANSACTIONS -> {
                            RecentTransactionsCard(
                                recentTransactions = recentTransactions,
                                onTransactionClick = { editingTransaction = it },
                                onAddNew = onQuickAddTransaction,
                                onViewAll = { onNavigate(AppScreen.TRANSACTIONS) }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Interactive Editing Dialogs for All Dashboard Cards ---

    // 1. Edit Account Dialog
    editingAccount?.let { acc ->
        EditAccountDialog(
            account = acc,
            onDismiss = { editingAccount = null },
            onSave = { updated ->
                onUpdateAccount(updated)
                editingAccount = null
            }
        )
    }

    // 2. Add Account Dialog
    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onSave = { newAcc ->
                onAddAccount(newAcc)
                showAddAccountDialog = false
            }
        )
    }

    // 3. Edit Exchange Rate & Net Worth Dialog
    if (showEditRateDialog) {
        EditExchangeRateDialog(
            currentRate = usdToZigRate,
            onDismiss = { showEditRateDialog = false },
            onSave = { newRate ->
                onUpdateExchangeRate(newRate)
                showEditRateDialog = false
            }
        )
    }

    // 4. Quick Deposit to Savings Dialog
    if (showQuickDepositDialog) {
        QuickDepositDialog(
            savingsAccounts = savingsAccounts,
            onDismiss = { showQuickDepositDialog = false },
            onDeposit = { acc, amt ->
                onDepositSavings(acc, amt)
                showQuickDepositDialog = false
            }
        )
    }

    // 5. Quick Budget Limit Editor Dialog
    if (showQuickBudgetDialog) {
        QuickBudgetEditDialog(
            categories = budgetCategories,
            onDismiss = { showQuickBudgetDialog = false },
            onSaveCategory = { cat ->
                onUpdateBudgetCategory(cat)
            }
        )
    }

    // 6. Quick Add Financial To-Do Dialog
    if (showAddTodoDialog) {
        QuickAddTodoDialog(
            onDismiss = { showAddTodoDialog = false },
            onSave = { title, amt, cat ->
                onAddTodoTask(title, amt, cat)
                showAddTodoDialog = false
            }
        )
    }

    // 7. Edit Stock Holding Dialog
    editingHolding?.let { h ->
        EditStockCardDialog(
            holding = h,
            onDismiss = { editingHolding = null },
            onSave = { updated ->
                onUpdateHolding(updated)
                editingHolding = null
            },
            onDelete = { id ->
                onDeleteHolding(id)
                editingHolding = null
            }
        )
    }

    // 8. Edit Transaction Dialog
    editingTransaction?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            accounts = accounts,
            onDismiss = { editingTransaction = null },
            onSave = { updated ->
                onUpdateTransaction(updated)
                editingTransaction = null
            },
            onDelete = { id ->
                onDeleteTransaction(id)
                editingTransaction = null
            }
        )
    }

    // 9. Quick Add Income / General Transaction Dialog
    if (showQuickAddExpenseDialog) {
        AddTransactionDialog(
            accounts = accounts,
            projects = projects,
            onDismiss = { showQuickAddExpenseDialog = false },
            onSave = { tx ->
                onUpdateTransaction(tx)
                showQuickAddExpenseDialog = false
            }
        )
    }
}

/**
 * Container that wraps each Dashboard card with reorder controls (Move Up / Move Down)
 * when customization mode is active.
 */
@Composable
fun MovableCardContainer(
    cardId: DashboardCardId,
    cardIndex: Int,
    totalCards: Int,
    isCustomizing: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    content: @Composable () -> Unit
) {
    if (isCustomizing) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("movable_container_${cardId.name.lowercase()}")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DragHandle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "#${cardIndex + 1} • ${cardId.title}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onMoveUp,
                            enabled = cardIndex > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move Up",
                                tint = if (cardIndex > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }

                        IconButton(
                            onClick = onMoveDown,
                            enabled = cardIndex < totalCards - 1,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move Down",
                                tint = if (cardIndex < totalCards - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                content()
            }
        }
    } else {
        content()
    }
}

// -------------------------------------------------------------
// Individual Interactive Dashboard Cards
// -------------------------------------------------------------

@Composable
fun HeroNetWorthCard(
    summary: DashboardFinancialSummary,
    usdToZigRate: Double,
    hideBalances: Boolean,
    onToggleHide: () -> Unit,
    onEditRate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("net_worth_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "TOTAL NET WORTH",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onToggleHide,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Balance Visibility",
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onEditRate,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Exchange Rate",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        CurrencyBadge(currency = "USD")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (hideBalances) "$ •••••••" else CurrencyFormatter.formatUsd(summary.netWorth),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onEditRate() }
                ) {
                    Text(
                        text = if (hideBalances) "≈ ZiG •••••••" else "≈ ZiG ${String.format("%.2f", summary.netWorth * usdToZigRate)} (Rate: $usdToZigRate)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Rate",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Cash & Wallets",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        )
                        Text(
                            text = if (hideBalances) "•••••" else CurrencyFormatter.formatUsd(summary.cashAndWalletsBalance),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "Savings Balance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        )
                        Text(
                            text = if (hideBalances) "•••••" else CurrencyFormatter.formatUsd(summary.totalSavingsBalance),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "C-Trade Portfolio",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        )
                        Text(
                            text = if (hideBalances) "•••••" else CurrencyFormatter.formatUsd(summary.portfolioMarketValue),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountsCarouselCard(
    accounts: List<AccountEntity>,
    hideBalances: Boolean,
    onAccountClick: (AccountEntity) -> Unit,
    onAddAccount: () -> Unit,
    onManage: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Accounts & Wallets (${accounts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+ Add",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable { onAddAccount() }
                        .padding(end = 12.dp)
                )
                Text(
                    text = "Manage",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.clickable { onManage() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            accounts.forEach { acc ->
                val icon = when (acc.type) {
                    "ECOCASH" -> Icons.Default.PhoneAndroid
                    "BANK" -> Icons.Default.AccountBalance
                    "INVESTMENT" -> Icons.Default.ShowChart
                    else -> Icons.Default.Payments
                }
                val accent = when (acc.type) {
                    "ECOCASH" -> EcoCashBlue
                    "BANK" -> MaterialTheme.colorScheme.tertiary
                    "INVESTMENT" -> MaterialTheme.colorScheme.secondary
                    else -> FinanceIncomeGreen
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .width(200.dp)
                        .clickable { onAccountClick(acc) }
                        .testTag("account_card_${acc.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = acc.currency,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Account",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = acc.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        Text(
                            text = acc.accountNumber.ifBlank { acc.type },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (hideBalances) "••••••" else {
                                if (acc.currency == "ZWG") "ZiG ${String.format("%.2f", acc.balance)}"
                                else CurrencyFormatter.formatUsd(acc.balance)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Quick Add Account Card at the end of carousel
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .width(130.dp)
                    .clickable { onAddAccount() }
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Account", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Add Wallet", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    todoCount: Int,
    onQuickAddExpense: () -> Unit,
    onQuickLogStock: () -> Unit,
    onQuickDeposit: () -> Unit,
    onNavigateBudget: () -> Unit,
    onNavigateTodo: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onQuickAddExpense,
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_add_expense_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expense", maxLines = 1)
            }

            Button(
                onClick = onQuickLogStock,
                modifier = Modifier
                    .weight(1f)
                    .testTag("quick_log_stock_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Stock", maxLines = 1)
            }

            Button(
                onClick = onQuickDeposit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Deposit", maxLines = 1)
            }
        }

        // Shortcut Row for Budget Planner and Financial To-Do List
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateBudget() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Budget Planner",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNavigateTodo() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Checklist,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "To-Do List ($todoCount)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyCashflowCard(
    summary: DashboardFinancialSummary,
    onRecordIncome: () -> Unit,
    onRecordExpense: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Period Income",
            value = CurrencyFormatter.formatUsd(summary.monthlyIncome),
            subtitle = "+ Tap to record",
            icon = Icons.Default.ArrowDownward,
            accentColor = FinanceIncomeGreen,
            modifier = Modifier
                .weight(1f)
                .clickable { onRecordIncome() }
        )
        StatCard(
            title = "Consumption Spend",
            value = CurrencyFormatter.formatUsd(summary.monthlyConsumptionExpense),
            subtitle = "Fees: $${String.format("%.2f", summary.monthlyEcoCashFees)}",
            icon = Icons.Default.ArrowUpward,
            accentColor = FinanceExpenseRed,
            modifier = Modifier
                .weight(1f)
                .clickable { onRecordExpense() }
        )
    }
}

@Composable
fun SavingsRateCard(
    summary: DashboardFinancialSummary,
    onDeposit: () -> Unit,
    onManageSavings: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDeposit() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Cash Flow",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.formatUsd(summary.netCashFlow),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.netCashFlow >= 0) FinanceIncomeGreen else FinanceExpenseRed
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Savings Rate: ${String.format("%.1f", summary.savingsRatePercent)}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDeposit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Deposit", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProgressBarWithTarget(
                progress = (summary.savingsRatePercent / 100.0).toFloat(),
                targetLabel = "Target: 20%",
                currentLabel = "Current Rate",
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Saved: ${CurrencyFormatter.formatUsd(summary.totalSavingsBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+ Quick Deposit",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable { onDeposit() }
                )
            }
        }
    }
}

@Composable
fun BudgetHealthCard(
    budgetCategories: List<BudgetCategoryEntity>,
    consumptionExpense: Double,
    transactions: List<TransactionEntity> = emptyList(),
    onEditLimits: () -> Unit,
    onNavigatePlanner: () -> Unit
) {
    val totalPlanned = budgetCategories.sumOf { it.monthlyLimit }
    val remainingFunds = (totalPlanned - consumptionExpense).coerceAtLeast(0.0)
    val isOverBudget = consumptionExpense > totalPlanned && totalPlanned > 0
    val overBudgetAmt = if (isOverBudget) consumptionExpense - totalPlanned else 0.0
    val budgetProgress = if (totalPlanned > 0) (consumptionExpense / totalPlanned).toFloat() else 0f

    val categorySpendMap: Map<String, Double> = remember(transactions) {
        transactions
            .filter { it.type.equals("EXPENSE", ignoreCase = true) }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigatePlanner() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Monthly Budget Tracker", fontWeight = FontWeight.Bold)
                        Text(
                            "${budgetCategories.size} Categories Tracked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditLimits, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Limits",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onNavigatePlanner, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "View Budget Planner",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Highlighted Remaining Funds Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isOverBudget -> FinanceExpenseRed.copy(alpha = 0.10f)
                            budgetProgress >= 0.8f -> ZiGAmber.copy(alpha = 0.12f)
                            else -> FinanceIncomeGreen.copy(alpha = 0.12f)
                        }
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isOverBudget) "Budget Exceeded By" else "Remaining Budget Funds",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isOverBudget) "-${CurrencyFormatter.formatUsd(overBudgetAmt)}" else CurrencyFormatter.formatUsd(remainingFunds),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                isOverBudget -> FinanceExpenseRed
                                budgetProgress >= 0.8f -> ZiGAmber
                                else -> FinanceIncomeGreen
                            }
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when {
                            isOverBudget -> FinanceExpenseRed.copy(alpha = 0.18f)
                            budgetProgress >= 0.8f -> ZiGAmber.copy(alpha = 0.18f)
                            else -> FinanceIncomeGreen.copy(alpha = 0.18f)
                        }
                    ) {
                        Text(
                            text = when {
                                isOverBudget -> "Over Budget"
                                budgetProgress >= 0.8f -> "Near Limit"
                                else -> "On Track"
                            },
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                isOverBudget -> FinanceExpenseRed
                                budgetProgress >= 0.8f -> ZiGAmber
                                else -> FinanceIncomeGreen
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Remaining Funds Progress Bar
            RemainingFundsProgressBar(
                remainingAmount = remainingFunds,
                totalBudget = totalPlanned,
                spentAmount = consumptionExpense,
                barHeight = 12.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Category Spending Breakdown (Top 3)
            if (budgetCategories.isNotEmpty()) {
                Text(
                    text = "Category Limits & Remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                budgetCategories.take(3).forEach { cat ->
                    val spent = categorySpendMap[cat.category] ?: 0.0
                    val rem = (cat.monthlyLimit - spent).coerceAtLeast(0.0)
                    val catRatio = if (cat.monthlyLimit > 0) ((cat.monthlyLimit - spent) / cat.monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f
                    val isCatOver = spent > cat.monthlyLimit

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cat.category,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isCatOver) "Over: -$${String.format("%.0f", spent - cat.monthlyLimit)}"
                                else "Rem: $${String.format("%.0f", rem)} / $${String.format("%.0f", cat.monthlyLimit)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isCatOver) FinanceExpenseRed else if (catRatio <= 0.20f) ZiGAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isCatOver || catRatio <= 0.20f) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onNavigatePlanner,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Manage All Categories", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}


@Composable
fun TodoChecklistCard(
    todoTasks: List<TodoTaskEntity>,
    onToggleTask: (TodoTaskEntity) -> Unit,
    onAddTask: () -> Unit,
    onViewAll: () -> Unit
) {
    val pending = todoTasks.filter { !it.isCompleted }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Financial To-Dos & Bills", fontWeight = FontWeight.Bold)
                        Text(
                            "${pending.size} tasks pending",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAddTask, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.clickable { onViewAll() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (pending.isEmpty()) {
                Text(
                    text = "All financial tasks and bills completed!",
                    style = MaterialTheme.typography.bodySmall,
                    color = FinanceIncomeGreen
                )
            } else {
                pending.take(3).forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggleTask(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "${task.category} • Due: ${CurrencyFormatter.formatShortDate(task.dueDateMillis)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (task.estimatedAmount > 0) {
                            Text(
                                text = CurrencyFormatter.formatUsd(task.estimatedAmount),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvestmentsCard(
    summary: DashboardFinancialSummary,
    holdings: List<HoldingEntity>,
    onHoldingClick: (HoldingEntity) -> Unit,
    onBuyStock: () -> Unit,
    onViewAll: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onViewAll() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("C-Trade Portfolio (VFEX & ZSE)", fontWeight = FontWeight.Bold)
                        Text("${holdings.size} Active Securities", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBuyStock, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Buy Stock", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onViewAll, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Market Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatUsd(summary.portfolioMarketValue), fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Total Invested", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.formatUsd(summary.portfolioCostBasis), fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Unrealized Return", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = CurrencyFormatter.formatPercent(summary.portfolioReturnPercent),
                        fontWeight = FontWeight.Bold,
                        color = if (summary.portfolioUnrealizedGain >= 0) FinanceIncomeGreen else FinanceExpenseRed
                    )
                }
            }

            // Quick holding pills allowing direct tap to edit
            if (holdings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    holdings.take(4).forEach { h ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { onHoldingClick(h) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${h.ticker.substringBefore(".")} (${h.shares.toInt()})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectsGoalsCard(
    summary: DashboardFinancialSummary,
    goals: List<GoalEntity>,
    onNavigate: () -> Unit,
    onQuickContribute: (GoalEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigate() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Business & Personal Projects", fontWeight = FontWeight.Bold)
                        Text("${summary.activeProjectsCount} Projects Active", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val projBudget = summary.totalProjectsBudget
            val projSpent = summary.totalProjectsSpent
            val progress = if (projBudget > 0) (projSpent / projBudget).toFloat() else 0f

            ProgressBarWithTarget(
                progress = progress,
                targetLabel = "Budget: $${String.format("%.0f", projBudget)}",
                currentLabel = "Spent: $${String.format("%.0f", projSpent)}",
                color = MaterialTheme.colorScheme.tertiary
            )

            if (goals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                val firstGoal = goals.first()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Goal: ${firstGoal.name} (${firstGoal.progressPercentage.toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "+$50 Quick Add",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable { onQuickContribute(firstGoal) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentTransactionsCard(
    recentTransactions: List<TransactionEntity>,
    onTransactionClick: (TransactionEntity) -> Unit,
    onAddNew: () -> Unit,
    onViewAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+ New",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable { onAddNew() }
                        .padding(end = 12.dp)
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.clickable { onViewAll() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val topRecent = recentTransactions.take(5)
        if (topRecent.isEmpty()) {
            Text(
                text = "No recent transactions. Tap + New to record or import EcoCash statements.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                topRecent.forEach { tx ->
                    TransactionItemRow(
                        tx = tx,
                        onClick = { onTransactionClick(tx) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    tx: TransactionEntity,
    onClick: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (tx.type == "INCOME" || tx.type == "DIVIDEND") FinanceIncomeGreen.copy(alpha = 0.15f)
                            else if (tx.category == "Transfers" || tx.type == "SAVINGS_DEPOSIT") MaterialTheme.colorScheme.primaryContainer
                            else FinanceExpenseRed.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.type == "INCOME" || tx.type == "DIVIDEND") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (tx.type == "INCOME" || tx.type == "DIVIDEND") FinanceIncomeGreen else FinanceExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tx.recipientOrSender.ifBlank { tx.category },
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (tx.fee > 0) {
                            Text(
                                text = " • Fee: $${String.format("%.2f", tx.fee)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    val isPositive = tx.type == "INCOME" || tx.type == "DIVIDEND"
                    Text(
                        text = (if (isPositive) "+" else "-") + CurrencyFormatter.formatUsd(tx.amount),
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) FinanceIncomeGreen else FinanceExpenseRed
                    )
                    Text(
                        text = tx.reference.take(12),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Transaction",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
