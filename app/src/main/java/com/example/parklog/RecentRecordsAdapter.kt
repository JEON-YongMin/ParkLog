package com.example.parklog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentRecordsAdapter(private val records: MutableList<Record>) :
    RecyclerView.Adapter<RecentRecordsAdapter.RecordViewHolder>() {

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationIcon: ImageView = itemView.findViewById(R.id.station_icon)
        val dateText: TextView = itemView.findViewById(R.id.date_text)
        val stationNameText: TextView = itemView.findViewById(R.id.station_name_text)
        val distanceTextCenter: TextView = itemView.findViewById(R.id.distance_text_center)
        val distanceTextEnd: TextView = itemView.findViewById(R.id.distance_text_end)
        val fuelText: TextView = itemView.findViewById(R.id.fuel_text)
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
        holder.stationNameText.text = record.stationName

        if (record.stationName == "주행 기록") {
            // 주행 기록: 거리를 오른쪽에 표시
            holder.distanceTextEnd.visibility = View.VISIBLE
            holder.distanceTextEnd.text = "구간 ${record.distance}km"

            // 주유 기록 관련 뷰 숨김
            holder.distanceTextCenter.visibility = View.GONE
            holder.fuelText.visibility = View.GONE
            holder.pricePerLiterText.visibility = View.GONE
            holder.totalCostText.visibility = View.GONE
        } else {
            // 주유 기록: 거리를 주유소 이름 아래에 표시
            holder.distanceTextEnd.visibility = View.GONE

            holder.distanceTextCenter.visibility = View.VISIBLE
            holder.distanceTextCenter.text = "구간 ${record.distance}km"

            // 주유량
            holder.fuelText.visibility = View.VISIBLE
            holder.fuelText.text = "주유 ${record.fuel}L"

            // 1L당 가격
            holder.pricePerLiterText.visibility = View.VISIBLE
            holder.pricePerLiterText.text = "${record.pricePerLiter} ₩/L"

            // 총 비용
            holder.totalCostText.visibility = View.VISIBLE
            holder.totalCostText.text = "₩${record.totalCost}"
        }
    }

    override fun getItemCount(): Int = records.size

    /**
     * 새로운 기록을 추가하고 최신 기록이 상단에 뜨도록 갱신
     */
    fun addRecord(record: Record) {
        records.add(0, record) // 리스트의 가장 앞에 기록 추가
        notifyItemInserted(0) // 첫 번째 위치에 아이템 추가 알림
    }
}
