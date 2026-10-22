package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.ProjectBudgetItemEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.SavingsAccountEntity
import com.example.data.local.entities.UserProfileEntity
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen

@Composable
fun AddSavingsAccountDialog(
    onDismiss: () -> Unit,
    onSave: (SavingsAccountEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var initialBalanceText by remember { mutableStateOf("0.00") }
    var ruleText by remember { mutableStateOf("$100 / month") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Savings Account / Fund", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (e.g. Emergency Fund, House Deposit)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("Target Balance (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text("Current / Starting Balance (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ruleText,
                    onValueChange = { ruleText = it },
                    label = { Text("Savings Rule / Plan (e.g. $150 / month, 10% of income)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetAmountText.toDoubleOrNull() ?: 1000.0
                    val current = initialBalanceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onSave(
                            SavingsAccountEntity(
                                name = name,
                                currentBalance = current,
                                targetBalance = target,
                                savingsRule = ruleText
                            )
                        )
                    }
                }
            ) {
                Text("Create Account")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditSavingsAccountDialog(
    account: SavingsAccountEntity,
    onDismiss: () -> Unit,
    onSave: (SavingsAccountEntity) -> Unit,
    onDelete: (Long) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var currentBalanceText by remember { mutableStateOf(account.currentBalance.toString()) }
    var targetBalanceText by remember { mutableStateOf(account.targetBalance.toString()) }
    var savingsRule by remember { mutableStateOf(account.savingsRule) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Savings Account?") },
            text = { Text("Are you sure you want to remove '${account.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(account.id)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Savings Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentBalanceText,
                    onValueChange = { currentBalanceText = it },
                    label = { Text("Current Balance (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Quick adjustment chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(-50.0, -10.0, 10.0, 50.0, 100.0).forEach { delta ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val current = currentBalanceText.toDoubleOrNull() ?: 0.0
                                    val next = (current + delta).coerceAtLeast(0.0)
                                    currentBalanceText = String.format("%.2f", next)
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (delta > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (delta > 0) "+$${delta.toInt()}" else "-$${(-delta).toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetBalanceText,
                    onValueChange = { targetBalanceText = it },
                    label = { Text("Target Balance (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = savingsRule,
                    onValueChange = { savingsRule = it },
                    label = { Text("Savings Plan / Rule") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Account")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val current = currentBalanceText.toDoubleOrNull() ?: account.currentBalance
                    val target = targetBalanceText.toDoubleOrNull() ?: account.targetBalance
                    if (name.isNotBlank()) {
                        onSave(
                            account.copy(
                                name = name,
                                currentBalance = current,
                                targetBalance = target,
                                savingsRule = savingsRule
                            )
                        )
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (GoalEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var currentAmountText by remember { mutableStateOf("0.00") }
    var priority by remember { mutableStateOf("HIGH") }
    var monthlyPlanText by remember { mutableStateOf("150") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Financial Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Title (e.g. Save $10k, Poultry Expansion)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Milestones") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("Target Amount (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentAmountText,
                    onValueChange = { currentAmountText = it },
                    label = { Text("Current Amount (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Priority Level:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                        val isSel = priority == p
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(p, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = monthlyPlanText,
                    onValueChange = { monthlyPlanText = it },
                    label = { Text("Planned Monthly Contribution (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetAmountText.toDoubleOrNull() ?: 1000.0
                    val current = currentAmountText.toDoubleOrNull() ?: 0.0
                    val monthly = monthlyPlanText.toDoubleOrNull() ?: 100.0
                    if (name.isNotBlank()) {
                        onSave(
                            GoalEntity(
                                name = name,
                                description = description,
                                targetAmount = target,
                                currentAmount = current,
                                priority = priority,
                                monthlyContribution = monthly
                            )
                        )
                    }
                }
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditGoalDialog(
    goal: GoalEntity,
    onDismiss: () -> Unit,
    onSave: (GoalEntity) -> Unit,
    onDelete: (Long) -> Unit
) {
    var name by remember { mutableStateOf(goal.name) }
    var description by remember { mutableStateOf(goal.description) }
    var targetAmountText by remember { mutableStateOf(goal.targetAmount.toString()) }
    var currentAmountText by remember { mutableStateOf(goal.currentAmount.toString()) }
    var priority by remember { mutableStateOf(goal.priority) }
    var monthlyPlanText by remember { mutableStateOf(goal.monthlyContribution.toString()) }
    var status by remember { mutableStateOf(goal.status) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Financial Goal?") },
            text = { Text("Are you sure you want to remove '${goal.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(goal.id)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Financial Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Milestones") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentAmountText,
                    onValueChange = { currentAmountText = it },
                    label = { Text("Current Saved Amount (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(20.0, 50.0, 100.0, 250.0).forEach { delta ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val current = currentAmountText.toDoubleOrNull() ?: 0.0
                                    currentAmountText = String.format("%.2f", (current + delta).coerceAtLeast(0.0))
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(modifier = Modifier.padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                                Text("+$${delta.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("Target Amount (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = monthlyPlanText,
                    onValueChange = { monthlyPlanText = it },
                    label = { Text("Monthly Contribution (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Priority Level:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                        val isSel = priority == p
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { priority = p },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(p, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Status:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ACTIVE", "COMPLETED").forEach { s ->
                        val isSel = status == s
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { status = s },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(s, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Goal")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val current = currentAmountText.toDoubleOrNull() ?: goal.currentAmount
                    val target = targetAmountText.toDoubleOrNull() ?: goal.targetAmount
                    val monthly = monthlyPlanText.toDoubleOrNull() ?: goal.monthlyContribution
                    if (name.isNotBlank()) {
                        onSave(
                            goal.copy(
                                name = name,
                                description = description,
                                currentAmount = current,
                                targetAmount = target,
                                monthlyContribution = monthly,
                                priority = priority,
                                status = status
                            )
                        )
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onSave: (project: ProjectEntity, initialItems: List<Pair<String, Double>>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("AGRICULTURE") }
    var totalBudgetText by remember { mutableStateOf("3000") }
    var item1Name by remember { mutableStateOf("Construction & Setup") }
    var item1Amount by remember { mutableStateOf("1200") }
    var item2Name by remember { mutableStateOf("Equipment & Assets") }
    var item2Amount by remember { mutableStateOf("1000") }
    var item3Name by remember { mutableStateOf("Operations & Reserves") }
    var item3Amount by remember { mutableStateOf("800") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Business / Personal Project", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name (e.g. Poultry Farm, Solar Power)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Project Scope & Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Category:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("AGRICULTURE", "ENERGY", "REAL_ESTATE", "BUSINESS").forEach { cat ->
                        val isSel = category == cat
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { category = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    cat.take(4),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = totalBudgetText,
                    onValueChange = { totalBudgetText = it },
                    label = { Text("Total Planned Budget (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Initial Budget Categories:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = item1Name, onValueChange = { item1Name = it }, label = { Text("Item 1") }, modifier = Modifier.weight(1.5f))
                    OutlinedTextField(value = item1Amount, onValueChange = { item1Amount = it }, label = { Text("Budget $") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = item2Name, onValueChange = { item2Name = it }, label = { Text("Item 2") }, modifier = Modifier.weight(1.5f))
                    OutlinedTextField(value = item2Amount, onValueChange = { item2Amount = it }, label = { Text("Budget $") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = item3Name, onValueChange = { item3Name = it }, label = { Text("Item 3") }, modifier = Modifier.weight(1.5f))
                    OutlinedTextField(value = item3Amount, onValueChange = { item3Amount = it }, label = { Text("Budget $") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = totalBudgetText.toDoubleOrNull() ?: 3000.0
                    val items = listOf(
                        Pair(item1Name, item1Amount.toDoubleOrNull() ?: 0.0),
                        Pair(item2Name, item2Amount.toDoubleOrNull() ?: 0.0),
                        Pair(item3Name, item3Amount.toDoubleOrNull() ?: 0.0)
                    ).filter { it.first.isNotBlank() && it.second > 0 }

                    if (name.isNotBlank()) {
                        onSave(
                            ProjectEntity(
                                name = name,
                                description = description,
                                category = category,
                                totalBudget = budget
                            ),
                            items
                        )
                    }
                }
            ) {
                Text("Launch Project")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditProjectDialog(
    project: ProjectEntity,
    onDismiss: () -> Unit,
    onSave: (ProjectEntity) -> Unit,
    onDelete: (Long) -> Unit
) {
    var name by remember { mutableStateOf(project.name) }
    var description by remember { mutableStateOf(project.description) }
    var category by remember { mutableStateOf(project.category) }
    var totalBudgetText by remember { mutableStateOf(project.totalBudget.toString()) }
    var status by remember { mutableStateOf(project.status) }
    var priority by remember { mutableStateOf(project.priority) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Project?") },
            text = { Text("Are you sure you want to delete '${project.name}' and all associated budget allocations?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(project.id)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Delete Project")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Project", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Scope & Location") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = totalBudgetText,
                    onValueChange = { totalBudgetText = it },
                    label = { Text("Total Planned Budget (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Category:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("AGRICULTURE", "ENERGY", "REAL_ESTATE", "BUSINESS").forEach { cat ->
                        val isSel = category == cat
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { category = cat },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(cat.take(4), style = MaterialTheme.typography.labelSmall, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Status:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("PLANNING", "IN_PROGRESS", "COMPLETED", "ON_HOLD").forEach { st ->
                        val isSel = status == st
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { status = st },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(st.take(4), style = MaterialTheme.typography.labelSmall, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Project")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budget = totalBudgetText.toDoubleOrNull() ?: project.totalBudget
                    if (name.isNotBlank()) {
                        onSave(
                            project.copy(
                                name = name,
                                description = description,
                                category = category,
                                totalBudget = budget,
                                status = status,
                                priority = priority
                            )
                        )
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditBudgetItemDialog(
    item: ProjectBudgetItemEntity,
    onDismiss: () -> Unit,
    onSave: (ProjectBudgetItemEntity) -> Unit,
    onDelete: (Long) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var allocatedAmountText by remember { mutableStateOf(item.allocatedAmount.toString()) }
    var spentAmountText by remember { mutableStateOf(item.spentAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Budget Allocation", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name (e.g. Fencing, Seeds)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = allocatedAmountText,
                    onValueChange = { allocatedAmountText = it },
                    label = { Text("Allocated Budget (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = spentAmountText,
                    onValueChange = { spentAmountText = it },
                    label = { Text("Spent Amount (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        onDelete(item.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Allocation")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val alloc = allocatedAmountText.toDoubleOrNull() ?: item.allocatedAmount
                    val spent = spentAmountText.toDoubleOrNull() ?: item.spentAmount
                    if (name.isNotBlank()) {
                        onSave(
                            item.copy(
                                name = name,
                                allocatedAmount = alloc,
                                spentAmount = spent
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddBudgetItemDialog(
    projectId: Long,
    onDismiss: () -> Unit,
    onSave: (ProjectBudgetItemEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var allocatedAmountText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Budget Allocation", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name (e.g. Feeds, Solar Inverter)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = allocatedAmountText,
                    onValueChange = { allocatedAmountText = it },
                    label = { Text("Allocated Budget (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val alloc = allocatedAmountText.toDoubleOrNull() ?: 100.0
                    if (name.isNotBlank()) {
                        onSave(
                            ProjectBudgetItemEntity(
                                projectId = projectId,
                                name = name,
                                allocatedAmount = alloc,
                                spentAmount = 0.0
                            )
                        )
                    }
                }
            ) {
                Text("Add Allocation")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateProfileDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, currency: String, usdToZigRate: Double) -> Unit
) {
    var profileName by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USD") }
    var exchangeRateText by remember { mutableStateOf("26.50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Profile (At Zero)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Create a clean user profile starting with everything at zero ($0.00 balances, zero transactions, empty portfolio & projects).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Profile / Business Name (e.g. My Clean Ledger)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Primary Currency:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("USD", "ZWG", "ZAR").forEach { curr ->
                        val isSel = currency == curr
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currency = curr },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(curr, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = exchangeRateText,
                    onValueChange = { exchangeRateText = it },
                    label = { Text("1 USD to ZiG Exchange Rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = FinanceIncomeGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Starts completely fresh with $0.00 balances", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = exchangeRateText.toDoubleOrNull() ?: 26.50
                    if (profileName.isNotBlank()) {
                        onCreate(profileName.trim(), currency, rate)
                    }
                }
            ) {
                Text("Create Clean Profile")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ProfileSwitcherDialog(
    profiles: List<UserProfileEntity>,
    activeProfileId: Long,
    onDismiss: () -> Unit,
    onSwitchProfile: (Long) -> Unit,
    onCreateNewProfile: () -> Unit,
    onResetCurrentToZero: () -> Unit,
    onDeleteProfile: (Long) -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Profile to Zero?") },
            text = { Text("All account balances will be set to $0.00, and all transactions, holdings, savings, goals, and projects for this profile will be cleared.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetCurrentToZero()
                        showResetConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinanceExpenseRed)
                ) {
                    Text("Reset Everything to Zero")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch User Profile", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Select a profile to switch ledgers, or create a brand new profile starting at zero.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                profiles.forEach { profile ->
                    val isActive = profile.id == activeProfileId
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSwitchProfile(profile.id)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        profile.userName,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                    Text(
                                        if (profile.isZeroProfile) "Clean Slate (At Zero) • ${profile.defaultCurrency}"
                                        else "Standard Demo • ${profile.defaultCurrency}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isActive) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            "ACTIVE",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                } else if (profiles.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteProfile(profile.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = FinanceExpenseRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onCreateNewProfile()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Profile At Zero")
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = FinanceExpenseRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset Current Profile To Zero")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
