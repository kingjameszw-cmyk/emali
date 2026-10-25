package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TransactionEntity
import com.example.ui.theme.FinanceExpenseRed
import com.example.util.CurrencyFormatter
import java.util.Locale

// Distinct vibrant palette for financial category visualization
val CategoryColors = listOf(
    Color(0xFF10B981), // Emerald - Groceries
    Color(0xFFF59E0B), // Amber - Transport & Fuel
    Color(0xFFEAB308), // Yellow - Electricity & ZETDC
    Color(0xFFEF4444), // Red - Food & Dining
    Color(0xFF0284C7), // Blue - Airtime & Data
    Color(0xFF6366F1), // Indigo - Rent & Housing
    Color(0xFFEC4899), // Pink - Shopping
    Color(0xFF14B8A6), // Teal - Healthcare
    Color(0xFF8B5CF6), // Purple - School Fees
    Color(0xFFF97316), // Orange - Entertainment
    Color(0xFF64748B)  // Slate - Other
)

data class CategoryExpenseData(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val color: Color,
    val count: Int
)

@Composable
fun ExpenseCategoryChart(
    expenses: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val expenseList = remember(expenses) { expenses.filter { it.type == "EXPENSE" } }
    val totalExpense = remember(expenseList) { expenseList.sumOf { it.amount + it.fee } }

    val categoryData = remember(expenseList, totalExpense) {
        if (totalExpense <= 0.0) return@remember emptyList<CategoryExpenseData>()

        val grouped = expenseList.groupBy { it.category }
        grouped.entries.mapIndexed { index, entry ->
            val catTotal = entry.value.sumOf { it.amount + it.fee }
            val pct = (catTotal / totalExpense) * 100.0
            val color = CategoryColors[index % CategoryColors.size]
            CategoryExpenseData(
                category = entry.key,
                amount = catTotal,
                percentage = pct,
                color = color,
                count = entry.value.size
            )
        }.sortedByDescending { it.amount }
    }

    var selectedCategory by remember { mutableStateOf<CategoryExpenseData?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(expenses) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_category_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Visual Expense Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Distribution across categories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${categoryData.size} Categories",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (categoryData.isEmpty() || totalExpense <= 0.0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No expense data to display yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Donut Chart with center label
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .testTag("donut_chart_canvas"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(190.dp)) {
                        val strokeWidth = 32.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(
                            (size.width - diameter) / 2f,
                            (size.height - diameter) / 2f
                        )
                        val arcSize = Size(diameter, diameter)

                        // Draw background track
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )

                        var startAngle = -90f
                        categoryData.forEach { item ->
                            val sweepAngle = ((item.percentage / 100.0) * 360f * animatedProgress.value).toFloat()
                            val isSelected = selectedCategory?.category == item.category
                            val effectiveStroke = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth

                            drawArc(
                                color = item.color,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle.coerceAtLeast(1.5f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = effectiveStroke, cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Center information
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selectedCategory != null) {
                            Text(
                                text = selectedCategory!!.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = selectedCategory!!.color,
                                maxLines = 1
                            )
                            Text(
                                text = CurrencyFormatter.formatUsd(selectedCategory!!.amount),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f%%", selectedCategory!!.percentage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "Total Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.formatUsd(totalExpense),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = FinanceExpenseRed
                            )
                            Text(
                                text = "${expenseList.size} expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Category List & Interactive Legend
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categoryData.forEach { item ->
                        val isSelected = selectedCategory?.category == item.category
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) item.color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategory = if (isSelected) null else item
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
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
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(item.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.category,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                            ),
                                            maxLines = 1
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyFormatter.formatUsd(item.amount),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = String.format(Locale.US, "%.1f%% • %d item(s)", item.percentage, item.count),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Proportional progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color.LightGray.copy(alpha = 0.25f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = (item.percentage / 100.0).toFloat().coerceIn(0.02f, 1f))
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(item.color)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
