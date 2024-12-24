package com.example.parklog

import android.content.Intent
import android.os.Bundle
import android.os.Looper
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarLogFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCarLogBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("ViewBinding is null")

    private val viewModel: CarLogViewModel by viewModels()

    private lateinit var adapter: RecentRecordsAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val polylinePoints = mutableListOf<LatLng>()
    private var currentPolyline: Polyline? = null
    private var locationCallback: LocationCallback? = null

    private var startLocation: LatLng? = null
    private var endLocation: LatLng? = null
    private var startLocationName: String? = null
    private var endLocationName: String? = null
    private var recordDate: String? = null

    // View 생성
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    // UI 초기화 및 ViewModel 관찰
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        observeViewModel()
        initMap()
    }

    // UI 초기화
    private fun initUI() {
        _binding?.let { binding ->
            adapter = RecentRecordsAdapter(mutableListOf())
            binding.recyclerRecentRecords.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerRecentRecords.adapter = adapter

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

            binding.homeButton.setOnClickListener {
                startActivity(Intent(requireContext(), MainActivity::class.java))
            }
            binding.btnAddMileage.setOnClickListener { handleMileageButtonClick() }
            binding.btnAddFuel.setOnClickListener { showAddFuelDialog() }
        }
    }

    // ViewModel 관찰
    private fun observeViewModel() {
        _binding?.let { binding ->
            viewModel.records.observe(viewLifecycleOwner) { records ->
                adapter.updateRecords(records)
            }

            viewModel.cumulativeData.observe(viewLifecycleOwner) { (mileage, fuelCost) ->
                binding.totalMileageValue.text = "$mileage km"
                binding.totalFuelCostValue.text = "₩$fuelCost"
            }
        }
    }

    private fun initMap() {
        val mapFragment = binding.mapFragment.getFragment<SupportMapFragment>()
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
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
        val defaultLocation = LatLng(37.5975, 126.8647) // 항공대
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
        Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
        )
    }

    private fun resetLocationData() {
        startLocation = null
        endLocation = null
        startLocationName = null
        endLocationName = null
        recordDate = null
    }

    private fun clearPreviousPolyline() {
        currentPolyline?.remove()
        currentPolyline = null
        polylinePoints.clear()
    }

    // 위치 추적 시작
    private fun startLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000).build()

        clearPreviousPolyline()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    polylinePoints.add(currentLatLng)
                    currentPolyline = googleMap.addPolyline(
                        PolylineOptions()
                            .addAll(polylinePoints)
                            .color(android.graphics.Color.BLUE)
                            .width(8f)
                    )
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            locationCallback?.let { callback ->
                fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
            }
        } else {
            requestLocationPermission()
        }
    }

    // 위치 추적 중지
    private fun stopLocationTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    // 주행 기록 시작&종료
    private fun handleMileageButtonClick() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                if (startLocation == null) {
                    startLocation = currentLatLng
                    polylinePoints.clear()
                    startLocationTracking()
                    googleMap.addMarker(MarkerOptions().position(currentLatLng).title("출발 위치"))
                    showLocationDialog("출발 위치 입력", true)
                } else if (endLocation == null) {
                    endLocation = currentLatLng
                    stopLocationTracking()
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

        if (isStart) {
            dialogBinding.inputStartLocation.hint = "출발 위치를 입력하세요"
            dialogBinding.inputStartLocation.setText("")
            dialogBinding.inputDate.visibility = View.VISIBLE
            dialogBinding.inputDate.setText("")
        } else {
            dialogBinding.inputStartLocation.hint = "도착 위치를 입력하세요"
            dialogBinding.inputStartLocation.setText("")
            dialogBinding.inputDate.visibility = View.GONE
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
        if (polylinePoints.size > 1) {
            val distanceInKm = calculateTotalDistance(polylinePoints)

            val record = RecordData(
                date = recordDate ?: "날짜 미입력",
                startLocation = startLocationName ?: "",
                endLocation = endLocationName ?: "",
                distance = distanceInKm,
                pricePerLiter = 0,
                totalCost = 0
            )

            viewModel.addRecord(record)
            resetLocationData()
        }
    }

    private fun calculateTotalDistance(points: List<LatLng>): Int {
        var totalDistance = 0f

        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]

            val startLocation = android.location.Location("").apply {
                latitude = start.latitude
                longitude = start.longitude
            }
            val endLocation = android.location.Location("").apply {
                latitude = end.latitude
                longitude = end.longitude
            }

            totalDistance += startLocation.distanceTo(endLocation)
        }
        return (totalDistance / 1000).toInt()
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

    // ViewBinding 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
