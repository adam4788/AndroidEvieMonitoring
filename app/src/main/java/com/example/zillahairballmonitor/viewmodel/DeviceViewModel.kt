package com.example.zillahairballmonitor.viewmodel

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zillahairballmonitor.bluetooth.BluetoothService
import com.example.zillahairballmonitor.data.ZillaMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that manages device connections and metrics data
 */
class DeviceViewModel(application: Application) : AndroidViewModel(application) {
    
    // State for metrics
    private val _metrics = MutableStateFlow(ZillaMetrics())
    val metrics: StateFlow<ZillaMetrics> = _metrics.asStateFlow()
    
    // State for bluetooth devices
    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()
    
    // Connection state
    private val _connectionState = MutableStateFlow(BluetoothService.Companion.ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<BluetoothService.Companion.ConnectionState> = _connectionState.asStateFlow()
    
    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Service connection
    private var bluetoothService: BluetoothService? = null
    private var bound = false
    
    // Service connection object
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothService.LocalBinder
            bluetoothService = binder.getService()
            bound = true
            
            // Start collecting metrics and connection state
            collectMetrics()
            collectConnectionState()
            collectErrorMessages()
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            bluetoothService = null
        }
    }
    
    init {
        // Bind to the service
        val intent = Intent(getApplication(), BluetoothService::class.java)
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        // Load paired devices
        refreshPairedDevices()
    }
    
    /**
     * Refresh the list of paired Bluetooth devices
     */
    fun refreshPairedDevices() {
        viewModelScope.launch {
            val bluetoothManager = getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            
            if (bluetoothAdapter != null) {
                if (hasBluetoothPermission()) {
                    val devices = bluetoothAdapter.bondedDevices.toList()
                    _pairedDevices.value = devices
                } else {
                    _errorMessage.value = "Bluetooth permission not granted"
                }
            } else {
                _errorMessage.value = "Bluetooth not available on this device"
            }
        }
    }
    
    /**
     * Connect to a Bluetooth device
     */
    fun connect(deviceAddress: String) {
        bluetoothService?.connect(deviceAddress)
    }
    
    /**
     * Disconnect from the current device
     */
    fun disconnect() {
        bluetoothService?.disconnect()
    }
    
    /**
     * Collect metrics from the service
     */
    private fun collectMetrics() {
        viewModelScope.launch {
            bluetoothService?.metricsFlow?.collect { metrics ->
                _metrics.value = metrics
            }
        }
    }
    
    /**
     * Collect connection state from the service
     */
    private fun collectConnectionState() {
        viewModelScope.launch {
            bluetoothService?.connectionState?.collect { state ->
                _connectionState.value = state
            }
        }
    }
    
    /**
     * Collect error messages from the service
     */
    private fun collectErrorMessages() {
        viewModelScope.launch {
            bluetoothService?.errorMessage?.collect { message ->
                _errorMessage.value = message
            }
        }
    }
    
    /**
     * Check if the app has necessary Bluetooth permissions
     */
    private fun hasBluetoothPermission(): Boolean {
        val context = getApplication<Application>()
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Send a command to the connected device
     */
    fun sendCommand(command: String): Boolean {
        return bluetoothService?.sendCommand(command) ?: false
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Unbind from the service
        if (bound) {
            getApplication<Application>().unbindService(serviceConnection)
            bound = false
        }
    }
}
