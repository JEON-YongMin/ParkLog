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

        // Firebase 데이터 변화를 실시간으로 감지
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                parkingList.clear() // 기존 데이터를 초기화
                for (data in snapshot.children) { // Firebase에서 데이터를 순회
                    val item = data.getValue(ParkingLocationData::class.java) // 데이터 객체로 변환
                    item?.let { parkingList.add(it) } // 리스트에 추가
                }
                adapter.notifyDataSetChanged() // RecyclerView 갱신
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
