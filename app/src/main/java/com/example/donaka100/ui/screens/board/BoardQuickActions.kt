package com.example.donaka100.ui.screens.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

@Composable
fun BoardQuickActions(onVente: () -> Unit, onSortie: () -> Unit, onFournee: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tuile("+ Client", Icons.Default.PersonAdd, onVente, Modifier.weight(1f))
        Tuile("+ Sortie", Icons.Default.ShoppingCartCheckout, onSortie, Modifier.weight(1f))
        Tuile("Fournée", Icons.Default.BakeryDining, onFournee, Modifier.weight(1f))
    }
}

@Composable
private fun Tuile(texte: String, icone: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBlanche),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) { Icon(icone, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp)) }
            Text(texte, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TexteFonce)
        }
    }
}