package cl.duoc.teqgastos.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.teqgastos.data.session.SessionManager
import cl.duoc.teqgastos.data.settings.ModoTema
import cl.duoc.teqgastos.data.settings.SettingsRepository
import cl.duoc.teqgastos.domain.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AjustesUiState(
    val usuario: Usuario? = null,
    val modoTema: ModoTema = ModoTema.SISTEMA
)

class AjustesViewModel(
    private val session: SessionManager,
    private val settings: SettingsRepository
) : ViewModel() {

    val ui: StateFlow<AjustesUiState> = combine(session.usuario, settings.modoTema) { u, m ->
        AjustesUiState(u, m)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AjustesUiState(session.usuario.value, settings.modoTema.value)
    )

    fun onModoTema(modo: ModoTema) = settings.cambiarModoTema(modo)

    fun cerrarSesion() = session.cerrar()
}
