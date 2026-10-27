package cl.duoc.teqgastos

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.teqgastos.data.settings.ModoTema
import cl.duoc.teqgastos.ui.navigation.AppNavigation
import cl.duoc.teqgastos.ui.theme.TeqGastosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as TeqGastosApp).container
        val sesionActiva = container.sessionManager.usuario.value != null

        setContent {
            val modo by container.settingsRepository.modoTema.collectAsStateWithLifecycle()
            val oscuro = when (modo) {
                ModoTema.SISTEMA -> isSystemInDarkTheme()
                ModoTema.CLARO -> false
                ModoTema.OSCURO -> true
            }

            // Íconos de la barra de estado claros u oscuros según el tema elegido
            DisposableEffect(oscuro) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { oscuro },
                    navigationBarStyle = SystemBarStyle.auto(SCRIM_CLARO, SCRIM_OSCURO) { oscuro }
                )
                onDispose {}
            }

            TeqGastosTheme(darkTheme = oscuro) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(sesionActiva = sesionActiva)
                }
            }
        }
    }

    private companion object {
        val SCRIM_CLARO = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
        val SCRIM_OSCURO = Color.argb(0x80, 0x1B, 0x1B, 0x1B)
    }
}
