package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.data.model.Product
import com.example.data.model.UserStock
import com.example.ui.components.formatNumber
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    onOpenSellDialog: () -> Unit,
    onOpenPoolDialog: () -> Unit,
    onOpenNewProductDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val userStocks by viewModel.userStock.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Inventaris & Jatah Stok",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pelacakan jatah pool dan rincian asal barang",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onOpenNewProductDialog,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = CobaltContainer),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = CobaltPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Produk", fontSize = 12.sp, color = CobaltPrimary, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenPoolDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Jatah Pool", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (products.isEmpty()) {
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
                            .padding(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Katalog Produk Masih Kosong",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Tekan tombol '+ Produk' untuk mendaftarkan barang pertama Anda.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            items(products) { product ->
                val stock = userStocks.find { it.productId == product.id }
                ProductStockCard(
                    product = product,
                    stock = stock,
                    onSellClick = onOpenSellDialog
                )
            }
        }
    }
}

@Composable
fun ProductStockCard(
    product: Product,
    stock: UserStock?,
    onSellClick: () -> Unit
) {
    var expandedOriginDetails by remember { mutableStateOf(false) }
    val totalStock = stock?.totalAvailableStock ?: 0L

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Price row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CobaltContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = product.brandOrOrigin.ifBlank { "Pabrik Utama" },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CobaltPrimary
                            )
                        }
                    }
                    Text(
                        text = "SKU: ${product.code.ifBlank { "-" }} | HPP: ${formatRupiah(product.hppPrice)} | Tabungan: ${formatRupiah(product.savingsPerUnit)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatRupiah(product.sellPrice),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CobaltPrimary
                    )
                    Text(
                        text = "Setor: ${formatRupiah(product.calculatedStandardDeposit)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Total Available Stock Highlight Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (totalStock > 0) CobaltContainer else SlateBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TOTAL STOK TERSEDIA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CobaltDark,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${formatNumber(totalStock)} Unit",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (totalStock > 0) CobaltDark else DebtRed
                        )
                    }

                    Button(
                        onClick = onSellClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Catat Jual", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expandable Origin Ledger Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expandedOriginDetails = !expandedOriginDetails }
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        tint = CobaltPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (expandedOriginDetails) "Sembunyikan Rincian Asal Barang" else "Lihat Rincian Asal Barang (Jatah, Sisa, Transfer)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CobaltPrimary
                    )
                }
                Icon(
                    imageVector = if (expandedOriginDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CobaltPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Expandable Detailed Origin Ledger
            AnimatedVisibility(visible = expandedOriginDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateBackground)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Rincian Asal Stok (Stock Origin Breakdown):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    OriginRowItem(
                        icon = Icons.Default.History,
                        label = "Sisa Jatah Minggu Lalu (Rollover)",
                        quantity = stock?.rolloverFromLastWeek ?: 0L,
                        tint = Color(0xFF64748B)
                    )
                    OriginRowItem(
                        icon = Icons.Default.LocalShipping,
                        label = "Jatah Kiriman Baru Minggu Ini (Pool)",
                        quantity = stock?.currentWeekAllocation ?: 0L,
                        tint = CobaltPrimary
                    )
                    OriginRowItem(
                        icon = Icons.Default.CallReceived,
                        label = "Transfer Masuk dari Rekan Sales",
                        quantity = stock?.transfersReceived ?: 0L,
                        tint = ProfitGreen
                    )
                    OriginRowItem(
                        icon = Icons.Default.CallMade,
                        label = "Transfer Keluar ke Sales Lain",
                        quantity = -(stock?.transfersSent ?: 0L),
                        tint = DebtRed
                    )
                    OriginRowItem(
                        icon = Icons.Default.Receipt,
                        label = "Pinjaman / Konsinyasi Tambahan",
                        quantity = stock?.debtBorrowed ?: 0L,
                        tint = Color(0xFFD97706)
                    )
                    OriginRowItem(
                        icon = Icons.Default.CheckCircle,
                        label = "Total Barang Terjual Siklus Ini",
                        quantity = stock?.soldCount ?: 0L,
                        tint = ProfitGreen
                    )
                }
            }
        }
    }
}

@Composable
fun OriginRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    quantity: Long,
    tint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 12.sp, color = TextPrimary)
        }
        Text(
            text = "${if (quantity > 0 && quantity != 0L) "+" else ""}$quantity unit",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (quantity < 0) DebtRed else TextPrimary
        )
    }
}
