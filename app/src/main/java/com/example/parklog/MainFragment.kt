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
import androidx.navigation.fragment.findNavController
import com.example.parklog.databinding.FragmentMainBinding
import com.example.parklog.viewmodel.MainViewModel

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("ViewBinding is null")

    private val viewModel: MainViewModel by viewModels()

    // View 생성
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    // UI 초기화 및 ViewModel 관찰
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.connectedCar.observe(viewLifecycleOwner) { car ->
            binding.connectedCarText.text = car
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        binding.addCarButton.setOnClickListener {
            showAddCarDialog()
        }

        binding.deleteCarButton.setOnClickListener {
            viewModel.deleteCar()
        }

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
            .show()
    }

    // ViewBinding 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
