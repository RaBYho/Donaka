package com.example.donaka100.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.donaka100.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonakaFormSheet(
    titre: String,
    onDismiss: () -> Unit,
    onValider: () -> Unit,
    modifier: Modifier = Modifier,
    labelValider: String = "Enregistrer",
    isLoading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Fond
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .imePadding()                          // remonte au-dessus du clavier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(titre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TexteFonce)

            content()

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DonakaButton(
                    texte = "Annuler", onClick = onDismiss,
                    style = StyleBouton.CONTOUR, modifier = Modifier.weight(1f)
                )
                DonakaButton(
                    texte = labelValider, onClick = onValider, isLoading = isLoading,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}