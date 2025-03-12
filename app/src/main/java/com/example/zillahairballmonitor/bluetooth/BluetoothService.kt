package com.example.zillahairballmonitor.bluetooth

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.example.zillahairballmonitor.data.ControllerStatus
import com.example.zillahairballmonitor.data.ErrorCode
import com.example.zillahairballmonitor.data.ZillaMetrics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Date
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Service that handles Bluetooth communication with the Zilla Hairball 2 interface
 */
class BluetoothService : Service() {
    companion object {
        private const val TAG = "BluetoothService"
        
        // Standard SPP (Serial Port Profile) UUID for Bluetooth serial communication
        private val UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        
        // Connection states
        enum class ConnectionState {
            DISCONNECTED,
            CONNECTING,
            CONNECTED,
            ERROR
        }
    }
    
    // Service binder
    private val binder = LocalBinder()
    
    // Coroutine scope for the service
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Bluetooth adapter instance
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    
    // Connection components
    private var bluetoothDevice: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    
    // Flags
    private val isRunning = AtomicBoolean(false)
    private val isConnecting = AtomicBoolean(false)
    
    // State flows for reactive UI updates
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _metricsFlow = MutableStateFlow(ZillaMetrics())
    val metricsFlow: StateFlow<ZillaMetrics> = _metricsFlow.asStateFlow()
    
    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    override fun onDestroy() {
        disconnect()
        serviceScope.cancel()
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }
    
    /**
     * Connect to a Bluetooth device
     */
    fun connect(deviceAddress: String): Boolean {
        // Already connecting
        if (isConnecting.get()) {
            return false
        }
        
        // Check if Bluetooth is available
        val adapter = bluetoothAdapter ?: return false
        
        // Check if already connected to the requested device
        if (isRunning.get() && bluetoothDevice?.address == deviceAddress) {
            return true
        }
        
        // Disconnect from any existing connection
        disconnect()
        
        isConnecting.set(true)
        _connectionState.value = ConnectionState.CONNECTING
        _errorMessage.value = null
        
        serviceScope.launch {
            try {
                // Get the BluetoothDevice object
                bluetoothDevice = adapter.getRemoteDevice(deviceAddress)
                
                // Create a socket and connect
                bluetoothSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(UUID_SPP)
                bluetoothSocket?.let { socket ->
                    // Cancel discovery as it slows down the connection
                    adapter.cancelDiscovery()
                    
                    try {
                        // Connect to the device
                        socket.connect()
                        
                        // Get the input and output streams
                        inputStream = socket.inputStream
                        outputStream = socket.outputStream
                        
                        // Update state
                        isRunning.set(true)
                        _connectionState.value = ConnectionState.CONNECTED
                        
                        // Start reading data
                        startReading()
                    } catch (e: IOException) {
                        Log.e(TAG, "Socket connect failed", e)
                        closeConnection()
                        _connectionState.value = ConnectionState.ERROR
                        _errorMessage.value = "Failed to connect: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connection error", e)
                closeConnection()
                _connectionState.value = ConnectionState.ERROR
                _errorMessage.value = "Connection error: ${e.message}"
            } finally {
                isConnecting.set(false)
            }
        }
        
        return true
    }
    
    /**
     * Disconnect from the Bluetooth device
     */
    fun disconnect() {
        if (isRunning.get()) {
            isRunning.set(false)
            closeConnection()
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
    
    /**
     * Send a command to the Zilla Hairball 2 interface
     */
    fun sendCommand(command: String): Boolean {
        if (!isRunning.get()) {
            return false
        }
        
        try {
            outputStream?.write(command.toByteArray())
            outputStream?.flush()
            return true
        } catch (e: IOException) {
            Log.e(TAG, "Error sending command", e)
            _errorMessage.value = "Error sending command: ${e.message}"
            return false
        }
    }
    
    /**
     * Close connection and clean up resources
     */
    private fun closeConnection() {
        try {
            inputStream?.close()
            outputStream?.close()
            bluetoothSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing connection", e)
        } finally {
            bluetoothSocket = null
            inputStream = null
            outputStream = null
            bluetoothDevice = null
        }
    }
    
    /**
     * Start reading data from the Bluetooth socket
     */
    private fun startReading() {
        serviceScope.launch {
            val buffer = ByteArray(1024)
            var bytes: Int
            
            try {
                while (isRunning.get() && isActive) {
                    inputStream?.let { input ->
                        if (input.available() > 0) {
                            bytes = input.read(buffer)
                            val data = String(buffer, 0, bytes)
                            processData(data)
                        }
                    }
                    
                    // Demo mode: If we're not receiving real data, simulate some metrics
                    if (inputStream == null || !isRunning.get()) {
                        simulateMetrics()
                    }
                    
                    delay(100) // Read every 100ms
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error reading", e)
                isRunning.set(false)
                _connectionState.value = ConnectionState.ERROR
                _errorMessage.value = "Connection lost: ${e.message}"
                closeConnection()
            }
        }
    }
    
    /**
     * Process data received from the Zilla Hairball 2 interface
     * This is a placeholder implementation - actual protocol parsing would go here
     */
    private fun processData(data: String) {
        // Implementation would depend on the actual protocol used by Zilla Hairball 2
        // This is a placeholder for demonstration
        
        // In a real implementation, you would parse the serial data according to the
        // Zilla Hairball 2 protocol specification and extract the various metrics
        
        Log.d(TAG, "Received data: $data")
    }
    
    /**
     * Simulate changing metrics for demonstration purposes
     */
    private suspend fun simulateMetrics() {
        // Get current metrics
        val current = _metricsFlow.value
        
        // Simulate small random changes
        val battV = current.batteryVoltage + (Math.random().toFloat() * 0.4f - 0.2f)
        val battI = current.batteryCurrent + (Math.random().toFloat() * 2f - 1f)
        val motV = current.motorVoltage + (Math.random().toFloat() * 0.4f - 0.2f)
        val motI = current.motorCurrent + (Math.random().toFloat() * 2f - 1f)
        val rpm = current.motorSpeed + ((Math.random() * 20 - 10).toInt())
        val accel = current.acceleratorPosition + (Math.random().toFloat() * 0.02f - 0.01f)
        val accelClamped = accel.coerceIn(0f, 1f)
        
        // Update metrics flow
        _metricsFlow.value = current.copy(
            timestamp = Date(),
            batteryVoltage = 120 + battV.coerceIn(-10f, 10f),
            batteryCurrent = 50 + battI.coerceIn(-30f, 100f),
            motorVoltage = 118 + motV.coerceIn(-10f, 10f),
            motorCurrent = 40 + motI.coerceIn(-20f, 80f),
            motorSpeed = 2000 + rpm.coerceIn(-500, 2000),
            acceleratorPosition = accelClamped,
            controllerStatus = ControllerStatus.RUNNING
        )
        
        delay(200) // Update every 200ms
    }
    
    /**
     * Binder class for clients to access this service
     */
    inner class LocalBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }
}
