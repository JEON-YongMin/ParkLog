package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.FragmentParkingLocationListBinding
import com.google.firebase.database.*

class ParkingLocationListFragment : Fragment() {

    private var _binding: FragmentParkingLocationListBinding? = null
    private val binding get() = _binding!!

    private val parkingList = mutableListOf<ParkingLocationData>()
    private lateinit var adapter: ParkingLocationAdapter
    private val database = FirebaseDatabase.getInstance()
    private val dbRef = database.reference.child("parking_locations")
    private var isSortedByLatest = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentParkingLocationListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        adapter = ParkingLocationAdapter(
            parkingList,
            onDeleteClicked = { position -> deleteParkingLocation(position) },
            onEditClicked = { position, location, fee -> showEditDialog(position, location, fee) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Firebase 데이터 가져오기
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                parkingList.clear()
                for (data in snapshot.children) {
                    val item = data.getValue(ParkingLocationData::class.java)
                    item?.let { parkingList.add(it) }
                }
                sortList()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })

        // 정렬 버튼 클릭 이벤트
        binding.sortButton.setOnClickListener {
            isSortedByLatest = !isSortedByLatest
            sortList()
        }

        // 홈 버튼 클릭 이벤트
        binding.homeButton.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            requireActivity().startActivity(intent)
        }
    }

    private fun showEditDialog(position: Int, currentLocation: String, currentFee: String) {
        val dialog = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_parking, null)

        val editLocation = view.findViewById<EditText>(R.id.editLocation)
        val editFee = view.findViewById<EditText>(R.id.editFee)

        editLocation.setText(currentLocation)
        editFee.setText(currentFee)

        dialog.setView(view)
        dialog.setPositiveButton("수정") { _, _ ->
            val updatedLocation = editLocation.text.toString()
            val updatedFee = editFee.text.toString()

            updateParkingLocation(position, updatedLocation, updatedFee)
        }
        dialog.setNegativeButton("취소", null)
        dialog.show()
    }

    private fun updateParkingLocation(position: Int, location: String, fee: String) {
        val item = parkingList[position]
        val updates = mapOf("location" to location, "fee" to fee)

        dbRef.orderByChild("photoUri").equalTo(item.photoUri)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.updateChildren(updates)
                    }
                    parkingList[position].location = location
                    parkingList[position].fee = fee
                    adapter.notifyItemChanged(position)
                    Toast.makeText(requireContext(), "수정 완료", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "수정 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun deleteParkingLocation(position: Int) {
        val item = parkingList[position]
        dbRef.orderByChild("photoUri").equalTo(item.photoUri)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        data.ref.removeValue()
                    }
                    parkingList.removeAt(position)
                    adapter.notifyItemRemoved(position)
                    Toast.makeText(requireContext(), "삭제 완료", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "삭제 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun sortList() {
        if (isSortedByLatest)
            parkingList.sortByDescending { it.timestamp }
        else
            parkingList.sortBy { it.timestamp }

        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
