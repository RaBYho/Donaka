package com.example.donaka100.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.donaka100.ui.theme.*

@Composable
fun <T> DonakaCardList(
    elements: List<T>,
    cle: (T) -> Any,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    titreVide: String = "Rien à afficher",
    messageVide: String = "",
    labelActionVide: String? = null,
    onActionVide: (() -> Unit)? = null,
    onItemClick: ((T) -> Unit)? = null,
    onEdit: ((T) -> Unit)? = null,
    onDelete: ((T) -> Unit)? = null,
    nomPourSuppression: (T) -> String = { "cet élément" },
    itemContent: @Composable ColumnScope.(T) -> Unit
) {
    var aSupprimer by remember { mutableStateOf<T?>(null) }

    when {
        isLoading -> LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            userScrollEnabled = false
        ) {
            items(count = 4, key = { index -> "skeleton-$index" }) { DonakaCard(isLoading = true) {} }
        }

        elements.isEmpty() -> DonakaEmptyState(
            titre = titreVide,
            message = messageVide,
            modifier = modifier,
            labelAction = labelActionVide,
            onAction = onActionVide
        )

        else -> LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(elements, key = cle) { element ->
                DonakaCard(
                    onClick = onItemClick?.let { clic -> { clic(element) } }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = 0.99f },
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemContent(element)
                        }
                        if (onEdit != null || onDelete != null) {
                            MenuActions(
                                onEdit = onEdit?.let { edit -> { edit(element) } },
                                onDelete = if (onDelete != null) ({ aSupprimer = element }) else null
                            )
                        }
                    }
                }
            }
        }
    }

    aSupprimer?.let { cible ->
        DonakaConfirmDialog(
            titre = "Supprimer ?",
            message = "Voulez-vous vraiment supprimer ${nomPourSuppression(cible)} ? Cette action est irréversible.",
            onConfirm = {
                onDelete?.invoke(cible)
                aSupprimer = null
            },
            onDismiss = { aSupprimer = null }
        )
    }
}

@Composable
private fun MenuActions(onEdit: (() -> Unit)?, onDelete: (() -> Unit)?) {
    var ouvert by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { ouvert = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Actions", tint = TexteGris)
        }
        DropdownMenu(expanded = ouvert, onDismissRequest = { ouvert = false }) {
            if (onEdit != null) {
                DropdownMenuItem(
                    text = { Text("Modifier") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = { ouvert = false; onEdit() }
                )
            }
            if (onDelete != null) {
                DropdownMenuItem(
                    text = { Text("Supprimer", color = Rouge) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Rouge) },
                    onClick = { ouvert = false; onDelete() }
                )
            }
        }
    }
}