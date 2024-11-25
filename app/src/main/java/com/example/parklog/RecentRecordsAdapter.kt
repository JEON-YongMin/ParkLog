package com.example.parklog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parklog.databinding.AdapterRecentRecordsBinding

class RecentRecordsAdapter(private val recordList: MutableList<RecordData>) :
    RecyclerView.Adapter<RecentRecordsAdapter.RecordViewHolder>() {

    // ViewHolder 클래스: View Binding을 사용하여 레이아웃의 뷰에 접근
    class RecordViewHolder(private val binding: AdapterRecentRecordsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recordData: RecordData) {
            binding.dateText.text = recordData.date

            if (recordData.stationName == "주행 기록") {
                // 주행 기록: 날짜 왼쪽, 거리 오른쪽
                binding.stationIcon.visibility = View.GONE
                binding.stationNameText.visibility = View.GONE
                binding.fuelText.visibility = View.GONE
                binding.pricePerLiterText.visibility = View.GONE
                binding.totalCostText.visibility = View.GONE

                binding.distanceTextCenter.visibility = View.GONE
                binding.distanceTextEnd.visibility = View.VISIBLE
                binding.distanceTextEnd.text = "구간 ${recordData.distance}km"
            } else {
                // 주유 기록: 기존 레이아웃 유지
                binding.stationIcon.visibility = View.VISIBLE
                binding.stationNameText.visibility = View.VISIBLE
                binding.fuelText.visibility = View.VISIBLE
                binding.pricePerLiterText.visibility = View.VISIBLE
                binding.totalCostText.visibility = View.VISIBLE

                binding.stationNameText.text = recordData.stationName
                binding.distanceTextCenter.visibility = View.VISIBLE
                binding.distanceTextCenter.text = "구간 ${recordData.distance}km"
                binding.distanceTextEnd.visibility = View.GONE

                binding.fuelText.text = "주유 ${recordData.fuelAmount}L"
                binding.pricePerLiterText.text = "${recordData.pricePerLiter} ₩/L"
                binding.totalCostText.text = "₩${recordData.totalCost}"
            }
        }
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val binding = AdapterRecentRecordsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecordViewHolder(binding)
    }

    // ViewHolder 데이터 바인딩
    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = recordList[position]
        holder.bind(record)
    }

    // RecyclerView 아이템 개수 반환
    override fun getItemCount(): Int = recordList.size
}
