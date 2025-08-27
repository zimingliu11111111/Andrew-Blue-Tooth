package com.example.bluetoothremote.password

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class PasswordManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "bluetooth_passwords"
        private const val DEFAULT_PASSWORD = "123456"
        private const val ENCRYPTION_KEY = "bluetooth_key_2024" // 简单的密钥，实际项目中应使用更安全的方式
        private const val HARDWARE_RESET_PASSWORD = "123456" // 硬件复位后的默认密码
    }
    
    /**
     * 简单加密密码（基础安全措施）
     */
    private fun encryptPassword(password: String): String {
        return try {
            val key = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encrypted = cipher.doFinal(password.toByteArray())
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        } catch (e: Exception) {
            // 加密失败时返回原密码（兼容处理）
            password
        }
    }
    
    /**
     * 简单解密密码
     */
    private fun decryptPassword(encryptedPassword: String): String {
        return try {
            val key = SecretKeySpec(ENCRYPTION_KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES")
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decoded = Base64.decode(encryptedPassword, Base64.DEFAULT)
            val decrypted = cipher.doFinal(decoded)
            String(decrypted)
        } catch (e: Exception) {
            // 解密失败时返回原字符串（兼容处理）
            encryptedPassword
        }
    }
    
    /**
     * 保存设备密码（加密存储）
     */
    fun saveDevicePassword(deviceAddress: String, password: String) {
        val encryptedPassword = encryptPassword(password)
        prefs.edit()
            .putString(deviceAddress, encryptedPassword)
            .apply()
    }
    
    /**
     * 获取设备密码（自动解密）
     */
    fun getDevicePassword(deviceAddress: String): String? {
        val encryptedPassword = prefs.getString(deviceAddress, null)
        return encryptedPassword?.let { decryptPassword(it) }
    }
    
    /**
     * 删除设备密码
     */
    fun removeDevicePassword(deviceAddress: String) {
        prefs.edit()
            .remove(deviceAddress)
            .apply()
    }
    
    /**
     * 更新设备密码（用于重新连接成功后）
     */
    fun updateDevicePassword(deviceAddress: String, newPassword: String) {
        saveDevicePassword(deviceAddress, newPassword)
        // 更新连接时间
        val currentTime = System.currentTimeMillis()
        prefs.edit()
            .putLong("last_connect_$deviceAddress", currentTime)
            .apply()
    }
    
    /**
     * 获取默认密码
     */
    fun getDefaultPassword(): String {
        return DEFAULT_PASSWORD
    }
    
    /**
     * 检查是否有保存的密码
     */
    fun hasPassword(deviceAddress: String): Boolean {
        return prefs.contains(deviceAddress)
    }
    
    /**
     * 清除所有保存的密码（安全清理）
     */
    fun clearAllPasswords() {
        prefs.edit().clear().apply()
    }
    
    /**
     * 获取所有保存密码的设备列表
     */
    fun getSavedDevices(): Set<String> {
        return prefs.all.keys.filter { 
            !it.startsWith("reset_flag_") && 
            !it.startsWith("last_connect_") && 
            !it.startsWith("device_name_") &&
            !it.startsWith("device_alias_") &&
            !it.startsWith("needs_reconfirm_")
        }.toSet()
    }
    
    /**
     * 保存设备信息（设备名称和最后连接时间）
     */
    fun saveDeviceInfo(deviceAddress: String, deviceName: String?) {
        val currentTime = System.currentTimeMillis()
        prefs.edit()
            .putString("device_name_$deviceAddress", deviceName ?: "Unknown Device")
            .putLong("last_connect_$deviceAddress", currentTime)
            .apply()
    }
    
    
    /**
     * 获取设备名称
     */
    fun getDeviceName(deviceAddress: String): String {
        return prefs.getString("device_name_$deviceAddress", "Unknown Device") ?: "Unknown Device"
    }
    
    /**
     * 获取最后连接时间
     */
    fun getLastConnectTime(deviceAddress: String): Long {
        return prefs.getLong("last_connect_$deviceAddress", 0L)
    }
    
    /**
     * 获取设备详细信息
     */
    data class DeviceInfo(
        val address: String,
        val name: String,
        val password: String,
        val lastConnectTime: Long
    )
    
    /**
     * 获取所有保存设备的详细信息
     */
    fun getAllDevicesInfo(): List<DeviceInfo> {
        val devices = getSavedDevices()
        return devices.map { address ->
            DeviceInfo(
                address = address,
                name = getDeviceName(address),
                password = getDevicePassword(address) ?: DEFAULT_PASSWORD,
                lastConnectTime = getLastConnectTime(address)
            )
        }.sortedByDescending { it.lastConnectTime } // 按最后连接时间排序
    }
    
    /**
     * 获取最近连接的设备信息（用于自动连接）
     */
    fun getLastConnectedDevice(): DeviceInfo? {
        val devices = getAllDevicesInfo()
        // 只返回有有效密码且有连接时间的设备
        return devices.firstOrNull { it.lastConnectTime > 0 && it.password.isNotEmpty() }
    }
    
    /**
     * 重置设备密码为默认值
     */
    fun resetDevicePassword(deviceAddress: String) {
        // 完全删除设备信息，包括密码、设备名、连接时间、备注等
        prefs.edit()
            .remove(deviceAddress) // 密码
            .remove("device_name_$deviceAddress")
            .remove("last_connect_$deviceAddress")
            .remove("device_alias_$deviceAddress")
            .remove("reset_flag_$deviceAddress")
            .remove("needs_reconfirm_$deviceAddress")
            .apply()
    }
    
    /**
     * 获取硬件复位密码
     */
    fun getHardwareResetPassword(): String {
        return HARDWARE_RESET_PASSWORD
    }
    
    /**
     * 智能密码建议：当连接失败时，提供可能的密码选项
     * @param deviceAddress 设备地址
     * @return 建议尝试的密码列表（按优先级排序）
     */
    fun getSuggestedPasswords(deviceAddress: String): List<String> {
        val suggestions = mutableListOf<String>()
        
        // 1. 默认密码（最常用）
        suggestions.add(DEFAULT_PASSWORD)
        
        // 2. 硬件复位密码（如果与默认密码不同）
        if (HARDWARE_RESET_PASSWORD != DEFAULT_PASSWORD) {
            suggestions.add(HARDWARE_RESET_PASSWORD)
        }
        
        // 3. 移除重复项并返回
        return suggestions.distinct()
    }
    
    /**
     * 标记设备可能已复位（连接失败时调用）
     * 这将在下次连接时优先建议复位密码
     */
    fun markDeviceAsReset(deviceAddress: String) {
        // 设置一个标记，表示设备可能已被硬件复位
        prefs.edit()
            .putBoolean("reset_flag_$deviceAddress", true)
            .apply()
    }
    
    // 已移除设备复位检查相关功能
    
    /**
     * 设备密码过期检查（可选功能）
     * 例如：定期清理长期未使用的设备密码
     */
    fun cleanupOldPasswords() {
        // 可以在这里添加基于时间戳的清理逻辑
        // 比如：超过30天未连接的设备自动清除密码
    }
}