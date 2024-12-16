package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.*
import com.example.parklog.model.RecordData
import com.example.parklog.viewmodel.CarLogViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarLogFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCarLogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CarLogViewModel by viewModels()

    private lateinit var adapter: RecentRecordsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    private var startLocation: LatLng? = null
    private var endLocation: LatLng? = null
    private var startLocationName: String? = null
    private var endLocationName: String? = null
    private var recordDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModel()
        initMap()
    }

    private fun initUI() {
        adapter = RecentRecordsAdapter(mutableListOf())
        binding.recyclerRecentRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentRecords.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // 버튼 이벤트 설정
        binding.homeButton.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        binding.btnAddMileage.setOnClickListener { handleMileageButtonClick() }
        binding.btnAddFuel.setOnClickListener { showAddFuelDialog() }
    }

    private fun observeViewModel() {
        viewModel.records.observe(viewLifecycleOwner) { records ->
            adapter.updateRecords(records)
        }

        viewModel.cumulativeData.observe(viewLifecycleOwner) { (mileage, fuelCost) ->
            binding.totalMileageValue.text = "$mileage km"
            binding.totalFuelCostValue.text = "₩$fuelCost"
        }
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        setCurrentLocation()
    }

    private fun setCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                } ?: showDefaultLocation()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun showDefaultLocation() {
        val defaultLocation = LatLng(37.5665, 126.9780) // 서울
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
        )
    }

    private fun handleMileageButtonClick() {
        // 권한 체크
        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            // 권한 요청
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
            return
        }

        // 권한이 허용된 경우에만 실행
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                if (startLocation == null) {
                    startLocation = currentLatLng
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("출발 위치"))
                    showLocationDialog("출발 위치 입력", true)
                } else if (endLocation == null) {
                    endLocation = currentLatLng
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("도착 위치"))
                    showLocationDialog("도착 위치 입력", false)
                }
            } ?: run {
                Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "위치 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLocationDialog(title: String, isStart: Boolean) {
        val dialogBinding = StartLocationBinding.inflate(LayoutInflater.from(requireContext()))

        // 힌트 값 명확하게 설정
        if (isStart) {
            dialogBinding.inputStartLocation.hint = "출발 위치를 입력하세요"
            dialogBinding.inputStartLocation.setText("") // 이전 값 초기화
            dialogBinding.inputDate.visibility = View.VISIBLE // 날짜 필드 표시
            dialogBinding.inputDate.setText("") // 날짜 입력 초기화
        } else {
            dialogBinding.inputStartLocation.hint = "도착 위치를 입력하세요"
            dialogBinding.inputStartLocation.setText("") // 이전 값 초기화
            dialogBinding.inputDate.visibility = View.GONE // 날짜 필드 숨김
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(dialogBinding.root)
            .setPositiveButton("확인") { _, _ ->
                val locationName = dialogBinding.inputStartLocation.text.toString()
                val date = if (isStart) dialogBinding.inputDate.text.toString() else ""

                if (locationName.isNotEmpty() && (isStart && date.isNotEmpty() || !isStart)) {
                    if (isStart) {
                        startLocationName = locationName
                        recordDate = date
                    } else {
                        endLocationName = locationName
                        calculateAndAddRecord()
                    }
                } else {
                    Toast.makeText(requireContext(), "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }



    private fun calculateAndAddRecord() {
        if (startLocation != null && endLocation != null) {
            // 거리 계산
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                startLocation!!.latitude, startLocation!!.longitude,
                endLocation!!.latitude, endLocation!!.longitude,
                results
            )
            val distanceInKm = (results[0] / 1000).toInt()

            // Polyline으로 출발 위치와 도착 위치를 연결
            val polylineOptions = PolylineOptions()
                .add(startLocation) // 출발 위치
                .add(endLocation)   // 도착 위치
                .color(android.graphics.Color.BLUE) // 선 색상
                .width(8f) // 선 두께
            googleMap.addPolyline(polylineOptions)

            // RecordData 객체 생성
            val record = RecordData(
                date = recordDate ?: "날짜 미입력",
                startLocation = startLocationName ?: "",
                endLocation = endLocationName ?: "",
                distance = distanceInKm,
                pricePerLiter = 0,
                totalCost = 0
            )

            // ViewModel에 기록 추가
            viewModel.addRecord(record)

            // 초기화
            resetLocationData()
        }
    }


    private fun resetLocationData() {
        startLocation = null
        endLocation = null
        startLocationName = null
        endLocationName = null
        recordDate = null
    }

    private fun showAddFuelDialog() {
        val dialogBinding = AddFuelBinding.inflate(LayoutInflater.from(requireContext()))
        AlertDialog.Builder(requireContext())
            .setTitle("주유 기록 추가")
            .setView(dialogBinding.root)
            .setPositiveButton("추가") { _, _ ->
                val record = RecordData(
                    date = dialogBinding.inputDate.text.toString(),
                    stationName = dialogBinding.inputStationName.text.toString(),
                    distance = dialogBinding.inputDistance.text.toString().toIntOrNull() ?: 0,
                    pricePerLiter = dialogBinding.inputPricePerLiter.text.toString().toIntOrNull() ?: 0,
                    totalCost = dialogBinding.inputTotalCost.text.toString().toIntOrNull() ?: 0
                )
                viewModel.addRecord(record)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
