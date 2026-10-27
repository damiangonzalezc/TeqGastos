package cl.duoc.teqgastos.data.settings

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ModoTema(val etiqueta: String) {
    SISTEMA("Sistema"),
    CLARO("Claro"),
    OSCURO("Oscuro")
}

/** Preferencias de la app (por ahora, el modo de tema), persistidas en SharedPreferences. */
class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
    private val _modoTema = MutableStateFlow(leerModo())
    val modoTema: StateFlow<ModoTema> = _modoTema.asStateFlow()

    fun cambiarModoTema(modo: ModoTema) {
        prefs.edit { putString(KEY_TEMA, modo.name) }
        _modoTema.value = modo
    }

    private fun leerModo(): ModoTema =
        prefs.getString(KEY_TEMA, null)
            ?.let { runCatching { ModoTema.valueOf(it) }.getOrNull() }
            ?: ModoTema.SISTEMA

    private companion object {
        const val KEY_TEMA = "modo_tema"
    }
}
