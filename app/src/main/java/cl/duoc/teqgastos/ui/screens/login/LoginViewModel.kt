package cl.duoc.teqgastos.ui.screens.login

import androidx.lifecycle.ViewModel
import cl.duoc.teqgastos.data.session.SessionManager
import cl.duoc.teqgastos.domain.LoginErrores
import cl.duoc.teqgastos.domain.LoginValidator
import cl.duoc.teqgastos.domain.Rol
import cl.duoc.teqgastos.domain.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rol: Rol = Rol.TECNICO,
    val errores: LoginErrores = LoginErrores(),
    val intentado: Boolean = false,
    val intentosFallidos: Int = 0,
    val ingresado: Boolean = false
)

class LoginViewModel(private val session: SessionManager) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun onEmail(valor: String) = actualizar { it.copy(email = valor) }
    fun onPassword(valor: String) = actualizar { it.copy(password = valor) }
    fun onRol(rol: Rol) = _ui.update { it.copy(rol = rol) }

    fun ingresar() {
        val s = _ui.value
        val errores = LoginValidator.validar(s.email, s.password)
        if (errores.hayErrores) {
            _ui.update { it.copy(errores = errores, intentado = true, intentosFallidos = it.intentosFallidos + 1) }
            return
        }
        // Prototipo: no hay backend, se acepta cualquier credencial válida en formato.
        session.iniciar(Usuario(s.email.trim().lowercase(), s.rol))
        _ui.update { it.copy(errores = errores, ingresado = true) }
    }

    /** Tras el primer intento, se revalida en cada tecla para que el error desaparezca al corregir. */
    private fun actualizar(cambio: (LoginUiState) -> LoginUiState) = _ui.update { actual ->
        val nuevo = cambio(actual)
        if (nuevo.intentado) nuevo.copy(errores = LoginValidator.validar(nuevo.email, nuevo.password)) else nuevo
    }
}
