package cl.duoc.teqgastos.domain

/**
 * Máquina de estados del gasto, centralizada fuera de la UI:
 *
 *   PENDIENTE ──Jefatura──▶ APROBADO ──Finanzas──▶ PAGADO
 *       │
 *       ├──Jefatura──▶ OBSERVADO ──Técnico corrige──▶ PENDIENTE
 *       └──Jefatura──▶ RECHAZADO
 */
object FlujoAprobacion {

    /** Estados a los que un rol puede mover un gasto que está en [estado]. */
    fun accionesPermitidas(rol: Rol, estado: EstadoGasto): List<EstadoGasto> = when (rol) {
        Rol.JEFATURA ->
            if (estado == EstadoGasto.PENDIENTE)
                listOf(EstadoGasto.APROBADO, EstadoGasto.OBSERVADO, EstadoGasto.RECHAZADO)
            else emptyList()
        Rol.FINANZAS ->
            if (estado == EstadoGasto.APROBADO) listOf(EstadoGasto.PAGADO) else emptyList()
        Rol.TECNICO -> emptyList()
    }

    /** Solo el técnico dueño del gasto puede corregirlo, y solo si fue observado. */
    fun puedeCorregir(usuario: Usuario, gasto: Gasto): Boolean =
        usuario.rol == Rol.TECNICO &&
            usuario.email == gasto.tecnicoEmail &&
            gasto.estado == EstadoGasto.OBSERVADO

    /** Observar o rechazar exige un motivo escrito. */
    fun requiereComentario(destino: EstadoGasto): Boolean =
        destino == EstadoGasto.OBSERVADO || destino == EstadoGasto.RECHAZADO

    /** Decisiones definitivas que piden confirmación antes de aplicarse. */
    fun requiereConfirmacion(destino: EstadoGasto): Boolean =
        destino == EstadoGasto.RECHAZADO || destino == EstadoGasto.PAGADO

    /** Estados que ve cada rol en su bandeja (null = todos). */
    fun estadosVisibles(rol: Rol): Set<EstadoGasto>? = when (rol) {
        Rol.FINANZAS -> setOf(EstadoGasto.APROBADO, EstadoGasto.PAGADO)
        else -> null
    }

    /** Montos destacados en el resumen de la pantalla principal según el rol. */
    fun resumen(rol: Rol): Pair<Pair<String, EstadoGasto>, Pair<String, EstadoGasto>> = when (rol) {
        Rol.TECNICO -> ("En revisión" to EstadoGasto.PENDIENTE) to ("Reembolsado" to EstadoGasto.PAGADO)
        Rol.JEFATURA -> ("Por revisar" to EstadoGasto.PENDIENTE) to ("Aprobado" to EstadoGasto.APROBADO)
        Rol.FINANZAS -> ("Por pagar" to EstadoGasto.APROBADO) to ("Pagado" to EstadoGasto.PAGADO)
    }
}
