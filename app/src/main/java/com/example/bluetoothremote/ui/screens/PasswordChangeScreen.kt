package com.example.bluetoothremote.ui.screens

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.bluetoothremote.bluetooth.BluetoothLeManager
import com.example.bluetoothremote.viewmodel.RemoteViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordChangeScreen(
    viewModel: RemoteViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    
    // 监听UI状态变化
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { 
            if (it.contains("密码")) {
                errorMessage = it
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("修改密码") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 设备信息
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "当前设备",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.connectedDeviceName ?: "未知设备",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "连接状态: ${uiState.connectionState.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.connectionState == BluetoothLeManager.ConnectionState.CONNECTED) 
                            Color.Green else Color.Red
                    )
                }
            }
            
            // 密码修改表单
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "密码修改",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // 当前密码输入
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { 
                            if (it.length <= 6) {
                                currentPassword = it
                                errorMessage = ""
                                successMessage = ""
                            }
                        },
                        label = { Text("当前密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    
                    // 新密码输入
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            if (it.length <= 6) {
                                newPassword = it
                                errorMessage = ""
                                successMessage = ""
                            }
                        },
                        label = { Text("新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                    
                    // 确认新密码
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            if (it.length <= 6) {
                                confirmPassword = it
                                errorMessage = ""
                                successMessage = ""
                            }
                        },
                        label = { Text("确认新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )
                }
            }
            
            // 错误或成功消息
            if (errorMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            if (successMessage.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Green.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = successMessage,
                        modifier = Modifier.padding(16.dp),
                        color = Color.Green
                    )
                }
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading
                ) {
                    Text("取消")
                }
                
                Button(
                    onClick = {
                        when {
                            uiState.connectionState != BluetoothLeManager.ConnectionState.CONNECTED -> {
                                errorMessage = "设备未连接，无法修改密码"
                            }
                            currentPassword.length != 6 -> {
                                errorMessage = "当前密码必须为6位"
                            }
                            newPassword.length != 6 -> {
                                errorMessage = "新密码必须为6位"
                            }
                            newPassword != confirmPassword -> {
                                errorMessage = "两次输入的新密码不一致"
                            }
                            else -> {
                                isLoading = true
                                errorMessage = ""
                                successMessage = ""
                                
                                val success = viewModel.changeDevicePassword(
                                    currentPassword, newPassword
                                )
                                
                                isLoading = false
                                if (success) {
                                    successMessage = "密码修改成功！"
                                    currentPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                } else {
                                    // 错误消息会通过bluetoothErrorMessage自动设置
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && uiState.connectionState == BluetoothLeManager.ConnectionState.CONNECTED
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("确认修改")
                    }
                }
            }
        }
    }
}