package com.example.parklog

data class Record(
    val date: String,            // 주유 날짜
    val stationName: String,     // 주유소 이름
    val fuel: Double,            // 주유량(L)
    val distance: Int,           // 이동 거리(km)
    val fuelEfficiency: Double,  // 연비(km/L)
    val pricePerLiter: Int,      // 1L당 가격(₩)
    val totalCost: Int           // 총 비용(₩)
)