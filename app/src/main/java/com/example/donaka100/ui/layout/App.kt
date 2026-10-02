package com.example.donaka100.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.donaka100.ui.navigation.Destination
import com.example.donaka100.ui.theme.Fond
import com.example.donaka100.ui.screens.CommandeScreen
import com.example.donaka100.ui.screens.BoardScreen
import com.example.donaka100.ui.screens.DepensesScreen
import com.example.donaka100.ui.screens.FourneauxScreen
import com.example.donaka100.ui.screens.StockScreen

@Composable
fun DonakaApp() {
    var courant by remember { mutableStateOf(Destination.BOARD) }

    Scaffold(
        containerColor = Fond,
        topBar = { TopBar() },
        bottomBar = { BottomBar(courant, onSelection = { courant = it }) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (courant) {
                Destination.BOARD -> BoardScreen( onVoirStock = { courant = Destination.STOCK },
                    onVoirDepenses = { courant = Destination.DEPENSES })
                Destination.COMMANDE -> CommandeScreen()
                Destination.FOURNEAUX -> FourneauxScreen()
                Destination.STOCK -> StockScreen()
                Destination.DEPENSES -> DepensesScreen(onVoirStock = { courant = Destination.STOCK })
            }
        }
    }
}