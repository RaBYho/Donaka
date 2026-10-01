package com.example.donaka100.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.donaka100.ui.theme.*

data class ActionMenu(
    val libelle: String,
    val icone: ImageVector,
    val danger: Boolean = false,
    val onClick: () -> Unit
)

/** Bouton ⋮ avec menu déroulant, réutilisable partout */
@Composable
fun DonakaOverflowMenu(actions: List<ActionMenu>, modifier: Modifier = Modifier) {
    var ouvert by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { ouvert = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = TexteGris)
        }
        DropdownMenu(expanded = ouvert, onDismissRequest = { ouvert = false }) {
            actions.forEach { a ->
                DropdownMenuItem(
                    text = { Text(a.libelle, color = if (a.danger) Rouge else Color.Unspecified) },
                    leadingIcon = {
                        Icon(a.icone, contentDescription = null, tint = if (a.danger) Rouge else TexteGris)
                    },
                    onClick = { ouvert = false; a.onClick() }
                )
            }
        }
    }
}