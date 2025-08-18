package com.example.bluetoothremote.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ReconnectPasswordDialog(
    deviceName: String,
    onPasswordEntered: (String) -> Unit,
    onDeleteDevice: () -> Unit,
    onDismiss: () -> Unit,
    isReconnecting: Boolean = false,
    isRetryAfterFailure: Boolean = false
) {
    var password by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (isRetryAfterFailure) "连接再次失败" else "连接失败",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "设备: $deviceName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isRetryAfterFailure) {
                    Text(
                        text = "密码可能仍然不正确，设备可能已经硬件复位。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "请重新输入密码，或删除设备重新添加：",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "连接失败，可能是密码不正确。设备可能已经硬件复位，请输入新密码：",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        if (it.length <= 6) password = it 
                    },
                    label = { Text("设备密码 (6位)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isReconnecting,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (password.isNotEmpty() && password.length != 6) {
                    Text(
                        text = "密码必须是6位数字",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isRetryAfterFailure) {
                    TextButton(
                        onClick = onDeleteDevice,
                        enabled = !isReconnecting
                    ) {
                        Text("删除设备", color = MaterialTheme.colorScheme.error)
                    }
                }
                
                Button(
                    onClick = { onPasswordEntered(password) },
                    enabled = !isReconnecting && password.length == 6
                ) {
                    if (isReconnecting) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text("连接中...")
                        }
                    } else {
                        Text(if (isRetryAfterFailure) "重试连接" else "重新连接")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isReconnecting
            ) {
                Text("取消")
            }
        }
    )
}