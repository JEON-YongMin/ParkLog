package com.example.parklog.repository

import com.example.parklog.model.ParkingLocationData
import com.google.firebase.database.*

class ParkingLocationListRepository {

    private val database = FirebaseDatabase.getInstance()
    private val dbRef = database.reference.child("parking_locations")

    fun fetchParkingLocations(
        onSuccess: (List<ParkingLocationData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ParkingLocationData>()
                for (data in snapshot.children) {
                    val item = data.getValue(ParkingLocationData::class.java)
                    item?.let { list.add(it) }
                }
                onSuccess(list)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure("데이터를 가져오지 못했습니다: ${error.message}")
            }
        })
    }

    fun updateParkingLocation(
        photoUri: String,
        updates: Map<String, Any>,
        onComplete: () -> Unit
    ) {
        dbRef.orderByChild("photoUri").equalTo(photoUri)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.updateChildren(updates)
                    }
                    onComplete()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun deleteParkingLocation(photoUri: String, onComplete: () -> Unit) {
        dbRef.orderByChild("photoUri").equalTo(photoUri)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.removeValue()
                    }
                    onComplete()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
