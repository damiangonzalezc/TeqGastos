package cl.duoc.teqgastos.domain

/**
 * Genera el archivo CSV que Finanzas carga en el sistema contable (ERP).
 * Separador ";" para que Excel en configuración regional chilena lo abra en columnas.
 */
object ErpExporter {

    val ENCABEZADO = listOf(
        "id", "fecha", "tecnico", "categoria", "medio_pago",
        "proveedor", "n_comprobante", "monto_clp", "estado"
    )

    /** Solo se exportan gastos aprobados o pagados. */
    fun exportables(gastos: List<Gasto>): List<Gasto> =
        gastos.filter { it.estado == EstadoGasto.APROBADO || it.estado == EstadoGasto.PAGADO }

    fun generarCsv(gastos: List<Gasto>): String = buildString {
        appendLine(ENCABEZADO.joinToString(";"))
        exportables(gastos).forEach { g ->
            appendLine(
                listOf(
                    g.id.toString(),
                    g.fecha.format(GastoValidator.FORMATO_FECHA),
                    g.tecnicoEmail,
                    g.categoria.name,
                    g.medioPago.name,
                    limpiar(g.proveedor),
                    g.numeroComprobante,
                    g.monto.toString(),
                    g.estado.name
                ).joinToString(";")
            )
        }
    }

    private fun limpiar(texto: String) = texto.replace(';', ',').replace('\n', ' ').trim()
}
