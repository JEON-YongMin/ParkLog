package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val devices = mutableListOf<String>() // 데이터를 수정할 수 있는 저장 리스트
    private lateinit var dialog: AlertDialog // 대화 상자
    private lateinit var deviceListContainer: LinearLayout // 기기 목록 표시

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // 초기화
        setContentView(R.layout.activity_main)

        val deviceButton: Button = findViewById(R.id.DeviceButton)
        val parkingLocationButton: Button = findViewById(R.id.parkingLocationButton)
        val myParkingListButton: Button = findViewById(R.id.myParkingListButton)
        val carLogButton: Button = findViewById(R.id.carLogButton)

        deviceButton.setOnClickListener {
            showAddDeviceDialog()
        }

        parkingLocationButton.setOnClickListener {
            val intent = Intent(this, ParkingLocationActivity::class.java)
            startActivity(intent)
        }

        myParkingListButton.setOnClickListener {
            val intent = Intent(this, ParkingLocationList::class.java)
            startActivity(intent)
        }

        carLogButton.setOnClickListener {
            val intent = Intent(this, CarLogActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showAddDeviceDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val editText = EditText(this).apply {
            hint = "차량 블루투스 기기명 입력"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val addButton = Button(this).apply {
            text = "등록"
            setOnClickListener {
                val deviceName = editText.text.toString()
                if (deviceName.isNotBlank()) {
                    devices.add(deviceName)
                    updateDeviceListInDialog()
                    editText.text.clear()
                }
            }
        }

        inputLayout.addView(editText) // inputLayout 안에 editText 포함
        inputLayout.addView(addButton)
        dialogLayout.addView(inputLayout)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            )
        }

        deviceListContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(deviceListContainer)
        dialogLayout.addView(scrollView)

        updateDeviceListInDialog() // 초기 기기 목록

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { // layoutParams 객체의 내부 속성을 설정
                setMargins(0, 32, 0, 0)
            }
        }

        val cancelButton = Button(this).apply {
            text = "취소"
            setOnClickListener {
                dialog.dismiss()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
        }

        buttonLayout.addView(cancelButton)
        dialogLayout.addView(buttonLayout)

        dialog = AlertDialog.Builder(this)
            .setTitle("차량 블루투스 기기 등록")
            .setView(dialogLayout)
            .create()

        dialog.show()
    }

    private fun updateDeviceListInDialog() {

        deviceListContainer.removeAllViews() // 기존 목록 초기화

        for (index in devices.indices) {
            val deviceName = devices[index]

            val deviceLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val deviceTextView = TextView(this).apply {
                text = "${index + 1}. $deviceName"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val editButton = Button(this).apply {
                text = "수정"
                setOnClickListener {
                    val editDialog = AlertDialog.Builder(this@MainActivity).apply {
                        val editText = EditText(this@MainActivity).apply {
                            setText(deviceName)
                        }
                        setTitle("기기명 수정")
                        setView(editText)
                        setPositiveButton("확인") { _, _ ->
                            val newDeviceName = editText.text.toString()
                            if (newDeviceName.isNotBlank()) {
                                devices[index] = newDeviceName
                                updateDeviceListInDialog()
                            }
                        }
                        setNegativeButton("취소", null)
                    }.create()
                    editDialog.show()
                }
            }

            val deleteButton = Button(this).apply {
                text = "삭제"
                setOnClickListener {
                    devices.removeAt(index)
                    updateDeviceListInDialog()
                }
            }

            deviceLayout.addView(deviceTextView)
            deviceLayout.addView(editButton)
            deviceLayout.addView(deleteButton)
            deviceListContainer.addView(deviceLayout)
        }
    }

}
