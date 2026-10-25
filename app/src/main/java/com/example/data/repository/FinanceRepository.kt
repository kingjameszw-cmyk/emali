package com.example.data.repository

import com.example.data.local.dao.FinanceDao
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
import com.example.util.EcoCashParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FinanceRepository(private val dao: FinanceDao) {

    val accounts: Flow<List<AccountEntity>> = dao.getAllAccounts()
    val transactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val holdings: Flow<List<HoldingEntity>> = dao.getAllHoldings()
    val investmentTransactions: Flow<List<InvestmentTransactionEntity>> = dao.getAllInvestmentTransactions()
    val investmentTargets: Flow<List<InvestmentTargetEntity>> = dao.getAllInvestmentTargets()
    val savingsAccounts: Flow<List<SavingsAccountEntity>> = dao.getAllSavingsAccounts()
    val goals: Flow<List<GoalEntity>> = dao.getAllGoals()
    val projects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val projectBudgetItems: Flow<List<ProjectBudgetItemEntity>> = dao.getAllProjectBudgetItems()
    val projectMilestones: Flow<List<ProjectMilestoneEntity>> = dao.getAllProjectMilestones()
    val budgetCategories: Flow<List<BudgetCategoryEntity>> = dao.getAllBudgetCategories()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val allProfiles: Flow<List<UserProfileEntity>> = dao.getAllProfiles()
    val todoTasks: Flow<List<com.example.data.local.entities.TodoTaskEntity>> = dao.getAllTodoTasks()

    fun getAccounts(profileId: Long): Flow<List<AccountEntity>> = dao.getAccountsByProfile(profileId)
    fun getTransactions(profileId: Long): Flow<List<TransactionEntity>> = dao.getTransactionsByProfile(profileId)
    fun getExpenses(profileId: Long): Flow<List<TransactionEntity>> = dao.getExpensesByProfile(profileId)
    fun getHoldings(profileId: Long): Flow<List<HoldingEntity>> = dao.getHoldingsByProfile(profileId)
    fun getInvestmentTransactions(profileId: Long): Flow<List<InvestmentTransactionEntity>> = dao.getInvestmentTransactionsByProfile(profileId)
    fun getInvestmentTargets(profileId: Long): Flow<List<InvestmentTargetEntity>> = dao.getInvestmentTargetsByProfile(profileId)
    fun getSavingsAccounts(profileId: Long): Flow<List<SavingsAccountEntity>> = dao.getSavingsAccountsByProfile(profileId)
    fun getGoals(profileId: Long): Flow<List<GoalEntity>> = dao.getGoalsByProfile(profileId)
    fun getProjects(profileId: Long): Flow<List<ProjectEntity>> = dao.getProjectsByProfile(profileId)
    fun getProjectBudgetItems(profileId: Long): Flow<List<ProjectBudgetItemEntity>> = dao.getProjectBudgetItemsByProfile(profileId)
    fun getProjectMilestones(profileId: Long): Flow<List<ProjectMilestoneEntity>> = dao.getProjectMilestonesByProfile(profileId)
    fun getBudgetCategories(profileId: Long): Flow<List<BudgetCategoryEntity>> = dao.getBudgetCategoriesByProfile(profileId)
    fun getTodoTasks(profileId: Long): Flow<List<com.example.data.local.entities.TodoTaskEntity>> = dao.getTodoTasksByProfile(profileId)
    fun getProfile(profileId: Long): Flow<UserProfileEntity?> = dao.getProfileById(profileId)

    suspend fun addTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        val id = dao.insertTransaction(transaction)

        // Adjust account balance
        val account = dao.getAccountById(transaction.accountId)
        if (account != null) {
            val delta = when (transaction.type) {
                "INCOME", "DIVIDEND" -> transaction.amount
                "EXPENSE", "SAVINGS_DEPOSIT", "INVESTMENT_BUY" -> -(transaction.amount + transaction.fee)
                "SAVINGS_WITHDRAWAL", "INVESTMENT_SELL" -> (transaction.amount - transaction.fee)
                "TRANSFER" -> -(transaction.amount + transaction.fee)
                else -> 0.0
            }
            dao.updateAccountBalance(account.id, (account.balance + delta).coerceAtLeast(0.0))
        }

        // If linked to savings account
        if (transaction.savingsAccountId != null) {
            val sa = dao.getSavingsAccountById(transaction.savingsAccountId)
            if (sa != null) {
                val newBal = if (transaction.type == "SAVINGS_DEPOSIT") {
                    sa.currentBalance + transaction.amount
                } else if (transaction.type == "SAVINGS_WITHDRAWAL") {
                    (sa.currentBalance - transaction.amount).coerceAtLeast(0.0)
                } else sa.currentBalance
                dao.updateSavingsAccount(sa.copy(currentBalance = newBal))
            }
        }

        // If linked to a project expense
        if (transaction.projectId != null && transaction.type == "EXPENSE") {
            val items = withContext(Dispatchers.IO) {
                // Find matching budget item or first item
            }
        }
        id
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTransaction(id)
    }

    /**
     * Import raw EcoCash SMS statements or CSV lines with duplicate detection
     * Returns Pair(importedCount, duplicateCount)
     */
    suspend fun importEcoCashStatement(rawText: String, accountId: Long): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val parsedList = EcoCashParser.parseRawText(rawText, accountId)
        var imported = 0
        var duplicates = 0

        for (tx in parsedList) {
            val existing = dao.findTransactionByReference(tx.reference)
            if (existing != null) {
                duplicates++
            } else {
                addTransaction(tx)
                imported++
            }
        }
        Pair(imported, duplicates)
    }

    suspend fun updateHolding(holding: HoldingEntity) = withContext(Dispatchers.IO) {
        dao.updateHolding(holding.copy(lastPriceUpdateMillis = System.currentTimeMillis()))
    }

    suspend fun updateHoldingQuantity(ticker: String, newShares: Double) = withContext(Dispatchers.IO) {
        val holding = dao.getHoldingByTicker(ticker)
        if (holding != null) {
            dao.updateHolding(holding.copy(shares = newShares))
        }
    }

    suspend fun deleteHolding(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteHolding(id)
    }

    suspend fun updateHoldingPrice(ticker: String, newPrice: Double) = withContext(Dispatchers.IO) {
        val holding = dao.getHoldingByTicker(ticker)
        if (holding != null) {
            dao.updateHolding(
                holding.copy(
                    currentPrice = newPrice,
                    lastPriceUpdateMillis = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Log and track quantity of stocks bought on C-Trade (VFEX / ZSE).
     * Tracks share count and purchase cost without deducting cash from bank/wallet accounts.
     */
    suspend fun logStockBought(
        profileId: Long,
        ticker: String,
        companyName: String,
        exchange: String,
        shares: Double,
        pricePerShare: Double,
        notes: String = ""
    ) = withContext(Dispatchers.IO) {
        val latestMarketPrice = com.example.util.ZimbabweMarketPriceService.pullLatestPrice(ticker, pricePerShare)

        // 1. Record investment transaction ledger entry
        dao.insertInvestmentTransaction(
            InvestmentTransactionEntity(
                profileId = profileId,
                ticker = ticker,
                companyName = companyName,
                type = "LOG_PURCHASE",
                shares = shares,
                pricePerShare = pricePerShare,
                fees = 0.0,
                totalAmount = shares * pricePerShare,
                notes = if (notes.isNotBlank()) notes else "Logged $shares shares on $exchange @ $${String.format("%.3f", pricePerShare)}"
            )
        )

        // 2. Update or insert holding
        val existing = dao.getHoldingByProfileAndTicker(profileId, ticker)
        if (existing != null) {
            val totalExistingCost = existing.shares * existing.avgBuyPrice
            val newTotalShares = existing.shares + shares
            val newAvgPrice = if (newTotalShares > 0) (totalExistingCost + (shares * pricePerShare)) / newTotalShares else pricePerShare

            dao.updateHolding(
                existing.copy(
                    shares = newTotalShares,
                    avgBuyPrice = newAvgPrice,
                    currentPrice = latestMarketPrice,
                    lastPriceUpdateMillis = System.currentTimeMillis()
                )
            )
        } else {
            dao.insertHolding(
                HoldingEntity(
                    profileId = profileId,
                    ticker = ticker,
                    companyName = companyName,
                    exchange = exchange,
                    shares = shares,
                    avgBuyPrice = pricePerShare,
                    currentPrice = latestMarketPrice,
                    lastPriceUpdateMillis = System.currentTimeMillis(),
                    notes = notes
                )
            )
        }
    }

    /**
     * Pulls the latest prices of stocks only and matches them against the quantities logged.
     * Returns count of updated holdings.
     */
    suspend fun pullLatestPrices(profileId: Long): Int = withContext(Dispatchers.IO) {
        val holdings = dao.getHoldingsListByProfile(profileId)
        val now = System.currentTimeMillis()
        var updatedCount = 0
        for (holding in holdings) {
            val latestPrice = com.example.util.ZimbabweMarketPriceService.pullLatestPrice(holding.ticker, holding.currentPrice)
            dao.updateHoldingPriceByProfileAndTicker(profileId, holding.ticker, latestPrice, now)
            updatedCount++
        }
        updatedCount
    }

    suspend fun recordDividend(
        ticker: String,
        companyName: String,
        shares: Double,
        amountPerShare: Double,
        isReinvest: Boolean,
        accountId: Long
    ) = withContext(Dispatchers.IO) {
        val totalDividend = shares * amountPerShare
        val type = if (isReinvest) "DIVIDEND_REINVEST" else "DIVIDEND_CASH"

        dao.insertInvestmentTransaction(
            InvestmentTransactionEntity(
                ticker = ticker,
                companyName = companyName,
                type = type,
                shares = shares,
                pricePerShare = amountPerShare,
                totalAmount = totalDividend,
                notes = if (isReinvest) "Reinvested into shares" else "Paid as cash dividend"
            )
        )

        val existingHolding = dao.getHoldingByTicker(ticker)
        if (isReinvest && existingHolding != null && existingHolding.currentPrice > 0) {
            val newShares = totalDividend / existingHolding.currentPrice
            dao.updateHolding(
                existingHolding.copy(shares = existingHolding.shares + newShares)
            )
        } else if (!isReinvest) {
            val account = dao.getAccountById(accountId)
            if (account != null) {
                dao.updateAccountBalance(account.id, account.balance + totalDividend)
            }
        }
    }

    suspend fun saveSavingsAccount(savingsAccount: SavingsAccountEntity) = withContext(Dispatchers.IO) {
        if (savingsAccount.id == 0L) {
            dao.insertSavingsAccount(savingsAccount)
        } else {
            dao.updateSavingsAccount(savingsAccount)
        }
    }

    suspend fun saveGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        if (goal.id == 0L) {
            dao.insertGoal(goal)
        } else {
            dao.updateGoal(goal)
        }
    }

    suspend fun saveProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        if (project.id == 0L) {
            dao.insertProject(project)
        } else {
            dao.updateProject(project)
        }
    }

    suspend fun addProjectBudgetItem(item: ProjectBudgetItemEntity) = withContext(Dispatchers.IO) {
        dao.insertBudgetItem(item)
    }

    suspend fun updateProjectBudgetItem(item: ProjectBudgetItemEntity) = withContext(Dispatchers.IO) {
        dao.updateBudgetItem(item)
    }

    suspend fun toggleMilestone(milestone: ProjectMilestoneEntity) = withContext(Dispatchers.IO) {
        dao.updateMilestone(milestone.copy(isCompleted = !milestone.isCompleted))
    }

    suspend fun addMilestone(milestone: ProjectMilestoneEntity) = withContext(Dispatchers.IO) {
        dao.insertMilestone(milestone)
    }

    suspend fun saveInvestmentTarget(target: InvestmentTargetEntity) = withContext(Dispatchers.IO) {
        if (target.id == 0L) {
            dao.insertInvestmentTarget(target)
        } else {
            dao.updateInvestmentTarget(target)
        }
    }

    suspend fun updateUserProfile(profile: UserProfileEntity) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun saveTodoTask(task: com.example.data.local.entities.TodoTaskEntity) = withContext(Dispatchers.IO) {
        if (task.id == 0L) {
            dao.insertTodoTask(task)
        } else {
            dao.updateTodoTask(task)
        }
    }

    suspend fun toggleTodoTask(task: com.example.data.local.entities.TodoTaskEntity) = withContext(Dispatchers.IO) {
        dao.updateTodoTaskStatus(task.id, !task.isCompleted)
    }

    suspend fun deleteTodoTask(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTodoTask(id)
    }

    suspend fun saveBudgetCategory(category: BudgetCategoryEntity) = withContext(Dispatchers.IO) {
        if (category.id == 0L) {
            dao.insertBudgetCategory(category)
        } else {
            dao.updateBudgetCategory(category)
        }
    }

    suspend fun deleteBudgetCategory(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteBudgetCategory(id)
    }

    suspend fun deleteSavingsAccount(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteSavingsAccount(id)
    }

    suspend fun deleteGoal(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteGoal(id)
    }

    suspend fun deleteProject(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteProject(id)
    }

    suspend fun deleteProjectBudgetItem(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteBudgetItem(id)
    }

    suspend fun deleteMilestone(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteMilestone(id)
    }

    suspend fun createProfileAtZero(name: String, currency: String, usdToZigRate: Double): Long = withContext(Dispatchers.IO) {
        val newProfileId = dao.insertProfile(
            UserProfileEntity(
                userName = name,
                defaultCurrency = currency,
                usdToZigRate = usdToZigRate,
                isZeroProfile = true
            )
        )
        // Baseline accounts with $0.00
        dao.insertAccount(
            AccountEntity(
                profileId = newProfileId,
                name = "EcoCash $currency Wallet",
                type = "ECOCASH",
                currency = currency,
                balance = 0.0,
                isDefault = true
            )
        )
        dao.insertAccount(
            AccountEntity(
                profileId = newProfileId,
                name = "CBZ Nostro USD",
                type = "BANK",
                currency = "USD",
                balance = 0.0,
                isDefault = false
            )
        )
        dao.insertAccount(
            AccountEntity(
                profileId = newProfileId,
                name = "Cash $currency Wallet",
                type = "CASH",
                currency = currency,
                balance = 0.0,
                isDefault = false
            )
        )
        if (currency == "USD") {
            dao.insertAccount(
                AccountEntity(
                    profileId = newProfileId,
                    name = "EcoCash ZiG Wallet",
                    type = "ECOCASH",
                    currency = "ZWG",
                    balance = 0.0,
                    isDefault = false
                )
            )
        }
        newProfileId
    }

    suspend fun resetProfileToZero(profileId: Long) = withContext(Dispatchers.IO) {
        dao.resetAccountBalancesForProfile(profileId)
        dao.deleteTransactionsForProfile(profileId)
        dao.deleteHoldingsForProfile(profileId)
        dao.deleteInvestmentTransactionsForProfile(profileId)
        dao.deleteInvestmentTargetsForProfile(profileId)
        dao.deleteSavingsForProfile(profileId)
        dao.deleteGoalsForProfile(profileId)
        dao.deleteProjectsForProfile(profileId)
        dao.deleteBudgetItemsForProfile(profileId)
        dao.deleteMilestonesForProfile(profileId)
        dao.deleteTodoTasksForProfile(profileId)
    }

    suspend fun deleteProfile(profileId: Long) = withContext(Dispatchers.IO) {
        resetProfileToZero(profileId)
        dao.deleteProfile(profileId)
    }

    suspend fun resetDemoData() = withContext(Dispatchers.IO) {
        dao.deleteAllTransactions()
        // Re-run sample data
        com.example.data.local.SampleDataInitializer.populateIfEmpty(dao)
    }
}
