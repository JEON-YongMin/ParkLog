package com.example.parklog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*

class CarLogViewModel : ViewModel() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    private val _records = MutableLiveData<List<RecordData>>()
    val records: LiveData<List<RecordData>> get() = _records

    private val _cumulativeData = MutableLiveData<Pair<Int, Int>>() // (totalMileage, totalFuelCost)
    val cumulativeData: LiveData<Pair<Int, Int>> get() = _cumulativeData

    init {
        fetchRecordsFromRealtimeDatabase()
        fetchCumulativeDataFromFirebase()
    }

    private fun fetchRecordsFromRealtimeDatabase() {
        database.child("CarRecords").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedRecords = mutableListOf<RecordData>()
                snapshot.children.forEach { child ->
                    val record = child.getValue(RecordData::class.java)
                    if (record != null) {
                        fetchedRecords.add(record)
                    }
                }
                _records.value = fetchedRecords.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun fetchCumulativeDataFromFirebase() {
        database.child("CumulativeData").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalMileage = snapshot.child("totalMileage").getValue(Int::class.java) ?: 0
                val totalFuelCost = snapshot.child("totalFuelCost").getValue(Int::class.java) ?: 0
                _cumulativeData.value = Pair(totalMileage, totalFuelCost)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun addRecord(record: RecordData) {
        database.child("CarRecords").push().setValue(record)
    }

    fun updateCumulativeData(newDistance: Int, newFuelCost: Int) {
        database.child("CumulativeData").get().addOnSuccessListener { snapshot ->
            val totalMileage = snapshot.child("totalMileage").getValue(Int::class.java) ?: 0
            val totalFuelCost = snapshot.child("totalFuelCost").getValue(Int::class.java) ?: 0

            val updatedData = mapOf(
                "totalMileage" to (totalMileage + newDistance),
                "totalFuelCost" to (totalFuelCost + newFuelCost)
            )

            database.child("CumulativeData").setValue(updatedData)
        }
    }
}
