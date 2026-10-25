package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.TransactionEntity
import com.example.ui.theme.EcoCashBlue
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.util.CurrencyFormatter

@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    onAddTransaction: () -> Unit,
    onImportStatement: () -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onUpdateTransaction: ((TransactionEntity) -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    val filterOptions = listOf("ALL", "ECOCASH", "EXPENSE", "INCOME", "TRANSFER", "BANK")

    val filteredList = transactions.filter { tx ->
        val matchesSearch = searchQuery.isBlank() ||
                tx.recipientOrSender.contains(searchQuery, ignoreCase = true) ||
                tx.reference.contains(searchQuery, ignoreCase = true) ||
                tx.category.contains(searchQuery, ignoreCase = true) ||
                tx.description.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "ECOCASH" -> tx.reference.startsWith("MP") || tx.description.contains("EcoCash", ignoreCase = true)
            "EXPENSE" -> tx.type == "EXPENSE"
            "INCOME" -> tx.type == "INCOME" || tx.type == "DIVIDEND"
            "TRANSFER" -> tx.type == "TRANSFER" || tx.type == "SAVINGS_DEPOSIT"
            "BANK" -> tx.reference.startsWith("CBZ") || tx.description.contains("Bank", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesFilter
    }

    val totalSpent = filteredList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalFees = filteredList.sumOf { it.fee }
    val totalReceived = filteredList.filter { it.type == "INCOME" || it.type == "DIVIDEND" }.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("transactions_screen")
        ) {
            // Search & Import Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search merchant, reference...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tx_search_bar")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onImportStatement,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("import_statement_btn")
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import")
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedFilter = filter },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = filter,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Aggregate Summary Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Filtered Spent", style = MaterialTheme.typography.labelSmall)
                        Text(CurrencyFormatter.formatUsd(totalSpent), fontWeight = FontWeight.Bold, color = FinanceExpenseRed)
                    }
                    Column {
                        Text("EcoCash Fees", style = MaterialTheme.typography.labelSmall)
                        Text(CurrencyFormatter.formatUsd(totalFees), fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Filtered Received", style = MaterialTheme.typography.labelSmall)
                        Text(CurrencyFormatter.formatUsd(totalReceived), fontWeight = FontWeight.Bold, color = FinanceIncomeGreen)
                    }
                }
            }

            // Transactions list
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions found matching criteria",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { tx ->
                        TransactionDetailCard(
                            tx = tx,
                            onClick = { txToEdit = tx },
                            onDelete = { txToDelete = tx }
                        )
                    }
                }
            }
        }
    }

    // Edit transaction dialog
    txToEdit?.let { tx ->
        com.example.ui.components.EditTransactionDialog(
            transaction = tx,
            accounts = accounts,
            onDismiss = { txToEdit = null },
            onSave = { updated ->
                onUpdateTransaction?.invoke(updated)
                txToEdit = null
            },
            onDelete = { id ->
                onDeleteTransaction(id)
                txToEdit = null
            }
        )
    }

    // Delete confirmation dialog
    if (txToDelete != null) {
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete '${txToDelete?.recipientOrSender}' for $${txToDelete?.amount}?") },
            confirmButton = {
                Button(
                    onClick = {
                        txToDelete?.let { onDeleteTransaction(it.id) }
                        txToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { txToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TransactionDetailCard(
    tx: TransactionEntity,
    onClick: () -> Unit = {},
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (tx.reference.startsWith("MP")) EcoCashBlue.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (tx.reference.startsWith("MP")) "EcoCash" else tx.type,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (tx.reference.startsWith("MP")) EcoCashBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tx.recipientOrSender.ifBlank { tx.category },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                val isIncome = tx.type == "INCOME" || tx.type == "DIVIDEND"
                Text(
                    text = (if (isIncome) "+" else "-") + CurrencyFormatter.formatUsd(tx.amount),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) FinanceIncomeGreen else FinanceExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${tx.category} • ${CurrencyFormatter.formatDate(tx.dateMillis)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (tx.fee > 0) {
                        Text(
                            text = "Fee: $${String.format("%.2f", tx.fee)} • Total Deducted: $${String.format("%.2f", tx.totalDeducted)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Ref: ${tx.reference}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
