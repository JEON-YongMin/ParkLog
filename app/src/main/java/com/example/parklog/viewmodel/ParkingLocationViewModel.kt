package com.example.parklog.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parklog.repository.ParkingLocationRepository

class ParkingLocationViewModel : ViewModel() {

    private val repository = ParkingLocationRepository()

    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    private val _isLoading = MutableLiveData<Boolean>()

    fun saveParkingLocation(photoUri: Uri, location: String, fee: String) {
        _isLoading.value = true

        repository.saveParkingLocation(
            photoUri,
            location,
            fee,
            onSuccess = {
                _uploadStatus.value = "저장 성공!"
                _isLoading.value = false
            },
            onFailure = { error ->
                _uploadStatus.value = error
                _isLoading.value = false
            }
        )
    }
}
