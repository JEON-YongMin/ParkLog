package com.example.parklog

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val devices = mutableListOf<String>() // 기기명 목록
    private lateinit var dialog: AlertDialog // 다이얼로그 변수
    private lateinit var deviceListContainer: LinearLayout // 기기 목록 컨테이너 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val deviceButton: Button = findViewById(R.id.DeviceButton)

        // 등록 버튼 클릭 시 팝업 창 표시
        deviceButton.setOnClickListener {
            showAddDeviceDialog()
        }
    }

    private fun showAddDeviceDialog() {
        // 다이얼로그 레이아웃을 포함하는 LinearLayout 생성
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        // 입력 창을 위한 EditText와 등록 버튼을 포함하는 Horizontal LinearLayout 생성
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
                    updateDeviceListInDialog() // 목록 갱신
                    editText.text.clear()
                }
            }
        }

        inputLayout.addView(editText)
        inputLayout.addView(addButton)
        dialogLayout.addView(inputLayout)

        // ScrollView로 기기 목록을 감싸기
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // 원하는 높이로 설정 (필요에 따라 조정 가능)
            )
        }

        // 기기 목록을 표시할 LinearLayout 컨테이너 추가
        deviceListContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scrollView.addView(deviceListContainer)
        dialogLayout.addView(scrollView)

        // 초기 기기 목록을 다이얼로그에 표시
        updateDeviceListInDialog()

        // 하단에 취소 버튼만 포함하는 버튼 레이아웃 추가
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 32, 0, 0)
            }
        }

        val cancelButton = Button(this).apply {
            text = "취소"
            setOnClickListener {
                // 다이얼로그 닫기
                dialog.dismiss()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
        }

        // 버튼 레이아웃에 취소 버튼만 추가하고, 오른쪽 아래로 배치
        buttonLayout.addView(cancelButton)
        dialogLayout.addView(buttonLayout)

        // AlertDialog 생성 및 설정
        dialog = AlertDialog.Builder(this)
            .setTitle("차량 블루투스 기기 등록")
            .setView(dialogLayout)
            .create()

        dialog.show()
    }

    // 기기 목록을 다이얼로그의 deviceListContainer에 업데이트하는 함수
    private fun updateDeviceListInDialog() {
        // 기존 목록 초기화
        deviceListContainer.removeAllViews()

        // 각 기기명에 대해 순번과 자동차 이모티콘을 포함하여 텍스트뷰 생성 및 수정/삭제 버튼 추가
        devices.forEachIndexed { index, deviceName ->
            // 기기명을 담을 수평 레이아웃 생성
            val deviceLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            // 자동차 이모티콘과 기기명을 포함한 텍스트뷰
            val deviceTextView = TextView(this).apply {
                text = "${index + 1}. $deviceName"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            // 수정 버튼
            val editButton = Button(this).apply {
                text = "수정"
                setOnClickListener {
                    // 수정 로직 (텍스트를 입력받아 변경)
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
                                updateDeviceListInDialog() // 목록 갱신
                            }
                        }
                        setNegativeButton("취소", null)
                    }.create()
                    editDialog.show()
                }
            }

            // 삭제 버튼
            val deleteButton = Button(this).apply {
                text = "삭제"
                setOnClickListener {
                    devices.removeAt(index) // 목록에서 삭제
                    updateDeviceListInDialog() // 목록 갱신
                }
            }

            // 레이아웃에 텍스트뷰와 버튼 추가
            deviceLayout.addView(deviceTextView)
            deviceLayout.addView(editButton)
            deviceLayout.addView(deleteButton)

            // 컨테이너에 추가
            deviceListContainer.addView(deviceLayout)
        }
    }
}
