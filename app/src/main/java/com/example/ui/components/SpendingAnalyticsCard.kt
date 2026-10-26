package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TransactionEntity
import com.example.ui.theme.FinanceExpenseRed
import com.example.ui.theme.FinanceIncomeGreen
import com.example.ui.theme.ZiGAmber
import com.example.util.CurrencyFormatter
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

data class MonthSpendPoint(
    val monthIndex: Int,
    val year: Int,
    val label: String,
    val fullLabel: String,
    val amount: Double
)

enum class AnalyticsChartTab {
    MONTHLY_TREND,
    CATEGORY_PIE
}

@Composable
fun SpendingAnalyticsCard(
    transactions: List<TransactionEntity>,
    onNavigateToDetails: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(AnalyticsChartTab.MONTHLY_TREND) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_spending_analytics_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Tab Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Spending Visualizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedTab == AnalyticsChartTab.MONTHLY_TREND) "6-Month Trendline" else "Category Distribution",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Tab Pills
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        ChartTabButton(
                            text = "Trends",
                            icon = Icons.Default.ShowChart,
                            isSelected = selectedTab == AnalyticsChartTab.MONTHLY_TREND,
                            onClick = { selectedTab = AnalyticsChartTab.MONTHLY_TREND }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        ChartTabButton(
                            text = "Pie Chart",
                            icon = Icons.Default.PieChart,
                            isSelected = selectedTab == AnalyticsChartTab.CATEGORY_PIE,
                            onClick = { selectedTab = AnalyticsChartTab.CATEGORY_PIE }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Animated Tab Content
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(200)) },
                label = "chart_tab_content"
            ) { tab ->
                when (tab) {
                    AnalyticsChartTab.MONTHLY_TREND -> {
                        MonthlySpendingLineChart(transactions = transactions)
                    }
                    AnalyticsChartTab.CATEGORY_PIE -> {
                        CategoryDistributionPieChart(transactions = transactions)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Card Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onNavigateToDetails) {
                    Text("Detailed Monthly Summary", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ChartTabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(9.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .height(34.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
        }
    }
}

// -------------------------------------------------------------
// 1. MONTHLY SPENDING TRENDS LINE CHART
// -------------------------------------------------------------

@Composable
fun MonthlySpendingLineChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val monthShortNames = remember { DateFormatSymbols().shortMonths }
    val monthFullNames = remember { DateFormatSymbols().months }

    // Aggregate expenses for the last 6 months
    val monthlyData = remember(transactions) {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val points = mutableListOf<MonthSpendPoint>()

        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth - i)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val startMillis = cal.timeInMillis
            val mIdx = cal.get(Calendar.MONTH)
            val yr = cal.get(Calendar.YEAR)

            cal.add(Calendar.MONTH, 1)
            val endMillis = cal.timeInMillis

            val monthExpenses = transactions
                .filter { it.type.equals("EXPENSE", ignoreCase = true) && it.dateMillis in startMillis until endMillis }
                .sumOf { it.amount + it.fee }

            points.add(
                MonthSpendPoint(
                    monthIndex = mIdx,
                    year = yr,
                    label = monthShortNames[mIdx],
                    fullLabel = "${monthFullNames[mIdx]} $yr",
                    amount = monthExpenses
                )
            )
        }
        points
    }

    var selectedPointIndex by remember { mutableIntStateOf(monthlyData.lastIndex) }
    val selectedPoint = monthlyData.getOrNull(selectedPointIndex)

    val maxAmount = remember(monthlyData) {
        val maxVal = monthlyData.maxOfOrNull { it.amount } ?: 0.0
        if (maxVal > 0.0) maxVal * 1.25 else 500.0
    }

    val avgSpend = remember(monthlyData) {
        val nonZero = monthlyData.filter { it.amount > 0 }
        if (nonZero.isNotEmpty()) nonZero.map { it.amount }.average() else 0.0
    }

    val latestSpend = monthlyData.lastOrNull()?.amount ?: 0.0
    val prevSpend = if (monthlyData.size >= 2) monthlyData[monthlyData.size - 2].amount else 0.0
    val monthOverMonthDiff = latestSpend - prevSpend
    val monthOverMonthPct = if (prevSpend > 0) (monthOverMonthDiff / prevSpend * 100.0) else 0.0

    // Animation progress
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(monthlyData) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // KPI Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (selectedPoint != null) selectedPoint.fullLabel else "Active Month",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatUsd(selectedPoint?.amount ?: latestSpend),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (monthOverMonthDiff <= 0) FinanceIncomeGreen.copy(alpha = 0.15f)
                else FinanceExpenseRed.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (monthOverMonthDiff <= 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (monthOverMonthDiff <= 0) FinanceIncomeGreen else FinanceExpenseRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (monthOverMonthDiff == 0.0) "Stable"
                        else "${if (monthOverMonthDiff > 0) "+" else ""}${String.format("%.1f", monthOverMonthPct)}% MoM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (monthOverMonthDiff <= 0) FinanceIncomeGreen else FinanceExpenseRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Line Chart Canvas
        val lineColor = MaterialTheme.colorScheme.primary
        val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        val textPaintColor = MaterialTheme.colorScheme.onSurfaceVariant

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("monthly_spending_line_chart_canvas")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(monthlyData) {
                        detectTapGestures { offset ->
                            val widthPerPoint = size.width / (monthlyData.size.coerceAtLeast(1))
                            val tappedIndex = (offset.x / widthPerPoint).toInt().coerceIn(0, monthlyData.lastIndex)
                            selectedPointIndex = tappedIndex
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val bottomPadding = 28.dp.toPx()
                val topPadding = 16.dp.toPx()
                val graphHeight = canvasHeight - bottomPadding - topPadding

                val numPoints = monthlyData.size
                if (numPoints < 2) return@Canvas

                val stepX = canvasWidth / (numPoints - 1)

                // 1. Draw horizontal gridlines (3 lines)
                val gridLines = 3
                for (g in 0..gridLines) {
                    val y = topPadding + (graphHeight / gridLines) * g
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                }

                // 2. Compute point coordinates
                val points = monthlyData.mapIndexed { index, point ->
                    val x = index * stepX
                    val normalizedAmount = (point.amount / maxAmount).coerceIn(0.0, 1.0).toFloat()
                    val animatedY = topPadding + graphHeight * (1f - (normalizedAmount * animationProgress.value))
                    Offset(x, animatedY)
                }

                // 3. Create smooth path & area gradient
                val linePath = Path()
                val areaPath = Path()

                points.forEachIndexed { i, pt ->
                    if (i == 0) {
                        linePath.moveTo(pt.x, pt.y)
                        areaPath.moveTo(pt.x, canvasHeight - bottomPadding)
                        areaPath.lineTo(pt.x, pt.y)
                    } else {
                        val prev = points[i - 1]
                        val control1 = Offset((prev.x + pt.x) / 2f, prev.y)
                        val control2 = Offset((prev.x + pt.x) / 2f, pt.y)
                        linePath.cubicTo(control1.x, control1.y, control2.x, control2.y, pt.x, pt.y)
                        areaPath.cubicTo(control1.x, control1.y, control2.x, control2.y, pt.x, pt.y)
                    }
                }

                areaPath.lineTo(points.last().x, canvasHeight - bottomPadding)
                areaPath.close()

                // Draw gradient area fill (Recharts / D3 inspired area fill)
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f),
                            lineColor.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        startY = topPadding,
                        endY = canvasHeight - bottomPadding
                    )
                )

                // Draw main curve stroke
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // 4. Draw data points and selection indicator
                points.forEachIndexed { i, pt ->
                    val isSelected = i == selectedPointIndex

                    if (isSelected) {
                        // Vertical guideline
                        drawLine(
                            color = lineColor.copy(alpha = 0.4f),
                            start = Offset(pt.x, topPadding),
                            end = Offset(pt.x, canvasHeight - bottomPadding),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Outer glowing circle
                        drawCircle(
                            color = lineColor.copy(alpha = 0.25f),
                            radius = 12.dp.toPx(),
                            center = pt
                        )
                    }

                    // Outer white ring
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
                        center = pt
                    )

                    // Inner primary dot
                    drawCircle(
                        color = if (isSelected) ZiGAmber else lineColor,
                        radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                        center = pt
                    )

                    // Draw X-axis label
                    val label = monthlyData[i].label
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            if (isSelected) {
                                isFakeBoldText = true
                            }
                        }
                        drawText(
                            label,
                            pt.x,
                            canvasHeight - 6.dp.toPx(),
                            paint
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Average helper text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "6-Mo Average: ${CurrencyFormatter.formatUsd(avgSpend)}/mo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap points to inspect months",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// -------------------------------------------------------------
// 2. CATEGORY DISTRIBUTION PIE / DONUT CHART
// -------------------------------------------------------------

@Composable
fun CategoryDistributionPieChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val expenseList = remember(transactions) {
        transactions.filter { it.type.equals("EXPENSE", ignoreCase = true) }
    }
    val totalExpense = remember(expenseList) {
        expenseList.sumOf { it.amount + it.fee }
    }

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

    LaunchedEffect(transactions) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (categoryData.isEmpty() || totalExpense <= 0.0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No category expense records to chart yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Donut Chart with center label
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .testTag("dashboard_donut_chart_canvas"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 30.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )
                    val arcSize = Size(diameter, diameter)

                    // Background ring track
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

                // Center callout info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
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
                            text = String.format(Locale.US, "%.1f%% of total", selectedCategory!!.percentage),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Total Expenses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = CurrencyFormatter.formatUsd(totalExpense),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = FinanceExpenseRed
                        )
                        Text(
                            text = "${categoryData.size} categories",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Category List / Legend (Top 4 displayed on dashboard)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categoryData.take(4).forEach { item ->
                    val isSelected = selectedCategory?.category == item.category

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) item.color.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategory = if (isSelected) null else item
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(item.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CurrencyFormatter.formatUsd(item.amount),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = item.color.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f%%", item.percentage),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = item.color,
                                            fontWeight = FontWeight.Bold
                                        )
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
