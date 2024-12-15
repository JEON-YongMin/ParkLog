package com.example.parklog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
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

            // 이미지 불러오기
            Glide.with(binding.root.context)
                .load(data.photoUri)
                .into(binding.imageView3)

            // 이미지 클릭 이벤트: Dialog로 확대 표시
            binding.imageView3.setOnClickListener {
                showImageDialog(binding.root.context, data.photoUri)
            }

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

    // Dialog를 사용해 이미지 확대 표시
    private fun showImageDialog(context: Context, imageUrl: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.image_zoom) // 확대 이미지 레이아웃
        val imageView: ImageView = dialog.findViewById(R.id.imageZoom)
        val closeButton: ImageView = dialog.findViewById(R.id.btnClose)

        // Glide로 이미지 불러오기
        Glide.with(context)
            .load(imageUrl)
            .into(imageView)

        // 닫기 버튼 클릭 시 Dialog 닫기
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
