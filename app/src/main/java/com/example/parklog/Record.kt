package com.example.parklog

data class Record(
    val date: String = "",            // 주유 날짜
    val stationName: String = "",     // 주유소 이름
    val fuelAmount: Double = 0.0,     // 총 주유량(L)
    val distance: Int = 0,            // 이동 거리(km)
    val pricePerLiter: Int = 0,       // 1L당 가격(₩)
    val totalCost: Int = 0            // 총 비용(₩)
)