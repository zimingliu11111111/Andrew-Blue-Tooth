package com.example.bluetoothremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluetoothremote.ui.theme.BluetoothremoteTheme
import com.example.bluetoothremote.ui.components.CleanStatusIndicator

class CleanTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothremoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CleanRemoteApp()
                }
            }
        }
    }
}

@Composable
fun CleanRemoteApp() {
    var pressedKey by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }
    var isLearningMode by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Bluetooth Remote",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Use cleaned status indicator
        CleanStatusIndicator(
            isConnected = isConnected,
            deviceName = if (isConnected) "Test Device" else "No Device",
            pressedKey = pressedKey,
            isLearningMode = isLearningMode
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Remote control button area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up button
            CleanButton("Up") { pressedKey = "Up" }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Left and right buttons
            Row {
                CleanButton("Left") { pressedKey = "Left" }
                Spacer(modifier = Modifier.width(80.dp))
                CleanButton("Right") { pressedKey = "Right" }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Down button
            CleanButton("Down") { pressedKey = "Down" }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Function buttons
            Row {
                CleanButton("Fn1", Color(0xFF4CAF50)) { pressedKey = "Function1" }
                Spacer(modifier = Modifier.width(12.dp))
                CleanButton("Fn2", Color(0xFF4CAF50)) { pressedKey = "Function2" }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row {
                CleanButton("Fn3", Color(0xFF4CAF50)) { pressedKey = "Function3" }
                Spacer(modifier = Modifier.width(12.dp))
                CleanButton("Fn4", Color(0xFF4CAF50)) { pressedKey = "Function4" }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Control buttons
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        isConnected = !isConnected
                        if (!isConnected) {
                            pressedKey = ""
                            isLearningMode = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isConnected) "Disconnect" else "Simulate Connect")
                }
                
                Button(
                    onClick = { pressedKey = "" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Clear Status")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { 
                    if (isConnected) {
                        isLearningMode = !isLearningMode
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLearningMode) Color.Red else MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text(if (isLearningMode) "Exit Learning Mode" else "Enter Learning Mode")
            }
        }
    }
}

@Composable
fun CleanButton(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = CircleShape,
        contentPadding = PaddingValues(4.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}