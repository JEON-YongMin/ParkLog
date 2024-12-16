package com.example.parklog.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parklog.model.RecordData
import com.example.parklog.repository.CarLogRepository

class CarLogViewModel : ViewModel() {

    private val repository = CarLogRepository()

    private val _records = MutableLiveData<List<RecordData>>()
    val records: LiveData<List<RecordData>> get() = _records

    private val _cumulativeData = MutableLiveData<Pair<Int, Int>>() // (totalMileage, totalFuelCost)
    val cumulativeData: LiveData<Pair<Int, Int>> get() = _cumulativeData

    init {
        fetchRecords()
    }

    fun fetchRecords() {
        repository.fetchRecords(
            onSuccess = { fetchedRecords, cumulative ->
                _records.value = fetchedRecords
                _cumulativeData.value = cumulative
            },
            onFailure = { /* Handle error */ }
        )
    }

    fun addRecord(record: RecordData) {
        repository.addRecord(record, onSuccess = {
            fetchRecords()
        }, onFailure = { /* Handle error */ })
    }
}
