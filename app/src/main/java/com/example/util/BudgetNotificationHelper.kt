package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import java.util.Locale

object BudgetNotificationHelper {

    const val CHANNEL_ID = "channel_budget_spending_alerts"
    const val CHANNEL_NAME = "Budget & Spending Alerts"
    const val CHANNEL_DESC = "Alerts triggered when monthly category spending reaches or exceeds defined budget limits."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendBudgetThresholdAlert(
        context: Context,
        category: String,
        spentAmount: Double,
        limitAmount: Double,
        thresholdPercent: Double = 100.0
    ): Boolean {
        createNotificationChannel(context)

        // Permission check for Android 13+ (API 33)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                return false
            }
        }

        val spentPct = if (limitAmount > 0) (spentAmount / limitAmount) * 100.0 else 100.0
        val isExceeded = spentAmount >= limitAmount
        val overBy = (spentAmount - limitAmount).coerceAtLeast(0.0)

        val title = if (isExceeded) {
            "🚨 Budget Exceeded: $category"
        } else {
            "⚠️ Spending Threshold Alert: $category"
        }

        val contentText = if (isExceeded) {
            "You have exceeded your monthly limit for $category by ${CurrencyFormatter.formatUsd(overBy)}."
        } else {
            "You have reached ${String.format(Locale.US, "%.0f%%", spentPct)} of your ${CurrencyFormatter.formatUsd(limitAmount)} $category budget."
        }

        val bigText = if (isExceeded) {
            "Alert: Monthly spending on $category reached ${CurrencyFormatter.formatUsd(spentAmount)}, surpassing your predefined ceiling of ${CurrencyFormatter.formatUsd(limitAmount)} by ${CurrencyFormatter.formatUsd(overBy)} (${String.format(Locale.US, "%.1f%%", spentPct)} utilized). Tap to review and adjust your allocations in MoneyPilot ZW."
        } else {
            "Notice: Monthly spending on $category is at ${CurrencyFormatter.formatUsd(spentAmount)} of your ${CurrencyFormatter.formatUsd(limitAmount)} limit (${String.format(Locale.US, "%.1f%%", spentPct)} of budget threshold ${String.format(Locale.US, "%.0f%%", thresholdPercent)}). You have ${CurrencyFormatter.formatUsd(limitAmount - spentAmount)} remaining."
        }

        // Tap action: open MainActivity directly to Budget Planner
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "BUDGET_PLANNER")
            putExtra("alert_category", category)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            category.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_alert)
            .setContentTitle(title)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(if (isExceeded) 0xFFDC2626.toInt() else 0xFFD97706.toInt())
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            val notificationId = 1000 + (category.hashCode() and 0x7FFF)
            notificationManager.notify(notificationId, notification)
            return true
        } catch (e: SecurityException) {
            return false
        }
    }
}
