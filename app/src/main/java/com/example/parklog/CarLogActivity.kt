package com.example.parklog

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CarLogActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarLogBinding
    private lateinit var adapter: RecentRecordsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    private val records = mutableListOf<RecordData>()

    private var recordDate: String? = null // 클래스 레벨 변수 추가
    private var startLocation: LatLng? = null
    private var endLocation: LatLng? = null
    private var startLocationName: String? = null
    private var endLocationName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding 초기화
        binding = ActivityCarLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView 초기화
        adapter = RecentRecordsAdapter(records)
        binding.recyclerRecentRecords.layoutManager = LinearLayoutManager(this)
        binding.recyclerRecentRecords.adapter = adapter

        // Firebase 초기화
        database = FirebaseDatabase.getInstance().reference.child("CarRecords")

        // 누적 데이터 가져오기
        fetchCumulativeDataFromFirebase()

        // 기존 데이터 가져오기
        fetchRecordsFromRealtimeDatabase()

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Google Maps 초기화
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        binding.btnAddMileage.setOnClickListener {
            handleMileageButtonClick()
        }

        binding.btnAddFuel.setOnClickListener {
            showAddFuelDialog()
        }
    }

    // Google Maps 준비 완료
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true

            // 현재 위치 가져오기
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                } else {
                    // 위치를 가져올 수 없는 경우 기본 위치 설정 (서울)
                    val defaultLocation = LatLng(37.5665, 126.9780)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
                    Toast.makeText(this, "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // 위치 권한이 없으면 요청
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }
    }


    // 주행 기록 처리
    private fun handleMileageButtonClick() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                if (startLocation == null) {
                    startLocation = currentLatLng
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("출발 위치"))
                    showStartLocationDialog()
                } else if (endLocation == null) {
                    endLocation = currentLatLng
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("도착 위치"))
                    showEndLocationDialog()
                }
            }
        }
    }

    private fun showStartLocationDialog() {
        val dialogBinding = StartLocationBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setTitle("출발 위치 입력")
            .setView(dialogBinding.root)
            .setPositiveButton("확인") { _, _ ->
                startLocationName = dialogBinding.inputStartLocation.text.toString()
                recordDate = dialogBinding.inputDate.text.toString()
                if (startLocationName.isNullOrEmpty() || recordDate.isNullOrEmpty()) {
                    Toast.makeText(this, "출발 위치와 날짜를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showEndLocationDialog() {
        val dialogBinding = EndLocationBinding.inflate(layoutInflater)
        AlertDialog.Builder(this)
            .setTitle("도착 위치 입력")
            .setView(dialogBinding.root)
            .setPositiveButton("확인") { _, _ ->
                endLocationName = dialogBinding.inputEndLocation.text.toString()
                if (!endLocationName.isNullOrEmpty()) {
                    calculateAndShowDistance()
                } else {
                    Toast.makeText(this, "도착 위치를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun calculateAndShowDistance() {
        if (startLocation != null && endLocation != null &&
            !startLocationName.isNullOrEmpty() && !endLocationName.isNullOrEmpty()) {
            val results = FloatArray(1)
            Location.distanceBetween(
                startLocation!!.latitude, startLocation!!.longitude,
                endLocation!!.latitude, endLocation!!.longitude,
                results
            )
            val distanceInKm = results[0] / 1000
            Toast.makeText(this, "주행 거리: %.2f km".format(distanceInKm), Toast.LENGTH_LONG).show()

            val record = RecordData(
                date = recordDate ?: "날짜 미입력",
                stationName = "",
                startLocation = startLocationName!!,
                endLocation = endLocationName!!,
                distance = distanceInKm.toInt(),
                pricePerLiter = 0,
                totalCost = 0
            )

            database.push().setValue(record)
            records.add(0, record)
            adapter.notifyItemInserted(0)

            startLocation = null
            endLocation = null
            startLocationName = null
            endLocationName = null
            recordDate = null
        }
    }

    // 주유 기록 추가 Dialog
    private fun showAddFuelDialog() {
        val dialogBinding = AddFuelBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this)
            .setTitle("주유 기록 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가") { _, _ ->
                val date = dialogBinding.inputDate.text.toString()
                val stationName = dialogBinding.inputStationName.text.toString()
                val distance = dialogBinding.inputDistance.text.toString().toIntOrNull() ?: 0
                val pricePerLiter = dialogBinding.inputPricePerLiter.text.toString().toIntOrNull() ?: 0
                val totalCost = dialogBinding.inputTotalCost.text.toString().toIntOrNull() ?: 0

                // RecordData 생성자 호출 시 모든 필드에 올바른 타입 전달
                val record = RecordData(
                    date = date,                   // String
                    stationName = stationName,     // String
                    startLocation = "",            // 출발 위치 (빈 문자열로 대체)
                    endLocation = "",              // 도착 위치 (빈 문자열로 대체)
                    distance = distance,           // Int
                    pricePerLiter = pricePerLiter, // Int
                    totalCost = totalCost          // Int
                )

                // 기록 추가
                records.add(0, record)
                adapter.notifyItemInserted(0)
                updateCumulativeData(record.distance, record.totalCost)

                // Firebase에 저장
                database.push().setValue(record)
                    .addOnSuccessListener {
                        Log.d("Firebase", "주유 기록 저장 성공")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firebase", "주유 기록 저장 실패", e)
                    }
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    private fun updateCumulativeData(newDistance: Int, newFuelCost: Int) {
        database.child("CumulativeData").get()
            .addOnSuccessListener { snapshot ->
                val totalMileage = snapshot.child("totalMileage").getValue(Int::class.java) ?: 0
                val totalFuelCost = snapshot.child("totalFuelCost").getValue(Int::class.java) ?: 0

                val updatedMileage = totalMileage + newDistance
                val updatedFuelCost = totalFuelCost + newFuelCost

                val cumulativeData = mapOf(
                    "totalMileage" to updatedMileage,
                    "totalFuelCost" to updatedFuelCost
                )

                database.child("CumulativeData").setValue(cumulativeData)
                    .addOnSuccessListener {
                        binding.totalMileageValue.text = "$updatedMileage km"
                        binding.totalFuelCostValue.text = "₩$updatedFuelCost"
                        Log.d("Firebase", "누적 데이터 업데이트 성공")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firebase", "누적 데이터 업데이트 실패", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "누적 데이터 가져오기 실패", e)
            }
    }

    private fun fetchRecordsFromRealtimeDatabase() {
        database.get()
            .addOnSuccessListener { snapshot ->
                records.clear()
                for (child in snapshot.children) {
                    val record = child.getValue(RecordData::class.java)
                    if (record != null) {
                        records.add(record)
                    }
                }
                records.sortByDescending { it.timestamp }
                adapter.notifyDataSetChanged()

                // 누적 데이터는 fetch 시점에 갱신하지 않음
                Log.d("Firebase", "주행 기록 데이터 불러오기 성공")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "데이터 가져오기 실패", e)
            }
    }

    private fun fetchCumulativeDataFromFirebase() {
        database.child("CumulativeData").get()
            .addOnSuccessListener { snapshot ->
                val cumulativeMileage = snapshot.child("totalMileage").getValue(Int::class.java) ?: 0
                val cumulativeFuelCost = snapshot.child("totalFuelCost").getValue(Int::class.java) ?: 0

                // 초기 누적 데이터 설정
                binding.totalMileageValue.text = "$cumulativeMileage km"
                binding.totalFuelCostValue.text = "₩$cumulativeFuelCost"
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "누적 데이터 가져오기 실패", e)
            }
    }
}
