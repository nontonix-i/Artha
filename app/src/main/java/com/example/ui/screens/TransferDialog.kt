package com.example.ui.screens

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
fun TransferDialog(
    products: List<Product>,
    teamUsers: List<UserProfile>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onConfirmTransfer: (toUser: UserProfile, product: Product, quantity: Long, note: String) -> Unit
) {
    val availableRecipients = remember(teamUsers, currentUserId) {
        teamUsers.filter { it.uid != currentUserId }
    }

    if (products.isEmpty() || availableRecipients.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Transfer Belum Dapat Dilakukan") },
            text = {
                Text(
                    if (products.isEmpty()) "Tambahkan produk terlebih dahulu."
                    else "Perlu minimal 1 pengguna tim sales lain untuk transfer barang."
                )
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
        )
        return
    }

    var selectedProduct by remember { mutableStateOf(products.first()) }
    var selectedUser by remember { mutableStateOf(availableRecipients.first()) }
    var quantityText by remember { mutableStateOf("1") }
    var noteText by remember { mutableStateOf("") }
    var expandedProductDropdown by remember { mutableStateOf(false) }
    var expandedUserDropdown by remember { mutableStateOf(false) }

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
                            text = "Transfer Barang Antar-Sales",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Kirim stok langsung ke sales lain",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Select Product
                Text("Pilih Produk", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedProductDropdown,
                    onExpandedChange = { expandedProductDropdown = !expandedProductDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedProduct.name,
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
                                text = { Text(prod.name, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    selectedProduct = prod
                                    expandedProductDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Select Recipient Sales
                Text("Penerima (Sales Lain)", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = expandedUserDropdown,
                    onExpandedChange = { expandedUserDropdown = !expandedUserDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedUser.displayName.ifBlank { selectedUser.email },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUserDropdown) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUserDropdown,
                        onDismissRequest = { expandedUserDropdown = false }
                    ) {
                        availableRecipients.forEach { user ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(user.displayName.ifBlank { user.email }, fontWeight = FontWeight.SemiBold)
                                        Text(user.email, fontSize = 11.sp, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    selectedUser = user
                                    expandedUserDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity
                Text("Jumlah Transfer (Unit)", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Catatan / Alasan Transfer (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Transfer Button
                Button(
                    onClick = {
                        val qty = quantityText.toLongOrNull() ?: 1L
                        onConfirmTransfer(selectedUser, selectedProduct, qty, noteText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CobaltPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Kirim Barang", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
