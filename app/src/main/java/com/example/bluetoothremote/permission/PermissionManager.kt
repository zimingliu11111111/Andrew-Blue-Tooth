package com.example.bluetoothremote.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PermissionManager(private val activity: ComponentActivity) {
    
    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState
    
    data class PermissionState(
        val bluetoothPermissionsGranted: Boolean = false,
        val locationPermissionGranted: Boolean = false,
        val allPermissionsGranted: Boolean = false,
        val deniedPermissions: List<String> = emptyList()
    )
    
    // Android 12+ BLE权限
    private val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf()
    }
    
    // 经典蓝牙权限 (Android 12以下)
    private val classicBluetoothPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    } else {
        arrayOf()
    }
    
    // 位置权限 (BLE扫描需要)
    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    // 所有需要的权限
    private val allRequiredPermissions = blePermissions + classicBluetoothPermissions + locationPermissions
    
    // 权限请求启动器
    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        updatePermissionState()
    }
    
    init {
        updatePermissionState()
    }
    
    /**
     * 请求所有必需的权限
     */
    fun requestPermissions() {
        val deniedPermissions = allRequiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (deniedPermissions.isNotEmpty()) {
            permissionLauncher.launch(deniedPermissions.toTypedArray())
        }
    }
    
    /**
     * 检查特定权限是否已授予
     */
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * 检查蓝牙权限是否已授予
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 需要新的BLE权限
            blePermissions.all { isPermissionGranted(it) }
        } else {
            // Android 12以下需要经典蓝牙权限
            classicBluetoothPermissions.all { isPermissionGranted(it) }
        }
    }
    
    /**
     * 检查位置权限是否已授予
     */
    fun hasLocationPermissions(): Boolean {
        return locationPermissions.any { isPermissionGranted(it) }
    }
    
    /**
     * 检查所有权限是否已授予
     */
    fun hasAllPermissions(): Boolean {
        return hasBluetoothPermissions() && hasLocationPermissions()
    }
    
    /**
     * 获取缺失的权限列表
     */
    fun getMissingPermissions(): List<String> {
        return allRequiredPermissions.filter { permission ->
            !isPermissionGranted(permission)
        }
    }
    
    /**
     * 获取权限说明文本
     */
    fun getPermissionRationale(permission: String): String {
        return when (permission) {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT -> 
                "需要蓝牙权限以连接和控制蓝牙遥控器设备"
                
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION -> 
                "需要位置权限以扫描附近的蓝牙设备"
                
            else -> "应用需要此权限才能正常工作"
        }
    }
    
    /**
     * 更新权限状态
     */
    private fun updatePermissionState() {
        val bluetoothGranted = hasBluetoothPermissions()
        val locationGranted = hasLocationPermissions()
        val allGranted = bluetoothGranted && locationGranted
        val denied = getMissingPermissions()
        
        _permissionState.value = PermissionState(
            bluetoothPermissionsGranted = bluetoothGranted,
            locationPermissionGranted = locationGranted,
            allPermissionsGranted = allGranted,
            deniedPermissions = denied
        )
    }
    
    /**
     * 检查是否应该显示权限说明
     */
    fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
}