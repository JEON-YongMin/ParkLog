package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.parklog.databinding.ActivityParkingLocationListBinding
import com.google.firebase.database.*

class ParkingLocationList : AppCompatActivity() {

    private lateinit var binding: ActivityParkingLocationListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding 초기화
        binding = ActivityParkingLocationListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recParkinglist.layoutManager = LinearLayoutManager(this)
        binding.recParkinglist.adapter

        // Firebase Realtime Database에서 가장 최근에 저장된 이미지 URL 가져오기
        val database = FirebaseDatabase.getInstance()
        val dbRef = database.reference.child("parking_images")

        dbRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val imageUrl = data.getValue(String::class.java)
                    if (imageUrl != null) {
                        // Glide를 사용해 ImageView에 이미지 로드
                        Glide.with(this@ParkingLocationList)
                            .load(imageUrl)
                            .into(binding.imageView) // ViewBinding으로 ImageView 참조
                    } else {
                        Toast.makeText(this@ParkingLocationList, "No image found", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ParkingLocationList, "Failed to load image: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // 홈 버튼 클릭 이벤트 설정
        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
