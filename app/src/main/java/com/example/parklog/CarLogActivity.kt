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
                val dateInput = dialogBinding.inputDate.text.toString() // 날짜 입력 필드

                if (startLocationInput.isNotBlank() && dateInput.isNotBlank()) {
                    startLocationName = startLocationInput
                    recordDate = dateInput // 입력받은 날짜를 recordDate에 저장
                    Toast.makeText(this, "출발 위치: $startLocationName", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "출발 위치와 날짜를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
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

            // recordDate가 null인 경우 기본값 처리
            val finalRecordDate = recordDate ?: "날짜 미입력"

            // 기록 추가
            val record = RecordData(
                date = finalRecordDate,
                stationName = "",
                startLocation = startLocationName ?: "",
                endLocation = endLocationName ?: "",
                distance = distanceInKm.toInt(),
                pricePerLiter = 0,
                fuelAmount = 0.0,
                totalCost = 0,
                latitude = startLocation!!.latitude,
                longitude = startLocation!!.longitude
            )
            records.add(0, record) // 기록 리스트에 추가
            adapter.notifyItemInserted(0) // RecyclerView 업데이트

            // 사용자 확인 후 저장
            AlertDialog.Builder(this)
                .setTitle("주행 기록 저장")
                .setMessage("주행 기록을 저장하시겠습니까?")
                .setPositiveButton("저장") { _, _ ->
                    database.push().setValue(record) // Firebase에 저장
                }
                .setNegativeButton("취소", null)
                .show()

            // 초기화
            startLocation = null
            endLocation = null
            startLocationName = null
            endLocationName = null
            recordDate = null // 초기화
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
                val fuelAmount = if (pricePerLiter > 0) totalCost.toDouble() / pricePerLiter else 0.0

                // RecordData 생성자 호출 시 모든 필드에 올바른 타입 전달
                val record = RecordData(
                    date = date,                   // String
                    stationName = stationName,     // String
                    startLocation = "",            // 출발 위치 (빈 문자열로 대체)
                    endLocation = "",              // 도착 위치 (빈 문자열로 대체)
                    distance = distance,           // Int
                    pricePerLiter = pricePerLiter, // Int
                    fuelAmount = fuelAmount,       // Double
                    totalCost = totalCost          // Int
                )

                // 기록 추가
                records.add(0, record)
                adapter.notifyItemInserted(0)
                updateCumulativeData()

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

    private fun updateCumulativeData() {
        var totalMileage = 0
        var totalFuelCost = 0

        for (record in records) {
            totalMileage += record.distance
            totalFuelCost += record.totalCost
        }

        // 누적 데이터 표시
        binding.totalMileageValue.text = "$totalMileage km"
        binding.totalFuelCostValue.text = "₩$totalFuelCost"

        // Firebase에 누적 데이터 저장
        val cumulativeData = mapOf(
            "totalMileage" to totalMileage,
            "totalFuelCost" to totalFuelCost
        )

        database.child("CumulativeData").setValue(cumulativeData)
            .addOnSuccessListener {
                Log.d("Firebase", "누적 데이터 저장 성공")
            }
            .addOnFailureListener { e ->
                Log.w("Firebase", "누적 데이터 저장 실패", e)
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
