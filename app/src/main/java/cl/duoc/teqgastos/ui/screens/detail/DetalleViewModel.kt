package cl.duoc.teqgastos.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.teqgastos.data.repository.GastoRepository
import cl.duoc.teqgastos.data.session.SessionManager
import cl.duoc.teqgastos.domain.EstadoGasto
import cl.duoc.teqgastos.domain.EventoHistorial
import cl.duoc.teqgastos.domain.FlujoAprobacion
import cl.duoc.teqgastos.domain.Gasto
import cl.duoc.teqgastos.domain.GastoValidator
import cl.duoc.teqgastos.domain.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RevisionForm(
    val observacion: String = "",
    val error: String? = null,
    val intentosFallidos: Int = 0,
    val confirmando: EstadoGasto? = null,
    val resultado: EstadoGasto? = null
)

data class DetalleUiState(
    val gasto: Gasto? = null,
    val usuario: Usuario? = null,
    val historial: List<EventoHistorial> = emptyList(),
    val revision: RevisionForm = RevisionForm()
) {
    /** Las acciones disponibles salen del flujo de aprobación, no de la pantalla. */
    val acciones: List<EstadoGasto>
        get() = if (gasto == null || usuario == null) emptyList()
        else FlujoAprobacion.accionesPermitidas(usuario.rol, gasto.estado)

    val pideComentario: Boolean get() = acciones.any(FlujoAprobacion::requiereComentario)

    val puedeCorregir: Boolean
        get() = gasto != null && usuario != null && FlujoAprobacion.puedeCorregir(usuario, gasto)
}

class DetalleViewModel(
    savedStateHandle: SavedStateHandle,
    private val repo: GastoRepository,
    private val session: SessionManager
) : ViewModel() {

    private val id: Long = checkNotNull(savedStateHandle["id"])
    private val revision = MutableStateFlow(RevisionForm())

    val ui: StateFlow<DetalleUiState> = combine(
        repo.observarPorId(id), session.usuario, repo.observarHistorial(id), revision
    ) { g, u, h, r ->
        DetalleUiState(gasto = g, usuario = u, historial = h, revision = r)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState())

    fun onObservacion(texto: String) = revision.update {
        it.copy(observacion = texto.take(200), error = null, resultado = null)
    }

    /** Valida la decisión; si es definitiva (rechazar/pagar) pide confirmación antes de aplicarla. */
    fun solicitar(destino: EstadoGasto) {
        val error = GastoValidator.validarRevision(destino, revision.value.observacion)
        if (error != null) {
            revision.update { it.copy(error = error, intentosFallidos = it.intentosFallidos + 1) }
            return
        }
        if (FlujoAprobacion.requiereConfirmacion(destino)) {
            revision.update { it.copy(confirmando = destino) }
        } else {
            aplicar(destino)
        }
    }

    fun confirmar() {
        revision.value.confirmando?.let(::aplicar)
    }

    fun cancelarConfirmacion() = revision.update { it.copy(confirmando = null) }

    private fun aplicar(destino: EstadoGasto) {
        revision.update { it.copy(confirmando = null) }
        val autor = session.usuario.value?.email ?: return
        // Se vuelve a verificar contra el flujo por si el estado cambió mientras tanto
        if (destino !in ui.value.acciones) return
        viewModelScope.launch {
            repo.cambiarEstado(id, destino, revision.value.observacion, autor)
            revision.value = RevisionForm(resultado = destino)
        }
    }
}
