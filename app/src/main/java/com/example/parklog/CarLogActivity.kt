package com.example.parklog

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.parklog.databinding.ActivityCarLogBinding
import com.example.parklog.databinding.StartLocationBinding
import com.example.parklog.databinding.EndLocationBinding
import com.example.parklog.databinding.AddFuelBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarLogActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarLogBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    private var startLocation: LatLng? = null
    private var endLocation: LatLng? = null
    private var startLocationName: String? = null
    private var endLocationName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Google Maps 초기화
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 주행 기록 버튼 클릭 처리
        binding.btnAddMileage.setOnClickListener {
            handleMileageButtonClick()
        }

        // 주유 기록 버튼 클릭 처리
        binding.btnAddFuel.setOnClickListener {
            showAddFuelDialog()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // 기본 위치로 서울 설정
        val defaultLocation = LatLng(37.5665, 126.9780) // 서울
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
    }

    // 주행 기록 처리
    private fun handleMileageButtonClick() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 권한 요청
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                if (startLocation == null) {
                    // 출발 위치 설정 및 지도에 표시
                    startLocation = currentLatLng
                    googleMap.addMarker(
                        MarkerOptions().position(currentLatLng).title("출발 위치")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    showStartLocationDialog()
                } else if (endLocation == null) {
                    // 도착 위치 설정 및 지도에 표시
                    endLocation = currentLatLng
                    googleMap.addMarker(
                        MarkerOptions().position(currentLatLng).title("도착 위치")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    showEndLocationDialog()
                }
            } else {
                Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showStartLocationDialog() {
        val dialogBinding = StartLocationBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("출발 위치 입력")
            .setView(dialogBinding.root)
            .setPositiveButton("확인") { _, _ ->
                val startLocationInput = dialogBinding.inputStartLocation.text.toString()
                if (startLocationInput.isNotBlank()) {
                    startLocationName = startLocationInput
                    Toast.makeText(this, "출발 위치: $startLocationName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "출발 위치를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    }

    private fun showEndLocationDialog() {
        val dialogBinding = EndLocationBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("도착 위치 입력")
            .setView(dialogBinding.root)
            .setPositiveButton("확인") { _, _ ->
                val endLocationInput = dialogBinding.inputEndLocation.text.toString()
                if (endLocationInput.isNotBlank()) {
                    endLocationName = endLocationInput
                    Toast.makeText(this, "도착 위치: $endLocationName", Toast.LENGTH_SHORT).show()

                    // 주행 거리 계산
                    calculateAndShowDistance()
                } else {
                    Toast.makeText(this, "도착 위치를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    }

    private fun calculateAndShowDistance() {
        if (startLocation != null && endLocation != null) {
            val results = FloatArray(1)
            Location.distanceBetween(
                startLocation!!.latitude, startLocation!!.longitude,
                endLocation!!.latitude, endLocation!!.longitude,
                results
            )
            val distanceInKm = results[0] / 1000
            Toast.makeText(this, "주행 거리: %.2f km".format(distanceInKm), Toast.LENGTH_LONG).show()

            // 주행 기록 초기화
            startLocation = null
            endLocation = null
            startLocationName = null
            endLocationName = null
        }
    }

    // 주유 기록 처리
    private fun showAddFuelDialog() {
        val dialogBinding = AddFuelBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setTitle("주유 기록 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("확인") { _, _ ->
                val stationName = dialogBinding.inputStationName.text.toString()
                val distance = dialogBinding.inputDistance.text.toString().toIntOrNull() ?: 0
                val pricePerLiter = dialogBinding.inputPricePerLiter.text.toString().toIntOrNull() ?: 0
                val totalCost = dialogBinding.inputTotalCost.text.toString().toIntOrNull() ?: 0

                if (stationName.isNotBlank()) {
                    val fuelAmount = if (pricePerLiter > 0) totalCost.toDouble() / pricePerLiter else 0.0
                    Toast.makeText(
                        this,
                        "주유소: $stationName\n주행 거리: $distance km\n가격: $pricePerLiter ₩/L\n주유량: %.2f L\n총 비용: $totalCost"
                            .format(fuelAmount),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this, "주유소 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .create()
        dialog.show()
    }
}
