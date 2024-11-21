package com.example.parklog

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
object BluetoothHandler {

    private lateinit var context: Context
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // BluetoothAdapter 초기화
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // BLE 장치 검색 결과를 저장하는 StateFlow
    private val _deviceFlow = MutableStateFlow<List<String>>(emptyList())
    val deviceFlow = _deviceFlow.asStateFlow()

    // BluetoothHandler 초기화
    fun initialize(context: Context) {
        this.context = context.applicationContext

        // BroadcastReceiver 등록
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND).apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        context.registerReceiver(discoveryReceiver, filter)

        log("BluetoothHandler initialized")
    }

    // 스캔 시작
    fun startScanning() {
        if (!hasPermissions()) {
            log("Missing required Bluetooth permissions.")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            log("Bluetooth is disabled. Please enable Bluetooth.")
            return
        }

        try {
            if (bluetoothAdapter.isDiscovering) {
                log("Already discovering. Stopping and restarting discovery.")
                bluetoothAdapter.cancelDiscovery()
            }

            bluetoothAdapter.startDiscovery()
            log("Started scanning for Bluetooth devices")
        } catch (e: SecurityException) {
            log("SecurityException: ${e.message}")
        }
    }

    // 스캔 중지
    fun stopScanning() {
        if (!hasPermissions()) {
            log("Missing required Bluetooth permissions.")
            return
        }

        try {
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
                log("Stopped scanning for Bluetooth devices")
            }
        } catch (e: SecurityException) {
            log("SecurityException: ${e.message}")
        }
    }

    // Bluetooth 사용 가능한지 확인
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter.isEnabled

    // BLE 권한 확인
    fun hasPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        return permissions.all { permission ->
            ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 장치를 StateFlow에 추가
    private fun addDeviceToFlow(deviceName: String) {
        val currentDevices = _deviceFlow.value.toMutableList()
        if (!currentDevices.contains(deviceName)) {
            currentDevices.add(deviceName)
            _deviceFlow.value = currentDevices
            log("Discovered device: $deviceName")
        }
    }

    // Bluetooth 장치 콜백
    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    val deviceName = device?.name ?: "Unknown Device"
                    addDeviceToFlow(deviceName)
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    log("Discovery started")
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    log("Discovery finished")
                }
            }
        }
    }

    // 디버그 로그 함수
    private fun log(message: String) {
        handler.post {
            println("[BluetoothHandler]: $message")
        }
    }
}
