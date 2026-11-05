package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.model.DebtRecord
import com.example.data.model.TransferRecord
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun OperationsScreen(
    viewModel: MainViewModel,
    onOpenTransferDialog: () -> Unit,
    onOpenDebtDialog: () -> Unit,
    onOpenDebtPaymentDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Transfers, 1: Debts
    val transfers by viewModel.transfers.collectAsState()
    val debts by viewModel.debts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        // Top Header
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Operasional Tim",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Mutasi transfer barang dan pelacakan hutang setoran",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardSurface,
                contentColor = CobaltPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Transfer Barang", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hutang & Pinjaman", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // TRANSFERS TAB
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Button(
                        onClick = onOpenTransferDialog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim Transfer ke Sales Lain", fontWeight = FontWeight.Bold)
                    }
                }

                if (transfers.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Belum Ada Riwayat Transfer",
                            desc = "Kirim stok produk ke rekan sesama sales jika stok berlebih."
                        )
                    }
                } else {
                    items(transfers) { transfer ->
                        TransferItemCard(transfer = transfer)
                    }
                }
            }
        } else {
            // DEBTS & LOANS TAB
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val pendingDebtsCount = debts.count { !it.isSettled }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenDebtDialog,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DebtRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Catat Hutang", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (pendingDebtsCount > 0) {
                            FilledTonalButton(
                                onClick = onOpenDebtPaymentDialog,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = ProfitGreenLight),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Bayar ($pendingDebtsCount)", fontWeight = FontWeight.Bold, color = ProfitGreen, fontSize = 12.sp)
                            }
                        }
                    }
                }

                if (debts.isEmpty()) {
                    item {
                        EmptyStateCard(
                            title = "Tidak Ada Hutang / Tunggakan",
                            desc = "Semua setoran kas dan pinjaman barang tercatat bersih dan lunas."
                        )
                    }
                } else {
                    items(debts) { debt ->
                        DebtItemCard(
                            debt = debt,
                            onSettle = { viewModel.settleDebt(debt.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransferItemCard(transfer: TransferRecord) {
    Card(
        modifier = Modifier
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CobaltContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = CobaltPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transfer.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ke: ${transfer.toUserName}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (transfer.note.isNotBlank()) {
                        Text(
                            text = "\"${transfer.note}\"",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
            Text(
                text = "${transfer.quantity} Unit",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = CobaltPrimary
            )
        }
    }
}

@Composable
fun DebtItemCard(
    debt: DebtRecord,
    onSettle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (debt.isSettled) BorderSubtle else DebtRed.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(if (debt.isSettled) ProfitGreenLight else DebtRedLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (debt.isSettled) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (debt.isSettled) ProfitGreen else DebtRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (debt.type == "SETORAN_TERTUNDA") "Tunggakan Setoran Kas" else "Pinjam Barang Fisik",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        if (debt.productName.isNotBlank() && debt.quantity > 0) {
                            Text(
                                text = "${debt.productName} (${debt.quantity} unit)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (debt.isSettled) ProfitGreenLight else DebtRedLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (debt.isSettled) "LUNAS" else "BELUM LUNAS",
                        color = if (debt.isSettled) ProfitGreen else DebtRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Nominal", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = formatRupiah(debt.totalNominal),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (debt.isSettled) TextPrimary else DebtRed
                    )
                }

                if (!debt.isSettled) {
                    FilledTonalButton(
                        onClick = onSettle,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = ProfitGreenLight),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lunasi Setoran", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(title: String, desc: String) {
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
                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Text(text = desc, fontSize = 12.sp, color = TextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}
