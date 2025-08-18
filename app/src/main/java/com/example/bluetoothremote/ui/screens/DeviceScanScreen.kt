package com.example.bluetoothremote.ui.screens

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluetoothremote.ui.components.PasswordInputDialog
import com.example.bluetoothremote.password.PasswordManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScanScreen(
    devices: List<BluetoothDevice>,
    isScanning: Boolean,
    passwordManager: PasswordManager,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceConnect: (BluetoothDevice, String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bluetooth Device Scan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = if (isScanning) onStopScan else onStartScan
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = if (isScanning) "Stop Scan" else "Start Scan"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Scanning status indicator
        if (isScanning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scanning nearby Bluetooth devices...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Device list
        if (devices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bluetooth,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isScanning) "Searching for devices..." else "No devices found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isScanning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the refresh button in the top right to start scanning",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices) { device ->
                    DeviceListItem(
                        device = device,
                        onDeviceClick = { 
                            // 检查是否有保存的密码
                            val savedPassword = passwordManager.getDevicePassword(device.address)
                            if (savedPassword != null) {
                                // 有保存密码，直接连接
                                onDeviceConnect(device, savedPassword, true)
                            } else {
                                // 没有保存密码，显示密码输入对话框
                                selectedDevice = device
                                showPasswordDialog = true
                            }
                        }
                    )
                }
            }
        }
    }
    
    // 密码输入对话框
    if (showPasswordDialog && selectedDevice != null) {
        PasswordInputDialog(
            device = selectedDevice!!,
            defaultPassword = passwordManager.getDefaultPassword(),
            onConnect = { password, remember ->
                onDeviceConnect(selectedDevice!!, password, remember)
                showPasswordDialog = false
                selectedDevice = null
            },
            onCancel = {
                showPasswordDialog = false
                selectedDevice = null
            }
        )
    }
    
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceListItem(
    device: BluetoothDevice,
    onDeviceClick: () -> Unit
) {
    // 移除别名管理，直接显示原始设备名称
    Card(
        onClick = onDeviceClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Bluetooth,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = try {
                        device.name ?: "CMT设备"
                    } catch (e: SecurityException) {
                        "CMT设备"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MAC: ${device.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 显示设备类型信息
                val deviceType = remember(device) {
                    try {
                        when (device.type) {
                            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "经典蓝牙"
                            BluetoothDevice.DEVICE_TYPE_LE -> "BLE"
                            BluetoothDevice.DEVICE_TYPE_DUAL -> "双模"
                            else -> "未知类型"
                        }
                    } catch (e: SecurityException) {
                        "权限受限"
                    }
                }
                Text(
                    text = "类型: $deviceType",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // 移除别名显示，只显示原始设备信息
            }
            
            Button(
                onClick = onDeviceClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Connect")
            }
        }
    }
}