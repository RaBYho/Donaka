package com.example.donaka100.ui.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.donaka100.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Barre supérieure Donaka.
 *
 * Choix de design (Artisan Warmth) :
 *  - Fond blanc pur (surfaceContainerLowest) pour détacher le header du
 *    contenu oatmeal — même logique que le BottomBar.
 *  - Hairline inférieure plutôt qu'ombre portée : cohérence avec la
 *    direction "crisp hairlines, warm tactile" du DESIGN.md.
 *  - "Donaka" en terracotta + date du jour en sous-titre : contexte immédiat
 *    pour le boulanger, sans ajouter de composant.
 *  - Avatar 36dp : compromis lisibilité / cible tactile (44dp avec padding).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    nomBoulangerie: String = "",
    onNotifications: () -> Unit = {},
    onProfile: () -> Unit = {},
    showNotificationBadge: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val dateLabel = remember { todayLabel() }
    val titreHeader = remember(nomBoulangerie) {
        if (nomBoulangerie.isBlank()) "Donaka" else nomBoulangerie
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.donaka_logo),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = titreHeader,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary,
                        )
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onNotifications) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = scheme.onSurfaceVariant,
                        )
                        if (showNotificationBadge) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(scheme.primary),
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(scheme.primary)
                        .clickable(onClick = onProfile),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = scheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = scheme.surfaceContainerLowest,
            ),
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = scheme.outlineVariant.copy(alpha = 0.5f),
        )
    }
}

private fun todayLabel(): String {
    val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)
    val formatted = LocalDate.now().format(formatter)
    return formatted.replaceFirstChar { it.uppercase(Locale.FRENCH) }
}