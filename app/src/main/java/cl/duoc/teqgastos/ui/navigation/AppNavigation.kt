package cl.duoc.teqgastos.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.teqgastos.ui.screens.detail.DetalleScreen
import cl.duoc.teqgastos.ui.screens.form.GastoFormScreen
import cl.duoc.teqgastos.ui.screens.home.HomeScreen
import cl.duoc.teqgastos.ui.screens.login.LoginScreen
import cl.duoc.teqgastos.ui.screens.settings.AjustesScreen

object Rutas {
    const val LOGIN = "login"
    const val HOME = "home"
    const val FORM = "gasto?id={id}"
    const val DETALLE = "detalle/{id}"
    const val AJUSTES = "ajustes"
    fun nuevoGasto() = "gasto"
    fun corregirGasto(id: Long) = "gasto?id=$id"
    fun detalle(id: Long) = "detalle/$id"
}

@Composable
fun AppNavigation(sesionActiva: Boolean, nav: NavHostController = rememberNavController()) {
    val duracion = 350

    NavHost(
        navController = nav,
        startDestination = if (sesionActiva) Rutas.HOME else Rutas.LOGIN,
        // Transiciones animadas entre todas las pantallas
        enterTransition = { slideIntoContainer(SlideDirection.Start, tween(duracion)) + fadeIn(tween(duracion)) },
        exitTransition = { fadeOut(tween(duracion)) },
        popEnterTransition = { fadeIn(tween(duracion)) },
        popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(duracion)) + fadeOut(tween(duracion)) }
    ) {
        composable(Rutas.LOGIN) {
            LoginScreen(onIngresar = {
                nav.navigate(Rutas.HOME) { popUpTo(Rutas.LOGIN) { inclusive = true } }
            })
        }
        composable(Rutas.HOME) {
            HomeScreen(
                onNuevoGasto = { nav.navigate(Rutas.nuevoGasto()) },
                onVerGasto = { id -> nav.navigate(Rutas.detalle(id)) },
                onAjustes = { nav.navigate(Rutas.AJUSTES) }
            )
        }
        composable(
            Rutas.FORM,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) {
            GastoFormScreen(onVolver = { nav.popBackStack() })
        }
        composable(
            Rutas.DETALLE,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            DetalleScreen(
                onVolver = { nav.popBackStack() },
                onCorregir = { id -> nav.navigate(Rutas.corregirGasto(id)) }
            )
        }
        composable(Rutas.AJUSTES) {
            AjustesScreen(
                onVolver = { nav.popBackStack() },
                onCerrarSesion = {
                    nav.navigate(Rutas.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
}
