package com.example.bluetoothremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluetoothremote.ui.theme.BluetoothremoteTheme
import com.example.bluetoothremote.ui.components.SimpleStatusCard

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothremoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TestRemoteApp()
                }
            }
        }
    }
}

@Composable
fun TestRemoteApp() {
    var pressedKey by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "蓝牙遥控器测试",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 使用简化的状态卡片
        SimpleStatusCard(
            isConnected = isConnected,
            deviceName = if (isConnected) "测试设备" else "未连接",
            pressedKey = pressedKey
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 方向键
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TestButton(
                text = "上",
                icon = Icons.Filled.ArrowUpward,
                onClick = { pressedKey = "上键" }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                TestButton(
                    text = "左",
                    icon = Icons.Filled.ArrowBack,
                    onClick = { pressedKey = "左键" }
                )
                Spacer(modifier = Modifier.width(80.dp))
                TestButton(
                    text = "右",
                    icon = Icons.Filled.ArrowForward,
                    onClick = { pressedKey = "右键" }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TestButton(
                text = "下",
                icon = Icons.Filled.ArrowDownward,
                onClick = { pressedKey = "下键" }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 功能键
        Row {
            TestButton(
                text = "功能1",
                icon = Icons.Filled.Star,
                onClick = { pressedKey = "功能1" },
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.width(12.dp))
            TestButton(
                text = "功能2",
                icon = Icons.Filled.Star,
                onClick = { pressedKey = "功能2" },
                color = Color(0xFF4CAF50)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isConnected = !isConnected
                    if (!isConnected) pressedKey = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isConnected) "断开" else "连接")
            }
            
            Button(
                onClick = { pressedKey = "" },
                modifier = Modifier.weight(1f)
            ) {
                Text("清除")
            }
        }
    }
}

@Composable
fun TestButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = CircleShape,
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}