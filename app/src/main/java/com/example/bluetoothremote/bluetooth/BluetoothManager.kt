package com.example.bluetoothremote.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.util.*

class BluetoothManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _receivedData = MutableStateFlow<ByteArray?>(null)
    val receivedData: StateFlow<ByteArray?> = _receivedData
    
    companion object {
        private const val SERVICE_NAME = "BluetoothRemoteService"
        private val SERVICE_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    }
    
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        LISTENING
    }
    
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true
    
    fun startAsServer() {
        try {
            serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(SERVICE_NAME, SERVICE_UUID)
            _connectionState.value = ConnectionState.LISTENING
            
            Thread {
                try {
                    serverSocket?.accept()?.let { socket ->
                        clientSocket = socket
                        _connectionState.value = ConnectionState.CONNECTED
                        listenForData()
                    }
                } catch (e: IOException) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }.start()
        } catch (e: SecurityException) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
    
    fun connectToDevice(device: BluetoothDevice) {
        try {
            _connectionState.value = ConnectionState.CONNECTING
            clientSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
            
            Thread {
                try {
                    clientSocket?.connect()
                    _connectionState.value = ConnectionState.CONNECTED
                    listenForData()
                } catch (e: IOException) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }.start()
        } catch (e: SecurityException) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
    
    fun sendData(data: ByteArray): Boolean {
        return try {
            clientSocket?.outputStream?.write(data)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    private fun listenForData() {
        Thread {
            val buffer = ByteArray(1024)
            while (_connectionState.value == ConnectionState.CONNECTED) {
                try {
                    val bytesRead = clientSocket?.inputStream?.read(buffer) ?: 0
                    if (bytesRead > 0) {
                        val receivedBytes = buffer.copyOf(bytesRead)
                        _receivedData.value = receivedBytes
                    }
                } catch (e: IOException) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    break
                }
            }
        }.start()
    }
    
    fun disconnect() {
        try {
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: IOException) {
            // Ignore
        } finally {
            clientSocket = null
            serverSocket = null
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
}