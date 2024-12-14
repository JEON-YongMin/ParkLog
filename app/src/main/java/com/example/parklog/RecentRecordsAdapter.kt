package com.example.parklog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parklog.databinding.MileageRecordBinding
import com.example.parklog.databinding.FuelRecordBinding

class RecentRecordsAdapter(private val recordList: MutableList<RecordData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_MILEAGE = 0
        private const val TYPE_FUEL = 1
    }

    // 주행 기록 ViewHolder
    class MileageViewHolder(private val binding: MileageRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: RecordData) {
            binding.dateText.text = record.date
            binding.startLocationText.text = "출발 위치: ${record.startLocation}"
            binding.endLocationText.text = "도착 위치: ${record.endLocation}"
            binding.mileageText.text = "주행 거리: ${record.distance} km"
        }
    }

    // 주유 기록 ViewHolder
    class FuelViewHolder(private val binding: FuelRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: RecordData) {
            binding.dateText.text = record.date
            binding.stationNameText.text = "주유소: ${record.stationName}"
            binding.distanceText.text = "주행 거리: ${record.distance} km"
            binding.pricePerLiterText.text = "가격: ${record.pricePerLiter} ₩/L"
            binding.fuelAmountText.text = "주유량: ${String.format("%.2f", record.fuelAmount)} L"
            binding.totalCostText.text = "총 비용: ₩${record.totalCost}"
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (recordList[position].stationName.isNullOrEmpty()) TYPE_MILEAGE else TYPE_FUEL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_MILEAGE) {
            val binding = MileageRecordBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            MileageViewHolder(binding)
        } else {
            val binding = FuelRecordBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            FuelViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val record = recordList[position]
        if (holder is MileageViewHolder) {
            holder.bind(record)
        } else if (holder is FuelViewHolder) {
            holder.bind(record)
        }
    }

    override fun getItemCount(): Int = recordList.size
}
