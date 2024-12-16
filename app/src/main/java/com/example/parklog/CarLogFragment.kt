package com.example.parklog

import android.app.AlertDialog
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
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

class CarLogFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCarLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecentRecordsAdapter
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    private val records = mutableListOf<RecordData>()

    private var recordDate: String? = null
    private var startLocation: LatLng? = null
    private var endLocation: LatLng? = null
    private var startLocationName: String? = null
    private var endLocationName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화
        adapter = RecentRecordsAdapter(records)
        binding.recyclerRecentRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentRecords.adapter = adapter

        // Firebase 초기화
        database = FirebaseDatabase.getInstance().reference.child("CarRecords")

        // 누적 데이터 가져오기
        fetchCumulativeDataFromFirebase()

        // 기존 데이터 가져오기
        fetchRecordsFromRealtimeDatabase()

        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Google Maps 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnAddMileage.setOnClickListener {
            handleMileageButtonClick()
        }

        binding.btnAddFuel.setOnClickListener {
            showAddFuelDialog()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                } else {
                    // 기본 위치 설정
                    val defaultLocation = LatLng(37.5665, 126.9780) // 서울
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
                    Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }
    }

    private fun handleMileageButtonClick() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
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
        val dialogBinding = StartLocationBinding.inflate(LayoutInflater.from(requireContext()))
        AlertDialog.Builder(requireContext())
            .setTitle("출발 위치 입력")
            .setView(dialogBinding.root)
            .setPositiveButton("확인") { _, _ ->
                startLocationName = dialogBinding.inputStartLocation.text.toString()
                recordDate = dialogBinding.inputDate.text.toString()
                if (startLocationName.isNullOrEmpty() || recordDate.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), "출발 위치와 날짜를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showEndLocationDialog() {
        val dialogBinding = EndLocationBinding.inflate(LayoutInflater.from(requireContext()))
        AlertDialog.Builder(requireContext())
            .setTitle("도착 위치 입력")
            .setView(dialogBinding.root)
            .setPositiveButton("확인") { _, _ ->
                endLocationName = dialogBinding.inputEndLocation.text.toString()
                if (!endLocationName.isNullOrEmpty()) {
                    calculateAndShowDistance()
                } else {
                    Toast.makeText(requireContext(), "도착 위치를 입력해주세요.", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(requireContext(), "주행 거리: %.2f km".format(distanceInKm), Toast.LENGTH_LONG).show()

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

    private fun showAddFuelDialog() {
        val dialogBinding = AddFuelBinding.inflate(LayoutInflater.from(requireContext()))

        AlertDialog.Builder(requireContext())
            .setTitle("주유 기록 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가") { _, _ ->
                val date = dialogBinding.inputDate.text.toString()
                val stationName = dialogBinding.inputStationName.text.toString()
                val distance = dialogBinding.inputDistance.text.toString().toIntOrNull() ?: 0
                val pricePerLiter = dialogBinding.inputPricePerLiter.text.toString().toIntOrNull() ?: 0
                val totalCost = dialogBinding.inputTotalCost.text.toString().toIntOrNull() ?: 0

                val record = RecordData(
                    date = date,
                    stationName = stationName,
                    startLocation = "",
                    endLocation = "",
                    distance = distance,
                    pricePerLiter = pricePerLiter,
                    totalCost = totalCost
                )

                records.add(0, record)
                adapter.notifyItemInserted(0)
                updateCumulativeData(record.distance, record.totalCost)

                database.push().setValue(record)
                    .addOnSuccessListener {
                        Log.d("Firebase", "주유 기록 저장 성공")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firebase", "주유 기록 저장 실패", e)
                    }
            }
            .setNegativeButton("취소", null)
            .show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
