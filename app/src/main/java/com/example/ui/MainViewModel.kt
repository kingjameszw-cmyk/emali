package com.example.ui

import android.app.Application
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    DASHBOARD,
    TRANSACTIONS,
    INVESTMENTS,
    SAVINGS_GOALS,
    PROJECTS,
    BUDGET_PLANNER,
    TODO_LIST,
    REPORTS,
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

    // Financial Dashboard Summary
    val dashboardSummary: StateFlow<DashboardFinancialSummary> = combine(
        accounts,
        transactions,
        holdings,
        savingsAccounts,
        selectedTimePeriod
    ) { accs, txs, holds, savings, period ->
        val window = when (period) {
            "This Week" -> FinancialMath.getStartOfWeekMillis()
            "This Year" -> FinancialMath.getStartOfYearMillis()
            "All Time" -> 0L
            else -> FinancialMath.getStartOfMonthMillis()
        }
        FinancialMath.computeSummary(
            accounts = accs,
            transactions = txs,
            holdings = holds,
            savingsAccounts = savings,
            goals = goals.value,
            projects = projects.value,
            projectBudgetItems = projectBudgetItems.value,
            timeWindowMillis = window
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardFinancialSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.0, 0.0)
    )

    init {
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
            repository.addTransaction(transaction)
            _userFeedbackMessage.value = "Recorded: ${transaction.category} ($${transaction.amount})"
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _userFeedbackMessage.value = "Transaction deleted"
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
