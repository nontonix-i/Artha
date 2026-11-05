package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirmSale: (product: Product, quantity: Long, isSavingsIncluded: Boolean, note: String) -> Unit
) {
    if (products.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Belum Ada Produk") },
            text = { Text("Tambahkan produk barang terlebih dahulu di menu Inventaris.") },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Tutup") }
            }
        )
        return
    }

    var selectedProduct by remember { mutableStateOf(products.first()) }
    var quantityText by remember { mutableStateOf("1") }
    var isSavingsIncluded by remember { mutableStateOf(true) }
    var noteInput by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    val quantity = quantityText.toLongOrNull()?.coerceAtLeast(1L) ?: 1L

    val hpp = selectedProduct.hppPrice
    val savings = if (isSavingsIncluded) selectedProduct.savingsPerUnit else 0.0
    val mandatoryDepositPerUnit = hpp + savings
    val totalRevenue = selectedProduct.sellPrice * quantity
    val totalDeposit = mandatoryDepositPerUnit * quantity
    val totalSavings = savings * quantity
    val netMarginSales = totalRevenue - totalDeposit

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Catat Penjualan & Setoran",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Picker
                Text(
                    text = "Pilih Produk & Asal Merk",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = "${selectedProduct.name} [${selectedProduct.brandOrOrigin.ifBlank { "Pabrik Utama" }}] - ${formatRupiah(selectedProduct.sellPrice)}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(prod.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                prod.brandOrOrigin.ifBlank { "Pabrik Utama" },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CobaltPrimary
                                            )
                                        }
                                        Text(
                                            "Jual: ${formatRupiah(prod.sellPrice)} | Modal HPP: ${formatRupiah(prod.hppPrice)}",
                                            fontSize = 12.sp,
                                            color = TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProduct = prod
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity Input & Stepper
                Text(
                    text = "Jumlah Terjual (Unit)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilledIconButton(
                        onClick = {
                            val current = quantityText.toLongOrNull() ?: 1L
                            if (current > 1) quantityText = (current - 1).toString()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = CobaltContainer),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurang", tint = CobaltPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { char -> char.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            val current = quantityText.toLongOrNull() ?: 0L
                            quantityText = (current + 1).toString()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = CobaltContainer),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah", tint = CobaltPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory Deposit & Savings Checklist Toggle (Key User Request)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isSavingsIncluded = !isSavingsIncluded },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSavingsIncluded) CobaltContainer else SlateBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSavingsIncluded,
                            onCheckedChange = { isSavingsIncluded = it },
                            colors = CheckboxDefaults.colors(checkedColor = CobaltPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Sertakan Tabungan (${formatRupiah(selectedProduct.savingsPerUnit)} / unit)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isSavingsIncluded)
                                    "Setoran wajib = HPP (${formatRupiah(hpp)}) + Tabungan = ${formatRupiah(mandatoryDepositPerUnit)}"
                                else
                                    "Setoran wajib hanya HPP murni = ${formatRupiah(hpp)}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Note
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Contoh: Pembeli langganan / titip warung") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Financial Breakdown Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SlateBackground)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Asal Barang/Pabrik", fontSize = 11.sp, color = TextSecondary)
                            Text(selectedProduct.brandOrOrigin.ifBlank { "Pabrik Utama" }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CobaltPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Kas Omset", fontSize = 11.sp, color = TextSecondary)
                            Text(formatRupiah(totalRevenue), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Setoran Wajib (${selectedProduct.brandOrOrigin})", fontSize = 11.sp, color = TextSecondary)
                            Text(formatRupiah(totalDeposit), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CobaltPrimary)
                        }
                        if (isSavingsIncluded) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pundi Tabungan Masuk", fontSize = 11.sp, color = TextSecondary)
                                Text(formatRupiah(totalSavings), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0284C7))
                            }
                        }
                        HorizontalDivider(color = BorderSubtle, thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimasi Laba Sales", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(formatRupiah(netMarginSales), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Button
                Button(
                    onClick = {
                        onConfirmSale(selectedProduct, quantity, isSavingsIncluded, noteInput)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Penjualan", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
