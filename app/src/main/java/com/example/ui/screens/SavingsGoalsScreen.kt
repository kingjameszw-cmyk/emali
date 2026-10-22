package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.SavingsAccountEntity
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.AddSavingsAccountDialog
import com.example.ui.components.EditGoalDialog
import com.example.ui.components.EditSavingsAccountDialog
import com.example.ui.components.ProgressBarWithTarget
import com.example.ui.theme.FinanceIncomeGreen
import com.example.util.CurrencyFormatter
import com.example.util.FinancialMath

@Composable
fun SavingsGoalsScreen(
    savingsAccounts: List<SavingsAccountEntity>,
    goals: List<GoalEntity>,
    onSaveSavingsAccount: (SavingsAccountEntity) -> Unit,
    onUpdateSavingsAccount: (SavingsAccountEntity) -> Unit,
    onDeleteSavingsAccount: (Long) -> Unit,
    onSaveGoal: (GoalEntity) -> Unit,
    onUpdateGoal: (GoalEntity) -> Unit,
    onDeleteGoal: (Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddSavingsDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var editingSavingsAccount by remember { mutableStateOf<SavingsAccountEntity?>(null) }
    var editingGoal by remember { mutableStateOf<GoalEntity?>(null) }

    val totalSavings = savingsAccounts.sumOf { it.currentBalance }
    val totalTargetSavings = savingsAccounts.sumOf { it.targetBalance }

    val totalGoalTarget = goals.sumOf { it.targetAmount }
    val totalGoalCurrent = goals.sumOf { it.currentAmount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) showAddSavingsDialog = true
                    else showAddGoalDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_savings_goal")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("savings_goals_screen")
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Savings Accounts (${savingsAccounts.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Financial Goals (${goals.size})") }
                )
            }

            if (selectedTab == 0) {
                // SAVINGS ACCOUNTS
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Total Saved Reserves", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    CurrencyFormatter.formatUsd(totalSavings),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val progress = if (totalTargetSavings > 0) (totalSavings / totalTargetSavings).toFloat() else 0f
                                ProgressBarWithTarget(
                                    progress = progress,
                                    targetLabel = "Target: ${CurrencyFormatter.formatUsd(totalTargetSavings)}",
                                    currentLabel = "${String.format("%.1f", progress * 100)}% Funded",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (savingsAccounts.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No Savings Accounts Yet", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Tap + below to create emergency funds, school fees, or equipment reserves.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(savingsAccounts, key = { it.id }) { acc ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingSavingsAccount = acc }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Savings,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    acc.name,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                            Text(
                                                "Rule: ${acc.savingsRule}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                CurrencyFormatter.formatUsd(acc.currentBalance),
                                                fontWeight = FontWeight.ExtraBold,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = FinanceIncomeGreen
                                            )
                                            Text(
                                                "Target: ${CurrencyFormatter.formatUsd(acc.targetBalance)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { editingSavingsAccount = acc },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Account",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val accProgress = if (acc.targetBalance > 0) (acc.currentBalance / acc.targetBalance).toFloat() else 0f
                                val monthlyNeeded = FinancialMath.calculateRequiredMonthlyContribution(
                                    acc.targetBalance,
                                    acc.currentBalance,
                                    acc.targetDateMillis
                                )

                                ProgressBarWithTarget(
                                    progress = accProgress,
                                    targetLabel = "Req. $${String.format("%.0f", monthlyNeeded)}/mo",
                                    currentLabel = "${String.format("%.1f", accProgress * 100)}% Reached",
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick stepper buttons for instant edits
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(20.0, 50.0, 100.0).forEach { amount ->
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    onUpdateSavingsAccount(
                                                        acc.copy(currentBalance = acc.currentBalance + amount)
                                                    )
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                                Text(
                                                    "+$${amount.toInt()}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { editingSavingsAccount = acc },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                            Text(
                                                "Edit",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // FINANCIAL GOALS
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Financial Goals Overview", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    "${CurrencyFormatter.formatUsd(totalGoalCurrent)} of ${CurrencyFormatter.formatUsd(totalGoalTarget)}",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val progress = if (totalGoalTarget > 0) (totalGoalCurrent / totalGoalTarget).toFloat() else 0f
                                ProgressBarWithTarget(
                                    progress = progress,
                                    targetLabel = "${goals.count { it.status == "ACTIVE" }} Active Goals",
                                    currentLabel = "${String.format("%.1f", progress * 100)}% Total Progress",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    if (goals.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No Financial Goals Set", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Tap + below to define milestone targets like house deposit, solar backup, or vehicle purchase.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    items(goals, key = { it.id }) { goal ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingGoal = goal }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(goal.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        if (goal.description.isNotBlank()) {
                                            Text(
                                                goal.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (goal.priority == "HIGH") MaterialTheme.colorScheme.errorContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "${goal.priority} PRIORITY",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (goal.priority == "HIGH") MaterialTheme.colorScheme.onErrorContainer
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { editingGoal = goal },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Goal",
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Saved so far", style = MaterialTheme.typography.labelSmall)
                                        Text(CurrencyFormatter.formatUsd(goal.currentAmount), fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Target Amount", style = MaterialTheme.typography.labelSmall)
                                        Text(CurrencyFormatter.formatUsd(goal.targetAmount), fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Remaining", style = MaterialTheme.typography.labelSmall)
                                        Text(CurrencyFormatter.formatUsd(goal.remainingAmount), fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val goalProgress = (goal.progressPercentage / 100.0).toFloat()
                                val monthlyNeeded = FinancialMath.calculateRequiredMonthlyContribution(
                                    goal.targetAmount,
                                    goal.currentAmount,
                                    goal.targetDateMillis
                                )

                                ProgressBarWithTarget(
                                    progress = goalProgress,
                                    targetLabel = "Need: $${String.format("%.0f", monthlyNeeded)}/mo",
                                    currentLabel = "${String.format("%.1f", goal.progressPercentage)}% Completed",
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(50.0, 100.0).forEach { amount ->
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    onUpdateGoal(
                                                        goal.copy(currentAmount = goal.currentAmount + amount)
                                                    )
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                                Text(
                                                    "+$${amount.toInt()}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { editingGoal = goal },
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                            Text("Edit Goal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSavingsDialog) {
        AddSavingsAccountDialog(
            onDismiss = { showAddSavingsDialog = false },
            onSave = {
                onSaveSavingsAccount(it)
                showAddSavingsDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = {
                onSaveGoal(it)
                showAddGoalDialog = false
            }
        )
    }

    editingSavingsAccount?.let { acc ->
        EditSavingsAccountDialog(
            account = acc,
            onDismiss = { editingSavingsAccount = null },
            onSave = {
                onUpdateSavingsAccount(it)
                editingSavingsAccount = null
            },
            onDelete = {
                onDeleteSavingsAccount(it)
                editingSavingsAccount = null
            }
        )
    }

    editingGoal?.let { goal ->
        EditGoalDialog(
            goal = goal,
            onDismiss = { editingGoal = null },
            onSave = {
                onUpdateGoal(it)
                editingGoal = null
            },
            onDelete = {
                onDeleteGoal(it)
                editingGoal = null
            }
        )
    }
}
