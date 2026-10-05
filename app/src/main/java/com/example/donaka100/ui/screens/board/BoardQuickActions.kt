package com.example.donaka100.ui.screens.board

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

/**
 * Boutons d'action rapide avec micro-interaction de pression fluide sur GPU.
 */
@Composable
fun BoardQuickActions(onClient: () -> Unit, onAcheter: () -> Unit, onFournee: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Tuile("+ Client", Icons.Default.PersonAdd, onClient, Modifier.weight(1f))
        Tuile("Acheter", Icons.Default.AddShoppingCart, onAcheter, Modifier.weight(1f))
        Tuile("Fournée", Icons.Default.LocalFireDepartment, onFournee, Modifier.weight(1f))
    }
}

@Composable
private fun Tuile(texte: String, icone: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessHigh),
        label = "tuileScale"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .heightIn(min = 48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
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