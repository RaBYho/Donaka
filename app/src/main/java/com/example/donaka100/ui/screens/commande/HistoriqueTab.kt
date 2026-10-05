package com.example.donaka100.ui.screens.commande

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.data.Encaissement
import com.example.donaka100.data.ModeReglement
import com.example.donaka100.ui.CommandeUiState
import com.example.donaka100.ui.components.*
import com.example.donaka100.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.donaka100.data.TypeMouvement

private fun Encaissement.icone(): ImageVector = when {
    type == TypeMouvement.A_CREDIT -> Icons.Default.Schedule
    else -> when (mode) {
        ModeReglement.MVOLA, ModeReglement.AIRTEL_ORANGE -> Icons.Default.PhoneAndroid
        ModeReglement.BANCAIRE -> Icons.Default.AccountBalance
        else -> Icons.Default.Payments
    }
}
private val formatHeure = DateTimeFormatter.ofPattern("HH:mm")
private val formatDateLongue = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)

private fun libelleJour(date: LocalDate, aujourdhui: LocalDate): String = when (date) {
    aujourdhui -> "Aujourd'hui"
    aujourdhui.minusDays(1) -> "Hier"
    else -> date.format(formatDateLongue).replaceFirstChar { it.uppercase() }
}

@Composable
fun HistoriqueTab(etat: CommandeUiState, modifier: Modifier = Modifier) {
    var detail by remember { mutableStateOf<Encaissement?>(null) }
    val liste = etat.historiqueAffiche
    val aujourdhui = LocalDate.now()
    val groupes = remember(liste) { liste.groupBy { it.dateHeure.toLocalDate() } }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = !etat.isLoading
    ) {
        item { HistoriqueBanner(etat.totalEncaisseAujourdhui, etat.isLoading) }

        when {
            etat.isLoading -> items(3) { DonakaCard(isLoading = true) {} }

            liste.isEmpty() -> item {
                AucunEncaissement(filtre = etat.rechercheHistorique.isNotBlank() || etat.filtre.ordinal > 0)
            }

            else -> {
                groupes.forEach { (date, encaissements) ->
                    item(key = "titre-$date") { TitreJour(libelleJour(date, aujourdhui), date == aujourdhui) }
                    items(encaissements, key = { it.id }) { e ->
                        EncaissementCard(e, onClick = { detail = e })
                    }
                }
                item(key = "pied") { PiedJournal() }
            }
        }
    }

    detail?.let { DetailEncaissementDialog(it, onDismiss = { detail = null }) }
}

@Composable
private fun HistoriqueBanner(total: Long, isLoading: Boolean) {
    DonakaCard(isLoading = isLoading) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
            Spacer(Modifier.width(8.dp))
            Text("TOTAL ENCAISSÉ CE JOUR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Vert)
        }
        Text(
            (if (total > 0) "+" else "") + total.enMGA(),
            fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Vert
        )
    }
}

@Composable
private fun TitreJour(libelle: String, estAujourdhui: Boolean) {
    Row(Modifier.padding(start = 4.dp, top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (estAujourdhui) Icons.Default.CalendarToday else Icons.Default.Schedule,
            contentDescription = null,
            tint = if (estAujourdhui) Primary else TexteGris,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(libelle, fontWeight = FontWeight.SemiBold, color = TexteFonce)
    }
}
@Composable
private fun EncaissementCard(e: Encaissement, onClick: () -> Unit) {
    val credit = e.type == TypeMouvement.A_CREDIT
    val couleur = if (credit) Ambre else Vert

    DonakaCard(onClick = onClick) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).clip(CircleShape)
                    .background(if (credit) AmbreClair else VertClair.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) { Icon(e.icone(), null, tint = couleur, modifier = Modifier.size(20.dp)) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    e.clientNom, fontWeight = FontWeight.Bold, color = TexteFonce,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${e.dateHeure.format(formatHeure)}   ${e.mode?.libelle ?: "À crédit"}",
                    fontSize = 12.sp, color = TexteGris
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text((if (credit) "" else "+") + e.montant.enMGA(), fontWeight = FontWeight.Bold, color = couleur)
                when (e.type) {
                    TypeMouvement.ENCAISSE -> DonakaBadge("✓ Encaissé", type = TypeBadge.SUCCES)
                    TypeMouvement.A_CREDIT -> DonakaBadge("À crédit", type = TypeBadge.ALERTE)
                    TypeMouvement.REGLEMENT -> DonakaBadge("Créance réglée", type = TypeBadge.INFO)
                }
            }
        }

        if (e.note.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, null, tint = TexteGris, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text(e.note, fontSize = 12.sp, color = TexteGris, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}


@Composable
private fun PiedJournal() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Verified, null, tint = Primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(6.dp))
        Text("Journal à jour", fontWeight = FontWeight.SemiBold, color = TexteFonce)
        Text(
            "Toutes les transactions précédentes sont archivées dans la comptabilité générale.",
            fontSize = 12.sp, color = TexteGris, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AucunEncaissement(filtre: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.SearchOff, null, tint = TexteGris.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            if (filtre) "Aucun résultat" else "Aucun encaissement",
            fontWeight = FontWeight.SemiBold, color = TexteFonce
        )
        Text(
            if (filtre) "Essaie un autre filtre ou une autre recherche."
            else "Les règlements apparaîtront ici dès le premier encaissement.",
            color = TexteGris, textAlign = TextAlign.Center, fontSize = 13.sp
        )
    }
}

@Composable
private fun DetailEncaissementDialog(e: Encaissement, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceBlanche,
        title = { Text("Bon #${e.numero}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Ligne("Client", e.clientNom)
                Ligne("Date", e.dateHeure.format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")))
                Ligne("Mode", e.mode?.libelle ?: "À crédit (non payé)")
                Ligne("Montant", e.montant.enMGA())
                if (e.note.isNotBlank()) Ligne("Note", e.note)
            }
        },
        confirmButton = { DonakaButton("Fermer", onClick = onDismiss, style = StyleBouton.TEXTE) }
    )
}

@Composable
private fun Ligne(titre: String, valeur: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(titre, color = TexteGris, fontSize = 13.sp)
        Spacer(Modifier.width(12.dp))
        Text(valeur, fontWeight = FontWeight.Medium, fontSize = 13.sp, textAlign = TextAlign.End)
    }
}