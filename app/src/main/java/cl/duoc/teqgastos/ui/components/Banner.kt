package cl.duoc.teqgastos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.teqgastos.ui.theme.EstadoColores

/** Aviso coloreado según un estado (p. ej. "gasto observado", "pago registrado"). */
@Composable
fun BannerEstado(
    colores: EstadoColores,
    icono: ImageVector,
    titulo: String,
    modifier: Modifier = Modifier,
    detalle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        color = colores.contenedor,
        contentColor = colores.contenido,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icono, contentDescription = null, tint = colores.acento)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(titulo, style = MaterialTheme.typography.titleSmall)
                if (detalle != null) {
                    Text(detalle, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
