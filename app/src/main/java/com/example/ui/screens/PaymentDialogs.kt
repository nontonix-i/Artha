package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtRecord
import com.example.data.model.SaleTransaction
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*

@Composable
fun DepositPaymentDialog(
    pendingTransactions: List<SaleTransaction>,
    onDismiss: () -> Unit,
    onSettleSingle: (txId: String) -> Unit,
    onSettleAll: (txIds: List<String>) -> Unit
) {
    // Collect all unique brands/factories from pending transactions
    val availableBrands = remember(pendingTransactions) {
        listOf("Semua Pabrik/Merk") + pendingTransactions.map { it.brandOrOrigin.ifBlank { "Pabrik Utama" } }.distinct()
    }
    var selectedBrandFilter by remember { mutableStateOf("Semua Pabrik/Merk") }

    val filteredTransactions = remember(pendingTransactions, selectedBrandFilter) {
        if (selectedBrandFilter == "Semua Pabrik/Merk") {
            pendingTransactions
        } else {
            pendingTransactions.filter { (it.brandOrOrigin.ifBlank { "Pabrik Utama" }) == selectedBrandFilter }
        }
    }

    val totalPendingAmount = filteredTransactions.sumOf { it.totalMandatoryDeposit }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CobaltContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = CobaltPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Bayar Setoran Pabrik",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pisahkan setoran kas berdasarkan asal merk/pabrik",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Brand/Factory Filter Chips
                if (availableBrands.size > 2) {
                    Text("Pilih Pabrik / Merk:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(availableBrands) { brand ->
                            FilterChip(
                                selected = selectedBrandFilter == brand,
                                onClick = { selectedBrandFilter = brand },
                                label = { Text(brand, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Header Total for currently filtered group
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Setoran $selectedBrandFilter (${filteredTransactions.size} Transaksi)",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = formatRupiah(totalPendingAmount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CobaltPrimary
                            )
                        }
                    }
                }

                if (filteredTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Setoran Sudah Lunas", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Tidak ada setoran tertunda untuk filter ini.", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTransactions) { tx ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(tx.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(CobaltContainer)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    tx.brandOrOrigin.ifBlank { "Pabrik Utama" },
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = CobaltPrimary
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${tx.quantity} unit | Setor: ${formatRupiah(tx.totalMandatoryDeposit)}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        if (tx.note.isNotBlank()) {
                                            Text(
                                                text = "\"${tx.note}\"",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                    FilledTonalButton(
                                        onClick = { onSettleSingle(tx.id) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = CobaltContainer)
                                    ) {
                                        Text("Setor Ini", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CobaltPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (filteredTransactions.isNotEmpty()) {
                Button(
                    onClick = { onSettleAll(filteredTransactions.map { it.id }) },
                    colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("settle_filtered_deposits_button")
                ) {
                    val label = if (selectedBrandFilter == "Semua Pabrik/Merk") "Setor Semua" else "Setor $selectedBrandFilter"
                    Text("$label (${formatRupiah(totalPendingAmount)})", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text(if (filteredTransactions.isEmpty()) "Tutup" else "Batal")
            }
        }
    )
}

@Composable
fun DebtPaymentDialog(
    pendingDebts: List<DebtRecord>,
    onDismiss: () -> Unit,
    onSettleDebt: (debtId: String) -> Unit
) {
    val totalDebtAmount = pendingDebts.sumOf { it.totalNominal }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DebtRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = DebtRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Bayar / Lunasi Hutang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${pendingDebts.size} Catatan tunggakan aktif",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Header Total
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DebtRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Hutang / Tunggakan Aktif", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = formatRupiah(totalDebtAmount),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DebtRed
                            )
                        }
                    }
                }

                if (pendingDebts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Nihil Tunggakan", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Semua hutang dan pinjaman telah lunas.", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(pendingDebts) { debt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (debt.type == "SETORAN_TERTUNDA") "Setoran Kas Pabrik" else "Pinjam ${debt.productName} (${debt.quantity} unit)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        if (debt.brandOrOrigin.isNotBlank()) {
                                            Text(
                                                text = "Asal: ${debt.brandOrOrigin}",
                                                fontSize = 10.sp,
                                                color = CobaltPrimary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (debt.note.isNotBlank()) {
                                            Text(
                                                text = "Catatan: \"${debt.note}\"",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        Text(
                                            text = formatRupiah(debt.totalNominal),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DebtRed
                                        )
                                    }
                                    Button(
                                        onClick = { onSettleDebt(debt.id) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Lunasi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        }
    )
}
