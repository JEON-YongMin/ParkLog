package com.example.parklog.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parklog.model.ParkingLocationData
import com.google.firebase.database.*

class ParkingLocationListViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val dbRef = database.reference.child("parking_locations")

    private val _parkingList = MutableLiveData<List<ParkingLocationData>>()
    val parkingList: LiveData<List<ParkingLocationData>> get() = _parkingList

    private val _isSortedByLatest = MutableLiveData<Boolean>(false)
    val isSortedByLatest: LiveData<Boolean> get() = _isSortedByLatest

    init {
        fetchParkingLocations()
    }

    private fun fetchParkingLocations() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ParkingLocationData>()
                for (data in snapshot.children) {
                    val item = data.getValue(ParkingLocationData::class.java)
                    item?.let { list.add(it) }
                }
                _parkingList.value = list.sortedBy { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
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
        dbRef.orderByChild("photoUri").equalTo(item.photoUri)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.updateChildren(updates)
                    }
                    fetchParkingLocations()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun deleteParkingLocation(item: ParkingLocationData) {
        dbRef.orderByChild("photoUri").equalTo(item.photoUri)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.removeValue()
                    }
                    fetchParkingLocations()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
