package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.BudgetCategoryEntity
import com.example.data.local.entities.TransactionEntity
import com.example.ui.components.ProgressBarWithTarget
import com.example.ui.components.StatCard
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.ZiGAmber
import com.example.util.CurrencyFormatter
import com.example.util.DashboardFinancialSummary

@Composable
fun ReportsScreen(
    summary: DashboardFinancialSummary,
    transactions: List<TransactionEntity>,
    budgetCategories: List<BudgetCategoryEntity>
) {
    val context = LocalContext.current

    // Group expenses by category
    val expensesByCategory = transactions
        .filter { it.type == "EXPENSE" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val totalExpense = expensesByCategory.sumOf { it.second }

    // EcoCash specific stats
    val ecoCashTxs = transactions.filter { it.reference.startsWith("MP") || it.description.contains("EcoCash", true) }
    val ecoCashTotalSpend = ecoCashTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val ecoCashTotalFees = ecoCashTxs.sumOf { it.fee }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Financial Analytics & Export", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Comprehensive ledger reconciliation and export", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = {
                            val csvContent = buildString {
                                appendLine("Date,Reference,Category,Recipient/Sender,Type,Amount,Fee,TotalDeducted,Currency")
                                for (tx in transactions) {
                                    appendLine("${CurrencyFormatter.formatShortDate(tx.dateMillis)},${tx.reference},\"${tx.category}\",\"${tx.recipientOrSender}\",${tx.type},${tx.amount},${tx.fee},${tx.totalDeducted},${tx.currency}")
                                }
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("MoneyPilot_Transactions_CSV", csvContent))
                            Toast.makeText(context, "Exported ${transactions.size} records to Clipboard (CSV)", Toast.LENGTH_LONG).show()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export CSV")
                    }
                }
            }
        }

        // Budget Threshold Warnings
        item {
            Text("Category Budget Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(budgetCategories.size) { idx ->
            val b = budgetCategories[idx]
            val actualSpent = expensesByCategory.find { it.first.equals(b.category, true) }?.second ?: 0.0
            val ratio = if (b.monthlyLimit > 0) (actualSpent / b.monthlyLimit).toFloat() else 0f

            val alertColor = when {
                ratio >= 1.0f -> FinanceExpenseRed
                ratio >= 0.8f -> ZiGAmber
                else -> MaterialTheme.colorScheme.primary
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(b.category, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (ratio >= 0.8f) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = alertColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "${CurrencyFormatter.formatUsd(actualSpent)} / ${CurrencyFormatter.formatUsd(b.monthlyLimit)}",
                                fontWeight = FontWeight.SemiBold,
                                color = alertColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ProgressBarWithTarget(
                        progress = ratio,
                        targetLabel = if (ratio >= 1.0f) "100% BUDGET EXCEEDED" else if (ratio >= 0.8f) "80% Threshold Reached" else "Within Limit",
                        currentLabel = "${String.format("%.1f", ratio * 100)}% Utilized",
                        color = alertColor
                    )
                }
            }
        }

        // EcoCash Dedicated Report Card
        item {
            Text("EcoCash Spending Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total EcoCash Spend", style = MaterialTheme.typography.labelSmall)
                            Text(CurrencyFormatter.formatUsd(ecoCashTotalSpend), fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Total IMTT & Fees", style = MaterialTheme.typography.labelSmall)
                            Text(CurrencyFormatter.formatUsd(ecoCashTotalFees), fontWeight = FontWeight.Bold, color = FinanceExpenseRed)
                        }
                        Column {
                            Text("Transactions", style = MaterialTheme.typography.labelSmall)
                            Text("${ecoCashTxs.size} events", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Spending by Category Breakdown
        item {
            Text("Spending by Category Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(expensesByCategory.size) { idx ->
            val (category, amount) = expensesByCategory[idx]
            val pct = if (totalExpense > 0) (amount / totalExpense) * 100.0 else 0.0

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${CurrencyFormatter.formatUsd(amount)} (${String.format("%.1f", pct)}%)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ProgressBarWithTarget(
                        progress = (pct / 100.0).toFloat(),
                        targetLabel = "",
                        currentLabel = "",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
