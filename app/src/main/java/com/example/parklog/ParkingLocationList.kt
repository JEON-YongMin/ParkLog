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
    private var isSortedByLatest = false // 정렬 상태 플래그
    private val database = FirebaseDatabase.getInstance()
    private val dbRef = database.reference.child("parking_locations")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityParkingLocationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ParkingLocationAdapter(parkingList) { position ->
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
                sortList() // 정렬 상태 유지
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ParkingLocationList,
                    "Failed to load data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // 정렬 버튼 클릭 이벤트
        binding.sortButton.setOnClickListener {
            isSortedByLatest = !isSortedByLatest // 정렬 상태 토글
            sortList()
        }

        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    // 리스트 정렬 함수
    private fun sortList() {
        if (isSortedByLatest)
            parkingList.sortByDescending { it.timestamp } // 최신순 정렬

        else
            parkingList.sortBy { it.timestamp } // 오래된 순 정렬

        adapter.notifyDataSetChanged() // RecyclerView 갱신
    }

    private fun deleteParkingLocation(item: ParkingLocationData, position: Int) {
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
                    Toast.makeText(this@ParkingLocationList, "삭제 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

