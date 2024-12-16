package com.example.parklog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parklog.databinding.FragmentCarLogBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat

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

        adapter = RecentRecordsAdapter(mutableListOf())
        binding.recyclerRecentRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentRecords.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        observeViewModel()

        binding.btnAddMileage.setOnClickListener {
            handleMileageButtonClick()
        }

        binding.btnAddFuel.setOnClickListener {
            showAddFuelDialog()
        }
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

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true

        if (ActivityCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val currentLocation = LatLng(it.latitude, it.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                } ?: run {
                    val defaultLocation = LatLng(37.5665, 126.9780) // 서울
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
                    Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
        }
    }

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
        val dialogBinding = com.example.parklog.databinding.StartLocationBinding.inflate(LayoutInflater.from(requireContext()))
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
        val dialogBinding = com.example.parklog.databinding.EndLocationBinding.inflate(LayoutInflater.from(requireContext()))
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
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
            !startLocationName.isNullOrEmpty() && !endLocationName.isNullOrEmpty()
        ) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
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

            viewModel.addRecord(record)
            viewModel.updateCumulativeData(record.distance, record.totalCost)

            startLocation = null
            endLocation = null
            startLocationName = null
            endLocationName = null
            recordDate = null
        }
    }

    private fun showAddFuelDialog() {
        val dialogBinding = com.example.parklog.databinding.AddFuelBinding.inflate(LayoutInflater.from(requireContext()))

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
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

                viewModel.addRecord(record)
                viewModel.updateCumulativeData(record.distance, record.totalCost)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
