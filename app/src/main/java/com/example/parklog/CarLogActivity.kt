package com.example.parklog

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.AddDistanceBinding
import com.example.parklog.databinding.AddFuelBinding
import com.example.parklog.databinding.ActivityCarLogBinding

class CarLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarLogBinding
    private val records = mutableListOf<Record>() // 기록 리스트
    private lateinit var adapter: RecentRecordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityCarLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView 초기화
        adapter = RecentRecordsAdapter(records)
        binding.recyclerRecentRecords.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentRecords.adapter = adapter

        // 누적 데이터 업데이트
        updateCumulativeData()

        // 버튼 클릭 이벤트
        binding.btnAddMileage.setOnClickListener {
            showAddDistanceDialog()
        }

        binding.btnAddFuel.setOnClickListener {
            showAddFuelDialog()
        }
    }

    /**
     * 누적 주행 거리와 주유 비용을 계산하여 UI를 업데이트
     */
    private fun updateCumulativeData() {
        val totalMileage = records.sumOf { it.distance }
        val totalFuelCost = records.sumOf { it.totalCost }

        binding.totalMileageValue.text = "$totalMileage km"
        binding.totalFuelCostValue.text = "₩$totalFuelCost"
    }

    /**
     * 주행 기록 추가 Dialog
     */
    private fun showAddDistanceDialog() {
        val dialogBinding = AddDistanceBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setTitle("주행 기록 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가") { _, _ ->
                val date = dialogBinding.inputDate.text.toString()
                val distance = dialogBinding.inputDistance.text.toString().toIntOrNull() ?: 0

                // 새로운 기록 추가
                val record = Record(date, "주행 기록", 0.0, distance, 0, 0)
                records.add(0, record) // 리스트의 가장 앞에 추가
                adapter.notifyItemInserted(0) // RecyclerView 갱신
                updateCumulativeData()
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    /**
     * 주유 기록 추가 Dialog
     */
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
                val record = Record(
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
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }
}
