package com.example.ui.components

import java.text.NumberFormat
import java.util.Locale

private val IndonesianLocale: Locale = Locale.forLanguageTag("id-ID")

fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(IndonesianLocale)
    return "Rp " + formatter.format(amount.toLong())
}

fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(IndonesianLocale)
    return "Rp " + formatter.format(amount)
}

fun formatNumber(number: Long): String {
    val formatter = NumberFormat.getNumberInstance(IndonesianLocale)
    return formatter.format(number)
}
