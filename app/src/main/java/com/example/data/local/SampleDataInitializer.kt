package com.example.data.local

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SampleDataInitializer {

    suspend fun populateIfEmpty(dao: FinanceDao) = withContext(Dispatchers.IO) {
        val existingProfile = dao.getUserProfileOnce()
        if (existingProfile != null) return@withContext // already seeded

        // 1. User Profile
        dao.insertOrUpdateProfile(
            UserProfileEntity(
                id = 1,
                userName = "Tendai Moyo",
                pinCode = null,
                isBiometricEnabled = false,
                isPinRequired = false,
                defaultCurrency = "USD",
                usdToZigRate = 26.50,
                isSetupCompleted = true
            )
        )

        // 2. Accounts & Wallets
        val ecocashUsdId = dao.insertAccount(
            AccountEntity(
                name = "EcoCash USD Wallet",
                type = "ECOCASH",
                currency = "USD",
                balance = 345.50,
                accountNumber = "+263 77 123 4567",
                isDefault = true
            )
        )

        val cbzBankId = dao.insertAccount(
            AccountEntity(
                name = "CBZ Nostro USD",
                type = "BANK",
                currency = "USD",
                balance = 1420.00,
                accountNumber = "0112-987654-001",
                isDefault = false
            )
        )

        val cashUsdId = dao.insertAccount(
            AccountEntity(
                name = "Cash USD (Wallet)",
                type = "CASH",
                currency = "USD",
                balance = 210.00,
                accountNumber = "Cash",
                isDefault = false
            )
        )

        val ctradeCashId = dao.insertAccount(
            AccountEntity(
                name = "C-Trade Trading Cash",
                type = "INVESTMENT",
                currency = "USD",
                balance = 350.00,
                accountNumber = "CT-889120",
                isDefault = false
            )
        )

        val ecocashZigId = dao.insertAccount(
            AccountEntity(
                name = "EcoCash ZiG Wallet",
                type = "ECOCASH",
                currency = "ZWG",
                balance = 4200.00,
                accountNumber = "+263 77 123 4567",
                isDefault = false
            )
        )

        // 3. C-Trade Zimbabwe Stock Holdings (VFEX & ZSE)
        dao.insertHolding(
            HoldingEntity(
                ticker = "ECO.zw",
                companyName = "Econet Wireless Zimbabwe",
                exchange = "VFEX",
                shares = 4500.0,
                avgBuyPrice = 0.22,
                currentPrice = 0.26,
                notes = "Telecommunications & FinTech leader (VFEX)"
            )
        )

        dao.insertHolding(
            HoldingEntity(
                ticker = "DLTA.zw",
                companyName = "Delta Corporation",
                exchange = "ZSE",
                shares = 1200.0,
                avgBuyPrice = 0.65,
                currentPrice = 0.78,
                notes = "Beverages & consumer goods anchor"
            )
        )

        dao.insertHolding(
            HoldingEntity(
                ticker = "SIM.zw",
                companyName = "Simbisa Brands",
                exchange = "VFEX",
                shares = 2000.0,
                avgBuyPrice = 0.34,
                currentPrice = 0.41,
                notes = "Quick service restaurants (VFEX)"
            )
        )

        dao.insertHolding(
            HoldingEntity(
                ticker = "INN.zw",
                companyName = "Innscor Africa",
                exchange = "VFEX",
                shares = 800.0,
                avgBuyPrice = 0.48,
                currentPrice = 0.52,
                notes = "Agro-processing & manufacturing conglomerate"
            )
        )

        dao.insertHolding(
            HoldingEntity(
                ticker = "PAD.zw",
                companyName = "Padenga Holdings",
                exchange = "VFEX",
                shares = 1500.0,
                avgBuyPrice = 0.18,
                currentPrice = 0.19,
                notes = "Agriculture & alternative mining"
            )
        )

        // 4. Investment Transactions
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 3600 * 1000

        dao.insertInvestmentTransaction(
            InvestmentTransactionEntity(
                ticker = "ECO.zw",
                companyName = "Econet Wireless",
                type = "BUY",
                shares = 2000.0,
                pricePerShare = 0.22,
                fees = 4.40,
                totalAmount = 444.40,
                dateMillis = now - 45 * dayMillis,
                notes = "Accumulation tranche 1 via C-Trade"
            )
        )

        dao.insertInvestmentTransaction(
            InvestmentTransactionEntity(
                ticker = "DLTA.zw",
                companyName = "Delta Corporation",
                type = "DIVIDEND_CASH",
                shares = 1200.0,
                pricePerShare = 0.04,
                fees = 0.0,
                totalAmount = 48.00,
                dateMillis = now - 12 * dayMillis,
                notes = "Interim cash dividend payout"
            )
        )

        dao.insertInvestmentTransaction(
            InvestmentTransactionEntity(
                ticker = "SIM.zw",
                companyName = "Simbisa Brands",
                type = "DIVIDEND_REINVEST",
                shares = 150.0,
                pricePerShare = 0.40,
                fees = 0.0,
                totalAmount = 60.00,
                dateMillis = now - 5 * dayMillis,
                notes = "Automatic dividend reinvestment into 150 shares"
            )
        )

        // 5. Investment Targets
        dao.insertInvestmentTarget(
            InvestmentTargetEntity(
                title = "Accumulate 10,000 Econet Shares",
                targetType = "SHARE_COUNT",
                ticker = "ECO.zw",
                targetValue = 10000.0,
                currentValue = 4500.0,
                targetDateMillis = now + 180 * dayMillis,
                notes = "Core VFEX portfolio building"
            )
        )

        dao.insertInvestmentTarget(
            InvestmentTargetEntity(
                title = "Build $10,000 C-Trade Portfolio",
                targetType = "PORTFOLIO_VALUE",
                targetValue = 10000.0,
                currentValue = 3500.0,
                targetDateMillis = now + 365 * dayMillis,
                notes = "VFEX and ZSE long-term holdings"
            )
        )

        dao.insertInvestmentTarget(
            InvestmentTargetEntity(
                title = "Earn $1,200 Annual Dividends",
                targetType = "DIVIDEND_INCOME",
                targetValue = 1200.0,
                currentValue = 340.0,
                targetDateMillis = now + 365 * dayMillis,
                notes = "Targeting ~10% annual dividend yield"
            )
        )

        // 6. Savings Accounts
        val emergencyFundId = dao.insertSavingsAccount(
            SavingsAccountEntity(
                name = "Emergency Reserve Fund",
                category = "EMERGENCY",
                currentBalance = 850.00,
                targetBalance = 3000.00,
                targetDateMillis = now + 120 * dayMillis,
                colorHex = "#10B981",
                savingsRule = "$150 / month"
            )
        )

        val solarSavingsId = dao.insertSavingsAccount(
            SavingsAccountEntity(
                name = "Solar Backup Power Fund",
                category = "ENERGY",
                currentBalance = 600.00,
                targetBalance = 1800.00,
                targetDateMillis = now + 90 * dayMillis,
                colorHex = "#F59E0B",
                savingsRule = "$200 / month"
            )
        )

        dao.insertSavingsAccount(
            SavingsAccountEntity(
                name = "Holiday & Family Fund",
                category = "PERSONAL",
                currentBalance = 250.00,
                targetBalance = 600.00,
                targetDateMillis = now + 60 * dayMillis,
                colorHex = "#3B82F6",
                savingsRule = "$100 / month"
            )
        )

        // 7. Financial Goals
        dao.insertGoal(
            GoalEntity(
                name = "Save $3,000 Emergency Buffer",
                description = "6 months of living expenses safely reserved",
                category = "EMERGENCY",
                targetAmount = 3000.00,
                currentAmount = 850.00,
                startDateMillis = now - 60 * dayMillis,
                targetDateMillis = now + 120 * dayMillis,
                priority = "HIGH",
                monthlyContribution = 150.00,
                status = "ACTIVE",
                linkedSavingsAccountId = emergencyFundId
            )
        )

        dao.insertGoal(
            GoalEntity(
                name = "Install 5kVA Solar Power System",
                description = "Zero-loadshedding solar inverter and lithium battery",
                category = "ENERGY",
                targetAmount = 2400.00,
                currentAmount = 600.00,
                startDateMillis = now - 30 * dayMillis,
                targetDateMillis = now + 90 * dayMillis,
                priority = "HIGH",
                monthlyContribution = 200.00,
                status = "ACTIVE",
                linkedSavingsAccountId = solarSavingsId
            )
        )

        dao.insertGoal(
            GoalEntity(
                name = "C-Trade 10,000 Share Milestone",
                description = "Accumulate high dividend paying equities",
                category = "INVESTMENT",
                targetAmount = 2600.00,
                currentAmount = 1170.00,
                startDateMillis = now - 90 * dayMillis,
                targetDateMillis = now + 180 * dayMillis,
                priority = "MEDIUM",
                monthlyContribution = 250.00,
                status = "ACTIVE"
            )
        )

        // 8. Projects (Business & Personal)
        val poultryProjectId = dao.insertProject(
            ProjectEntity(
                name = "Commercial Poultry Farm Project",
                description = "Broiler chicken unit (1,000 birds batch capacity) at Goromonzi plot",
                category = "AGRICULTURE",
                totalBudget = 4500.00,
                startDateMillis = now - 20 * dayMillis,
                targetDateMillis = now + 70 * dayMillis,
                status = "IN_PROGRESS",
                priority = "HIGH",
                coverIcon = "agriculture"
            )
        )

        // Poultry Budget Breakdown
        dao.insertBudgetItem(
            ProjectBudgetItemEntity(
                projectId = poultryProjectId,
                name = "Fowl Run Construction & Fencing",
                allocatedAmount = 1200.00,
                spentAmount = 1150.00
            )
        )
        dao.insertBudgetItem(
            ProjectBudgetItemEntity(
                projectId = poultryProjectId,
                name = "Day-Old Broiler Chicks (1,000 birds)",
                allocatedAmount = 950.00,
                spentAmount = 950.00
            )
        )
        dao.insertBudgetItem(
            ProjectBudgetItemEntity(
                projectId = poultryProjectId,
                name = "Broiler Starter & Finisher Feed",
                allocatedAmount = 1800.00,
                spentAmount = 1100.00
            )
        )
        dao.insertBudgetItem(
            ProjectBudgetItemEntity(
                projectId = poultryProjectId,
                name = "Vaccines, Heating & Gas Reserve",
                allocatedAmount = 550.00,
                spentAmount = 320.00
            )
        )

        // Poultry Milestones
        dao.insertMilestone(
            ProjectMilestoneEntity(
                projectId = poultryProjectId,
                title = "Fowl run structure & curtaining complete",
                isCompleted = true,
                dueDateMillis = now - 10 * dayMillis
            )
        )
        dao.insertMilestone(
            ProjectMilestoneEntity(
                projectId = poultryProjectId,
                title = "Day-old chicks delivered from Irvine's",
                isCompleted = true,
                dueDateMillis = now - 5 * dayMillis
            )
        )
        dao.insertMilestone(
            ProjectMilestoneEntity(
                projectId = poultryProjectId,
                title = "Switch to Broiler Finisher feed (Week 4)",
                isCompleted = false,
                dueDateMillis = now + 14 * dayMillis
            )
        )
        dao.insertMilestone(
            ProjectMilestoneEntity(
                projectId = poultryProjectId,
                title = "Slaughter, packaging & wholesale delivery",
                isCompleted = false,
                dueDateMillis = now + 35 * dayMillis
            )
        )

        // Project 2: Solar System
        val solarProjectId = dao.insertProject(
            ProjectEntity(
                name = "Residential Solar 5kVA Setup",
                description = "Complete off-grid backup installation with Deye inverter and Dyness battery",
                category = "ENERGY",
                totalBudget = 2400.00,
                startDateMillis = now - 10 * dayMillis,
                targetDateMillis = now + 60 * dayMillis,
                status = "PLANNING",
                priority = "MEDIUM",
                coverIcon = "solar"
            )
        )
        dao.insertBudgetItem(
            ProjectBudgetItemEntity(
                projectId = solarProjectId,
                name = "5kVA Hybrid Inverter (Deye)",
                allocatedAmount = 650.00,
                spentAmount = 0.00
            )
        )
        dao.insertBudgetItem(
            ProjectBudgetItemEntity(
                projectId = solarProjectId,
                name = "5.12kWh Lithium Battery",
                allocatedAmount = 1100.00,
                spentAmount = 0.00
            )
        )
        dao.insertBudgetItem(
            ProjectBudgetItemEntity(
                projectId = solarProjectId,
                name = "6x 550W Tier 1 Solar Panels & Rails",
                allocatedAmount = 650.00,
                spentAmount = 0.00
            )
        )

        // 9. Monthly Budget Categories
        dao.insertBudgetCategory(BudgetCategoryEntity(category = "Groceries", monthlyLimit = 350.00))
        dao.insertBudgetCategory(BudgetCategoryEntity(category = "Transport & Fuel", monthlyLimit = 180.00))
        dao.insertBudgetCategory(BudgetCategoryEntity(category = "Electricity", monthlyLimit = 60.00))
        dao.insertBudgetCategory(BudgetCategoryEntity(category = "Food & Dining", monthlyLimit = 120.00))
        dao.insertBudgetCategory(BudgetCategoryEntity(category = "Airtime & Data", monthlyLimit = 50.00))
        dao.insertBudgetCategory(BudgetCategoryEntity(category = "Business Expense", monthlyLimit = 800.00))

        // 10. Realistic Transactions (EcoCash, Bank, Transfers)
        dao.insertTransaction(
            TransactionEntity(
                accountId = cbzBankId,
                type = "INCOME",
                amount = 1850.00,
                fee = 0.0,
                totalDeducted = 1850.00,
                category = "Salary",
                recipientOrSender = "Delta Corporation Payroll",
                reference = "SAL-OCT-2026-091",
                description = "Monthly Salary Nostro USD",
                dateMillis = now - 4 * dayMillis
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                accountId = ecocashUsdId,
                type = "EXPENSE",
                amount = 35.00,
                fee = 0.70,
                totalDeducted = 35.70,
                category = "Transport & Fuel",
                recipientOrSender = "Zuva Petroleum Samora Machel",
                reference = "MP241018.1512.Z9910",
                description = "Fuel top-up 22 litres unleaded",
                dateMillis = now - 1 * dayMillis
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                accountId = ecocashUsdId,
                type = "EXPENSE",
                amount = 25.00,
                fee = 0.50,
                totalDeducted = 25.50,
                category = "Electricity",
                recipientOrSender = "ZETDC Prepaid Electricity",
                reference = "MP241017.0845.E4410",
                description = "Prepaid electricity token 180 kWh",
                dateMillis = now - 2 * dayMillis
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                accountId = ecocashUsdId,
                type = "EXPENSE",
                amount = 18.50,
                fee = 0.37,
                totalDeducted = 18.87,
                category = "Food & Dining",
                recipientOrSender = "Chicken Inn Belgravia",
                reference = "MP241016.1330.F8821",
                description = "Lunch meeting 2-piecer + drink",
                dateMillis = now - 3 * dayMillis
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                accountId = ecocashUsdId,
                type = "EXPENSE",
                amount = 8.00,
                fee = 0.16,
                totalDeducted = 8.16,
                category = "Airtime & Data",
                recipientOrSender = "Econet Wireless",
                reference = "MP241015.1910.A0012",
                description = "SmartBiz Monthly Data Bundle",
                dateMillis = now - 4 * dayMillis
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                accountId = ecocashUsdId,
                type = "EXPENSE",
                amount = 180.00,
                fee = 3.60,
                totalDeducted = 183.60,
                category = "Business Expense",
                recipientOrSender = "Agrifoods Feed Depot",
                reference = "MP241014.1105.P7712",
                description = "10x 50kg Bags Broiler Starter Feed",
                dateMillis = now - 5 * dayMillis,
                projectId = poultryProjectId
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                accountId = ecocashUsdId,
                type = "SAVINGS_DEPOSIT",
                amount = 150.00,
                fee = 1.50,
                totalDeducted = 151.50,
                category = "Savings",
                recipientOrSender = "Emergency Reserve Fund",
                reference = "MP241013.1000.S1122",
                description = "Monthly Emergency fund allocation",
                dateMillis = now - 6 * dayMillis,
                savingsAccountId = emergencyFundId
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                accountId = cbzBankId,
                type = "TRANSFER",
                amount = 250.00,
                fee = 2.00,
                totalDeducted = 252.00,
                category = "Transfers",
                recipientOrSender = "EcoCash Wallet (+263 77 123 4567)",
                reference = "CBZ-TRF-44129",
                description = "Bank to EcoCash wallet transfer",
                dateMillis = now - 7 * dayMillis
            )
        )

        // 11. Initial Financial & Project To-Do Tasks
        dao.insertTodoTask(
            com.example.data.local.entities.TodoTaskEntity(
                title = "Purchase ZETDC Prepaid Electricity token",
                description = "Recharge 180 kWh token via EcoCash Bill Pay before month-end tariff step",
                category = "BILLS",
                priority = "HIGH",
                dueDateMillis = now + 2 * dayMillis,
                estimatedAmount = 25.00
            )
        )

        dao.insertTodoTask(
            com.example.data.local.entities.TodoTaskEntity(
                title = "Procure 10 bags Broiler Finisher feed",
                description = "Pick up at Agrifoods depot for Week 4 brooder transition",
                category = "PROJECT",
                priority = "HIGH",
                dueDateMillis = now + 4 * dayMillis,
                linkedProjectId = poultryProjectId,
                estimatedAmount = 180.00
            )
        )

        dao.insertTodoTask(
            com.example.data.local.entities.TodoTaskEntity(
                title = "Accumulate 500 Econet shares on C-Trade",
                description = "Target VFEX order at $0.26 support level to advance 10,000 share goal",
                category = "CTRADE",
                priority = "MEDIUM",
                dueDateMillis = now + 6 * dayMillis,
                estimatedAmount = 130.00
            )
        )

        dao.insertTodoTask(
            com.example.data.local.entities.TodoTaskEntity(
                title = "Transfer $150 to Emergency Reserve Fund",
                description = "Monthly planned savings contribution from CBZ Nostro",
                category = "SAVINGS",
                priority = "HIGH",
                dueDateMillis = now + 10 * dayMillis,
                estimatedAmount = 150.00
            )
        )

        dao.insertTodoTask(
            com.example.data.local.entities.TodoTaskEntity(
                title = "Reconcile EcoCash USD monthly statement",
                description = "Import latest SMS transactions and verify operator IMTT charges",
                category = "ECOCASH",
                priority = "MEDIUM",
                dueDateMillis = now + 1 * dayMillis,
                isCompleted = true,
                estimatedAmount = 0.00
            )
        )

        dao.insertTodoTask(
            com.example.data.local.entities.TodoTaskEntity(
                title = "Review Delta Corp interim dividend distribution",
                description = "Confirm receipt of interim dividend in C-Trade trading account",
                category = "CTRADE",
                priority = "LOW",
                dueDateMillis = now + 14 * dayMillis,
                isCompleted = true,
                estimatedAmount = 48.00
            )
        )
    }
}
