package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

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
}

