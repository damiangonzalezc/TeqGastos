package cl.duoc.teqgastos.ui.screens.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.teqgastos.domain.EstadoGasto
import cl.duoc.teqgastos.domain.EventoHistorial
import cl.duoc.teqgastos.domain.Gasto
import cl.duoc.teqgastos.domain.GastoValidator
import cl.duoc.teqgastos.domain.Rol
import cl.duoc.teqgastos.domain.formatoClp
import cl.duoc.teqgastos.ui.AppViewModelProvider
import cl.duoc.teqgastos.ui.components.BannerEstado
import cl.duoc.teqgastos.ui.components.EstadoChip
import cl.duoc.teqgastos.ui.components.ValidatedTextField
import cl.duoc.teqgastos.ui.components.icono
import cl.duoc.teqgastos.ui.components.rememberShake
import cl.duoc.teqgastos.ui.theme.TeqTheme
import coil.compose.AsyncImage
import java.time.format.DateTimeFormatter

private val FORMATO_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleScreen(
    onVolver: () -> Unit,
    onCorregir: (Long) -> Unit,
    vm: DetalleViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val shake = rememberShake(ui.revision.intentosFallidos)
    val paleta = TeqTheme.estados

    ui.revision.confirmando?.let { destino ->
        val c = paleta.de(destino)
        AlertDialog(
            onDismissRequest = vm::cancelarConfirmacion,
            icon = { Icon(destino.icono(), null, tint = c.acento) },
            title = { Text(if (destino == EstadoGasto.PAGADO) "¿Registrar el pago?" else "¿Rechazar el gasto?") },
            text = {
                Text(
                    if (destino == EstadoGasto.PAGADO) "El gasto quedará como pagado y listo para la contabilidad."
                    else "El técnico no podrá corregirlo y no será reembolsado."
                )
            },
            confirmButton = {
                Button(
                    onClick = vm::confirmar,
                    colors = ButtonDefaults.buttonColors(containerColor = c.acento, contentColor = c.sobreAcento)
                ) { Text(if (destino == EstadoGasto.PAGADO) "Registrar pago" else "Rechazar") }
            },
            dismissButton = { TextButton(onClick = vm::cancelarConfirmacion) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { Text("Detalle del gasto") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
        }
    ) { padding ->
        val gasto = ui.gasto ?: return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FotoComprobante(gasto)

            // Aviso según el estado actual (observado / rechazado muestran el motivo)
            BannerEstado(
                colores = paleta.de(gasto.estado),
                icono = gasto.estado.icono(),
                titulo = gasto.estado.descripcion,
                detalle = gasto.observacion?.let { "Comentario de revisión: $it" }
            )

            DatosGasto(gasto)

            AnimatedVisibility(visible = ui.puedeCorregir, enter = fadeIn() + expandVertically()) {
                Button(
                    onClick = { onCorregir(gasto.id) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Filled.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Corregir y reenviar", fontWeight = FontWeight.SemiBold)
                }
            }

            AnimatedVisibility(
                visible = ui.revision.resultado != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ui.revision.resultado?.let { r ->
                    BannerEstado(
                        colores = paleta.de(r),
                        icono = r.icono(),
                        titulo = "Gasto marcado como ${r.etiqueta.lowercase()}"
                    )
                }
            }

            // Panel de decisión: solo aparece si el rol tiene acciones sobre el estado actual
            AnimatedVisibility(visible = ui.acciones.isNotEmpty(), enter = fadeIn() + expandVertically()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.graphicsLayer { translationX = shake }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            if (ui.usuario?.rol == Rol.FINANZAS) "Registro de pago" else "Revisión de jefatura",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (ui.pideComentario) {
                            ValidatedTextField(
                                value = ui.revision.observacion,
                                onValueChange = vm::onObservacion,
                                label = "Observación",
                                placeholder = "Obligatoria para observar o rechazar",
                                icon = Icons.AutoMirrored.Filled.Comment,
                                error = ui.revision.error,
                                singleLine = false
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ui.acciones.forEach { destino ->
                                BotonDecision(destino, Modifier.weight(1f)) { vm.solicitar(destino) }
                            }
                        }
                    }
                }
            }

            Historial(ui.historial)
        }
    }
}

@Composable
private fun FotoComprobante(gasto: Gasto) {
    AsyncImage(
        model = gasto.fotoUri,
        contentDescription = "Comprobante",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    )
}

@Composable
private fun DatosGasto(gasto: Gasto) {
    val context = LocalContext.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatoClp(gasto.monto),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(gasto.estado)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Fila("Categoría", gasto.categoria.etiqueta)
            Fila("Medio de pago", gasto.medioPago.etiqueta)
            Fila("Proveedor", gasto.proveedor)
            Fila("N° comprobante", gasto.numeroComprobante)
            Fila("Fecha", gasto.fecha.format(GastoValidator.FORMATO_FECHA))
            Fila("Técnico", gasto.tecnicoEmail)

            if (gasto.latitud != null && gasto.longitud != null) {
                OutlinedButton(
                    onClick = {
                        // Abre la app de mapas del dispositivo en el punto donde se registró el gasto
                        val uri = Uri.parse("geo:${gasto.latitud},${gasto.longitud}?q=${gasto.latitud},${gasto.longitud}")
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, "No hay app de mapas instalada", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Map, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ver ubicación en el mapa")
                }
            }
        }
    }
}

@Composable
private fun Fila(etiqueta: String, valor: String) {
    Row {
        Text(
            etiqueta,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(130.dp)
        )
        Text(valor, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BotonDecision(destino: EstadoGasto, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = TeqTheme.estados.de(destino)
    val texto = when (destino) {
        EstadoGasto.APROBADO -> "Aprobar"
        EstadoGasto.OBSERVADO -> "Observar"
        EstadoGasto.RECHAZADO -> "Rechazar"
        EstadoGasto.PAGADO -> "Registrar pago"
        EstadoGasto.PENDIENTE -> "Reabrir"
    }
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = c.acento, contentColor = c.sobreAcento),
        contentPadding = ButtonDefaults.TextButtonContentPadding,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(48.dp)
    ) {
        Icon(destino.icono(), null, Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(texto, maxLines = 1)
    }
}

/** Línea de tiempo de auditoría: cada punto tiene el color de su estado. */
@Composable
private fun Historial(eventos: List<EventoHistorial>) {
    if (eventos.isEmpty()) return
    val paleta = TeqTheme.estados
    Column {
        Text("Historial", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        eventos.forEachIndexed { i, e ->
            val c = paleta.de(e.estado)
            Row(Modifier.height(IntrinsicSize.Min)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(22.dp)
                            .background(c.contenedor, CircleShape)
                    ) {
                        Icon(e.estado.icono(), null, tint = c.acento, modifier = Modifier.size(14.dp))
                    }
                    if (i < eventos.lastIndex) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.padding(bottom = 16.dp)) {
                    Text(e.estado.etiqueta, fontWeight = FontWeight.SemiBold, color = c.acento)
                    Text(
                        "${e.usuarioEmail} · ${e.fecha.format(FORMATO_HORA)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    e.comentario?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }
}
