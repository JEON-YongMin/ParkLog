package com.example.parklog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parklog.databinding.AdapterRecentRecordsBinding

// RecyclerView.Adapter를 상속받아(RecyclerView와 데이터 간의 연결을 통해) 데이터 표시
class RecentRecordsAdapter(private val recordList: MutableList<RecordData>) :
    RecyclerView.Adapter<RecentRecordsAdapter.RecordViewHolder>() {

    // 새로운 Adapter 클래스 정의(RecyclerView의 데이터와 UI를 연결하는 역할)
    class RecordViewHolder(private val binding: AdapterRecentRecordsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recordData: RecordData) {
            binding.dateText.text = recordData.date
                if (recordData.stationName == "주행 기록") {
                    binding.stationIcon.visibility = View.GONE
                    binding.stationNameText.visibility = View.GONE
                    binding.pricePerLiterText.visibility = View.GONE
                    binding.fuelText.visibility = View.GONE
                    binding.totalCostText.visibility = View.GONE

                    binding.distanceTextCenter.visibility = View.GONE
                    binding.distanceTextEnd.visibility = View.VISIBLE
                    binding.distanceTextEnd.text = "구간 ${recordData.distance}km"
                } else {
                    binding.stationIcon.visibility = View.VISIBLE
                    binding.stationNameText.visibility = View.VISIBLE
                    binding.pricePerLiterText.visibility = View.VISIBLE
                    binding.fuelText.visibility = View.VISIBLE
                    binding.totalCostText.visibility = View.VISIBLE

                    binding.stationNameText.text = recordData.stationName
                    binding.distanceTextCenter.visibility = View.VISIBLE
                    binding.distanceTextCenter.text = "구간 ${recordData.distance}km"
                    binding.distanceTextEnd.visibility = View.GONE

                    binding.pricePerLiterText.text = "${recordData.pricePerLiter} ₩/L"
                    binding.fuelText.text = "주유 ${recordData.fuelAmount}L"
                    binding.totalCostText.text = "₩${recordData.totalCost}"
                }
        }
    }

    // RecyclerView.Adapter를 상속 -> 1. ViewHolder 생성(새로운 화면에 표시할 각 항목의 뷰 생성)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = AdapterRecentRecordsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordViewHolder(binding)
    }

    // RecyclerView.Adapter를 상속 -> 2. ViewHolder에 데이터 바인딩(연결)
    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = recordList[position]
        holder.bind(record)
    }

    // RecyclerView.Adapter를 상속 -> 3.RecyclerView에 표시할 데이터 개수 반환
    override fun getItemCount(): Int = recordList.size
}
