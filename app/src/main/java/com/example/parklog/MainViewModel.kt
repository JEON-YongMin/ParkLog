package com.example.parklog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val _connectedCar = MutableLiveData<String>("현재 연결된 차량 없음")
    val connectedCar: LiveData<String> get() = _connectedCar

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val cars = mutableMapOf<String, String>()

    init {
        loadCarsFromDatabase()
    }

    fun addCar(carModel: String, carNumber: String) {
        if (carModel.isNotBlank() && carNumber.isNotBlank()) {
            val newCar = "$carModel : $carNumber"
            val newCarKey = database.child("cars").push().key
            if (newCarKey != null) {
                database.child("cars").child(newCarKey).setValue(newCar)
                    .addOnSuccessListener {
                        cars[newCarKey] = newCar
                        _connectedCar.value = newCar
                        _toastMessage.value = "차량이 등록되었습니다."
                    }
                    .addOnFailureListener {
                        _toastMessage.value = "차량 등록에 실패했습니다."
                    }
            }
        } else {
            _toastMessage.value = "차종과 차 번호를 입력해주세요."
        }
    }

    fun deleteCar() {
        if (cars.isNotEmpty()) {
            val lastCarKey = cars.entries.last().key
            database.child("cars").child(lastCarKey).removeValue()
                .addOnSuccessListener {
                    cars.remove(lastCarKey)
                    _connectedCar.value = if (cars.isNotEmpty()) cars.values.last() else "현재 연결된 차량 없음"
                    _toastMessage.value = "차량이 삭제되었습니다."
                }
                .addOnFailureListener {
                    _toastMessage.value = "차량 삭제에 실패했습니다."
                }
        } else {
            _toastMessage.value = "삭제할 차량이 없습니다."
        }
    }

    private fun loadCarsFromDatabase() {
        database.child("cars").get()
            .addOnSuccessListener { snapshot ->
                cars.clear()
                for (child in snapshot.children) {
                    val car = child.getValue(String::class.java)
                    val key = child.key
                    if (car != null && key != null) {
                        cars[key] = car
                    }
                }
                _connectedCar.value = if (cars.isNotEmpty()) cars.values.last() else "현재 연결된 차량 없음"
            }
    }
}
