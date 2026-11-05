package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun DebtDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onConfirmDebt: (type: String, product: Product?, quantity: Long, nominal: Double, note: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("SETORAN_TERTUNDA") } // or "PINJAM_BARANG"
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantityText by remember { mutableStateOf("1") }
    var noteInput by remember { mutableStateOf("") }
    var expandedProductDropdown by remember { mutableStateOf(false) }

    val initialNominal = if (products.isNotEmpty()) {
        products.first().hppPrice.toLong().toString()
    } else "50000"
    var nominalText by remember { mutableStateOf(initialNominal) }

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
                    Column {
                        Text(
                            text = "Catat Hutang / Pinjaman",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DebtRed
                        )
                        Text(
                            text = "Tunggakan kas atau pinjam barang (HPP pabrik)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Type Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "SETORAN_TERTUNDA",
                        onClick = { selectedType = "SETORAN_TERTUNDA" },
                        label = { Text("Hutang Setoran Kas", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == "PINJAM_BARANG",
                        onClick = {
                            selectedType = "PINJAM_BARANG"
                            selectedProduct?.let { p ->
                                val q = quantityText.toLongOrNull() ?: 1L
                                nominalText = (p.hppPrice * q).toLong().toString()
                            }
                        },
                        label = { Text("Pinjam Barang (HPP)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedType == "PINJAM_BARANG" && products.isNotEmpty()) {
                    Text("Pilih Produk Dipinjam", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedProductDropdown,
                        onExpandedChange = { expandedProductDropdown = !expandedProductDropdown }
                    ) {
                        OutlinedTextField(
                            value = "${selectedProduct?.name ?: "Pilih Barang"} [${selectedProduct?.brandOrOrigin ?: "Pabrik Utama"}]",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProductDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProductDropdown,
                            onDismissRequest = { expandedProductDropdown = false }
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
                                                Text(prod.brandOrOrigin.ifBlank { "Pabrik Utama" }, fontSize = 11.sp, color = CobaltPrimary, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                "Modal HPP: ${formatRupiah(prod.hppPrice)} (Bukan harga jual)",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedProduct = prod
                                        expandedProductDropdown = false
                                        val q = quantityText.toLongOrNull() ?: 1L
                                        nominalText = (prod.hppPrice * q).toLong().toString()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Jumlah Barang (Unit)", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { newVal ->
                            quantityText = newVal.filter { it.isDigit() }
                            val q = quantityText.toLongOrNull() ?: 0L
                            selectedProduct?.let { p ->
                                nominalText = (p.hppPrice * q).toLong().toString()
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    selectedProduct?.let { p ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DebtRedLight)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Dihitung berdasarkan Modal Pokok HPP (${formatRupiah(p.hppPrice)} / unit), BUKAN harga jual konsumen.",
                                fontSize = 11.sp,
                                color = DebtRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Nominal Input
                Text("Total Nilai Nominal (Rp)", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nominalText,
                    onValueChange = { nominalText = it.filter { c -> c.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Note
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Keterangan / Catatan (Opsional)") },
                    placeholder = { Text("Contoh: Pinjam display toko / titip bayar") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val qty = quantityText.toLongOrNull() ?: 0L
                        val nominal = nominalText.toDoubleOrNull() ?: 0.0
                        onConfirmDebt(selectedType, selectedProduct, qty, nominal, noteInput)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DebtRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Catatan Hutang", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
