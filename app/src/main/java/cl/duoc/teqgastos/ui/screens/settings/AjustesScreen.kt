package cl.duoc.teqgastos.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.teqgastos.data.settings.ModoTema
import cl.duoc.teqgastos.domain.EstadoGasto
import cl.duoc.teqgastos.ui.AppViewModelProvider
import cl.duoc.teqgastos.ui.components.EstadoChip
import cl.duoc.teqgastos.ui.components.icono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit,
    vm: AjustesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.background),
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- Perfil ----------
            ui.usuario?.let { u ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = cs.primary, contentColor = cs.onPrimary, shape = CircleShape) {
                            Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                                Text(u.iniciales, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(u.nombre, style = MaterialTheme.typography.titleMedium)
                            Text(u.email, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(u.rol.icono(), null, tint = cs.primary, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(u.rol.etiqueta, style = MaterialTheme.typography.labelLarge, color = cs.primary)
                            }
                        }
                    }
                }
            }

            // ---------- Apariencia ----------
            Seccion("Apariencia") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    ModoTema.entries.forEachIndexed { i, modo ->
                        SegmentedButton(
                            selected = ui.modoTema == modo,
                            onClick = { vm.onModoTema(modo) },
                            shape = SegmentedButtonDefaults.itemShape(index = i, count = ModoTema.entries.size),
                            icon = {
                                SegmentedButtonDefaults.Icon(active = ui.modoTema == modo) {
                                    Icon(modo.icono(), null, Modifier.size(SegmentedButtonDefaults.IconSize))
                                }
                            },
                            label = { Text(modo.etiqueta) }
                        )
                    }
                }
                Text(
                    "\"Sistema\" sigue el modo claro u oscuro configurado en el teléfono.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }

            // ---------- Leyenda de estados ----------
            Seccion("Estados de un gasto") {
                LeyendaEstados()
            }

            OutlinedButton(
                onClick = { vm.cerrarSesion(); onCerrarSesion() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }

            Text(
                "TeqGastos v0.2 · Prototipo DSY1105",
                style = MaterialTheme.typography.bodySmall,
                color = cs.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            contenido()
        }
    }
}


@Composable
private fun LeyendaEstados() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EstadoGasto.entries.forEach { e ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                EstadoChip(e, Modifier.width(118.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    e.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun ModoTema.icono(): ImageVector = when (this) {
    ModoTema.SISTEMA -> Icons.Filled.BrightnessAuto
    ModoTema.CLARO -> Icons.Filled.LightMode
    ModoTema.OSCURO -> Icons.Filled.DarkMode
}
