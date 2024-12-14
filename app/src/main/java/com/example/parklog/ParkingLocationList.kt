package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityParkingLocationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ParkingLocationAdapter(parkingList) { position ->
            // 삭제 버튼 클릭 처리
            val item = parkingList[position]
            deleteParkingLocation(item, position)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                parkingList.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(ParkingLocationData::class.java)
                    item?.let { parkingList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ParkingLocationList, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun deleteParkingLocation(item: ParkingLocationData, position: Int) {
        dbRef.orderByChild("photoUri").equalTo(item.photoUri)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.removeValue() // Firebase에서 삭제
                    }
                    parkingList.removeAt(position) // 로컬 리스트에서 삭제
                    adapter.notifyItemRemoved(position) // RecyclerView 업데이트
                    Toast.makeText(this@ParkingLocationList, "삭제 완료", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ParkingLocationList, "삭제 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
