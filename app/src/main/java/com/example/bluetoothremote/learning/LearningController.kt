package com.example.bluetoothremote.learning

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LearningController(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("remote_learning", Context.MODE_PRIVATE)
    
    private val _learningState = MutableStateFlow(LearningState.IDLE)
    val learningState: StateFlow<LearningState> = _learningState
    
    private val _learnedRemotes = MutableStateFlow(loadLearnedRemotes())
    val learnedRemotes: StateFlow<List<RemoteDevice>> = _learnedRemotes
    
    private val maxRemoteCount = 4
    private var currentRemoteIndex = 0
    
    enum class LearningState {
        IDLE,
        LEARNING,
        LEARNED_SUCCESS,
        LEARNED_FULL
    }
    
    data class RemoteDevice(
        val id: String,
        val name: String,
        val learnedTime: Long,
        val keyMappings: Map<String, Byte> = emptyMap()
    )
    
    fun startLearning(): Boolean {
        return if (_learningState.value == LearningState.IDLE) {
            _learningState.value = LearningState.LEARNING
            true
        } else {
            false
        }
    }
    
    fun stopLearning() {
        _learningState.value = LearningState.IDLE
    }
    
    fun learnRemoteDevice(deviceId: String, deviceName: String = "Remote${currentRemoteIndex + 1}"): Boolean {
        if (_learningState.value != LearningState.LEARNING) {
            return false
        }
        
        val currentRemotes = _learnedRemotes.value.toMutableList()
        
        // 如果已学习4个遥控器，从第一个开始循环覆盖
        if (currentRemotes.size >= maxRemoteCount) {
            currentRemoteIndex = 0
            currentRemotes[currentRemoteIndex] = RemoteDevice(
                id = deviceId,
                name = deviceName,
                learnedTime = System.currentTimeMillis()
            )
            _learningState.value = LearningState.LEARNED_FULL
        } else {
            // 添加新的遥控器
            currentRemoteIndex = currentRemotes.size
            val newRemote = RemoteDevice(
                id = deviceId,
                name = deviceName,
                learnedTime = System.currentTimeMillis()
            )
            currentRemotes.add(newRemote)
            _learningState.value = LearningState.LEARNED_SUCCESS
        }
        
        _learnedRemotes.value = currentRemotes
        saveLearnedRemotes(currentRemotes)
        currentRemoteIndex = (currentRemoteIndex + 1) % maxRemoteCount
        
        return true
    }
    
    fun isRemoteLearned(deviceId: String): Boolean {
        return _learnedRemotes.value.any { it.id == deviceId }
    }
    
    fun getRemoteDevice(deviceId: String): RemoteDevice? {
        return _learnedRemotes.value.find { it.id == deviceId }
    }
    
    fun clearAllRemotes() {
        _learnedRemotes.value = emptyList()
        sharedPreferences.edit().clear().apply()
        currentRemoteIndex = 0
        _learningState.value = LearningState.IDLE
    }
    
    fun removeRemote(deviceId: String): Boolean {
        val currentRemotes = _learnedRemotes.value.toMutableList()
        val removed = currentRemotes.removeAll { it.id == deviceId }
        if (removed) {
            _learnedRemotes.value = currentRemotes
            saveLearnedRemotes(currentRemotes)
        }
        return removed
    }
    
    private fun loadLearnedRemotes(): List<RemoteDevice> {
        val remotesJson = sharedPreferences.getString("learned_remotes", null)
        return if (remotesJson != null) {
            try {
                // 简化版本：从SharedPreferences加载
                val remoteCount = sharedPreferences.getInt("remote_count", 0)
                (0 until remoteCount).mapNotNull { index ->
                    val id = sharedPreferences.getString("remote_${index}_id", null)
                    val name = sharedPreferences.getString("remote_${index}_name", null)
                    val time = sharedPreferences.getLong("remote_${index}_time", 0L)
                    if (id != null && name != null) {
                        RemoteDevice(id, name, time)
                    } else null
                }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    private fun saveLearnedRemotes(remotes: List<RemoteDevice>) {
        val editor = sharedPreferences.edit()
        editor.putInt("remote_count", remotes.size)
        remotes.forEachIndexed { index, remote ->
            editor.putString("remote_${index}_id", remote.id)
            editor.putString("remote_${index}_name", remote.name)
            editor.putLong("remote_${index}_time", remote.learnedTime)
        }
        editor.apply()
    }
}