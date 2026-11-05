package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleTransaction
import com.example.ui.theme.*
import java.util.Calendar

data class DailyCashFlow(
    val dayLabel: String,
    val revenue: Double,
    val deposit: Double
)

@Composable
fun CashFlowBarChart(
    transactions: List<SaleTransaction>,
    modifier: Modifier = Modifier
) {
    // Aggregate by last 7 days
    val dailyData = remember(transactions) {
        val calendar = Calendar.getInstance()
        val days = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed

        (6 downTo 0).map { dayOffset ->
            val targetDayIndex = (currentDayOfWeek - dayOffset + 7) % 7
            val label = days[targetDayIndex]

            // Calculate aggregate for transactions on this relative day
            val dayTxs = transactions.filter { tx ->
                val txCal = Calendar.getInstance().apply {
                    timeInMillis = (tx.timestamp?.seconds ?: 0L) * 1000L
                }
                txCal.get(Calendar.DAY_OF_WEEK) - 1 == targetDayIndex
            }
            DailyCashFlow(
                dayLabel = label,
                revenue = dayTxs.sumOf { it.totalRevenue },
                deposit = dayTxs.sumOf { it.totalMandatoryDeposit }
            )
        }
    }

    val maxVal = remember(dailyData) {
        dailyData.maxOfOrNull { it.revenue }?.coerceAtLeast(50000.0) ?: 50000.0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Arus Kas 7 Hari Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CobaltPrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Omset", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ProfitGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Setoran", fontSize = 11.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Bar Chart
            var animatedProgress by remember { mutableStateOf(0f) }
            LaunchedEffect(dailyData) {
                animatedProgress = 1f
            }
            val animVal by animateFloatAsState(
                targetValue = animatedProgress,
                animationSpec = tween(durationMillis = 600),
                label = "barAnimation"
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = 12.dp.toPx()
                val groupSpacing = canvasWidth / dailyData.size

                dailyData.forEachIndexed { index, dayData ->
                    val groupCenterX = (index * groupSpacing) + (groupSpacing / 2)

                    val revHeight = ((dayData.revenue / maxVal) * (canvasHeight - 30f)).toFloat() * animVal
                    val depHeight = ((dayData.deposit / maxVal) * (canvasHeight - 30f)).toFloat() * animVal

                    // Draw baseline guideline
                    drawLine(
                        color = Color(0xFFF1F5F9),
                        start = Offset(0f, canvasHeight - 20f),
                        end = Offset(canvasWidth, canvasHeight - 20f),
                        strokeWidth = 1f
                    )

                    // Draw Revenue Bar
                    if (revHeight > 0) {
                        drawRoundRect(
                            color = CobaltPrimary,
                            topLeft = Offset(groupCenterX - barWidth - 2f, (canvasHeight - 20f) - revHeight),
                            size = Size(barWidth, revHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    // Draw Mandatory Deposit Bar
                    if (depHeight > 0) {
                        drawRoundRect(
                            color = ProfitGreen,
                            topLeft = Offset(groupCenterX + 2f, (canvasHeight - 20f) - depHeight),
                            size = Size(barWidth, depHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            // Labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dailyData.forEach { dayData ->
                    Text(
                        text = dayData.dayLabel,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CashBreakdownDonutChart(
    totalHpp: Double,
    totalSavings: Double,
    totalProfit: Double,
    modifier: Modifier = Modifier
) {
    val total = (totalHpp + totalSavings + totalProfit).coerceAtLeast(1.0)
    val hppAngle = ((totalHpp / total) * 360f).toFloat()
    val savingsAngle = ((totalSavings / total) * 360f).toFloat()
    val profitAngle = ((totalProfit / total) * 360f).toFloat()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Struktur Aliran Dana",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Donut Chart Canvas
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(100.dp)) {
                        val strokeWidth = 16.dp.toPx()
                        val arcSize = size.width - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                        if (totalHpp == 0.0 && totalSavings == 0.0 && totalProfit == 0.0) {
                            drawArc(
                                color = Color(0xFFE2E8F0),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = Size(arcSize, arcSize),
                                style = Stroke(width = strokeWidth)
                            )
                        } else {
                            var currentAngle = -90f
                            // HPP Modal Arc
                            if (hppAngle > 0f) {
                                drawArc(
                                    color = CobaltPrimary,
                                    startAngle = currentAngle,
                                    sweepAngle = hppAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth)
                                )
                                currentAngle += hppAngle
                            }
                            // Tabungan Arc
                            if (savingsAngle > 0f) {
                                drawArc(
                                    color = Color(0xFF06B6D4), // Cyan
                                    startAngle = currentAngle,
                                    sweepAngle = savingsAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth)
                                )
                                currentAngle += savingsAngle
                            }
                            // Net Profit Arc
                            if (profitAngle > 0f) {
                                drawArc(
                                    color = ProfitGreen,
                                    startAngle = currentAngle,
                                    sweepAngle = profitAngle,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                        }
                    }
                    Text(
                        text = "100%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legend Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendItem(
                        color = CobaltPrimary,
                        label = "Modal HPP Pabrik",
                        amount = totalHpp
                    )
                    LegendItem(
                        color = Color(0xFF06B6D4),
                        label = "Pundi Tabungan",
                        amount = totalSavings
                    )
                    LegendItem(
                        color = ProfitGreen,
                        label = "Margin Laba Sales",
                        amount = totalProfit
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    amount: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Text(
            text = formatRupiah(amount),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
