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
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*

@Composable
fun NewProductDialog(
    onDismiss: () -> Unit,
    onConfirmProduct: (name: String, code: String, brandOrOrigin: String, sellPrice: Double, hppPrice: Double, savings: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var brandOrOrigin by remember { mutableStateOf("Pabrik A") }
    var sellPriceText by remember { mutableStateOf("15000") }
    var hppPriceText by remember { mutableStateOf("11000") }
    var savingsText by remember { mutableStateOf("500") }

    val quickBrands = listOf("Pabrik A", "Pabrik B", "Pabrik C", "Merk Utama")
    val sellPrice = sellPriceText.toDoubleOrNull() ?: 0.0
    val hppPrice = hppPriceText.toDoubleOrNull() ?: 0.0
    val savings = savingsText.toDoubleOrNull() ?: 0.0
    val mandatoryDeposit = hppPrice + savings
    val estimatedMargin = sellPrice - mandatoryDeposit

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
                        text = "Tambah Produk Baru",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Produk (cth: Kopi Robusta 250g)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Brand / Pabrik Asal
                OutlinedTextField(
                    value = brandOrOrigin,
                    onValueChange = { brandOrOrigin = it },
                    label = { Text("Asal Pabrik / Merk Barang") },
                    placeholder = { Text("Contoh: Pabrik A / Merk ABC") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickBrands.forEach { b ->
                        SuggestionChip(
                            onClick = { brandOrOrigin = b },
                            label = { Text(b, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Kode SKU / Barcode (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = sellPriceText,
                        onValueChange = { sellPriceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Harga Jual (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hppPriceText,
                        onValueChange = { hppPriceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Modal HPP (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = savingsText,
                    onValueChange = { savingsText = it.filter { c -> c.isDigit() } },
                    label = { Text("Porsi Tabungan per Unit (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Calculated info preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CobaltContainer)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Asal Pabrik/Merk:", fontSize = 12.sp, color = CobaltDark)
                            Text(brandOrOrigin.ifBlank { "Pabrik Utama" }, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CobaltDark)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Setor Wajib Standar:", fontSize = 12.sp, color = CobaltDark)
                            Text(formatRupiah(mandatoryDeposit), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CobaltDark)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Margin Laba Sales per Unit:", fontSize = 12.sp, color = CobaltDark)
                            Text(formatRupiah(estimatedMargin), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfitGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onConfirmProduct(name, code, brandOrOrigin, sellPrice, hppPrice, savings)
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Produk", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
