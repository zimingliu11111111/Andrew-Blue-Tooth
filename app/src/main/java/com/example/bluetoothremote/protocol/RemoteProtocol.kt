package com.example.bluetoothremote.protocol

import com.example.bluetoothremote.bluetooth.BluetoothLeManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
// Log 导入已移除

class RemoteProtocol(private val bluetoothManager: BluetoothLeManager) {
    
    private val _receivedKeyData = MutableStateFlow<Set<String>>(emptySet())
    val receivedKeyData: StateFlow<Set<String>> = _receivedKeyData
    
    private val _isLearningMode = MutableStateFlow(false)
    val isLearningMode: StateFlow<Boolean> = _isLearningMode
    
    private var queryJob: Job? = null
    private var sendJob: Job? = null
    private var hasRequestedKeyAccept: Boolean = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        // 主机指令
        const val CMD_REQUEST_KEY = 0xAA.toByte()
        const val CMD_ENTER_LEARNING = 0x33.toByte()
        const val CMD_EXIT_LEARNING = 0xCC.toByte()
        
        // 按键码值
        const val KEY_UP = 0x01.toByte()        // K1
        const val KEY_DOWN = 0x02.toByte()      // K2
        const val KEY_LEFT = 0x04.toByte()      // K3
        const val KEY_RIGHT = 0x08.toByte()     // K4
        const val KEY_FUNC1 = 0x10.toByte()     // K5
        const val KEY_FUNC2 = 0x20.toByte()     // K6
        const val KEY_FUNC3 = 0x40.toByte()     // K7
        const val KEY_FUNC4 = 0x80.toByte()     // K8
        
        const val KEY_RELEASE = 0x00.toByte()   // 按键释放
        
        // 50ms发送间隔（按下期间连续发送）
        private const val SEND_INTERVAL_MS = 50L
        // 启用查询模式：连接后每 75ms 发送 0xAA，设备回报状态
        private const val USE_QUERY_MODE = false
        
        // 按键组合映射
        val keyMap = mapOf(
            "K1" to KEY_UP,
            "K2" to KEY_DOWN,
            "K3" to KEY_LEFT,
            "K4" to KEY_RIGHT,
            "K5" to KEY_FUNC1,
            "K6" to KEY_FUNC2,
            "K7" to KEY_FUNC3,
            "K8" to KEY_FUNC4
        )
    }
    
    init {
        // 监听蓝牙接收数据
        coroutineScope.launch {
            bluetoothManager.receivedData.collect { data ->
                data?.let { processReceivedData(it) }
            }
        }
        
        // 查询模式关闭时不启动0xAA轮询
        if (USE_QUERY_MODE) {
            coroutineScope.launch {
                bluetoothManager.connectionState.collect { state ->
                    if (state == BluetoothLeManager.ConnectionState.CONNECTED) {
                        startKeyQueryLoop()
                    } else {
                        stopKeyQueryLoop()
                    }
                }
            }
        }
    }
    
    /**
     * 开始75ms间隔的按键查询循环
     */
    private fun startKeyQueryLoop() {
        queryJob?.cancel()
        if (!USE_QUERY_MODE) return
        queryJob = coroutineScope.launch {
            while (isActive) {
                // 发送查询指令
                bluetoothManager.writeData(byteArrayOf(CMD_REQUEST_KEY))
                delay(75L)
            }
        }
    }
    
    /**
     * 停止按键查询循环
     */
    private fun stopKeyQueryLoop() {
        queryJob?.cancel()
        queryJob = null
        _receivedKeyData.value = emptySet()
    }
    
    /**
     * 进入学习模式
     */
    fun enterLearningMode(): Boolean {
        if (bluetoothManager.connectionState.value != BluetoothLeManager.ConnectionState.CONNECTED) {
            return false
        }
        
        val success = bluetoothManager.writeData(byteArrayOf(CMD_ENTER_LEARNING))
        if (success) {
            _isLearningMode.value = true
        }
        return success
    }
    
    /**
     * 退出学习模式
     */
    fun exitLearningMode(): Boolean {
        if (bluetoothManager.connectionState.value != BluetoothLeManager.ConnectionState.CONNECTED) {
            return false
        }
        
        val success = bluetoothManager.writeData(byteArrayOf(CMD_EXIT_LEARNING))
        if (success) {
            _isLearningMode.value = false
        }
        return success
    }
    
    /**
     * 发送按键数据
     */
    fun beginContinuousSend(keys: Set<String>): Boolean {
        // 重启发送任务
        sendJob?.cancel()
        val composite = calculateCompositeKey(keys)
        // 开始发送按键组合
        sendJob = coroutineScope.launch {
            while (isActive) {
                // 发送按键数据
                bluetoothManager.writeData(byteArrayOf(composite))
                delay(SEND_INTERVAL_MS)
            }
        }
        return true
    }
    
    /**
     * 发送按键释放
     */
    fun sendKeyRelease(): Boolean {
        // 停止持续发送并发送两次0x00
        sendJob?.cancel()
        sendJob = null
        hasRequestedKeyAccept = false
        // 发送释放信号 1/2
        bluetoothManager.writeData(byteArrayOf(KEY_RELEASE))
        // 发送释放信号 2/2
        return bluetoothManager.writeData(byteArrayOf(KEY_RELEASE))
    }

    val useQueryMode: Boolean
        get() = USE_QUERY_MODE
    
    /**
     * 复合按键计算
     */
    fun calculateCompositeKey(keys: Set<String>): Byte {
        var result = 0
        keys.forEach { key ->
            keyMap[key]?.let { keyValue ->
                result = result or keyValue.toInt()
            }
        }
        return result.toByte()
    }
    
    /**
     * 解析按键数据
     */
    fun parseKeyData(data: Byte): Set<String> {
        val pressedKeys = mutableSetOf<String>()
        keyMap.forEach { (key, value) ->
            if ((data.toInt() and value.toInt()) != 0) {
                pressedKeys.add(key)
            }
        }
        return pressedKeys
    }
    
    /**
     * 处理接收到的数据
     */
    private fun processReceivedData(data: ByteArray) {
        if (data.isEmpty()) return
        
        when {
            isValidKeyData(data) -> {
                // 解析接收到的按键数据
                val keys = parseKeyData(data[0])
                _receivedKeyData.value = keys
            }
            
            isValidCommand(data) -> {
                // 处理指令响应
                when (data[0]) {
                    CMD_ENTER_LEARNING -> {
                        // 学习模式确认响应
                    }
                    CMD_EXIT_LEARNING -> {
                        // 退出学习模式确认响应
                    }
                }
            }
        }
    }
    
    /**
     * 创建指令数据包
     */
    fun createCommandPacket(command: Byte): ByteArray {
        return byteArrayOf(command)
    }
    
    /**
     * 创建按键数据包
     */
    fun createKeyPacket(keyValue: Byte): ByteArray {
        return byteArrayOf(keyValue)
    }
    
    /**
     * 验证数据包有效性
     */
    fun isValidCommand(data: ByteArray): Boolean {
        if (data.isEmpty()) return false
        val cmd = data[0]
        return cmd == CMD_REQUEST_KEY || cmd == CMD_ENTER_LEARNING || cmd == CMD_EXIT_LEARNING
    }
    
    /**
     * 验证按键数据有效性
     */
    fun isValidKeyData(data: ByteArray): Boolean {
        if (data.isEmpty()) return false
        val keyValue = data[0].toInt() and 0xFF
        return keyValue in 0..255
    }
    
    /**
     * 获取按键名称显示
     */
    fun getKeyDisplayName(key: String): String {
        return when (key) {
            "K1" -> "上"
            "K2" -> "下"
            "K3" -> "左"
            "K4" -> "右"
            "K5" -> "功能1"
            "K6" -> "功能2"
            "K7" -> "功能3"
            "K8" -> "功能4"
            else -> key
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        queryJob?.cancel()
        sendJob?.cancel()
        coroutineScope.cancel()
    }
}