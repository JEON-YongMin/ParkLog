package com.example.parklog

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.AddMileageBinding
import com.example.parklog.databinding.AddFuelBinding
import com.example.parklog.databinding.ActivityCarLogBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class CarLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarLogBinding
    private lateinit var adapter: RecentRecordsAdapter
    private lateinit var database: DatabaseReference // Firebase Realtime Database 참조
    private val records = mutableListOf<RecordData>() // 등록된 차량 목록

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityCarLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView 초기화
        adapter = RecentRecordsAdapter(records)
        binding.recyclerRecentRecords.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentRecords.adapter = adapter

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance().reference.child("CarRecords")

        // Firebase에서 초기 데이터 가져오기
        fetchRecordsFromRealtimeDatabase()

        // 누적 데이터 업데이트
        updateCumulativeData()

        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.btnAddMileage.setOnClickListener {
            showAddDistanceDialog()
        }

        binding.btnAddFuel.setOnClickListener {
            showAddFuelDialog()
        }
    }
    
    // 누적 주행 거리와 누적 주유 비용을 계산하여 UI 업데이트
    private fun updateCumulativeData() {
        var totalMileage = 0
        var totalFuelCost = 0

        for (record in records) {
            totalMileage += record.distance
            totalFuelCost += record.totalCost
        }

        binding.totalMileageValue.text = "$totalMileage km"
        binding.totalFuelCostValue.text = "₩$totalFuelCost"
    }

    // 주행 기록 추가 Dialog
    private fun showAddDistanceDialog() {
        val dialogBinding = AddMileageBinding.inflate(LayoutInflater.from(this))

        // AlertDialog 생성
        val dialog = AlertDialog.Builder(this)
            .setTitle("주행 기록 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가") { _, _ ->
                val date = dialogBinding.inputDate.text.toString()
                val distance = dialogBinding.inputDistance.text.toString().toIntOrNull() ?: 0

                // 새로운 기록 추가
                val record = RecordData(date, "주행 기록", distance,0,0.0, 0)
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

        // AlertDialog 생성
        val dialog = AlertDialog.Builder(this)
            .setTitle("주유 기록 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가") { _, _ ->
                val date = dialogBinding.inputDate.text.toString()
                val stationName = dialogBinding.inputStationName.text.toString()
                val distance = dialogBinding.inputDistance.text.toString().toIntOrNull() ?: 0
                val pricePerLiter = dialogBinding.inputPricePerLiter.text.toString().toIntOrNull() ?: 0
                val totalCost = dialogBinding.inputTotalCost.text.toString().toIntOrNull() ?: 0
                val fuelAmount = if (pricePerLiter > 0) totalCost.toDouble() / pricePerLiter else 0.0

                // 새로운 기록 추가
                val record = RecordData(
                    date,
                    stationName,
                    distance,
                    pricePerLiter,
                    String.format("%.1f", fuelAmount).toDouble(), // 주유량 소수점 1자리
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

    // Firebase Realtime Database에서 기록 데이터 가져오기
    private fun fetchRecordsFromRealtimeDatabase() {
        database.get()

            // 성공적으로 데이터 가져왔을 때
            .addOnSuccessListener { snapshot ->
                records.clear()
                snapshot.children.forEach { child ->
                    child.getValue(RecordData::class.java)?.let { records.add(it) } // 변환 후 리스트에 추가
                }

                // 데이터 정렬: 최신 순으로 timestamp 기준 내림차순 정렬
                records.sortByDescending { it.timestamp }

                // RecyclerView 갱신
                adapter.notifyDataSetChanged()

                // 누적 데이터 업데이트
                updateCumulativeData()
            }

            // 성공적으로 데이터 가져오지 못했을 때
            .addOnFailureListener { e ->
                Log.w("Firebase", "데이터 가져오기 실패", e)
            }
    }

}

