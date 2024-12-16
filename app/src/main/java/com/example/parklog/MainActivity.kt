package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.parklog.databinding.ActivityMainBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dialog: AlertDialog
    private lateinit var database: DatabaseReference
    private val cars = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance().reference

        // Firebase에서 기존 데이터 불러오기
        loadCarsFromDatabase()

        // 차량 추가 버튼 이벤트
        binding.addCarButton.setOnClickListener {
            showAddCarDialog()
        }

        // 삭제 다이얼로그 표시
        binding.deleteCarButton.setOnClickListener {
            showDeleteCarDialog()
        }

        // 다른 버튼 클릭 이벤트
        binding.parkingLocationButton.setOnClickListener {
            startActivity(Intent(this, ParkingLocationActivity::class.java))
        }
        binding.myParkingListButton.setOnClickListener {
            startActivity(Intent(this, ParkingLocationList::class.java))
        }
        binding.carLogButton.setOnClickListener {
            startActivity(Intent(this, CarLogActivity::class.java))
        }
    }

    // 차량 추가 다이얼로그
    private fun showAddCarDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val carModelEditText = EditText(this).apply { hint = "차종 입력" }
        val carNumberEditText = EditText(this).apply { hint = "차 번호 입력" }

        dialogLayout.addView(carModelEditText)
        dialogLayout.addView(carNumberEditText)

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.END
            setPadding(0, 16, 0, 0)
        }

        val addButton = Button(this).apply {
            text = "등록"
            setOnClickListener {
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
                                Toast.makeText(this@MainActivity, "차량이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@MainActivity, "차량 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "차종과 차 번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val cancelButton = Button(this).apply {
            text = "취소"
            setOnClickListener { dialog.dismiss() }
        }

        buttonLayout.addView(addButton)
        buttonLayout.addView(cancelButton)
        dialogLayout.addView(buttonLayout)

        dialog = AlertDialog.Builder(this)
            .setTitle("차량 등록")
            .setView(dialogLayout)
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
                    Toast.makeText(this, "차량이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "차량 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "삭제할 차량이 없습니다.", Toast.LENGTH_SHORT).show()
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
}
