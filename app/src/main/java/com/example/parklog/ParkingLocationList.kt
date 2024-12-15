package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.ActivityParkingLocationListBinding
import com.google.firebase.database.*

class ParkingLocationList : AppCompatActivity() {

    private lateinit var binding: ActivityParkingLocationListBinding
    private val parkingList = mutableListOf<ParkingLocationData>()
    private lateinit var adapter: ParkingLocationAdapter
    private val database = FirebaseDatabase.getInstance()
    private val dbRef = database.reference.child("parking_locations")
    private var isSortedByLatest = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParkingLocationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ParkingLocationAdapter(parkingList,
            onDeleteClicked = { position -> deleteParkingLocation(position) },
            onEditClicked = { position, location, fee -> showEditDialog(position, location, fee) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                parkingList.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(ParkingLocationData::class.java)
                    item?.let { parkingList.add(it) }
                }
                sortList()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ParkingLocationList, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })

        binding.sortButton.setOnClickListener {
            isSortedByLatest = !isSortedByLatest
            sortList()
        }

        binding.homeButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun showEditDialog(position: Int, currentLocation: String, currentFee: String) {
        val dialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_parking, null)

        val editLocation = view.findViewById<EditText>(R.id.editLocation)
        val editFee = view.findViewById<EditText>(R.id.editFee)

        editLocation.setText(currentLocation)
        editFee.setText(currentFee)

        dialog.setView(view)
        dialog.setPositiveButton("수정") { _, _ ->
            val updatedLocation = editLocation.text.toString()
            val updatedFee = editFee.text.toString()

            updateParkingLocation(position, updatedLocation, updatedFee)
        }
        dialog.setNegativeButton("취소", null)
        dialog.show()
    }

    private fun updateParkingLocation(position: Int, location: String, fee: String) {
        val item = parkingList[position]
        val updates = mapOf("location" to location, "fee" to fee)

        dbRef.orderByChild("photoUri").equalTo(item.photoUri)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.updateChildren(updates)
                    }
                    parkingList[position].location = location
                    parkingList[position].fee = fee
                    adapter.notifyItemChanged(position)
                    Toast.makeText(this@ParkingLocationList, "수정 완료", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ParkingLocationList, "수정 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteParkingLocation(position: Int) {
        val item = parkingList[position]
        dbRef.orderByChild("photoUri").equalTo(item.photoUri)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.removeValue()
                    }
                    parkingList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    Toast.makeText(this@ParkingLocationList, "삭제 완료", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ParkingLocationList, "삭제 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sortList() {
        if (isSortedByLatest)
            parkingList.sortByDescending { it.timestamp }
        else
            parkingList.sortBy { it.timestamp }

        adapter.notifyDataSetChanged()
    }
}
