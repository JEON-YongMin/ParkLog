package com.example.parklog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parklog.databinding.ListParkingBinding

class ParkingLocationAdapter(
    private val onDeleteClicked: (ParkingLocationData) -> Unit,
    private val onEditClicked: (ParkingLocationData) -> Unit
) : ListAdapter<ParkingLocationData, ParkingLocationAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ParkingLocationData>() {
            override fun areItemsTheSame(oldItem: ParkingLocationData, newItem: ParkingLocationData): Boolean {
                return oldItem.photoUri == newItem.photoUri
            }

            override fun areContentsTheSame(oldItem: ParkingLocationData, newItem: ParkingLocationData): Boolean {
                return oldItem == newItem
            }
        }
    }

    inner class ViewHolder(private val binding: ListParkingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: ParkingLocationData) {
            binding.txtLocation.text = data.location
            binding.txtFee.text = data.fee
            binding.txtTimestamp.text = data.timestamp

            Glide.with(binding.root.context)
                .load(data.photoUri)
                .into(binding.imageView3)

            // 이미지 확대
            binding.imageView3.setOnClickListener {
                showImageDialog(binding.root.context, data.photoUri)
            }

            // 삭제 버튼
            binding.btnDelete.setOnClickListener {
                onDeleteClicked(data)
            }

            // 수정 버튼
            binding.btnEdit.setOnClickListener {
                onEditClicked(data)
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
        holder.bind(getItem(position)) // getItem 사용
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
