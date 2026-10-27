package cl.duoc.teqgastos

import android.app.Application
import android.content.Context
import cl.duoc.teqgastos.data.local.AppDatabase
import cl.duoc.teqgastos.data.repository.GastoRepository
import cl.duoc.teqgastos.data.session.SessionManager
import cl.duoc.teqgastos.data.settings.SettingsRepository

/** Contenedor simple de dependencias (inyección manual). */
class AppContainer(context: Context) {
    val gastoRepository = GastoRepository(AppDatabase.obtener(context))
    val sessionManager = SessionManager(context)
    val settingsRepository = SettingsRepository(context)
}

class TeqGastosApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
