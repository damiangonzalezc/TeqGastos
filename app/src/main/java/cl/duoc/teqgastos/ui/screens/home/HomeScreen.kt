package cl.duoc.teqgastos.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.teqgastos.domain.EstadoGasto
import cl.duoc.teqgastos.domain.FlujoAprobacion
import cl.duoc.teqgastos.domain.Gasto
import cl.duoc.teqgastos.domain.GastoValidator
import cl.duoc.teqgastos.domain.formatoClp
import cl.duoc.teqgastos.ui.AppViewModelProvider
import cl.duoc.teqgastos.ui.components.BannerEstado
import cl.duoc.teqgastos.ui.components.EstadoChip
import cl.duoc.teqgastos.ui.components.icono
import cl.duoc.teqgastos.ui.theme.TeqTheme
import cl.duoc.teqgastos.util.ShareHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNuevoGasto: () -> Unit,
    onVerGasto: (Long) -> Unit,
    onAjustes: () -> Unit,
    vm: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = {
                    Column {
                        Text(ui.titulo)
                        Text(
                            "${ui.usuario?.nombre.orEmpty()} · ${ui.rol.etiqueta}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (ui.puedeExportar) {
                        IconButton(onClick = {
                            val csv = vm.generarExportacion()
                            if (csv == null) {
                                scope.launch { snackbar.showSnackbar("No hay gastos aprobados o pagados para exportar") }
                            } else {
                                ShareHelper.compartirCsv(context, csv)
                            }
                        }) {
                            Icon(Icons.Filled.FileDownload, "Exportar a ERP")
                        }
                    }
                    IconButton(onClick = onAjustes) { Icon(Icons.Filled.Settings, "Ajustes") }
                }
            )
        },
        floatingActionButton = {
            // Solo el técnico registra gastos
            AnimatedVisibility(visible = ui.puedeRegistrar && !ui.cargando, enter = scaleIn(), exit = scaleOut()) {
                ExtendedFloatingActionButton(
                    onClick = onNuevoGasto,
                    icon = { Icon(Icons.Filled.Add, null) },
                    text = { Text("Nuevo gasto") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Resumen(ui) }

            item {
                AnimatedVisibility(
                    visible = ui.observadosPropios > 0,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    BannerEstado(
                        colores = TeqTheme.estados.observado,
                        icono = EstadoGasto.OBSERVADO.icono(),
                        titulo = "Tienes ${ui.observadosPropios} gasto(s) observado(s)",
                        detalle = "Revisa el motivo de la jefatura y corrígelos para reenviarlos.",
                        onClick = { if (ui.filtro != EstadoGasto.OBSERVADO) vm.onFiltro(EstadoGasto.OBSERVADO) }
                    )
                }
            }

            item {
                FiltrosEstado(ui = ui, onSeleccion = vm::onFiltro)
            }

            if (!ui.cargando && ui.gastosFiltrados.isEmpty()) {
                item { EstadoVacio(ui) }
            }

            items(ui.gastosFiltrados, key = { it.id }) { gasto ->
                GastoCard(
                    gasto = gasto,
                    mostrarTecnico = !ui.puedeRegistrar,
                    onClick = { onVerGasto(gasto.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun Resumen(ui: HomeUiState) {
    val (principal, secundario) = FlujoAprobacion.resumen(ui.rol)
    // Los montos "cuentan" hasta su valor cuando cambian
    val montoPrincipal by animateIntAsState(ui.total(principal.second), tween(700), label = "principal")
    val montoSecundario by animateIntAsState(ui.total(secundario.second), tween(700), label = "secundario")
    val cs = MaterialTheme.colorScheme

    Box(
        Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(cs.primary, lerp(cs.primary, cs.tertiary, 0.45f))),
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Row {
            Column(Modifier.weight(1f)) {
                Text(principal.first, color = cs.onPrimary.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
                Text(
                    formatoClp(montoPrincipal),
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onPrimary
                )
                Text(
                    "${ui.cantidad(principal.second)} gasto(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onPrimary.copy(alpha = 0.85f)
                )
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(secundario.first, color = cs.onPrimary.copy(alpha = 0.85f), style = MaterialTheme.typography.labelLarge)
                Text(
                    formatoClp(montoSecundario),
                    style = MaterialTheme.typography.titleLarge,
                    color = cs.onPrimary
                )
                Text(
                    "${ui.cantidad(secundario.second)} gasto(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onPrimary.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/** Filtros con el color de cada estado y su contador. Tocar de nuevo un filtro lo quita. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosEstado(ui: HomeUiState, onSeleccion: (EstadoGasto?) -> Unit) {
    val paleta = TeqTheme.estados
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(
                selected = ui.filtro == null,
                onClick = { onSeleccion(null) },
                label = { Text("Todos · ${ui.gastos.size}") }
            )
        }
        items(ui.estadosFiltrables) { estado ->
            val c = paleta.de(estado)
            FilterChip(
                selected = ui.filtro == estado,
                onClick = { onSeleccion(estado) },
                label = { Text("${estado.etiqueta} · ${ui.cantidad(estado)}") },
                leadingIcon = { Icon(estado.icono(), null, Modifier.size(18.dp), tint = c.acento) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = c.contenedor,
                    selectedLabelColor = c.contenido,
                    selectedLeadingIconColor = c.acento
                )
            )
        }
    }
}

@Composable
private fun GastoCard(gasto: Gasto, mostrarTecnico: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colores = TeqTheme.estados.de(gasto.estado)
    val barra by animateColorAsState(colores.acento, tween(500), label = "barraEstado")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            // Barra lateral con el color del estado
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(barra)
            )
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                ) {
                    Icon(gasto.categoria.icono(), null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        gasto.proveedor,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${gasto.categoria.etiqueta} · ${gasto.fecha.format(GastoValidator.FORMATO_FECHA)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (mostrarTecnico) {
                            Text(
                                gasto.tecnicoEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                        if (gasto.latitud != null) {
                            Icon(
                                Icons.Filled.LocationOn, "Con ubicación",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatoClp(gasto.monto), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    EstadoChip(gasto.estado)
                }
            }
        }
    }
}

@Composable
private fun EstadoVacio(ui: HomeUiState) {
    val aparecer = remember { MutableTransitionState(false).apply { targetState = true } }
    val mensaje = when {
        ui.filtro != null -> "No hay gastos en estado \"${ui.filtro.etiqueta}\""
        ui.puedeRegistrar -> "Aún no registras gastos.\nToca \"Nuevo gasto\" para comenzar."
        ui.puedeExportar -> "No hay gastos aprobados por pagar"
        else -> "No hay gastos para revisar"
    }
    AnimatedVisibility(visibleState = aparecer, enter = fadeIn(tween(600)) + scaleIn(initialScale = 0.9f)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp)
        ) {
            Icon(
                Icons.Filled.Inbox, null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            Text(
                mensaje,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
