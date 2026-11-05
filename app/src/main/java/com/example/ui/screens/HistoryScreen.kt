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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleTransaction
import com.example.data.model.SavingsRecord
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Penjualan & Setoran, 1: Mutasi Tabungan
    val transactions by viewModel.transactions.collectAsState()
    val savingsRecords by viewModel.savingsRecords.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterPendingDepositOnly by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactions, searchQuery, filterPendingDepositOnly) {
        transactions.filter { tx ->
            val matchesQuery = searchQuery.isBlank() ||
                    tx.productName.contains(searchQuery, ignoreCase = true) ||
                    tx.userName.contains(searchQuery, ignoreCase = true)
            val matchesPending = !filterPendingDepositOnly || !tx.isDepositSettled
            matchesQuery && matchesPending
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Catatan Riwayat Finansial",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Log transparan penjualan, status setoran kas, dan mutasi tabungan",
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
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Penjualan & Setoran", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mutasi Tabungan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // TAB PENJUALAN & SETORAN
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari produk atau penjualan...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Hapus", tint = TextSecondary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !filterPendingDepositOnly,
                        onClick = { filterPendingDepositOnly = false },
                        label = { Text("Semua (${transactions.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = filterPendingDepositOnly,
                        onClick = { filterPendingDepositOnly = true },
                        label = {
                            val count = transactions.count { !it.isDepositSettled }
                            Text("Belum Disetor ($count)", fontSize = 11.sp, color = if (count > 0) DebtRed else TextPrimary)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada riwayat transaksi yang cocok.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions) { tx ->
                        DetailedTransactionCard(
                            tx = tx,
                            onSettle = { viewModel.settleDeposit(tx.id) }
                        )
                    }
                }
            }
        } else {
            // TAB MUTASI TABUNGAN
            if (savingsRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Savings, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum Ada Mutasi Mandiri", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Gunakan tombol '+ Nabung' atau 'Tarik' di Dashboard untuk mutasi mandiri.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savingsRecords) { record ->
                        SavingsRecordCard(record = record)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedTransactionCard(
    tx: SaleTransaction,
    onSettle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (tx.isDepositSettled) BorderSubtle else DebtRed.copy(alpha = 0.4f),
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tx.productName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CobaltContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tx.brandOrOrigin.ifBlank { "Pabrik Utama" },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CobaltPrimary
                            )
                        }
                    }
                    Text(
                        text = "${tx.quantity} unit x ${formatRupiah(tx.sellPricePerUnit)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (tx.note.isNotBlank()) {
                        Text(
                            text = "\"${tx.note}\"",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
                Text(
                    text = formatRupiah(tx.totalRevenue),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = ProfitGreen
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Setoran Kas Pabrik", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = formatRupiah(tx.totalMandatoryDeposit),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CobaltPrimary
                    )
                }
                Column {
                    Text("Pundi Tabungan", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = if (tx.isSavingsIncluded) "+ ${formatRupiah(tx.totalSavings)}" else "Rp 0",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.isSavingsIncluded) Color(0xFF0284C7) else TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Laba Sales", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = formatRupiah(tx.netMarginSales),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Status Bar & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (tx.isDepositSettled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ProfitGreenLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("SETORAN LUNAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DebtRedLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("BELUM DISETOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DebtRed)
                    }

                    Button(
                        onClick = onSettle,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Setor Sekarang", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SavingsRecordCard(record: SavingsRecord) {
    val isTopUp = record.type == "SETOR_MANDIRI"

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
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isTopUp) Color(0xFFE0F2FE) else Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTopUp) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                        contentDescription = null,
                        tint = if (isTopUp) Color(0xFF0284C7) else Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isTopUp) "Nabung Mandiri (Top Up)" else "Penarikan Tabungan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = record.note.ifBlank { if (isTopUp) "Setor tabungan langsung" else "Ambil tabungan" },
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "${if (isTopUp) "+" else "-"} ${formatRupiah(record.nominal)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (isTopUp) Color(0xFF0284C7) else Color(0xFFD97706)
            )
        }
    }
}
