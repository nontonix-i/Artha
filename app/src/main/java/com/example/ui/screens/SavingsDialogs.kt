package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.formatRupiah
import com.example.ui.theme.*

@Composable
fun SavingsDepositDialog(
    currentSavingsBalance: Double,
    onDismiss: () -> Unit,
    onConfirmDeposit: (nominal: Double, note: String) -> Unit
) {
    var nominalInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    val quickAmounts = listOf(10000.0, 25000.0, 50000.0, 100000.0, 200000.0)
    val parsedNominal = nominalInput.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Tambah Tabungan Langsung",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Setor tabungan mandiri tanpa jualan barang",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Info saldo saat ini
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
                        Text("Saldo Pundi Saat Ini", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = formatRupiah(currentSavingsBalance),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                // Quick amount chips
                Text("Pilih Nominal Cepat:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAmounts.take(3).forEach { amt ->
                        SuggestionChip(
                            onClick = { nominalInput = amt.toLong().toString() },
                            label = { Text(formatRupiah(amt), fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickAmounts.drop(3).forEach { amt ->
                        SuggestionChip(
                            onClick = { nominalInput = amt.toLong().toString() },
                            label = { Text(formatRupiah(amt), fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Nominal Input
                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) nominalInput = input
                    },
                    label = { Text("Nominal Nabung (Rp)") },
                    placeholder = { Text("Contoh: 50000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_deposit_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Note Input
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Keterangan / Catatan (Opsional)") },
                    placeholder = { Text("Contoh: Simpanan bonus mingguan") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (parsedNominal > 0) {
                    Text(
                        text = "Estimasi Saldo Setelah Nabung: ${formatRupiah(currentSavingsBalance + parsedNominal)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ProfitGreen
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmDeposit(parsedNominal, noteInput) },
                enabled = parsedNominal > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("confirm_savings_deposit_button")
            ) {
                Text("Nabung Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun SavingsWithdrawDialog(
    currentSavingsBalance: Double,
    onDismiss: () -> Unit,
    onConfirmWithdraw: (nominal: Double, note: String) -> Unit
) {
    var nominalInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    val parsedNominal = nominalInput.toDoubleOrNull() ?: 0.0
    val isExceeding = parsedNominal > currentSavingsBalance

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ambil Tabungan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tarik dana dari pundi tabungan Anda",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Info Saldo Saat Ini
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SlateBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Saldo Tabungan Tersedia", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = formatRupiah(currentSavingsBalance),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                // Nominal Input
                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) nominalInput = input
                    },
                    label = { Text("Nominal Penarikan (Rp)") },
                    placeholder = { Text("Maksimal ${formatRupiah(currentSavingsBalance)}") },
                    isError = isExceeding,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("savings_withdraw_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (isExceeding) {
                    Text(
                        text = "Nominal melebihi saldo tabungan tersedia!",
                        color = DebtRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Note Input
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Tujuan / Catatan Penarikan") },
                    placeholder = { Text("Contoh: Keperluan operasional/pribadi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                if (parsedNominal > 0 && !isExceeding) {
                    Text(
                        text = "Sisa Saldo Setelah Ditarik: ${formatRupiah(currentSavingsBalance - parsedNominal)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmWithdraw(parsedNominal, noteInput) },
                enabled = parsedNominal > 0 && !isExceeding,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("confirm_savings_withdraw_button")
            ) {
                Text("Tarik Dana", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Text("Batal")
            }
        }
    )
}
