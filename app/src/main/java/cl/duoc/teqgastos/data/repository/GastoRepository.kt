package cl.duoc.teqgastos.data.repository

import androidx.room.withTransaction
import cl.duoc.teqgastos.data.local.AppDatabase
import cl.duoc.teqgastos.data.local.GastoEntity
import cl.duoc.teqgastos.data.local.HistorialEntity
import cl.duoc.teqgastos.domain.Categoria
import cl.duoc.teqgastos.domain.EstadoGasto
import cl.duoc.teqgastos.domain.EventoHistorial
import cl.duoc.teqgastos.domain.Gasto
import cl.duoc.teqgastos.domain.MedioPago
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** Única puerta de entrada a los datos para los ViewModels. */
class GastoRepository(private val db: AppDatabase) {

    private val dao = db.gastoDao()

    fun observarTodos(): Flow<List<Gasto>> = dao.observarTodos().map { it.map(::aDominio) }

    fun observarPorTecnico(email: String): Flow<List<Gasto>> =
        dao.observarPorTecnico(email).map { it.map(::aDominio) }

    fun observarPorEstados(estados: Set<EstadoGasto>): Flow<List<Gasto>> =
        dao.observarPorEstados(estados.map { it.name }).map { it.map(::aDominio) }

    fun observarPorId(id: Long): Flow<Gasto?> = dao.observarPorId(id).map { it?.let(::aDominio) }

    fun observarHistorial(id: Long): Flow<List<EventoHistorial>> =
        dao.observarHistorial(id).map { lista ->
            lista.map {
                EventoHistorial(
                    estado = EstadoGasto.valueOf(it.estado),
                    usuarioEmail = it.usuarioEmail,
                    comentario = it.comentario,
                    fecha = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.fechaMillis), ZoneId.systemDefault())
                )
            }
        }

    suspend fun obtener(id: Long): Gasto? = dao.obtenerPorId(id)?.let(::aDominio)

    suspend fun comprobanteYaRendido(proveedor: String, numero: String, excluirId: Long = 0): Boolean =
        dao.contarComprobante(normalizar(proveedor), numero.trim(), excluirId) > 0

    /** Guarda el gasto y su primer evento de auditoría en una sola transacción. */
    suspend fun registrar(gasto: Gasto, autor: String): Long = db.withTransaction {
        val id = dao.insertar(aEntidad(gasto))
        dao.insertarHistorial(HistorialEntity(gastoId = id, estado = EstadoGasto.PENDIENTE.name, usuarioEmail = autor, comentario = "Gasto registrado"))
        id
    }

    /** El técnico corrige un gasto observado: vuelve a Pendiente y queda registrado. */
    suspend fun corregir(gasto: Gasto, autor: String) = db.withTransaction {
        val original = dao.obtenerPorId(gasto.id) ?: return@withTransaction
        dao.actualizar(
            aEntidad(gasto).copy(
                id = original.id,
                estado = EstadoGasto.PENDIENTE.name,
                observacion = null,
                creadoEn = original.creadoEn
            )
        )
        dao.insertarHistorial(
            HistorialEntity(gastoId = gasto.id, estado = EstadoGasto.PENDIENTE.name, usuarioEmail = autor, comentario = "Corregido y reenviado")
        )
    }

    suspend fun cambiarEstado(id: Long, estado: EstadoGasto, comentario: String?, autor: String) =
        db.withTransaction {
            val texto = comentario?.trim()?.ifBlank { null }
            dao.actualizarEstado(id, estado.name, texto)
            dao.insertarHistorial(HistorialEntity(gastoId = id, estado = estado.name, usuarioEmail = autor, comentario = texto))
        }

    private fun normalizar(proveedor: String) = proveedor.trim().uppercase()

    private fun aEntidad(g: Gasto) = GastoEntity(
        id = g.id,
        categoria = g.categoria.name,
        medioPago = g.medioPago.name,
        monto = g.monto,
        fechaEpochDay = g.fecha.toEpochDay(),
        proveedor = normalizar(g.proveedor),
        numeroComprobante = g.numeroComprobante.trim(),
        fotoUri = g.fotoUri,
        latitud = g.latitud,
        longitud = g.longitud,
        estado = g.estado.name,
        observacion = g.observacion,
        tecnicoEmail = g.tecnicoEmail
    )

    private fun aDominio(e: GastoEntity) = Gasto(
        id = e.id,
        categoria = Categoria.valueOf(e.categoria),
        medioPago = MedioPago.valueOf(e.medioPago),
        monto = e.monto,
        fecha = LocalDate.ofEpochDay(e.fechaEpochDay),
        proveedor = e.proveedor,
        numeroComprobante = e.numeroComprobante,
        fotoUri = e.fotoUri,
        latitud = e.latitud,
        longitud = e.longitud,
        estado = EstadoGasto.valueOf(e.estado),
        observacion = e.observacion,
        tecnicoEmail = e.tecnicoEmail,
        registradoEl = Instant.ofEpochMilli(e.creadoEn).atZone(ZoneId.systemDefault()).toLocalDate()
    )
}
