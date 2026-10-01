package com.example.donaka100.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.donaka100.ui.theme.*

@Composable
fun DonakaSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Rechercher…",
    onSearch: (() -> Unit)? = null
) {
    val focus = LocalFocusManager.current

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        placeholder = { Text(placeholder, color = TexteGris) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TexteGris) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Effacer", tint = TexteGris)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {
            onSearch?.invoke()
            focus.clearFocus()
        }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = SurfaceMoyenne,
            unfocusedContainerColor = SurfaceMoyenne,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Primary,
            focusedTextColor = TexteFonce,
            unfocusedTextColor = TexteFonce
        )
    )
}