package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {

    private val usdFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    private val numberFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    fun formatUsd(amount: Double): String {
        return usdFormat.format(amount)
    }

    fun formatCurrency(amount: Double, currency: String = "USD", usdToZigRate: Double = 26.50): String {
        return if (currency.equals("ZWG", ignoreCase = true) || currency.equals("ZiG", ignoreCase = true)) {
            "ZiG " + numberFormat.format(amount)
        } else {
            usdFormat.format(amount)
        }
    }

    fun formatWithEquivalent(amountInUsd: Double, usdToZigRate: Double = 26.50): String {
        val usdStr = formatUsd(amountInUsd)
        val zigAmount = amountInUsd * usdToZigRate
        return "$usdStr (ZiG ${numberFormat.format(zigAmount)})"
    }

    fun formatPercent(percent: Double): String {
        val sign = if (percent > 0) "+" else ""
        return "$sign${String.format(Locale.US, "%.1f", percent)}%"
    }

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatMonth(millis: Long): String {
        val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
