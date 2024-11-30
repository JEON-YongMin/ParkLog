package com.example.parklog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.parklog.databinding.ListParkingBinding

// ParkingLocationAdapter: RecyclerView 어댑터 정의
class ParkingLocationAdapter(
    private val parkingList: List<ParkingLocationData>
) : RecyclerView.Adapter<ParkingLocationAdapter.ViewHolder>() {

    // ViewHolder 클래스: RecyclerView 아이템의 뷰를 관리
    inner class ViewHolder(val binding: ListParkingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ParkingLocationData) {

            // 데이터 바인딩: 주차 위치와 요금을 텍스트뷰에 설정
            binding.txtLocation.text = data.location
            binding.txtFee.text = data.fee

            Glide.with(binding.root.context)
                .load(data.photoUri)
                .into(binding.imageView3)
        }
    }

    // ViewHolder 생성 시 호출되는 메서드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ListParkingBinding.inflate(
            LayoutInflater.from(parent.context), // 부모 컨텍스트에서 LayoutInflater 생성
            parent,
            false // parent에 바로 추가하지 않음 : Recycler View가 뷰를 직접관리
        )
        return ViewHolder(binding) // 생성된 ViewHolder 반환
    }

    // ViewHolder에 데이터를 바인딩할 때 호출되는 메서드
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(parkingList[position]) // 해당 위치의 데이터 바인딩
    }

    // RecyclerView의 아이템 수를 반환
    override fun getItemCount(): Int = parkingList.size
}
