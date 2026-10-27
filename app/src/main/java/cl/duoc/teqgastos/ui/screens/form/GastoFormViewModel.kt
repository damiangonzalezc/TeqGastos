package cl.duoc.teqgastos.ui.screens.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.teqgastos.data.repository.GastoRepository
import cl.duoc.teqgastos.data.session.SessionManager
import cl.duoc.teqgastos.domain.Categoria
import cl.duoc.teqgastos.domain.Gasto
import cl.duoc.teqgastos.domain.GastoErrores
import cl.duoc.teqgastos.domain.GastoInput
import cl.duoc.teqgastos.domain.GastoValidator
import cl.duoc.teqgastos.domain.MedioPago
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class GastoFormUiState(
    val idEdicion: Long? = null,
    val observacionJefatura: String? = null,
    val referenciaPlazo: LocalDate = LocalDate.now(),
    val categoria: Categoria? = null,
    val medioPago: MedioPago? = null,
    val monto: String = "",
    val fecha: String = LocalDate.now().format(GastoValidator.FORMATO_FECHA),
    val proveedor: String = "",
    val numeroComprobante: String = "",
    val fotoUri: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val buscandoUbicacion: Boolean = false,
    val mensajeUbicacion: String? = null,
    val errores: GastoErrores = GastoErrores(),
    val intentado: Boolean = false,
    val intentosFallidos: Int = 0,
    val guardando: Boolean = false,
    val guardado: Boolean = false
) {
    val esEdicion: Boolean get() = idEdicion != null
    fun aInput() = GastoInput(categoria, medioPago, monto, fecha, proveedor, numeroComprobante, fotoUri)
    val progreso: Float get() = aInput().camposCompletos / GastoInput.TOTAL_CAMPOS.toFloat()
}

/**
 * Toda la lógica del formulario vive aquí; la pantalla solo dibuja el estado
 * y envía eventos. Las reglas de negocio vienen de GastoValidator.
 * Sirve para registrar un gasto nuevo o corregir uno observado (id en la ruta).
 */
class GastoFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo: GastoRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _ui = MutableStateFlow(GastoFormUiState())
    val ui: StateFlow<GastoFormUiState> = _ui.asStateFlow()

    init {
        val id = savedStateHandle.get<Long>("id") ?: -1L
        if (id > 0) cargarParaCorregir(id)
    }

    private fun cargarParaCorregir(id: Long) = viewModelScope.launch {
        val g = repo.obtener(id) ?: return@launch
        _ui.value = GastoFormUiState(
            idEdicion = g.id,
            observacionJefatura = g.observacion,
            referenciaPlazo = g.registradoEl,
            categoria = g.categoria,
            medioPago = g.medioPago,
            monto = g.monto.toString(),
            fecha = g.fecha.format(GastoValidator.FORMATO_FECHA),
            proveedor = g.proveedor,
            numeroComprobante = g.numeroComprobante,
            fotoUri = g.fotoUri,
            latitud = g.latitud,
            longitud = g.longitud
        )
    }

    fun onCategoria(c: Categoria) = actualizar { it.copy(categoria = c) }
    fun onMedioPago(m: MedioPago) = actualizar { it.copy(medioPago = m) }
    fun onMonto(v: String) = actualizar { it.copy(monto = v.filter(Char::isDigit).take(9)) }
    fun onFecha(v: String) = actualizar { it.copy(fecha = v.take(10)) }
    fun onFechaSeleccionada(fecha: LocalDate) = onFecha(fecha.format(GastoValidator.FORMATO_FECHA))
    fun onProveedor(v: String) = actualizar { it.copy(proveedor = v.take(60)) }
    fun onNumero(v: String) = actualizar { it.copy(numeroComprobante = v.filter(Char::isDigit).take(12)) }
    fun onFotoTomada(uri: String) = actualizar { it.copy(fotoUri = uri) }

    fun onBuscandoUbicacion() = _ui.update { it.copy(buscandoUbicacion = true, mensajeUbicacion = null) }

    fun onUbicacion(lat: Double?, lon: Double?) = _ui.update {
        it.copy(
            latitud = lat, longitud = lon, buscandoUbicacion = false,
            mensajeUbicacion = if (lat == null) "No se pudo obtener la ubicación. Activa el GPS e inténtalo de nuevo." else null
        )
    }

    fun onPermisoUbicacionDenegado() = _ui.update {
        it.copy(buscandoUbicacion = false, mensajeUbicacion = "Permiso de ubicación denegado. Puedes guardar el gasto sin ella.")
    }

    fun quitarUbicacion() = _ui.update { it.copy(latitud = null, longitud = null, mensajeUbicacion = null) }

    fun guardar() {
        val estado = _ui.value
        if (estado.guardando) return
        val errores = GastoValidator.validar(estado.aInput(), estado.referenciaPlazo)
        if (errores.hayErrores) {
            fallo(errores)
            return
        }
        val usuario = session.usuario.value ?: return

        viewModelScope.launch {
            _ui.update { it.copy(guardando = true) }
            // Regla clave del caso: un comprobante no puede rendirse dos veces
            if (repo.comprobanteYaRendido(estado.proveedor, estado.numeroComprobante, estado.idEdicion ?: 0)) {
                fallo(errores.copy(numeroComprobante = "Este comprobante ya fue rendido"))
                return@launch
            }
            val gasto = Gasto(
                id = estado.idEdicion ?: 0,
                categoria = estado.categoria!!,
                medioPago = estado.medioPago!!,
                monto = estado.monto.toInt(),
                fecha = GastoValidator.parsearFecha(estado.fecha)!!,
                proveedor = estado.proveedor,
                numeroComprobante = estado.numeroComprobante,
                fotoUri = estado.fotoUri!!,
                latitud = estado.latitud,
                longitud = estado.longitud,
                tecnicoEmail = usuario.email
            )
            if (estado.esEdicion) repo.corregir(gasto, usuario.email) else repo.registrar(gasto, usuario.email)
            _ui.update { it.copy(guardando = false, guardado = true) }
        }
    }

    private fun fallo(errores: GastoErrores) = _ui.update {
        it.copy(errores = errores, intentado = true, guardando = false, intentosFallidos = it.intentosFallidos + 1)
    }

    /** Después del primer intento de guardar, se revalida en vivo con cada cambio. */
    private fun actualizar(cambio: (GastoFormUiState) -> GastoFormUiState) = _ui.update { actual ->
        val nuevo = cambio(actual)
        if (nuevo.intentado) nuevo.copy(errores = GastoValidator.validar(nuevo.aInput(), nuevo.referenciaPlazo)) else nuevo
    }
}
