package com.example.bluetoothremote.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetoothremote.bluetooth.BluetoothLeManager
import com.example.bluetoothremote.protocol.RemoteProtocol
import com.example.bluetoothremote.learning.LearningController
import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
// Log 导入已移除

class RemoteViewModel(
    private val bluetoothManager: BluetoothLeManager,
    private val protocol: RemoteProtocol,
    private val learningController: LearningController,
    private val context: Context
) : ViewModel() {
    
    // UI state
    private val _uiState = MutableStateFlow(RemoteUiState())
    val uiState: StateFlow<RemoteUiState> = _uiState.asStateFlow()
    
    // Set of pressed keys
    private val pressedKeys = mutableSetOf<String>()
    
    // 连接状态跟踪
    private var isUserInitiatedDisconnect = false
    private var isConnectingInProgress = false
    private var isAppJustStarted = true  // 应用刚启动标志
    private var isAutoConnecting = false  // 正在自动连接标志
    
    data class RemoteUiState(
        val connectionState: BluetoothLeManager.ConnectionState = BluetoothLeManager.ConnectionState.DISCONNECTED,
        val scannedDevices: List<BluetoothDevice> = emptyList(),
        val connectedDeviceName: String? = null,
        val isScanning: Boolean = false,
        val isLearningMode: Boolean = false,
        val receivedKeys: Set<String> = emptySet(),
        val pressedKeys: Set<String> = emptySet(),
        val signalStrength: Int? = null,
        val batteryLevel: Int? = null,
        val snackbarMessage: String? = null,
        val reconnectAttempt: Int = 0,
        // 重新连接相关状态
        val showPasswordRetryDialog: Boolean = false,
        val retryDeviceInfo: Pair<BluetoothDevice, String>? = null, // 设备和旧密码
        val isRetryAfterFailure: Boolean = false,
        val isReconnecting: Boolean = false
    )

    
    init {
        // Listen to Bluetooth connection state
        viewModelScope.launch {
            bluetoothManager.connectionState.collect { state ->
                updateUiState { copy(connectionState = state) }
            }
        }
        
        // Listen to scanned devices
        viewModelScope.launch {
            bluetoothManager.scannedDevices.collect { devices ->
                updateUiState { copy(scannedDevices = devices) }
            }
        }
        
        // Listen to scanning state
        viewModelScope.launch {
            bluetoothManager.isScanning.collect { scanning ->
                updateUiState { copy(isScanning = scanning) }
            }
        }
        
        // Listen to learning mode
        viewModelScope.launch {
            protocol.isLearningMode.collect { learning ->
                updateUiState { copy(isLearningMode = learning) }
            }
        }
        
        // Listen to received key data
        viewModelScope.launch {
            protocol.receivedKeyData.collect { keys ->
                updateUiState { copy(receivedKeys = keys) }
            }
        }
        // Listen to error messages
        viewModelScope.launch {
            bluetoothManager.errorMessage.collect { msg ->
                if (msg != null) {
                    updateUiState { copy(snackbarMessage = msg) }
                }
            }
        }

        // Listen to reconnect attempts
        viewModelScope.launch {
            bluetoothManager.reconnectAttemptsFlow.collect { count ->
                updateUiState { copy(reconnectAttempt = count) }
            }
        }
        
        // Listen to signal strength
        viewModelScope.launch {
            bluetoothManager.signalStrength.collect { rssi ->
                updateUiState { copy(signalStrength = rssi) }
            }
        }
    }
    
    // Start scanning devices
    fun startScanning() {
        bluetoothManager.startScanning()
    }
    
    // Stop scanning devices
    fun stopScanning() {
        bluetoothManager.stopScanning()
    }
    
    // Connect to device
    fun connectToDevice(device: BluetoothDevice, password: String) {
        val deviceName = try {
            device.name ?: "Unknown Device"
        } catch (e: SecurityException) {
            "Unknown Device"
        }
        
        // 监听连接状态，检测连接失败
        viewModelScope.launch {
            val passwordManager = com.example.bluetoothremote.password.PasswordManager(context)
            
            // 重置状态标志
            isUserInitiatedDisconnect = false
            isConnectingInProgress = true
            
            // 启动连接
            bluetoothManager.connectToDevice(device, password)
            updateUiState { copy(connectedDeviceName = deviceName) }
            
            // 保存设备信息（连接尝试时就保存，成功后会自动更新最后连接时间）
            passwordManager.saveDeviceInfo(device.address, deviceName)
            
            // 监听连接状态变化
            var timeoutJob: Job? = null
            bluetoothManager.connectionState.collect { state ->
                when (state) {
                    BluetoothLeManager.ConnectionState.CONNECTED -> {
                        timeoutJob?.cancel()
                        isConnectingInProgress = false
                        // 连接成功，更新密码和连接时间
                        passwordManager.updateDevicePassword(device.address, password)
                        return@collect
                    }
                    BluetoothLeManager.ConnectionState.CONNECTING -> {
                        // 设置连接超时检测
                        timeoutJob = launch {
                            kotlinx.coroutines.delay(15000) // 15秒超时
                            // 连接超时，直接弹出重试对话框
                            if (isConnectingInProgress) {
                                isConnectingInProgress = false
                                showPasswordRetryDialog(device, password)
                            }
                        }
                    }
                    BluetoothLeManager.ConnectionState.DISCONNECTED -> {
                        timeoutJob?.cancel()
                        // 连接失败时直接弹出重试对话框（基于蓝牙状态，不再依赖密码比较）
                        if (isConnectingInProgress && !isUserInitiatedDisconnect) {
                            isConnectingInProgress = false
                            showPasswordRetryDialog(device, password)
                        } else {
                            isConnectingInProgress = false
                        }
                        return@collect
                    }
                    else -> {
                        // 其他状态继续等待
                    }
                }
            }
        }
    }
    
    // Disconnect
    fun disconnect() {
        isUserInitiatedDisconnect = true
        bluetoothManager.disconnect()
        updateUiState { copy(connectedDeviceName = null) }
    }
    
    // Change password (only when connected)
    fun changeDevicePassword(currentPassword: String, newPassword: String): Boolean {
        return bluetoothManager.changePasswordWithVerification(currentPassword, newPassword)
    }

    fun clearSnackbar() {
        updateUiState { copy(snackbarMessage = null) }
    }
    
    // Save device password
    fun saveDevicePassword(deviceAddress: String, password: String) {
        val passwordManager = com.example.bluetoothremote.password.PasswordManager(context)
        passwordManager.saveDevicePassword(deviceAddress, password)
    }
    
    // Key pressed
    fun onKeyPressed(key: String) {
        pressedKeys.add(key)
        val snapshot = pressedKeys.toSet()
        updateUiState { copy(pressedKeys = snapshot) }
        protocol.beginContinuousSend(snapshot)
    }
    
    // Key released
    fun onKeyReleased(key: String) {
        pressedKeys.remove(key)
        updateUiState { copy(pressedKeys = pressedKeys.toSet()) }
        
        if (pressedKeys.isEmpty()) {
            protocol.sendKeyRelease()
        } else {
            protocol.beginContinuousSend(pressedKeys)
        }
    }

    
    // Enter learning mode
    fun enterLearningMode() {
        protocol.enterLearningMode()
    }
    
    // Exit learning mode
    fun exitLearningMode() {
        protocol.exitLearningMode()
    }
    
    // Check if Bluetooth is enabled
    fun isBluetoothEnabled(): Boolean {
        return bluetoothManager.isBluetoothEnabled()
    }
    
    private fun updateUiState(update: RemoteUiState.() -> RemoteUiState) {
        _uiState.value = _uiState.value.update()
    }
    
    /**
     * 自动连接最近连接的设备（仅在应用启动时）
     */
    fun tryAutoConnect() {
        if (!isAppJustStarted) {
            android.util.Log.d("RemoteViewModel", "非首次启动，跳过自动连接")
            return // 只在应用刚启动时自动连接
        }
        
        android.util.Log.d("RemoteViewModel", "开始自动连接流程")
        isAppJustStarted = false // 立即标记已不是刚启动状态，避免重复调用
        
        viewModelScope.launch {
            try {
                val passwordManager = com.example.bluetoothremote.password.PasswordManager(context)
                val lastDevice = passwordManager.getLastConnectedDevice()
                
                if (lastDevice != null) {
                    // 有保存的设备，尝试自动连接
                    isAutoConnecting = true
                    android.util.Log.d("RemoteViewModel", "尝试自动连接设备: ${lastDevice.name}")
                    startAutoConnectScan(lastDevice.address, lastDevice.password)
                } else {
                    // 首次使用，没有保存的设备，直接开始扫描
                    android.util.Log.d("RemoteViewModel", "首次使用，直接开始扫描")
                    startScanning()
                }
            } catch (e: Exception) {
                // 自动连接失败，静默处理
                android.util.Log.d("RemoteViewModel", "自动连接失败: ${e.message}")
                isAutoConnecting = false
            }
        }
    }
    
    /**
     * 启动扫描以查找需要自动连接的设备
     */
    private fun startAutoConnectScan(targetAddress: String, password: String) {
        if (!hasBluetoothPermissions()) {
            isAutoConnecting = false
            return
        }
        
        viewModelScope.launch {
            bluetoothManager.startScanning()
            
            // 设置超时时间，如果10秒内没找到设备则停止
            val scanTimeoutJob = launch {
                kotlinx.coroutines.delay(10000)
                if (isAutoConnecting) {
                    android.util.Log.d("RemoteViewModel", "自动连接扫描超时，未找到目标设备")
                    bluetoothManager.stopScanning()
                    isAutoConnecting = false
                }
            }
            
            // 监听扫描结果，寻找目标设备
            bluetoothManager.scannedDevices.collect { devices ->
                val targetDevice = devices.find { it.address == targetAddress }
                if (targetDevice != null && isAutoConnecting) {
                    scanTimeoutJob.cancel()
                    // 找到目标设备，停止扫描并连接
                    bluetoothManager.stopScanning()
                    android.util.Log.d("RemoteViewModel", "找到目标设备，开始自动连接")
                    
                    // 使用特殊的自动连接方法，连接失败时弹出重试对话框
                    connectToDeviceWithAutoRetry(targetDevice, password)
                    return@collect
                }
            }
        }
    }
    
    /**
     * 自动连接设备，失败时弹出重试对话框
     */
    private fun connectToDeviceWithAutoRetry(device: BluetoothDevice, password: String) {
        val deviceName = try {
            device.name ?: "Unknown Device"
        } catch (e: SecurityException) {
            "Unknown Device"
        }
        
        // 重置连接状态标志
        isUserInitiatedDisconnect = false
        isConnectingInProgress = true
        
        // 启动连接
        bluetoothManager.connectToDevice(device, password)
        updateUiState { copy(connectedDeviceName = deviceName) }
        
        viewModelScope.launch {
            // 监听连接状态变化
            var connectionTimeoutJob: Job? = null
            connectionTimeoutJob = launch {
                kotlinx.coroutines.delay(15000) // 15秒超时
                if (isConnectingInProgress && isAutoConnecting) {
                    android.util.Log.d("RemoteViewModel", "自动连接超时，弹出密码重试对话框")
                    isConnectingInProgress = false
                    isAutoConnecting = false
                    showPasswordRetryDialog(device, password)
                }
            }
            
            bluetoothManager.connectionState.collect { state ->
                when (state) {
                    BluetoothLeManager.ConnectionState.CONNECTED -> {
                        connectionTimeoutJob?.cancel()
                        isConnectingInProgress = false
                        isAutoConnecting = false
                        android.util.Log.d("RemoteViewModel", "自动连接成功")
                        
                        // 连接成功，更新密码和连接时间
                        val passwordManager = com.example.bluetoothremote.password.PasswordManager(context)
                        passwordManager.updateDevicePassword(device.address, password)
                        return@collect
                    }
                    BluetoothLeManager.ConnectionState.DISCONNECTED -> {
                        if (isConnectingInProgress && isAutoConnecting) {
                            connectionTimeoutJob?.cancel()
                            isConnectingInProgress = false
                            isAutoConnecting = false
                            android.util.Log.d("RemoteViewModel", "自动连接失败，弹出密码重试对话框")
                            showPasswordRetryDialog(device, password)
                            return@collect
                        }
                    }
                    else -> {
                        // 其他状态继续等待
                    }
                }
            }
        }
    }
    
    /**
     * 检查是否具有蓝牙权限
     */
    private fun hasBluetoothPermissions(): Boolean {
        val hasLocationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context, 
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            hasLocationPermission &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            hasLocationPermission &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.BLUETOOTH
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.BLUETOOTH_ADMIN
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    // 重新连接相关方法
    
    /**
     * 连接失败时显示密码重试对话框
     */
    fun showPasswordRetryDialog(device: BluetoothDevice, oldPassword: String) {
        updateUiState {
            copy(
                showPasswordRetryDialog = true,
                retryDeviceInfo = device to oldPassword,
                isRetryAfterFailure = false
            )
        }
    }
    
    /**
     * 使用新密码重新连接
     */
    fun retryConnectWithNewPassword(newPassword: String) {
        val retryInfo = _uiState.value.retryDeviceInfo
        if (retryInfo != null) {
            val (device, oldPassword) = retryInfo
            
            updateUiState {
                copy(
                    isReconnecting = true,
                    showPasswordRetryDialog = true // 保持对话框显示
                )
            }
            
            viewModelScope.launch {
                try {
                    // 重置连接状态标志
                    isUserInitiatedDisconnect = false
                    isConnectingInProgress = true
                    
                    // 直接调用BluetoothManager连接，避免重复监听
                    bluetoothManager.connectToDevice(device, newPassword)
                    
                    // 设置超时检测
                    val timeoutJob = launch {
                        kotlinx.coroutines.delay(15000) // 15秒超时
                        if (isConnectingInProgress) {
                            isConnectingInProgress = false
                            updateUiState {
                                copy(
                                    isReconnecting = false,
                                    isRetryAfterFailure = true,
                                    showPasswordRetryDialog = true, // 保持对话框开启
                                    snackbarMessage = "连接超时，请检查密码或删除设备"
                                )
                            }
                        }
                    }
                    
                    // 监听连接状态，但只监听一次结果
                    var hasProcessedResult = false
                    bluetoothManager.connectionState.collect { state ->
                        if (!hasProcessedResult) {
                            when (state) {
                                BluetoothLeManager.ConnectionState.CONNECTED -> {
                                    hasProcessedResult = true
                                    timeoutJob.cancel()
                                    isConnectingInProgress = false
                                    
                                    // 连接成功，更新密码
                                    val passwordManager = com.example.bluetoothremote.password.PasswordManager(context)
                                    passwordManager.updateDevicePassword(device.address, newPassword)
                                    
                                    // 关闭对话框
                                    updateUiState {
                                        copy(
                                            showPasswordRetryDialog = false,
                                            retryDeviceInfo = null,
                                            isReconnecting = false,
                                            isRetryAfterFailure = false,
                                            snackbarMessage = "连接成功，密码已更新"
                                        )
                                    }
                                    return@collect
                                }
                                BluetoothLeManager.ConnectionState.DISCONNECTED -> {
                                    if (isConnectingInProgress) {
                                        hasProcessedResult = true
                                        timeoutJob.cancel()
                                        isConnectingInProgress = false
                                        
                                        // 连接失败，保持重试对话框开启，允许用户继续重试
                                        updateUiState {
                                            copy(
                                                isReconnecting = false,
                                                isRetryAfterFailure = true,
                                                showPasswordRetryDialog = true, // 保持对话框开启
                                                snackbarMessage = "连接失败，请检查密码或删除设备"
                                            )
                                        }
                                        return@collect
                                    }
                                }
                                else -> {
                                    // 连接中，保持状态
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    isConnectingInProgress = false
                    updateUiState {
                        copy(
                            isReconnecting = false,
                            isRetryAfterFailure = true,
                            showPasswordRetryDialog = true, // 保持对话框开启
                            snackbarMessage = "重新连接失败: ${e.message}"
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 删除连接失败的设备
     */
    fun deleteFailedDevice() {
        val retryInfo = _uiState.value.retryDeviceInfo
        if (retryInfo != null) {
            val (device, _) = retryInfo
            val passwordManager = com.example.bluetoothremote.password.PasswordManager(context)
            passwordManager.resetDevicePassword(device.address)
            
            updateUiState {
                copy(
                    showPasswordRetryDialog = false,
                    retryDeviceInfo = null,
                    isReconnecting = false,
                    isRetryAfterFailure = false,
                    snackbarMessage = "设备已删除，请重新扫描添加"
                )
            }
        }
    }
    
    /**
     * 关闭密码重试对话框
     */
    fun dismissPasswordRetryDialog() {
        updateUiState {
            copy(
                showPasswordRetryDialog = false,
                retryDeviceInfo = null,
                isReconnecting = false,
                isRetryAfterFailure = false
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        protocol.cleanup()
        bluetoothManager.cleanup()
    }
}