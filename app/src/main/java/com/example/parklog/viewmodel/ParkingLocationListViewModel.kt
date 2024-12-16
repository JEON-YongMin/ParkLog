package com.example.parklog.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parklog.model.ParkingLocationData
import com.example.parklog.repository.ParkingLocationListRepository

class ParkingLocationListViewModel : ViewModel() {

    private val repository = ParkingLocationListRepository() // 클래스명 변경 확인!

    private val _parkingList = MutableLiveData<List<ParkingLocationData>>()
    val parkingList: LiveData<List<ParkingLocationData>> get() = _parkingList

    private val _isSortedByLatest = MutableLiveData<Boolean>(false)
    val isSortedByLatest: LiveData<Boolean> get() = _isSortedByLatest

    init {
        fetchParkingLocations()
    }

    private fun fetchParkingLocations() {
        repository.fetchParkingLocations(
            onSuccess = { list ->
                _parkingList.value = list.sortedBy { it.timestamp }
            },
            onFailure = {
                // 에러 처리
            }
        )
    }

    fun toggleSortOrder() {
        _isSortedByLatest.value = !(_isSortedByLatest.value ?: false)
        sortList()
    }

    private fun sortList() {
        _parkingList.value = _parkingList.value?.let {
            if (_isSortedByLatest.value == true) it.sortedByDescending { it.timestamp }
            else it.sortedBy { it.timestamp }
        }
    }

    fun updateParkingLocation(item: ParkingLocationData, newLocation: String, newFee: String) {
        val updates = mapOf("location" to newLocation, "fee" to newFee)
        repository.updateParkingLocation(item.photoUri, updates) {
            fetchParkingLocations()
        }
    }

    fun deleteParkingLocation(item: ParkingLocationData) {
        repository.deleteParkingLocation(item.photoUri) {
            fetchParkingLocations()
        }
    }
}
