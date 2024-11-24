package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.ActivityParkingLocationListBinding
import com.example.parklog.ParkingLocationAdapter // Adapter import 추가
import com.google.firebase.database.*

class ParkingLocationList : AppCompatActivity() {

    private lateinit var binding: ActivityParkingLocationListBinding
    private val parkingList = mutableListOf<ParkingLocationData>()
    private lateinit var adapter: ParkingLocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityParkingLocationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView 설정
        adapter = ParkingLocationAdapter(parkingList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Firebase Realtime Database에서 데이터 가져오기
        val database = FirebaseDatabase.getInstance()
        val dbRef = database.reference.child("parking_locations")

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

        // 홈 버튼 클릭 이벤트
        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
