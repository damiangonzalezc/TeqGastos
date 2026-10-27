package cl.duoc.teqgastos.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.teqgastos.data.repository.GastoRepository
import cl.duoc.teqgastos.data.session.SessionManager
import cl.duoc.teqgastos.domain.ErpExporter
import cl.duoc.teqgastos.domain.EstadoGasto
import cl.duoc.teqgastos.domain.FlujoAprobacion
import cl.duoc.teqgastos.domain.Gasto
import cl.duoc.teqgastos.domain.Rol
import cl.duoc.teqgastos.domain.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val usuario: Usuario? = null,
    val gastos: List<Gasto> = emptyList(),
    val filtro: EstadoGasto? = null,
    val cargando: Boolean = true
) {
    val rol: Rol get() = usuario?.rol ?: Rol.TECNICO
    val puedeRegistrar: Boolean get() = rol == Rol.TECNICO
    val puedeExportar: Boolean get() = rol == Rol.FINANZAS

    val titulo: String
        get() = when (rol) {
            Rol.TECNICO -> "Mis rendiciones"
            Rol.JEFATURA -> "Rendiciones del equipo"
            Rol.FINANZAS -> "Pagos y exportación"
        }

    /** Estados que se ofrecen como filtro (los que el rol puede ver). */
    val estadosFiltrables: List<EstadoGasto>
        get() = FlujoAprobacion.estadosVisibles(rol)?.let { v -> EstadoGasto.entries.filter { it in v } }
            ?: EstadoGasto.entries

    val gastosFiltrados: List<Gasto> get() = if (filtro == null) gastos else gastos.filter { it.estado == filtro }

    fun cantidad(estado: EstadoGasto) = gastos.count { it.estado == estado }
    fun total(estado: EstadoGasto) = gastos.filter { it.estado == estado }.sumOf { it.monto }

    val observadosPropios: Int get() = if (rol == Rol.TECNICO) cantidad(EstadoGasto.OBSERVADO) else 0
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    repo: GastoRepository,
    session: SessionManager
) : ViewModel() {

    private val filtro = MutableStateFlow<EstadoGasto?>(null)

    /** Técnico: solo sus gastos · Jefatura: todo el equipo · Finanzas: aprobados y pagados. */
    private val gastos = session.usuario.flatMapLatest { u ->
        when (u?.rol) {
            null -> flowOf(emptyList())
            Rol.TECNICO -> repo.observarPorTecnico(u.email)
            Rol.JEFATURA -> repo.observarTodos()
            Rol.FINANZAS -> repo.observarPorEstados(FlujoAprobacion.estadosVisibles(Rol.FINANZAS)!!)
        }
    }

    val ui: StateFlow<HomeUiState> = combine(session.usuario, gastos, filtro) { u, g, f ->
        HomeUiState(usuario = u, gastos = g, filtro = f, cargando = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onFiltro(estado: EstadoGasto?) {
        filtro.value = if (filtro.value == estado) null else estado
    }

    /** Devuelve el CSV listo para compartir, o null si no hay gastos exportables. */
    fun generarExportacion(): String? {
        val exportables = ErpExporter.exportables(ui.value.gastosFiltrados)
        return if (exportables.isEmpty()) null else ErpExporter.generarCsv(exportables)
    }
}
