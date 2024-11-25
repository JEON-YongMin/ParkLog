package com.example.parklog

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.AddDistanceBinding
import com.example.parklog.databinding.AddFuelBinding
import com.example.parklog.databinding.ActivityCarLogBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class CarLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarLogBinding
    private val records = mutableListOf<RecordData>() // 기록 리스트
    private lateinit var adapter: RecentRecordsAdapter
    private lateinit var database: DatabaseReference // Realtime Database 참조

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityCarLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance().reference.child("CarRecords")

        // RecyclerView 초기화
        adapter = RecentRecordsAdapter(records)
        binding.recyclerRecentRecords.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentRecords.adapter = adapter

        // Firebase에서 초기 데이터 가져오기
        fetchRecordsFromRealtimeDatabase()

        // 누적 데이터 업데이트
        updateCumulativeData()

        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // MainActivity로 이동
            startActivity(intent) // 새 액티비티 시작
        }

        // 버튼 클릭 이벤트
        binding.btnAddMileage.setOnClickListener {
            showAddDistanceDialog()
        }

        binding.btnAddFuel.setOnClickListener {
            showAddFuelDialog()
        }
    }

    // Firebase Realtime Database에서 기록 데이터 가져오기
    private fun fetchRecordsFromRealtimeDatabase() {
        database.get().addOnSuccessListener { snapshot ->
            records.clear()
            snapshot.children.forEach { child ->
                val record = child.getValue(RecordData::class.java)
                record?.let { records.add(it) }
            }

            // 데이터 정렬: 최신 순으로 timestamp 기준 내림차순 정렬
            records.sortByDescending { it.timestamp }

            // RecyclerView 갱신
            adapter.notifyDataSetChanged()

            // 누적 데이터 업데이트
            updateCumulativeData()
        }.addOnFailureListener { e ->
            Log.w("Firebase", "Error getting documents", e)
        }
    }
    
    // 누적 주행 거리와 주유 비용을 계산하여 UI를 업데이트
    private fun updateCumulativeData() {
        val totalMileage = records.sumOf { it.distance }
        val totalFuelCost = records.sumOf { it.totalCost }

        binding.totalMileageValue.text = "$totalMileage km"
        binding.totalFuelCostValue.text = "₩$totalFuelCost"
    }

    // 주행 기록 추가 Dialog
    private fun showAddDistanceDialog() {
        val dialogBinding = AddDistanceBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setTitle("주행 기록 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가") { _, _ ->
                val date = dialogBinding.inputDate.text.toString()
                val distance = dialogBinding.inputDistance.text.toString().toIntOrNull() ?: 0

                // 새로운 기록 추가
                val record = RecordData(date, "주행 기록", 0.0, distance, 0, 0)
                records.add(0, record) // 리스트의 가장 앞에 추가
                adapter.notifyItemInserted(0) // RecyclerView 갱신
                updateCumulativeData()

                // Firebase에 저장
                database.push().setValue(record)
                    .addOnSuccessListener {
                        Log.d("Firebase", "주행 기록 저장 성공")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firebase", "주행 기록 저장 실패", e)
                    }
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    // 주유 기록 추가 Dialog
    private fun showAddFuelDialog() {
        val dialogBinding = AddFuelBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setTitle("주유 기록 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가") { _, _ ->
                val date = dialogBinding.inputDate.text.toString()
                val stationName = dialogBinding.inputStationName.text.toString()
                val pricePerLiter = dialogBinding.inputPricePerLiter.text.toString().toIntOrNull() ?: 0
                val totalCost = dialogBinding.inputTotalCost.text.toString().toIntOrNull() ?: 0
                val distance = dialogBinding.inputDistance.text.toString().toIntOrNull() ?: 0
                val fuelAmount = if (pricePerLiter > 0) totalCost.toDouble() / pricePerLiter else 0.0

                // 새로운 기록 추가
                val record = RecordData(
                    date,
                    stationName,
                    String.format("%.1f", fuelAmount).toDouble(), // 주유량 소수점 1자리
                    distance,
                    pricePerLiter,
                    totalCost
                )

                records.add(0, record) // 리스트의 가장 앞에 추가
                adapter.notifyItemInserted(0) // RecyclerView 갱신
                updateCumulativeData()

                // Firebase에 저장
                database.push().setValue(record)
                    .addOnSuccessListener {
                        Log.d("Firebase", "주유 기록 저장 성공")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firebase", "주유 기록 저장 실패", e)
                    }
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }
}
