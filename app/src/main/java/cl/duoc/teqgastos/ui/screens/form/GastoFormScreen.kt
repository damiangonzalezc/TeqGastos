package cl.duoc.teqgastos.ui.screens.form

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.teqgastos.domain.Categoria
import cl.duoc.teqgastos.domain.EstadoGasto
import cl.duoc.teqgastos.domain.GastoInput
import cl.duoc.teqgastos.domain.GastoValidator
import cl.duoc.teqgastos.domain.MedioPago
import cl.duoc.teqgastos.domain.formatoClp
import cl.duoc.teqgastos.ui.AppViewModelProvider
import cl.duoc.teqgastos.ui.components.BannerEstado
import cl.duoc.teqgastos.ui.components.MensajeError
import cl.duoc.teqgastos.ui.components.ValidatedTextField
import cl.duoc.teqgastos.ui.components.icono
import cl.duoc.teqgastos.ui.components.rememberShake
import cl.duoc.teqgastos.ui.theme.TeqTheme
import cl.duoc.teqgastos.util.ImageStorage
import cl.duoc.teqgastos.util.LocationHelper
import coil.compose.AsyncImage
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastoFormScreen(
    onVolver: () -> Unit,
    vm: GastoFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val shake = rememberShake(ui.intentosFallidos)
    var mostrarCalendario by rememberSaveable { mutableStateOf(false) }

    // ---------- Recurso nativo 1: cámara ----------
    var uriPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) uriPendiente?.let(vm::onFotoTomada)
    }
    val lanzarCamara = {
        val uri = ImageStorage.crearUriComprobante(context)
        uriPendiente = uri.toString()
        tomarFoto.launch(uri)
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) lanzarCamara()
        else Toast.makeText(context, "Se necesita la cámara para fotografiar el comprobante", Toast.LENGTH_LONG).show()
    }
    val abrirCamara = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            lanzarCamara()
        } else {
            permisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    // ---------- Recurso nativo 2: GPS ----------
    val buscarUbicacion = {
        vm.onBuscandoUbicacion()
        LocationHelper.obtenerUbicacion(context) { loc -> vm.onUbicacion(loc?.latitude, loc?.longitude) }
    }
    val permisoUbicacion = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) buscarUbicacion() else vm.onPermisoUbicacionDenegado()
    }

    LaunchedEffect(ui.guardado) {
        if (ui.guardado) {
            val msg = if (ui.esEdicion) "Gasto corregido y reenviado a revisión" else "Gasto registrado y enviado a aprobación"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onVolver()
        }
    }
    LaunchedEffect(ui.intentosFallidos) {
        if (ui.intentosFallidos > 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    if (mostrarCalendario) {
        val estadoFecha = rememberDatePickerState(
            initialSelectedDateMillis = GastoValidator.parsearFecha(ui.fecha)
                ?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoFecha.selectedDateMillis?.let {
                        vm.onFechaSeleccionada(Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    mostrarCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = estadoFecha)
        }
    }

    // Barra de avance del formulario, animada
    val progreso by animateFloatAsState(ui.progreso, tween(400), label = "progreso")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = { Text(if (ui.esEdicion) "Corregir gasto" else "Nuevo gasto") },
                    navigationIcon = {
                        IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                    }
                )
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "${ui.aInput().camposCompletos} de ${GastoInput.TOTAL_CAMPOS} datos obligatorios completos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (ui.esEdicion && ui.observacionJefatura != null) {
                BannerEstado(
                    colores = TeqTheme.estados.observado,
                    icono = EstadoGasto.OBSERVADO.icono(),
                    titulo = "Observación de jefatura",
                    detalle = ui.observacionJefatura
                )
            }

            SelectorCategoria(ui.categoria, ui.errores.categoria, vm::onCategoria)
            SelectorMedioPago(ui.medioPago, ui.errores.medioPago, vm::onMedioPago)

            ValidatedTextField(
                value = ui.monto,
                onValueChange = vm::onMonto,
                label = "Monto (CLP)",
                icon = Icons.Filled.AttachMoney,
                error = ui.errores.monto,
                mostrarValido = ui.intentado,
                ayuda = ui.categoria?.let { "Tope para ${it.etiqueta}: ${formatoClp(it.tope)}" },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            ValidatedTextField(
                value = ui.fecha,
                onValueChange = vm::onFecha,
                label = "Fecha del gasto",
                placeholder = "dd/mm/aaaa",
                icon = Icons.Filled.CalendarToday,
                error = ui.errores.fecha,
                ayuda = "Plazo máximo de rendición: ${GastoValidator.PLAZO_DIAS} días",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                trailing = {
                    IconButton(onClick = { mostrarCalendario = true }) {
                        Icon(
                            Icons.Filled.CalendarMonth, "Elegir fecha",
                            tint = if (ui.errores.fecha != null) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
            ValidatedTextField(
                value = ui.proveedor,
                onValueChange = vm::onProveedor,
                label = "Proveedor",
                placeholder = "Ej: Copec Ruta 160",
                icon = Icons.Filled.Store,
                error = ui.errores.proveedor,
                mostrarValido = ui.intentado,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            ValidatedTextField(
                value = ui.numeroComprobante,
                onValueChange = vm::onNumero,
                label = "N° boleta / factura",
                icon = Icons.Filled.Receipt,
                error = ui.errores.numeroComprobante,
                mostrarValido = ui.intentado,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )

            TarjetaFoto(ui.fotoUri, ui.errores.foto, onTomarFoto = abrirCamara)

            TarjetaUbicacion(
                ui = ui,
                onRegistrar = {
                    permisoUbicacion.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                },
                onQuitar = vm::quitarUbicacion
            )

            AnimatedVisibility(visible = ui.errores.hayErrores) {
                Text(
                    "Revisa ${ui.errores.cantidad} campo(s) marcados en rojo",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = vm::guardar,
                enabled = !ui.guardando,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .graphicsLayer { translationX = shake }
            ) {
                AnimatedContent(targetState = ui.guardando, label = "botonGuardar") { guardando ->
                    if (guardando) {
                        CircularProgressIndicator(
                            Modifier.size(22.dp), strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (ui.esEdicion) Icons.AutoMirrored.Filled.Send else Icons.Filled.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (ui.esEdicion) "Reenviar a revisión" else "Guardar y enviar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectorCategoria(seleccionada: Categoria?, error: String?, onSeleccion: (Categoria) -> Unit) {
    Column {
        Text("Categoría", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Categoria.entries.forEach { c ->
                FilterChip(
                    selected = c == seleccionada,
                    onClick = { onSeleccion(c) },
                    label = { Text(c.etiqueta) },
                    leadingIcon = { Icon(c.icono(), null, Modifier.size(18.dp)) }
                )
            }
        }
        MensajeError(error)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectorMedioPago(seleccionado: MedioPago?, error: String?, onSeleccion: (MedioPago) -> Unit) {
    Column(Modifier.animateContentSize()) {
        Text("Medio de pago", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MedioPago.entries.forEach { m ->
                FilterChip(
                    selected = m == seleccionado,
                    onClick = { onSeleccion(m) },
                    label = { Text(m.etiqueta) },
                    leadingIcon = { Icon(m.icono(), null, Modifier.size(18.dp)) }
                )
            }
        }
        if (seleccionado != null && error == null) {
            Text(
                seleccionado.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
        MensajeError(error)
    }
}

@Composable
private fun TarjetaFoto(fotoUri: String?, error: String?, onTomarFoto: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = if (error != null) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error) else null,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Foto del comprobante", style = MaterialTheme.typography.titleSmall)
            AnimatedVisibility(visible = fotoUri != null, enter = scaleIn() + fadeIn()) {
                AsyncImage(
                    model = fotoUri,
                    contentDescription = "Comprobante fotografiado",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onTomarFoto, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.CameraAlt, null)
                Spacer(Modifier.width(8.dp))
                Text(if (fotoUri == null) "Tomar foto" else "Volver a tomar")
            }
            MensajeError(error)
        }
    }
}

@Composable
private fun TarjetaUbicacion(ui: GastoFormUiState, onRegistrar: () -> Unit, onQuitar: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Ubicación del gasto (opcional)", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            AnimatedContent(
                targetState = Triple(ui.buscandoUbicacion, ui.latitud, ui.longitud),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ubicacion"
            ) { (buscando, lat, lon) ->
                when {
                    buscando -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Obteniendo ubicación…")
                    }
                    lat != null && lon != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = TeqTheme.estados.aprobado.acento)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            String.format(Locale.US, "%.5f, %.5f", lat, lon),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onQuitar) { Icon(Icons.Filled.Close, "Quitar ubicación") }
                    }
                    else -> OutlinedButton(onClick = onRegistrar, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.MyLocation, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Registrar ubicación actual")
                    }
                }
            }
            ui.mensajeUbicacion?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
