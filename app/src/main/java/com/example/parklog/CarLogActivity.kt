package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CarLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_log)

        // 더미 데이터 생성
        val recentRecords = listOf(
            Record("2024-11-23", "지에스칼텍스 서울점", 13.64, 249, 18.26, 1466, 20000),
            Record("2024-11-20", "S-Oil 경기점", 22.08, 382, 17.3, 1359, 30000),
            Record("2024-11-18", "현대오일뱅크 부산점", 11.0, 150, 13.5, 1500, 16500)
        )

        // RecyclerView 초기화
        val recyclerView: RecyclerView = findViewById(R.id.recycler_recent_records)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecentRecordsAdapter(recentRecords)

        // "더보기" 버튼 클릭 시 RecentRecordActivity로 이동
        val moreButton: TextView = findViewById(R.id.more_button)
        moreButton.setOnClickListener {
            val intent = Intent(this, RecentRecordActivity::class.java)
            startActivity(intent)
        }
    }
}
