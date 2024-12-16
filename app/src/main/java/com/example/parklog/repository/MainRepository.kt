package com.example.parklog.repository

import com.google.firebase.database.FirebaseDatabase

class MainRepository {

    private val database = FirebaseDatabase.getInstance().reference

    fun addCar(
        carModel: String,
        carNumber: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val newCar = "$carModel : $carNumber"
        database.child("car")
            .setValue(newCar)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure("차량 등록에 실패했습니다.") }
    }

    fun deleteCar(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.child("car")
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure("차량 삭제에 실패했습니다.") }
    }

    fun loadCarFromDatabase(
        onSuccess: (String?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.child("car")
            .get()
            .addOnSuccessListener { snapshot ->
                val car = snapshot.getValue(String::class.java)
                onSuccess(car)
            }
            .addOnFailureListener { onFailure("데이터를 불러오는 데 실패했습니다.") }
    }
}
