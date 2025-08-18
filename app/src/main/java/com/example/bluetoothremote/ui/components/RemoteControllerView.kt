package com.example.bluetoothremote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RemoteControllerView(
    isEnabled: Boolean,
    onKeyPressed: (String) -> Unit,
    onKeyReleased: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 十字方向键区域
        DirectionKeysLayout(
            isEnabled = isEnabled,
            onKeyPressed = { key ->
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onKeyPressed(key)
            },
            onKeyReleased = onKeyReleased
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // 功能键区域
        FunctionKeysLayout(
            isEnabled = isEnabled,
            onKeyPressed = { key ->
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onKeyPressed(key)
            },
            onKeyReleased = onKeyReleased
        )
    }
}

@Composable
private fun DirectionKeysLayout(
    isEnabled: Boolean,
    onKeyPressed: (String) -> Unit,
    onKeyReleased: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 上键
        RemoteKeyButton(
            key = "K1",
            icon = Icons.Filled.KeyboardArrowUp,
            label = "上",
            isEnabled = isEnabled,
            keyColor = MaterialTheme.colorScheme.primary,
            onKeyPressed = onKeyPressed,
            onKeyReleased = onKeyReleased
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 中间行：左键、中间空白、右键
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左键
            RemoteKeyButton(
                key = "K3",
                icon = Icons.Filled.KeyboardArrowLeft,
                label = "左",
                isEnabled = isEnabled,
                keyColor = MaterialTheme.colorScheme.primary,
                onKeyPressed = onKeyPressed,
                onKeyReleased = onKeyReleased
            )
            
            Spacer(modifier = Modifier.width(94.dp)) // 70dp + 12dp + 12dp
            
            // 右键
            RemoteKeyButton(
                key = "K4",
                icon = Icons.Filled.KeyboardArrowRight,
                label = "右",
                isEnabled = isEnabled,
                keyColor = MaterialTheme.colorScheme.primary,
                onKeyPressed = onKeyPressed,
                onKeyReleased = onKeyReleased
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 下键
        RemoteKeyButton(
            key = "K2",
            icon = Icons.Filled.KeyboardArrowDown,
            label = "下",
            isEnabled = isEnabled,
            keyColor = MaterialTheme.colorScheme.primary,
            onKeyPressed = onKeyPressed,
            onKeyReleased = onKeyReleased
        )
    }
}

@Composable
private fun FunctionKeysLayout(
    isEnabled: Boolean,
    onKeyPressed: (String) -> Unit,
    onKeyReleased: (String) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // K5 功能1
            RemoteKeyButton(
                key = "K5",
                icon = Icons.Filled.Star,
                label = "功能1",
                isEnabled = isEnabled,
                keyColor = Color(0xFF4CAF50), // 绿色
                onKeyPressed = onKeyPressed,
                onKeyReleased = onKeyReleased
            )
            
            // K6 功能2
            RemoteKeyButton(
                key = "K6",
                icon = Icons.Filled.Star,
                label = "功能2",
                isEnabled = isEnabled,
                keyColor = Color(0xFF4CAF50), // 绿色
                onKeyPressed = onKeyPressed,
                onKeyReleased = onKeyReleased
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // K7 功能3
            RemoteKeyButton(
                key = "K7",
                icon = Icons.Filled.Settings,
                label = "功能3",
                isEnabled = isEnabled,
                keyColor = Color(0xFF4CAF50), // 绿色
                onKeyPressed = onKeyPressed,
                onKeyReleased = onKeyReleased
            )
            
            // K8 功能4
            RemoteKeyButton(
                key = "K8",
                icon = Icons.Filled.Settings,
                label = "功能4",
                isEnabled = isEnabled,
                keyColor = Color(0xFF4CAF50), // 绿色
                onKeyPressed = onKeyPressed,
                onKeyReleased = onKeyReleased
            )
        }
    }
}

@Composable
private fun RemoteKeyButton(
    key: String,
    icon: ImageVector,
    label: String,
    isEnabled: Boolean,
    keyColor: Color,
    onKeyPressed: (String) -> Unit,
    onKeyReleased: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    
    // 监听按压交互
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    if (!isPressed) {
                        isPressed = true
                        onKeyPressed(key)
                    }
                }
                is PressInteraction.Release -> {
                    if (isPressed) {
                        isPressed = false
                        onKeyReleased(key)
                    }
                }
                is PressInteraction.Cancel -> {
                    if (isPressed) {
                        isPressed = false
                        onKeyReleased(key)
                    }
                }
            }
        }
    }
    
    val buttonColor = when {
        !isEnabled -> Color.Gray
        isPressed -> keyColor.copy(alpha = 0.8f)
        else -> keyColor
    }
    
    val contentColor = if (isEnabled) Color.White else Color.Gray
    
    Box(
        modifier = modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(buttonColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isEnabled
            ) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}