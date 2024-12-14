package com.example.parklog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parklog.databinding.ListParkingBinding

class ParkingLocationAdapter(
    private val parkingList: MutableList<ParkingLocationData>,
    private val onDeleteClicked: (Int) -> Unit
) : RecyclerView.Adapter<ParkingLocationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ListParkingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ParkingLocationData, position: Int) {
            binding.txtLocation.text = data.location
            binding.txtFee.text = data.fee
            binding.txtTimestamp.text = data.timestamp // 저장시간 표시

            Glide.with(binding.root.context)
                .load(data.photoUri)
                .into(binding.imageView3)

            binding.btnDelete.setOnClickListener {
                onDeleteClicked(position)
            }

            binding.btnEdit.setOnClickListener {
                // TODO: 수정 기능 추가
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListParkingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(parkingList[position], position)
    }

    override fun getItemCount(): Int = parkingList.size
}
