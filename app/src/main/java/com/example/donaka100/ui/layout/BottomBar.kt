package com.example.donaka100.ui.layout

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.navigation.Destination
import com.example.donaka100.ui.theme.*

@Composable
fun BottomBar(
    courant: Destination,
    onSelection: (Destination) -> Unit
) {
    NavigationBar(containerColor = Fond) {
        Destination.entries.forEach { dest ->
            NavigationBarItem(
                selected = courant == dest,
                onClick = { onSelection(dest) },
                icon = { Icon(dest.icone, contentDescription = dest.titre) },
                label = { Text(dest.titre, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = TexteGris,
                    unselectedTextColor = TexteGris,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}