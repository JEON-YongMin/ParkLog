package com.example.parklog.repository

import com.example.parklog.model.RecordData
import com.google.firebase.database.*

class CarLogRepository {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun fetchRecords(
        onSuccess: (List<RecordData>, Pair<Int, Int>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.child("CarRecords").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val records = mutableListOf<RecordData>()
                var totalMileage = 0
                var totalFuelCost = 0

                snapshot.children.forEach { child ->
                    val record = child.getValue(RecordData::class.java)
                    if (record != null) {
                        records.add(record)
                        totalMileage += record.distance
                        totalFuelCost += record.totalCost
                    }
                }

                onSuccess(records.sortedByDescending { it.timestamp }, Pair(totalMileage, totalFuelCost))
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure("기록을 가져오지 못했습니다: ${error.message}")
            }
        })
    }

    fun addRecord(record: RecordData, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        database.child("CarRecords").push().setValue(record)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure("기록을 추가하지 못했습니다.") }
    }
}
