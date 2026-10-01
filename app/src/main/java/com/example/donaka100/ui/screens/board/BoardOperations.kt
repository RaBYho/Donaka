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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Operation
import com.example.donaka100.ui.components.avecEspacesMilliers
import com.example.donaka100.ui.theme.*
import kotlin.math.abs

@Composable
fun BoardOperations(operations: List<Operation>, onToutVoir: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dernières Opérations", fontWeight = FontWeight.Bold, color = TexteFonce)
            TextButton(onClick = onToutVoir) { Text("Tout voir →", color = Primary, fontSize = 12.sp) }
        }
        if (operations.isEmpty()) {
            Text("Aucune opération aujourd'hui", fontSize = 13.sp, color = TexteGris)
        } else {
            operations.forEach { OperationRow(it) }
        }
    }
}

@Composable
private fun OperationRow(op: Operation) {
    val estEntree = op.montant >= 0
    val couleur = if (estEntree) Vert else Rouge
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlanche),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(if (estEntree) VertClair else RougeClair),
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
                Text(op.titre, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
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