package com.example.parklog

data class ParkingLocationData(
    val photoUri: String = "",
    val location: String = "",
    val fee: String = "",
    val timestamp: String = "" // 저장시간 필드 추가
)
