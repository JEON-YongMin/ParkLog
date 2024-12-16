package com.example.parklog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.parklog.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // LiveData Observer
        viewModel.connectedCar.observe(viewLifecycleOwner, Observer { car ->
            binding.connectedCarText.text = car
        })

        viewModel.toastMessage.observe(viewLifecycleOwner, Observer { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        })

        // 차량 추가 버튼
        binding.addCarButton.setOnClickListener {
            showAddCarDialog()
        }

        // 차량 삭제 버튼
        binding.deleteCarButton.setOnClickListener {
            viewModel.deleteCar()
        }

        // 네비게이션 버튼
        binding.parkingLocationButton.setOnClickListener {
            findNavController().navigate(R.id.parkingLocationFragment)
        }
        binding.myParkingListButton.setOnClickListener {
            findNavController().navigate(R.id.parkingLocationListFragment)
        }
        binding.carLogButton.setOnClickListener {
            findNavController().navigate(R.id.carLogFragment)
        }
    }

    private fun showAddCarDialog() {
        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val carModelEditText = EditText(requireContext()).apply { hint = "차종 입력" }
        val carNumberEditText = EditText(requireContext()).apply { hint = "차 번호 입력" }

        dialogLayout.addView(carModelEditText)
        dialogLayout.addView(carNumberEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("차량 등록")
            .setView(dialogLayout)
            .setPositiveButton("등록") { _, _ ->
                val carModel = carModelEditText.text.toString()
                val carNumber = carNumberEditText.text.toString()
                viewModel.addCar(carModel, carNumber)
            }
            .setNegativeButton("취소", null)
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
