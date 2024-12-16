package com.example.parklog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parklog.databinding.ListParkingBinding

class ParkingLocationAdapter(
    private var parkingList: MutableList<ParkingLocationData>,
    private val onDeleteClicked: (Int) -> Unit,
    private val onEditClicked: (Int, String, String) -> Unit
) : RecyclerView.Adapter<ParkingLocationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ListParkingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ParkingLocationData, position: Int) {
            binding.txtLocation.text = data.location
            binding.txtFee.text = data.fee
            binding.txtTimestamp.text = data.timestamp

            Glide.with(binding.root.context)
                .load(data.photoUri)
                .into(binding.imageView3)

            binding.imageView3.setOnClickListener {
                showImageDialog(binding.root.context, data.photoUri)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClicked(position)
            }

            binding.btnEdit.setOnClickListener {
                onEditClicked(position, data.location, data.fee)
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

    fun updateData(newList: List<ParkingLocationData>) {
        parkingList.clear()
        parkingList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun showImageDialog(context: Context, imageUrl: String) {
        val dialog = AlertDialog.Builder(context).create()
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.image_zoom, null)

        val imageView: ImageView = view.findViewById(R.id.imageZoom)
        val closeButton: ImageView = view.findViewById(R.id.btnClose)

        Glide.with(context).load(imageUrl).into(imageView)
        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.setView(view)
        dialog.show()
    }
}