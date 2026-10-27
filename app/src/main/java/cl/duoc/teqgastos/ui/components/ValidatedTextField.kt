package cl.duoc.teqgastos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.teqgastos.ui.theme.TeqTheme

/**
 * Campo de texto con retroalimentación visual:
 * ícono rojo + mensaje animado si hay error, check verde si es válido.
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    mostrarValido: Boolean = false,
    ayuda: String? = null,
    placeholder: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = {
                Icon(
                    icon, contentDescription = null,
                    tint = if (error != null) MaterialTheme.colorScheme.error else LocalContentColor.current
                )
            },
            trailingIcon = when {
                trailing != null -> trailing
                error != null -> {
                    { Icon(Icons.Filled.Error, "Campo con error", tint = MaterialTheme.colorScheme.error) }
                }
                mostrarValido && value.isNotBlank() -> {
                    { Icon(Icons.Filled.CheckCircle, "Campo válido", tint = TeqTheme.estados.aprobado.acento) }
                }
                else -> null
            },
            isError = error != null,
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 2,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            modifier = Modifier.fillMaxWidth()
        )
        MensajeError(error)
        if (error == null && ayuda != null) {
            Text(
                ayuda,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/** Mensaje de error que aparece/desaparece con animación. Reutilizable fuera de los TextField. */
@Composable
fun MensajeError(error: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = error != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        ) {
            Icon(
                Icons.Filled.Error, null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
