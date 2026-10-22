package com.example.data.local.converters

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.util.Date

enum class CurrencyType(val code: String, val symbol: String, val displayName: String) {
    USD("USD", "$", "US Dollar"),
    ZWG("ZWG", "ZiG", "Zimbabwe Gold"),
    ZAR("ZAR", "R", "South African Rand"),
    GBP("GBP", "£", "British Pound"),
    EUR("EUR", "€", "Euro");

    companion object {
        fun fromCode(code: String?): CurrencyType {
            if (code == null) return USD
            val clean = code.trim().uppercase()
            return when {
                clean == "ZWG" || clean == "ZIG" -> ZWG
                clean == "ZAR" -> ZAR
                clean == "GBP" -> GBP
                clean == "EUR" -> EUR
                else -> USD
            }
        }
    }
}

class AppTypeConverters {

    // --- Date Converters ---
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // --- Currency Converters ---
    @TypeConverter
    fun fromCurrencyType(currency: CurrencyType?): String? {
        return currency?.code
    }

    @TypeConverter
    fun toCurrencyType(value: String?): CurrencyType {
        return CurrencyType.fromCode(value)
    }

    // --- BigDecimal / Fixed Decimal Minor Units ---
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let {
            try {
                BigDecimal(it)
            } catch (e: Exception) {
                BigDecimal.ZERO
            }
        }
    }
}
