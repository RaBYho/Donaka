package com.example.donaka100.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.donaka100.ui.theme.*

@Composable
fun DonakaEmptyState(
    titre: String,
    message: String,
    modifier: Modifier = Modifier,
    icone: ImageVector = Icons.Default.Inbox,
    labelAction: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icone, contentDescription = null, tint = TexteGris.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(12.dp))
        Text(titre, fontWeight = FontWeight.SemiBold, color = TexteFonce)
        if (message.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(message, color = TexteGris, textAlign = TextAlign.Center)
        }
        if (labelAction != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            DonakaButton(texte = labelAction, onClick = onAction)
        }
    }
}

@Composable
fun DonakaConfirmDialog(
    titre: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    labelConfirmer: String = "Supprimer"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceBlanche,
        title = { Text(titre, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = TexteGris) },
        confirmButton = {
            DonakaButton(labelConfirmer, onClick = onConfirm, style = StyleBouton.DANGER)
        },
        dismissButton = {
            DonakaButton("Annuler", onClick = onDismiss, style = StyleBouton.TEXTE)
        }
    )
}