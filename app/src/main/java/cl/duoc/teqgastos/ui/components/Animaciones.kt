package cl.duoc.teqgastos.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * Desplazamiento horizontal tipo "sacudida" que se dispara cada vez que
 * [disparador] cambia (por ejemplo, al fallar una validación).
 * Uso: Modifier.graphicsLayer { translationX = shake }
 */
@Composable
fun rememberShake(disparador: Int): Float {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(disparador) {
        if (disparador > 0) {
            for (x in listOf(-24f, 24f, -16f, 16f, -8f, 8f, 0f)) {
                offset.animateTo(x, tween(durationMillis = 45))
            }
        }
    }
    return offset.value
}
