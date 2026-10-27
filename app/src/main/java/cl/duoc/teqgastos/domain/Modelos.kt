package cl.duoc.teqgastos.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

/**
 * Categorías de gasto con su tope por comprobante (CLP).
 * Los topes son referenciales: TEQMED los define informalmente y la app los aplica de forma consistente.
 */
enum class Categoria(val etiqueta: String, val tope: Int) {
    COMBUSTIBLE("Combustible", 60_000),
    PEAJE("Peaje", 15_000),
    ESTACIONAMIENTO("Estacionamiento", 10_000),
    ALIMENTACION("Alimentación", 12_000),
    ALOJAMIENTO("Alojamiento", 70_000),
    INSUMOS("Insumos urgentes", 50_000)
}

/** Con qué se pagó el gasto. Solo "Dinero propio" genera reembolso al técnico. */
enum class MedioPago(val etiqueta: String, val descripcion: String) {
    FONDO_POR_RENDIR("Fondo por rendir", "Se descuenta del fondo asignado"),
    TARJETA_CORPORATIVA("Tarjeta corporativa", "Pagado con la tarjeta de la empresa"),
    DINERO_PROPIO("Dinero propio", "Se reembolsa al técnico")
}

enum class EstadoGasto(val etiqueta: String, val descripcion: String) {
    PENDIENTE("Pendiente", "Esperando revisión de jefatura"),
    APROBADO("Aprobado", "Aprobado por jefatura, pendiente de pago"),
    OBSERVADO("Observado", "Requiere corrección del técnico"),
    RECHAZADO("Rechazado", "No será reembolsado"),
    PAGADO("Pagado", "Pago registrado por Finanzas")
}

enum class Rol(val etiqueta: String) {
    TECNICO("Técnico"),
    JEFATURA("Jefatura"),
    FINANZAS("Finanzas")
}

data class Usuario(val email: String, val rol: Rol) {
    val nombre: String
        get() = email.substringBefore("@").replace('.', ' ').replace('_', ' ')
            .split(" ").filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }

    val iniciales: String
        get() = nombre.split(" ").take(2).mapNotNull { it.firstOrNull() }.joinToString("").uppercase()
}

data class Gasto(
    val id: Long = 0,
    val categoria: Categoria,
    val medioPago: MedioPago,
    val monto: Int,
    val fecha: LocalDate,
    val proveedor: String,
    val numeroComprobante: String,
    val fotoUri: String,
    val latitud: Double?,
    val longitud: Double?,
    val estado: EstadoGasto = EstadoGasto.PENDIENTE,
    val observacion: String? = null,
    val tecnicoEmail: String,
    val registradoEl: LocalDate = LocalDate.now()
)

/** Registro de auditoría: quién cambió el estado de un gasto, cuándo y por qué. */
data class EventoHistorial(
    val estado: EstadoGasto,
    val usuarioEmail: String,
    val comentario: String?,
    val fecha: LocalDateTime
)

fun formatoClp(monto: Int): String =
    "$" + String.format(Locale.US, "%,d", monto).replace(',', '.')
