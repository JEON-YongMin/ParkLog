package com.example.parklog

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val devices = mutableListOf<String>() // 데이터를 수정할 수 있는 저장 리스트
    private lateinit var dialog: AlertDialog // 대화 상자
    private lateinit var deviceListContainer: LinearLayout // 기기 목록 표시
    private lateinit var database: DatabaseReference // Firebase Realtime Database 참조
    private var permissionRequestInProgress = false

    private val enableBleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            restartScanning()
        }
    }

    private val blePermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissionRequestInProgress = false
            permissions.entries.forEach {
                Timber.d("${it.key} = ${it.value}")
            }
            restartScanning()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance().reference

        // Firebase에서 기존 데이터를 불러오기
        loadDevicesFromDatabase()

        // BluetoothHandler 초기화
        BluetoothHandler.initialize(this)

        val deviceButton: Button = findViewById(R.id.DeviceButton)
        val parkingLocationButton: Button = findViewById(R.id.parkingLocationButton)
        val myParkingListButton: Button = findViewById(R.id.myParkingListButton)
        val carLogButton: Button = findViewById(R.id.carLogButton)

        deviceButton.setOnClickListener {
            showBluetoothScannerDialog()
        }

        parkingLocationButton.setOnClickListener {
            val intent = Intent(this, ParkingLocationActivity::class.java)
            startActivity(intent)
        }

        myParkingListButton.setOnClickListener {
            val intent = Intent(this, ParkingLocationList::class.java)
            startActivity(intent)
        }

        carLogButton.setOnClickListener {
            val intent = Intent(this, CarLogActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showBluetoothScannerDialog() {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val listView = ListView(this)
        val scannedDevicesAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        listView.adapter = scannedDevicesAdapter

        dialogLayout.addView(listView)

        val startScanButton = Button(this).apply {
            text = "Start Scanning"
            setOnClickListener {
                restartScanning()
            }
        }
        dialogLayout.addView(startScanButton)

        val cancelButton = Button(this).apply {
            text = "Close"
            setOnClickListener {
                dialog.dismiss()
            }
        }
        dialogLayout.addView(cancelButton)

        dialog = AlertDialog.Builder(this)
            .setTitle("Bluetooth Scanner")
            .setView(dialogLayout)
            .create()

        dialog.show()

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedDevice = scannedDevicesAdapter.getItem(position)
            selectedDevice?.let {
                saveDeviceToDatabase(it)
                Toast.makeText(this, "$it saved to Firebase", Toast.LENGTH_SHORT).show()
            }
        }

        // Launch a coroutine to collect BluetoothHandler.deviceFlow
        lifecycleScope.launch {
            BluetoothHandler.deviceFlow.collect { deviceNames ->
                runOnUiThread {
                    scannedDevicesAdapter.clear()
                    scannedDevicesAdapter.addAll(deviceNames)
                    scannedDevicesAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun restartScanning() {
        if (!BluetoothHandler.isBluetoothEnabled()) {
            Toast.makeText(this, "Bluetooth is not enabled. Please enable it to scan.", Toast.LENGTH_SHORT).show()
            enableBleRequest.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }

        if (BluetoothHandler.hasPermissions()) {
            BluetoothHandler.startScanning()
        } else {
            Toast.makeText(this, "Permissions are required to scan for devices.", Toast.LENGTH_SHORT).show()
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        val missingPermissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        blePermissionRequest.launch(missingPermissions)
    }

    private fun saveDeviceToDatabase(deviceName: String) {
        if (devices.contains(deviceName)) {
            Toast.makeText(this, "Device already exists in Firebase", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("devices").push().setValue(deviceName)
            .addOnSuccessListener {
                Toast.makeText(this, "Device saved to Firebase", Toast.LENGTH_SHORT).show()
                devices.add(deviceName)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save device", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadDevicesFromDatabase() {
        database.child("devices").get()
            .addOnSuccessListener { snapshot ->
                devices.clear() // 기존 데이터를 초기화
                for (child in snapshot.children) {
                    val deviceName = child.getValue(String::class.java)
                    deviceName?.let {
                        devices.add(it) // Firebase에서 가져온 데이터를 리스트에 추가
                    }
                }

                // UI 업데이트
                if (::deviceListContainer.isInitialized) {
                    deviceListContainer.removeAllViews()
                    devices.forEach { deviceName ->
                        val deviceTextView = TextView(this).apply {
                            text = deviceName
                        }
                        deviceListContainer.addView(deviceTextView)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load devices: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        BluetoothHandler.stopScanning()
    }
}
