package com.example.parklog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parklog.databinding.ListParkingBinding


class ParkingLocationAdapter(
    private val parkingList: List<ParkingLocationData> // Immutable List
) : RecyclerView.Adapter<ParkingLocationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ListParkingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ParkingLocationData) {
            binding.txtLocation.text = data.location
            binding.txtFee.text = data.fee
            Glide.with(binding.root.context)
                .load(data.photoUri)
                .into(binding.imageView3)
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
        holder.bind(parkingList[position])
    }

    override fun getItemCount(): Int = parkingList.size
}
