package com.example.parklog

data class RecordData(
    val date: String = "",            // 주유 날짜
    val stationName: String = "",     // 주유소 이름
    val startLocation: String = "",   // 출발 위치
    val endLocation: String = "",     // 도착 위치
    val distance: Int = 0,            // 이동 거리(km)
    val pricePerLiter: Int = 0,       // 1L당 가격(₩)
    val fuelAmount: Double = 0.0,     // 총 주유량(L)
    val totalCost: Int = 0,           // 총 비용(₩)
    val timestamp: Long = System.currentTimeMillis(),  // 타임스탬프
    val latitude: Double? = null,  // 위도
    val longitude: Double? = null // 경도
)