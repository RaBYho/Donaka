package com.example.donaka100.ui.screens.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Operation
import com.example.donaka100.ui.components.avecEspacesMilliers
import com.example.donaka100.ui.theme.*
import kotlin.math.abs

/**
 * Affichage des dernières opérations avec rendu optimisé sur GPU.
 */
@Composable
fun BoardOperations(operations: List<Operation>) {
    val operationsListe = remember(operations) { operations }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Dernières Opérations", fontWeight = FontWeight.Bold, color = TexteFonce)

        if (operationsListe.isEmpty()) {
            Text("Aucune opération aujourd'hui", fontSize = 13.sp, color = TexteGris)
        } else {
            operationsListe.forEach { op ->
                key(op.id) {
                    OperationRow(op)
                }
            }
        }
    }
}

@Composable
private fun OperationRow(op: Operation) {
    val estEntree = op.montant >= 0
    val couleur = if (estEntree) Vert else Rouge

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = 0.99f },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlanche),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(CircleShape)
                    .background(if (estEntree) VertClair else RougeClair),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (estEntree) Icons.Default.CallReceived else Icons.Default.CallMade,
                    contentDescription = if (estEntree) "Entrée" else "Sortie",
                    tint = couleur, modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    op.titre, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text("${op.heure} • ${op.detail}", fontSize = 11.sp, color = TexteGris)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (estEntree) "+" else "-") + abs(op.montant).toString().avecEspacesMilliers(),
                    fontWeight = FontWeight.Bold, color = couleur, textAlign = TextAlign.End
                )
                Text("MGA", fontSize = 11.sp, color = couleur)
            }
        }
    }
}