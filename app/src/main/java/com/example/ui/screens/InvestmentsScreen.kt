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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.HoldingEntity
import com.example.data.local.entities.InvestmentTargetEntity
import com.example.data.local.entities.InvestmentTransactionEntity
import com.example.ui.components.EditStockCardDialog
import com.example.ui.components.LogStockBoughtDialog
import com.example.ui.components.ProgressBarWithTarget
import com.example.ui.components.UpdatePriceDialog
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.util.CurrencyFormatter

@Composable
fun InvestmentsScreen(
    holdings: List<HoldingEntity>,
    investmentTransactions: List<InvestmentTransactionEntity>,
    investmentTargets: List<InvestmentTargetEntity>,
    onLogStockBought: (ticker: String, company: String, exchange: String, shares: Double, price: Double, notes: String) -> Unit,
    onPullLatestPrices: () -> Unit,
    onUpdatePrice: (ticker: String, newPrice: Double) -> Unit,
    onUpdateHolding: (HoldingEntity) -> Unit,
    onDeleteHolding: ((Long) -> Unit)? = null
) {
    var selectedHoldingForPriceUpdate by remember { mutableStateOf<HoldingEntity?>(null) }
    var selectedHoldingForEdit by remember { mutableStateOf<HoldingEntity?>(null) }
    var showLogStockDialog by remember { mutableStateOf(false) }

    val totalMarketValue = holdings.sumOf { it.currentMarketValue }
    val totalCostBasis = holdings.sumOf { it.totalCostBasis }
    val totalUnrealizedGain = totalMarketValue - totalCostBasis
    val totalReturnPercent = if (totalCostBasis > 0) (totalUnrealizedGain / totalCostBasis) * 100.0 else 0.0

    val totalDividends = investmentTransactions
        .filter { it.type.startsWith("DIVIDEND") }
        .sumOf { it.totalAmount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLogStockDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag("fab_log_stock")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Stocks Bought")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("investments_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Portfolio Summary Card with "Pull Latest Prices" button
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "C-Trade Logged Portfolio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Tracking logged stock quantities & live market quotes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "VFEX & ZSE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = CurrencyFormatter.formatUsd(totalMarketValue),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (totalUnrealizedGain >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (totalUnrealizedGain >= 0) FinanceIncomeGreen else FinanceExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${CurrencyFormatter.formatUsd(totalUnrealizedGain)} (${CurrencyFormatter.formatPercent(totalReturnPercent)})",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (totalUnrealizedGain >= 0) FinanceIncomeGreen else FinanceExpenseRed
                            )
                            Text(
                                text = " • Logged Cost: ${CurrencyFormatter.formatUsd(totalCostBasis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Bar: Pull Latest Prices & Log Stock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onPullLatestPrices,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("pull_latest_prices_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pull Latest Prices", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { showLogStockDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("log_stock_bought_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Stock", maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Dividends", style = MaterialTheme.typography.labelSmall)
                                Text(CurrencyFormatter.formatUsd(totalDividends), fontWeight = FontWeight.Bold, color = FinanceIncomeGreen)
                            }
                            Column {
                                Text("Securities Tracked", style = MaterialTheme.typography.labelSmall)
                                Text("${holdings.size} Companies", fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Annual Yield Est.", style = MaterialTheme.typography.labelSmall)
                                val yield = if (totalMarketValue > 0) (totalDividends / totalMarketValue) * 100.0 else 0.0
                                Text("${String.format("%.1f", yield)}%", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Investment Targets Section
            if (investmentTargets.isNotEmpty()) {
                item {
                    Text(
                        text = "Investment Targets & Milestones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(investmentTargets) { target ->
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
                                Text(target.title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = if (target.targetType == "SHARE_COUNT") "${target.currentValue.toInt()} / ${target.targetValue.toInt()} shares"
                                    else "$${target.currentValue.toInt()} / $${target.targetValue.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val progress = if (target.targetValue > 0) (target.currentValue / target.targetValue).toFloat() else 0f
                            ProgressBarWithTarget(
                                progress = progress,
                                targetLabel = "Target Date: ${CurrencyFormatter.formatShortDate(target.targetDateMillis)}",
                                currentLabel = "${String.format("%.1f", progress * 100)}% Complete",
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Holdings Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Logged Stock Holdings (${holdings.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onPullLatestPrices() }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Refresh Prices",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            if (holdings.isEmpty()) {
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
                            Text("No Stocks Logged Yet", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap '+ Log Stock' above to record the quantity of Econet, Delta, Simbisa, or other VFEX/ZSE shares bought.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Holdings List (Only quantity tracking & matched market prices, NO buy/sell options)
            items(holdings, key = { it.id }) { holding ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("holding_card_${holding.ticker}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = holding.ticker,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = holding.exchange,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { selectedHoldingForEdit = holding },
                                        modifier = Modifier.size(24.dp).testTag("edit_card_btn_${holding.ticker}")
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Holding",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = holding.companyName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyFormatter.formatUsd(holding.currentMarketValue),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = CurrencyFormatter.formatPercent(holding.returnPercentage),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (holding.unrealizedGain >= 0) FinanceIncomeGreen else FinanceExpenseRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stock Quantities Stepper & In-place Auto-update
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { selectedHoldingForEdit = holding }
                            ) {
                                Text(
                                    text = "Quantity Logged: ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${if (holding.shares % 1.0 == 0.0) holding.shares.toInt() else String.format("%.1f", holding.shares)} shares",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Quantity",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            // Quick +/- steppers that auto-update logged stock quantity
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            val newShares = (holding.shares - 50.0).coerceAtLeast(0.0)
                                            onUpdateHolding(holding.copy(shares = newShares))
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "-50",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            val newShares = holding.shares + 50.0
                                            onUpdateHolding(holding.copy(shares = newShares))
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "+50",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clickable {
                                            val newShares = holding.shares + 250.0
                                            onUpdateHolding(holding.copy(shares = newShares))
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            "+250",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Price matching & Card action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Avg Buy Cost: $${String.format("%.3f", holding.avgBuyPrice)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { selectedHoldingForPriceUpdate = holding }
                                ) {
                                    Text(
                                        text = "Latest Price: $${String.format("%.3f", holding.currentPrice)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Pull Latest Price",
                                        modifier = Modifier.size(13.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { selectedHoldingForPriceUpdate = holding },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Pull Price", style = MaterialTheme.typography.labelSmall)
                                }

                                OutlinedButton(
                                    onClick = { selectedHoldingForEdit = holding },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("edit_holding_btn_${holding.ticker}")
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Edit Log", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Log Stock Purchase Dialog
    if (showLogStockDialog) {
        LogStockBoughtDialog(
            onDismiss = { showLogStockDialog = false },
            onConfirm = { ticker, company, exchange, shares, price, notes ->
                onLogStockBought(ticker, company, exchange, shares, price, notes)
                showLogStockDialog = false
            }
        )
    }

    // Price Update & Pulling Dialog
    if (selectedHoldingForPriceUpdate != null) {
        UpdatePriceDialog(
            holding = selectedHoldingForPriceUpdate!!,
            onDismiss = { selectedHoldingForPriceUpdate = null },
            onUpdate = { newPrice ->
                onUpdatePrice(selectedHoldingForPriceUpdate!!.ticker, newPrice)
                selectedHoldingForPriceUpdate = null
            }
        )
    }

    // Comprehensive Stock Card & Quantity Edit Dialog
    if (selectedHoldingForEdit != null) {
        EditStockCardDialog(
            holding = selectedHoldingForEdit!!,
            onDismiss = { selectedHoldingForEdit = null },
            onSave = { updatedHolding ->
                onUpdateHolding(updatedHolding)
                selectedHoldingForEdit = null
            },
            onDelete = { holdingId ->
                onDeleteHolding?.invoke(holdingId)
                selectedHoldingForEdit = null
            }
        )
    }
}
