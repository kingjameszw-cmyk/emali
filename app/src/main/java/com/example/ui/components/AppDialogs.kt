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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.AccountEntity
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.HoldingEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.SavingsAccountEntity
import com.example.data.local.entities.TransactionEntity

val standardCategories = listOf(
    "Groceries", "Transport & Fuel", "Airtime & Data", "Electricity",
    "Food & Dining", "Rent & Housing", "Shopping", "Family Support",
    "School Fees", "Healthcare", "Business Expense", "Salary", "Dividends", "Other"
)

@Composable
fun AddTransactionDialog(
    accounts: List<AccountEntity>,
    projects: List<ProjectEntity>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var feeText by remember { mutableStateOf("0.00") }
    var type by remember { mutableStateOf("EXPENSE") }
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var recipient by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("MP" + System.currentTimeMillis().toString().takeLast(8)) }
    var description by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var selectedProjectId by remember { mutableStateOf<Long?>(null) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var accountExpanded by remember { mutableStateOf(false) }
    var projectExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Transaction", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("EXPENSE", "INCOME", "TRANSFER").forEach { t ->
                        val isSelected = type == t
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    type = t
                                    if (t == "INCOME") selectedCategory = "Salary"
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = t,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_amount_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fee (EcoCash IMTT / withdrawal fee)
                OutlinedTextField(
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Transaction Fee (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tx_fee_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Dropdown
                Box {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { categoryExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        standardCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Recipient / Merchant
                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("Recipient / Merchant (e.g. Zuva, Chicken Inn)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reference (EcoCash Ref)
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Reference ID (EcoCash Trans ID)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Account selection
                val selectedAccount = accounts.find { it.id == selectedAccountId }
                Box {
                    OutlinedTextField(
                        value = selectedAccount?.name ?: "Select Account",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid from / Received into Account") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { accountExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { accountExpanded = true }
                    )
                    DropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.name} ($${acc.balance})") },
                                onClick = {
                                    selectedAccountId = acc.id
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                if (projects.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val selectedProj = projects.find { it.id == selectedProjectId }
                    Box {
                        OutlinedTextField(
                            value = selectedProj?.name ?: "None (General Personal Expense)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Link to Project (Optional)") },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { projectExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { projectExpanded = true }
                        )
                        DropdownMenu(
                            expanded = projectExpanded,
                            onDismissRequest = { projectExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    selectedProjectId = null
                                    projectExpanded = false
                                }
                            )
                            projects.forEach { proj ->
                                DropdownMenuItem(
                                    text = { Text(proj.name) },
                                    onClick = {
                                        selectedProjectId = proj.id
                                        projectExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val fee = feeText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onSave(
                            TransactionEntity(
                                accountId = selectedAccountId,
                                type = type,
                                amount = amt,
                                fee = fee,
                                totalDeducted = if (type == "EXPENSE") amt + fee else amt,
                                currency = "USD",
                                category = selectedCategory,
                                recipientOrSender = recipient,
                                reference = reference,
                                description = description.ifBlank { "$type at $recipient" },
                                dateMillis = System.currentTimeMillis(),
                                projectId = selectedProjectId
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ImportStatementDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }

    val sampleEcoCashText = """
EcoCash: Transfer Confirmation: Trans ID: MP261019.1402.G88102. USD 22.50 sent to SPAR SUPERMARKET BELGRAVIA. Fee: USD 0.45. New Balance: USD 322.55.
EcoCash: Bill Payment USD 15.00 to ZETDC Prepaid Electricity. Fee: USD 0.30. Trans ID: MP261019.0910.E1102.
EcoCash: Transfer Confirmation: Trans ID: MP261018.1720.T3341. USD 40.00 sent to TOTAL ENERGIES AVONDALE. Fee: USD 0.80. New Balance: USD 337.85.
EcoCash: You have received USD 100.00 from Simba Moyo (0777654321). Trans ID: MP261018.1130.R9022. New Balance: USD 378.65.
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import EcoCash Statement / SMS", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Paste your EcoCash SMS confirmation messages, or a CSV statement export. MoneyPilot ZW will automatically parse amounts, fees, merchants, and categories with duplicate protection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { rawText = sampleEcoCashText },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Load Sample EcoCash SMS Batch")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    label = { Text("Paste Statement / SMS Text") },
                    placeholder = { Text("Paste one or more EcoCash SMS or CSV lines here...") },
                    minLines = 6,
                    maxLines = 10,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_statement_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawText.isNotBlank()) {
                        onImport(rawText)
                    }
                },
                modifier = Modifier.testTag("confirm_import_button")
            ) {
                Text("Process & Import")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LogStockBoughtDialog(
    initialHolding: HoldingEntity? = null,
    onDismiss: () -> Unit,
    onConfirm: (ticker: String, company: String, exchange: String, shares: Double, price: Double, notes: String) -> Unit
) {
    var ticker by remember { mutableStateOf(initialHolding?.ticker ?: "ECO.zw") }
    var company by remember { mutableStateOf(initialHolding?.companyName ?: "Econet Wireless Zimbabwe") }
    var exchange by remember { mutableStateOf(initialHolding?.exchange ?: "VFEX") }
    var sharesText by remember { mutableStateOf(if (initialHolding != null) initialHolding.shares.toInt().toString() else "500") }
    var priceText by remember { mutableStateOf(initialHolding?.avgBuyPrice?.toString() ?: "0.265") }
    var notes by remember { mutableStateOf(initialHolding?.notes ?: "") }

    val shares = sharesText.toDoubleOrNull() ?: 0.0
    val costPrice = priceText.toDoubleOrNull() ?: 0.0
    val totalCost = shares * costPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Log Stocks Bought",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Log and track the quantity of stocks bought on C-Trade (VFEX / ZSE).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                Text("Popular Listed Securities:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))

                // Quick stock picker chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ECO.zw", "DLTA.zw", "SIM.zw", "INN.zw", "PAD.zw").forEach { t ->
                        val isSelected = ticker.equals(t, ignoreCase = true)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    ticker = t
                                    val info = com.example.util.ZimbabweMarketPriceService.findStock(t)
                                    if (info != null) {
                                        company = info.name
                                        exchange = info.exchange
                                        priceText = info.basePriceUsd.toString()
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = t.substringBefore("."),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = ticker,
                    onValueChange = { input ->
                        ticker = input
                        val match = com.example.util.ZimbabweMarketPriceService.findStock(input)
                        if (match != null) {
                            company = match.name
                            exchange = match.exchange
                            if (priceText.isBlank() || priceText == "0.0") {
                                priceText = match.basePriceUsd.toString()
                            }
                        }
                    },
                    label = { Text("Stock Ticker (e.g. ECO.zw, DLTA.zw, SIM.zw)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Exchange:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("VFEX", "ZSE").forEach { ex ->
                        val isSel = exchange == ex
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { exchange = ex },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(ex, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = sharesText,
                    onValueChange = { sharesText = it },
                    label = { Text("Quantity of Shares Bought") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Purchase / Cost Price per Share (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Tracking Notes (Optional)") },
                    placeholder = { Text("e.g. C-Trade purchase ref, accumulation tranche") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total Logged Cost: $${String.format("%.2f", totalCost)}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Latest prices will be pulled and matched against this logged quantity.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (shares > 0 && costPrice > 0 && ticker.isNotBlank()) {
                        onConfirm(ticker.trim(), company.trim(), exchange, shares, costPrice, notes.trim())
                    }
                }
            ) {
                Text("Log Stock Holding")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UpdatePriceDialog(
    holding: HoldingEntity,
    onDismiss: () -> Unit,
    onUpdate: (Double) -> Unit
) {
    var newPriceText by remember { mutableStateOf(holding.currentPrice.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pull & Match Latest Price", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Pull and update the latest market quote for ${holding.companyName} (${holding.ticker}):")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val pulled = com.example.util.ZimbabweMarketPriceService.pullLatestPrice(holding.ticker, holding.currentPrice)
                        newPriceText = pulled.toString()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pull Latest Market Quote (${holding.exchange})")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPriceText,
                    onValueChange = { newPriceText = it },
                    label = { Text("Market Price (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                val p = newPriceText.toDoubleOrNull() ?: 0.0
                val previewVal = holding.shares * p
                Text(
                    text = "Matched Valuation: ${holding.shares.toInt()} shares × $${String.format("%.3f", p)} = $${String.format("%.2f", previewVal)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = newPriceText.toDoubleOrNull()
                    if (p != null && p > 0) {
                        onUpdate(p)
                    }
                }
            ) {
                Text("Match & Update")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditStockCardDialog(
    holding: HoldingEntity,
    onDismiss: () -> Unit,
    onSave: (HoldingEntity) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    var sharesText by remember { mutableStateOf(if (holding.shares % 1.0 == 0.0) holding.shares.toInt().toString() else holding.shares.toString()) }
    var currentPriceText by remember { mutableStateOf(holding.currentPrice.toString()) }
    var avgBuyPriceText by remember { mutableStateOf(holding.avgBuyPrice.toString()) }
    var companyNameText by remember { mutableStateOf(holding.companyName) }
    var exchangeText by remember { mutableStateOf(holding.exchange) }
    var notesText by remember { mutableStateOf(holding.notes) }

    val shares = sharesText.toDoubleOrNull() ?: 0.0
    val currentPrice = currentPriceText.toDoubleOrNull() ?: 0.0
    val avgPrice = avgBuyPriceText.toDoubleOrNull() ?: 0.0

    val previewMarketVal = shares * currentPrice
    val previewCostBasis = shares * avgPrice
    val previewGain = previewMarketVal - previewCostBasis
    val previewReturn = if (previewCostBasis > 0) (previewGain / previewCostBasis) * 100.0 else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit ${holding.ticker} Stock", fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = exchangeText,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Live preview banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Market Value", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "$${String.format("%.2f", previewMarketVal)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column {
                            Text("Cost Basis", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "$${String.format("%.2f", previewCostBasis)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column {
                            Text("Unrealized", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${if (previewGain >= 0) "+" else ""}${String.format("%.2f", previewGain)} (${String.format("%.1f", previewReturn)}%)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (previewGain >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity / Shares Field
                Text("Stock Quantity (Shares)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = sharesText,
                    onValueChange = { sharesText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("edit_shares_input"),
                    trailingIcon = { Text("shares", modifier = Modifier.padding(end = 12.dp), style = MaterialTheme.typography.labelSmall) }
                )

                // Quick Quantity Stepper Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(-100, -50, +50, +100, +500).forEach { delta ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val current = sharesText.toDoubleOrNull() ?: 0.0
                                    val updated = (current + delta).coerceAtLeast(0.0)
                                    sharesText = if (updated % 1.0 == 0.0) updated.toInt().toString() else updated.toString()
                                }
                        ) {
                            Text(
                                text = if (delta > 0) "+$delta" else "$delta",
                                modifier = Modifier.padding(vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Market Price Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Current Market Price (USD)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Pull Latest Quote",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                        modifier = Modifier.clickable {
                            val pulled = com.example.util.ZimbabweMarketPriceService.pullLatestPrice(holding.ticker, holding.currentPrice)
                            currentPriceText = pulled.toString()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = currentPriceText,
                    onValueChange = { currentPriceText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("edit_price_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Average Buy Price Field
                Text("Average Buy Price (USD)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = avgBuyPriceText,
                    onValueChange = { avgBuyPriceText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("edit_avg_price_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Company Name
                Text("Company Name", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = companyNameText,
                    onValueChange = { companyNameText = it },
                    modifier = Modifier.fillMaxWidth().testTag("edit_company_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Exchange Selector
                Text("Stock Exchange", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("VFEX", "ZSE").forEach { ex ->
                        val isSelected = exchangeText == ex
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { exchangeText = ex },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = ex,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes / Strategy
                Text("Notes / Investment Strategy", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text("e.g. Accumulate for long-term dividends") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalShares = sharesText.toDoubleOrNull() ?: holding.shares
                    val finalCurrentPrice = currentPriceText.toDoubleOrNull() ?: holding.currentPrice
                    val finalAvgPrice = avgBuyPriceText.toDoubleOrNull() ?: holding.avgBuyPrice

                    onSave(
                        holding.copy(
                            shares = finalShares,
                            currentPrice = finalCurrentPrice,
                            avgBuyPrice = finalAvgPrice,
                            companyName = companyNameText.ifBlank { holding.companyName },
                            exchange = exchangeText,
                            notes = notesText,
                            lastPriceUpdateMillis = System.currentTimeMillis()
                        )
                    )
                },
                modifier = Modifier.testTag("save_holding_btn")
            ) {
                Text("Save & Auto-Update")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = { onDelete(holding.id) },
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
                OutlinedButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

