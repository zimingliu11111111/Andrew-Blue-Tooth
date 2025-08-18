package com.example.bluetoothremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothremote.ui.theme.BluetoothremoteTheme

class DebugMainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("DebugMainActivity", "onCreate 开始")
            
            setContent {
                BluetoothremoteTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "调试模式 - 应用正常启动",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("如果你看到这个界面，说明基础启动没问题")
                        }
                    }
                }
            }
            
            android.util.Log.d("DebugMainActivity", "onCreate 完成")
            
        } catch (e: Exception) {
            android.util.Log.e("DebugMainActivity", "onCreate 崩溃", e)
            throw e
        }
    }
}