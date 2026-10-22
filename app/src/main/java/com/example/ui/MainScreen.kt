package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.LogStockBoughtDialog
import com.example.ui.components.CreateProfileDialog
import com.example.ui.components.ImportStatementDialog
import com.example.ui.components.ProfileSwitcherDialog
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.BudgetPlannerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InvestmentsScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.SecurityLockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodoListScreen
import com.example.ui.screens.TransactionsScreen
import com.example.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
    val securityError by viewModel.securityError.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()

    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val holdings by viewModel.holdings.collectAsStateWithLifecycle()
    val investmentTransactions by viewModel.investmentTransactions.collectAsStateWithLifecycle()
    val investmentTargets by viewModel.investmentTargets.collectAsStateWithLifecycle()
    val savingsAccounts by viewModel.savingsAccounts.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val projectBudgetItems by viewModel.projectBudgetItems.collectAsStateWithLifecycle()
    val projectMilestones by viewModel.projectMilestones.collectAsStateWithLifecycle()
    val budgetCategories by viewModel.budgetCategories.collectAsStateWithLifecycle()
    val todoTasks by viewModel.todoTasks.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedTimePeriod.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showLogStockDialog by remember { mutableStateOf(false) }
    var showProfileSwitcher by remember { mutableStateOf(false) }
    var showCreateProfile by remember { mutableStateOf(false) }

    // Feedback snackbar
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    // Android Hardware / Gesture BackHandler
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        val handled = viewModel.navigateBack()
        if (!handled) {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    // Security Gate
    if (!isUnlocked) {
        SecurityLockScreen(
            userProfile = userProfile,
            errorMessage = securityError,
            onUnlock = { pin -> viewModel.unlockWithPin(pin) },
            onBypassForDemo = { viewModel.unlockWithPin(userProfile?.pinCode ?: "") }
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showProfileSwitcher = true }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = "MoneyPilot ZW Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = userProfile?.userName ?: "MoneyPilot ZW",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                if (userProfile?.isZeroProfile == true) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            "ZERO",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Net Worth: ${CurrencyFormatter.formatUsd(summary.netWorth)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showProfileSwitcher = true },
                        modifier = Modifier.testTag("top_bar_profile_switcher_btn")
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Switch Profile"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.BUDGET_PLANNER) },
                        modifier = Modifier.testTag("top_bar_budget_btn")
                    ) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = "Budget Planner"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.TODO_LIST) },
                        modifier = Modifier.testTag("top_bar_todo_btn")
                    ) {
                        Icon(
                            Icons.Default.Checklist,
                            contentDescription = "To-Do List"
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.ASSISTANT) },
                        modifier = Modifier.testTag("top_bar_assistant_btn")
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        modifier = Modifier.testTag("top_bar_settings_btn")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val navItems = listOf(
                    Triple(AppScreen.DASHBOARD, "Dashboard", Icons.Default.Dashboard),
                    Triple(AppScreen.TRANSACTIONS, "Ledger", Icons.Default.ReceiptLong),
                    Triple(AppScreen.INVESTMENTS, "C-Trade", Icons.Default.ShowChart),
                    Triple(AppScreen.SAVINGS_GOALS, "Savings", Icons.Default.Savings),
                    Triple(AppScreen.PROJECTS, "Projects", Icons.Default.Work)
                )

                navItems.forEach { (screen, label, icon) ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(screen) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(
                    summary = summary,
                    accounts = accounts,
                    recentTransactions = transactions,
                    holdings = holdings,
                    goals = goals,
                    projects = projects,
                    budgetCategories = budgetCategories,
                    todoTasks = todoTasks,
                    selectedPeriod = selectedPeriod,
                    usdToZigRate = userProfile?.usdToZigRate ?: 26.50,
                    onPeriodSelected = { viewModel.setTimePeriod(it) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onQuickAddTransaction = { showAddTxDialog = true },
                    onQuickBuyStock = { showBuyStockDialog = true },
                    onToggleTask = { viewModel.toggleTodoTask(it) }
                )

                AppScreen.BUDGET_PLANNER -> BudgetPlannerScreen(
                    summary = summary,
                    budgetCategories = budgetCategories,
                    transactions = transactions,
                    onSaveCategory = { viewModel.saveBudgetCategory(it) },
                    onDeleteCategory = { viewModel.deleteBudgetCategory(it) }
                )

                AppScreen.TODO_LIST -> TodoListScreen(
                    tasks = todoTasks,
                    onToggleTask = { viewModel.toggleTodoTask(it) },
                    onSaveTask = { viewModel.saveTodoTask(it) },
                    onDeleteTask = { viewModel.deleteTodoTask(it) }
                )

                AppScreen.TRANSACTIONS -> TransactionsScreen(
                    transactions = transactions,
                    accounts = accounts,
                    onAddTransaction = { showAddTxDialog = true },
                    onImportStatement = { showImportDialog = true },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                )

                AppScreen.INVESTMENTS -> InvestmentsScreen(
                    holdings = holdings,
                    investmentTransactions = investmentTransactions,
                    investmentTargets = investmentTargets,
                    onBuyStock = { ticker, company, exchange, shares, price, fees ->
                        viewModel.buyStock(ticker, company, exchange, shares, price, fees)
                    },
                    onSellStock = { ticker, shares, price, fees ->
                        viewModel.sellStock(ticker, shares, price, fees)
                    },
                    onUpdatePrice = { ticker, newPrice ->
                        viewModel.updatePrice(ticker, newPrice)
                    },
                    onUpdateHolding = { holding ->
                        viewModel.updateHolding(holding)
                    },
                    onDeleteHolding = { id ->
                        viewModel.deleteHolding(id)
                    }
                )

                AppScreen.SAVINGS_GOALS -> SavingsGoalsScreen(
                    savingsAccounts = savingsAccounts,
                    goals = goals,
                    onSaveSavingsAccount = { viewModel.saveSavingsAccount(it) },
                    onUpdateSavingsAccount = { viewModel.saveSavingsAccount(it) },
                    onDeleteSavingsAccount = { viewModel.deleteSavingsAccount(it) },
                    onSaveGoal = { viewModel.saveGoal(it) },
                    onUpdateGoal = { viewModel.saveGoal(it) },
                    onDeleteGoal = { viewModel.deleteGoal(it) }
                )

                AppScreen.PROJECTS -> ProjectsScreen(
                    projects = projects,
                    budgetItems = projectBudgetItems,
                    milestones = projectMilestones,
                    onSaveProject = { proj, items -> viewModel.saveProject(proj, items) },
                    onUpdateProject = { viewModel.updateProject(it) },
                    onDeleteProject = { viewModel.deleteProject(it) },
                    onUpdateBudgetItem = { viewModel.updateProjectBudgetItem(it) },
                    onDeleteBudgetItem = { viewModel.deleteProjectBudgetItem(it) },
                    onAddBudgetItem = { viewModel.addProjectBudgetItem(it) },
                    onToggleMilestone = { viewModel.toggleMilestone(it) }
                )

                AppScreen.REPORTS -> ReportsScreen(
                    summary = summary,
                    transactions = transactions,
                    budgetCategories = budgetCategories
                )

                AppScreen.ASSISTANT -> AssistantScreen(
                    summary = summary,
                    transactions = transactions,
                    holdings = holdings,
                    investmentTransactions = investmentTransactions,
                    investmentTargets = investmentTargets,
                    savingsAccounts = savingsAccounts,
                    goals = goals,
                    projects = projects
                )

                AppScreen.SETTINGS -> SettingsScreen(
                    userProfile = userProfile,
                    allProfiles = allProfiles,
                    activeProfileId = activeProfileId,
                    onSwitchProfile = { viewModel.switchProfile(it) },
                    onCreateProfile = { name, curr, rate -> viewModel.createZeroProfile(name, curr, rate) },
                    onResetProfileToZero = { viewModel.resetActiveProfileToZero() },
                    onDeleteProfile = { viewModel.deleteProfile(it) },
                    onUpdateExchangeRate = { viewModel.updateExchangeRate(it) },
                    onSetPin = { viewModel.setSecurityPin(it) },
                    onLockApp = { viewModel.lockApp() },
                    onResetDemoData = { viewModel.resetDemoData() }
                )

                AppScreen.SECURITY -> SecurityLockScreen(
                    userProfile = userProfile,
                    errorMessage = securityError,
                    onUnlock = { viewModel.unlockWithPin(it) },
                    onBypassForDemo = { viewModel.unlockWithPin(userProfile?.pinCode ?: "") }
                )
            }
        }
    }

    // Quick Transaction Dialog
    if (showAddTxDialog) {
        AddTransactionDialog(
            accounts = accounts,
            projects = projects,
            onDismiss = { showAddTxDialog = false },
            onSave = {
                viewModel.addTransaction(it)
                showAddTxDialog = false
            }
        )
    }

    // Statement Import Dialog
    if (showImportDialog) {
        ImportStatementDialog(
            onDismiss = { showImportDialog = false },
            onImport = { rawText ->
                viewModel.importStatement(rawText)
                showImportDialog = false
            }
        )
    }

    // Quick Buy Stock Dialog
    if (showBuyStockDialog) {
        BuySellStockDialog(
            holding = holdings.firstOrNull(),
            isBuy = true,
            onDismiss = { showBuyStockDialog = false },
            onConfirm = { ticker, company, exchange, shares, price, fees ->
                viewModel.buyStock(ticker, company, exchange, shares, price, fees)
                showBuyStockDialog = false
            }
        )
    }

    // Profile Switcher Dialog (from TopAppBar)
    if (showProfileSwitcher) {
        ProfileSwitcherDialog(
            profiles = allProfiles,
            activeProfileId = activeProfileId,
            onDismiss = { showProfileSwitcher = false },
            onSwitchProfile = { viewModel.switchProfile(it) },
            onCreateNewProfile = { showCreateProfile = true },
            onResetCurrentToZero = { viewModel.resetActiveProfileToZero() },
            onDeleteProfile = { viewModel.deleteProfile(it) }
        )
    }

    // Create Profile Dialog
    if (showCreateProfile) {
        CreateProfileDialog(
            onDismiss = { showCreateProfile = false },
            onCreate = { name, curr, rate ->
                viewModel.createZeroProfile(name, curr, rate)
                showCreateProfile = false
            }
        )
    }
}
