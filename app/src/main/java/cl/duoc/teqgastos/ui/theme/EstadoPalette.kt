package cl.duoc.teqgastos.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import cl.duoc.teqgastos.domain.EstadoGasto

/**
 * Colores de un estado del gasto.
 * - contenedor / contenido: fondo y texto de chips, banners y tarjetas suaves.
 * - acento / sobreAcento: color fuerte (barra lateral, puntos, botones) y su texto.
 * Cada par cumple contraste AA en su modo (claro u oscuro).
 */
@Immutable
data class EstadoColores(
    val contenedor: Color,
    val contenido: Color,
    val acento: Color,
    val sobreAcento: Color
)

@Immutable
data class PaletaEstados(
    val pendiente: EstadoColores,
    val aprobado: EstadoColores,
    val observado: EstadoColores,
    val rechazado: EstadoColores,
    val pagado: EstadoColores
) {
    fun de(estado: EstadoGasto): EstadoColores = when (estado) {
        EstadoGasto.PENDIENTE -> pendiente
        EstadoGasto.APROBADO -> aprobado
        EstadoGasto.OBSERVADO -> observado
        EstadoGasto.RECHAZADO -> rechazado
        EstadoGasto.PAGADO -> pagado
    }
}

/** Pendiente = azul (en espera), Aprobado = verde, Observado = ámbar, Rechazado = rojo, Pagado = violeta. */
val PaletaEstadosClara = PaletaEstados(
    pendiente = EstadoColores(Color(0xFFDDE6FF), Color(0xFF1B3A80), Color(0xFF3558C8), Color.White),
    aprobado = EstadoColores(Color(0xFFD4F3DC), Color(0xFF0D5223), Color(0xFF237A35), Color.White),
    observado = EstadoColores(Color(0xFFFFEBC4), Color(0xFF633F00), Color(0xFF9A5B00), Color.White),
    rechazado = EstadoColores(Color(0xFFFFDAD6), Color(0xFF8C1D18), Color(0xFFC62828), Color.White),
    pagado = EstadoColores(Color(0xFFEBDDFF), Color(0xFF45238A), Color(0xFF6E48C4), Color.White)
)

val PaletaEstadosOscura = PaletaEstados(
    pendiente = EstadoColores(Color(0xFF1F2E57), Color(0xFFC9D6FF), Color(0xFF8EA8FF), Color(0xFF0E1A3D)),
    aprobado = EstadoColores(Color(0xFF173D22), Color(0xFFB2EDC0), Color(0xFF79D68E), Color(0xFF07290F)),
    observado = EstadoColores(Color(0xFF45330C), Color(0xFFFFDDA0), Color(0xFFF2B544), Color(0xFF2E1F00)),
    rechazado = EstadoColores(Color(0xFF55201C), Color(0xFFFFC9C3), Color(0xFFFF8A80), Color(0xFF3B0A07)),
    pagado = EstadoColores(Color(0xFF35275A), Color(0xFFE0D2FF), Color(0xFFB9A2FF), Color(0xFF21114A))
)

val LocalPaletaEstados = staticCompositionLocalOf { PaletaEstadosClara }
