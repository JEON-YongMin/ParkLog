package com.example.parklog.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parklog.repository.MainRepository

class MainViewModel : ViewModel() {

    private val repository = MainRepository()

    private val _connectedCar = MutableLiveData("현재 연결된 차량 없음")
    val connectedCar: LiveData<String> get() = _connectedCar

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    init {
        loadCarFromDatabase()
    }

    fun addCar(carModel: String, carNumber: String) {
        if (carModel.isBlank() || carNumber.isBlank()) {
            _toastMessage.value = "차종과 차 번호를 입력해주세요."
            return
        }

        val newCar = "$carModel : $carNumber"
        repository.addCar(
            carModel, carNumber,
            onSuccess = {
                _connectedCar.value = newCar
                _toastMessage.value = "차량이 등록되었습니다."
            },
            onFailure = { message ->
                _toastMessage.value = message
            }
        )
    }

    fun deleteCar() {
        if (_connectedCar.value == "현재 연결된 차량 없음") {
            _toastMessage.value = "삭제할 차량이 없습니다."
            return
        }

        repository.deleteCar(
            onSuccess = {
                _connectedCar.value = "현재 연결된 차량 없음"
                _toastMessage.value = "차량이 삭제되었습니다."
            },
            onFailure = { message ->
                _toastMessage.value = message
            }
        )
    }

    private fun loadCarFromDatabase() {
        repository.loadCarFromDatabase(
            onSuccess = { car ->
                _connectedCar.value = car ?: "현재 연결된 차량 없음"
            },
            onFailure = { message ->
                _toastMessage.value = message
            }
        )
    }
}
