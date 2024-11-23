package com.example.parklog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentRecordsAdapter(private val records: List<Record>) :
    RecyclerView.Adapter<RecentRecordsAdapter.RecordViewHolder>() {

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationIcon: ImageView = itemView.findViewById(R.id.station_icon)
        val dateText: TextView = itemView.findViewById(R.id.date_text)
        val fuelText: TextView = itemView.findViewById(R.id.fuel_text)
        val distanceText: TextView = itemView.findViewById(R.id.distance_text)
        val pricePerLiterText: TextView = itemView.findViewById(R.id.price_per_liter_text)
        val totalCostText: TextView = itemView.findViewById(R.id.total_cost_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_recent_records, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]
        holder.dateText.text = record.date
        holder.fuelText.text = "주유 ${record.fuel}L"
        holder.distanceText.text = "구간 ${record.distance}km / ${record.fuelEfficiency}km/L"
        holder.pricePerLiterText.text = "${record.pricePerLiter} ₩/L"
        holder.totalCostText.text = "₩${record.totalCost}"

        // 주유소 아이콘 설정 (여기서는 기본 아이콘 사용)
        holder.stationIcon.setImageResource(R.drawable.ic_gas_station)
    }

    override fun getItemCount(): Int = records.size
}
