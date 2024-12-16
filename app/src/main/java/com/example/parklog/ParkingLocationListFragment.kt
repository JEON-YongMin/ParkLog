package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.FragmentParkingLocationListBinding
import com.example.parklog.model.ParkingLocationData
import com.example.parklog.viewmodel.ParkingLocationListViewModel

class ParkingLocationListFragment : Fragment() {

    private var _binding: FragmentParkingLocationListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ParkingLocationListViewModel by viewModels()
    private lateinit var adapter: ParkingLocationAdapter

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
            onDeleteClicked = { data -> viewModel.deleteParkingLocation(data) },
            onEditClicked = { data ->
                showEditDialog(data, data.location, data.fee) // 값 전달
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // ViewModel 관찰
        viewModel.parkingList.observe(viewLifecycleOwner, Observer { list ->
            adapter.submitList(list)
        })

        viewModel.isSortedByLatest.observe(viewLifecycleOwner, Observer { isSorted ->
            binding.sortButton.contentDescription = if (isSorted) "Sort: Oldest" else "Sort: Latest"
        })

        // 정렬 버튼 클릭 이벤트
        binding.sortButton.setOnClickListener {
            viewModel.toggleSortOrder()
        }

        // 홈 버튼 클릭 이벤트
        binding.homeButton.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            requireActivity().startActivity(intent)
        }
    }

    private fun showEditDialog(item: ParkingLocationData, currentLocation: String, currentFee: String) {
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

            viewModel.updateParkingLocation(item, updatedLocation, updatedFee)
        }
        dialog.setNegativeButton("취소", null)
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
