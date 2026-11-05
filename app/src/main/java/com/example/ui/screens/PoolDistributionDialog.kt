package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoolDistributionDialog(
    products: List<Product>,
    teamUsers: List<UserProfile>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onConfirmDistribution: (
        product: Product,
        weekIdentifier: String,
        totalPoolQty: Long,
        allocations: Map<String, Long>
    ) -> Unit
) {
    if (products.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Belum Ada Produk") },
            text = { Text("Tambahkan produk terlebih dahulu sebelum membagi jatah pool.") },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
        )
        return
    }

    var selectedProduct by remember { mutableStateOf(products.first()) }
    var weekIdentifier by remember { mutableStateOf("Minggu Baru (Pool)") }
    var totalPoolQtyText by remember { mutableStateOf("100") }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Map of userId to allocated quantity text
    val allocationMap = remember(teamUsers) {
        mutableStateMapOf<String, String>().apply {
            teamUsers.forEach { user ->
                put(user.uid, if (user.uid == currentUserId) "20" else "0")
            }
        }
    }

    val totalPoolQty = totalPoolQtyText.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    val totalAllocated = allocationMap.values.sumOf { it.toLongOrNull() ?: 0L }
    val remainingInPool = (totalPoolQty - totalAllocated).coerceAtLeast(0L)

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
                            text = "Distribusi Jatah Pool",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Kiriman baru mingguan untuk tim sales",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Picker
                Text("Pilih Produk Barang", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedProduct.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = { Text(prod.name, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    selectedProduct = prod
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Week Identifier & Total Pool Quantity
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weekIdentifier,
                        onValueChange = { weekIdentifier = it },
                        label = { Text("Kode Periode", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = totalPoolQtyText,
                        onValueChange = { totalPoolQtyText = it.filter { c -> c.isDigit() } },
                        label = { Text("Total Kiriman", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Allocation Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Alokasi ke Tim Sales", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Text(
                        text = "Sisa di Pool: $remainingInPool unit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (remainingInPool >= 0) CobaltPrimary else DebtRed
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sales list with quantity inputs
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(teamUsers) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName.ifBlank { user.email },
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (user.uid == currentUserId) "(Akun Saya)" else user.role,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            OutlinedTextField(
                                value = allocationMap[user.uid] ?: "0",
                                onValueChange = { newVal ->
                                    allocationMap[user.uid] = newVal.filter { c -> c.isDigit() }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.width(80.dp),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Distribution Button
                Button(
                    onClick = {
                        val allocations = allocationMap.mapValues { it.value.toLongOrNull() ?: 0L }
                        onConfirmDistribution(selectedProduct, weekIdentifier, totalPoolQty, allocations)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Distribusikan Jatah Sekarang", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
