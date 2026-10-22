package com.example

import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.HoldingEntity
import com.example.data.local.entities.TransactionEntity
import com.example.util.EcoCashParser
import com.example.util.FinancialMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testEcoCashSmsParser() {
        val sampleSms = "EcoCash: Transfer Confirmation: Trans ID: MP261019.1402.G88102. USD 22.50 sent to SPAR SUPERMARKET BELGRAVIA. Fee: USD 0.45. New Balance: USD 322.55."
        val parsed = EcoCashParser.parseRawText(sampleSms, accountId = 1)

        assertEquals(1, parsed.size)
        val tx = parsed[0]
        assertEquals("MP261019.1402.G88102", tx.reference)
        assertEquals(22.50, tx.amount, 0.001)
        assertEquals(0.45, tx.fee, 0.001)
        assertEquals(22.95, tx.totalDeducted, 0.001)
        assertEquals("EXPENSE", tx.type)
        assertEquals("Groceries", tx.category)
    }

    @Test
    fun testEcoCashElectricityAutoCategorization() {
        val sampleSms = "EcoCash: Bill Payment USD 15.00 to ZETDC Prepaid Electricity. Fee: USD 0.30. Trans ID: MP261019.0910.E1102."
        val parsed = EcoCashParser.parseRawText(sampleSms, accountId = 1)

        assertEquals(1, parsed.size)
        val tx = parsed[0]
        assertEquals(15.00, tx.amount, 0.001)
        assertEquals(0.30, tx.fee, 0.001)
        assertEquals("Electricity", tx.category)
    }

    @Test
    fun testStockHoldingGainCalculation() {
        val holding = HoldingEntity(
            ticker = "ECO.zw",
            companyName = "Econet Wireless Zimbabwe",
            exchange = "VFEX",
            shares = 4500.0,
            avgBuyPrice = 0.22,
            currentPrice = 0.26
        )

        assertEquals(990.0, holding.totalCostBasis, 0.001)
        assertEquals(1170.0, holding.currentMarketValue, 0.001)
        assertEquals(180.0, holding.unrealizedGain, 0.001)
        assertTrue(holding.returnPercentage > 18.0)
    }

    @Test
    fun testFinancialMathNonDoubleCounting() {
        val accounts = listOf(
            AccountEntity(id = 1, name = "EcoCash USD", type = "ECOCASH", balance = 300.0),
            AccountEntity(id = 2, name = "CBZ Nostro", type = "BANK", balance = 1000.0)
        )
        val transactions = listOf(
            TransactionEntity(
                accountId = 1,
                type = "TRANSFER",
                amount = 100.0,
                fee = 1.0,
                category = "Transfers",
                dateMillis = System.currentTimeMillis()
            ),
            TransactionEntity(
                accountId = 1,
                type = "EXPENSE",
                amount = 50.0,
                fee = 1.0,
                category = "Groceries",
                dateMillis = System.currentTimeMillis()
            ),
            TransactionEntity(
                accountId = 2,
                type = "INCOME",
                amount = 500.0,
                fee = 0.0,
                category = "Salary",
                dateMillis = System.currentTimeMillis()
            )
        )

        val summary = FinancialMath.computeSummary(
            accounts = accounts,
            transactions = transactions,
            holdings = emptyList(),
            savingsAccounts = emptyList(),
            goals = emptyList(),
            projects = emptyList(),
            projectBudgetItems = emptyList()
        )

        // Transfer amount must NOT be counted as consumption expense!
        assertEquals(50.0, summary.monthlyConsumptionExpense, 0.001)
        // Fees: 1.0 for transfer + 1.0 for expense = 2.0
        assertEquals(2.0, summary.monthlyEcoCashFees, 0.001)
        // Income = 500.0
        assertEquals(500.0, summary.monthlyIncome, 0.001)
        // Net Cash Flow = 500 - (50 + 2) = 448.0
        assertEquals(448.0, summary.netCashFlow, 0.001)
    }

    @Test
    fun testGoalMonthlyContributionCalculation() {
        val target = 1200.0
        val current = 0.0
        val targetDateMillis = System.currentTimeMillis() + (180L * 24 * 3600 * 1000) // 6 months

        val monthlyNeeded = FinancialMath.calculateRequiredMonthlyContribution(target, current, targetDateMillis)
        assertEquals(200.0, monthlyNeeded, 1.0)
    }

    @Test
    fun testAppTypeConvertersDate() {
        val converters = com.example.data.local.converters.AppTypeConverters()
        val nowMillis = System.currentTimeMillis()
        val date = java.util.Date(nowMillis)

        val timestamp = converters.dateToTimestamp(date)
        assertEquals(nowMillis, timestamp)

        val convertedDate = converters.fromTimestamp(timestamp)
        assertNotNull(convertedDate)
        assertEquals(nowMillis, convertedDate?.time)
    }

    @Test
    fun testAppTypeConvertersCurrency() {
        val converters = com.example.data.local.converters.AppTypeConverters()

        val usdType = com.example.data.local.converters.CurrencyType.USD
        val zigType = com.example.data.local.converters.CurrencyType.ZWG

        assertEquals("USD", converters.fromCurrencyType(usdType))
        assertEquals("ZWG", converters.fromCurrencyType(zigType))

        assertEquals(usdType, converters.toCurrencyType("USD"))
        assertEquals(zigType, converters.toCurrencyType("ZWG"))
        assertEquals(zigType, converters.toCurrencyType("ZiG"))
    }

    @Test
    fun testTodoTaskEntityCreationAndToggle() {
        val task = com.example.data.local.entities.TodoTaskEntity(
            title = "Purchase ZETDC Electricity Token",
            description = "180 kWh token via EcoCash",
            category = "BILLS",
            priority = "HIGH",
            estimatedAmount = 25.0,
            isCompleted = false
        )

        assertEquals("Purchase ZETDC Electricity Token", task.title)
        assertEquals("BILLS", task.category)
        assertEquals("HIGH", task.priority)
        assertEquals(25.0, task.estimatedAmount, 0.001)
        assertFalse(task.isCompleted)

        val completedTask = task.copy(isCompleted = true)
        assertTrue(completedTask.isCompleted)
    }

    @Test
    fun testBudgetPlannerThresholdCalculation() {
        val budget = com.example.data.local.entities.BudgetCategoryEntity(
            category = "Groceries",
            monthlyLimit = 350.0
        )
        val spent = 290.0
        val remaining = (budget.monthlyLimit - spent).coerceAtLeast(0.0)
        val ratio = spent / budget.monthlyLimit

        assertEquals(60.0, remaining, 0.001)
        assertTrue(ratio >= 0.8) // Reached 80% caution threshold
        assertTrue(ratio < 1.0) // Not yet exceeded
    }
}
