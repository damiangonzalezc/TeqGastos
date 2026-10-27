package cl.duoc.teqgastos.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/** Datos tal como los escribe el usuario en el formulario. */
data class GastoInput(
    val categoria: Categoria?,
    val medioPago: MedioPago?,
    val monto: String,
    val fecha: String,
    val proveedor: String,
    val numeroComprobante: String,
    val fotoUri: String?
) {
    /** Cuántos de los 7 datos obligatorios están completos (para la barra de progreso). */
    val camposCompletos: Int
        get() = listOf(
            categoria != null, medioPago != null, monto.isNotBlank(), fecha.isNotBlank(),
            proveedor.isNotBlank(), numeroComprobante.isNotBlank(), !fotoUri.isNullOrBlank()
        ).count { it }

    companion object {
        const val TOTAL_CAMPOS = 7
    }
}

/** Un mensaje por campo; null = campo válido. */
data class GastoErrores(
    val categoria: String? = null,
    val medioPago: String? = null,
    val monto: String? = null,
    val fecha: String? = null,
    val proveedor: String? = null,
    val numeroComprobante: String? = null,
    val foto: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(categoria, medioPago, monto, fecha, proveedor, numeroComprobante, foto).any { it != null }

    val cantidad: Int
        get() = listOf(categoria, medioPago, monto, fecha, proveedor, numeroComprobante, foto).count { it != null }
}

/**
 * Reglas de negocio de TeqGastos. No depende de Android ni de la UI,
 * por eso se prueba con tests unitarios simples.
 */
object GastoValidator {

    /** Plazo máximo para rendir un gasto desde su fecha. */
    const val PLAZO_DIAS = 30

    val FORMATO_FECHA: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)

    fun parsearFecha(texto: String): LocalDate? = try {
        LocalDate.parse(texto.trim(), FORMATO_FECHA)
    } catch (e: DateTimeParseException) {
        null
    }

    /**
     * @param referencia día contra el que se mide el plazo: hoy para un gasto nuevo,
     * o el día en que se registró originalmente si se está corrigiendo.
     */
    fun validar(input: GastoInput, referencia: LocalDate = LocalDate.now()): GastoErrores {
        val montoNum = input.monto.trim().toIntOrNull()
        val fecha = parsearFecha(input.fecha)

        return GastoErrores(
            categoria = if (input.categoria == null) "Selecciona una categoría" else null,

            medioPago = if (input.medioPago == null) "Indica con qué pagaste" else null,

            monto = when {
                input.monto.isBlank() -> "Ingresa el monto"
                montoNum == null -> "Solo números, sin puntos ni signos"
                montoNum <= 0 -> "El monto debe ser mayor a 0"
                input.categoria != null && montoNum > input.categoria.tope ->
                    "Supera el tope de ${input.categoria.etiqueta} (${formatoClp(input.categoria.tope)})"
                else -> null
            },

            fecha = when {
                input.fecha.isBlank() -> "Ingresa la fecha"
                fecha == null -> "Formato válido: dd/mm/aaaa"
                fecha.isAfter(referencia) -> "La fecha no puede ser futura"
                fecha.isBefore(referencia.minusDays(PLAZO_DIAS.toLong())) ->
                    "Fuera de plazo: máximo $PLAZO_DIAS días para rendir"
                else -> null
            },

            proveedor = when {
                input.proveedor.isBlank() -> "Ingresa el proveedor"
                input.proveedor.trim().length < 3 -> "Mínimo 3 caracteres"
                else -> null
            },

            numeroComprobante = when {
                input.numeroComprobante.isBlank() -> "Ingresa el N° de boleta o factura"
                !input.numeroComprobante.trim().all(Char::isDigit) -> "Solo números"
                input.numeroComprobante.trim().length < 3 -> "Mínimo 3 dígitos"
                else -> null
            },

            foto = if (input.fotoUri.isNullOrBlank()) "Toma una foto del comprobante" else null
        )
    }

    /** Observar o rechazar exige justificar la decisión. */
    fun validarRevision(nuevoEstado: EstadoGasto, observacion: String): String? {
        val requiere = FlujoAprobacion.requiereComentario(nuevoEstado)
        return when {
            requiere && observacion.isBlank() -> "Indica el motivo para ${nuevoEstado.etiqueta.lowercase()}"
            requiere && observacion.trim().length < 10 -> "Describe el motivo (mín. 10 caracteres)"
            else -> null
        }
    }
}
