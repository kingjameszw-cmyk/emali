package com.example.util

import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.HoldingEntity
import com.example.data.local.entities.ProjectBudgetItemEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.SavingsAccountEntity
import com.example.data.local.entities.TransactionEntity
import java.util.Calendar

data class DashboardFinancialSummary(
    val netWorth: Double,
    val cashAndWalletsBalance: Double,
    val totalSavingsBalance: Double,
    val portfolioMarketValue: Double,
    val portfolioCostBasis: Double,
    val portfolioUnrealizedGain: Double,
    val portfolioReturnPercent: Double,
    val monthlyIncome: Double,
    val monthlyConsumptionExpense: Double,
    val monthlyEcoCashFees: Double,
    val monthlySavingsContributions: Double,
    val netCashFlow: Double,
    val savingsRatePercent: Double,
    val goalsCompletionPercent: Double,
    val activeProjectsCount: Int,
    val totalProjectsBudget: Double,
    val totalProjectsSpent: Double
)

object FinancialMath {

    /**
     * Compute clean, non-double-counted financial summary.
     * Note: Transfers, savings deposits, and investment buys do NOT count as consumption expenses.
     */
    fun computeSummary(
        accounts: List<com.example.data.local.entities.AccountEntity>,
        transactions: List<TransactionEntity>,
        holdings: List<HoldingEntity>,
        savingsAccounts: List<SavingsAccountEntity>,
        goals: List<GoalEntity>,
        projects: List<ProjectEntity>,
        projectBudgetItems: List<ProjectBudgetItemEntity>,
        timeWindowMillis: Long = getStartOfMonthMillis()
    ): DashboardFinancialSummary {
        // Cash in accounts
        val totalCash = accounts.sumOf { it.balance }
        val totalSavings = savingsAccounts.sumOf { it.currentBalance }

        // Portfolio
        val portfolioMarketValue = holdings.sumOf { it.currentMarketValue }
        val portfolioCostBasis = holdings.sumOf { it.totalCostBasis }
        val portfolioUnrealizedGain = portfolioMarketValue - portfolioCostBasis
        val portfolioReturnPercent = if (portfolioCostBasis > 0) (portfolioUnrealizedGain / portfolioCostBasis) * 100.0 else 0.0

        val netWorth = totalCash + totalSavings + portfolioMarketValue

        // Filter transactions for selected time period
        val recentTransactions = transactions.filter { it.dateMillis >= timeWindowMillis }

        var incomeSum = 0.0
        var consumptionExpenseSum = 0.0
        var feesSum = 0.0
        var savingsContributionsSum = 0.0

        for (tx in recentTransactions) {
            when (tx.type) {
                "INCOME", "DIVIDEND" -> {
                    incomeSum += tx.amount
                }
                "EXPENSE" -> {
                    consumptionExpenseSum += tx.amount
                    feesSum += tx.fee
                }
                "SAVINGS_DEPOSIT" -> {
                    savingsContributionsSum += tx.amount
                    feesSum += tx.fee
                }
                "TRANSFER" -> {
                    // Internal transfer does NOT count as income or expense
                    feesSum += tx.fee
                }
                "INVESTMENT_BUY" -> {
                    // Capital allocation, not consumption
                    feesSum += tx.fee
                }
            }
        }

        val totalExpensesWithFees = consumptionExpenseSum + feesSum
        val netCashFlow = incomeSum - totalExpensesWithFees
        val savingsRatePercent = if (incomeSum > 0) {
            ((savingsContributionsSum + (netCashFlow.coerceAtLeast(0.0))) / incomeSum) * 100.0
        } else 0.0

        // Goals completion percentage
        val totalGoalTarget = goals.sumOf { it.targetAmount }
        val totalGoalCurrent = goals.sumOf { it.currentAmount }
        val goalsCompletionPercent = if (totalGoalTarget > 0) (totalGoalCurrent / totalGoalTarget) * 100.0 else 0.0

        // Projects
        val activeProjects = projects.filter { it.status == "IN_PROGRESS" || it.status == "PLANNING" }
        val totalProjectsBudget = activeProjects.sumOf { it.totalBudget }
        val totalProjectsSpent = projectBudgetItems.filter { item -> activeProjects.any { it.id == item.projectId } }.sumOf { it.spentAmount }

        return DashboardFinancialSummary(
            netWorth = netWorth,
            cashAndWalletsBalance = totalCash,
            totalSavingsBalance = totalSavings,
            portfolioMarketValue = portfolioMarketValue,
            portfolioCostBasis = portfolioCostBasis,
            portfolioUnrealizedGain = portfolioUnrealizedGain,
            portfolioReturnPercent = portfolioReturnPercent,
            monthlyIncome = incomeSum,
            monthlyConsumptionExpense = consumptionExpenseSum,
            monthlyEcoCashFees = feesSum,
            monthlySavingsContributions = savingsContributionsSum,
            netCashFlow = netCashFlow,
            savingsRatePercent = savingsRatePercent.coerceIn(0.0, 100.0),
            goalsCompletionPercent = goalsCompletionPercent.coerceIn(0.0, 100.0),
            activeProjectsCount = activeProjects.size,
            totalProjectsBudget = totalProjectsBudget,
            totalProjectsSpent = totalProjectsSpent
        )
    }

    fun getStartOfMonthMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfWeekMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfDayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfYearMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Calculate required monthly contribution for a goal or savings target
     */
    fun calculateRequiredMonthlyContribution(targetAmount: Double, currentAmount: Double, targetDateMillis: Long): Double {
        val remaining = (targetAmount - currentAmount).coerceAtLeast(0.0)
        if (remaining <= 0.0) return 0.0

        val now = System.currentTimeMillis()
        val diffMillis = (targetDateMillis - now).coerceAtLeast(1L)
        val monthsRemaining = kotlin.math.max(1.0, kotlin.math.round(diffMillis.toDouble() / (1000.0 * 60 * 60 * 24 * 30.0)))
        return remaining / monthsRemaining
    }
}
