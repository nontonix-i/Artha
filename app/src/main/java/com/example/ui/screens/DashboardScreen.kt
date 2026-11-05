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
import androidx.compose.ui.platform.testTag
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
    onOpenDepositPaymentDialog: () -> Unit,
    onOpenSavingsDepositDialog: () -> Unit,
    onOpenSavingsWithdrawDialog: () -> Unit,
    onOpenDebtPaymentDialog: () -> Unit,
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
        // --- 1. HERO CASH FLOW & NET PROFIT CARD ---
        item {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CobaltContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = CobaltPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Arus Kas Masuk (Omset)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        StatusBadge(
                            text = "Real-Time",
                            isPositive = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = formatRupiah(metrics.totalRevenue),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Laba Bersih Sales", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = formatRupiah(metrics.totalNetProfit),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ProfitGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Terjual", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${transactions.sumOf { it.quantity }} Unit",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Setoran Sudah Lunas", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = formatRupiah(metrics.settledMandatoryDeposit),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CobaltPrimary
                            )
                        }
                    }
                }
            }
        }

        // --- 2. UNIFORM FINANCIAL METRIC CARDS (2x2 GRID) ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Baris 1: Pundi Tabungan & Setoran Wajib
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card Pundi Tabungan
                    UnifiedFinancialCard(
                        title = "Pundi Tabungan",
                        amount = metrics.totalSavings,
                        subtitle = "Dari penjualan & setor mandiri",
                        icon = Icons.Default.Savings,
                        iconTint = Color(0xFF0284C7),
                        iconBg = Color(0xFFE0F2FE),
                        badgeText = "Saldo Aktif",
                        badgeTextColor = Color(0xFF0284C7),
                        badgeBgColor = Color(0xFFE0F2FE),
                        modifier = Modifier.weight(1f),
                        primaryActionLabel = "+ Nabung",
                        primaryActionColor = Color(0xFF0284C7),
                        onPrimaryAction = onOpenSavingsDepositDialog,
                        secondaryActionLabel = "Tarik",
                        secondaryActionColor = Color(0xFFD97706),
                        onSecondaryAction = onOpenSavingsWithdrawDialog
                    )

                    // Card Setoran Wajib Pabrik
                    val hasPendingDeposit = metrics.pendingMandatoryDeposit > 0
                    UnifiedFinancialCard(
                        title = "Setoran Wajib",
                        amount = metrics.pendingMandatoryDeposit,
                        subtitle = if (hasPendingDeposit) "${metrics.pendingDepositCount} Transaksi belum disetor" else "Semua setoran lunas",
                        icon = Icons.Default.AccountBalance,
                        iconTint = if (hasPendingDeposit) CobaltPrimary else ProfitGreen,
                        iconBg = if (hasPendingDeposit) CobaltContainer else ProfitGreenLight,
                        badgeText = if (hasPendingDeposit) "${metrics.pendingDepositCount} Tertunda" else "Lunas",
                        badgeTextColor = if (hasPendingDeposit) DebtRed else ProfitGreen,
                        badgeBgColor = if (hasPendingDeposit) DebtRedLight else ProfitGreenLight,
                        modifier = Modifier.weight(1f),
                        primaryActionLabel = "Bayar Setor",
                        primaryActionColor = CobaltPrimary,
                        onPrimaryAction = onOpenDepositPaymentDialog
                    )
                }

                // Baris 2: Hutang / Tunggakan & Jatah Stok Aktif (Sama persis tata letak & proporsi!)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card Hutang / Tunggakan
                    val hasPendingDebt = metrics.pendingDebtNominal > 0
                    UnifiedFinancialCard(
                        title = "Hutang / Tunggakan",
                        amount = metrics.pendingDebtNominal,
                        subtitle = if (hasPendingDebt) "${metrics.pendingDebtCount} Catatan belum lunas" else "Tidak ada hutang aktif",
                        icon = Icons.Default.Warning,
                        iconTint = if (hasPendingDebt) DebtRed else ProfitGreen,
                        iconBg = if (hasPendingDebt) DebtRedLight else ProfitGreenLight,
                        badgeText = if (hasPendingDebt) "${metrics.pendingDebtCount} Aktif" else "Lunas",
                        badgeTextColor = if (hasPendingDebt) DebtRed else ProfitGreen,
                        badgeBgColor = if (hasPendingDebt) DebtRedLight else ProfitGreenLight,
                        modifier = Modifier.weight(1f),
                        primaryActionLabel = "Bayar Hutang",
                        primaryActionColor = if (hasPendingDebt) DebtRed else ProfitGreen,
                        onPrimaryAction = onOpenDebtPaymentDialog
                    )

                    // Card Stok & Jatah Siap Jual
                    UnifiedFinancialCard(
                        title = "Stok Siap Jual",
                        amount = metrics.totalAvailableStock.toDouble(),
                        subtitle = "Total jatah mingguan & transfer",
                        icon = Icons.Default.Inventory2,
                        iconTint = Color(0xFF475569),
                        iconBg = Color(0xFFF1F5F9),
                        badgeText = "${metrics.totalAvailableStock} Unit",
                        badgeTextColor = Color(0xFF334155),
                        badgeBgColor = Color(0xFFE2E8F0),
                        modifier = Modifier.weight(1f),
                        primaryActionLabel = "Jatah Pool",
                        primaryActionColor = Color(0xFF334155),
                        onPrimaryAction = onOpenPoolDialog
                    )
                }
            }
        }

        // --- 3. AKSI CEPAT ROW ---
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
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_sell"),
                        onClick = onOpenSellDialog
                    )
                    QuickActionButton(
                        icon = Icons.Default.SwapHoriz,
                        label = "Transfer",
                        containerColor = CardSurface,
                        contentColor = CobaltPrimary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_transfer"),
                        onClick = onOpenTransferDialog
                    )
                    QuickActionButton(
                        icon = Icons.Default.Savings,
                        label = "Nabung",
                        containerColor = CardSurface,
                        contentColor = Color(0xFF0284C7),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_savings"),
                        onClick = onOpenSavingsDepositDialog
                    )
                    QuickActionButton(
                        icon = Icons.Default.AttachMoney,
                        label = "Hutang",
                        containerColor = CardSurface,
                        contentColor = DebtRed,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_action_debt"),
                        onClick = onOpenDebtDialog
                    )
                }
            }
        }

        // --- 4. GRAFIK ARUS KAS REAL-TIME ---
        item {
            CashFlowBarChart(transactions = transactions)
        }

        // --- 5. DONUT BREAKDOWN BIAYA & LABA ---
        item {
            CashBreakdownDonutChart(
                totalHpp = transactions.sumOf { it.totalHpp },
                totalSavings = metrics.totalSavings,
                totalProfit = metrics.totalNetProfit
            )
        }

        // --- 6. TRANSAKSI TERAKHIR DENGAN STATUS SETORAN ---
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
                    Text("Lihat Semua", color = CobaltPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                DashboardTransactionItemCard(
                    tx = tx,
                    onSettleDeposit = { viewModel.settleDeposit(tx.id) }
                )
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
fun DashboardTransactionItemCard(
    tx: com.example.data.model.SaleTransaction,
    onSettleDeposit: () -> Unit,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.productName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CobaltContainer)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = tx.brandOrOrigin.ifBlank { "Pabrik Utama" },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CobaltPrimary
                            )
                        }
                    }
                    Text(
                        text = "${tx.quantity} unit | Setor: ${formatRupiah(tx.totalMandatoryDeposit)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (tx.isSavingsIncluded) {
                        Text(
                            text = "+ Tabungan ${formatRupiah(tx.totalSavings)}",
                            fontSize = 11.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (tx.note.isNotBlank()) {
                        Text(
                            text = "\"${tx.note}\"",
                            fontSize = 10.sp,
                            color = TextMuted
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

                Spacer(modifier = Modifier.height(4.dp))

                if (tx.isDepositSettled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ProfitGreenLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("SETOR LUNAS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                    }
                } else {
                    FilledTonalButton(
                        onClick = onSettleDeposit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DebtRedLight),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Setor", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DebtRed)
                    }
                }
            }
        }
    }
}
