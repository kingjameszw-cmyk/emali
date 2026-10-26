package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.first

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MoneyPilot ZW", appName)
  }

  @Test
  fun `test database initialization and seeding`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    com.example.data.local.SampleDataInitializer.populateIfEmpty(db.financeDao())
    val profile = db.financeDao().getUserProfileOnce()
    org.junit.Assert.assertNotNull(profile)
    org.junit.Assert.assertEquals("Tendai Moyo", profile?.userName)
  }

  @Test
  fun `test transaction dao persists expense and income data`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    val txDao = db.transactionDao()

    val expense = com.example.data.local.entities.TransactionEntity(
      accountId = 1,
      type = "EXPENSE",
      amount = 45.50,
      fee = 0.50,
      category = "Groceries",
      recipientOrSender = "TM Pick n Pay",
      reference = "TEST-EXP-001"
    )
    val income = com.example.data.local.entities.TransactionEntity(
      accountId = 1,
      type = "INCOME",
      amount = 1200.00,
      fee = 0.0,
      category = "Salary",
      recipientOrSender = "Employer Ltd",
      reference = "TEST-INC-001"
    )

    val expId = txDao.insertTransaction(expense)
    val incId = txDao.insertTransaction(income)
    org.junit.Assert.assertTrue(expId > 0)
    org.junit.Assert.assertTrue(incId > 0)

    val fetchedExp = txDao.getTransactionById(expId)
    org.junit.Assert.assertNotNull(fetchedExp)
    org.junit.Assert.assertEquals("EXPENSE", fetchedExp?.type)
    org.junit.Assert.assertEquals(45.50, fetchedExp?.amount ?: 0.0, 0.001)

    val fetchedInc = txDao.getTransactionById(incId)
    org.junit.Assert.assertNotNull(fetchedInc)
    org.junit.Assert.assertEquals("INCOME", fetchedInc?.type)
    org.junit.Assert.assertEquals(1200.00, fetchedInc?.amount ?: 0.0, 0.001)
  }

  @Test
  fun `test ctrade holding quantity editing and auto update`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    val financeDao = db.financeDao()

    val holding = com.example.data.local.entities.HoldingEntity(
      ticker = "TEST.zw",
      companyName = "Test Corporation",
      exchange = "VFEX",
      shares = 1000.0,
      avgBuyPrice = 0.20,
      currentPrice = 0.25
    )
    financeDao.insertHolding(holding)

    val stored = financeDao.getHoldingByTicker("TEST.zw")
    org.junit.Assert.assertNotNull(stored)
    org.junit.Assert.assertEquals(250.0, stored?.currentMarketValue ?: 0.0, 0.001)
    org.junit.Assert.assertEquals(200.0, stored?.totalCostBasis ?: 0.0, 0.001)

    // Edit quantity and verify auto-update calculations
    financeDao.updateHolding(stored!!.copy(shares = 2500.0, currentPrice = 0.30))
    val updated = financeDao.getHoldingByTicker("TEST.zw")
    org.junit.Assert.assertNotNull(updated)
    org.junit.Assert.assertEquals(2500.0, updated?.shares ?: 0.0, 0.001)
    org.junit.Assert.assertEquals(750.0, updated?.currentMarketValue ?: 0.0, 0.001)
    org.junit.Assert.assertEquals(500.0, updated?.totalCostBasis ?: 0.0, 0.001)
    org.junit.Assert.assertEquals(250.0, updated?.unrealizedGain ?: 0.0, 0.001)
  }

  @Test
  fun `test expense tracking creation and calculation`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    val txDao = db.transactionDao()

    val dateMillis = System.currentTimeMillis()
    val trackedExpense = com.example.data.local.entities.TransactionEntity(
      accountId = 1,
      type = "EXPENSE",
      amount = 85.0,
      fee = 1.70,
      category = "Electricity & ZETDC",
      recipientOrSender = "ZETDC Prepaid",
      reference = "EXP-ZETDC-99",
      description = "Prepaid Power Token",
      dateMillis = dateMillis
    )

    val id = txDao.insertTransaction(trackedExpense)
    val fetched = txDao.getTransactionById(id)
    org.junit.Assert.assertNotNull(fetched)
    org.junit.Assert.assertEquals(85.0, fetched?.amount ?: 0.0, 0.001)
    org.junit.Assert.assertEquals(1.70, fetched?.fee ?: 0.0, 0.001)
    org.junit.Assert.assertEquals("Electricity & ZETDC", fetched?.category)
    org.junit.Assert.assertEquals("ZETDC Prepaid", fetched?.recipientOrSender)
    org.junit.Assert.assertEquals(dateMillis, fetched?.dateMillis ?: 0L)
  }

  @Test
  fun `test monthly budget limit creation and remaining calculation`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    val financeDao = db.financeDao()

    val category = com.example.data.local.entities.BudgetCategoryEntity(
      category = "Groceries",
      monthlyLimit = 350.0
    )
    financeDao.insertBudgetCategory(category)
    val fetched = financeDao.getAllBudgetCategories().first()
    val groceriesBudget = fetched.find { it.category == "Groceries" }
    org.junit.Assert.assertNotNull(groceriesBudget)
    org.junit.Assert.assertEquals(350.0, groceriesBudget?.monthlyLimit ?: 0.0, 0.001)

    // Calculate remaining funds against sample spent amount
    val spent = 280.0
    val remaining = (groceriesBudget!!.monthlyLimit - spent).coerceAtLeast(0.0)
    val remainingRatio = (remaining / groceriesBudget.monthlyLimit).toFloat()
    org.junit.Assert.assertEquals(70.0, remaining, 0.001)
    org.junit.Assert.assertEquals(0.20f, remainingRatio, 0.01f)
  }

  @Test
  fun `test monthly income vs expense calculation and top spending category detection`() {
    val cal = java.util.Calendar.getInstance()
    val now = cal.timeInMillis

    val transactions = listOf(
      com.example.data.local.entities.TransactionEntity(
        type = "INCOME",
        amount = 3000.0,
        category = "Salary",
        dateMillis = now
      ),
      com.example.data.local.entities.TransactionEntity(
        type = "INCOME",
        amount = 250.0,
        category = "Dividends",
        dateMillis = now
      ),
      com.example.data.local.entities.TransactionEntity(
        type = "EXPENSE",
        amount = 450.0,
        category = "Groceries",
        dateMillis = now
      ),
      com.example.data.local.entities.TransactionEntity(
        type = "EXPENSE",
        amount = 120.0,
        category = "Transport & Fuel",
        dateMillis = now
      ),
      com.example.data.local.entities.TransactionEntity(
        type = "EXPENSE",
        amount = 80.0,
        category = "Electricity",
        dateMillis = now
      )
    )

    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense

    org.junit.Assert.assertEquals(3250.0, totalIncome, 0.001)
    org.junit.Assert.assertEquals(650.0, totalExpense, 0.001)
    org.junit.Assert.assertEquals(2600.0, netSavings, 0.001)

    // Verify top spending category
    val expensesByCategory = transactions
      .filter { it.type == "EXPENSE" }
      .groupBy { it.category }
      .mapValues { it.value.sumOf { tx -> tx.amount } }
      .toList()
      .sortedByDescending { it.second }

    val topCategory = expensesByCategory.first()
    org.junit.Assert.assertEquals("Groceries", topCategory.first)
    org.junit.Assert.assertEquals(450.0, topCategory.second, 0.001)

    val topCategoryPercent = (topCategory.second / totalExpense) * 100.0
    org.junit.Assert.assertEquals(69.23, topCategoryPercent, 0.1)
  }

  @Test
  fun `test theme mode preference switching`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("moneypilot_prefs", Context.MODE_PRIVATE)

    // Save dark mode
    prefs.edit().putString("theme_mode", com.example.ui.ThemeMode.DARK.name).apply()
    val saved = com.example.ui.ThemeMode.valueOf(prefs.getString("theme_mode", "SYSTEM")!!)
    org.junit.Assert.assertEquals(com.example.ui.ThemeMode.DARK, saved)

    // Switch to light mode
    prefs.edit().putString("theme_mode", com.example.ui.ThemeMode.LIGHT.name).apply()
    val updated = com.example.ui.ThemeMode.valueOf(prefs.getString("theme_mode", "SYSTEM")!!)
    org.junit.Assert.assertEquals(com.example.ui.ThemeMode.LIGHT, updated)
  }

  @Test
  fun `test savings account deposit operation`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    val financeDao = db.financeDao()

    val savingsAcc = com.example.data.local.entities.SavingsAccountEntity(
      name = "Emergency Buffer",
      category = "EMERGENCY",
      currentBalance = 500.0,
      targetBalance = 2500.0
    )
    val id = financeDao.insertSavingsAccount(savingsAcc)
    org.junit.Assert.assertTrue(id > 0)

    val existing = financeDao.getSavingsAccountById(id)
    org.junit.Assert.assertNotNull(existing)

    // Deposit $150
    financeDao.updateSavingsAccount(existing!!.copy(currentBalance = existing.currentBalance + 150.0))
    val updated = financeDao.getSavingsAccountById(id)
    org.junit.Assert.assertEquals(650.0, updated?.currentBalance ?: 0.0, 0.001)
  }

  @Test
  fun `test budget category alert threshold persistence`() = kotlinx.coroutines.runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = com.example.data.local.AppDatabase.getInstance(context)
    val financeDao = db.financeDao()

    val catWithAlert = com.example.data.local.entities.BudgetCategoryEntity(
      category = "Transport & Fuel",
      monthlyLimit = 200.0,
      alertThresholdPercent = 80.0,
      alertEnabled = true
    )
    val id = financeDao.insertBudgetCategory(catWithAlert)
    val fetched = financeDao.getAllBudgetCategories().first()
    val saved = fetched.find { it.id == id }

    org.junit.Assert.assertNotNull(saved)
    org.junit.Assert.assertEquals(200.0, saved?.monthlyLimit ?: 0.0, 0.001)
    org.junit.Assert.assertEquals(80.0, saved?.alertThresholdPercent ?: 0.0, 0.001)
    org.junit.Assert.assertTrue(saved?.alertEnabled ?: false)

    // Calculate threshold trigger amount
    val thresholdAmount = saved!!.monthlyLimit * (saved.alertThresholdPercent / 100.0)
    org.junit.Assert.assertEquals(160.0, thresholdAmount, 0.001)

    // Verify spent comparison
    val spentUnder = 150.0
    val spentOver = 165.0
    val isThresholdReachedUnder = spentUnder >= thresholdAmount
    val isThresholdReachedOver = spentOver >= thresholdAmount
    org.junit.Assert.assertFalse(isThresholdReachedUnder)
    org.junit.Assert.assertTrue(isThresholdReachedOver)
  }

  @Test
  fun `test budget notification channel creation and notification dispatch`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.util.BudgetNotificationHelper.createNotificationChannel(context)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val channel = notificationManager.getNotificationChannel(com.example.util.BudgetNotificationHelper.CHANNEL_ID)
    org.junit.Assert.assertNotNull(channel)
    org.junit.Assert.assertEquals(com.example.util.BudgetNotificationHelper.CHANNEL_NAME, channel.name)

    // Grant notification permission for API 33+ under Robolectric
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      org.robolectric.Shadows.shadowOf(context as android.app.Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    // Dispatch threshold alert
    val dispatched = com.example.util.BudgetNotificationHelper.sendBudgetThresholdAlert(
      context = context,
      category = "Groceries",
      spentAmount = 320.0,
      limitAmount = 350.0,
      thresholdPercent = 85.0
    )
    org.junit.Assert.assertTrue(dispatched)
  }
}

