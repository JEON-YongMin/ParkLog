package com.example.parklog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecentRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_record)

        // 더미 데이터 생성
        val allRecords = listOf(
            Record("2024-11-23", "지에스칼텍스 서울점", 13.64, 249, 18.26, 1466, 20000),
            Record("2024-11-20", "S-Oil 경기점", 22.08, 382, 17.3, 1359, 30000),
            Record("2024-11-18", "현대오일뱅크 부산점", 11.0, 150, 13.5, 1500, 16500),
            Record("2024-11-15", "GS칼텍스 대구점", 19.4, 300, 15.5, 1520, 29480),
            Record("2024-11-10", "S-Oil 부산점", 17.0, 250, 14.7, 1480, 25160)
        )

        // RecyclerView 초기화
        val recyclerView: RecyclerView = findViewById(R.id.recycler_recent_records)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecentRecordsAdapter(allRecords)
    }
}
