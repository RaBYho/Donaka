package com.example.donaka100.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(val titre: String, val icone: ImageVector) {
    BOARD("Board", Icons.Default.Dashboard),
    COMMANDE("Commande", Icons.Default.Receipt),
    FOURNEAUX("Fourneaux", Icons.Default.BakeryDining),
    STOCK("Stock", Icons.Default.Inventory2),
    DEPENSES("Dépenses", Icons.Default.Payments)
}