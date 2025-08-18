package com.example.bluetoothremote.ui.components

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PasswordInputDialog(
    device: BluetoothDevice,
    defaultPassword: String = "123456",
    onConnect: (password: String, remember: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var password by remember { mutableStateOf(defaultPassword) }
    var rememberPassword by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Connect Device",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Device: ${try { device.name ?: device.address } catch (e: SecurityException) { device.address }}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        if (it.length <= 6) {
                            password = it
                            showError = false
                        }
                    },
                    label = { Text("连接密码 (6位)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError,
                    supportingText = if (showError) {
                        { Text("密码必须为6位") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // 记住密码选项
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberPassword,
                        onCheckedChange = { rememberPassword = it }
                    )
                    Text(
                        text = "记住此设备密码",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            if (password.length == 6) {
                                onConnect(password, rememberPassword)
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("连接")
                    }
                }
            }
        }
    }
}