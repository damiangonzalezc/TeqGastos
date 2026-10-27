package cl.duoc.teqgastos.data.session

import android.content.Context
import androidx.core.content.edit
import cl.duoc.teqgastos.domain.Rol
import cl.duoc.teqgastos.domain.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Guarda la sesión en SharedPreferences para que sobreviva al cierre de la app. */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)
    private val _usuario = MutableStateFlow(leer())
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    fun iniciar(usuario: Usuario) {
        prefs.edit {
            putString(KEY_EMAIL, usuario.email)
            putString(KEY_ROL, usuario.rol.name)
        }
        _usuario.value = usuario
    }

    fun cerrar() {
        prefs.edit { clear() }
        _usuario.value = null
    }

    private fun leer(): Usuario? {
        val email = prefs.getString(KEY_EMAIL, null) ?: return null
        val rol = prefs.getString(KEY_ROL, null)?.let { runCatching { Rol.valueOf(it) }.getOrNull() }
            ?: return null
        return Usuario(email, rol)
    }

    private companion object {
        const val KEY_EMAIL = "email"
        const val KEY_ROL = "rol"
    }
}
