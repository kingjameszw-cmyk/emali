package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // --- Accounts ---
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, id ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("UPDATE accounts SET balance = :balance WHERE id = :id")
    suspend fun updateAccountBalance(id: Long, balance: Double)

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = 'EXPENSE' ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY dateMillis DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE projectId = :projectId ORDER BY dateMillis DESC")
    fun getTransactionsByProject(projectId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE reference = :reference LIMIT 1")
    suspend fun findTransactionByReference(reference: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    // --- Holdings (C-Trade) ---
    @Query("SELECT * FROM holdings ORDER BY shares * currentPrice DESC")
    fun getAllHoldings(): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE ticker = :ticker LIMIT 1")
    suspend fun getHoldingByTicker(ticker: String): HoldingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: HoldingEntity): Long

    @Update
    suspend fun updateHolding(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE id = :id")
    suspend fun deleteHolding(id: Long)

    // --- Investment Transactions ---
    @Query("SELECT * FROM investment_transactions ORDER BY dateMillis DESC")
    fun getAllInvestmentTransactions(): Flow<List<InvestmentTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestmentTransaction(tx: InvestmentTransactionEntity): Long

    @Query("DELETE FROM investment_transactions WHERE id = :id")
    suspend fun deleteInvestmentTransaction(id: Long)

    // --- Investment Targets ---
    @Query("SELECT * FROM investment_targets ORDER BY targetDateMillis ASC")
    fun getAllInvestmentTargets(): Flow<List<InvestmentTargetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestmentTarget(target: InvestmentTargetEntity): Long

    @Update
    suspend fun updateInvestmentTarget(target: InvestmentTargetEntity)

    @Query("DELETE FROM investment_targets WHERE id = :id")
    suspend fun deleteInvestmentTarget(id: Long)

    // --- Savings Accounts ---
    @Query("SELECT * FROM savings_accounts ORDER BY currentBalance DESC")
    fun getAllSavingsAccounts(): Flow<List<SavingsAccountEntity>>

    @Query("SELECT * FROM savings_accounts WHERE id = :id LIMIT 1")
    suspend fun getSavingsAccountById(id: Long): SavingsAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsAccount(account: SavingsAccountEntity): Long

    @Update
    suspend fun updateSavingsAccount(account: SavingsAccountEntity)

    @Query("DELETE FROM savings_accounts WHERE id = :id")
    suspend fun deleteSavingsAccount(id: Long)

    // --- Financial Goals ---
    @Query("SELECT * FROM financial_goals ORDER BY priority = 'HIGH' DESC, targetDateMillis ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM financial_goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)

    // --- Projects ---
    @Query("SELECT * FROM projects ORDER BY priority = 'HIGH' DESC, id DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    // --- Project Budget Items ---
    @Query("SELECT * FROM project_budget_items WHERE projectId = :projectId")
    fun getBudgetItemsForProject(projectId: Long): Flow<List<ProjectBudgetItemEntity>>

    @Query("SELECT * FROM project_budget_items")
    fun getAllProjectBudgetItems(): Flow<List<ProjectBudgetItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetItem(item: ProjectBudgetItemEntity): Long

    @Update
    suspend fun updateBudgetItem(item: ProjectBudgetItemEntity)

    @Query("DELETE FROM project_budget_items WHERE id = :id")
    suspend fun deleteBudgetItem(id: Long)

    // --- Project Milestones ---
    @Query("SELECT * FROM project_milestones WHERE projectId = :projectId ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getMilestonesForProject(projectId: Long): Flow<List<ProjectMilestoneEntity>>

    @Query("SELECT * FROM project_milestones")
    fun getAllProjectMilestones(): Flow<List<ProjectMilestoneEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: ProjectMilestoneEntity): Long

    @Update
    suspend fun updateMilestone(milestone: ProjectMilestoneEntity)

    @Query("DELETE FROM project_milestones WHERE id = :id")
    suspend fun deleteMilestone(id: Long)

    // --- Budget Categories ---
    @Query("SELECT * FROM budget_categories")
    fun getAllBudgetCategories(): Flow<List<BudgetCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategory(category: BudgetCategoryEntity): Long

    @Update
    suspend fun updateBudgetCategory(category: BudgetCategoryEntity)

    @Query("DELETE FROM budget_categories WHERE id = :id")
    suspend fun deleteBudgetCategory(id: Long)

    // --- User Profile ---
    @Query("SELECT * FROM user_profile ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun getProfileById(id: Long): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdOnce(id: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Query("DELETE FROM user_profile WHERE id = :id")
    suspend fun deleteProfile(id: Long)

    // --- Profile-Scoped Selectors ---
    @Query("SELECT * FROM accounts WHERE profileId = :profileId ORDER BY isDefault DESC, id ASC")
    fun getAccountsByProfile(profileId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM transactions WHERE profileId = :profileId ORDER BY dateMillis DESC")
    fun getTransactionsByProfile(profileId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE profileId = :profileId AND type = 'EXPENSE' ORDER BY dateMillis DESC")
    fun getExpensesByProfile(profileId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM holdings WHERE profileId = :profileId ORDER BY shares * currentPrice DESC")
    fun getHoldingsByProfile(profileId: Long): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE profileId = :profileId")
    suspend fun getHoldingsListByProfile(profileId: Long): List<HoldingEntity>

    @Query("SELECT * FROM holdings WHERE profileId = :profileId AND ticker = :ticker LIMIT 1")
    suspend fun getHoldingByProfileAndTicker(profileId: Long, ticker: String): HoldingEntity?

    @Query("UPDATE holdings SET currentPrice = :price, lastPriceUpdateMillis = :timestamp WHERE profileId = :profileId AND ticker = :ticker")
    suspend fun updateHoldingPriceByProfileAndTicker(profileId: Long, ticker: String, price: Double, timestamp: Long)

    @Query("SELECT * FROM investment_transactions WHERE profileId = :profileId ORDER BY dateMillis DESC")
    fun getInvestmentTransactionsByProfile(profileId: Long): Flow<List<InvestmentTransactionEntity>>

    @Query("SELECT * FROM investment_targets WHERE profileId = :profileId ORDER BY targetDateMillis ASC")
    fun getInvestmentTargetsByProfile(profileId: Long): Flow<List<InvestmentTargetEntity>>

    @Query("SELECT * FROM savings_accounts WHERE profileId = :profileId ORDER BY currentBalance DESC")
    fun getSavingsAccountsByProfile(profileId: Long): Flow<List<SavingsAccountEntity>>

    @Query("SELECT * FROM financial_goals WHERE profileId = :profileId ORDER BY priority = 'HIGH' DESC, targetDateMillis ASC")
    fun getGoalsByProfile(profileId: Long): Flow<List<GoalEntity>>

    @Query("SELECT * FROM projects WHERE profileId = :profileId ORDER BY priority = 'HIGH' DESC, id DESC")
    fun getProjectsByProfile(profileId: Long): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM project_budget_items WHERE profileId = :profileId")
    fun getProjectBudgetItemsByProfile(profileId: Long): Flow<List<ProjectBudgetItemEntity>>

    @Query("SELECT * FROM project_milestones WHERE profileId = :profileId")
    fun getProjectMilestonesByProfile(profileId: Long): Flow<List<ProjectMilestoneEntity>>

    @Query("SELECT * FROM budget_categories WHERE profileId = :profileId")
    fun getBudgetCategoriesByProfile(profileId: Long): Flow<List<BudgetCategoryEntity>>

    @Query("SELECT * FROM todo_tasks WHERE profileId = :profileId ORDER BY isCompleted ASC, priority = 'HIGH' DESC, dueDateMillis ASC")
    fun getTodoTasksByProfile(profileId: Long): Flow<List<com.example.data.local.entities.TodoTaskEntity>>

    // --- Reset Active Profile to Zero ---
    @Query("UPDATE accounts SET balance = 0.0 WHERE profileId = :profileId")
    suspend fun resetAccountBalancesForProfile(profileId: Long)

    @Query("DELETE FROM transactions WHERE profileId = :profileId")
    suspend fun deleteTransactionsForProfile(profileId: Long)

    @Query("DELETE FROM holdings WHERE profileId = :profileId")
    suspend fun deleteHoldingsForProfile(profileId: Long)

    @Query("DELETE FROM investment_transactions WHERE profileId = :profileId")
    suspend fun deleteInvestmentTransactionsForProfile(profileId: Long)

    @Query("DELETE FROM investment_targets WHERE profileId = :profileId")
    suspend fun deleteInvestmentTargetsForProfile(profileId: Long)

    @Query("DELETE FROM savings_accounts WHERE profileId = :profileId")
    suspend fun deleteSavingsForProfile(profileId: Long)

    @Query("DELETE FROM financial_goals WHERE profileId = :profileId")
    suspend fun deleteGoalsForProfile(profileId: Long)

    @Query("DELETE FROM projects WHERE profileId = :profileId")
    suspend fun deleteProjectsForProfile(profileId: Long)

    @Query("DELETE FROM project_budget_items WHERE profileId = :profileId")
    suspend fun deleteBudgetItemsForProfile(profileId: Long)

    @Query("DELETE FROM project_milestones WHERE profileId = :profileId")
    suspend fun deleteMilestonesForProfile(profileId: Long)

    @Query("DELETE FROM todo_tasks WHERE profileId = :profileId")
    suspend fun deleteTodoTasksForProfile(profileId: Long)

    // --- Financial To-Do Tasks ---
    @Query("SELECT * FROM todo_tasks ORDER BY isCompleted ASC, priority = 'HIGH' DESC, dueDateMillis ASC")
    fun getAllTodoTasks(): Flow<List<com.example.data.local.entities.TodoTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodoTask(task: com.example.data.local.entities.TodoTaskEntity): Long

    @Update
    suspend fun updateTodoTask(task: com.example.data.local.entities.TodoTaskEntity)

    @Query("UPDATE todo_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateTodoTaskStatus(id: Long, completed: Boolean)

    @Query("DELETE FROM todo_tasks WHERE id = :id")
    suspend fun deleteTodoTask(id: Long)
}
