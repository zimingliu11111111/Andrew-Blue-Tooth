package com.example.bluetoothremote.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothremote.bluetooth.BluetoothLeManager

@Composable
fun StatusIndicator(
    connectionState: BluetoothLeManager.ConnectionState,
    deviceName: String?,
    modifier: Modifier = Modifier,
    signalStrength: Int? = null,
    isLearningMode: Boolean = false,
    batteryLevel: Int? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // First row: connection status and device name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectionStatusIndicator(connectionState)
                
                Text(
                    text = deviceName ?: "No Device Connected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Second row: signal strength, learning mode, battery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signal strength
                SignalStrengthIndicator(signalStrength)
                
                // Learning mode indicator
                if (isLearningMode) {
                    LearningModeIndicator()
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Battery indicator
                if (batteryLevel != null) {
                    BatteryIndicator(batteryLevel)
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusIndicator(
    connectionState: BluetoothLeManager.ConnectionState
) {
    val (icon, color, text) = when (connectionState) {
        BluetoothLeManager.ConnectionState.DISCONNECTED -> 
            Triple(Icons.Filled.Close, Color.Gray, "Disconnected")
        BluetoothLeManager.ConnectionState.SCANNING -> 
            Triple(Icons.Filled.Search, Color.Blue, "Scanning")
        BluetoothLeManager.ConnectionState.CONNECTING -> 
            Triple(Icons.Filled.Settings, Color(0xFFFF9800), "Connecting")
        BluetoothLeManager.ConnectionState.AUTHENTICATING -> 
            Triple(Icons.Filled.Lock, Color(0xFFFF9800), "Authenticating")
        BluetoothLeManager.ConnectionState.CONNECTED -> 
            Triple(Icons.Filled.CheckCircle, Color.Green, "Connected")
        BluetoothLeManager.ConnectionState.RECONNECTING -> 
            Triple(Icons.Filled.Refresh, Color(0xFFFF9800), "Reconnecting")
    }
    
    // Connection status animation
    val isAnimating = connectionState in listOf(
        BluetoothLeManager.ConnectionState.SCANNING,
        BluetoothLeManager.ConnectionState.CONNECTING,
        BluetoothLeManager.ConnectionState.AUTHENTICATING,
        BluetoothLeManager.ConnectionState.RECONNECTING
    )
    
    val animationSpec = infiniteRepeatable<Float>(
        animation = tween(1000),
        repeatMode = RepeatMode.Reverse
    )
    val alpha by animateFloatAsState(
        targetValue = if (isAnimating) 0.5f else 1f,
        animationSpec = if (isAnimating) animationSpec else tween(0),
        label = "connection_status_alpha"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color.copy(alpha = alpha),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = color.copy(alpha = alpha),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SignalStrengthIndicator(signalStrength: Int?) {
    if (signalStrength == null) return
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = "Signal Strength",
            tint = Color.Green,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${signalStrength}dBm",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun LearningModeIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "learning_animation")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),

        label = "learning_alpha"
    )
    
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Red.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Learning Mode",
            tint = Color.Red.copy(alpha = alpha),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Learning",
            color = Color.Red.copy(alpha = alpha),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BatteryIndicator(batteryLevel: Int) {
    val batteryColor = when {
        batteryLevel >= 70 -> Color.Green
        batteryLevel >= 30 -> Color(0xFFFF9800) // Orange
        else -> Color.Red
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Star, // Temporarily use Star icon instead of battery icon
            contentDescription = "Battery",
            tint = batteryColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${batteryLevel}%",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ReceivedKeyIndicator(
    receivedKeys: Set<String>,
    modifier: Modifier = Modifier
) {
    if (receivedKeys.isEmpty()) return
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Reception Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Current Keys: ${receivedKeys.joinToString(" + ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}