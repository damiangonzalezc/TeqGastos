package cl.duoc.teqgastos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.teqgastos.domain.Categoria
import cl.duoc.teqgastos.domain.EstadoGasto
import cl.duoc.teqgastos.domain.MedioPago
import cl.duoc.teqgastos.domain.Rol
import cl.duoc.teqgastos.ui.theme.TeqTheme

fun Categoria.icono(): ImageVector = when (this) {
    Categoria.COMBUSTIBLE -> Icons.Filled.LocalGasStation
    Categoria.PEAJE -> Icons.Filled.Toll
    Categoria.ESTACIONAMIENTO -> Icons.Filled.LocalParking
    Categoria.ALIMENTACION -> Icons.Filled.Restaurant
    Categoria.ALOJAMIENTO -> Icons.Filled.Hotel
    Categoria.INSUMOS -> Icons.Filled.Build
}

fun MedioPago.icono(): ImageVector = when (this) {
    MedioPago.FONDO_POR_RENDIR -> Icons.Filled.AccountBalanceWallet
    MedioPago.TARJETA_CORPORATIVA -> Icons.Filled.CreditCard
    MedioPago.DINERO_PROPIO -> Icons.Filled.Payments
}

/** Cada estado tiene un ícono propio: el color nunca es la única señal (accesibilidad). */
fun EstadoGasto.icono(): ImageVector = when (this) {
    EstadoGasto.PENDIENTE -> Icons.Filled.Schedule
    EstadoGasto.APROBADO -> Icons.Filled.CheckCircle
    EstadoGasto.OBSERVADO -> Icons.Filled.ReportProblem
    EstadoGasto.RECHAZADO -> Icons.Filled.Cancel
    EstadoGasto.PAGADO -> Icons.Filled.Paid
}

fun Rol.icono(): ImageVector = when (this) {
    Rol.TECNICO -> Icons.Filled.Engineering
    Rol.JEFATURA -> Icons.Filled.SupervisorAccount
    Rol.FINANZAS -> Icons.Filled.AccountBalance
}

/** Etiqueta de estado con ícono; los colores cambian con transición cuando el estado cambia. */
@Composable
fun EstadoChip(estado: EstadoGasto, modifier: Modifier = Modifier) {
    val colores = TeqTheme.estados.de(estado)
    val fondo by animateColorAsState(colores.contenedor, tween(500), label = "fondoEstado")
    val texto by animateColorAsState(colores.contenido, tween(500), label = "textoEstado")
    Surface(
        color = fondo,
        contentColor = texto,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Icon(estado.icono(), contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                estado.etiqueta,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
