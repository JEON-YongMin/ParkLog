package com.example.parklog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.parklog.databinding.MileageRecordBinding
import com.example.parklog.databinding.FuelRecordBinding
import com.example.parklog.model.RecordData

class RecentRecordsAdapter(private var recordList: MutableList<RecordData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_MILEAGE = 0
        private const val TYPE_FUEL = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_MILEAGE) {
            MileageViewHolder(MileageRecordBinding.inflate(inflater, parent, false))
        } else {
            FuelViewHolder(FuelRecordBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val record = recordList[position]
        when (holder) {
            is MileageViewHolder -> holder.bind(record)
            is FuelViewHolder -> holder.bind(record)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (recordList[position].stationName.isNullOrEmpty()) TYPE_MILEAGE else TYPE_FUEL
    }

    override fun getItemCount(): Int = recordList.size

    fun updateRecords(newRecords: List<RecordData>) {
        recordList.clear()
        recordList.addAll(newRecords)
        notifyDataSetChanged()
    }

    // ViewHolder for mileage records
    class MileageViewHolder(private val binding: MileageRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: RecordData) {
            binding.date.text = record.date
            binding.startLocation.text = "출발 위치: ${record.startLocation}"
            binding.endLocation.text = "도착 위치: ${record.endLocation}"
            binding.mileage.text = "주행 거리: ${record.distance} km"
        }
    }

    // ViewHolder for fuel records
    class FuelViewHolder(private val binding: FuelRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(record: RecordData) {
            binding.date.text = record.date
            binding.stationName.text = "주유소: ${record.stationName}"
            binding.pricePerLiter.text = "가격: ${record.pricePerLiter} ₩/L"
            binding.distance.text = "주행 거리: ${record.distance} km"
            binding.totalCost.text = "총 비용: ₩${record.totalCost}"
        }
    }
}
