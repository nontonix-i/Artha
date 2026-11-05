package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterSavingsOnly by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactions, searchQuery, filterSavingsOnly) {
        transactions.filter { tx ->
            val matchesQuery = searchQuery.isBlank() ||
                    tx.productName.contains(searchQuery, ignoreCase = true) ||
                    tx.userName.contains(searchQuery, ignoreCase = true)
            val matchesSavings = !filterSavingsOnly || tx.isSavingsIncluded
            matchesQuery && matchesSavings
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
                text = "Log lengkap penjualan, setoran pabrik, dan tabungan",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari produk atau transaksi...", fontSize = 13.sp) },
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
                    selected = !filterSavingsOnly,
                    onClick = { filterSavingsOnly = false },
                    label = { Text("Semua Transaksi (${transactions.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = filterSavingsOnly,
                    onClick = { filterSavingsOnly = true },
                    label = { Text("Dgn Tabungan Saja", fontSize = 11.sp) }
                )
            }
        }

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada transaksi yang cocok.",
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
                    DetailedTransactionCard(tx = tx)
                }
            }
        }
    }
}

@Composable
fun DetailedTransactionCard(tx: com.example.data.model.SaleTransaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tx.productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Text(
                    text = formatRupiah(tx.totalRevenue),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = ProfitGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = BorderSubtle, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Volume Terjual", fontSize = 11.sp, color = TextSecondary)
                    Text("${tx.quantity} Unit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Column {
                    Text("Setoran Wajib", fontSize = 11.sp, color = TextSecondary)
                    Text(formatRupiah(tx.totalMandatoryDeposit), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CobaltPrimary)
                }
                Column {
                    Text("Pundi Tabungan", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = if (tx.isSavingsIncluded) formatRupiah(tx.totalSavings) else "Rp 0 (Nonaktif)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (tx.isSavingsIncluded) Color(0xFF06B6D4) else TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Laba Sales", fontSize = 11.sp, color = TextSecondary)
                    Text(formatRupiah(tx.netMarginSales), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                }
            }
        }
    }
}
