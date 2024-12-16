package com.example.parklog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.appcompat.app.AlertDialog
import com.example.parklog.databinding.FragmentMainBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var dialog: AlertDialog
    private lateinit var database: DatabaseReference
    private val cars = mutableMapOf<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance().reference
        loadCarsFromDatabase()

        // 차량 추가 버튼 이벤트
        binding.addCarButton.setOnClickListener {
            showAddCarDialog()
        }

        // 차량 삭제 버튼 이벤트
        binding.deleteCarButton.setOnClickListener {
            showDeleteCarDialog()
        }

        // 네비게이션 버튼 이벤트
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

    // 차량 추가 다이얼로그
    private fun showAddCarDialog() {
        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val carModelEditText = EditText(requireContext()).apply { hint = "차종 입력" }
        val carNumberEditText = EditText(requireContext()).apply { hint = "차 번호 입력" }

        dialogLayout.addView(carModelEditText)
        dialogLayout.addView(carNumberEditText)

        dialog = AlertDialog.Builder(requireContext())
            .setTitle("차량 등록")
            .setView(dialogLayout)
            .setPositiveButton("등록") { _, _ ->
                val carModel = carModelEditText.text.toString()
                val carNumber = carNumberEditText.text.toString()

                if (carModel.isNotBlank() && carNumber.isNotBlank()) {
                    val newCar = "$carModel : $carNumber"
                    val newCarKey = database.child("cars").push().key

                    if (newCarKey != null) {
                        database.child("cars").child(newCarKey).setValue(newCar)
                            .addOnSuccessListener {
                                cars[newCarKey] = newCar
                                updateConnectedCarText(newCar)
                                Toast.makeText(requireContext(), "차량이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "차량 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "차종과 차 번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    // 차량 삭제 다이얼로그
    private fun showDeleteCarDialog() {
        if (cars.isNotEmpty()) {
            val lastCarKey = cars.entries.last().key
            database.child("cars").child(lastCarKey).removeValue()
                .addOnSuccessListener {
                    cars.remove(lastCarKey)
                    updateConnectedCarText("현재 연결된 차량 없음")
                    Toast.makeText(requireContext(), "차량이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "차량 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "삭제할 차량이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 현재 연결된 차량 텍스트 업데이트
    private fun updateConnectedCarText(car: String) {
        binding.connectedCarText.text = car
    }

    // Firebase에서 차량 데이터 불러오기
    private fun loadCarsFromDatabase() {
        database.child("cars").get()
            .addOnSuccessListener { snapshot ->
                cars.clear()
                for (child in snapshot.children) {
                    val car = child.getValue(String::class.java)
                    val key = child.key
                    if (car != null && key != null) {
                        cars[key] = car
                    }
                }
                if (cars.isNotEmpty()) {
                    updateConnectedCarText(cars.values.last())
                } else {
                    updateConnectedCarText("현재 연결된 차량 없음")
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
