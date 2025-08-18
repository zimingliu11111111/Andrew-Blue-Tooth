package com.example.bluetoothremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bluetoothremote.ui.theme.BluetoothremoteTheme
import android.util.Log

class SimpleMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BluetoothremoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SimpleRemoteApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleRemoteApp(modifier: Modifier = Modifier) {
    var connectionStatus by remember { mutableStateOf("未连接") }
    var pressedKey by remember { mutableStateOf("") }
    var isAnyKeyPressed by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // LED指示灯
        Box(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (isAnyKeyPressed) Color.Red else Color.Red.copy(alpha = 0.3f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isAnyKeyPressed) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.8f))
                )
            }
        }
        
        // 状态栏
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when(connectionStatus) {
                                "已连接" -> Icons.Filled.BluetoothConnected
                                "连接中" -> Icons.Filled.Bluetooth
                                else -> Icons.Filled.BluetoothDisabled
                            },
                            contentDescription = "连接状态",
                            tint = when(connectionStatus) {
                                "已连接" -> Color.Green
                                "连接中" -> Color.Blue
                                else -> Color.Gray
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = connectionStatus,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Text(
                        text = "蓝牙遥控器",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                if (pressedKey.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "当前按键: $pressedKey",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 遥控器区域
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 上键
            SimpleRemoteButton(
                text = "上",
                icon = Icons.Filled.KeyboardArrowUp,
                color = MaterialTheme.colorScheme.primary,
                onPress = {
                    pressedKey = "上键"
                    isAnyKeyPressed = true
                    Log.d("Demo", "TX -> 0x01 (K1)")
                },
                onRelease = {
                    pressedKey = ""
                    isAnyKeyPressed = false
                    Log.d("Demo", "TX -> 0x00 (release)")
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 左右键
            Row {
                SimpleRemoteButton(
                    text = "左",
                    icon = Icons.Filled.KeyboardArrowLeft,
                    color = MaterialTheme.colorScheme.primary,
                    onPress = {
                        pressedKey = "左键"
                        isAnyKeyPressed = true
                        Log.d("Demo", "TX -> 0x04 (K3)")
                    },
                    onRelease = {
                        pressedKey = ""
                        isAnyKeyPressed = false
                        Log.d("Demo", "TX -> 0x00 (release)")
                    }
                )
                Spacer(modifier = Modifier.width(94.dp))
                SimpleRemoteButton(
                    text = "右",
                    icon = Icons.Filled.KeyboardArrowRight,
                    color = MaterialTheme.colorScheme.primary,
                    onPress = {
                        pressedKey = "右键"
                        isAnyKeyPressed = true
                        Log.d("Demo", "TX -> 0x08 (K4)")
                    },
                    onRelease = {
                        pressedKey = ""
                        isAnyKeyPressed = false
                        Log.d("Demo", "TX -> 0x00 (release)")
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 下键
            SimpleRemoteButton(
                text = "下",
                icon = Icons.Filled.KeyboardArrowDown,
                color = MaterialTheme.colorScheme.primary,
                onPress = {
                    pressedKey = "下键"
                    isAnyKeyPressed = true
                    Log.d("Demo", "TX -> 0x02 (K2)")
                },
                onRelease = {
                    pressedKey = ""
                    isAnyKeyPressed = false
                    Log.d("Demo", "TX -> 0x00 (release)")
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 功能键
            Row {
                SimpleRemoteButton(
                    text = "功能1",
                    icon = Icons.Filled.Star,
                    color = Color(0xFF4CAF50),
                    onPress = {
                        pressedKey = "功能1"
                        isAnyKeyPressed = true
                        Log.d("Demo", "TX -> 0x10 (K5)")
                    },
                    onRelease = {
                        pressedKey = ""
                        isAnyKeyPressed = false
                        Log.d("Demo", "TX -> 0x00 (release)")
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                SimpleRemoteButton(
                    text = "功能2",
                    icon = Icons.Filled.Star,
                    color = Color(0xFF4CAF50),
                    onPress = {
                        pressedKey = "功能2"
                        isAnyKeyPressed = true
                        Log.d("Demo", "TX -> 0x20 (K6)")
                    },
                    onRelease = {
                        pressedKey = ""
                        isAnyKeyPressed = false
                        Log.d("Demo", "TX -> 0x00 (release)")
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                SimpleRemoteButton(
                    text = "功能3",
                    icon = Icons.Filled.Settings,
                    color = Color(0xFF4CAF50),
                    onPress = {
                        pressedKey = "功能3"
                        isAnyKeyPressed = true
                        Log.d("Demo", "TX -> 0x40 (K7)")
                    },
                    onRelease = {
                        pressedKey = ""
                        isAnyKeyPressed = false
                        Log.d("Demo", "TX -> 0x00 (release)")
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                SimpleRemoteButton(
                    text = "功能4",
                    icon = Icons.Filled.Settings,
                    color = Color(0xFF4CAF50),
                    onPress = {
                        pressedKey = "功能4"
                        isAnyKeyPressed = true
                        Log.d("Demo", "TX -> 0x80 (K8)")
                    },
                    onRelease = {
                        pressedKey = ""
                        isAnyKeyPressed = false
                        Log.d("Demo", "TX -> 0x00 (release)")
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 连接按钮
        Button(
            onClick = {
                connectionStatus = when(connectionStatus) {
                    "未连接" -> "连接中"
                    "连接中" -> "已连接"
                    else -> "未连接"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when(connectionStatus) {
                    "未连接" -> "开始连接"
                    "连接中" -> "连接中..."
                    else -> "断开连接"
                }
            )
        }
    }
}

@Composable
fun SimpleRemoteButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .then(
                if (isPressed) {
                    Modifier.padding(2.dp)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                isPressed = !isPressed
                if (isPressed) {
                    onPress()
                } else {
                    onRelease()
                }
            },
            modifier = Modifier.fillMaxSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPressed) color.copy(alpha = 0.8f) else color
            ),
            shape = CircleShape,
            contentPadding = PaddingValues(4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(20.dp),
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
}

@Preview(showBackground = true)
@Composable
fun SimpleRemoteAppPreview() {
    BluetoothremoteTheme {
        SimpleRemoteApp()
    }
}