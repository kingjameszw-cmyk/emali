package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SampleDataInitializer
import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.BudgetCategoryEntity
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.HoldingEntity
import com.example.data.local.entities.InvestmentTargetEntity
import com.example.data.local.entities.InvestmentTransactionEntity
import com.example.data.local.entities.ProjectBudgetItemEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.ProjectMilestoneEntity
import com.example.data.local.entities.SavingsAccountEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserProfileEntity
import com.example.data.repository.FinanceRepository
import com.example.util.DashboardFinancialSummary
import com.example.util.FinancialMath
import com.example.util.BudgetNotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class DashboardCardId(val title: String) {
    HERO_NET_WORTH("Net Worth Hero"),
    ACCOUNTS_CAROUSEL("Accounts & Wallets"),
    QUICK_ACTIONS("Quick Actions Bar"),
    TIME_FILTER("Time Filter & Rates"),
    MONTHLY_CASHFLOW("Monthly Cashflow & Stats"),
    SPENDING_CHARTS("Spending Trends & Category Charts"),
    SAVINGS_RATE("Savings Rate & Goals"),
    BUDGET_HEALTH("Monthly Budget Health"),
    TODO_CHECKLIST("Bills & Tasks Checklist"),
    INVESTMENTS("C-Trade Stocks"),
    PROJECTS_GOALS("Active Projects & Goals"),
    RECENT_TRANSACTIONS("Recent Ledger Transactions")
}

enum class AppScreen {
    DASHBOARD,
    EXPENSE_TRACKER,
    SAVED_EXPENSES,
    TRANSACTIONS,
    INVESTMENTS,
    SAVINGS_GOALS,
    PROJECTS,
    BUDGET_PLANNER,
    TODO_LIST,
    REPORTS,
    MONTHLY_SUMMARY,
    ASSISTANT,
    SECURITY,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FinanceRepository(db.financeDao())

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenStack = MutableStateFlow(listOf(AppScreen.DASHBOARD))

    // Time filter
    private val _selectedTimePeriod = MutableStateFlow("This Month")
    val selectedTimePeriod: StateFlow<String> = _selectedTimePeriod.asStateFlow()

    // Security lock
    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _securityError = MutableStateFlow<String?>(null)
    val securityError: StateFlow<String?> = _securityError.asStateFlow()

    // Status / Toast alert
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    // Dark Mode / Theme Mode state
    private val prefs = application.getSharedPreferences("moneypilot_prefs", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        try {
            ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
        _userFeedbackMessage.value = "Display mode set to ${mode.label}"
    }

    fun toggleDarkMode() {
        val next = when (_themeMode.value) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(next)
    }

    // Multi-profile state
    private val _activeProfileId = MutableStateFlow(1L)
    val activeProfileId: StateFlow<Long> = _activeProfileId.asStateFlow()

    val allProfiles: StateFlow<List<UserProfileEntity>> = repository.allProfiles.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Data streams scoped to active profile
    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<UserProfileEntity?> = _activeProfileId
        .flatMapLatest { id -> repository.getProfile(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val accounts: StateFlow<List<AccountEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getAccounts(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<TransactionEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getTransactions(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val savedExpenses: StateFlow<List<TransactionEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getExpenses(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val holdings: StateFlow<List<HoldingEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getHoldings(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val investmentTransactions: StateFlow<List<InvestmentTransactionEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getInvestmentTransactions(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val investmentTargets: StateFlow<List<InvestmentTargetEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getInvestmentTargets(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val savingsAccounts: StateFlow<List<SavingsAccountEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getSavingsAccounts(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val goals: StateFlow<List<GoalEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getGoals(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val projects: StateFlow<List<ProjectEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getProjects(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val projectBudgetItems: StateFlow<List<ProjectBudgetItemEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getProjectBudgetItems(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val projectMilestones: StateFlow<List<ProjectMilestoneEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getProjectMilestones(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val budgetCategories: StateFlow<List<BudgetCategoryEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getBudgetCategories(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val todoTasks: StateFlow<List<com.example.data.local.entities.TodoTaskEntity>> = _activeProfileId
        .flatMapLatest { id -> repository.getTodoTasks(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Dashboard Summary (Combined reactively across accounts, ledger transactions, savings, goals, projects, budget)
    private data class DashPart1(
        val accs: List<AccountEntity>,
        val txs: List<TransactionEntity>,
        val holds: List<HoldingEntity>,
        val savings: List<SavingsAccountEntity>
    )

    private data class DashPart2(
        val gls: List<GoalEntity>,
        val prjs: List<ProjectEntity>,
        val bItems: List<ProjectBudgetItemEntity>,
        val period: String
    )

    private val part1Flow = combine(accounts, transactions, holdings, savingsAccounts) { a, b, c, d ->
        DashPart1(a, b, c, d)
    }

    private val part2Flow = combine(goals, projects, projectBudgetItems, selectedTimePeriod) { a, b, c, d ->
        DashPart2(a, b, c, d)
    }

    val dashboardSummary: StateFlow<DashboardFinancialSummary> = combine(part1Flow, part2Flow) { p1, p2 ->
        val window = when (p2.period) {
            "This Week" -> FinancialMath.getStartOfWeekMillis()
            "This Year" -> FinancialMath.getStartOfYearMillis()
            "All Time" -> 0L
            else -> FinancialMath.getStartOfMonthMillis()
        }
        FinancialMath.computeSummary(
            accounts = p1.accs,
            transactions = p1.txs,
            holdings = p1.holds,
            savingsAccounts = p1.savings,
            goals = p2.gls,
            projects = p2.prjs,
            projectBudgetItems = p2.bItems,
            timeWindowMillis = window
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardFinancialSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0)
    )

    // Movable & Reorderable Dashboard Card Order
    private val _dashboardCardOrder = MutableStateFlow(
        listOf(
            DashboardCardId.HERO_NET_WORTH,
            DashboardCardId.ACCOUNTS_CAROUSEL,
            DashboardCardId.QUICK_ACTIONS,
            DashboardCardId.TIME_FILTER,
            DashboardCardId.MONTHLY_CASHFLOW,
            DashboardCardId.SPENDING_CHARTS,
            DashboardCardId.SAVINGS_RATE,
            DashboardCardId.BUDGET_HEALTH,
            DashboardCardId.TODO_CHECKLIST,
            DashboardCardId.INVESTMENTS,
            DashboardCardId.PROJECTS_GOALS,
            DashboardCardId.RECENT_TRANSACTIONS
        )
    )
    val dashboardCardOrder: StateFlow<List<DashboardCardId>> = _dashboardCardOrder.asStateFlow()

    private val _isCustomizingDashboard = MutableStateFlow(false)
    val isCustomizingDashboard: StateFlow<Boolean> = _isCustomizingDashboard.asStateFlow()

    fun toggleCustomizeDashboard() {
        _isCustomizingDashboard.value = !_isCustomizingDashboard.value
    }

    fun setCustomizingDashboard(value: Boolean) {
        _isCustomizingDashboard.value = value
    }

    fun moveDashboardCardUp(cardId: DashboardCardId) {
        val list = _dashboardCardOrder.value.toMutableList()
        val index = list.indexOf(cardId)
        if (index > 0) {
            val item = list.removeAt(index)
            list.add(index - 1, item)
            _dashboardCardOrder.value = list
            _userFeedbackMessage.value = "Moved ${cardId.title} up"
        }
    }

    fun moveDashboardCardDown(cardId: DashboardCardId) {
        val list = _dashboardCardOrder.value.toMutableList()
        val index = list.indexOf(cardId)
        if (index in 0 until list.size - 1) {
            val item = list.removeAt(index)
            list.add(index + 1, item)
            _dashboardCardOrder.value = list
            _userFeedbackMessage.value = "Moved ${cardId.title} down"
        }
    }

    fun resetDashboardLayout() {
        _dashboardCardOrder.value = listOf(
            DashboardCardId.HERO_NET_WORTH,
            DashboardCardId.ACCOUNTS_CAROUSEL,
            DashboardCardId.QUICK_ACTIONS,
            DashboardCardId.TIME_FILTER,
            DashboardCardId.MONTHLY_CASHFLOW,
            DashboardCardId.SAVINGS_RATE,
            DashboardCardId.BUDGET_HEALTH,
            DashboardCardId.TODO_CHECKLIST,
            DashboardCardId.INVESTMENTS,
            DashboardCardId.PROJECTS_GOALS,
            DashboardCardId.RECENT_TRANSACTIONS
        )
        _userFeedbackMessage.value = "Reset dashboard layout to default"
    }

    init {
        BudgetNotificationHelper.createNotificationChannel(application)
        viewModelScope.launch {
            SampleDataInitializer.populateIfEmpty(db.financeDao())
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            val updated = _screenStack.value.toMutableList()
            updated.add(screen)
            _screenStack.value = updated
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        val stack = _screenStack.value
        return if (stack.size > 1) {
            val updated = stack.dropLast(1)
            _screenStack.value = updated
            _currentScreen.value = updated.last()
            true
        } else {
            false
        }
    }

    fun setTimePeriod(period: String) {
        _selectedTimePeriod.value = period
    }

    fun clearFeedback() {
        _userFeedbackMessage.value = null
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.addTransaction(transaction.copy(profileId = _activeProfileId.value))
            _userFeedbackMessage.value = "Recorded: ${transaction.category} ($${transaction.amount})"
            if (transaction.type.equals("EXPENSE", ignoreCase = true)) {
                checkBudgetThresholdAndAlert(transaction.category, transaction.amount)
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction.copy(profileId = _activeProfileId.value))
            _userFeedbackMessage.value = "Updated transaction: ${transaction.category}"
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _userFeedbackMessage.value = "Transaction deleted"
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            db.financeDao().updateAccount(account)
            _userFeedbackMessage.value = "Updated ${account.name} balance to $${String.format("%.2f", account.balance)}"
        }
    }

    fun addAccount(account: AccountEntity) {
        viewModelScope.launch {
            db.financeDao().insertAccount(account.copy(profileId = _activeProfileId.value))
            _userFeedbackMessage.value = "Added account ${account.name}"
        }
    }

    fun depositToSavings(account: SavingsAccountEntity, amount: Double) {
        viewModelScope.launch {
            val updated = account.copy(
                currentBalance = account.currentBalance + amount,
                profileId = _activeProfileId.value
            )
            repository.saveSavingsAccount(updated)

            // Log corresponding savings transaction in ledger
            val tx = TransactionEntity(
                profileId = _activeProfileId.value,
                accountId = 1,
                type = "SAVINGS_DEPOSIT",
                amount = amount,
                fee = 0.0,
                totalDeducted = amount,
                currency = "USD",
                category = "Savings Deposit",
                recipientOrSender = account.name,
                reference = "SAV-" + System.currentTimeMillis().toString().takeLast(6),
                description = "Deposit into ${account.name}",
                dateMillis = System.currentTimeMillis(),
                savingsAccountId = account.id
            )
            repository.addTransaction(tx)
            _userFeedbackMessage.value = "Deposited $${String.format("%.2f", amount)} into ${account.name}"
        }
    }

    fun contributeToGoal(goal: GoalEntity, amount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(
                currentAmount = goal.currentAmount + amount,
                profileId = _activeProfileId.value
            )
            repository.saveGoal(updated)

            // Log corresponding goal transaction in ledger
            val tx = TransactionEntity(
                profileId = _activeProfileId.value,
                accountId = 1,
                type = "SAVINGS_DEPOSIT",
                amount = amount,
                fee = 0.0,
                totalDeducted = amount,
                currency = "USD",
                category = "Goal: ${goal.name}",
                recipientOrSender = goal.name,
                reference = "GOAL-" + System.currentTimeMillis().toString().takeLast(6),
                description = "Contribution to ${goal.name}",
                dateMillis = System.currentTimeMillis()
            )
            repository.addTransaction(tx)
            _userFeedbackMessage.value = "Contributed $${String.format("%.2f", amount)} to ${goal.name}"
        }
    }

    fun importStatement(rawText: String, accountId: Long = 1) {
        viewModelScope.launch {
            val (imported, duplicates) = repository.importEcoCashStatement(rawText, accountId)
            _userFeedbackMessage.value = "Imported $imported transactions ($duplicates duplicates skipped)"
        }
    }

    fun updatePrice(ticker: String, newPrice: Double) {
        viewModelScope.launch {
            repository.updateHoldingPrice(ticker, newPrice)
            _userFeedbackMessage.value = "Updated $ticker to $${String.format("%.3f", newPrice)}"
        }
    }

    fun updateHolding(holding: HoldingEntity) {
        viewModelScope.launch {
            repository.updateHolding(holding)
            _userFeedbackMessage.value = "Updated ${holding.ticker} (${holding.shares.toInt()} shares @ $${String.format("%.3f", holding.currentPrice)})"
        }
    }

    fun updateHoldingQuantity(ticker: String, newShares: Double) {
        viewModelScope.launch {
            repository.updateHoldingQuantity(ticker, newShares)
            _userFeedbackMessage.value = "Updated $ticker to ${newShares.toInt()} shares"
        }
    }

    fun deleteHolding(id: Long) {
        viewModelScope.launch {
            repository.deleteHolding(id)
            _userFeedbackMessage.value = "Holding removed from portfolio"
        }
    }

    fun logStockBought(
        ticker: String,
        company: String,
        exchange: String,
        shares: Double,
        price: Double,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.logStockBought(
                profileId = _activeProfileId.value,
                ticker = ticker,
                companyName = company,
                exchange = exchange,
                shares = shares,
                pricePerShare = price,
                notes = notes
            )
            _userFeedbackMessage.value = "Logged ${shares.toInt()} shares of $ticker"
        }
    }

    fun pullLatestPrices() {
        viewModelScope.launch {
            val count = repository.pullLatestPrices(_activeProfileId.value)
            _userFeedbackMessage.value = "Pulled latest market prices for $count securities"
        }
    }

    fun saveSavingsAccount(account: SavingsAccountEntity) {
        viewModelScope.launch {
            repository.saveSavingsAccount(account.copy(profileId = _activeProfileId.value))
            _userFeedbackMessage.value = "Savings account saved"
        }
    }

    fun saveGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.saveGoal(goal.copy(profileId = _activeProfileId.value))
            _userFeedbackMessage.value = "Goal saved"
        }
    }

    fun saveProject(project: ProjectEntity, initialItems: List<Pair<String, Double>>) {
        viewModelScope.launch {
            val projectId = db.financeDao().insertProject(project.copy(profileId = _activeProfileId.value))
            for (item in initialItems) {
                db.financeDao().insertBudgetItem(
                    ProjectBudgetItemEntity(
                        profileId = _activeProfileId.value,
                        projectId = projectId,
                        name = item.first,
                        allocatedAmount = item.second
                    )
                )
            }
            _userFeedbackMessage.value = "Project '${project.name}' launched"
        }
    }

    fun toggleMilestone(milestone: ProjectMilestoneEntity) {
        viewModelScope.launch {
            repository.toggleMilestone(milestone)
        }
    }

    fun saveInvestmentTarget(target: InvestmentTargetEntity) {
        viewModelScope.launch {
            repository.saveInvestmentTarget(target)
            _userFeedbackMessage.value = "Target saved"
        }
    }

    fun updateExchangeRate(rate: Double) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(current.copy(usdToZigRate = rate))
            _userFeedbackMessage.value = "Exchange rate updated: 1 USD = $rate ZiG"
        }
    }

    fun setSecurityPin(pin: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.updateUserProfile(current.copy(pinCode = pin, isPinRequired = true))
            _isUnlocked.value = true
            _userFeedbackMessage.value = "PIN Protection activated"
        }
    }

    fun unlockWithPin(pin: String): Boolean {
        val currentPin = userProfile.value?.pinCode
        return if (currentPin.isNullOrBlank() || currentPin == pin) {
            _isUnlocked.value = true
            _securityError.value = null
            true
        } else {
            _securityError.value = "Incorrect PIN. Try again."
            false
        }
    }

    fun lockApp() {
        if (userProfile.value?.isPinRequired == true) {
            _isUnlocked.value = false
        }
    }

    fun saveTodoTask(task: com.example.data.local.entities.TodoTaskEntity) {
        viewModelScope.launch {
            repository.saveTodoTask(task)
            _userFeedbackMessage.value = "Task saved"
        }
    }

    fun toggleTodoTask(task: com.example.data.local.entities.TodoTaskEntity) {
        viewModelScope.launch {
            repository.toggleTodoTask(task)
            _userFeedbackMessage.value = if (!task.isCompleted) "Completed: ${task.title}" else "Marked pending"
        }
    }

    fun deleteTodoTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTodoTask(id)
            _userFeedbackMessage.value = "Task removed"
        }
    }

    fun saveBudgetCategory(category: BudgetCategoryEntity) {
        viewModelScope.launch {
            repository.saveBudgetCategory(category)
            _userFeedbackMessage.value = "Budget updated for ${category.category}"
        }
    }

    fun deleteBudgetCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteBudgetCategory(id)
            _userFeedbackMessage.value = "Category budget removed"
        }
    }

    fun checkBudgetThresholdAndAlert(category: String, addedAmount: Double = 0.0) {
        val catBudget = budgetCategories.value.find { it.category.equals(category, ignoreCase = true) }
        if (catBudget != null && catBudget.alertEnabled && catBudget.monthlyLimit > 0) {
            val startOfMonth = FinancialMath.getStartOfMonthMillis()
            val currentSpent = transactions.value
                .filter { it.type == "EXPENSE" && it.category.equals(category, ignoreCase = true) && it.dateMillis >= startOfMonth }
                .sumOf { it.amount } + addedAmount
            val thresholdRatio = (catBudget.alertThresholdPercent / 100.0).coerceAtLeast(0.01)
            val thresholdAmount = catBudget.monthlyLimit * thresholdRatio

            if (currentSpent >= thresholdAmount) {
                val sent = BudgetNotificationHelper.sendBudgetThresholdAlert(
                    context = getApplication(),
                    category = catBudget.category,
                    spentAmount = currentSpent,
                    limitAmount = catBudget.monthlyLimit,
                    thresholdPercent = catBudget.alertThresholdPercent
                )
                if (sent) {
                    _userFeedbackMessage.value = "Alert: ${catBudget.category} reached ${String.format(java.util.Locale.US, "%.0f%%", (currentSpent / catBudget.monthlyLimit) * 100)} of budget!"
                }
            }
        }
    }

    fun triggerTestBudgetAlert(category: String = "Groceries") {
        val catBudget = budgetCategories.value.find { it.category.equals(category, ignoreCase = true) }
        val limit = catBudget?.monthlyLimit ?: 350.00
        val threshold = catBudget?.alertThresholdPercent ?: 85.0
        val testSpent = limit * (threshold / 100.0)

        val sent = BudgetNotificationHelper.sendBudgetThresholdAlert(
            context = getApplication(),
            category = category,
            spentAmount = testSpent,
            limitAmount = limit,
            thresholdPercent = threshold
        )
        if (sent) {
            _userFeedbackMessage.value = "Test alert sent for $category (${String.format(java.util.Locale.US, "%.0f%%", threshold)} threshold)"
        } else {
            _userFeedbackMessage.value = "Alert dispatched. Note: Enable notifications permission if prompted."
        }
    }

    fun deleteSavingsAccount(id: Long) {
        viewModelScope.launch {
            repository.deleteSavingsAccount(id)
            _userFeedbackMessage.value = "Savings account removed"
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
            _userFeedbackMessage.value = "Goal removed"
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.saveProject(project.copy(profileId = _activeProfileId.value))
            _userFeedbackMessage.value = "Updated project '${project.name}'"
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            _userFeedbackMessage.value = "Project removed"
        }
    }

    fun updateProjectBudgetItem(item: ProjectBudgetItemEntity) {
        viewModelScope.launch {
            repository.updateProjectBudgetItem(item.copy(profileId = _activeProfileId.value))
            _userFeedbackMessage.value = "Updated allocation: ${item.name}"
        }
    }

    fun deleteProjectBudgetItem(id: Long) {
        viewModelScope.launch {
            repository.deleteProjectBudgetItem(id)
            _userFeedbackMessage.value = "Budget item removed"
        }
    }

    fun addProjectBudgetItem(item: ProjectBudgetItemEntity) {
        viewModelScope.launch {
            repository.addProjectBudgetItem(item.copy(profileId = _activeProfileId.value))
            _userFeedbackMessage.value = "Added allocation '${item.name}'"
        }
    }

    // --- User Profile Switching & Zero Profiles ---
    fun switchProfile(profileId: Long) {
        _activeProfileId.value = profileId
        _userFeedbackMessage.value = "Switched to profile"
    }

    fun createZeroProfile(name: String, currency: String = "USD", usdToZigRate: Double = 26.50) {
        viewModelScope.launch {
            val newId = repository.createProfileAtZero(name, currency, usdToZigRate)
            _activeProfileId.value = newId
            _userFeedbackMessage.value = "Created profile '$name' with everything at zero"
        }
    }

    fun resetActiveProfileToZero() {
        viewModelScope.launch {
            val currentId = _activeProfileId.value
            repository.resetProfileToZero(currentId)
            _userFeedbackMessage.value = "Profile reset: all balances and records are at zero"
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
            if (_activeProfileId.value == profileId) {
                _activeProfileId.value = 1L
            }
            _userFeedbackMessage.value = "Profile deleted"
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            _userFeedbackMessage.value = "Demonstration data reset successfully"
        }
    }
}
