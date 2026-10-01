package com.example.donaka100.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onNotifications: () -> Unit = {}) {
    TopAppBar(
        title = {
            Column {
                Text("Donaka", fontWeight = FontWeight.Bold, color = TexteFonce)
            }
        },
        actions = {
            IconButton(onClick = onNotifications) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TexteGris)
            }
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Fond)
    )
}