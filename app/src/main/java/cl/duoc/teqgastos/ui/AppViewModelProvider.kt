package cl.duoc.teqgastos.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.duoc.teqgastos.AppContainer
import cl.duoc.teqgastos.TeqGastosApp
import cl.duoc.teqgastos.ui.screens.detail.DetalleViewModel
import cl.duoc.teqgastos.ui.screens.form.GastoFormViewModel
import cl.duoc.teqgastos.ui.screens.home.HomeViewModel
import cl.duoc.teqgastos.ui.screens.login.LoginViewModel
import cl.duoc.teqgastos.ui.screens.settings.AjustesViewModel

/** Fábrica central de ViewModels: les entrega sus dependencias desde AppContainer. */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(deps().sessionManager) }
        initializer { HomeViewModel(deps().gastoRepository, deps().sessionManager) }
        initializer { GastoFormViewModel(createSavedStateHandle(), deps().gastoRepository, deps().sessionManager) }
        initializer { DetalleViewModel(createSavedStateHandle(), deps().gastoRepository, deps().sessionManager) }
        initializer { AjustesViewModel(deps().sessionManager, deps().settingsRepository) }
    }
}

private fun CreationExtras.deps(): AppContainer = (this[APPLICATION_KEY] as TeqGastosApp).container
