package com.example.parklog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.parklog.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NavHostFragment를 통해 Navigation Graph 관리
    }
}
