package com.example.donaka100.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.donaka100.ui.theme.*

@Composable
fun donakaFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Primary,
    unfocusedBorderColor = TexteGris.copy(alpha = 0.4f),
    errorBorderColor = Rouge,
    focusedLabelColor = Primary,
    unfocusedLabelColor = TexteGris,
    errorLabelColor = Rouge,
    cursorColor = Primary,
    focusedTextColor = TexteFonce,
    unfocusedTextColor = TexteFonce,
    focusedContainerColor = SurfaceBlanche,
    unfocusedContainerColor = SurfaceBlanche,
    errorContainerColor = SurfaceBlanche,
    disabledContainerColor = SurfaceBlanche,
    disabledTextColor = TexteFonce,
    disabledBorderColor = TexteGris.copy(alpha = 0.4f),
    disabledLabelColor = TexteGris,
    disabledLeadingIconColor = TexteGris,
    disabledTrailingIconColor = TexteGris,
    disabledPlaceholderColor = TexteGris,
    disabledSuffixColor = TexteGris
)

@Composable
fun DonakaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    helper: String? = null,
    trailingIcon: ImageVector? = null,
    leadingIcon: ImageVector? = null,
    suffix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailingIcon?.let { { Icon(it, contentDescription = null) } },
        suffix = suffix?.let { { Text(it) } },
        supportingText = (error ?: helper)?.let { { Text(it) } },
        isError = error != null,
        enabled = enabled,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(12.dp),
        colors = donakaFieldColors()
    )
}