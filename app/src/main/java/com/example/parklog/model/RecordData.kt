package com.example.parklog.model

data class RecordData(
    val date: String = "",
    val stationName: String = "",
    val startLocation: String = "",
    val endLocation: String = "",
    val distance: Int = 0,
    val pricePerLiter: Int = 0,
    val totalCost: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),  // 타임스탬프
    val latitude: Double? = null,
    val longitude: Double? = null
)