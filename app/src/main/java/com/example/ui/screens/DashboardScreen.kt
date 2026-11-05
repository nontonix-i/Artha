package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onOpenSellDialog: () -> Unit,
    onOpenTransferDialog: () -> Unit,
    onOpenPoolDialog: () -> Unit,
    onOpenDebtDialog: () -> Unit,
    onViewAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.financialMetrics.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Financial Overview Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Primary Net Profit & Cash Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Arus Kas Masuk (Omset)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            StatusBadge(
                                text = "Real-Time",
                                isPositive = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = formatRupiah(metrics.totalRevenue),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = BorderSubtle, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Laba Bersih Sales", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    text = formatRupiah(metrics.totalNetProfit),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Pundi Tabungan", fontSize = 12.sp, color = TextSecondary)
                                Text(
                                    text = formatRupiah(metrics.totalSavings),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF06B6D4)
                                )
                            }
                        }
                    }
                }

                // Sub-cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Setoran Wajib Card
                    MetricCard(
                        title = "Setoran Wajib",
                        amount = metrics.totalMandatoryDeposit,
                        icon = Icons.Default.AccountBalance,
                        iconTint = CobaltPrimary,
                        iconBg = CobaltContainer,
                        modifier = Modifier.weight(1f)
                    )

                    // Tunggakan / Hutang Card (Crucial financial highlight in sharp Red)
                    MetricCard(
                        title = "Hutang / Tunggakan",
                        amount = metrics.pendingDebtNominal,
                        icon = Icons.Default.Warning,
                        iconTint = if (metrics.pendingDebtNominal > 0) DebtRed else ProfitGreen,
                        iconBg = if (metrics.pendingDebtNominal > 0) DebtRedLight else ProfitGreenLight,
                        badgeText = if (metrics.pendingDebtNominal > 0) "${metrics.pendingDebtCount} Tertunda" else "Lunas",
                        badgeColor = if (metrics.pendingDebtNominal > 0) DebtRed else ProfitGreen,
                        badgeBg = if (metrics.pendingDebtNominal > 0) DebtRedLight else ProfitGreenLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Aksi Cepat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        icon = Icons.Default.AddShoppingCart,
                        label = "Catat Jual",
                        containerColor = CobaltPrimary,
                        contentColor = PureWhite,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenSellDialog
                    )
                    QuickActionButton(
                        icon = Icons.Default.SwapHoriz,
                        label = "Transfer",
                        containerColor = CardSurface,
                        contentColor = CobaltPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenTransferDialog
                    )
                    QuickActionButton(
                        icon = Icons.Default.LocalShipping,
                        label = "Jatah Pool",
                        containerColor = CardSurface,
                        contentColor = CobaltPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenPoolDialog
                    )
                    QuickActionButton(
                        icon = Icons.Default.AttachMoney,
                        label = "Hutang",
                        containerColor = CardSurface,
                        contentColor = DebtRed,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenDebtDialog
                    )
                }
            }
        }

        // Visual Cash Flow Bar Chart
        item {
            CashFlowBarChart(transactions = transactions)
        }

        // Donut Breakdown Chart
        item {
            CashBreakdownDonutChart(
                totalHpp = transactions.sumOf { it.totalHpp },
                totalSavings = metrics.totalSavings,
                totalProfit = metrics.totalNetProfit
            )
        }

        // Recent Sales Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaksi Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = onViewAllHistory) {
                    Text("Lihat Semua", color = CobaltPrimary, fontSize = 13.sp)
                }
            }
        }

        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum Ada Transaksi",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Text(
                                text = "Klik 'Catat Jual' untuk memulai transaksi pertama",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        } else {
            items(transactions.take(5)) { tx ->
                TransactionRowCard(tx = tx)
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TransactionRowCard(
    tx: com.example.data.model.SaleTransaction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CobaltContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = CobaltPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tx.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${tx.quantity} unit | Setor: ${formatRupiah(tx.totalMandatoryDeposit)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (tx.isSavingsIncluded) {
                        Text(
                            text = "+ Tabungan ${formatRupiah(tx.totalSavings)}",
                            fontSize = 11.sp,
                            color = Color(0xFF06B6D4),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+ ${formatRupiah(tx.totalRevenue)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ProfitGreen
                )
                Text(
                    text = "Laba: ${formatRupiah(tx.netMarginSales)}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
