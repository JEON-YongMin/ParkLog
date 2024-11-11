package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CarLogActivity : AppCompatActivity() {

    private lateinit var etTotalTime: EditText
    private lateinit var etTotalDistance: EditText
    private lateinit var etFuelCost: EditText
    private lateinit var btnSaveCarLog: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_log)

        val homeButton: ImageButton = findViewById(R.id.homeButton)

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // EditText와 Button 초기화
        etTotalTime = findViewById(R.id.etTotalTime)
        etTotalDistance = findViewById(R.id.etTotalDistance)
        etFuelCost = findViewById(R.id.etFuelCost)
        btnSaveCarLog = findViewById(R.id.btnSaveCarLog)

        // 저장 버튼 클릭 시 차계부 정보 저장
        btnSaveCarLog.setOnClickListener {
            Toast.makeText(this, "Successfully saved!", Toast.LENGTH_SHORT).show()
        }
    }
}
