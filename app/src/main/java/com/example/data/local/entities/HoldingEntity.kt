package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "holdings",
    indices = [Index(value = ["profileId", "ticker"], unique = true)]
)
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long = 1,
    val ticker: String, // e.g. ECO.zw, DLTA.zw, INN.zw, SIM.zw, PAD.zw, SCZ.zw
    val companyName: String,
    val exchange: String = "VFEX", // VFEX or ZSE
    val shares: Double = 0.0,
    val avgBuyPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val lastPriceUpdateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val totalCostBasis: Double get() = shares * avgBuyPrice
    val currentMarketValue: Double get() = shares * currentPrice
    val unrealizedGain: Double get() = currentMarketValue - totalCostBasis
    val returnPercentage: Double
        get() = if (totalCostBasis > 0) (unrealizedGain / totalCostBasis) * 100.0 else 0.0
}
