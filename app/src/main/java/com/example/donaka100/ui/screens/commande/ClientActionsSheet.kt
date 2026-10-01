package com.example.donaka100.ui.screens.commande

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Client
import com.example.donaka100.ui.components.enTelephone
import com.example.donaka100.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientActionsSheet(
    client: Client,
    onDismiss: () -> Unit,
    onAppeler: () -> Unit,
    onRappel: () -> Unit,
    onReleve: () -> Unit,
    onModifier: () -> Unit,
    onSupprimer: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Fond) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Text(client.nom, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TexteFonce)
            Spacer(Modifier.height(8.dp))

            ActionRow(Icons.Default.Call, "Appeler", client.telephone.enTelephone(), Primary, onAppeler)
            if (!client.aJour) {
                ActionRow(Icons.Default.Sms, "Envoyer un rappel amical", "SMS avec montant restant", Ambre, onRappel)
            }
            ActionRow(Icons.Default.ReceiptLong, "Relevé de créance & Factures", "Bientôt disponible", TexteGris, onReleve)
            ActionRow(Icons.Default.Edit, "Modifier les coordonnées", "Contact, adresse, limite", TexteGris, onModifier)
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = SurfaceMoyenne)
            ActionRow(Icons.Default.Delete, "Supprimer ce client", "Action irréversible", Rouge, onSupprimer)
        }
    }
}

@Composable
private fun ActionRow(
    icone: ImageVector,
    titre: String,
    sousTitre: String,
    couleur: Color,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, contentDescription = null, tint = couleur, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(titre, fontWeight = FontWeight.Medium, color = if (couleur == Rouge) Rouge else TexteFonce)
            Text(sousTitre, fontSize = 12.sp, color = TexteGris)
        }
    }
}