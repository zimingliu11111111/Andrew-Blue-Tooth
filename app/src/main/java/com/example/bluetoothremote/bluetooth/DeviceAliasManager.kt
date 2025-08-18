package com.example.bluetoothremote.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.SharedPreferences

/**
 * 管理设备别名：按连接顺序分配 BLUE1/BLUE2/... 并基于 MAC 地址持久化。
 */
class DeviceAliasManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("device_alias_prefs", Context.MODE_PRIVATE)

    private val aliasPrefix = "BLUE"

    fun getAliasFor(device: BluetoothDevice): String {
        val key = keyFor(device.address)
        val existing = prefs.getString(key, null)
        return existing ?: assignAlias(device.address)
    }

    fun registerOnConnect(device: BluetoothDevice): String {
        // 确保在连接时已有别名
        return getAliasFor(device)
    }

    private fun assignAlias(address: String): String {
        val nextIndex = prefs.getInt(KEY_NEXT_INDEX, 1)
        val alias = "$aliasPrefix$nextIndex"
        prefs.edit()
            .putString(keyFor(address), alias)
            .putInt(KEY_NEXT_INDEX, nextIndex + 1)
            .apply()
        return alias
    }

    /**
     * 仅查询是否已有别名，不会进行分配。
     */
    fun peekAliasFor(device: BluetoothDevice): String? {
        return prefs.getString(keyFor(device.address), null)
    }

    private fun keyFor(address: String): String = "alias_$address"

    companion object {
        private const val KEY_NEXT_INDEX = "next_index"
    }
}

