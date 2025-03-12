package com.example.zillahairballmonitor.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zillahairballmonitor.R
import com.example.zillahairballmonitor.bluetooth.BluetoothService
import com.example.zillahairballmonitor.ui.components.CircularGauge
import com.example.zillahairballmonitor.ui.theme.DarkBackground
import com.example.zillahairballmonitor.ui.theme.Red500
import com.example.zillahairballmonitor.ui.theme.StatusDisabled
import com.example.zillahairballmonitor.ui.theme.StatusError
import com.example.zillahairballmonitor.ui.theme.StatusOk
import com.example.zillahairballmonitor.ui.theme.StatusWarning
import com.example.zillahairballmonitor.viewmodel.DeviceViewModel

/**
 * Main dashboard screen displaying all metrics and controls
 */
@Composable
fun DashboardScreen(deviceViewModel: DeviceViewModel) {
    val metrics by deviceViewModel.metrics.collectAsState()
    val connectionState by deviceViewModel.connectionState.collectAsState()
    val errorMessage by deviceViewModel.errorMessage.collectAsState()
    val pairedDevices by deviceViewModel.pairedDevices.collectAsState()
    
    val scaffoldState = rememberScaffoldState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error message if available
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(message = it)
        }
    }
    
    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header with title and connection status
            DashboardHeader(
                connectionState = connectionState,
                onConnectClick = {
                    // For demo purposes, connect to first device or show demo mode
                    pairedDevices.firstOrNull()?.let {
                        deviceViewModel.connect(it.address)
                    }
                },
                onDisconnectClick = {
                    deviceViewModel.disconnect()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main metrics display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Main gauges
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Battery voltage
                        CircularGauge(
                            value = metrics.batteryVoltage,
                            maxValue = 150f,
                            minValue = 80f,
                            title = stringResource(R.string.battery_voltage),
                            unit = stringResource(R.string.unit_volts),
                            warningThreshold = 0.85f,
                            criticalThreshold = 0.95f
                        )
                        
                        // Battery current
                        CircularGauge(
                            value = metrics.batteryCurrent,
                            maxValue = 200f,
                            title = stringResource(R.string.battery_current),
                            unit = stringResource(R.string.unit_amps),
                            warningThreshold = 0.75f,
                            criticalThreshold = 0.9f
                        )
                        
                        // Motor voltage
                        CircularGauge(
                            value = metrics.motorVoltage,
                            maxValue = 150f,
                            minValue = 80f,
                            title = stringResource(R.string.motor_voltage),
                            unit = stringResource(R.string.unit_volts),
                            warningThreshold = 0.85f,
                            criticalThreshold = 0.95f
                        )
                        
                        // Motor current
                        CircularGauge(
                            value = metrics.motorCurrent,
                            maxValue = 200f,
                            title = stringResource(R.string.motor_current),
                            unit = stringResource(R.string.unit_amps),
                            warningThreshold = 0.75f,
                            criticalThreshold = 0.9f
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Secondary metrics row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Motor speed
                        CircularGauge(
                            value = metrics.motorSpeed.toFloat(),
                            maxValue = 4000f,
                            title = stringResource(R.string.motor_speed),
                            unit = stringResource(R.string.unit_rpm),
                            warningThreshold = 0.7f,
                            criticalThreshold = 0.9f
                        )
                        
                        // Power output
                        CircularGauge(
                            value = metrics.powerOutput,
                            maxValue = 30f,
                            title = stringResource(R.string.power_output),
                            unit = stringResource(R.string.unit_kw),
                            warningThreshold = 0.7f,
                            criticalThreshold = 0.9f
                        )
                        
                        // Accelerator position
                        CircularGauge(
                            value = metrics.acceleratorPosition * 100,
                            maxValue = 100f,
                            title = stringResource(R.string.accelerator_position),
                            unit = stringResource(R.string.unit_percent),
                            warningThreshold = 0.8f,
                            criticalThreshold = 0.95f
                        )
                        
                        // Motor efficiency (if available)
                        CircularGauge(
                            value = metrics.motorEfficiency ?: 0f,
                            maxValue = 100f,
                            title = stringResource(R.string.efficiency),
                            unit = stringResource(R.string.unit_percent),
                            warningThreshold = 0.4f,
                            criticalThreshold = 0.2f
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status indicators row
                    StatusIndicators(metrics = metrics)
                }
            }
        }
    }
}

/**
 * Dashboard header with title and connection status
 */
@Composable
fun DashboardHeader(
    connectionState: BluetoothService.Companion.ConnectionState,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Title
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.h4.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        )
        
        // Connection status and buttons
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status indicator
            val (statusColor, statusText) = when (connectionState) {
                BluetoothService.Companion.ConnectionState.CONNECTED -> Pair(StatusOk, stringResource(R.string.status_connected))
                BluetoothService.Companion.ConnectionState.CONNECTING -> Pair(StatusWarning, stringResource(R.string.status_connecting))
                BluetoothService.Companion.ConnectionState.ERROR -> Pair(StatusError, stringResource(R.string.status_error))
                BluetoothService.Companion.ConnectionState.DISCONNECTED -> Pair(StatusDisabled, stringResource(R.string.status_disconnected))
            }
            
            Box(
                modifier = Modifier
                    .background(
                        color = statusColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            
            // Connection buttons
            if (connectionState == BluetoothService.Companion.ConnectionState.DISCONNECTED || 
                connectionState == BluetoothService.Companion.ConnectionState.ERROR) {
                Button(
                    onClick = onConnectClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Red500
                    )
                ) {
                    Text(text = stringResource(R.string.connect_bluetooth))
                }
            } else {
                Button(
                    onClick = onDisconnectClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.DarkGray
                    )
                ) {
                    Text(text = stringResource(R.string.disconnect_bluetooth))
                }
            }
        }
    }
}

/**
 * Status indicators for various controller states
 */
@Composable
fun StatusIndicators(metrics: com.example.zillahairballmonitor.data.ZillaMetrics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.7f),
        elevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Controller status
            StatusLabel(
                label = stringResource(R.string.controller_status),
                value = metrics.controllerStatus.toString(),
                color = when (metrics.controllerStatus) {
                    com.example.zillahairballmonitor.data.ControllerStatus.RUNNING -> StatusOk
                    com.example.zillahairballmonitor.data.ControllerStatus.PRECHARGE -> StatusWarning
                    com.example.zillahairballmonitor.data.ControllerStatus.FAULT -> StatusError
                    else -> StatusDisabled
                }
            )
            
            // Precharge status
            StatusLabel(
                label = "Precharge",
                value = if (metrics.prechargeStatus) "Active" else "Inactive",
                color = if (metrics.prechargeStatus) StatusWarning else StatusDisabled
            )
            
            // Reverse mode
            StatusLabel(
                label = "Reverse",
                value = if (metrics.reverseMode) "Active" else "Off",
                color = if (metrics.reverseMode) StatusWarning else StatusDisabled
            )
            
            // Valet mode
            StatusLabel(
                label = "Valet Mode",
                value = if (metrics.valetMode) "Active" else "Off",
                color = if (metrics.valetMode) StatusWarning else StatusDisabled
            )
            
            // Low battery indicator
            StatusLabel(
                label = "Battery",
                value = if (metrics.batteryLow) "Low" else "OK",
                color = if (metrics.batteryLow) StatusError else StatusOk
            )
            
            // Error codes
            StatusLabel(
                label = "Errors",
                value = if (metrics.errorCodes.isEmpty()) "None" else "${metrics.errorCodes.size}",
                color = if (metrics.errorCodes.isEmpty()) StatusOk else StatusError
            )
        }
    }
}

/**
 * Individual status label component
 */
@Composable
fun StatusLabel(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .background(
                    color = color,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}
