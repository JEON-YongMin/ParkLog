package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

class MainActivity : AppCompatActivity() {

    private val cars = mutableMapOf<String, String>() // 등록된 차량 목록
    private lateinit var dialog: AlertDialog // 대화 상자
    private lateinit var database: DatabaseReference // Firebase Realtime Database 참조
    private lateinit var connectedCarButton: Button // 현재 연결된 차량 버튼

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance().reference

        // Firebase에서 기존 데이터를 불러오기
        loadCarsFromDatabase()

        // UI 요소 초기화
        connectedCarButton = findViewById(R.id.connectedCarButton)
        val addCarButton: Button = findViewById(R.id.addCarButton) // + 버튼

        val parkingLocationButton: Button = findViewById(R.id.parkingLocationButton)
        val myParkingListButton: Button = findViewById(R.id.myParkingListButton)
        val carLogButton: Button = findViewById(R.id.carLogButton)

        // 현재 연결된 차량 버튼 클릭 시 등록된 차량 목록 표시
        connectedCarButton.setOnClickListener {
            showCarListDialog()
        }

        // + 버튼 클릭 시 차량 등록 다이얼로그 표시
        addCarButton.setOnClickListener {
            showAddCarDialog()
        }

        // 주차 위치 저장 버튼
        parkingLocationButton.setOnClickListener {
            val intent = Intent(this, ParkingLocationActivity::class.java)
            startActivity(intent)
        }

        // 내 주차 목록 버튼
        myParkingListButton.setOnClickListener {
            val intent = Intent(this, ParkingLocationList::class.java)
            startActivity(intent)
        }

        // 차계부 버튼
        carLogButton.setOnClickListener {
            val intent = Intent(this, CarLogActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showAddCarDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val carModelEditText = EditText(this).apply {
            hint = "차종 입력"
        }

        val carNumberEditText = EditText(this).apply {
            hint = "차 번호 입력"
        }

        dialogLayout.addView(carModelEditText)
        dialogLayout.addView(carNumberEditText)

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.END
            setPadding(0, 16, 0, 0)
        }

        val cancelButton = Button(this).apply {
            text = "취소"
            setOnClickListener {
                dialog.dismiss()
            }
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
                                cars[newCarKey] = newCar // 로컬 목록 업데이트
                                updateConnectedCarButton(newCar) // UI 업데이트
                                Toast.makeText(this@MainActivity, "차량이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@MainActivity, "차량 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this@MainActivity, "Firebase 키 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "차종과 차 번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonLayout.addView(cancelButton)
        buttonLayout.addView(addButton)
        dialogLayout.addView(buttonLayout)

        dialog = AlertDialog.Builder(this)
            .setTitle("차량 등록")
            .setView(dialogLayout)
            .create()

        dialog.show()
    }

    private fun showCarListDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        for ((key, car) in cars) {
            val carLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val carButton = Button(this).apply {
                text = car
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // Equal weight to fill remaining space
                )
                setOnClickListener {
                    updateConnectedCarButton(car)
                    dialog.dismiss()
                }
            }

            val deleteButton = Button(this).apply {
                text = "삭제"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    deleteCarFromDatabase(key)
                    cars.remove(key)
                    listContainer.removeView(carLayout)
                    Toast.makeText(this@MainActivity, "차량이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            carLayout.addView(carButton)
            carLayout.addView(deleteButton)
            listContainer.addView(carLayout)
        }

        scrollView.addView(listContainer)

        val closeButton = Button(this).apply {
            text = "닫기"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                dialog.dismiss()
            }
        }

        dialogLayout.addView(scrollView)
        dialogLayout.addView(closeButton)

        dialog = AlertDialog.Builder(this)
            .setTitle("등록된 차량 목록")
            .setView(dialogLayout)
            .create()

        dialog.show()
    }

    private fun updateConnectedCarButton(car: String) {
        connectedCarButton.text = car
    }

    private fun loadCarsFromDatabase() {
        database.child("cars").get()
            .addOnSuccessListener { snapshot ->
                cars.clear()
                for (child in snapshot.children) {
                    val car = child.getValue(String::class.java)
                    car?.let { cars[child.key!!] = it }
                }
                if (cars.isNotEmpty()) {
                    updateConnectedCarButton(cars.values.last())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "차량 데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteCarFromDatabase(carKey: String) {
        database.child("cars").child(carKey).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "차량이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "차량 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}
