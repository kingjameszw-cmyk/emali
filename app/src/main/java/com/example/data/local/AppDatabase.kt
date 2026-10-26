package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.converters.AppTypeConverters
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.FinanceDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.UserDao
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
import com.example.data.local.entities.TodoTaskEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserProfileEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        HoldingEntity::class,
        InvestmentTransactionEntity::class,
        InvestmentTargetEntity::class,
        SavingsAccountEntity::class,
        GoalEntity::class,
        ProjectEntity::class,
        ProjectBudgetItemEntity::class,
        ProjectMilestoneEntity::class,
        BudgetCategoryEntity::class,
        UserProfileEntity::class,
        TodoTaskEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    // Primary Entity DAOs
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun todoDao(): com.example.data.local.dao.TodoDao

    // Aggregated Finance DAO for full application domain
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moneypilot_zw.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
