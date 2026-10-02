package com.example.donaka100.ui.layout

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.example.donaka100.ui.navigation.Destination
import com.example.donaka100.ui.screens.BoardScreen
import com.example.donaka100.ui.screens.CommandeScreen
import com.example.donaka100.ui.screens.DepensesScreen
import com.example.donaka100.ui.screens.FourneauxScreen
import com.example.donaka100.ui.screens.StockScreen
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAB_ANIM_MS = 300

@Composable
fun DonakaApp() {
    val destinations = Destination.entries

    // Source de vérité : l'onglet courant.
    var courant by remember { mutableStateOf(Destination.BOARD) }

    // État du pager, aligné sur l'onglet courant.
    val pagerState = rememberPagerState(
        initialPage = courant.ordinal,
        pageCount = { destinations.size },
    )

    // Préserve l'état (scroll, filtres) des écrans quand on les quitte.
    val stateHolder = rememberSaveableStateHolder()

    // --- Swipe → état -------------------------------------------------------
    // Quand l'utilisateur balaye et que le pager se stabilise sur une page,
    // on met à jour `courant` pour que le BottomBar reflète le bon onglet.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val dest = destinations[page]
                if (dest != courant) courant = dest
            }
    }

    // --- État → Pager -------------------------------------------------------
    // Quand on tape un onglet dans le BottomBar, on anime le pager jusqu'à
    // la page correspondante au lieu de sauter instantanément.
    LaunchedEffect(courant) {
        val target = courant.ordinal
        if (pagerState.currentPage != target) {
            pagerState.animateScrollToPage(
                page = target,
                animationSpec = tween(TAB_ANIM_MS),
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopBar() },
        bottomBar = { BottomBar(courant, onSelection = { courant = it }) },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            // 0 = seule la page visible est composée. Les voisines sont
            // composées à la volée pendant le drag. Meilleur compromis
            // perf/mémoire pour des écrans chargés.
            beyondViewportPageCount = 0,
        ) { page ->
            stateHolder.SaveableStateProvider(destinations[page].name) {
                Box(Modifier.fillMaxSize()) {
                    when (destinations[page]) {
                        Destination.BOARD -> BoardScreen(
                            onVoirStock = { courant = Destination.STOCK },
                            onVoirDepenses = { courant = Destination.DEPENSES },
                        )
                        Destination.COMMANDE -> CommandeScreen()
                        Destination.FOURNEAUX -> FourneauxScreen()
                        Destination.STOCK -> StockScreen()
                        Destination.DEPENSES -> DepensesScreen(
                            onVoirStock = { courant = Destination.STOCK },
                        )
                    }
                }
            }
        }
    }
}