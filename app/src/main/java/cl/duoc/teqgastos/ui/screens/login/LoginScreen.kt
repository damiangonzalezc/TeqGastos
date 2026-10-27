package cl.duoc.teqgastos.ui.screens.login

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.teqgastos.domain.LoginValidator
import cl.duoc.teqgastos.domain.Rol
import cl.duoc.teqgastos.ui.AppViewModelProvider
import cl.duoc.teqgastos.ui.components.ValidatedTextField
import cl.duoc.teqgastos.ui.components.icono
import cl.duoc.teqgastos.ui.components.rememberShake

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onIngresar: () -> Unit,
    vm: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    var verPassword by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val shake = rememberShake(ui.intentosFallidos)
    val cs = MaterialTheme.colorScheme

    // Animación de entrada del logo
    var visible by remember { mutableStateOf(false) }
    val escalaLogo by animateFloatAsState(
        targetValue = if (visible) 1f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "logo"
    )
    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(ui.ingresado) { if (ui.ingresado) onIngresar() }
    LaunchedEffect(ui.intentosFallidos) {
        if (ui.intentosFallidos > 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cs.primaryContainer, cs.background, cs.background)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = cs.primary,
                contentColor = cs.onPrimary,
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer { scaleX = escalaLogo; scaleY = escalaLogo; alpha = escalaLogo }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, null, modifier = Modifier.size(52.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("TeqGastos", style = MaterialTheme.typography.headlineMedium, color = cs.onBackground)
            Text(
                "Rendición de gastos en terreno · TEQMED",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))

            Column(Modifier.widthIn(max = 480.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ValidatedTextField(
                    value = ui.email,
                    onValueChange = vm::onEmail,
                    label = "Correo corporativo",
                    placeholder = "nombre${LoginValidator.DOMINIO}",
                    icon = Icons.Filled.Email,
                    error = ui.errores.email,
                    mostrarValido = ui.intentado,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(12.dp))
                ValidatedTextField(
                    value = ui.password,
                    onValueChange = vm::onPassword,
                    label = "Contraseña",
                    icon = Icons.Filled.Lock,
                    error = ui.errores.password,
                    ayuda = "Mínimo 6 caracteres, con al menos un número",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { vm.ingresar() }),
                    visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        IconButton(onClick = { verPassword = !verPassword }) {
                            Icon(
                                if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (verPassword) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))
                Text(
                    "Ingresar como",
                    style = MaterialTheme.typography.labelLarge,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Rol.entries.forEachIndexed { i, rol ->
                        SegmentedButton(
                            selected = ui.rol == rol,
                            onClick = { vm.onRol(rol) },
                            shape = SegmentedButtonDefaults.itemShape(index = i, count = Rol.entries.size),
                            icon = {
                                SegmentedButtonDefaults.Icon(active = ui.rol == rol) {
                                    Icon(rol.icono(), null, Modifier.size(SegmentedButtonDefaults.IconSize))
                                }
                            },
                            label = { Text(rol.etiqueta, maxLines = 1) }
                        )
                    }
                }

                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = vm::ingresar,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .graphicsLayer { translationX = shake }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Login, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ingresar", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cs.surfaceContainerHigh, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Icon(Icons.Filled.Info, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Prototipo sin servidor: ingresa con cualquier correo ${LoginValidator.DOMINIO} " +
                            "y elige el rol que quieres probar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }
        }
    }
}
